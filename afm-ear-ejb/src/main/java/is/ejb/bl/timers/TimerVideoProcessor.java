package is.ejb.bl.timers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferCommand;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.crf.CRFManager;
import is.ejb.bl.monitoring.server.ServerStats;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerProviders.quidco.QuidcoTransactionReader;
import is.ejb.bl.offerProviders.snapdeal.SerDeSnapdealProviderConfiguration;
import is.ejb.bl.offerProviders.snapdeal.SnapdealAPIManager;
import is.ejb.bl.offerProviders.snapdeal.SnapdealProviderConfig;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.bl.video.VideoManager;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.BlockedOffersEntity;
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
import java.net.UnknownHostException;
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
import javax.ejb.Lock;
import javax.ejb.LockType;
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

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

//http://docs.oracle.com/javaee/6/tutorial/doc/gipvi.html
//@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@Startup
public class TimerVideoProcessor {

	public static boolean busy = false;
	
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	//http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
    @Resource
    TimerService timerService;

	@Inject 
	private DAOProperty daoProperty;

    private Timer timer = null;
    private int triggerInterval = 24; //in h
    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster
    
   @Inject
   private VideoManager videoManager;
	
    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			//Thread.sleep(10000);
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			triggerInterval = 20;
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_VIDEO_PROCESSOR, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
			    expression.second("*/10").minute("*").hour("*");
			    //using calendar expression
			    timer = timerService.createCalendarTimer(expression,tc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
    }

    //http://stackoverflow.com/questions/14441366/error-invoking-timeout-for-timer-could-not-obtain-lock-within-5minutes-at-ejb
    @Timeout
    @AccessTimeout(value = 60, unit = TimeUnit.MINUTES)
    @Lock(LockType.READ)
    public void timeout(Timer timer) {
        logger.info("----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

        if(timer.getInfo().equals(TimerType.TIMER_VIDEO_PROCESSOR)) {
    		try {
    	        RealmEntity realmEntity = daoRealm.findByName("BPM");
    	        if(realmEntity != null ) {
    				Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, 
    						LogStatus.OK, 
    						Application.VIDEO_REWARD_ACTIVITY+" TIMER triggered - processing video");
    				videoManager.processData();
    				//quidcoTransactionReader.loadTransactions();
    	        } else {
    				Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, 
    						LogStatus.OK, 
    						Application.VIDEO_REWARD_ACTIVITY+" NOT PROCESSING TIMER triggered - disabled");
    	        }
    	        
    		} catch(Exception exc) {
    			exc.printStackTrace();
    			logger.severe(exc.toString());
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO, -1, 
						LogStatus.ERROR, 
						Application.QUIDCO_GET_DELTA+" TIMER error: "+exc.toString());
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