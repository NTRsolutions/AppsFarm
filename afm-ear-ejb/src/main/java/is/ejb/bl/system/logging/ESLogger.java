package is.ejb.bl.system.logging;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;

import is.ejb.bl.business.UserEventType;
import is.ejb.dl.entities.RewardTicketEntity;
public class ESLogger {

	protected static final Logger logger = Logger.getLogger(ESLogger.class.getName());

	private ESLoggerWorkerThread threadLogsQueue = null;
	
	private ESLoggerWorkerThread threadUserClicksQueue = null;
	private String indexNameUserClicks = ESIndexName.ab_clicks.toString();
	private String typeNameUserClicks = ESTypeName.clicks.toString();

	private ESLoggerWorkerThread threadUserRegistrationsQueue = null;
	private String indexNameUserRegistrations = ESIndexName.ab_registrations.toString();
	private String typeNameUserRegistrations = ESTypeName.registrations.toString();

	private ESLoggerWorkerThread threadUserSupportRequestsQueue = null;
	private String indexNameUserSupportRequests = ESIndexName.ab_support_requests.toString();
	private String typeNameUserSupportRequests = ESTypeName.support_requests.toString();

	private ESLoggerWorkerThread threadMobileFaultsQueue = null;
	private String indexNameMobileFaults = ESIndexName.ab_mobile_faults.toString();
	private String typeNameMobileFaults = ESTypeName.mobile_faults.toString();

	private ESLoggerWorkerThread threadWallSelectionsQueue = null;
	private String indexNameWallSelections = ESIndexName.ab_wall_selections.toString();
	private String typeNameWallSelections = ESTypeName.wall_selections.toString();

	private ESLoggerWorkerThread threadWalletTransactionsQueue = null;
	private String indexNameWalletTransactions = ESIndexName.ab_wallet_transactions.toString();
	private String typeNameWalletTransactions = ESTypeName.wallet_transactions.toString();

	private ESLoggerWorkerThread threadCrashReportsQueue = null;
	private String indexNameCrashReports = ESIndexName.ab_crash_reports.toString();
	private String typeNameCrashReports = ESTypeName.crash_reports.toString();
	
	private ESLoggerWorkerThread threadRewardTicketsQueue = null;
	private String indexNameRewardTickets = ESIndexName.ab_reward_tickets.toString();
	private String typeNameRewardTickets = ESTypeName.reward_tickets.toString();

	
	
	private Client client = null;
	private Client clientBackup = null;
	
	private String localhostname = "default name";
	public static int threadCounter = 0;
	
	public ESLogger(String hostName,
					String hostNameBackup,
					String indexName,
					String typeName,
					int pushTimeInterval, 
					int batchSizeLimit, 
					int batchNumberLimit,
					int statusQuoLimit) {
		initClient(hostName, hostNameBackup, 
				indexName, typeName, 
				pushTimeInterval, batchSizeLimit, batchNumberLimit, statusQuoLimit);
	}

	public Client getClient() {
		return client;
	}
	
	public void stop() {
		threadLogsQueue.interrupt();
	}
	  
	public static Settings buildSettings(String hostName, boolean isBackupServer) { 
		Settings settings; 
		// once we find one node in the cluster ask about the others      
		Builder settingsBuilder = 
		ImmutableSettings.settingsBuilder().put("client.transport.sniff", true);
		if(!isBackupServer) {
			settingsBuilder.put("cluster.name", "airrewardz"); 
		} else {
			settingsBuilder.put("cluster.name", "airrewardzBackup");
		}
		settingsBuilder.put("client.transport.ping_timeout", "10s");
		settingsBuilder.put("http.enabled", "false");
		settingsBuilder.put("transport.tcp.port", "9300-9400");
		settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
		settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostName);
		settings = settingsBuilder.build(); 
		return settings; 
	}
	  
	public void initClient(String hostName,
			String hostNameBackup,
			String indexName,
			String typeName,
			int pushTimeInterval, 
			int batchSizeLimit, 
			int batchNumberLimit,
			int statusQuoLimit) {

		//init primary es logging system
		client = new TransportClient(buildSettings(hostName, false)).addTransportAddress(new InetSocketTransportAddress(hostName, 9300));

		//init backup es logging system
		if(hostNameBackup != null && hostNameBackup.length() > 0) {
			clientBackup = new TransportClient(buildSettings(hostNameBackup, true)).addTransportAddress(new InetSocketTransportAddress(hostNameBackup, 9300));
			logger.info("ElasticSearch Backup Log Client initialised for destination host: "+hostNameBackup);
		} else {
			logger.info("ElasticSearch Backup Log Client not initialised as host is not setup: "+hostNameBackup);
			clientBackup = null;
		}

		//-------------- raw logs queue ------------------------
		threadLogsQueue = new ESLoggerWorkerThread(client, clientBackup,
				indexName, typeName, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		//-------------- user clicks queue ------------------------
		threadUserClicksQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameUserClicks, typeNameUserClicks, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);
		
		//-------------- user registrations queue ------------------------
		threadUserRegistrationsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameUserRegistrations, typeNameUserRegistrations, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);
		
		//-------------- user support requests queue ------------------------
		threadUserSupportRequestsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameUserSupportRequests, typeNameUserSupportRequests, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		//-------------- mobile faults queue ------------------------
		threadMobileFaultsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameMobileFaults, typeNameMobileFaults, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		//-------------- wall selections queue ------------------------
		threadWallSelectionsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameWallSelections, typeNameWallSelections, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		//-------------- wallet transactions queue ------------------------
		threadWalletTransactionsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameWalletTransactions, typeNameWalletTransactions, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		//-------------- wallet transactions queue ------------------------
		threadCrashReportsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameCrashReports, typeNameCrashReports, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);
		
		//-------------- reward tickets queue ------------------------
		threadRewardTicketsQueue = new ESLoggerWorkerThread(client, clientBackup, 
				indexNameRewardTickets, typeNameRewardTickets, 
				pushTimeInterval, batchSizeLimit, 
				batchNumberLimit, statusQuoLimit);

		try {
			localhostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
		
		logger.info("ElasticSearch Log Client initialised on host: "+localhostname);
	}

	public void indexLog(String sn, int realmId, LogStatus logStatus, String logContent, int deviceSchemaId) {
		try {
			if(threadLogsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("SN", sn);
				xcb.field("realmId", realmId);
				xcb.field("@timestamp", new Date());
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@logStatus", logStatus);
				xcb.field("deviceSchemaId", deviceSchemaId);
				xcb.field("message", logContent);
				xcb.field("serverName", localhostname);
				xcb.endObject();
				threadLogsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	public void indexLog(String sn, int realmId, LogStatus logStatus, String logContent) {
		try {
			if(threadLogsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("SN", sn);
				xcb.field("realmId", realmId);
				xcb.field("@timestamp", new Date());
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@logStatus", logStatus);
				xcb.field("message", logContent);
				xcb.field("deviceSchemaId", -1);
				xcb.field("serverName", localhostname);
				xcb.endObject();
				threadLogsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}
	
	
	
	public void indexUserClick(int realmId, 
			String phoneNumber, 
			String email, 
			String deviceType,
			String offerId,
			String offerIdProvider,
			String offerName,
			String offerProviderName,
			String rewardType,
			double offerPayout,
			double offerReward,
			String offerCurrency,
			double profit,
			String networkName,
			String redirectUrl,
			String eventType,
			String internalTransactionId,
			String carrierName,
			String userEventCategory,
			String miscData,
			String systemInfo,
			String ipAddress,
			String countryCode,
			boolean instantReaward,
			String applicationName,
			String gaid,
			String idfa,
			boolean testMode,
			double customRewardValue,
			String customRewardValueCurrency) {
		try {
			if(threadUserClicksQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("email", email);
				xcb.field("deviceType", deviceType);
				xcb.field("offerId", offerId);
				xcb.field("offerIdProvider", offerIdProvider);
				xcb.field("offerName", offerName);
				xcb.field("offerProviderName", offerProviderName);
				xcb.field("rewardType", rewardType);
				xcb.field("offerPayout", offerPayout);
				xcb.field("offerReward", offerReward);
				xcb.field("offerCurrency", offerCurrency);
				xcb.field("profit", profit);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("networkName", networkName);
				xcb.field("redirectUrl", redirectUrl);
				xcb.field("eventType", eventType);
				xcb.field("internalTransactionId", internalTransactionId);
				xcb.field("carrierName", carrierName);
				xcb.field("userEventCategory", userEventCategory);
				xcb.field("miscData", miscData);
				xcb.field("systemInfo", systemInfo);
				xcb.field("ipAddress", ipAddress);
				xcb.field("countryCode", countryCode);
				xcb.field("instantReaward", instantReaward);
				xcb.field("applicationName", applicationName);
				xcb.field("gaid", gaid);
				xcb.field("idfa", idfa);
				xcb.field("testMode", testMode);
				xcb.field("customRewardValue", customRewardValue);
				xcb.field("customRewardValueCurrency", customRewardValueCurrency);
				xcb.endObject();
				threadUserClicksQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}
	
	public void indexWallSelection(
			String networkName,
    		int userId,
    		String email,
    		String phoneNumber,
    		String phoneNumberExt,
			int wallId, 
    		String wallRewardType,
    		String wallGeo,
    		String wallDeviceType,
    		String locale,
    		String ua,
    		String ipAddress,
    		String systemInfo,
    		String miscData) {

		try {
			if(threadWallSelectionsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("networkName", networkName);
				xcb.field("userId", userId);
				xcb.field("email", email);
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("phoneNumberExt", phoneNumberExt);
				xcb.field("wallId", wallId);
				xcb.field("wallRewardType", wallRewardType);
				xcb.field("wallGeo", wallGeo);
				xcb.field("wallDeviceType", wallDeviceType);
				xcb.field("locale", locale);
				xcb.field("ua", ua);
				xcb.field("systemInfo", systemInfo);
				xcb.field("miscData", miscData);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("ipAddress", ipAddress);
				xcb.endObject();
				threadWallSelectionsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	public void indexUserRegistration(String fullName,
			String email,
			String phoneNumberExtension,
			String phoneNumber,
			String locale,
			String systemInfo,
			String miscData,
			String ipAddress,
			String ageRange,
			boolean male,
			String deviceType,
			String networkName,
			String countryCode,
			String referralCode,
			String rewardTypeName,
			String gaid,
			String idfa,
			String applicationName) {
		try {
			if(threadUserRegistrationsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("phoneNumberExtension", phoneNumberExtension);
				xcb.field("email", email);
				xcb.field("locale", locale);
				xcb.field("systemInfo", systemInfo);
				xcb.field("miscData", miscData);
				xcb.field("ipAddress", ipAddress);
				xcb.field("ageRange", ageRange);
				xcb.field("male", male);
				xcb.field("deviceType", deviceType);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("networkName", networkName);
				xcb.field("countryCode", countryCode);
				xcb.field("referralCode", referralCode);
				xcb.field("rewardTypeName", rewardTypeName);
				xcb.field("gaid", gaid);
				xcb.field("idfa", idfa);
				xcb.field("applicationName", applicationName);
				xcb.endObject();
				threadUserRegistrationsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	public void indexUserSupportRequest(
			String errorCategory,
			String fullName,
			String email,
			String phoneNumberExtension,
			String phoneNumber,
			String locale,
			String systemInfo,
			String miscData,
			String ipAddress,
			String deviceType,
			String networkName,
			String supportQuestion) {
		try {
			if(threadUserSupportRequestsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("errorCategory", errorCategory);
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("phoneNumberExtension", phoneNumberExtension);
				xcb.field("email", email);
				xcb.field("locale", locale);
				xcb.field("systemInfo", systemInfo);
				xcb.field("miscData", miscData);
				xcb.field("ipAddress", ipAddress);
				xcb.field("deviceType", deviceType);
				xcb.field("supportQuestion", supportQuestion);
				xcb.field("networkName", networkName);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.endObject();
				threadUserSupportRequestsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	public void indexMobileFault(String fullName,
			String email,
			String phoneNumberExtension,
			String phoneNumber,
			String locale,
			String systemInfo,
			String deviceType,
			String networkName,
			String errorMessage,
			String miscData,
			String action,
			String ipAddress) {
		try {
			if(threadMobileFaultsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("phoneNumberExtension", phoneNumberExtension);
				xcb.field("email", email);
				xcb.field("locale", locale);
				xcb.field("systemInfo", systemInfo);
				xcb.field("deviceType", deviceType);
				xcb.field("networkName", networkName);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("errorMessage", errorMessage);
				xcb.field("miscData", miscData);
				xcb.field("action", action);
				xcb.field("ipAddress", ipAddress);
				xcb.endObject();
				threadMobileFaultsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	
	
	public void indexWalletTransaction(int realmId, 
			String phoneNumber, 
			String email, 
			String deviceType,
			String offerId,
			String offerIdProvider,
			String offerName,
			String offerProviderName,
			String rewardType,
			double offerPayout,
			double offerReward,
			String offerCurrency,
			double profit,
			String networkName,
			String redirectUrl,
			String eventType,
			String internalTransactionId,
			String carrierName,
			String userEventCategory,
			String miscData,
			String systemInfo,
			String ipAddress,
			String countryCode,
			boolean instantReaward,
			String applicationName,
			boolean testMode,
			int userId) {
		try {
			if(threadWalletTransactionsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("email", email);
				xcb.field("deviceType", deviceType);
				xcb.field("offerId", offerId);
				xcb.field("offerIdProvider", offerIdProvider);
				xcb.field("offerName", offerName);
				xcb.field("offerProviderName", offerProviderName);
				xcb.field("rewardType", rewardType);
				xcb.field("offerPayout", offerPayout);
				xcb.field("offerReward", offerReward);
				xcb.field("offerCurrency", offerCurrency);
				xcb.field("profit", profit);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("networkName", networkName);
				xcb.field("redirectUrl", redirectUrl);
				xcb.field("eventType", eventType);
				xcb.field("internalTransactionId", internalTransactionId);
				xcb.field("carrierName", carrierName);
				xcb.field("userEventCategory", userEventCategory);
				xcb.field("miscData", miscData);
				xcb.field("systemInfo", systemInfo);
				xcb.field("ipAddress", ipAddress);
				xcb.field("countryCode", countryCode);
				xcb.field("instantReaward", instantReaward);
				xcb.field("applicationName", applicationName);
				xcb.field("testMode", testMode);
				xcb.field("userId", userId);
				xcb.endObject();
				threadWalletTransactionsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}

	public void indexCrashReport(
			String phoneNumberExtension,
			String phoneNumber,
			String deviceInfo,
			String deviceVersion,
			String applicationName,
			String applicationVersion,
			String breadcrumb,
			String stackTrace,
			String ipAddress) {
		try {
			if(threadCrashReportsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("phoneNumberExtension", phoneNumberExtension);
				xcb.field("phoneNumber", phoneNumber);
				xcb.field("deviceInfo", deviceInfo);
				xcb.field("deviceVersion", deviceVersion);
				xcb.field("applicationName", applicationName);
				xcb.field("applicationVersion", applicationVersion);
				xcb.field("breadcrumb", breadcrumb);
				xcb.field("stackTrace", stackTrace);
				xcb.field("ipAddress", ipAddress);
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.endObject();
				threadCrashReportsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}
	
	public void indexRewardTicket(LogStatus logStatus, String message, RewardTicketEntity ticket) {
		try {
			if(threadRewardTicketsQueue.isQueueNotFull()) {
				XContentBuilder xcb = jsonBuilder().startObject();
				xcb.field("userId", ticket.getUserId());
				xcb.field("email", ticket.getEmail());
				xcb.field("rewardName", ticket.getRewardName());
				xcb.field("creditPoints", ticket.getCreditPoints());
				xcb.field("requestDate", ticket.getRequestDate().toString());
				xcb.field("processingDate", ticket.getProcessingDate());
				xcb.field("closeDate", ticket.getCloseDate());
				xcb.field("status", ticket.getStatus().toString());
				xcb.field("comment", ticket.getComment());
				xcb.field("ticketOwner", ticket.getTicketOwner());
				xcb.field("hash", ticket.getHash());
				xcb.field("@logStatus", logStatus.toString());
				xcb.field("@time", System.currentTimeMillis());
				xcb.field("@timestamp", new Date());
				xcb.field("serverName", localhostname);
				xcb.field("message", message);
				xcb.endObject();
				threadRewardTicketsQueue.addToQueue(xcb);
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
		}
	}



}
