package is.ejb.bl.monitoring.operation;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.monitoring.server.ServerStats;

import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailDataHolder;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOMonitoringSetup;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.RealmEntity;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.elasticsearch.client.Client;

@Stateless
public class SystemOpsMonitor {

	@Inject
	private Logger logger;


	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/MailQueue")
	private Queue queue;

	@Inject
	private DAOMonitoringSetup daoMonitoringSetup;
	private MonitoringSetupEntity monitoringSetup;
	private Mailer mailer = new Mailer();
	private SystemOpsAnalyser systemOpsAnalyser; //for retrieving KPIs from data storage (ES mostly)
	//private ServerStatusMonitor serverStatusMonitor;
	private boolean isServerMonitoringEnabled;
	public void monitor(RealmEntity realm, int monitoringInterval, boolean isServerMonitoringEnabled) throws Exception {
    	monitoringSetup = daoMonitoringSetup.findByRealmId(realm.getId());
    	this.isServerMonitoringEnabled = isServerMonitoringEnabled;
    	
    	if(monitoringSetup == null) {
        	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, 
        			LogStatus.WARNING, 
        			Application.SYSTEM_OPS_MONITORING+" aborting system monitoring for realm: "+realm.getId()+" name: "+realm.getName()+" no monitoring setup defined for this realm");
    		return;
    	}
    	systemOpsAnalyser = new SystemOpsAnalyser();
    	
		Client esClient  =  Application.getESClient();
    	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, 
    			LogStatus.OK, 
    			Application.SYSTEM_OPS_MONITORING+" performing ops monitoring for monitoring setup with log analytics connection: "+esClient);
    	
    	//calculate KPI metrics
    	SystemOpsStatsHolder statsHolder = new SystemOpsStatsHolder();
    	//ERROR SIDE
    	//total count of rejected offers (due to low payout treshold)
    	//total count of rejected offers (due to lack of currency support)
    	//total count of rejected offers (due to no currency data)
    	//total count of failed clicks (due to badly formatted phone number)
    	//total count of failed conversions (due to duplicate triggers)
    	
    	//for testing
    	//monitoringInterval = 15;
    	statsHolder.setIntervalTime(monitoringInterval);;
    	//TOODO error side
    	//errors
    	statsHolder.setErrorsCount(systemOpsAnalyser.getErrorsCount(monitoringInterval, realm.getId()));//total count of all errors (predefined history interval)
    	statsHolder.setWarningsCount(systemOpsAnalyser.getWarningsCount(monitoringInterval, realm.getId()));//total count of all errors (predefined history interval)

    	//offers selection
    	statsHolder.setOffersCOWIdsSelectCount(systemOpsAnalyser.getCOWIdsSelectCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersCOWIdsSelectFailedCount(systemOpsAnalyser.getCOWIdsSelectFailedCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersCOWSelectByIdCount(systemOpsAnalyser.getCOWSelectByIdCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersCOWSelectByIdFailedCount(systemOpsAnalyser.getCOWSelectByIdFailedCount(monitoringInterval, realm.getId()));

    	//composite offers generation
    	statsHolder.setOffersCompositeWallGenerationCount(systemOpsAnalyser.getOffersCompositeWallGenerationCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersCompositeWallGenerationFailedCount(systemOpsAnalyser.getOffersCompositeWallGenerationFailedCount(monitoringInterval, realm.getId()));

    	
    	//single offer wall generation
    	statsHolder.setOffersSingleWallGenerationCount(systemOpsAnalyser.getOffersSIngleWallGenerationCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersSingleWallGenerationFailedCount(systemOpsAnalyser.getOffersSIngleWallGenerationFailedCount(monitoringInterval, realm.getId()));

    	statsHolder.setOffersRejectedCount(systemOpsAnalyser.getOffersRejectedCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersInsufficientCount(systemOpsAnalyser.getOffersInsufficientCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersPayoutBelowTresholdCount(systemOpsAnalyser.getSingleOffersPayoutBelowTresholdCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersNoCurrencyDefinedCount(systemOpsAnalyser.getSingleOffersNoCurrencyDefinedCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersCreatedCount(systemOpsAnalyser.getSingleOffersGeneratedCount(monitoringInterval, realm.getId()));
    	
    	statsHolder.setOffersNoImageDefinedCount(systemOpsAnalyser.getSingleOffersNoImageDefinedCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersNoCountryCodesSuppliedByOfferProviderCount(systemOpsAnalyser.getSingleOffersNoGeoFilteringDataSuppliedByOfferProviderCout(monitoringInterval, realm.getId()));
    	statsHolder.setOffersNoTargetDevicesSuppliedByOfferProviderCount(systemOpsAnalyser.getSingleOffersNoTargetDeviceFilteringDataSuppliedByOfferProviderCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersNoSupportedPayoutCurrencyDefinedCount(systemOpsAnalyser.getSingleOffersNoSupportedPayoutCurrencyCount(monitoringInterval, realm.getId()));
    	statsHolder.setOffersRejectedDuplicatesCount(systemOpsAnalyser.getSingleOffersDuplicatesRejected(monitoringInterval, realm.getId()));
    	
    	//clicks
    	statsHolder.setClicksIdentifiedCount(systemOpsAnalyser.getClicksIdentifiedCount(monitoringInterval, realm.getId()));
    	statsHolder.setClicksSuccessfulCount(systemOpsAnalyser.getClicksSuccessfulCount(monitoringInterval, realm.getId()));
    	statsHolder.setClicksFailedCount(systemOpsAnalyser.getClicksFailedCount(monitoringInterval, realm.getId()));
    	//conversions
    	statsHolder.setConversionsIdentifiedCount(systemOpsAnalyser.getConversionsIdentifiedCount(monitoringInterval, realm.getId()));
    	statsHolder.setConversionsSuccessfulCount(systemOpsAnalyser.getConversionsSuccessfulCount(monitoringInterval, realm.getId()));
    	statsHolder.setConversionsFailedCount(systemOpsAnalyser.getConversionsFailedCount(monitoringInterval, realm.getId()));

    	//reward requests
    	statsHolder.setRewardRequestsIdentifiedCount(systemOpsAnalyser.getRewardRequestsIdentifiedCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardRequestsSuccessfulCount(systemOpsAnalyser.getRewardRequestsSuccessfulCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardRequestsFailedCount(systemOpsAnalyser.getRewardRequestsFailedCount(monitoringInterval, realm.getId()));

    	//reward responses
    	statsHolder.setRewardResponsesIdentifiedCount(systemOpsAnalyser.getRewardResponsesIdentifiedCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardResponsesSuccessfulCount(systemOpsAnalyser.getRewardResponsesSuccessfulCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardResponsesFailedCount(systemOpsAnalyser.getRewardResponsesFailedCount(monitoringInterval, realm.getId()));

    	//reward responses with identified successful status
    	statsHolder.setRewardReponsesesWithSuccessStatusIdentifiedCount(systemOpsAnalyser.getRewardResponsesWithSuccessStatusIdentifiedCount(monitoringInterval, realm.getId()));

    	//reward notifications to mobile app
    	statsHolder.setRewardNotificationRequestsSuccessCount(systemOpsAnalyser.getRewardNotificationRequestsSuccessCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardNotificationRequestsFailedCount(systemOpsAnalyser.getRewardNotificationRequestsFailedCount(monitoringInterval, realm.getId()));
    	statsHolder.setRewardNotificationRequestsIdentifiedCount(
    			statsHolder.getRewardNotificationRequestsSuccessCount()+statsHolder.getRewardNotificationRequestsFailedCount());
    	
    	System.out.println(LogStatus.ERROR+" "+statsHolder.getErrorsCount()+" \n"+
    			LogStatus.WARNING+" "+statsHolder.getWarningsCount()+" \n"+
    			Application.COMPOSITE_OFFER_WALL_GENERATION_FAILED+ " "+statsHolder.getOffersCompositeWallGenerationFailedCount()+" \n"+
    			Application.SINGLE_OFFER_REJECTED+ " "+statsHolder.getOffersRejectedCount()+" \n"+
    			Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+ " "+statsHolder.getOffersInsufficientCount()+" \n"+
    			Application.SINGLE_OFFER_NO_CURRENCY_DEFINED+ " "+statsHolder.getOffersNoCurrencyDefinedCount()+" \n"+
    			Application.SINGLE_OFFER_PAYOUT_BELOW_TRESHOLD+ " "+statsHolder.getOffersPayoutBelowTresholdCount()+" \n"+

    			"cow ids select count: "+statsHolder.getOffersCOWIdsSelectCount()+" \n"+
    			"cow ids select failed count: "+statsHolder.getOffersCOWIdsSelectFailedCount()+" \n"+
    			"cow select by id count: "+statsHolder.getOffersCOWSelectByIdCount()+" \n"+
    			"cow select by id failed count: "+statsHolder.getOffersCOWSelectByIdFailedCount()+" \n"+

    			"clicks identfied: "+statsHolder.getClicksIdentifiedCount()+" \n"+
    			"clicks success: "+statsHolder.getClicksSuccessfulCount()+" \n"+
    			"clicks failed: "+statsHolder.getClicksFailedCount()+" \n"+
    	
				"conversions identfied: "+statsHolder.getConversionsIdentifiedCount()+" \n"+
				"conversions success: "+statsHolder.getConversionsSuccessfulCount()+" \n"+
				"conversions failed: "+statsHolder.getConversionsFailedCount()+" \n"+
				
				"reward requests identfied: "+statsHolder.getRewardRequestsIdentifiedCount()+" \n"+
				"reward requests success: "+statsHolder.getRewardRequestsSuccessfulCount()+" \n"+
				"reward requests failed: "+statsHolder.getRewardRequestsFailedCount()+" \n"+

				"reward responses identfied: "+statsHolder.getRewardResponsesIdentifiedCount()+" \n"+
				"reward responses success: "+statsHolder.getRewardResponsesSuccessfulCount()+" \n"+
				"reward responses failed: "+statsHolder.getRewardResponsesFailedCount()+" \n"

    			);
    	//total count of all warnings (predefined history interval)
    	//total count of rejected offers [key: REJECTED_OFFER]
    	//total count of failed clicks [key: REJECTED_CLICK]
    	//total count of failed reward requests [key: REJECTED_REWARD]

    	//TODO OPS SIDE
    	//total count of conversions
    	//total count of clicks
    	//total count of rows in UserEvent table
    	//total count of rows in Offer table

    	ServerStats serverStats = null;
    	if(isServerMonitoringEnabled) {
        	try {
            //	serverStatusMonitor = new ServerStatusMonitor(100);
            	//serverStats = serverStatusMonitor.regenerateStats();
        	} catch(Exception exc) {
        		serverStats = new ServerStats();
        		
            	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, 
            			LogStatus.WARNING, 
            			Application.SYSTEM_OPS_MONITORING+" error retrieving system performance stats: "+exc.toString());
        	}
    	}
    	
    	//identify alerts
    	//notify users about KPI metrics
    	if(monitoringSetup.isEmailNotificationActive()){
    		sendStatusReport(monitoringSetup, serverStats, statsHolder);
    	}
    	
    	//notify users about alerts and reports
    	if(monitoringSetup.isEmailNotificationActive()){
    		sendAlertReport(monitoringSetup, serverStats, statsHolder);
    	}

	}
	
	private void sendStatusReport(MonitoringSetupEntity monitoringSetup, 
			ServerStats serverStats,
			SystemOpsStatsHolder statsHolder) {
		String statusEmails = monitoringSetup.getOperationStatusReportEmails();
	
		//populate content
		String emailSubject = "AdBroker operation status report";
		String emailContent = "Server status at time: "+new Timestamp(System.currentTimeMillis()).toString();
		if(isServerMonitoringEnabled) {
			//emailContent = "Server status at time: "+serverStats.getCurrentTime()
			emailContent = emailContent +  "\n --------------------------------"
					+ "\n CPU utilisation (%):  "+serverStats.getCpuUtilisation()
					+ "\n Mem used (%):         "+serverStats.getMemTotalUsedPercentage()
					+ "\n Java usage ratio (%): "+serverStats.getMemJavaUsageRatio()* (double)100
					+ "\n HTTP load (r/s):      "+serverStats.getRequestsCWMP()
					+ "\n Disk reads (MB):      "+serverStats.getDiskReads()
					+ "\n Disk writes (MB):     "+serverStats.getDiskWrites()
					+ "\n Net upload (KB):      "+serverStats.getNetUpload()
					+ "\n Net download (KB):    "+serverStats.getNetDownload();
		}
		
		emailContent = emailContent + "\n --------------------------------"
				+ "\n Logs: errors: "+statsHolder.getErrorsCount()+" warnings: "+statsHolder.getWarningsCount()
				+ "\n --------------------------------"
				+ "\n Clicks:      count: "+statsHolder.getClicksIdentifiedCount()+" succ: "+statsHolder.getClicksSuccessfulCount()+" fail: "+statsHolder.getClicksFailedCount()+" T: "+statsHolder.getThroughputOfferClick()+"/s"
				+ "\n Conversions: count: "+statsHolder.getConversionsIdentifiedCount()+" succ: "+statsHolder.getConversionsSuccessfulCount()+" fail: "+statsHolder.getConversionsFailedCount()+" T: "+statsHolder.getThroughputOfferConversion()+"/s"
				+ "\n Reward req:  count: "+statsHolder.getRewardRequestsIdentifiedCount()+" succ: "+statsHolder.getRewardRequestsSuccessfulCount()+" fail: "+statsHolder.getRewardRequestsFailedCount()+" T: "+statsHolder.getThroughputOfferRewardRequest()+"/s"
				+ "\n Reward resp: count: "+statsHolder.getRewardResponsesIdentifiedCount()+" succ: "+statsHolder.getRewardResponsesSuccessfulCount()+" fail: "+statsHolder.getRewardResponsesFailedCount()+" T: "+statsHolder.getThroughputOfferRewardResponse()+"/s"
				+ "\n Reward notif count: "+statsHolder.getRewardNotificationRequestsIdentifiedCount()+" succ: "+statsHolder.getRewardNotificationRequestsSuccessCount()+" fail: "+statsHolder.getRewardNotificationRequestsFailedCount()
				+ "\n --------------------------------"
				+ "\n Reward resp with: "+RespStatusEnum.SUCCESS+" code: "+statsHolder.getRewardReponsesesWithSuccessStatusIdentifiedCount()
				+ "\n --------------------------------"
				+ "\n Composite Offer Walls (COW) Selection: "
				+ "\n -> select ids count: "+statsHolder.getOffersCOWIdsSelectCount()+" Failed: "+statsHolder.getOffersCOWIdsSelectFailedCount()+""
				+ "\n -> select by id count: "+statsHolder.getOffersCOWSelectByIdCount()+" Failed: "+statsHolder.getOffersCOWSelectByIdFailedCount()+" T: "+statsHolder.getThroughputOfferCOWRequest()+"/s"
				+ "\n --------------------------------"
				+ "\n Composite Offer Walls (COW) Generation: "
				+ "\n -> Generated: "+ statsHolder.getOffersCompositeWallGenerationCount()+" Failed: "+statsHolder.getOffersCompositeWallGenerationFailedCount()+" T: "+statsHolder.getThroughputOfferCOWGeneration()+"/s"
				+ "\n --> insufficient offers errors: "+statsHolder.getOffersInsufficientCount()
				+ "\n --------------------------------"
				+ "\n Single Offer Walls (SOW) Generation: "
				+ "\n -> Generated: "+ statsHolder.getOffersSingleWallGenerationCount()+" Failed: "+statsHolder.getOffersSingleWallGenerationFailedCount()+" T: "+statsHolder.getThroughputOfferSingleWallGeneration()+"/s"
				+ "\n --------------------------------"
				+ "\n Single Offers (SO) Generation: "
				+ "\n -> Generated: "+ statsHolder.getOffersCreatedCount()+" Rejected: "+statsHolder.getOffersRejectedCount()+" T: "+statsHolder.getThroughputOfferIndividualGeneration()+"/s"
				+ "\n --> rejected (no currency defined):          "+statsHolder.getOffersNoCurrencyDefinedCount()
				+ "\n --> rejected (payout below treshold):        "+statsHolder.getOffersPayoutBelowTresholdCount()
				+ "\n --> rejected (no images defined):            "+statsHolder.getOffersNoImageDefinedCount()
				+ "\n --> rejected (no target countries supplied): "+statsHolder.getOffersNoCountryCodesSuppliedByOfferProviderCount()
				+ "\n --> rejected (no target devices supplied):   "+statsHolder.getOffersNoTargetDevicesSuppliedByOfferProviderCount()
				+ "\n --> rejected (no supported payout currency): "+statsHolder.getOffersNoSupportedPayoutCurrencyDefinedCount()
				+ "\n --> rejected (duplicates):                   "+statsHolder.getOffersRejectedDuplicatesCount();

		

		//send status e-mails
		if(statusEmails.length()>0) {
			StringTokenizer st = new StringTokenizer(statusEmails, ",");
			while(st.hasMoreElements()) {
				String emailAddress = ((String)st.nextElement()).trim();
				try {
					//mailer.send(emailAddress, emailSubject, emailContent, monitoringSetup); //send status report email
					MailDataHolder mail = new MailDataHolder();
					mail.setEmailAddress(emailAddress);
					mail.setEmailSubject(emailSubject);
					mail.setEmailContent(emailContent);
					mail.setMailboxSetup(monitoringSetup);
					dispatchMail(mail); //dispatch mail
					
			    	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, monitoringSetup.getRealm().getId(), 
			    			LogStatus.OK, 
			    			Application.SYSTEM_OPS_MONITORING+" successfully sent status report for realm: "+monitoringSetup.getRealm().getName()+" to email: "+emailAddress);
				} catch(Exception exc) {
					exc.printStackTrace();
			    	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, monitoringSetup.getRealm().getId(), 
			    			LogStatus.ERROR, 
			    			Application.SYSTEM_OPS_MONITORING+" error sending status report for monitoring setup for realm: "+monitoringSetup.getRealm().getName()+" error: "+exc.toString());
				}
			}
		}
	}
	
	private void sendAlertReport(MonitoringSetupEntity monitoringSetup, 
			ServerStats serverStats,
			SystemOpsStatsHolder statsHolder) {
		String alertEmails = monitoringSetup.getAlertEmails();
	
		//populate content
		String emailSubject = "AdBroker operation status report";
		String emailContent = "Server status at time: "+new Timestamp(System.currentTimeMillis()).toString();
		if(isServerMonitoringEnabled) {
			//emailContent = "Server status at time: "+serverStats.getCurrentTime()
			emailContent =  "\n --------------------------------"
					+ "\n CPU utilisation (%):  "+serverStats.getCpuUtilisation()
					+ "\n Mem used (%):         "+serverStats.getMemTotalUsedPercentage()
					+ "\n Java usage ratio (%): "+serverStats.getMemJavaUsageRatio()* (double)100
					+ "\n HTTP load (r/s):      "+serverStats.getRequestsCWMP()
					+ "\n Disk reads (MB):      "+serverStats.getDiskReads()
					+ "\n Disk writes (MB):     "+serverStats.getDiskWrites()
					+ "\n Net upload (KB):      "+serverStats.getNetUpload()
					+ "\n Net download (KB):    "+serverStats.getNetDownload();
		}
		
		emailContent = emailContent + "\n --------------------------------"
				+ "\n Total Error Logs:    "+statsHolder.getErrorsCount()
				+ "\n Total Warning Logs:  "+statsHolder.getWarningsCount()
				+ "\n --------------------------------"
				+ "\n Clicks:      ident: "+statsHolder.getClicksIdentifiedCount()+" succ: "+statsHolder.getClicksSuccessfulCount()+" fail: "+statsHolder.getClicksFailedCount()
				+ "\n Conversions: ident: "+statsHolder.getConversionsIdentifiedCount()+" succ: "+statsHolder.getConversionsSuccessfulCount()+" fail: "+statsHolder.getConversionsFailedCount()
				+ "\n Reward req:  ident: "+statsHolder.getRewardRequestsIdentifiedCount()+" succ: "+statsHolder.getRewardRequestsSuccessfulCount()+" fail: "+statsHolder.getRewardRequestsFailedCount()
				+ "\n Reward resp: ident: "+statsHolder.getRewardResponsesIdentifiedCount()+" succ: "+statsHolder.getRewardResponsesSuccessfulCount()+" fail: "+statsHolder.getRewardResponsesFailedCount()
				+ "\n --------------------------------"
				+ "\n Composite Offer Walls (COW) Selection: "
				+ "\n -> select ids count: "+statsHolder.getOffersCOWIdsSelectCount()+" Failed: "+statsHolder.getOffersCOWIdsSelectFailedCount()+""
				+ "\n -> select by id count: "+statsHolder.getOffersCOWSelectByIdCount()+" Failed: "+statsHolder.getOffersCOWSelectByIdFailedCount()+""
				+ "\n --------------------------------"
				+ "\n Composite Offer Walls (COW) Generation: "
				+ "\n -> failed COW generation              "+statsHolder.getOffersCompositeWallGenerationFailedCount()
				+ "\n -> insufficient offers errors:        "+statsHolder.getOffersInsufficientCount()
				+ "\n -> rejected indiviual offers:         "+statsHolder.getOffersRejectedCount()
				+ "\n --> rejected (no currency defined):   "+statsHolder.getOffersNoCurrencyDefinedCount()
				+ "\n --> rejected (payout below treshold): "+statsHolder.getOffersPayoutBelowTresholdCount();
		
		//send status e-mails
		if(alertEmails.length()>0) {
			StringTokenizer st = new StringTokenizer(alertEmails, ",");
			while(st.hasMoreElements()) {
				String emailAddress = ((String)st.nextElement()).trim();
				try {
					//mailer.send(emailAddress, emailSubject, emailContent, monitoringSetup); //send status report email
					
					MailDataHolder mail = new MailDataHolder();
					mail.setEmailAddress(emailAddress);
					mail.setEmailSubject(emailSubject);
					mail.setEmailContent(emailContent);
					mail.setMailboxSetup(monitoringSetup);
					dispatchMail(mail); //dispatch mail

			    	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, monitoringSetup.getRealm().getId(), 
			    			LogStatus.OK, 
			    			Application.SYSTEM_OPS_MONITORING+" successfully sent alert report for realm: "+monitoringSetup.getRealm().getName()+" to email: "+emailAddress);
				} catch(Exception exc) {
					exc.printStackTrace();
			    	Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, monitoringSetup.getRealm().getId(), 
			    			LogStatus.ERROR, 
			    			Application.SYSTEM_OPS_MONITORING+" error sending alert report for monitoring setup for realm: "+monitoringSetup.getRealm().getName()+" error: "+exc.toString());
				}
			}
		}
	}

    private void dispatchMail(MailDataHolder mail)
    {
		Connection connection = null;
		
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(queue);
			connection.start();
			ObjectMessage message = session.createObjectMessage();
			message.setObject(mail);
			messageProducer.send(message);
		} catch (JMSException e) {
            logger.severe(e.getMessage());
			e.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, 
					LogStatus.ERROR, 
					Application.SYSTEM_MONITORING+" TIMER error sending email to JMS queue: "+e.toString());
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
    

}
