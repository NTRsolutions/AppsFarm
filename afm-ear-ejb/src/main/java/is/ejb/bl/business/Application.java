package is.ejb.bl.business;

import is.ejb.bl.denominationModels.CustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.DefaultDenominationModel;
import is.ejb.bl.denominationModels.DenominationModelTable;
import is.ejb.bl.denominationModels.SerDeCustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.SerDeDenominationModelTable;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerProviders.woobi.SerDeWoobiProviderConfiguration;
import is.ejb.bl.offerProviders.woobi.WoobiProviderConfig;
import is.ejb.bl.system.logging.ESLogger;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.bl.timers.TimerStartupConfig;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAOCustomDenominationModel;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAODeviceProfile;
import is.ejb.dl.dao.DAOMonitoringSetup;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAORole;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.CustomDenominationModelEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.DeviceProfileEntity;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.PropertyEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.RoleEntity;
import is.ejb.dl.entities.UserEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.Stateless;
import javax.ejb.TimerConfig;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.hibernate.SessionFactory;
import org.rosuda.REngine.Rserve.RConnection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;


public class Application {
	
	static Logger logger = Logger.getLogger(Application.class.getName());

	@Inject
	private Logger log;
	@Inject
	private SecurityManager secManager;
	@Inject 
	private DAODeviceProfile daoDeviceProfile;
	@Inject
	private DAOUser daoCustomer;
	@Inject
	private DAORole daoRole;
	@Inject
	private DAORealm daoRealm;
	@Inject
	private DAOAdProvider daoAdProvider;
	@Inject
	private DAOProperty daoProperty;
	@Inject
	private DAODenominationModel daoDenominationModel;
	@Inject
	private SerDeDenominationModelTable serDeDenominationModelTabe;

	@Inject
	private DAORewardType daoRewardType;


	@Inject
	private DAOBlockedOffers daoBlockedOffers;
	@Inject
	private SerDeBlockedOffers serDeBlockedOffers;

	@Inject
	private DAOCustomDenominationModel daoCustomDenominationModel;
	@Inject
	private SerDeCustomDenominationModelAssignments serDeCustomDenominationModelAssignments;

	@Inject
	private DAOCurrencyCode daoCurrencyCode;
	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;
	private CurrencyCodeEntity currencyConfigurationEntity;
	private CurrencyCodes currencyCodes = new CurrencyCodes(); 
	private ArrayList<CurrencyCode> listCurrencyCodes = new ArrayList<CurrencyCode>();

	@Inject
	private DAOOfferFilter daoOfferFilter;
	private OfferFilterEntity offerFilter;

	@Inject
	private DAOMonitoringSetup daoMonitoringSetup; 
	
	@Inject
	private DAOAppUser daoAppUser;
	
	MonitoringSetupEntity monitoringSetup = null;

	private static Client esClient = null; //es client connection used for analytics (we keep one for the whole system)
	
	public static final String SERVER_STATS_FOLDER_PATH = "DATACOP-STATS";

	public static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final String PROPERTY_OVERRIDESERVERNAME = "overrideServerName";
    private static final String PROPERTY_AUTOCREATECPE = "autoCreateCpe";
    private static final String PROPERTY_FIRMWAREPATH = "firmwarePath";
    private static final String PROPERTY_STUNPORT = "STUNPort";
    private static final String PROPERTY_MONITORING_INTERVALS = "monitoringIntervals";
    private static final String PROPERTY_FORECASTING_INTERVALS = "forecastingIntervals";
    private static final String PROPERTY_MAINTENANCE_DURATION = "maintenanceDuration";
    private static final String PROPERTY_SSC_INTERVALS = "serverStatusCollectionIntervals";
    private static final String PROPERTY_REPORTING_INTERVALS = "reportingIntervals";
    private static final String PROPERTY_NONATNET = "NoNATNet";
    
    private static final String PROPERTY_VERBOSE_LOGGING = "verboseLogging";
    private static final String PROPERTY_CPE_LOGGING_ENABLED = "cpeLoggingEnabled";
    private static final String PROPERTY_LOG_SERVER_ADDRESS = "logServerAddress";
    private static final String PROPERTY_LOG_SERVER_BACKUP_ADDRESS = "logServerBackupAddress";
    private static final String PROPERTY_FORECASTING_SERVER_ADDRESS = "forecastingServerAddress";
    private static final String PROPERTY_IMDG_SERVER_ADDRESS = "imdgServerAddress";
    private static final String PROPERTY_AC_ENABLED = "acEnabled";
    private static final String PROPERTY_MONITORING_ENABLED = "monitoringEnabled";
    private static final String PROPERTY_SERVER_MONITORING_ENABLED = "serverMonitoringEnabled";
    private static final String PROPERTY_CRF_ENABLED = "crfEnabled";
    private static final String PROPERTY_OFFER_GENERATION_ENABLED = "offerGenerationEnabled";
    private static final String PROPERTY_OFFER_INTERVALS = "offerGenerationIntervals";
    private static final String PROPERTY_CRF_INTERVALS = "crfIntervals";
    private static final String PROPERTY_MASETER_SERVER_ADDRESS = "masterServerIp";
    
    private static final String PROPERTY_FC_ENABLED = "forecastingEnabled";
    private static final String PROPERTY_MAINTENANCE_ENABLED = "maintenanceEnabled";
    private static final String PROPERTY_SSC_ENABLED = "serverStatusCollectionEnabled";
    private static final String PROPERTY_REPORTING_ENABLED = "reportingEnabled";
    private static final String PROPERTY_DISPLAYED_CPE_LOG_EVENTS = "displayedCPELogsEvents";
    private static final String PROPERTY_DISPLAYED_ALERT_EVENTS = "displayedAlertEvents";
    
    private static final String PROPERTY_SQL_STORAGE_SIZE_MONITORING_INTERVALS = "sqlStorageSizeMonitoringIntervals";
    private static final String PROPERTY_SQL_DATA_SIZE_MONITOR = "sqlDataSizeMonitorEnabled";
    private static final String PROPERTY_SQL_DATA_STORAGE_HISTORY_LENGTH = "sqlDataStorageHistoryLength";
    
    private static final String PROPERTY_CONNECTION_REQUEST_USERNAME = "user";
    private static final String PROPERTY_CONNECTION_REQUEST_PASSWORD = "password";

    public static String REFERRAL_ADBROKER_PREVIX="ADBROKER_3"; 
    
    public static String REWARD_PROVIDER_AIR_REWARDZ_KENYA="AirRewardz-Kenya";
    public static String REWARD_PROVIDER_AIR_REWARDZ_INDIA="AirRewardz-India";
    public static String REWARD_PROVIDER_AIR_REWARDZ_SOUTH_AFRICA="AirRewardz-SouthAfrica";
    public static String REWARD_PROVIDER_AIR_REWARDZ_TEST= "AirRewardz-Test";
    public static String REWARD_PROVIDER_GO_AHEAD_BRIGHTON_HOVE_UK="Trippa-GB";
    public static String REWARD_PROVIDER_AFA="BPMRewardz-GB";
    public static String REWARD_PROVIDER_CINETREATS = "Cinetreats-GB";
    public static String REWARD_PROVIDER_CINETREATS_AU = "Cinetreats-AU";
    
    public static String GENERIC_SERVICE_TYPE="generic";
    public static String GENERIC_USER_ACTIVITY_LOG="USER_ACTIVITY";
    public static String SYSTEM_MONITORING="SYSTEM_MONITORING";
    public static String SYSTEM_OPS_MONITORING="SYSTEM_OPS_MONITORING";
    public static String LOG_ANALYTICS_CONNECTOR="LOG_ANALYTICS_CONNECTOR";
    public static String SQL_MONITORING_ACTIVITY="SQL_MONITORING_ACTIVITY";
    public static String GENERIC_SYSTEM_ACTIVITY_LOG="SYSTEM_ACTIVITY";
    public static String GENERIC_NETWORK_STATS_HOURLY="NETWORK_STATS_HOURLY";
    public static String GENERIC_OFFER_WALL_SELECTION_ACTIVITY="OFFER_WALL_SELECTION_ACTIVITY";
    public static String GENERIC_MONITORING_ACTIVITY_LOG="MONITORING_ACTIVITY";
    public static String GENERIC_FORECASTING_ACTIVITY_LOG="FORECASTING_ACTIVITY";
    public static String GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG="SYSTEM_MAINTENANCE_ACTIVITY";    
    public static String AUTOREGISTER_STATUS_NOT_REGISTERED="Awaiting registration";
    public static String AUTOREGISTER_STATUS_REGISTERED="Registered";

    //mail manager
    public static String MAILER="MAILER";
    
    //crf
    public static String CRF_TRIGGER_ACTIVITY="CRF_TRIGGER_ACTIVITY";
    public static String CRF_BLOCK_ACTIVITY="CRF_BLOCK_ACTIVITY";
    public static String CRF_REMOVE_FROM_BLOCK="CRF_REMOVE_FROM_BLOCK";
    public static String CRF_ADD_TO_BLOCK="CRF_ADD_TO_BLOCK";
    public static String CRF_UPDATE_BLOCK="CRF_UPDATE_BLOCK";
    
    //reporting
    public static String REPORTING_TIMER_TRIGGERED="REPORTING_TIMER_TRIGGERED";
    public static String REPORTING_ACTIVITY_TRIGGERED="REPORTING_ACTIVITY_TRIGGERED";
    public static String REPORTING_ACTIVITY_DISABLED="REPORTING_ACTIVITY_DISABLED";
    public static String REPORTING_ACTIVITY="REPORTING_ACTIVITY";
    public static String REPORT_GENERATION="REPORT_GENERATION";
    
    //reportingAPI
    public static final String REPORTING_API_ACTIVITY = "REPORTING_API_ACTIVITY";
    
    //offer wall generation
    public static String OFFER_POOL_SIZE="OFFER_POOL_SIZE";
    public static String OFFER_WALL_GENERATION_ACTIVITY="OFFER_WALL_GENERATION_ACTIVITY";
    public static String EMPTY_OFFER_WALL_REMOVED="EMPTY_OFFER_WALL_REMOVED";
    public static String OFFER_WALL_GENERATION_NOTIFICATION_SUCCESS="OFFER_WALL_GENERATION_NOTIFICATION_SUCCESS";
    public static String OFFER_WALL_GENERATION_NOTIFICATION_FAILED="OFFER_WALL_GENERATION_NOTIFICATION_FAILED";

    //quidco 
    public static String QUIDCO="QUIDCO";
    public static String QUIDCO_GET_DELTA="QUIDCO_GET_DELTA";
    
    
    //snapdeal
    public static String SNAPDEAL="SNAPDEAL";
    public static String TIMER_SNAPDEAL_APPROVE_REWARDS="TIMER_SNAPDEAL_APPROVE_REWARDS";
    public static String SNAPDEAL_GET_REPORTS="SNAPDEAL_GET_REPORTS";
    public static String SNAPDEAL_GET_OFFERS_FEED="SNAPDEAL_GET_OFFERS_FEED";
    public static String SNAPDEAL_WS_REQUEST="SNAPDEAL_WS_REQUEST";
    public static String SNAPDEAL_GET_CATEGORIES="SNAPDEAL_GET_CATEGORIES";
    public static String SNAPDEAL_GET_CATEGORY_OFFERS="SNAPDEAL_GET_CATEGORY_OFFERS";
    public static String SNAPDEAL_CLICK = "SNAPDEAL_CLICK";
    public static String SNAPDEAL_MANAGER_ACTIVITY = "SNAPDEAL_MANAGER_ACTIVITY";
    public static String SNAPDEAL_REPORT_TIMER = "SNAPDEAL_REPORT_TIMER";
    public static String SNAPDEAL_6_WEEK_TIMER  = "SNAPDEAL_6_WEEK_TIMER";
	public static String SNAPDEAL_FLITER = "SNAPDEAL_FILTER";
	public static String SNAPDEAL_TOPLIST = "SNAPDEAL_TOPLIST";
    public static String SNAPDEAL_TOPLIST_MANAGER = "SNAPDEAL_TOPLIST_MANAGER";
	
    //user 
    public static String MOBILE_USER_ACTIVITY="MOBILE_USER_ACTIVITY";
    public static String USER_REGISTRATION_ACTIVITY="USER_REGISTRATION_ACTIVITY";
    public static String USER_ALREADY_REGISTERED="USER_ALREADY_REGISTERED";
    public static String USER_TO_UPDATE_NOT_FOUND="USER_TO_UPDATE_NOT_FOUND";
    public static String USER_TO_LOGIN_NOT_FOUND="USER_TO_LOGIN_NOT_FOUND";
    public static String USER_SUCCESSFULLY_REGISTERED="USER_SUCCESSFULLY_REGISTERED";
    public static String USER_SUCCESSFULLY_UPDATED="USER_SUCCESSFULLY_UPDATED";
    public static String USER_SUCCESSFULLY_LOGGED="USER_SUCCESSFULLY_LOGGED";
    public static String USER_UPDATE_ACTIVITY="USER_UPDATE_ACTIVITY";
    public static String USER_PASSWORD_UPDATE_ACTIVITY="USER_PASSWORD_UPDATE_ACTIVITY";
    public static String USER_RESTORE_PASSWORD_ACTIVITY="USER_RESTORE_PASSWORD_ACTIVITY";
    public static String USER_LOGIN_ACTIVITY="USER_LOGIN_ACTIVITY";
    public static String USER_PASSWORD_DOES_NOT_MATCH="USER_PASSWORD_DOES_NOT_MATCH";
    public static String USER_DEVICE_REGISTRATION_CHECK = "USER_DEVICE_REGISTRATION_CHECK";
    public static String USER_ACCOUNT_ACTIVATION_ACTIVITY = "USER_ACCOUNT_ACTIVATION_ACTIVITY";
    public static String USER_DETAILS_FIX = "USER_DETAILS_FIX";
    
    //hash validation activity
    public static String HASH_VALIDATION_ACTIVITY="HASH_VALIDATION_ACTIVITY";
    public static String HASH_VALIDATION_SUCCESSFUL="HASH_VALIDATION_SUCCESSFUL";
    public static String HASH_VALIDATION_ABORTED="HASH_VALIDATION_ABORTED";
    
    //click history request activity
    public static String USER_CLICK_HISTORY_REQUEST_ACTIVITY="USER_CLICK_HISTORY_REQUEST_ACTIVITY";
    
    //token validation
    public static String TOKEN_VALIDATION_ACTIVITY="TOKEN_VALIDATION_ACTIVITY";
    
    //clicks
    public static String CLICK_ACTIVITY="CLICK_ACTIVITY";

    //UI state manager
    public static String UI_STATE_MANAGER="UI_STATE_MANAGER";

    //invitation
    public static String INVITATION_ACTIVITY = "INVITATION_ACTIVITY";
    public static String INVITATION_ACTIVITY_ABORTED = "INVITATION_ACTIVITY_ABORTED";
    public static String INVITATION_ACTIVITY_SUCCESSFULLY_VALIDATED = "INVITATION_ACTIVITY_SUCCESSFULLY_VALIDATED";
    public static String INVITATION_ABUSE_DETECTOR_CHECK = "INVITATION_ABUSE_DETECTOR_CHECK";
    public static String INVITATION_ABUSE_DETECTED_REFERRAL_REWARD_REJECTED = "INVITATION_ABUSE_DETECTED_REFERRAL_REWARD_REJECTED";
    public static String INVITATION_ABUSE_DETECTED = "INVITATION_ABUSE_DETECTED";
    
    public static String ACCOUNT_NOT_ACTIVATED_ACTIVITY = "ACCOUNT_NOT_ACTIVATED_ACTIVITY";
    //error reporting
    public static String ERROR_REPORTING_ACTIVITY="ERROR_REPORTING_ACTIVITY";
    public static String USER_ERROR_REPORTING_ACTIVITY="USER_ERROR_REPORTING_ACTIVITY";

    //conversions
    public static String CONVERSION_NOTIFICATION="CONVERSION_NOTIFICATION"; //sent to AR server
    public static String CONVERSION_ACTIVITY="CONVERSION_ACTIVITY";
    public static String CONVERSION_ACTIVITY_SUCCESS="CONVERSION_ACTIVITY_SUCCESS";

    public static String CONVERSION_NOTIFICATION_SUCCESS="CONVERSION_NOTIFICATION_SUCCESS"; //sent to AR server
    public static String CONVERSION_NOTIFICATION_FAILURE="CONVERSION_NOTIFICATION_FAILURE"; //sent to AR server

    public static String DOWNLOAD_HISTORY_UPDATE="DOWNLOAD_HISTORY_UPDATE";
    public static String DOWNLOAD_HISTORY_CLICK_UPDATE="DOWNLOAD_HISTORY_CLICK_UPDATE";
    public static String DOWNLOAD_HISTORY_CONVERSION_UPDATE="DOWNLOAD_HISTORY_CONVERSION_UPDATE";
    public static String DOWNLOAD_HISTORY_REWARD_UPDATE="DOWNLOAD_HISTORY_REWARD_UPDATE";

    //event queue
    public static String EVENT_QUEUE_ACTIVITY="EVENT_QUEUE_ACTIVITY";
    public static String EVENT_QUEUE_PUSH_CHECK="EVENT_QUEUE_PUSH_CHECK";
    public static String EVENT_QUEUE_PUSH_CHECK_RESP="EVENT_QUEUE_PUSH_CHECK_RESP";
    public static String EVENT_QUEUE_DELETE="EVENT_QUEUE_DELETE";
    public static String EVENT_QUEUE_NO_ELEMENTS_TO_PROCESS="EVENT_QUEUE_NO_ELEMENTS_TO_PROCESS";
    
    
    //reward requests
    public static String REWARD_ACTIVITY="REWARD_ACTIVITY";
    public static String REWARD_REQUEST_ACTIVITY="REWARD_REQUEST_ACTIVITY";
    public static String REWARD_REQUEST_IDENTIFIED="REWARD_REQUEST_IDENTIFIED";
    public static String REWARD_CREDIT_CALCULATION="REWARD_CREDIT_CALCULATION";
    public static String REWARD_TICKET_CREATE_ACTIVITY = "REWARD_TICKET_CREATE_ACTIVITY";
    
    //reward responses
    public static String REWARD_RESPONSE_ACTIVITY="REWARD_RESPONSE_ACTIVITY";
    public static String REWARD_RESPONSE_IDENTIFIED="REWARD_RESPONSE_IDENTIFIED";
    public static String REWARD_RESPONSE_FAILED="REWARD_RESPONSE_FAILED";
    public static String REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE_FAILED="REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE_FAILED";
    public static String REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE="REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE";
    
    public static String GENERIC_MODE_CREDIT_RESPONSE_ACTIVITY="MODE_CREDIT_RESPONSE_ACTIVITY";
    public static String GENERIC_REWARD_TRACKING_ACTIVITY="REWARD_TRACKING_ACTIVITY";

    //reward notifications to mobile app
    public static String REWARD_NOTIFICATION_ACTIVITY_DONKEY_TRIGGER="REWARD_NOTIFICATION_ACTIVITY_DONKEY_TRIGGER";
    public static String REWARD_NOTIFICATION_ACTIVITY="REWARD_NOTIFICATION_ACTIVITY";    
    public static String REWARD_NOTIFICATION_SUCCESS="REWARD_NOTIFICATION_SUCCESS";
    public static String REWARD_NOTIFICATION_FAILURE="REWARD_NOTIFICATION_FAILURE";
    public static String ABUSE_NOTIFICATION_ACTIVITY = "ABUSE_NOTIFICATION_ACTIVITY";
    
    //composite offer walls generation
    public static String COMPOSITE_OFFER_WALL_GENERATION_IDENTIFIED = "COMPOSITE_OFFER_WALL_GENERATION_IDENTIFIED";
    public static String COMPOSITE_OFFER_WALL_GENERATION_FAILED = "COMPOSITE_OFFER_WALL_GENERATION_FAILED";
    public static String OFFERS_GENERATION_OFFERS_INSUFFICIENT = "OFFERS_GENERATION_OFFERS_INSUFFICIENT";

    
    public static String SINGLE_OFFER_CREATED = "SINGLE_OFFER_CREATED";
    public static String SINGLE_OFFER_REJECTED = "SINGLE_OFFER_REJECTED";
    public static String SINGLE_OFFER_NOT_INCENTIVISED = "SINGLE_OFFER_NOT_INCENTIVISED";
    public static String SINGLE_OFFER_NO_CURRENCY_DEFINED = "SINGLE_OFFER_NO_CURRENCY_DEFINED";
    public static String SINGLE_OFFER_NO_PAYOUT_DEFINED = "SINGLE_OFFER_NO_PAYOUT_DEFINED";
    public static String SINGLE_OFFER_PAYOUT_BELOW_TRESHOLD = "SINGLE_OFFER_PAYOUT_BELOW_TRESHOLD";
    public static String SINGLE_OFFER_NO_IMAGE_DEFINED = "SINGLE_OFFER_NO_IMAGE_DEFINED";
    public static String SINGLE_OFFER_NO_URL_DEFINED = "SINGLE_OFFER_NO_URL_DEFINED";
    public static String SINGLE_OFFER_NO_SUPPORTED_PAYOUT_CURRENCY_DEFINED = "SINGLE_OFFER_NO_SUPPORTED_PAYOUT_CURRENCY_DEFINED";
    public static String SINGLE_OFFER_BLOCKED_OFFER_REJECTED = "SINGLE_OFFER_BLOCKED_OFFER_REJECTED";
    public static String OFFERS_POSITIONING = "OFFERS_POSITIONING";

    public static String STALE_OFFER_WALL_FILTERING = "STALE_OFFER_WALL_FILTERING";
    public static String STALE_OFFER_WALL_REJECTED = "STALE_OFFER_WALL_REJECTED";

    public static String SINGLE_OFFER_GEO_FILTERING = "SINGLE_OFFER_GEO_FILTERING";
    public static String SINGLE_OFFER_GEO_FILTERING_ACCEPTED = "SINGLE_OFFER_GEO_FILTERING_ACCEPTED";
    public static String SINGLE_OFFER_GEO_FILTERING_ACCEPTED_NO_FILTER = "SINGLE_OFFER_GEO_FILTERING_ACCEPTED_NO_FILTER";
    public static String SINGLE_OFFER_GEO_FILTERING_ERROR_NO_COUNTRY_CODES_SUPPLIED_BY_OFFER_PROVIDER = "SINGLE_OFFER_GEO_FILTERING_ERROR_NO_COUNTRY_CODES_SUPPLIED_BY_OFFER_PROVIDER";

    public static String SINGLE_OFFER_DUPLICATES_FILTERING = "SINGLE_OFFER_DUPLICATES_FILTERING";
    public static String SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_IDENTIFIED = "SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_IDENTIFIED";
    public static String SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_WITH_HP_IDENTIFIED = "SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_WITH_HP_IDENTIFIED";
    public static String SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_MARKED_FOR_REJECTION = "SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_MARKED_FOR_REJECTION";
    public static String SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_REJECTED = "SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_REJECTED";
    
    public static String SINGLE_OFFER_TARGET_DEVICE_FILTERING = "SINGLE_OFFER_TARGET_DEVICE_FILTERING";
    public static String SINGLE_OFFER_TARGET_DEVICE_FILTERING_ACCEPTED = "SINGLE_OFFER_TARGET_DEVICE_FILTERING_ACCEPTED";
    public static String SINGLE_OFFER_TARGET_DEVICE_FILTERING_ACCEPTED_NO_FILTER_DEFINED = "SINGLE_OFFER_TARGET_DEVICE_FILTERING_ACCEPTED_NO_FILTER_DEFINED";
    public static String SINGLE_OFFER_TARGET_DEVICE_FILTERING_ERROR_NO_TARGET_DEVICES_SUPPLIED_BY_OFFER_PROVIDER = "SINGLE_OFFER_TARGET_DEVICE_FILTERING_ERROR_NO_TARGET_DEVICES_SUPPLIED_BY_OFFER_PROVIDER";

    public static String SINGLE_OFFER_WALL_GENERATION_IDENTIFIED = "SINGLE_OFFER_WALL_GENERATION_IDENTIFIED";
    public static String SINGLE_OFFER_WALL_GENERATION_FAILED = "SINGLE_OFFER_WALL_GENERATION_FAILED";
    //composite offer walls requests
    public static String COW_SELECTION_ACTIVITY = "COW_SELECTION_ACTIVITY";
    public static String COW_IDS_SELECTION_IDENTIFIED = "COW_IDS_SELECTION_IDENTIFIED";
    public static String COW_IDS_SELECTION = "COW_IDS_SELECTION";
    public static String COW_SELECTION_BY_ID = "COW_SELECTION_BY_ID";
    public static String COW_SELECTION_BY_ID_IDENTIFIED = "COW_SELECTION_BY_ID_IDENTIFIED";
    public static String OFFER_REJECTED_AS_ALREADY_CONVERTED = "OFFER_REJECTED_AS_ALREADY_CONVERTED";
    //clicks
    public static String CLICK_IDENTIFIED = "CLICK_IDENTIFIED";
    public static String CONVERSION_IDENTIFIED = "CONVERSION_IDENTIFIED";
    
    public static String DEFAULT_DOMAIN_DEFAULT_NAME = "Default";
    public static String DEFAULT_DOMAIN_TEST_NAME = "Test";
    public static String DEFAULT_INFORM_SCHEMA_NAME = "Default";
    public static String DEFAULT_DEVICE_PROFILE_SCHEMA_NAME = "Default";
    public static String DEFAULT_AC_TriggerPeriodicInforms_SERVICE_NAME = "TriggerPeriodicInforms";
    public static String DEFAULT_AC_TriggerConnectionRequest_SERVICE_NAME = "TriggerConnectionRequest";
    public static String DEFAULT_AC_EnablePeriodicInforms_SERVICE_NAME = "EnablePeriodicInforms";
    
    //manual reward tool
    public static String REWARD_MANUAL_USER = "REWARD_MANUAL_USER";
    public static String REWARD_MANUAL_USER_WS_RESULT = "REWARD_MANUAL_USER_WS_RESULT";
    
    //support ticket activity
    public static String SUPPORT_TICKET_ACTIVITY = "SUPPORT_TICKET_ACTIVITY"; 
    public static String DONKEY_REQUEST = "DONKEY_REQUEST";
    public static String DONKEY_RESPONSE = "DONKEY_RESPONSE";
    public static String ZENDESK_REQUEST = "ZENDESK_REQUEST";
    public static String ZENDESK_RESPONSE = "ZENDESK_RESPONSE";

    
    public static String APPLICATION_NAME_GO_AHEAD = "GoAhead";
    public static String APPLICATION_NAME_AIR_REWARDZ = "AirRewardz";
    public static String APPLICATION_NAME_REWARDZ = "Rewardz";
    public static String APPLICATION_NAME_CINETREATS = "Cinetreats";
    //Applications
    public static String APPLICATION_VERSION_ACTIVITY = "APPLICATION_VERSION_ACTIVITY";
    public static String APPLICATION_GET_CONFIGURATION_ACTIVITY = "APPLICATION_GET_CONFIGURATION_ACTIVITY";
    
    //referral monitor
    public static String REFERRAL_MONITOR_ACTIVITY = "REFERRAL_MONITOR_ACTIVITY";
    public static String REFERRAL_MONITOR_ACTIVITY_ERROR = "REFERRAL_MONITOR_ACTIVITY_ERROR";
    public static String REFERRAL_MONITOR_TRIGGER = "REFERRAL_MONITOR_TRIGGER";
    
    //Wallet activity
    public static String WALLET_CARRIER_ACTIVITY = "WALLET_CARRIER_ACTIVITY";
    public static String WALLET_DATA_ACTIVITY = "WALLET_DATA_ACTIVITY";
    public static String WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY = "WALLET_CARRIER_PAYOUT_ACTIVITY";
    public static String WALLET_TRANSACTION_ACTIVITY = "WALLET_TRANSACTION_ACTIVITY";
    public static String WALLET_TRANSACTION_ACTIVITY_CHECK = "WALLET_TRANSACTION_ACTIVITY_CHECK";
    public static String WALLET_PAY_IN = "WALLET_PAY_IN";
    public static String WALLET_PAY_OUT = "WALLET_PAY_OUT";
    public static String WALLET_PAY_OUT_DUPLICATE = "WALLET_PAY_OUT_DUPLICATE";
    public static String WALLET_TRANSACTION_ACTIVITY_ABORTED = "WALLET_TRANSACTION_ACTIVITY_ABORTED";
    public static String WALLET_TRANSACTION_ACTIVITY_ABORTED_NOT_ENOUGH_FUNDS = "WALLET_TRANSACTION_ACTIVITY_ABORTED_NOT_ENOUGH_FUNDS";
    public static String WALLET_REWARD_ACTIVITY = "WALLET_REWARD_ACTIVITY";
    public static String WALLET_REWARD_BUY_ACTIVITY = "WALLET_REWARD_BUY_ACTIVITY";
    public static String WALLET_PAYOUT_OFFER_HISTORY = "WALLET_PAYOUT_OFFER_HISTORY";
    public static String WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE = "WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE";
    public static String WALLET_USE_PAYOUT_OFFER = "WALLET_USE_PAYOUT_OFFER";
    
    public static String CLICK_URL_ACTIVITY = "CLICK_URL_ACTIVITY";
    
    //Video
    public static String VIDEO_REWARD_ACTIVITY = "VIDEO_REWARD_ACTIVITY";
    public static String VIDEO_REWARD_ACTIVITY_ERROR = "VIDEO_REWARD_ACTIVITY_ERROR";
    public static String VIDEO_REWARD_ACTIVITY_ABORTED = "VIDEO_REWARD_ACTIVITY_ABORTED";
    public static String ERROR_SERVER_NOT_WHITE_LISTED = "ERROR_SERVER_NOT_WHITE_LISTED";
    
    //Spinner
    public static String SPINNER_GENERATOR_ACTIVITY = "SPINNER_GENERATOR_ACTIVITY";
    public static String SPINNER_MANAGER_ACTIVITY = "SPINNER_MANAGER_ACTIVITY";
    public static String SPINNER_SERVICE_ACTIVITY = "SPINNER_SERVICE_ACTIVITY";
    public static String SPINNER_USER_REWARD_ACTIVITY = "SPINNER_USER_REWARD_ACTIVITY";
    public static String SPINNER_USER_REWARD_LIST_ACTIVITY = "SPINNER_USER_REWARD_LIST_ACTIVITY";
    
    //LogsViewer
    public static final String LOGS_VIEWER_ACTIVITY = "LOGS_VIEWER_ACTIVITY";
    
    //Acra report 
    public static final String ACRA_ERROR_REPORT = "ACRA_ERROR_REPORT";
    
    public static final String REFERRAL_INFO_ACTIVITY = "REFERRAL_INFO_ACTIVITY";

	public static final String WALLET_STATUS_UPDATE = "WALLET_STATUS_UPDATE";

	public static final String WALLET_STATUS_UPDATE_CASH_RETURN = "WALLET_STATUS_UPDATE_CASH_RETURN";
	
	//MODE MOCKUP
	public static final String MODE_MOCKUP_ACTIVITY = "MODE_MOCKUP_ACTIVITY";
	
	//GAMIFICATION
	public static final String VIDEO_GAMIFICATION_ACTIVITY = "VIDEO_GAMIFICATION_ACTIVITY";
	
	public static final String GET_DEVICE_DATA = "GET_DEVICE_DATA";
	
	//QUIDCO
	public static final String QUIDCO_SERVICE_REGISTER_QUIDCO_DATA = "QUIDCO_SERVICE_REGISTER_QUIDCO_DATA";
	public static final String QUIDCO_TRANSACTION_READER_ACTIVITY = "QUIDCO_TRANSACTION_READER_ACTIVITY";
	public static final String QUIDCO_MANAGER_ACTIVITY = "QUIDCO_MANAGER_ACTIVITY";
	
	//EXTERNAL SERVER 
	public static final String EXTERNAL_SERVER_MANAGER_ACTIVITY = "EXTERNAL_SERVER_MANAGER_ACTIVITY";
	public static final String EXTERNAL_SERVER_NOT_LISTED  = "EXTERNAL_SERVER_NOT_LISTED";
	
    //MANUAL REWARD
	public static final String EVENT_MANUALLY_REWARDED = "EVENT_MANUALLY_REWARDED";
	
	public static final String LOGGER_SERVICE_LOG = "LOGGER_SERVICE_LOG";
	
	public static final String REWARD_TICKET_ACTIVITY = "REWARD_TICKET_ACTIVITY";

	//ATTENDANCE
	public static final String ATTENDANCE_ACTIVITY = "ATTENDANCE_ACTIVITY";

	
	//NOTIFICATION
	public static final String NOTIFICATION_ACTIVITY = "NOTIFICATION_ACTIVITY";

	public static final String PERSONAL_DETAILS = "PERSONAL_DETAILS";

	public static final String FYBER_CALLBACK = "FYBER_CALLBACK";

	public static final String FYBER_VIDEO_CALLBACK = "FYBER_VIDEO_CALLBACK";

	public static final String EXTERNAL_OFFER_WALL = "EXTERNAL_OFFER_WALL";

	public static final String TRIALPAY_CALLBACK = "TRIALPAY_CALLBACK";

	public static final String ADGATE_CALLBACK = "ADGATE_CALLBACK";

	public static final String PERSONALY_CALLBACK = "PERSONALY_CALLBACK";
	
    private static Application app;
    private String overrideServerName = null;
    private String firmwarePath = null;
    private int STUNPort = 0;
    private boolean autoCreateCpe = true;
    private boolean generateOffers = true;
    private boolean crfEnabled = true;
    private boolean sqlDataSizeMonitor = true;
    private String NoNATNetString = "";
    private int displayedCPELogEvents = 5;
    private int displayedAlertEvents = 5;
    private int monitoringIntervals = 5;
    private int crfIntervals = 15; //in h
    private int offerGenerationIntervals = 24; //in h
    private int sqlStorageSizeMonitoringIntervals = 15; //in mins
    private int sqlStorageHistoryLength = 14; //in days
    private int forecastingIntervals = 5;
    private int reportingIntervals = 24;
    private int serverStatusCollectionIntervals = 30;
    private int maintenanceDuration = 60;
    
    private boolean verboseLogging = false;
    private boolean cpeLoggingEnabled = false; 
    private boolean acEnabled = false;
    private boolean fcEnabled = false;
    private boolean monitoringEnabled = false;
    private boolean serverMonitoringEnabled = false;
    private boolean reportingEnabled = false;
    private boolean serverStatusCollectionEnabled = false;
    private boolean maintenanceEnabled = false;

    private String masterServerIp = null;
    //TODO should be read from the config
    private String logServerName = "airrewardz"; 
    private String logServerAddress = null;
    private String logServerBackupAddress = null;
    private String forecastingServerAddress = null;
    private String imdgServerAddress = null;
    public String connectionRequestUsername = null;
    public String connectionRequestPassword = null;
    
    private static HazelcastInstance dataGrid = null;
    private static ESLogger esLogger = null;
    
    //es indexes and types
    public static String esLogIndexName = "ab-logs";
    public static String esLogTypeName = "ab-log_data";

    public static String esInformIndexName = "ab-informs";
    public static String esInformTypeName = "ab-inform_data";

    public static String esBackupIndexName = "ab-backups";
    public static String esBackupTypeName = "ab-backup_data";

    public static String esMonitoringIndexName = "ab-alerts";
    public static String esMonitoringTypeName = "ab-alerts_data";

    public static String esServerStatsIndexName = "ab-serverstats";
    public static String esServerStatsTypeName = "ab-serverstats_data";

    //public static void init(ServletContextEvent ctx) {
    public void init(ServletContext ctx) {
        app = this;//new Application();
        app.generateOffers = app.getPropertyBool(PROPERTY_OFFER_GENERATION_ENABLED, "true");
        app.sqlDataSizeMonitor = app.getPropertyBool(PROPERTY_SQL_DATA_SIZE_MONITOR, "true");
        app.offerGenerationIntervals = app.getPropertyInt(PROPERTY_OFFER_INTERVALS, "24");
        app.masterServerIp = app.getProperty(PROPERTY_MASETER_SERVER_ADDRESS, "127.0.0.1");
        app.sqlStorageSizeMonitoringIntervals = app.getPropertyInt(PROPERTY_SQL_STORAGE_SIZE_MONITORING_INTERVALS, "15");
        app.sqlStorageHistoryLength = app.getPropertyInt(PROPERTY_SQL_DATA_STORAGE_HISTORY_LENGTH, "14");
        app.overrideServerName = app.getProperty(PROPERTY_OVERRIDESERVERNAME, null);
        app.autoCreateCpe = app.getPropertyBool(PROPERTY_AUTOCREATECPE, "true");
        app.firmwarePath = app.getProperty(PROPERTY_FIRMWAREPATH, "/mnt/data/firmware");
        app.STUNPort = app.getPropertyInt(PROPERTY_STUNPORT, "5060");
        app.verboseLogging = app.getPropertyBool(PROPERTY_VERBOSE_LOGGING, "true");
        app.cpeLoggingEnabled = app.getPropertyBool(PROPERTY_CPE_LOGGING_ENABLED, "true");
        app.logServerAddress = app.getProperty(PROPERTY_LOG_SERVER_ADDRESS, "127.0.0.1");
        app.logServerBackupAddress = app.getProperty(PROPERTY_LOG_SERVER_BACKUP_ADDRESS, "");
        app.forecastingServerAddress = app.getProperty(PROPERTY_FORECASTING_SERVER_ADDRESS, "127.0.0.1");
        app.imdgServerAddress = app.getProperty(PROPERTY_IMDG_SERVER_ADDRESS, "127.0.0.1");

        app.connectionRequestUsername = app.getProperty(PROPERTY_CONNECTION_REQUEST_USERNAME, "user");
        app.connectionRequestPassword = app.getProperty(PROPERTY_CONNECTION_REQUEST_PASSWORD, "password");

        app.acEnabled = app.getPropertyBool(PROPERTY_AC_ENABLED, "false");
        app.fcEnabled = app.getPropertyBool(PROPERTY_FC_ENABLED, "false");
        app.serverStatusCollectionEnabled = app.getPropertyBool(PROPERTY_SSC_ENABLED, "false");
        app.monitoringEnabled = app.getPropertyBool(PROPERTY_MONITORING_ENABLED, "false");
        app.serverMonitoringEnabled = app.getPropertyBool(PROPERTY_SERVER_MONITORING_ENABLED, "false");
        app.reportingEnabled = app.getPropertyBool(PROPERTY_REPORTING_ENABLED, "false");
        app.maintenanceEnabled = app.getPropertyBool(PROPERTY_MAINTENANCE_ENABLED, "false");
        
        app.displayedCPELogEvents = app.getPropertyInt(PROPERTY_DISPLAYED_CPE_LOG_EVENTS, "5");
        app.displayedAlertEvents = app.getPropertyInt(PROPERTY_DISPLAYED_ALERT_EVENTS, "5");
        app.monitoringIntervals = app.getPropertyInt(PROPERTY_MONITORING_INTERVALS, "5");
        app.forecastingIntervals = app.getPropertyInt(PROPERTY_FORECASTING_INTERVALS, "5");
        app.reportingIntervals = app.getPropertyInt(PROPERTY_REPORTING_INTERVALS, "24");
        app.monitoringIntervals = app.getPropertyInt(PROPERTY_MONITORING_INTERVALS, "5");
        app.serverStatusCollectionIntervals = app.getPropertyInt(PROPERTY_SSC_INTERVALS, "30");
        app.maintenanceDuration = app.getPropertyInt(PROPERTY_MAINTENANCE_DURATION, "60");

        logger.info("logging enabled: "+app.cpeLoggingEnabled+" verbose logging: "+app.verboseLogging+" log server address: "+app.logServerAddress+" ac enabled: "+app.acEnabled);
        logger.info("backup logging enabled: "+app.cpeLoggingEnabled+" verbose logging: "+app.verboseLogging+" backup log server address: "+app.logServerBackupAddress+" ac enabled: "+app.acEnabled);
        
        //create default profiles/services for acs
        app.initESConnectors();
        app.createDefaultRealm();
        
        
        
        
        
        //System.setProperty("http.keepAlive","false"); //enforce connections to be closed
    }

    public static synchronized ESLogger getElasticSearchLogger() {
    	if(esLogger == null) {
    		try {
        		logger.info("**************** Instantiating ElasticSearch logger on host: "+app.logServerAddress);
        		logger.info("**************** Instantiating ElasticSearch backup logger on host: "+app.logServerBackupAddress);
        		boolean initSeparateConnector = true;
        	    int pushTimeInterval = 5000;
        	    int batchSizeLimit = 1000;
        	    int batchNumberLimit = 25;
        	    int statusQuoLimit = 2;
        		esLogger = new ESLogger(app.logServerAddress, app.logServerBackupAddress, 
        				esLogIndexName, esLogTypeName, 
        				pushTimeInterval, batchSizeLimit, batchNumberLimit, statusQuoLimit);
        		Thread.sleep(TimerStartupConfig.InitWaitTime); //wait to make sure that ES logger has initialised
        		logger.info("**************** ElasticSearch logger initialised on host: "+app.logServerAddress);
    		} catch(Exception exc) {
    			exc.printStackTrace();
    			//logger.severe("Error instantiating ElasticSearch on host: "+app.logServerAddress);
    			esLogger = null;
    		}
    	} 
    	
    	return esLogger;
    }

    public static synchronized Client getESClient() {
    	if(esClient == null) {
    		try {
        		logger.info("*********************** Instantiating ElasticSearch connection client on host: "+app.logServerAddress);
        		Settings settings; 
        		// once we find one node in the cluster ask about the others      
        		Builder settingsBuilder = 
        		ImmutableSettings.settingsBuilder().put("client.transport.sniff", true); 
        		settingsBuilder.put("cluster.name", "airrewardz"); 
        		settingsBuilder.put("client.transport.ping_timeout", "10s");
        		settingsBuilder.put("http.enabled", "false");
        		settingsBuilder.put("transport.tcp.port", "9300-9400");
        		settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
        		settingsBuilder.put("discovery.zen.ping.unicast.hosts", app.logServerAddress);
        		settings = settingsBuilder.build(); 

        		esClient = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(app.logServerAddress, 9300));
    	    	Application.getElasticSearchLogger().indexLog(Application.LOG_ANALYTICS_CONNECTOR, -1, 
    	    			LogStatus.OK, 
    	    			"LOG_ANALYTICS_CONNECTOR succesfully established analytics connection to ES");

        		Thread.sleep(TimerStartupConfig.InitWaitTime); //wait to make sure that ES logger has initialised
        		logger.info("*********************** ElasticSearch connection client initialised on host: "+app.logServerAddress);
    		} catch(Exception exc) {
    			logger.severe("Error instantiating ElasticSearch connection client on host: "+app.logServerAddress);
    			esClient = null;
    		}
    	} 
    	
    	return esClient;
    }

    public Application() { }
    
    private void initESConnectors() {
    	logger.info("Initialising ES connectors...");
    	Application.getElasticSearchLogger().indexLog("ES", -1, LogStatus.OK, "ESClient successfully initialised by Application class...");
    	getESClient(); //init es connector used for analytics
    }
   
	
    public void createDefaultRealm() {
    	
    	RealmEntity realm = null;
    	Vector rolesSU = null;
    	Vector rolesAdmin = null;
    	Vector rolesUser = null;
    	
        try {
        	//create default realm
        	if (daoAppUser.findGuest() == null){
        		logger.info("**** INSERTING GUEST USER *****");
        		daoAppUser.insertGuest();
        		logger.info("****GUEST INSERTED****");
        	}
        	
            realm = daoRealm.findByName("BPM");
            if(realm == null) {

            	//create default roles that exist within the system
            	RoleEntity roleSU = new RoleEntity();
            	roleSU.setName(UserRoles.SUPER_USER.toString());
            	roleSU.setDisplayName("Super User");
            	roleSU.setDescription("Role enabling full access to platform functionality");
            	roleSU= daoRole.createOrUpdate(roleSU);
            	
            	//create default roles that exist within the system
            	RoleEntity roleAdmin = new RoleEntity();
            	roleAdmin.setName(UserRoles.ADMIN.toString());
            	roleAdmin.setDisplayName("System Admin");
            	roleAdmin.setDescription("Role enabling full access to system functionality");
            	roleAdmin = daoRole.createOrUpdate(roleAdmin);

            	RoleEntity roleUser = new RoleEntity();
            	roleUser.setName(UserRoles.USER.toString());
            	roleUser.setDisplayName("System User");
            	roleUser.setDescription("Role enabling access to system functionality for normal users");
            	roleUser = daoRole.createOrUpdate(roleUser);

            	rolesSU = new Vector();
            	rolesSU.add(roleSU);
            	
            	rolesAdmin = new Vector();
            	rolesAdmin.add(roleAdmin);
            	
            	rolesAdmin = new Vector();
            	rolesAdmin.add(roleAdmin);
            	
            	rolesUser = new Vector();
            	rolesUser.add(roleUser);

            	//create realm
            	realm = new RealmEntity();
            	realm.setName("BPM");
            	realm.setConnectionTimeout(10);
            	realm.setReadTimeout(10);
            	realm.setModeBPUser("");
            	realm.setModeBPPassword("");
            	realm.setModeCreditUrl("");
            	realm = daoRealm.createOrUpdate(realm);
                logger.info("Created realm...");
          
                //add customers
                Vector customers = new Vector();
                //customer 1
                UserEntity customer = new UserEntity();
                customer.setName("Normal user name");
                customer.setEmail("Normal user e-mail address");
                customer.setLogin("user");
                customer.setPassword(secManager.generateStrongPasswordHash("afm!@#"));
                customer.setRealm(realm);
                customer.setRoles(rolesUser);
                customer = daoCustomer.createOrUpdate(customer);
                customers.add(customer);
                
                customer = new UserEntity();
                customer.setName("Admin user name");
                customer.setEmail("Admin user e-mail address");
                customer.setLogin("admin");
                customer.setPassword(secManager.generateStrongPasswordHash("afm!@#"));
                customer.setRealm(realm);
                customer.setRoles(rolesAdmin);
                customer = daoCustomer.createOrUpdate(customer);
                customers.add(customer);

                customer = new UserEntity();
                customer.setName("SuerUser name");
                customer.setEmail("SuerUser user e-mail address");
                customer.setLogin("su");
                customer.setPassword(secManager.generateStrongPasswordHash("afm!@#"));
                customer.setRealm(realm);
                customer.setRoles(rolesSU);
                customer = daoCustomer.createOrUpdate(customer);
                customers.add(customer);
                
            	logger.info("Assigned default users to realm...");

                //realm.setDomains(adProviders);
            	logger.info("Assigned default ad providers domains to realm...");

            	realm = daoRealm.createOrUpdate(realm);
            	
            	String defaultRewardTypeName="Default reward type";
            	RewardTypeEntity rt = daoRewardType.findByRealmIdAndName(realm.getId(), defaultRewardTypeName);
            	if(rt == null) {
            		rt = new RewardTypeEntity();
            		rt.setRealm(realm);
            		rt.setName(defaultRewardTypeName);
            		rt.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            		daoRewardType.create(rt);
            	}
            	logger.info("Created default rewardType and assigned to app realm...");
            	
            	DenominationModelEntity dm = daoDenominationModel.findBySourcePayoutCurrencyCodeAndRewardTypeNameAndRealmId(
            			true,
            			"USD", 
            			defaultRewardTypeName, realm.getId());
            	if(dm == null) {
            		DefaultDenominationModel defaultModel = new DefaultDenominationModel();
            		DenominationModelTable modelTable = new DenominationModelTable();
            		modelTable.setRows(defaultModel.getListRows());
            		dm = new DenominationModelEntity();
            		dm.setName(defaultRewardTypeName);
            		dm.setDefaultModel(true);//set as default model
            		dm.setRealm(realm);
            		dm.setContent(serDeDenominationModelTabe.serialize(modelTable));
            		dm.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            		dm.setSourcePayoutCurrencyCode("USD");
            		dm.setTargetPayoutCurrencyCode("GBP");
            		dm.setRewardTypeName(defaultRewardTypeName);
            		daoDenominationModel.create(dm);
                	logger.info("Created default Denomination Model and assigned to app realm...");
            	} else {
                	logger.info("Default Denomination Model already exists...");
            	}
            	
            	//create supported currencies config            	
            	currencyConfigurationEntity = daoCurrencyCode.findByRealmId(realm.getId());
            	if(currencyConfigurationEntity == null) {
            		currencyConfigurationEntity = new CurrencyCodeEntity();
            		currencyConfigurationEntity.setRealm(realm);
            		currencyConfigurationEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            		CurrencyCode cc = new CurrencyCode();
            		cc.setCode("USD");
            		cc.setPayoutTreshold(0.1);
            		listCurrencyCodes.add(cc);
            		cc = new CurrencyCode();
            		cc.setCode("USD");
            		cc.setPayoutTreshold(5.0);
            		listCurrencyCodes.add(cc);
            		CurrencyCodes codesConfig = new CurrencyCodes();
            		codesConfig.setListCodes(listCurrencyCodes);
            		String strCurrencyConfig = serDeCurrencyCode.serialize(codesConfig);
            		currencyConfigurationEntity.setSupportedCurrencies(strCurrencyConfig);
            		daoCurrencyCode.createOrUpdate(currencyConfigurationEntity);
            	}

            	//create blocked offers config            	
            	BlockedOffersEntity blockedOffersEntity = daoBlockedOffers.findByRealmId(realm.getId());
            	if(blockedOffersEntity == null) {
            		blockedOffersEntity = new BlockedOffersEntity();
            		blockedOffersEntity.setRealmId(realm.getId());
            		BlockedOffers blockedOffers = new BlockedOffers();
            		String strBlockedOffers = serDeBlockedOffers.serialize(blockedOffers);
            		blockedOffersEntity.setContent(strBlockedOffers);
            		blockedOffersEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            		daoBlockedOffers.createOrUpdate(blockedOffersEntity);
            	}

            	//create custom denomination models config            	
            	CustomDenominationModelEntity customDenominationModelEntity = daoCustomDenominationModel.findByRealmId(realm.getId());
            	if(customDenominationModelEntity == null) {
            		customDenominationModelEntity = new CustomDenominationModelEntity();
            		customDenominationModelEntity.setRealmId(realm.getId());
            		CustomDenominationModelAssignments dh = new CustomDenominationModelAssignments();
            		String strDH = serDeCustomDenominationModelAssignments.serialize(dh);
            		customDenominationModelEntity.setContent(strDH);
            		customDenominationModelEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            		daoCustomDenominationModel.createOrUpdate(customDenominationModelEntity);
            	}

            	//create offer filter config
            	offerFilter = daoOfferFilter.findByRealmId(realm.getId());
            	if(offerFilter == null){
            		offerFilter = new OfferFilterEntity();
            		offerFilter.setRealm(realm);
            		daoOfferFilter.createOrUpdate(offerFilter);
            	}
            	
            	//create monitoring setup config
            	monitoringSetup = daoMonitoringSetup.findByRealmId(realm.getId());
            	if(monitoringSetup == null) {
            		monitoringSetup = new MonitoringSetupEntity();
            		monitoringSetup.setRealm(realm);
            		monitoringSetup.setEmailNotificationActive(true);
            		monitoringSetup.setAlertEmails("");
            		monitoringSetup.setOperationStatusReportEmails("mariusz.jacyno@gmail.com");
            		monitoringSetup.setMailFromAddress("audrius.vetsikas@gmail.com");
            		monitoringSetup.setMailAccountUserName("audrius.vetsikas@gmail.com");
            		monitoringSetup.setMailAccountPassword("3okidoki3");
            		monitoringSetup.setSmtpHost("smtp.gmail.com");
            		monitoringSetup.setSmtpAuth("TRUE");
            		monitoringSetup.setSmtpTTLS("TRUE");
            		monitoringSetup.setSmtpPort("587");
            		daoMonitoringSetup.createOrUpdate(monitoringSetup);
            	}
            } else {
            	logger.info("realm already exists...");
            }

            logger.info("Registered users:");
            Collection<UserEntity> customers = daoCustomer.findAll();
            for(UserEntity c:customers) {
            	logger.info("Customer name: "+c.getName()+" network: "+c.getRealm().getName());
            	RealmEntity r = c.getRealm();
            	Collection<AdProviderEntity> domains = daoAdProvider.findAllByRealmId(r.getId());
            	for(AdProviderEntity d:domains) {
            		logger.info("-> Domain: "+d.getName()+" assigned to realm: "+d.getRealm().getName());
            	}
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(ex.toString());
        }
    }

    private void _destroy(ServletContextEvent ctx) {
    }

    public static void destroy(ServletContextEvent ctx) {
        app._destroy(ctx);
        logger.info("Application.contextDestroyed " + ctx.getServletContext().getContextPath());
        logger.info("stopping inform and logs loggers...");
        esLogger.stop();
    }

    public static Application getApplication() {
        return app;
    }

    public static String getOverrideServerName() {
        return app.overrideServerName;
    }

    public static String getConnectionRequestUsername() {
        return app.connectionRequestUsername;
    }

    public static String getConnectionRequestPassword() {
        return app.connectionRequestPassword;
    }

    public static String getFirmwarePath() {
        String fwbase = app.firmwarePath;
        if (fwbase == null) {
            fwbase = "c:/firmware/";
        }
        String osname = (String) System.getProperty("os.name");
        if (!osname.startsWith("Windows") && fwbase.length() >= 2 && fwbase.charAt(1) == ':') {
            fwbase = fwbase.substring(2);
        }
        if (!fwbase.endsWith(File.separator)) {
            fwbase += File.separator;
        }
//        System.out.println ("fwbase="+fwbase);
        return fwbase;
    }

    public static void setFirmwarePath(String firmwarePath) throws CreateException {
        app.firmwarePath = firmwarePath;
        app.setProperty(PROPERTY_FIRMWAREPATH, firmwarePath);
    }

    private boolean getPropertyBool(String name, String defvalue) {
        String p = getProperty(name, defvalue);
        return Boolean.parseBoolean(p);
    }

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

    public static void setSTUNport(int STUNPort) throws Exception {
        app.STUNPort = STUNPort;
        app.setProperty(PROPERTY_STUNPORT, Integer.toString(STUNPort));
    }

    public static int getSTUNport() {
        return app.STUNPort;
    }

    public static void setMonitoringIntervas(int intervals) throws Exception {
        app.monitoringIntervals = intervals;
        app.setProperty(PROPERTY_MONITORING_INTERVALS, Integer.toString(intervals));
    }

    public static int getMonitoringIntervals() {
        return app.monitoringIntervals;
    }

    public static void setOfferWallIntervals(int intervals) throws Exception {
        app.offerGenerationIntervals = intervals;
        app.setProperty(PROPERTY_OFFER_INTERVALS, Integer.toString(intervals));
    }

    public static int getOfferWallIntervals() {
        return app.offerGenerationIntervals;
    }

    public static void setCRFIntervals(int intervals) throws Exception {
        app.crfIntervals = intervals;
        app.setProperty(PROPERTY_CRF_INTERVALS, Integer.toString(intervals));
    }

    public static int getCRFIntervals() {
        return app.crfIntervals;
    }

    public static void setSQLStorageSizeMonitoringIntervals(int intervals) throws Exception {
        app.sqlStorageSizeMonitoringIntervals = intervals;
        app.setProperty(PROPERTY_SQL_STORAGE_SIZE_MONITORING_INTERVALS, Integer.toString(intervals));
    }

    public static int getSQLStorageSizeMonitoringIntervals() {
        return app.sqlStorageSizeMonitoringIntervals;
    }

    public static void setSQLStorageHistoryLenght(int intervals) throws Exception {
        app.sqlStorageHistoryLength = intervals;
        app.setProperty(PROPERTY_SQL_DATA_STORAGE_HISTORY_LENGTH, Integer.toString(intervals));
    }

    public static int getSQLStorageHistoryLenght() {
        return app.sqlStorageHistoryLength;
    }

    public static void setForecastingIntervas(int intervals) throws Exception {
        app.forecastingIntervals = intervals;
        app.setProperty(PROPERTY_FORECASTING_INTERVALS, Integer.toString(intervals));
    }

    public static int getForecastingIntervals() {
        return app.forecastingIntervals;
    }

    public static void setMaintenanceDuration(int duration) throws Exception {
        app.maintenanceDuration = duration;
        app.setProperty(PROPERTY_MAINTENANCE_DURATION, Integer.toString(duration));
    }

    public static int getMaintenanceDuration() {
        return app.maintenanceDuration;
    }

    public static void setServerStatusCollectionIntervas(int intervals) throws Exception {
        app.serverStatusCollectionIntervals = intervals;
        app.setProperty(PROPERTY_SSC_INTERVALS, Integer.toString(intervals));
    }

    public static int getServerStatusCollectionIntervals() {
        return app.serverStatusCollectionIntervals;
    }

    public static void setReportingIntervas(int intervals) throws Exception {
        app.reportingIntervals = intervals;
        app.setProperty(PROPERTY_REPORTING_INTERVALS, Integer.toString(intervals));
    }

    public static int getReportingIntervals() {
        return app.reportingIntervals;
    }

    public static void setDisplayedCPELogEvents(int events) throws Exception {
        app.displayedCPELogEvents = events;
        app.setProperty(PROPERTY_DISPLAYED_CPE_LOG_EVENTS, Integer.toString(events));
    }

    public static int getDisplayedCPELogEvents() {
        return app.displayedCPELogEvents;
    }

    public static void setDisplayedAlertEvents(int events) throws Exception {
        app.displayedAlertEvents = events;
        app.setProperty(PROPERTY_DISPLAYED_ALERT_EVENTS, Integer.toString(events));
    }

    public static int getDisplayedAlertEvents() {
        return app.displayedAlertEvents;
    }

    private String getProperty(String name, String defvalue) {
        try {
            PropertyEntity pl = daoProperty.findByPK(0, PropertyEntity.TYPE_APPLICATION, name);
            return pl.getValue();
        } catch (Exception ex) {
            return defvalue;
        }
    }

    private void setProperty(String name, String value) throws CreateException {
        PropertyEntity pl = null;
        try {
            pl = daoProperty.findByPK(0, PropertyEntity.TYPE_APPLICATION, name);
            pl.setValue(value);
            daoProperty.update(pl);
        } catch (Exception ex) {
        	PropertyEntity entityProperty = new PropertyEntity(0, PropertyEntity.TYPE_APPLICATION, name, value);
        	daoProperty.create(entityProperty);
        }
    }

    public static void setOverrideServerName(String overrideServerName) throws CreateException {
        app.overrideServerName = overrideServerName;
        app.setProperty(PROPERTY_OVERRIDESERVERNAME, overrideServerName);
    }

    public static void setConnectionRequestUsername(String connectionRequestUsername) throws CreateException {
        app.connectionRequestUsername = connectionRequestUsername;
        app.setProperty(PROPERTY_CONNECTION_REQUEST_USERNAME, connectionRequestUsername);
    }

    public static void setConnectionRequestPassword(String connectionRequestPassword) throws CreateException {
        app.connectionRequestPassword = connectionRequestPassword;
        app.setProperty(PROPERTY_CONNECTION_REQUEST_PASSWORD, connectionRequestPassword);
    }

    public static String getNoNATNet() {
        return app.NoNATNetString;
    }

    public static void updateServerHealthStats(Level level)
    {
      	if(level == Level.INFO || level == Level.FINE || level == Level.FINER || level == Level.FINEST) 
    	{
			//mzj server health stats performance log
    	}
    	else if(level == Level.WARNING)
    	{
			//mzj server health stats performance log
    	}
    	else if(level == Level.SEVERE)
    	{
			//mzj server health stats performance log
    	}
    }

    public static boolean getGenerateOffers() {
    	return app.generateOffers;
    }

    public static void setGenerateOffers(Boolean generateOffersRef) throws Exception {
    	app.generateOffers = generateOffersRef;
    	app.setProperty(PROPERTY_OFFER_GENERATION_ENABLED, generateOffersRef.toString());
    }

    public static boolean getCRFEnabled() {
    	return app.crfEnabled;
    }

    public static void setCRFEnabled(Boolean value) throws Exception {
    	app.crfEnabled = value;
    	app.setProperty(PROPERTY_CRF_ENABLED, value.toString());
    }

    public static boolean isSQLDataSizeMonitorEnabled() {
    	return app.sqlDataSizeMonitor;
    }

    public static void setSQLDataSizeMonitorEnabled(Boolean val) throws Exception {
    	app.sqlDataSizeMonitor = val;
    	app.setProperty(PROPERTY_SQL_DATA_SIZE_MONITOR, val.toString());
    }

    public static boolean getAutoCreateCpe() {
//        System.out.println ("autoCreateCPE="+app.autoCreateCpe);
        return app.autoCreateCpe;
    }

        public static void setAutoCreateCpe(Boolean autoCreateCpe) throws Exception {
        app.autoCreateCpe = autoCreateCpe;
        app.setProperty(PROPERTY_AUTOCREATECPE, autoCreateCpe.toString());
    }
    
    public static boolean isVerboseLogging() {
		return app.verboseLogging;
	}

    public static void setVerboseLogging(Boolean verboseLogging) throws Exception {
        app.verboseLogging = verboseLogging;
        app.setProperty(PROPERTY_VERBOSE_LOGGING, verboseLogging.toString());
    }

    public static boolean isCpeLoggingEnabled() {
		return app.cpeLoggingEnabled;
	}

    public static void setCpeLoggingEnabled(Boolean cpeLoggingEnabled) throws CreateException {
        app.cpeLoggingEnabled = cpeLoggingEnabled;
        app.setProperty(PROPERTY_CPE_LOGGING_ENABLED, cpeLoggingEnabled.toString());
    }

    public static String getLogServerName() {
		return app.logServerName;
	}

    public static String getLogServerAddress() {
		return app.logServerAddress;
	}

	public static void setLogServerAddress(String logServerAddress) throws Exception {
        app.logServerAddress = logServerAddress;
        app.setProperty(PROPERTY_LOG_SERVER_ADDRESS, logServerAddress);
	}

    public static String getLogServerBackupAddress() {
		return app.logServerBackupAddress;
	}

	public static void setLogServerBackupAddress(String logServerBackupAddress) throws Exception {
        app.logServerBackupAddress = logServerBackupAddress;
        app.setProperty(PROPERTY_LOG_SERVER_BACKUP_ADDRESS, logServerBackupAddress);
	}

    public static String getForecastingServerAddress() {
		return app.forecastingServerAddress;
	}

	public static void setForecastingServerAddress(String forecastingServerAddress) throws Exception {
        app.forecastingServerAddress = forecastingServerAddress;
        app.setProperty(PROPERTY_FORECASTING_SERVER_ADDRESS, forecastingServerAddress);
	}

    public static String getIMDGServerAddress() {
		return app.imdgServerAddress;
	}

	public static void setIMDGServerAddress(String imdgServerAddress) throws Exception {
        app.imdgServerAddress = imdgServerAddress;
        app.setProperty(PROPERTY_IMDG_SERVER_ADDRESS, imdgServerAddress);
	}

    public static String getMasterServerIp() {
		return app.masterServerIp;
	}

	public static void setMasterServerIp(String masterServerIpRef) throws Exception {
        app.masterServerIp = masterServerIpRef;
        app.setProperty(PROPERTY_MASETER_SERVER_ADDRESS, masterServerIpRef);
	}

    public static boolean isAcEnabled() {
		return app.acEnabled;
	}
    
    public static void setAcEnabled(Boolean acEnabled) throws Exception {
        app.acEnabled = acEnabled;
        app.setProperty(PROPERTY_AC_ENABLED, acEnabled.toString());
    }

    public static boolean isFcEnabled() {
		return app.fcEnabled;
	}

    public static void setFcEnabled(Boolean fcEnabled) throws Exception {
        app.fcEnabled = fcEnabled;
        app.setProperty(PROPERTY_FC_ENABLED, fcEnabled.toString());
    }

    public static boolean isMaintenanceEnabled() {
		return app.maintenanceEnabled;
	}

    public static void setMaintenanceEnabled(Boolean maintenanceEnabled) throws Exception {
        app.maintenanceEnabled = maintenanceEnabled;
        app.setProperty(PROPERTY_MAINTENANCE_ENABLED, maintenanceEnabled.toString());
    }

    public static boolean isServerStatusCollectionEnabled() {
		return app.serverStatusCollectionEnabled;
	}

    public static void setServerStatusCollectionEnabled(Boolean serverStatusCollectionEnabled) throws Exception {
        app.serverStatusCollectionEnabled = serverStatusCollectionEnabled;
        app.setProperty(PROPERTY_SSC_ENABLED, serverStatusCollectionEnabled.toString());
    }

    public static boolean isMonitoringEnabled() {
		return app.monitoringEnabled;
	}

    public static boolean isReportingEnabled() {
		return app.reportingEnabled;
	}

    public static void setMonitoringEnabled(Boolean monitoringEnabled) throws Exception {
        app.monitoringEnabled = monitoringEnabled;
        app.setProperty(PROPERTY_MONITORING_ENABLED, monitoringEnabled.toString());
    }

    public static void setReportingEnabled(Boolean reportingEnabled) throws Exception {
        app.reportingEnabled = reportingEnabled;
        app.setProperty(PROPERTY_REPORTING_ENABLED, reportingEnabled.toString());
    }

    public static boolean isServerMonitoringEnabled() {
		return app.serverMonitoringEnabled;
	}

    public static void setServerMonitoringEnabled(Boolean monitoringEnabled) throws Exception {
        app.serverMonitoringEnabled = monitoringEnabled;
        app.setProperty(PROPERTY_SERVER_MONITORING_ENABLED, monitoringEnabled.toString());
    }

}
