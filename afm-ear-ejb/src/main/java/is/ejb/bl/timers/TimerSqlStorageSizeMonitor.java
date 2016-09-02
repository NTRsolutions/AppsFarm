package is.ejb.bl.timers;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.server.ServerStats;

import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.PropertyEntity;
import is.ejb.dl.entities.RealmEntity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
public class TimerSqlStorageSizeMonitor {
	
	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOOffer daoOffer;


	//http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
    @Resource
    TimerService timerService;

	@Inject 
	private DAOProperty daoProperty;

    private Timer sqlStorageSizeMonitorTimer = null;
    //read monitoring interval from system configuration 
    private int sqlStorageSizeMonitorInterval = 15; //in mins
    private int sqlDataStorageHistoryLength = 14; //in days

    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster

    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			//Thread.sleep(20000);
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			sqlStorageSizeMonitorInterval = getPropertyInt("sqlStorageSizeMonitoringIntervals", "15");
			sqlDataStorageHistoryLength = getPropertyInt("sqlDataStorageHistoryLength", "14");
			
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_SQL_DATA_SIZE_MONITORING, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
			    //expression.second("*/"+ analyticsCPEMonitoringInterval +"/").minute("*").hour("*");
			    expression.minute("*").minute("*/"+ sqlStorageSizeMonitorInterval +"/").hour("*");
			    //using calendar expression
		        sqlStorageSizeMonitorTimer = timerService.createCalendarTimer(expression,tc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
    }

    //http://stackoverflow.com/questions/14441366/error-invoking-timeout-for-timer-could-not-obtain-lock-within-5minutes-at-ejb
    @Timeout
    @AccessTimeout(value = 20, unit = TimeUnit.MINUTES)
    public void timeout(Timer timer) {

        if(timer.getInfo().equals(TimerType.TIMER_SQL_DATA_SIZE_MONITORING)) {
        	if(Application.isSQLDataSizeMonitorEnabled()) {
                logger.info("!!!!----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());
                
        		int numberOfHistoryDays = sqlDataStorageHistoryLength;
        		Calendar c = Calendar.getInstance();
        		c.add(Calendar.DAY_OF_MONTH, -numberOfHistoryDays);
        		Date dateStart = new Date();
        		dateStart.setTime(c.getTime().getTime());

        		Timestamp tresholdDay = new Timestamp(dateStart.getTime()); //in days
                //Timestamp tresholdDay = new Timestamp(System.currentTimeMillis() - 3600*24*1000*sqlDataStorageHistoryLength); //in days
                //Timestamp tresholdDay = new Timestamp(System.currentTimeMillis() - 60*1*1000*sqlDataStorageHistoryLength); //in minutes (for testing)
            	Application.getElasticSearchLogger().indexLog(Application.SQL_MONITORING_ACTIVITY, -1, 
            			LogStatus.OK, "SQL_DATA_SIZE_MONITORING TIMER triggering monitoring process - removing data that is older than: "+sqlDataStorageHistoryLength+ " days (older than: "+tresholdDay.toString());
        		try {
                    int numberOfRemovedRecords = daoUserEvent.deleteHistoryData(tresholdDay);
                	Application.getElasticSearchLogger().indexLog(Application.SQL_MONITORING_ACTIVITY, -1, 
                			LogStatus.OK, 
                			Application.SQL_MONITORING_ACTIVITY+" TIMER TRANSACTIONS_REMOVAL process finished, number of removed records: "+numberOfRemovedRecords);
                    numberOfRemovedRecords = daoOffer.deleteHistoryData(tresholdDay);
                	Application.getElasticSearchLogger().indexLog(Application.SQL_MONITORING_ACTIVITY, -1, 
                			LogStatus.OK, 
                			Application.SQL_MONITORING_ACTIVITY+" TIMER OFFERS_REMOVAL process finished, number of removed records: "+numberOfRemovedRecords);

        		} catch(Exception exc) {
        			exc.printStackTrace();
        			logger.severe(exc.toString());
        			Application.getElasticSearchLogger().indexLog(Application.SQL_MONITORING_ACTIVITY, -1, 
        					LogStatus.ERROR, 
        					Application.SQL_MONITORING_ACTIVITY+" TIMER error generating offer walls, error: "+exc.toString());
        		}
            } else {
            	Application.getElasticSearchLogger().indexLog(Application.SQL_MONITORING_ACTIVITY, -1, 
            			LogStatus.OK, 
            			Application.SQL_MONITORING_ACTIVITY+" TIMER service disabled");
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
							"MASTER_ELECTION "+TimerType.TIMER_SQL_DATA_SIZE_MONITORING+" detected host address: "+addr.getHostAddress());
					if(addr.getHostAddress().equals(masterServerIp)) {
						logger.info("MASTER_ELECTION SELECTED "+TimerType.TIMER_SQL_DATA_SIZE_MONITORING+" server with IP: "+masterServerIp+" elected as a master server");
						Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
								LogStatus.OK, 
								"MASTER_ELECTION SELECTED "+TimerType.TIMER_SQL_DATA_SIZE_MONITORING+" server with IP: "+masterServerIp+" elected as a master server");
						return true;
					}
	            }
	        }
	 	} catch (SocketException e1) {
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
					LogStatus.ERROR, 
					"MASTER_ELECTION "+TimerType.TIMER_SQL_DATA_SIZE_MONITORING+" error during master server election: "+e1.toString());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
    	return false;
    }
}