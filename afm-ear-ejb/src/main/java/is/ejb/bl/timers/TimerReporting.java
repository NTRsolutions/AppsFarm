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
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.reporting.ReportDH;
import is.ejb.bl.reporting.ReportPeriodName;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.dao.DAOUserEvent;
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
public class TimerReporting {

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
	private MailManager managerMail;

	@Inject
	private SpinnerManager spinnerManager;
	
	@Inject
	private DAOUserEvent daoUserEvent;
	
	@Inject
	private DAOInvitation daoInvitation;
	
	private ReportingManager managerReporting = null;
	
    private Timer timer = null;
    private int triggerInterval = 24; //in h
    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster

    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			//Thread.sleep(10000);
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			triggerInterval = getPropertyInt("reportingIntervals", "24");
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_REPORTING, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
		        //expression.minute("0").hour("*/"+ triggerInterval +"/");
		        //triggerInterval = 5;
		        expression.minute("*").minute("*/"+ triggerInterval +"/").hour("*");
		        //expression.second("*/60").minute("*").hour("*");
			    //using calendar expression
			    timer = timerService.createCalendarTimer(expression,tc);
			    managerReporting = new ReportingManager(Application.getLogServerAddress(),
			    				Application.getLogServerName(),spinnerManager,daoUserEvent,daoInvitation);
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
        logger.info("----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

        if(timer.getInfo().equals(TimerType.TIMER_REPORTING)) {
        	if(Application.isReportingEnabled() == false || Application.isReportingEnabled() == true) {
                logger.info("!!!!----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

            	Application.getElasticSearchLogger().indexLog(Application.REPORTING_TIMER_TRIGGERED, -1, 
            			LogStatus.OK, 
            			Application.REPORTING_TIMER_TRIGGERED+" TIMER REPORTING");
        		try {
        			List<RealmEntity> listRealms = daoRealm.findAll();
        			Iterator i = listRealms.iterator();

        			while(i.hasNext()) {
        				RealmEntity realm = (RealmEntity)i.next();
        				try {
        					if(realm.isReportingEnabled()) {
            					Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(), 
            							LogStatus.OK, 
            							Application.REPORTING_ACTIVITY_TRIGGERED+" triggering for realm: "+realm.getName()+" id: "+realm.getId());

            					//india
            					String rewardType = "AirRewardz-India";
            					logger.info("triggering reporting for realm: "+realm.getName()+" rewardType: "+rewardType);
            					ArrayList<ReportDH> listReports = generateReports(realm, rewardType);

            					//south africa
            					rewardType = "AirRewardz-SouthAfrica";
            					logger.info("triggering reporting for realm: "+realm.getName()+" rewardType: "+rewardType);
            					ArrayList<ReportDH> listNewReports = generateReports(realm, rewardType);
            					for(int r=0;r<listNewReports.size();r++) {
            						listReports.add(listNewReports.get(r));
            					}

            					//kenya
            					rewardType = "AirRewardz-Kenya";
            					logger.info("triggering reporting for realm: "+realm.getName()+" rewardType: "+rewardType);
            					listNewReports = generateReports(realm, rewardType);
            					for(int r=0;r<listNewReports.size();r++) {
            						listReports.add(listNewReports.get(r));
            					}

            					//--------------------------- send e-mail
            					if(realm.getReportingEmails() != null && realm.getReportingEmails().length()>0) {
                					MailParamsHolder mailParamsHolder = new MailParamsHolder();
                					mailParamsHolder.setEmailRecipientAddress(realm.getReportingEmails());
                					mailParamsHolder.setReports(listReports);
                					//managerMail.sendEmail(realm, mailParamsHolder, EmailType.REPORTING);
            					}
        					} else {
            					Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(), 
            							LogStatus.OK, 
            							Application.REPORTING_ACTIVITY_DISABLED+" for realm: "+realm.getName()+" id: "+realm.getId());
        					}
        				} catch(Exception exc) {
        					exc.printStackTrace();
        					logger.severe(exc.toString());
        					Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(), 
        							LogStatus.ERROR, 
        							Application.REPORTING_ACTIVITY_TRIGGERED+" TIMER error triggering for realm: "+realm.getName()+" id: "+realm.getId()+" error: "+exc.toString());
        				}
        			}
        			
        		} catch(Exception exc) {
        			exc.printStackTrace();
        			logger.severe(exc.toString());
					Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, -1, 
							LogStatus.ERROR, 
							Application.REPORTING_ACTIVITY+" TIMER error: "+exc.toString());
        		}
            } else {
            	Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, -1, 
            			LogStatus.OK, 
            			Application.REPORTING_ACTIVITY+" TIMER disabled");
            }
        }
    }
    
    private ArrayList<ReportDH> generateReports(RealmEntity realm, 
    		String rewardType) {
		ArrayList<ReportDH> listReports = new ArrayList<ReportDH>();

		Date dateEnd = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, -7);
		Date dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		ReportDH reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_HOUR,
				"1 week",
				dateStart, dateEnd,
				rewardType,
				realm.getName());
		listReports.add(reportDH);
		
		
	    dateEnd = dateStart;
		c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, -14);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_HOUR,
				"2 week",
				dateStart, dateEnd,
				rewardType,
				realm.getName());
		listReports.add(reportDH);
		
		
		dateEnd = dateStart;
		c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, -21);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_HOUR,
				"3 week",
				dateStart, dateEnd,
				rewardType,
				realm.getName());
		listReports.add(reportDH);
		
		
		dateEnd = dateStart;
		c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, -28);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_HOUR,
				"4 week",
				dateStart, dateEnd,
				rewardType,
				realm.getName());
		listReports.add(reportDH);
		
		
		//get 1h report
		/*Date dateEnd = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.HOUR_OF_DAY, -1);
		Date dateStart = new Date();
		
		dateStart.setTime(c.getTime().getTime());
		ReportDH reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_HOUR,
				"last hour",
				dateStart, dateEnd,
				rewardType,
				realm.getName());
		listReports.add(reportDH);
	
		//get 24h report
		dateEnd = new Date();
		c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.DAY_OF_MONTH, -1);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_DAY,
				"last 24 hours",
				dateStart, dateEnd, 
				rewardType,
				realm.getName());
		listReports.add(reportDH);

		//get last week
		dateEnd = new Date();
		c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.DAY_OF_MONTH, -7);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_WEEK,
				"last week",
				dateStart, dateEnd, 
				rewardType,
				realm.getName());
		listReports.add(reportDH);

		//get last 2 weeks
		dateEnd = new Date();
		c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.DAY_OF_MONTH, -14);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_2_WEEKS,
				"last 2 weeks",
				dateStart, dateEnd, 
				rewardType,
				realm.getName());
		listReports.add(reportDH);

		//get last month 
		dateEnd = new Date();
		c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.DAY_OF_MONTH, -30);
		dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());
		reportDH = managerReporting.getReportData(realm,
				ReportPeriodName.LAST_4_WEEKS,
				"last month",
				dateStart, dateEnd, 
				rewardType,
				realm.getName());
		listReports.add(reportDH);*/

		//generate retention matrix for last 30 days
    	double[][] userRetentionMatrix = managerReporting.getUserRetention(realm, 
				dateStart, dateEnd,
				31,
				rewardType, realm.getName());
    	//add user retention matrix to the recently generated report dh
    	reportDH.setUserRetentionMatrix(userRetentionMatrix);
    	
    	return listReports;
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