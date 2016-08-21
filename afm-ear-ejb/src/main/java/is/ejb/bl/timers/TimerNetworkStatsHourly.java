package is.ejb.bl.timers;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.server.ServerStats;
import is.ejb.bl.monitoring.server.ServerStatusMonitor;
import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAONetworkStatsHourly;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.NetworkStatsHourlyEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.PropertyEntity;
import is.ejb.dl.entities.RealmEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
public class TimerNetworkStatsHourly {
	
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	//http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
    @Resource
    TimerService timerService;

	@Inject 
	private DAOProperty daoProperty;

	@Inject
	private DAONetworkStatsHourly daoNetworkStatsHourly;

	@Inject
	private DAOUserEvent daoUserEvent;

	//offer wall generator
    private Timer timer = null;
    //read monitoring interval from system configuration 
    private int interval = 1; //in h

    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster
    
    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			//Thread.sleep(10000);
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_NETWORK_STATS_HOURLY, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
			    //expression.second("*/"+ analyticsCPEMonitoringInterval +"/").minute("*").hour("*");
			    expression.minute("0").hour("*/"+ interval +"/"); //for hour in production
			    //expression.minute("*").minute("*/"+ interval +"/").hour("*"); //for minutes during testing
			    //using calendar expression
		        timer = timerService.createCalendarTimer(expression,tc);
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

        if(timer.getInfo().equals(TimerType.TIMER_NETWORK_STATS_HOURLY)) {
	        logger.info("!!!!----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());
	
			try {
    			List<RealmEntity> listRealms = daoRealm.findAll();
    			Iterator i = listRealms.iterator();
    			while(i.hasNext()) {
    				RealmEntity realm = (RealmEntity)i.next();
    				try {
    					//get last entry in hourly network stats
    					NetworkStatsHourlyEntity lastStats = daoNetworkStatsHourly.getLastEntry();
    					if(lastStats == null) { //this is the first element
    						//Timestamp startTimestamp = new Timestamp(System.currentTimeMillis()-1000*60); //minute for testing
    						//Timestamp endTimestamp = new Timestamp(System.currentTimeMillis()); //minute for testing

    						Timestamp startTimestamp = new Timestamp(System.currentTimeMillis()-1000*3600); //minute for production
    						Timestamp endTimestamp = new Timestamp(System.currentTimeMillis()); //minute for production
    						
    				    	Application.getElasticSearchLogger().indexLog(Application.GENERIC_NETWORK_STATS_HOURLY, realm.getId(), 
    				    			LogStatus.OK, 
    				    			Application.GENERIC_NETWORK_STATS_HOURLY+" TIMER triggering first stats generation process for period: "+startTimestamp.toString()+" - "+endTimestamp.toString());

    				    	generateStats(startTimestamp, endTimestamp, true, realm.getId());
    					} else {
    						Timestamp startTimestamp = lastStats.getGenerationEndDate();
    						Timestamp endTimestamp = new Timestamp(System.currentTimeMillis()); //minute for testing
    						//Timestamp endTimestamp = new Timestamp(startTimestamp.getTime()+1000*3600); //minute for testing
    				    	Application.getElasticSearchLogger().indexLog(Application.GENERIC_NETWORK_STATS_HOURLY, realm.getId(), 
    				    			LogStatus.OK, 
    				    			Application.GENERIC_NETWORK_STATS_HOURLY+" TIMER triggering stats generation process for period: "+startTimestamp.toString()+" - "+endTimestamp.toString());
    				    	generateStats(startTimestamp, endTimestamp, true, realm.getId());
    					}
            		} catch(Exception exc) {
            			exc.printStackTrace();
            			logger.severe(exc.toString());
            			Application.getElasticSearchLogger().indexLog(Application.GENERIC_NETWORK_STATS_HOURLY, -1, 
            					LogStatus.ERROR, 
            					Application.GENERIC_NETWORK_STATS_HOURLY+" TIMER error: "+exc.toString());
            		}
    			}
			} catch(Exception exc) {
				exc.printStackTrace();
				logger.severe(exc.toString());
				Application.getElasticSearchLogger().indexLog(Application.GENERIC_NETWORK_STATS_HOURLY, -1, 
						LogStatus.ERROR, 
						Application.GENERIC_NETWORK_STATS_HOURLY+" TIMER error: "+exc.toString());
			}
        }
    }
    
    private NetworkStatsHourlyEntity generateStats(Timestamp startDate, Timestamp endDate, boolean approved, int realmId){
		//generate stats
		double payoutSum = round(daoUserEvent.getSumPayout(startDate, endDate, approved, realmId),2);
		double profitSum = round(daoUserEvent.getSumProfit(startDate, endDate, approved, realmId),2); 
		double rewardSum = round(daoUserEvent.getSumReward(startDate, endDate, approved, realmId),2);
		
		String payoutIsoCurrencyCode = "KSH"; //this in future needs to be more flexible
		int clicksSum = daoUserEvent.getClicksCount(startDate, endDate, realmId);
		int conversionsSum = daoUserEvent.getConversionsCount(startDate, endDate, approved, realmId);

		NetworkStatsHourlyEntity newStats = new NetworkStatsHourlyEntity();
		newStats.setPayout(payoutSum);
		newStats.setProfit(profitSum);
		newStats.setReward(rewardSum);
		newStats.setPayoutIsoCurrencyCode(payoutIsoCurrencyCode);
		newStats.setClicks(clicksSum);
		newStats.setConversions(conversionsSum);
		newStats.setGenerationStartDate(startDate);
		newStats.setGenerationEndDate(endDate);
		newStats.setRealmId(realmId);
		//generate stats
		daoNetworkStatsHourly.create(newStats);

		return newStats;
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
							"MASTER_ELECTION "+TimerType.TIMER_NETWORK_STATS_HOURLY+" AB system detected host address: "+addr.getHostAddress());
					if(addr.getHostAddress().equals(masterServerIp)) {
						logger.info("MASTER_ELECTION SELECTED "+TimerType.TIMER_NETWORK_STATS_HOURLY+" server with IP: "+masterServerIp+" elected as a master server");
						Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
								LogStatus.OK, 
								"MASTER_ELECTION SELECTED "+TimerType.TIMER_NETWORK_STATS_HOURLY+" server with IP: "+masterServerIp+" elected as a master server");
						return true;
					}
	            }
	        }
	 	} catch (SocketException e1) {
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
					LogStatus.ERROR, 
					"MASTER_ELECTION "+TimerType.TIMER_NETWORK_STATS_HOURLY+" error during master server election: "+e1.toString());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
    	return false;
    }
    
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

}