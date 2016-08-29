package is.ejb.bl.eventQueue;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationNameEnum;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.business.WalletTransactionStatus;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.referral.ReferralManager;
import is.ejb.bl.rewardSystems.mode.TestModeManager;
import is.ejb.bl.rewardSystems.radius.RadiusProvider;
import is.ejb.bl.spinner.SpinnerRewardType;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.bl.testing.TestManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOEventQueueEntity;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOSpinnerData;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.ejb.dl.entities.WalletPayoutOfferTransactionEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.bcel.generic.ISUB;
import org.zendesk.client.v2.model.Ticket;

@Stateless
public class EventQueueManager {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private TestManager testManager;

	public boolean validateEventPushToMode(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());

			//check if there is a request that was send in the last 72 minutes and has not gotten response back for the same user
			Timestamp last72Mins = new Timestamp(System.currentTimeMillis() - 72*60*1000);
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());

			Application.getElasticSearchLogger().indexLog(
					Application.EVENT_QUEUE_ACTIVITY,
					event.getRealmId(),
					LogStatus.OK,
					Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK + " " 
							+ " checking if request is to be pushed to mode internalT: " + event.getInternalTransactionId()
							+ " current time: "+currentTime.toString()+" last 72m time: "+last72Mins.toString()
							+ " issuing credit request for payout: " + event.getOfferPayout()
							+ " (in target currency: " + event.getRevenueValue() 
							+ " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() 
							+ " rewardType: "+ event.getRewardTypeName() 
							+ " reward: " + event.getRewardValue() 
							+ " reward currency: "+ event.getRewardIsoCurrencyCode());

			//check if there is a request that was send in the last 72 minutes and has not gotten response back for the same user
			int sentRequests = daoUserEvent.countStillProcessingEventsOnMode(event.getUserId(), last72Mins, RespStatusEnum.SUCCESS.toString());
			
			Application.getElasticSearchLogger().indexLog(
					Application.EVENT_QUEUE_ACTIVITY,
					event.getRealmId(),
					LogStatus.OK,
					Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK + " " 
							+ " checking if request is to be pushed to mode internalT: " + event.getInternalTransactionId()
							+ " current time: "+currentTime.toString()+" last 72m time: "+last72Mins.toString()
							+ " identified number of already sent to mode transactions: "+sentRequests
							+ " issuing credit request for payout: " + event.getOfferPayout()
							+ " (in target currency: " + event.getRevenueValue() 
							+ " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() 
							+ " rewardType: "+ event.getRewardTypeName() 
							+ " reward: " + event.getRewardValue() 
							+ " reward currency: "+ event.getRewardIsoCurrencyCode());
			
			if(sentRequests > 0) {
				Application.getElasticSearchLogger().indexLog(
						Application.EVENT_QUEUE_ACTIVITY,
						event.getRealmId(),
						LogStatus.WARNING,
						Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK + " " 
								+ " there are pending requests for this user: "+sentRequests+ " aborting sending following task to mode: "
								+ " internalT: " + event.getInternalTransactionId()
								+ " current time: "+currentTime.toString()+" last 72m time: "+last72Mins.toString()
								+ " identified number of already sent to mode transactions: "+sentRequests
								+ " issuing credit request for payout: " + event.getOfferPayout()
								+ " (in target currency: " + event.getRevenueValue() 
								+ " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() 
								+ " rewardType: "+ event.getRewardTypeName() 
								+ " reward: " + event.getRewardValue() 
								+ " reward currency: "+ event.getRewardIsoCurrencyCode());
				return false;
			} else {
				return true;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(
					Application.EVENT_QUEUE_ACTIVITY,
					event.getRealmId(),
					LogStatus.ERROR,
					Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
							+ " server error: "+exc.toString()
							+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
							+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
							+ event.getRewardTypeName() + " reward: " + event.getRewardValue());
			//do not push that request to mode as there was an error  
			return false;
		}
	}
	
	public boolean pushEventToModeDeprecated(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());

			String carrierName = "Unknown";
			try {
				UserEventEntity recentClickEvent = daoUserEvent.findMostRecentOfferClickEvent(event.getUserId(), "INSTALL");
				if (recentClickEvent != null) {
					carrierName = recentClickEvent.getCarrierName();
				}
			} catch (Exception exc) {
				logger.severe("error when retrieving most recent install event from user with id: " + event.getUserId() + " error: " + exc.toString());
				exc.printStackTrace();
			}

			Application.getElasticSearchLogger().indexLog(
					Application.EVENT_QUEUE_ACTIVITY,
					event.getRealmId(),
					LogStatus.OK,
					Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK + " " 
							+ " checking if request is to be pushed to mode internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
							+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
							+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
							+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);

			// extract mode configuration
			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();
			String url = realm.getModeCreditUrl();

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(realm.getConnectionTimeout() * 1000);
			con.setReadTimeout(realm.getReadTimeout() * 1000);
			// add reuqest header
			con.setRequestMethod("POST");
			String urlParameters = "MSISDN=" + event.getPhoneNumberExt() + event.getPhoneNumber() + "&OriginTransactionID=" + event.getId()
					+ // "&OriginTransactionID="+event.getInternalTransactionId()+
					"&Reward=" + event.getRewardValue() + "&ISOCurrCode=" + event.getRewardIsoCurrencyCode() + "&User=" + bpUser + "&Password="
					+ bpPass + "&Operator=" + carrierName;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			String responseString = "OK";
			String statusMessage = responseString;

			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				responseString = response.toString();
				// code 200 and status 0 - request to credit user received
				// successfully
				// code 200 and status 1 - request with similar transaction id
				// already exists
				// code 403 - authentication failure
				String STATUS_FAILED = "\"Status\":1"; // request with similar
														// transaction id
														// already exists
				statusMessage = "Unable to parse";
				try {
					statusMessage = responseString.substring(responseString.indexOf("Msg\":") + 6,
							responseString.indexOf(",\"OriginTransactionID") - 1);
					Application.getElasticSearchLogger().indexLog(
							Application.EVENT_QUEUE_ACTIVITY,
							event.getRealmId(),
							LogStatus.OK,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
									+ " mode resp: "+statusMessage
									+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
									+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
									+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);

					//indicate that mode responded OK and we can send that event to mode immediately
					return true;
				} catch (Exception exc) {
					logger.severe("Error parsing response from mode: "+exc.toString());
					exc.printStackTrace();
					
					Application.getElasticSearchLogger().indexLog(
							Application.EVENT_QUEUE_ACTIVITY,
							event.getRealmId(),
							LogStatus.ERROR,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
									+ " error: "+exc.toString()
									+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
									+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
									+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);
					//do not push that request to mode as there was an error
					return false;
				}
			} else {
				if (responseCode == 403) {
					
					Application.getElasticSearchLogger().indexLog(
							Application.EVENT_QUEUE_ACTIVITY,
							event.getRealmId(),
							LogStatus.ERROR,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
									+ " mode resp: 403"
									+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
									+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
									+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);

					//do not push that request to mode as there was an error  
					return false;
				} else {
					Application.getElasticSearchLogger().indexLog(
							Application.EVENT_QUEUE_ACTIVITY,
							event.getRealmId(),
							LogStatus.ERROR,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
									+ " mode resp: UNKNOWN"
									+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
									+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
									+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);
					//do not push that request to mode as there was an error  
					return false;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(
					Application.EVENT_QUEUE_ACTIVITY,
					event.getRealmId(),
					LogStatus.ERROR,
					Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_PUSH_CHECK_RESP + " " 
							+ " server error: "+exc.toString()
							+ " internalT: " + event.getInternalTransactionId() + " issuing credit request for payout: " + event.getOfferPayout()
							+ " (in target currency: " + event.getRevenueValue() + " phone: " + event.getPhoneNumberExt()+" "+event.getPhoneNumber() + " rewardType: "
							+ event.getRewardTypeName() + " reward: " + event.getRewardValue());
			//do not push that request to mode as there was an error  
			return false;
		}
	}
}
