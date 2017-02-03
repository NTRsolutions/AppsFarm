package is.ejb.bl.reward;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationNameEnum;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.EventQueueStatus;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.business.WalletTransactionStatus;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.eventQueue.EventQueueManager;
import is.ejb.bl.firebase.FirebaseResponse;
import is.ejb.bl.friends.UserFriendManager;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.NotificationMessageDictionary;
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
import is.ejb.bl.wallet.WalletManager;
import is.ejb.bl.wallet.WalletTransactionType;
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
import is.ejb.dl.entities.EventQueueEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.UserFriendEntity;
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
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.bcel.generic.ISUB;
import org.zendesk.client.v2.model.Ticket;

@Stateless
public class RewardManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private WalletManager walletManager;

	public String issueReward(RealmEntity realm, UserEventEntity event, InvitationEntity invitation,
			boolean isEventFromUserThatWasInviting) {

		try {
			logger.info("Request reward for : " + event + " realm: " + realm);
			requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return getSuccessfullResponse(event);

	}

	private String getSuccessfullResponse(UserEventEntity event) {
		return "{\"response\":\" status: " + RespStatusEnum.SUCCESS + " " + event.getTransactionId() + ":OK" + " code: "
				+ RespCodesEnum.OK_NO_CONTENT + "\"}";
	}

	private void requestWalletTopup(RealmEntity realm, UserEventEntity event, boolean isEventFromUserThatWasInviting) {
		AppUserEntity appUser = null;
		try {
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, realm.getId(),
					LogStatus.OK,
					Application.WALLET_PAY_IN + " " + " performing wallet topup for user: " + event.getEmail() + " "
							+ event.getPhoneNumberExt() + " " + event.getPhoneNumber() + " application name: "
							+ event.getApplicationName() + " rewardTypeName: " + event.getRewardTypeName()
							+ " reward name: " + event.getRewardName() + " internalT: "
							+ event.getInternalTransactionId());

			appUser = daoAppUser.findById(event.getUserId());
			String offerTitle = "Wallet payin";
			if (event.getOfferTitle() != null){
				offerTitle = event.getOfferTitle();
			}
			walletManager.createWalletAction(appUser, WalletTransactionType.ADDITION, event.getCustomRewardValue(),
					offerTitle);
			sendNotification(appUser, event, NotificationMessageDictionary.REWARD_MESSAGE);
			
			
			logger.info("Reward completed to wallet");
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardResponseStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardResponseStatusMessage("OK");

			daoUserEvent.createOrUpdate(event, 2);
			logger.info("User event updated");
			// create wallet payin es log
			Application.getElasticSearchLogger().indexWalletTransaction(realm.getId(), event.getPhoneNumber(), "",
					event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
					event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
					event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(),
					"", UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
					UserEventCategory.WALLET_PAY_IN.toString(), "", "", event.getIpAddress(), event.getCountryCode(),
					event.isInstant(), event.getApplicationName(), event.isTestMode(), event.getUserId());

			

		} catch (Exception exc) {
			exc.printStackTrace();
			sendNotification(appUser, event, NotificationMessageDictionary.REWARD_ERROR_MESSAGE);
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, realm.getId(),
					LogStatus.ERROR,
					Application.WALLET_PAY_IN + " " + Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " internalT: "
							+ event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + Arrays.toString(exc.getStackTrace()));
		}
	}

	private void sendNotification(AppUserEntity appUser, UserEventEntity event, String message) {
		logger.info("Sending notification: " + message + " event: " + event + " appUser: " + appUser);
		message = message.replaceAll("\\{reward\\}", event.getCustomRewardValue() + " points ");
		message = message.replaceAll("\\{offer\\}", event.getOfferTitle());
		FirebaseResponse response = notificationManager.sendNotification(appUser, message);
		event.setMobileAppNotificationDate(new Timestamp(new Date().getTime()));
		event.setMobileAppNotificationStatus(response.getCode()+ "");
		event.setMobileAppNotificationStatusMessage(response.getContent());
	}
	
	
	

	/**
	 * update conversion history for this specific user and offer entry for this
	 * specific offer was already created during click event just need to update
	 * it with conversion date
	 */
	public void updateUserConversionHistory(UserEventEntity event) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.CONVERSION_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE + " "
							+ Application.DOWNLOAD_HISTORY_CONVERSION_UPDATE + " "
							+ " updating conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId());
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
			if (conversionHistory == null) {
				throw new Exception("Unable to identify conversion history for this user!");
			}

			// only update conversionTime entry that matches specific offer id
			// for which this conversion was identified
			ConversionHistoryEntry conversionHistoryEntryToUpdate = daoConversionHistory
					.getConversionEntryToUpdate(event, conversionHistory);
			if (conversionHistoryEntryToUpdate == null) {
				throw new Exception("Unable to idetnify conversion entry for offer with id: " + event.getOfferId()
						+ " internalT: " + event.getInternalTransactionId());
			}

			// update entity
			conversionHistoryEntryToUpdate.setConversionTimestamp(event.getConversionDate());
			conversionHistoryEntryToUpdate.setRewardTimestamp(event.getRewardDate());
			conversionHistoryEntryToUpdate.setRewardStatusMessage(event.getRewardResponseStatusMessage());
			conversionHistoryEntryToUpdate.setRewardStatus(event.getRewardRequestStatus());
			conversionHistoryEntryToUpdate.setApproved(event.isApproved());
			conversionHistory.setGenerationTime(new Timestamp(System.currentTimeMillis()));
			// persist in db (dao takes care of json serialisation)
			daoConversionHistory.createOrUpdate(conversionHistory);
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.CONVERSION_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE
							+ " error adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
		}
	}

}
