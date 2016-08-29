package is.ejb.bl.notificationSystems;

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
import is.ejb.bl.currencyCodes.CurrencyCodeConverter;
import is.ejb.bl.gamification.GamificationManager;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.rewardSystems.mode.TestModeManager;
import is.ejb.bl.rewardSystems.radius.RadiusProvider;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.bl.system.support.donky.DonkyManager;
import is.ejb.bl.testing.TestManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.bcel.generic.ISUB;
import org.zendesk.client.v2.model.Ticket;

@Stateless
public class NotificationManager {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	NotificationMessageDictionary notificationMessageDictionary;

	@Inject
	GamificationManager gamificationManager;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private SpinnerManager spinnerManager;

	private final String AIR_REWARDZ_DONKY_API_KEY = "lOYyOYjTwK234J7w0nIkiGQyuuspPg95eHpvsx6+GJbfcMbMTSII3AtCzkSUnvkl9++lFv+CLgJ2dodMSsuQ";
	private final String GO_AHEAD_DONKY_API_KEY = "4f603F1fxZOdM+HiYOr0lgcJWgT5KJI4ctdRjuA5L3zY4cY9yjxeye58Sd0DocTfhUfUtGF21KqanWlKJJNDw";
	private final String CINETREATS_DONKY_API_KEY = "sUeUt4uFhR156ziOXZe0F8HwY80ZoNQsNR7fFVFqfbtcrklL3NJrpmppqG1g3H4KHCynzhdiNqUs8Ikf6KkBIA";

	public void sendRewardNotification(UserEventEntity event, boolean isSuccess,
			boolean isEventFromUserThatWasInviting) {

		// issue donkey trigger notification via chatz
		if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
			triggerDonkeyNotification(event, isSuccess, NotificationType.REWARD_INSTANT.toString(),
					isEventFromUserThatWasInviting);
		} else if (event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
			triggerDonkeyNotification(event, isSuccess, NotificationType.REWARD_VIA_REFERRAL_REGISTRATION.toString(),
					isEventFromUserThatWasInviting);
		}
		if (event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_IN.toString())) {
			triggerDonkeyNotification(event, isSuccess, NotificationType.REWARD_VIA_WALLET_PAYIN.toString(),
					isEventFromUserThatWasInviting);
		}
		if (event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
			triggerDonkeyNotification(event, isSuccess, NotificationType.WALLET_PAYOUT.toString(),
					isEventFromUserThatWasInviting);
		}
		if (event.getUserEventCategory().equals(UserEventCategory.SNAPDEAL.toString())) {
			triggerDonkeyNotification(event, isSuccess, NotificationType.SNAPDEAL_EVENT_CHANGE.toString(),
					isEventFromUserThatWasInviting);
		}

		// trigger mobile app notification
		try {
			String notificationPayload = "";

			if (event.getRewardTypeName().equals("Trippa-GB")) {
				if (isSuccess) {
					notificationPayload = "The Trippa Reward " + event.getRewardName()
							+ " you just cashed in is now valid for use on your next ticket purchase";
				} else {
					notificationPayload = "The Trippa Reward " + event.getRewardName()
							+ " you just cashed in was not valid - please contact support@trippareward.com so we can fix this for you";
				}
			} else if (event.getRewardTypeName().toLowerCase().contains("cinetreats")) {
				if (isSuccess) {
					notificationPayload = "You have received your Stubs Reward. Your wallet in the Cinetreats app has been recharged with "
							+ event.getCustomRewardValue() + " stubs";
				} else {
					notificationPayload = " Your Stubs Reward: " + event.getOfferTitle()
							+ " has not made it to your wallet. Please contact support@cinetreats.co.uk so we can fix this for you.";
				}
			} else {
				if (isSuccess) {
					notificationPayload = "You have been rewarded "
							+ CurrencyCodeConverter.getUserFriendlyCurrencyCodeByGeo(event.getRewardIsoCurrencyCode())
							+ " " + event.getRewardValue();
				} else {
					notificationPayload = "Your reward has failed - please contact support@airrewardz.net for help";
				}
			}
			if (event.getUserEventCategory().equals(UserEventCategory.SNAPDEAL.toString())){
				notificationPayload = "Hot offer status changed. Check chatz.";
			}
			

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " "
							+ Application.REWARD_NOTIFICATION_SUCCESS + " "
							+ " attempting to send reward notification for device type: " + event.getDeviceType()
							+ " phone number: " + event.getPhoneNumber() + " internalT: "
							+ event.getInternalTransactionId());

			if (event.getDeviceType().toUpperCase().equals(DeviceType.iOS.toString().toUpperCase())) {
				if (event.getIosDeviceToken() == null) {
					event = updateIosDeviceTokenInEvent(event);
				}

				// push notification
				String rootDir = new java.io.File(".").getCanonicalPath();
				IOSNotificationSender iOSNotificationSender = new IOSNotificationSender();
				String resultPayload = iOSNotificationSender.pushNotification(rootDir, event.getIosDeviceToken(),
						notificationPayload);

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " "
								+ Application.REWARD_NOTIFICATION_SUCCESS + " "
								+ " successfully issued reward notification for device type: " + event.getDeviceType()
								+ " status: " + RespStatusEnum.SUCCESS + " phone number: " + event.getPhoneNumber()
								+ " iOS device token: " + event.getIosDeviceToken() + " internalT: "
								+ event.getInternalTransactionId() + " rootDir: " + rootDir + " payload content: "
								+ resultPayload);

				// update notification status
				event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
				event.setMobileAppNotificationStatusMessage(resultPayload);
				event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
				daoUserEvent.createOrUpdate(event, 8);
			} else if (event.getDeviceType().toUpperCase().equals(DeviceType.Android.toString().toUpperCase())) {
				if (event.getAndroidDeviceToken() == null) {
					event = updateAndroidDeviceTokenInEvent(event);
				}
				// push notification
				String googleNotificationSenderAccessKey = daoRealm.findById(event.getRealmId())
						.getGoogleNotificationsAccessKey();
				GoogleNotificationSender gns = new GoogleNotificationSender(googleNotificationSenderAccessKey);
				String strSendStatus = gns.sendMessage(event.getAndroidDeviceToken(), notificationPayload);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " "
								+ Application.REWARD_NOTIFICATION_SUCCESS + " "
								+ " successfully issued reward notification for device type: " + event.getDeviceType()
								+ " status: " + RespStatusEnum.SUCCESS + " phone number: " + event.getPhoneNumber()
								+ " android device token: " + event.getAndroidDeviceToken()
								+ " google notification access key: " + googleNotificationSenderAccessKey
								+ " internalT: " + event.getInternalTransactionId() + " payload content: "
								+ strSendStatus);
				// update notification status
				event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
				event.setMobileAppNotificationStatusMessage(strSendStatus);
				event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
				daoUserEvent.createOrUpdate(event, 8);
			} else if (event.getDeviceType().toUpperCase().equals(DeviceType.Windows.toString().toUpperCase())) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.ERROR,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " "
								+ Application.REWARD_NOTIFICATION_FAILURE + " " + " phone number: "
								+ event.getPhoneNumber() + " error sending notification: notification for device: "
								+ event.getDeviceType() + " not supported" + " internalT: "
								+ event.getInternalTransactionId());
			} else {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.ERROR,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " "
								+ Application.REWARD_NOTIFICATION_FAILURE + " " + " phone number: "
								+ event.getPhoneNumber() + " error sending notification: notification for device: "
								+ event.getDeviceType() + " not supported" + " internalT: "
								+ event.getInternalTransactionId());
			}
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ Application.REWARD_NOTIFICATION_FAILURE + " "
							+ " error sending notification: notification for device: " + event.getDeviceType()
							+ " not supported" + " internalT: " + event.getInternalTransactionId() + " error: "
							+ stackTraceToString(exc));
			// update notification status
			event.setMobileAppNotificationStatus(RespStatusEnum.FAILED.toString());
			event.setMobileAppNotificationStatusMessage(exc.toString());
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 8);
		}
	}

	private UserEventEntity updateIosDeviceTokenInEvent(UserEventEntity event) {

		try {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.OK, Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ " updating event " + "id : " + event.getId() + " with IosDeviceToken");
			int userId = event.getUserId();
			if (userId != 0) {
				AppUserEntity appUser = daoAppUser.findById(userId);
				if (appUser != null) {
					event.setIosDeviceToken(appUser.getiOSDeviceToken());
					event = daoUserEvent.createOrUpdate(event, 0);
					Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
							LogStatus.OK,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
									+ " updated event " + "id : " + event.getId() + " with IosDeviceToken: "
									+ event.getIosDeviceToken());
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " updating event "
							+ "id : " + event.getId() + " with IosDeviceToken failed: "
							+ stackTraceToString(exception));
		}
		return event;

	}

	private UserEventEntity updateAndroidDeviceTokenInEvent(UserEventEntity event) {

		try {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.OK, Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ " updating event " + "id : " + event.getId() + " with AndroidDeviceToken");
			int userId = event.getUserId();
			if (userId != 0) {
				AppUserEntity appUser = daoAppUser.findById(userId);
				if (appUser != null) {
					event.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
					event = daoUserEvent.createOrUpdate(event, 0);
					Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
							LogStatus.OK,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
									+ " updated event " + "id : " + event.getId() + " with AndroidDeviceToken: "
									+ event.getAndroidDeviceToken());
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY + " updating event "
							+ "id : " + event.getId() + " with AndroidDevicetoken failed: "
							+ stackTraceToString(exception));
		}
		return event;
	}

	private String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public void sendWalletTopupNotification(UserEventEntity event, boolean success,
			boolean isEventFromUserThatWasInviting) {

		// issue donkey trigger notification via chatz
		triggerDonkeyNotification(event, success, NotificationType.REWARD_VIA_WALLET_PAYIN.toString(),
				isEventFromUserThatWasInviting);
		// trigger mobile app notification
		try {
			String notificationPayload = "";
			if (event.getRewardTypeName().equals("Trippa-GB")) {
				if (success) {
					notificationPayload = "You have received your Trippa Reward. Your wallet in the Trippa Reward app has been recharged with "
							+ event.getCustomRewardValue() + " points";
				} else {
					notificationPayload = " Your Trippa Reward: " + event.getOfferTitle()
							+ " has not made it to your wallet.  Please contact support@trippareward.com so we can fix this for you.";
				}
			} else if (event.getRewardTypeName().toLowerCase().contains("cinetreats")) {
				if (success) {
					notificationPayload = "You have received your Stubs Reward. Your wallet in the Cinetreats app has been recharged with "
							+ event.getCustomRewardValue() + " stubs";
				} else {
					notificationPayload = " Your Stubs Reward: " + event.getOfferTitle()
							+ " has not made it to your wallet. Please contact support@cinetreats.co.uk so we can fix this for you.";
				}
			} else {

				if (success) {
					notificationPayload = "Your wallet has been recharged with "
							+ CurrencyCodeConverter.getUserFriendlyCurrencyCodeByGeo(event.getRewardIsoCurrencyCode())
							+ " " + event.getRewardValue();
				} else {
					notificationPayload = "Your wallet recharge has failed - please contact support@airrewardz.net for help";
				}
			}

			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.WALLET_PAY_IN + " " + " attempting to send notification for device type: "
							+ event.getDeviceType() + " phone number: " + event.getPhoneNumber() + " rewardTypeName: "
							+ event.getRewardTypeName() + " internalT: " + event.getInternalTransactionId());

			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.WALLET_PAY_IN
							+ " attempting to send notification for event with internalTransactionId: "
							+ event.getInternalTransactionId() + " device type: " + event.getDeviceType()
							+ " iOSDeviceToken: " + event.getIosDeviceToken() + " androidDeviceToken "
							+ event.getAndroidDeviceToken() + " event type: " + event.getUserEventCategory()
							+ " reward type: " + event.getRewardTypeName() + " phone number:" + event.getPhoneNumber()
							+ " user id: " + event.getUserId()

			);

			if (event.getDeviceType() == null) {
				event = fixDeviceTypeInEvent(event);
			}

			if (event.getDeviceType().toUpperCase().equals(DeviceType.iOS.toString().toUpperCase())) {
				// push notification
				String rootDir = new java.io.File(".").getCanonicalPath();
				IOSNotificationSender iOSNotificationSender = new IOSNotificationSender();
				String resultPayload = iOSNotificationSender.pushNotification(rootDir, event.getIosDeviceToken(),
						notificationPayload);

				Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY,
						event.getRealmId(), LogStatus.OK,
						Application.WALLET_PAY_IN + " " + " successfully issued reward notification for device type: "
								+ event.getDeviceType() + " status: " + RespStatusEnum.SUCCESS + " phone number: "
								+ event.getPhoneNumber() + " iOS device token: " + event.getIosDeviceToken()
								+ " internalT: " + event.getInternalTransactionId() + " rootDir: " + rootDir
								+ " payload content: " + resultPayload);

				// update notification status
				event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
				event.setMobileAppNotificationStatusMessage(resultPayload);
				event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
				daoUserEvent.createOrUpdate(event, 8);
			} else if (event.getDeviceType().toUpperCase().equals(DeviceType.Android.toString().toUpperCase())) {
				// push notification
				String googleNotificationSenderAccessKey = daoRealm.findById(event.getRealmId())
						.getGoogleNotificationsAccessKey();
				GoogleNotificationSender gns = new GoogleNotificationSender(googleNotificationSenderAccessKey);
				String strSendStatus = gns.sendMessage(event.getAndroidDeviceToken(), notificationPayload);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.WALLET_PAY_IN + " " + " successfully issued reward notification for device type: "
								+ event.getDeviceType() + " status: " + RespStatusEnum.SUCCESS + " phone number: "
								+ event.getPhoneNumber() + " android device token: " + event.getAndroidDeviceToken()
								+ " google notification access key: " + googleNotificationSenderAccessKey
								+ " internalT: " + event.getInternalTransactionId() + " payload content: "
								+ strSendStatus);
				// update notification status
				event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
				event.setMobileAppNotificationStatusMessage(strSendStatus);
				event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
				daoUserEvent.createOrUpdate(event, 8);
			} else if (event.getDeviceType().toUpperCase().equals(DeviceType.Windows.toString().toUpperCase())) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY,
						event.getRealmId(), LogStatus.ERROR,
						Application.WALLET_PAY_IN + " " + " phone number: " + event.getPhoneNumber()
								+ " error sending notification: notification for device: " + event.getDeviceType()
								+ " not supported" + " internalT: " + event.getInternalTransactionId());
			} else {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY,
						event.getRealmId(), LogStatus.ERROR,
						Application.WALLET_PAY_IN + " " + " phone number: " + event.getPhoneNumber()
								+ " error sending notification: notification for device: " + event.getDeviceType()
								+ " not supported" + " internalT: " + event.getInternalTransactionId());
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.WALLET_PAY_IN + " " + " error sending notification: notification for device: "
							+ event.getInternalTransactionId() + " device type: " + event.getDeviceType()
							+ " iOSDeviceToken: " + event.getIosDeviceToken() + " androidDeviceToken "
							+ event.getAndroidDeviceToken() + " event type: " + event.getUserEventCategory()
							+ " reward type: " + event.getRewardTypeName() + " phone number:" + event.getPhoneNumber()
							+ " user id: " + event.getUserId() + " error: " + exc.getStackTrace());

			// update notification status
			event.setMobileAppNotificationStatus(RespStatusEnum.FAILED.toString());
			event.setMobileAppNotificationStatusMessage(exc.toString());
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 8);
		}

	}

	private UserEventEntity fixDeviceTypeInEvent(UserEventEntity event) {
		try {

			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, event.getRealmId(),
					LogStatus.OK, Application.WALLET_PAY_IN + " fixing device type in event with id" + event.getId()
							+ " and internalTransactionId: " + event.getInternalTransactionId());

			AppUserEntity appUser = daoAppUser.findById(event.getUserId());
			if (appUser.getiOSDeviceToken() != null) {
				event.setDeviceType(DeviceType.iOS.toString());
				event.setIosDeviceToken(appUser.getiOSDeviceToken());
			}

			if (appUser.getAndroidDeviceToken() != null) {
				event.setDeviceType(DeviceType.Android.toString());
				event.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
			}

		} catch (Exception exc) {
			Application.getElasticSearchLogger()
					.indexLog(Application.WALLET_TRANSACTION_ACTIVITY, event
							.getRealmId(), LogStatus.OK,
					Application.WALLET_PAY_IN + " exception occured during fixing device type: " + exc.getMessage());
		}
		return event;
	}

	public void sendRewardNotificationSMS(UserEventEntity event, RealmEntity realm, String smsMessageContent) {
		try {
			smsMessageContent = URLEncoder.encode(smsMessageContent, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.toString());
		}
		// setup clickatell url to call
		String urlToCall = "http://api.clickatell.com/http/sendmsg?user=Bluepodmedia&"
				+ "password=VIgLTKVHCZXdAN&api_id=3538043&to=" + event.getPhoneNumber() + "&text=" + smsMessageContent;

		// execute call
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		String reqResponse = "";
		int responseCode = -1;

		try {
			URL url = new URL(urlToCall);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setConnectTimeout(realm.getConnectionTimeout() * 1000);
			urlConnection.setReadTimeout(realm.getReadTimeout() * 1000);
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				reqResponse = inputLine;
			}
			responseCode = urlConnection.getResponseCode();
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_NOTIFICATION_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ " internalT: " + event.getInternalTransactionId() + " rewardType: "
							+ event.getRewardTypeName() + " error crediting user: " + exc.toString() + " status: "
							+ RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: " + exc.toString());
			daoUserEvent.createOrUpdate(event, 7);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception exc) {
					logger.severe(exc.toString());
				}
			}
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		logger.info("SMS gateway response code: " + responseCode + " Response content: " + reqResponse
				+ " phone number: " + event.getPhoneNumber());
		String responseString = reqResponse;
		String statusMessage = responseString;
		String status = "";
		// TODO if we get error - notify AR about problem with reward ask
		// Rodgers if we only need 200 response to know that request was
		// successful)
		if (responseCode == 200) {
			status = "SUCCESS";
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_NOTIFICATION_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ " reward notification successfully issued for internalT: "
							+ event.getInternalTransactionId() + " reward value: " + event.getRewardValue()
							+ " offerPayout: " + event.getOfferPayout() + " offer payout currency: "
							+ event.getOfferPayoutIsoCurrencyCode() + " rewardType: " + event.getRewardTypeName()
							+ " rewardUrl: " + urlToCall + " status: " + RespStatusEnum.SUCCESS + " code: "
							+ RespCodesEnum.OK_NO_CONTENT);
			// update event
			event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
			event.setMobileAppNotificationStatusMessage(responseString);
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 8);
		} else {
			status = "FAILED";
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.REWARD_NOTIFICATION_ACTIVITY + " " + Application.REWARD_NOTIFICATION_ACTIVITY
							+ " error during reward notification: unknown response code: " + responseCode
							+ " for event: " + event.getUserId() + " rewardType: " + event.getRewardTypeName()
							+ " phone: " + event.getPhoneNumber() + " internalT: " + event.getInternalTransactionId()
							+ " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);
			// update event
			event.setMobileAppNotificationStatus(RespStatusEnum.FAILED.toString());
			event.setMobileAppNotificationStatusMessage(responseString);
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 4);
		}
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

	// TODO Jakub to finish
	public void triggerDonkeyNotification(UserEventEntity event, boolean success, String notificationTypeStr,
			boolean isEventFromUserThatWasInviting) {

		logger.info("Notification manager");
		gamificationManager.countEvent(event);

		logger.info("triggering donkey notification with values: " + event + " success: " + success
				+ " notificationTypeStr: " + notificationTypeStr + " isEventFromuserThatWasInviting: "
				+ isEventFromUserThatWasInviting);

		try {
			String rewardzKey = "lOYyOYjTwK234J7w0nIkiGQyuuspPg95eHpvsx6+GJbfcMbMTSII3AtCzkSUnvkl9++lFv+CLgJ2dodMSsuQ";
			String goaheadKey = "4f603F1fxZOdM+HiYOr0lgcJWgT5KJI4ctdRjuA5L3zY4cY9yjxeye58Sd0DocTfhUfUtGF21KqanWlKJJNDw";
			String cinemaKey = "sUeUt4uFhR156ziOXZe0F8HwY80ZoNQsNR7fFVFqfbtcrklL3NJrpmppqG1g3H4KHCynzhdiNqUs8Ikf6KkBIA";

			DonkyManager donkyManager;

			if (event.getApplicationName() != null && event.getApplicationName().toLowerCase().contains("goahead")) {
				donkyManager = new DonkyManager(goaheadKey);

			} else if (event.getApplicationName().toLowerCase().contains("cine")) {
				donkyManager = new DonkyManager(cinemaKey);

			} else {
				donkyManager = new DonkyManager(rewardzKey);

			}
			String notificationMessage = "";
			String notificationTitle = "";

			if (notificationTypeStr.toString().toLowerCase()
					.equals(NotificationType.REWARD_INSTANT.toString().toLowerCase())) {

				notificationMessage = notificationMessageDictionary.getRewardNotificationMessage(event, success,
						notificationTypeStr, isEventFromUserThatWasInviting);
				notificationTitle = "Instant reward";

			} else if (notificationTypeStr.toString().toLowerCase()
					.equals(NotificationType.REWARD_VIA_REFERRAL_REGISTRATION.toString().toLowerCase())) {

				notificationMessage = notificationMessageDictionary.getRewardNotificationMessage(event, success,
						notificationTypeStr, isEventFromUserThatWasInviting);
				notificationTitle = "Referral reward";

			} else if (notificationTypeStr.toString().toLowerCase()
					.equals(NotificationType.REWARD_VIA_WALLET_PAYIN.toString().toLowerCase())) {
				notificationMessage = notificationMessageDictionary.getRewardNotificationMessage(event, success,
						notificationTypeStr, isEventFromUserThatWasInviting);
				notificationTitle = "Wallet pay in";
			} else if (notificationTypeStr.toString().toLowerCase()
					.equals(NotificationType.WALLET_PAYOUT.toString().toLowerCase())) {

				notificationMessage = notificationMessageDictionary.getRewardNotificationMessage(event, success,
						notificationTypeStr, isEventFromUserThatWasInviting);
				notificationTitle = "Wallet payout";
			} else if (notificationTypeStr.toString().toLowerCase()
					.equals(NotificationType.SNAPDEAL_EVENT_CHANGE.toString().toLowerCase())) {
				notificationMessage = notificationMessageDictionary.getRewardNotificationMessage(event, success,
						notificationTypeStr, isEventFromUserThatWasInviting);
				notificationTitle = "Hot offer status change";
			}

			// generate donky trigger es log
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_NOTIFICATION_ACTIVITY + " "
							+ Application.REWARD_NOTIFICATION_ACTIVITY_DONKEY_TRIGGER + " userId: " + event.getUserId()
							+ " rewardType: " + event.getRewardTypeName() + " phone: " + event.getPhoneNumber()
							+ " internalT: " + event.getInternalTransactionId() + " notification title: "
							+ notificationTitle + " notification message: " + notificationMessage);

			donkyManager.sendRichMessage(notificationMessage, notificationTitle, event.getPhoneNumber());
		} catch (Exception exc) {

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.REWARD_NOTIFICATION_ACTIVITY + " "
							+ Application.REWARD_NOTIFICATION_ACTIVITY_DONKEY_TRIGGER + " "
							+ Application.REWARD_NOTIFICATION_ACTIVITY + " " + " userId: " + event.getUserId()
							+ " rewardType: " + event.getRewardTypeName() + " phone: " + event.getPhoneNumber()
							+ " internalT: " + event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED
							+ " error: " + exc.toString());
			logger.severe("Exception for internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
			logger.severe(exc.toString());
			exc.printStackTrace();
		}
	}

	public void sendNoActivatedAccountNotification(InvitationEntity entity) {
		try {
			String title = "Warning";
			DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
			String emailInvited = entity.getEmailInvited();
			String emailInviting = entity.getEmailInviting();
			AppUserEntity appUserInvited = daoAppUser.findByEmail(emailInvited);
			AppUserEntity appUserInviting = daoAppUser.findByEmail(emailInviting);
			// we know that appUserInviting will be populated, but lets find now
			// invited user

			if (appUserInvited == null) {
				logger.info("User didnt found by email...");
				appUserInvited = daoAppUser.findByReferralCode(entity.getCode());
				if (appUserInvited == null) {
					logger.info("User didnt found by referral code in 'code' field.");
					appUserInvited = daoAppUser.findByReferralCode(entity.getInvitingFBInviteCode());
					if (appUserInvited == null) {
						logger.info("Cant find user for referral code.");
					}
				}

			}

			RealmEntity realm = null;
			if (appUserInvited != null) {
				realm = daoRealm.findById(appUserInvited.getRealmId());
			}
			if (realm == null && appUserInviting != null) {
				realm = daoRealm.findById(appUserInviting.getRealmId());
			}
			String esLog = "";
			if (realm != null) {
				String messageToInviting = realm.getMessageReferralRewardWithoutAccountActivatedToInviting();
				if (appUserInvited != null)
					messageToInviting += " Invited person phone number: " + appUserInvited.getPhoneNumber() + ".";

				String messageToInvited = realm.getMessageReferralRewardWithoutAccountActivatedToInvited();

				if (appUserInviting != null) {
					if (messageToInviting != null) {
						logger.info("Sending message to inviting user...");
						esLog += "Message to inviting user(id: " + appUserInviting.getId() + ") sent.";
						donkyManager.sendRichMessage(messageToInviting, title, appUserInviting.getPhoneNumber());
					} else {
						logger.info("Cant send message to inviting user. null message.");
						esLog += "Cant send message to inviting user. null message.";
					}

				} else {
					logger.info("Cant send message to inviting user. Null.");
					esLog += "Cant send message to inviting user. null object.";
				}

				if (appUserInvited != null) {
					if (messageToInvited != null) {
						logger.info("Sending message to invited user..");
						esLog += "Message to invited user(" + appUserInvited.getId() + ") sent.";
						donkyManager.sendRichMessage(messageToInvited, title, appUserInvited.getPhoneNumber());
					} else {
						logger.info("Cant send message to invited user. null message.");
						esLog += "Cant send message to invited user. null message.";
					}

				} else {
					logger.info("Cant send message to invited user. Null.");
					esLog += "Cant sned message to invited user. Null";
				}

			} else {
				esLog += " Realm empty. Cant get message.";
			}
			Application.getElasticSearchLogger().indexLog(Application.ACCOUNT_NOT_ACTIVATED_ACTIVITY, -1, LogStatus.OK,
					"" + esLog);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.ACCOUNT_NOT_ACTIVATED_ACTIVITY, -1, LogStatus.OK,
					"" + exc.getStackTrace().toString());

		}
	}

	public void sendReferralAbuseNotification(InvitationEntity entity) {
		try {
			logger.info("Sending notification to persons from invitation id: " + entity.getId());
			String title = "Warning";
			DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
			String emailInvited = entity.getEmailInvited();
			String emailInviting = entity.getEmailInviting();
			AppUserEntity appUserInvited = daoAppUser.findByEmail(emailInvited);
			AppUserEntity appUserInviting = daoAppUser.findByEmail(emailInviting);
			// we know that appUserInviting will be populated, but lets find now
			// invited user

			if (appUserInvited == null) {
				logger.info("User didnt found by email...");
				appUserInvited = daoAppUser.findByReferralCode(entity.getCode());
				if (appUserInvited == null) {
					logger.info("User didnt found by referral code in 'code' field.");
					appUserInvited = daoAppUser.findByReferralCode(entity.getInvitingFBInviteCode());
					if (appUserInvited == null) {
						logger.info("Cant find user for referral code.");
					}
				}

			}

			String esLog = "";
			String message = "";
			RealmEntity realm = null;
			if (appUserInviting != null) {
				realm = daoRealm.findById(appUserInviting.getRealmId());
			}

			if (realm != null) {
				message = realm.getMessageReferralAbuseDetectedToInviting();
				if (appUserInviting != null) {
					if (message != null) {
						logger.info("Sending message to inviting user...");
						esLog += "Message to inviting user(id: " + appUserInviting.getId() + ") sent.";
						donkyManager.sendRichMessage(message, title, appUserInviting.getPhoneNumber());
					} else {
						logger.info("Cant send message to inviting user. Null message");
						esLog += "Cant send message to inviting user. null message.";
					}
				} else {
					logger.info("Cant send message to inviting user. Null.");
					esLog += "Cant send message to inviting user. null object.";
				}
				if (appUserInvited != null) {
					if (message != null) {
						logger.info("Sending message to invited user..");
						esLog += "Message to invited user(" + appUserInvited.getId() + ") sent.";
						donkyManager.sendRichMessage(message, title, appUserInvited.getPhoneNumber());
					} else {
						logger.info("Cant send message to invited user. Null message");
						esLog += "Cant send message to invited user. null message.";
					}

				} else {
					logger.info("Cant send message to invited user. Null.");
					esLog += "Cant sned message to invited user. Null";
				}
			} else {
				esLog += "Realm is null.";
			}
			Application.getElasticSearchLogger().indexLog(Application.ABUSE_NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					"Messages status:" + esLog);

		} catch (Exception exc) {
			logger.info(exc.getStackTrace().toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.ABUSE_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					exc.getStackTrace().toString());

		}

	}

	public void sendExceededLimitNotification(InvitationEntity entity) {
		try {
			logger.info("Sending notification to persons from invitation id: " + entity.getId());
			String title = "Warning";
			DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
			String emailInviting = entity.getEmailInviting();
			AppUserEntity appUserInviting = daoAppUser.findByEmail(emailInviting);
			// we know that appUserInviting will be populated, but lets find now
			// invited user

			String esLog = "";
			String message = "";
			RealmEntity realm = null;
			if (appUserInviting != null) {
				realm = daoRealm.findById(appUserInviting.getRealmId());
			}

			if (realm != null) {
				message = realm.getMessageReferralExceededLimitToInviting();
				if (appUserInviting != null) {
					if (message != null) {
						logger.info("Sending exceeded limit message to inviting user... " + message);
						esLog += "Exceeded limit Message to inviting user(id: " + appUserInviting.getId() + ") sent: "
								+ message;
						donkyManager.sendRichMessage(message, title, appUserInviting.getPhoneNumber());
					} else {
						logger.info("Cant send exceeded limit message to inviting user. Null message");
						esLog += "Cant send exceeded limit message to inviting user. null message.";
					}
				} else {
					logger.info("Cant send exceeded limit message to inviting user. Null.");
					esLog += "Cant send exceeded limit message to inviting user. null object.";
				}

			} else {
				esLog += "Realm is null.";
			}
			Application.getElasticSearchLogger().indexLog(Application.ABUSE_NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					"Messages status:" + esLog);

		} catch (Exception exc) {
			logger.info(exc.getStackTrace().toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.ABUSE_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					exc.getStackTrace().toString());

		}

	}

	public static String formatDouble(double d) {
		if (d == (long) d)
			return String.format("%d", (long) d);
		else
			return String.format("%s", d);
	}

	public void sendSpinnerRewardNotification(final UserEventEntity entity) {
		try {
			logger.info("Sending spinner reward notification to person from event id: " + entity.getId());
			String title = "Information";
			DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
			String esLog = "";

			String message = spinnerManager.getNotificationMessageForSpinnerRewardWithId(entity.getOfferSourceId())
					.replaceAll("VALUE", formatDouble(entity.getRewardValue()));
			donkyManager.sendRichMessage(message, title, entity.getPhoneNumber());
			logger.info("Sent spinner reward manager message:" + message + " to user:" + entity.getPhoneNumber());
			esLog += "Sent spinner reward manager message:" + message + " to user:" + entity.getPhoneNumber();

			entity.setMobileAppNotificationStatusMessage(message);
			entity.setMobileAppNotificationDate(new Timestamp(new Date().getTime()));
			entity.setMobileAppNotificationStatus("SUCCESS");
			entity.setRewardRequestDate(new Timestamp(new Date().getTime()));
			entity.setRewardRequestStatus("SUCCESS");
			entity.setRewardRequestStatusMessage("OK");
			entity.setRewardResponseStatus("SUCCESS");
			entity.setRewardResponseStatusMessage("OK");
			daoUserEvent.createOrUpdate(entity, 0);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					"Messages status:" + esLog);

		} catch (Exception exc) {
			logger.info(exc.getStackTrace().toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					exc.getStackTrace().toString());

		}

	}

	public void sendSpinnerDailyBonusNotification(AppUserEntity appUser) {
		try {
			logger.info("Sending spinner daily notification to person with id:" + appUser.getId());
			String title = "Information";
			DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
			String esLog = "";
			String message = spinnerManager.getNotificationMessageForDailyBonus(appUser);
			donkyManager.sendRichMessage(message, title, appUser.getPhoneNumber());
			logger.info("Sent spinner daily bonus notification: " + message + " for user: " + appUser.getId());
			esLog += "Sent spinner daily bonus notification: " + message + " for user: " + appUser.getId();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					"Messages status:" + esLog);
		} catch (Exception exc) {
			logger.info(exc.getStackTrace().toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					exc.getStackTrace().toString());

		}
	}

	public void sendVouchers(AppUserEntity appUser, List<String> voucherCodesList) {
		try {
			if (appUser != null && voucherCodesList != null) {
				logger.info("Sending vouchers for user: " + appUser.getId());
				String title = "Voucher";
				String esLog = "";
				DonkyManager donkyManager = getDonkyManager("AIR_REWARDZ");
				for (String voucherCode : voucherCodesList) {

					String message = "Congratulations! You have been rewarded! Please use the following recharge voucher pin: <b>"
							+ voucherCode
							+ "</b> in order to top-up, using your network provider's usual channels (SMS, USSD etc).";
					esLog += " Sending voucher code: " + voucherCode + " to :" + appUser.getId();
					donkyManager.sendRichMessage(message, title, appUser.getPhoneNumber());
				}
				Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1,
						LogStatus.OK, "Send vouchers log: " + esLog);
			}
		} catch (Exception exc) {
			logger.info(exc.getStackTrace().toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					exc.getStackTrace().toString());

		}

	}

	private DonkyManager getDonkyManager(String type) {
		logger.info("Getting donky manager with type: " + type);
		DonkyManager resultDonkyManager = null;
		if (type.equals("AIR_REWARDZ")) {
			resultDonkyManager = createDonkyManager("AIR_REWARDZ", this.AIR_REWARDZ_DONKY_API_KEY);
		}
		if (type.equals("GO_AHEAD")) {
			resultDonkyManager = createDonkyManager("GO_AHEAD", this.GO_AHEAD_DONKY_API_KEY);
		}
		if (type.equals("CINETREATS")) {
			resultDonkyManager = createDonkyManager("CINETREATS", this.CINETREATS_DONKY_API_KEY);
		}

		logger.info("Returning donky manager with type: " + type);
		return resultDonkyManager;
	}

	private DonkyManager createDonkyManager(String type, String key) {
		logger.info("Creating donky manager with type:" + type + " and key:" + key);
		DonkyManager donkyManager = new DonkyManager(key);
		return donkyManager;
	}

}
