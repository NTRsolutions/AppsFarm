package is.ejb.bl.timers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.monitoring.server.ServerStats;

import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.PropertyEntity;
import is.ejb.dl.entities.RealmEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

//http://docs.oracle.com/javaee/6/tutorial/doc/gipvi.html
//@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@Startup
public class TimerOfferWallsGenerator {
	
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOOfferWall daoOfferWall;
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/OfferWallGenerationQueue")
	private Queue queue;

	//http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
    @Resource
    TimerService timerService;

	@Inject 
	private DAOProperty daoProperty;

	@Inject
	private DAOServerStats daoServerStats;

    private Timer analyticsOfferWallGenerationTimer = null;
    private int analyticsOfferWallGenerationInterval = 24; //in h
    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster
    
    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			//Thread.sleep(10000);
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			analyticsOfferWallGenerationInterval = getPropertyInt("offerGenerationIntervals", "24");
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_OFFER_WALL_GENERATION, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
			    //expression.second("*/"+ analyticsCPEMonitoringInterval +"/").minute("*").hour("*");
			    expression.minute("*").minute("*/"+ analyticsOfferWallGenerationInterval +"/").hour("*");
			    //using calendar expression
		        analyticsOfferWallGenerationTimer = timerService.createCalendarTimer(expression,tc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
    }

    //system status collection
    int systemStatusMonitoringCounter = 0;

    //http://stackoverflow.com/questions/14441366/error-invoking-timeout-for-timer-could-not-obtain-lock-within-5minutes-at-ejb
    @Timeout
    @AccessTimeout(value = 20, unit = TimeUnit.MINUTES)
    public void timeout(Timer timer) {
        logger.info("----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

        if(timer.getInfo().equals(TimerType.TIMER_OFFER_WALL_GENERATION)) {
        	if(Application.getGenerateOffers()) {
                logger.info("!!!!----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

            	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
            			LogStatus.OK, 
            			Application.OFFER_WALL_GENERATION_ACTIVITY+" TIMER triggering offer wall generation process");
        		try {
        			ArrayList<OfferWallEntity> listOfferWallsToGenerate = new ArrayList<OfferWallEntity>();
        			List<RealmEntity> listRealms = daoRealm.findAll();
        			Iterator i = listRealms.iterator();
        			OfferWallEntity offerWall = null;

        			while(i.hasNext()) {
        				RealmEntity realm = (RealmEntity)i.next();
        				try {
        					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, realm.getId(), 
        							LogStatus.OK, 
        							Application.OFFER_WALL_GENERATION_ACTIVITY+" TIMER generating offer walls for realm: "+realm.getName()+" id: "+realm.getId());
        					
        					//logger.info("generating offer walls for realm: "+realm.getName());
        					List<OfferWallEntity> listOfferWalls = daoOfferWall.findAllByRealmId(realm.getId());
        					Iterator it = listOfferWalls.iterator();
        					while(it.hasNext()) {
        						offerWall = (OfferWallEntity)it.next();
        						if(offerWall.isActive()) { //process only active offer walls
        							listOfferWallsToGenerate.add(offerWall);
        						}
        					}
        				} catch(Exception exc) {
        					exc.printStackTrace();
        					logger.severe(exc.toString());
        					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
        							LogStatus.ERROR, 
        							Application.OFFER_WALL_GENERATION_ACTIVITY+" TIMER error generating offer walls for realm: "+realm.getName()+" id: "+realm.getId()+" error: "+exc.toString());
        				}
        			}
        			
        			//dispatch list of offerwalls to jms queue
                	dispatchOfferWallGenerationRequestToJMS(listOfferWallsToGenerate);
        		} catch(Exception exc) {
        			exc.printStackTrace();
        			logger.severe(exc.toString());
        			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
        					LogStatus.ERROR, 
        					Application.OFFER_WALL_GENERATION_ACTIVITY+" TIMER error generating offer walls, error: "+exc.toString());
        		}
            } else {
            	Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
            			LogStatus.OK, 
            			Application.OFFER_WALL_GENERATION_ACTIVITY+" TIMER offer wall generation disabled");
            }
        }
    }
    
    private void dispatchOfferWallGenerationRequestToJMS(ArrayList<OfferWallEntity> listOfferWallsToGenerate)
    {
		Connection connection = null;
		
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(queue);
			connection.start();
			for(int i=0;i<listOfferWallsToGenerate.size();i++) {
				ObjectMessage message = session.createObjectMessage();
				message.setObject(listOfferWallsToGenerate.get(i));
				messageProducer.send(message);
			}
		} catch (JMSException e) {
            logger.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
    }
     
    //this is used to read monitoring interval parameter from db (cannot do it from Application class as it is load later than this one)
    private int getPropertyInt(String name, String defvalue) {
        String p = getProperty(name, defvalue);
        try {
            if (p == null || p.equals("")) {
                return Integer.parseInt(defvalue);
            }
            return Integer.parseInt(p);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getProperty(String name, String defvalue) {
        try {
            PropertyEntity pl = daoProperty.findByPK(0, PropertyEntity.TYPE_APPLICATION, name);
            return pl.getValue();
        } catch (Exception ex) {
            return defvalue;
        }
    }

    private boolean isMasterServer(String masterServerIp) {
        Enumeration<NetworkInterface> n;
		try {
			n = NetworkInterface.getNetworkInterfaces();
	        for (; n.hasMoreElements();)
	        {
	            NetworkInterface e = n.nextElement();
	            Enumeration<InetAddress> a = e.getInetAddresses();
	            for (; a.hasMoreElements();)
	            {
	                InetAddress addr = a.nextElement();
	                logger.info("detected host address:  " + addr.getHostAddress());
					Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
							LogStatus.OK, 
							"MASTER_ELECTION "+TimerType.TIMER_OFFER_WALL_GENERATION+" detected host address: "+addr.getHostAddress());
					if(addr.getHostAddress().equals(masterServerIp)) {
						logger.info("MASTER_ELECTION SELECTED "+TimerType.TIMER_OFFER_WALL_GENERATION+" server with IP: "+masterServerIp+" elected as a master server");
						Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
								LogStatus.OK, 
								"MASTER_ELECTION SELECTED "+TimerType.TIMER_OFFER_WALL_GENERATION+" server with IP: "+masterServerIp+" elected as a master server");
						return true;
					}
	            }
	        }
	 	} catch (SocketException e1) {
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
					LogStatus.ERROR, 
					"MASTER_ELECTION "+TimerType.TIMER_OFFER_WALL_GENERATION+" error during master server election: "+e1.toString());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
    	return false;
    }
}