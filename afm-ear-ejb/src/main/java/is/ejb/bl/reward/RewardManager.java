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
import is.ejb.bl.friends.UserFriendManager;
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
import is.ejb.dl.dao.DAOCloudtraxConfiguration;
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
import is.ejb.dl.entities.CloudtraxConfigurationEntity;
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
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private DAOCloudtraxConfiguration daoCloudtraxConfiguration;

	@Inject
	private DAORadiusConfiguration daoRadiusConfiguration;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private DAOWalletTransaction daoWalletTransaction;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAOWalletPayoutCarrier daoWalletPayoutCarrier;

	@Inject
	private ZendeskManager zendeskManager;

	@Inject
	private MailManager mailManager;

	@Inject
	private TestManager testManager;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private ReferralManager referralManager;

	@Inject
	private DAOSpinnerData daoSpinnerData;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAOEventQueueEntity daoEventQueue;

	@Inject
	private EventQueueManager eventQueueManager;

	@Inject
	private UserFriendManager userFriendManager;

	

	public void createUserConversionHistory(UserEventEntity event) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.CLICK_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE + " "
							+ " adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId());

			// logger.info("CONVERSION HISTORY CREATE!");
			// logger.info(event.toString());
			// logger.info("CONVERSION HISTORY CREATE!");
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
			if (conversionHistory == null) {
				conversionHistory = new ConversionHistoryEntity();
			}

			conversionHistory.setUserId(event.getUserId());
			conversionHistory.setRealmId(event.getRealmId());
			conversionHistory.setGenerationTime(new Timestamp(System.currentTimeMillis()));

			// create new entry for this conversion
			ConversionHistoryEntry newConversionHistoryEntry = new ConversionHistoryEntry();
			newConversionHistoryEntry.setAdProviderCodeName(event.getAdProviderCodeName());
			newConversionHistoryEntry.setApproved(false);
			newConversionHistoryEntry.setClickTimestamp(event.getClickDate());
			newConversionHistoryEntry.setInternalTransactionId(event.getInternalTransactionId());
			newConversionHistoryEntry.setOfferId(event.getOfferId());
			newConversionHistoryEntry.setOfferTitle(event.getOfferTitle());
			newConversionHistoryEntry.setRewardCurrency(event.getRewardIsoCurrencyCode());
			newConversionHistoryEntry.setRewardTypeName(event.getRewardTypeName());

			if ((event.getRewardTypeName().toLowerCase().contains("goahead")
					|| event.getRewardTypeName().toLowerCase().contains("cine"))
					&& !event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				newConversionHistoryEntry.setRewardValue(event.getCustomRewardValue());
				newConversionHistoryEntry.setRewardCurrency(event.getCustomRewardCurrencyCode());
			} else {
				newConversionHistoryEntry.setRewardValue(event.getRewardValue());
			}
			newConversionHistoryEntry.setSourceOfferId(event.getOfferSourceId());
			newConversionHistoryEntry.setUserEventCategory(UserEventCategory.INSTALL.toString());
			// add to the existing conversion history list of this user
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
			conversionHistoryHolder.getListConversionHistoryEntries().add(0, newConversionHistoryEntry);
			// persist in db (dao takes care of json serialisation)
			daoConversionHistory.createOrUpdate(conversionHistory);

			// logger.info(conversionHistory.toString());

		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.CLICK_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE + " "
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE
							+ " error adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
		}
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

	public String issueReward(RealmEntity realm, UserEventEntity event, InvitationEntity invitation,
			boolean isEventFromUserThatWasInviting) {
		
		try{
			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		return null;
		
		
		// System.out.println(invitation);
		/*System.out.println(event);

		if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_SOUTH_AFRICA.toString())) {
			// monitor if referral reward should be assigned as a result of
			// reward (conversion) that is either instant or wallet recharge
			if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				validateReferralReward(realm, event);
			}

			// handle reward appropriately based on its category
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				// request reward from mode
				boolean isPayoutRequestValid = isWalletPayoutRequestValid(realm, event, isEventFromUserThatWasInviting);
				boolean isWalletPayoutSuccessful = requestWalletPayout(realm, event, isEventFromUserThatWasInviting);

				if (isPayoutRequestValid && isWalletPayoutSuccessful) {
					requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
				}
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}
		} else if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_INDIA.toString())) {
			// monitor if referral reward should be assigned as a result of
			// reward (conversion) that is either instant or wallet recharge
			if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				validateReferralReward(realm, event);
			}

			// handle reward appropriately based on its category
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				// request reward from mode
				boolean isPayoutRequestValid = isWalletPayoutRequestValid(realm, event, isEventFromUserThatWasInviting);
				boolean isWalletPayoutSuccessful = requestWalletPayout(realm, event, isEventFromUserThatWasInviting);

				if (isPayoutRequestValid && isWalletPayoutSuccessful) {
					requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
				}
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}

		} else if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_TEST.toString())) {
			// monitor if referral reward should be assigned as a result of
			// reward (conversion) that is either instant or wallet recharge
			if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				validateReferralReward(realm, event);
			}

			// handle reward appropriately based on its category
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				// request reward from mode
				boolean isPayoutRequestValid = isWalletPayoutRequestValid(realm, event, isEventFromUserThatWasInviting);
				boolean isWalletPayoutSuccessful = requestWalletPayout(realm, event, isEventFromUserThatWasInviting);

				if (isPayoutRequestValid && isWalletPayoutSuccessful) {
					requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
				}
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}
		} else if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_KENYA.toString())) {
			// monitor if referral reward should be assigned as a result of
			// reward (conversion) that is either instant or wallet recharge
			if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				validateReferralReward(realm, event);
			}

			// handle reward appropriately based on its category
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				// request reward from mode
				boolean isPayoutRequestValid = isWalletPayoutRequestValid(realm, event, isEventFromUserThatWasInviting);
				boolean isWalletPayoutSuccessful = requestWalletPayout(realm, event, isEventFromUserThatWasInviting);

				if (isPayoutRequestValid && isWalletPayoutSuccessful) {
					requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
				}
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}
		} else if (event.getRewardTypeName().toUpperCase()
				.equals(Application.REWARD_PROVIDER_GO_AHEAD_BRIGHTON_HOVE_UK.toString().toUpperCase())) {
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) { // instant

				// TODO do nothing as we don't support instant install activity
				// for GoAhead - all goes via wallet
			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) { // wallet
																									// topup
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) { // wallet
																														// topup
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) { // wallet
				// payout for user selected reward
				requestWalletPayout(realm, event, isEventFromUserThatWasInviting);
				// request reward from mode
				requestRewardGoAhead(event, invitation, isEventFromUserThatWasInviting);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.QUIDCO.toString().toUpperCase())) {
				requestRewardQuidco(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}

		} else if (event.getRewardTypeName().toUpperCase()
				.equals(Application.REWARD_PROVIDER_CINETREATS.toString().toUpperCase())
				|| event.getRewardTypeName().toUpperCase()
						.equals(Application.REWARD_PROVIDER_CINETREATS_AU.toString().toUpperCase())) {
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {

			} else if (!event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			} else if (event.isInstant()
					&& event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) { // wallet
				// payout for user selected reward
				requestWalletPayout(realm, event, isEventFromUserThatWasInviting);
				// request reward from mode
				requestRewardGoAhead(event, invitation, isEventFromUserThatWasInviting);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SPINNER.toString().toUpperCase())) {
				requestRewardSpinner(realm, event);
			} else if (event.getUserEventCategory().toUpperCase()
					.equals(UserEventCategory.SNAPDEAL.toString().toUpperCase())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}
		} else if (event.getRewardTypeName().toUpperCase().equals(UserEventCategory.INVITE.toString().toUpperCase())) {
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
				// request reward from mode
				requestRewardMode(event, invitation, isEventFromUserThatWasInviting);
			} else if (!event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting); // WALLET_PAY_IN
			}

		} else if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AFA.toString())) {
			if (event.isInstant()) { // instant reward request
				requestRewardAfa(event);
				// requestRewardRadius(event);
			} else { // wallet topup
				requestWalletTopup(realm, event, isEventFromUserThatWasInviting);
			}
		} else {
			// if no matching reward provider identifed - trigger error
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_NO_REWARD_TYPE_NAME_DEFINED);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage(RespCodesEnum.ERROR_NO_REWARD_TYPE_NAME_DEFINED.toString());
			daoUserEvent.createOrUpdate(event, 3);
		}

		// return fake status to the calling service (this is only to send
		// confirmation back to offer provider that AB successfully processed
		// their request
		return "{\"response\":\" status: " + RespStatusEnum.SUCCESS + " " + event.getTransactionId() + ":OK" + " code: "
				+ RespCodesEnum.OK_NO_CONTENT + "\"}";*/
	}

	private void requestRewardQuidco(RealmEntity realm, UserEventEntity event) {
		logger.info("***********************");
		logger.info("Requesting reward for quidco event: " + event);
		requestWalletTopup(realm, event, false);
	}

	private void requestRewardSpinner(RealmEntity realm, UserEventEntity event) {
		try {
			logger.info("Request spinner reward for event:" + event);
			if (event.getOfferId().equals(SpinnerRewardType.MONEY.toString())) {
				logger.info("Requesting spinner reward - wallet topup");
				requestWalletTopup(realm, event, false);
			} else if (event.getOfferId().equals(SpinnerRewardType.SPIN_AGAIN.toString())) {
				logger.info("Requesting spinner reward - additional spin");
				rewardUserWithSpin(event);
			} else if (event.getOfferId().equals(SpinnerRewardType.FAIL.toString())) {
				logger.info("Spinner reward is fail - send notification to user.");
				notificationManager.sendSpinnerRewardNotification(event);
			} else if (event.getOfferId().equals(SpinnerRewardType.UNLOCK_OFFERS.toString())) {
				logger.info("Requesting spinner reward - unlock offers");
				// do nothing atm
			} else if (event.getOfferId().equals(SpinnerRewardType.UNLOCK_VIDEOS.toString())) {
				logger.info("Requesting spinner reward - unlock videos");
				rewardUserWithUnlockVideo(event);
			} else if (event.getOfferId().equals(SpinnerRewardType.SUPRISE.toString())) {
				// do nothing atm
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	private void rewardUserWithUnlockVideo(UserEventEntity event) {
		try {
			if (event != null) {
				logger.info("Giving user:" + event.getUserId() + " unlock video.");
				RewardTypeEntity rewardType = daoRewardType.findByName(event.getRewardTypeName());
				int installCounter = rewardType.getInstallCounterVG();
				AppUserEntity appUser = daoAppUser.findById(event.getUserId());
				appUser.setInstallConversionCounterVG(installCounter);
				appUser.setVideoConversionCounterVG(0);

				notificationManager.sendSpinnerRewardNotification(event);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	private void rewardUserWithSpin(UserEventEntity event) {
		try {
			if (event != null) {
				logger.info("Giving user:" + event.getUserId() + " additional spin.");
				Application.getElasticSearchLogger()
						.indexLog(Application.REWARD_ACTIVITY, event
								.getRealmId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + " Giving user:" + event.getUserId() + " additional spin.");
				SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(event.getUserId());
				if (spinnerData != null) {
					logger.info(
							"Adding spin:" + (int) event.getRewardValue() + "before:" + spinnerData.getAvailableUses());
					spinnerData.setAvailableUses(spinnerData.getAvailableUses() + (int) event.getRewardValue());
					logger.info("After:" + spinnerData.getAvailableUses());
					daoSpinnerData.createOrUpdate(spinnerData);
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
							LogStatus.OK, Application.REWARD_ACTIVITY + "User " + event.getUserId()
									+ " has been rewarded with " + event.getRewardValue() + "additional uses");
					logger.info("User " + event.getUserId() + " has been rewarded with " + event.getRewardValue()
							+ "additional uses");

					notificationManager.sendSpinnerRewardNotification(event);

				} else {
					logger.info("User " + event.getUserId() + " has empty spinner data row");
				}

			}
		} catch (Exception exc) {
			logger.info(Arrays.toString(exc.getStackTrace()));
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR, Application.REWARD_ACTIVITY + Arrays.toString(exc.getStackTrace()));
		}

	}

	private void requestRewardAfa(UserEventEntity event) {
		logger.info("Request reward afa call");
		logger.info("Creating zendesk ticket. (RADIUS API CALL IMITATION)");
		try {
			ZendeskManager zendeskManager = new ZendeskManager();

			if (event.getUserId() != 0) {
				logger.info("Event contains user id. Proceed");
				AppUserEntity appUserEntity = daoAppUser.findById(event.getUserId());
				String title = "mPass time request(offer)";
				String message = "User: " + appUserEntity.getId() + " and mac: " + appUserEntity.getMac()
						+ " clicked offer. Reward user with additional " + event.getRewardValue() + " minutes.";
				RealmEntity realm = daoRealm.findByName("BPM");
				if (realm != null) {
					logger.info("Realm is valid. Creating ticket.");
					String supportSystemUrl = "https://afahelp.zendesk.com";
					String supportSystemLogin = "stefan.hohmann@bluepodmedia.com";
					String supportSystemPassword = "Bluep0d!";
					Ticket ticket = zendeskManager.createTicket(appUserEntity.getFullName(), appUserEntity.getEmail(),
							title, message, supportSystemUrl, supportSystemLogin, supportSystemPassword, null);
					logger.info("Zendesk ticket created. ");
				} else {
					logger.info("Zendesk cant be created - realm is null.");
				}
			} else {
				logger.info("Zendesk cant be created. Event user id is empty. ");
			}
		} catch (Exception exc) {
			logger.info(exc.toString());
			exc.printStackTrace();
		}

	}

	private void validateReferralReward(RealmEntity realm, UserEventEntity event) {
		try {
			// topup user balance with reward value provided in the event object
			Application.getElasticSearchLogger().indexLog(Application.REFERRAL_MONITOR_ACTIVITY, realm.getId(),
					LogStatus.OK,
					Application.REFERRAL_MONITOR_TRIGGER + " " + " triggering REFERRAL MONITOR for user: "
							+ event.getEmail() + " " + event.getPhoneNumberExt() + " " + event.getPhoneNumber()
							+ " application name: " + event.getApplicationName() + " rewardTypeName: "
							+ event.getRewardTypeName() + " successful reward name: " + event.getRewardName()
							+ " internalT: " + event.getInternalTransactionId());

			// check if user registered using referral code
			// check if user received 1st conversion -> if so trigger reward for
			// referrer
			// check if user received 4th converison -> if so trigger reward for
			// referrer
			AppUserEntity appUser = daoAppUser.findById(event.getUserId());
			// increment the number of successful install conversions
			appUser.setSuccessfulInstallConversions(appUser.getSuccessfulInstallConversions() + 1);
			appUser = daoAppUser.createOrUpdate(appUser);
			boolean isReferralRewardApproved = referralManager.isReferralRewardApproved(realm, appUser);
			if (isReferralRewardApproved) {
				referralManager.processReferralRewardRequest(realm, appUser);
			}
		} catch (Exception exc) {
			// issue notification
			logger.severe(exc.toString());
			exc.printStackTrace();

			Application.getElasticSearchLogger().indexLog(Application.REFERRAL_MONITOR_ACTIVITY, realm.getId(),
					LogStatus.ERROR,
					Application.REFERRAL_MONITOR_ACTIVITY_ERROR + " " + Application.REFERRAL_MONITOR_TRIGGER
							+ " internalT: " + event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED
							+ " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());
		}
	}

	private void requestWalletTopup(RealmEntity realm, UserEventEntity event, boolean isEventFromUserThatWasInviting) {

		try {

			// topup user balance with reward value provided in the event object
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, realm.getId(),
					LogStatus.OK,
					Application.WALLET_PAY_IN + " " + " performing wallet topup for user: " + event.getEmail() + " "
							+ event.getPhoneNumberExt() + " " + event.getPhoneNumber() + " application name: "
							+ event.getApplicationName() + " rewardTypeName: " + event.getRewardTypeName()
							+ " reward name: " + event.getRewardName() + " internalT: "
							+ event.getInternalTransactionId());

			// get wallet attached to user - if null - create one
			WalletDataEntity wallet = daoWalletData.findByUserId(event.getUserId());
			if (wallet == null) {
				wallet = new WalletDataEntity();
				wallet.setIsoCurrencyCode(event.getRewardIsoCurrencyCode());
				wallet.setBalance(0);
				wallet.setUserId(event.getUserId());
			}

			if (event.getApplicationName() != null && event.getApplicationName().toLowerCase().contains("goahead")
					|| event.getApplicationName().toLowerCase().contains("cine")) {
				wallet.setBalance(wallet.getBalance() + event.getCustomRewardValue());
			} else {
				wallet.setBalance(wallet.getBalance() + event.getRewardValue());
			}
			wallet.setTransactionCounter(wallet.getTransactionCounter() + 1);
			daoWalletData.createOrUpdate(wallet);

			// generate wallet transaction connecting wallet topup with
			// UserEvent that generated it
			WalletTransactionEntity walletTransactionEntity = new WalletTransactionEntity();
			walletTransactionEntity.setInternalTransactionId(event.getInternalTransactionId());
			walletTransactionEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
			walletTransactionEntity.setPayoutCurrencyCode(event.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
			if (event.getApplicationName().toLowerCase().contains("goahead")
					|| event.getApplicationName().toLowerCase().contains("cine")) {
				walletTransactionEntity.setPayoutValue(event.getCustomRewardValue());
				walletTransactionEntity.setPayoutCurrencyCode(event.getCustomRewardCurrencyCode());
			} else {
				walletTransactionEntity.setPayoutValue(event.getRewardValue());
			}
			walletTransactionEntity.setUserId(event.getUserId());
			walletTransactionEntity.setStatus(WalletTransactionStatus.SUCCESS.toString());
			walletTransactionEntity.setType(UserEventCategory.WALLET_PAY_IN.toString());
			walletTransactionEntity.setRewardName(event.getOfferTitle());
			// for
			daoWalletTransaction.createOrUpdate(walletTransactionEntity);

			// update event that reward request and response was successful
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardResponseStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardResponseStatusMessage("OK");

			daoUserEvent.createOrUpdate(event, 2);

			// create wallet payin es log
			Application.getElasticSearchLogger().indexWalletTransaction(realm.getId(), event.getPhoneNumber(), "",
					event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
					event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
					event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(),
					"", UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
					UserEventCategory.WALLET_PAY_IN.toString(), "", "", event.getIpAddress(), event.getCountryCode(),
					event.isInstant(), event.getApplicationName(), event.isTestMode());

			// issue notification
			notificationManager.sendWalletTopupNotification(event, true, isEventFromUserThatWasInviting);

		} catch (Exception exc) {
			exc.printStackTrace();
			// issue notification
			notificationManager.sendWalletTopupNotification(event, false, isEventFromUserThatWasInviting);
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, realm.getId(),
					LogStatus.ERROR,
					Application.WALLET_PAY_IN + " " + Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " internalT: "
							+ event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + Arrays.toString(exc.getStackTrace()));
		}
	}

	public boolean requestWalletPayout(RealmEntity realm, UserEventEntity event,
			boolean isEventFromUserThatWasInviting) {
		boolean isSuccess = false;

		try {
			// topup user balance with reward value provided in the event object
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_PAY_OUT + " "
							+ " performing wallet payout for user: " + event.getEmail() + " "
							+ event.getPhoneNumberExt() + " " + event.getPhoneNumber() + " application name: "
							+ event.getApplicationName() + " rewardTypeName: " + event.getRewardTypeName()
							+ " reward name: " + event.getRewardName() + " internalT: "
							+ event.getInternalTransactionId());

			/**
			 * check if that transaction (user event) is not already on the list
			 * of pending wallet transactions - if so do not add it again first
			 * attempt to check if this transaction is not already in the system
			 * - if so mark it as validated as it could be left in the queue
			 */
			WalletTransactionEntity existingWalletTransaction = daoWalletTransaction
					.findByInternalTransactionId(event.getInternalTransactionId());
			if (existingWalletTransaction != null) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
						LogStatus.WARNING,
						Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_PAY_OUT + " "
								+ Application.WALLET_PAY_OUT_DUPLICATE + " "
								+ " transaction already on the list of pending transactions - aborting creation of the new transaction and wallet payout for user: "
								+ event.getEmail() + " " + event.getPhoneNumberExt() + " " + event.getPhoneNumber()
								+ " application name: " + event.getApplicationName() + " rewardTypeName: "
								+ event.getRewardTypeName() + " reward name: " + event.getRewardName() + " internalT: "
								+ event.getInternalTransactionId());
				return false;
			}

			/**
			 * If this is not pending transaction that we have on the list -
			 * deduct cash from wallet and create new pending wallet transaction
			 */
			// get wallet attached to user - if null - create one
			WalletDataEntity wallet = daoWalletData.findByUserId(event.getUserId());
			if (wallet == null) {
				wallet = new WalletDataEntity();
				wallet.setIsoCurrencyCode(event.getRewardIsoCurrencyCode());
				wallet.setBalance(0);
				wallet.setUserId(event.getUserId());
			}

			// process payout for all reward types except go ahead ones
			// go ahead deducts from wallet only after offer is processed on
			// GoAhead side
			if (event.getApplicationName().toUpperCase()
					.equals(Application.APPLICATION_NAME_GO_AHEAD.toString().toUpperCase())
					|| event.getApplicationName().toUpperCase()
							.equals(Application.APPLICATION_NAME_CINETREATS.toString().toUpperCase())) {
			} else {
				double[] payoutCalculation = calculateRewardPayoutBasedOnCarrier(event.getRewardTypeName(),
						event.getRewardValue());
				double payoutThatGoesToUser = payoutCalculation[0];
				double restThatGoesToWallet = payoutCalculation[1];
				if (wallet.getBalance() >= payoutThatGoesToUser) {
					wallet.setBalance(wallet.getBalance() - payoutThatGoesToUser);
					// set reward to be a multiplication of 10 denominals
					// (required
					// by carriers)
					event.setRewardValue(payoutThatGoesToUser);
					// wallet.setBalance(wallet.getBalance()-event.getRewardValue());
					// //deprecated as it didn't take into account carriers
					// payouts
					// that are in 10ns of magnitude only
					wallet.setTransactionCounter(wallet.getTransactionCounter() + 1);
					isSuccess = true;
				} else {
					event.setRewardValue(payoutThatGoesToUser);
					event.setRewardRequestStatus(RespStatusEnum.FAILED + "  reason: not enough funds in the wallet :"
							+ wallet.getBalance() + " for payout: " + payoutThatGoesToUser);
					Application.getElasticSearchLogger().indexLog(Application.WALLET_PAY_OUT, realm.getId(),
							LogStatus.ERROR,
							Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_PAY_OUT + " "
									+ Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " "
									+ Application.WALLET_TRANSACTION_ACTIVITY_ABORTED_NOT_ENOUGH_FUNDS + " "
									+ " status: " + RespStatusEnum.FAILED
									+ "  reason: not enough funds in the wallet for user with id: " + event.getUserId()
									+ " email: " + event.getEmail() + " phone: " + event.getPhoneNumber()
									+ " payout value: " + payoutThatGoesToUser + " internalT: "
									+ event.getInternalTransactionId());
					isSuccess = false;
				}
			}

			// update event
			daoUserEvent.createOrUpdate(event, 1);

			// update wallet data in db
			daoWalletData.createOrUpdate(wallet);

			// TODO move to mode reward response object as payout is valid only
			// when transaction was successful
			// generate wallet transaction connecting wallet topup with
			// UserEvent that generated it

			WalletTransactionEntity walletTransactionEntity = new WalletTransactionEntity();
			walletTransactionEntity.setInternalTransactionId(event.getInternalTransactionId());
			walletTransactionEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
			walletTransactionEntity.setPayoutCurrencyCode(event.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
			walletTransactionEntity.setPayoutValue(event.getRewardValue());
			walletTransactionEntity.setUserId(event.getUserId());
			walletTransactionEntity.setApplicationName(event.getApplicationName());
			if (event.getApplicationName().toUpperCase()
					.equals(Application.APPLICATION_NAME_GO_AHEAD.toString().toUpperCase())
					|| event.getApplicationName().toUpperCase()
							.equals(Application.APPLICATION_NAME_CINETREATS.toString().toUpperCase())) {
				walletTransactionEntity.setStatus("PENDING");
			} else {
				if (!isSuccess) {
					walletTransactionEntity.setStatus(WalletTransactionStatus.FAILED.toString());
				} else {
					// we set to pendign as we await response from mode - only
					// during updateWalletPayoutTransactionStatus we set it to
					// success
					walletTransactionEntity.setStatus(WalletTransactionStatus.PENDING.toString());
				}

				// create wallet payout es log - only when it is in success
				// state as go ahead does not deduct funds if its in pending
				// state
				Application.getElasticSearchLogger().indexWalletTransaction(realm.getId(), event.getPhoneNumber(), "",
						event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
						event.getAdProviderCodeName(), event.getRewardTypeName(),
						event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
						event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
						UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
						UserEventCategory.WALLET_PAY_OUT.toString(), "", "", event.getIpAddress(),
						event.getCountryCode(), event.isInstant(), event.getApplicationName(), event.isTestMode());
			}

			walletTransactionEntity.setType(UserEventCategory.WALLET_PAY_OUT.toString());
			walletTransactionEntity.setRewardName(event.getRewardName());
			daoWalletTransaction.createOrUpdate(walletTransactionEntity);
		} catch (Exception exc) {
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.WALLET_PAY_OUT, realm.getId(), LogStatus.ERROR,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_PAY_OUT + " "
							+ Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " status: " + RespStatusEnum.FAILED
							+ "  code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return false;
		}

		return isSuccess;
	}

	public boolean updateWalletPayoutTransactionStatus(RealmEntity realm, UserEventEntity event,
			boolean isEventFromUserThatWasInviting) {
		boolean isSuccess = false;

		try {
			// topup user balance with reward value provided in the event object
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_STATUS_UPDATE + " "
							+ " wallet transaction status update for user: " + event.getEmail() + " "
							+ event.getPhoneNumberExt() + " " + event.getPhoneNumber() + " application name: "
							+ event.getApplicationName() + " rewardTypeName: " + event.getRewardTypeName()
							+ " reward name: " + event.getRewardName() + " reward response status: "
							+ event.getRewardResponseStatus() + " status response message: "
							+ event.getRewardResponseStatusMessage() + " internalT: "
							+ event.getInternalTransactionId());

			// update wallet transaction status
			WalletTransactionEntity walletTransactionEntity = daoWalletTransaction
					.findByInternalTransactionId(event.getInternalTransactionId());

			// get wallet attached to user - if null - create one
			WalletDataEntity wallet = daoWalletData.findByUserId(event.getUserId());
			if (wallet == null) {
				wallet = new WalletDataEntity();
				wallet.setIsoCurrencyCode(event.getRewardIsoCurrencyCode());
				wallet.setBalance(0);
				wallet.setUserId(event.getUserId());
			}

			// process payout for all reward types except go ahead ones
			// go ahead deducts from wallet only after offer is processed on
			// GoAhead side
			if (event.getApplicationName().toUpperCase()
					.equals(Application.APPLICATION_NAME_GO_AHEAD.toString().toUpperCase())
					|| event.getApplicationName().toUpperCase()
							.equals(Application.APPLICATION_NAME_CINETREATS.toString().toUpperCase())) {
			} else {
				// check operation status - if failed - then return money back
				// to user's wallet
				if (event.getRewardResponseStatus().equals(RewardStatus.SUCCESS.toString())) {
					// if transaction was processed then user cash was
					// successfully into airtime
					walletTransactionEntity.setStatus(WalletTransactionStatus.SUCCESS.toString());
					isSuccess = true;
				} else {
					// return money back to user wallet for transactions that
					// had pending status
					if (walletTransactionEntity.getStatus().equals(WalletTransactionStatus.PENDING.toString())) {
						walletTransactionEntity.setStatus(WalletTransactionStatus.FAILED.toString());
						isSuccess = false;
						double cashDeductedFromWallet = event.getRewardValue();
						wallet.setBalance(wallet.getBalance() + cashDeductedFromWallet);
						Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
								LogStatus.OK,
								Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_STATUS_UPDATE + " "
										+ Application.WALLET_STATUS_UPDATE_CASH_RETURN + " "
										+ " wallet cash return due to failed transaction for user: " + event.getEmail()
										+ " " + event.getPhoneNumberExt() + " " + event.getPhoneNumber()
										+ " application name: " + event.getApplicationName() + " rewardTypeName: "
										+ event.getRewardTypeName() + " reward name: " + event.getRewardName()
										+ " returned cash: " + cashDeductedFromWallet + " b: " + wallet.getBalance()
										+ " reward response status: " + event.getRewardResponseStatus()
										+ " status response message: " + event.getRewardResponseStatusMessage()
										+ " internalT: " + event.getInternalTransactionId());
					}
				}
			}

			// update wallet data in db
			daoWalletData.createOrUpdate(wallet);
			walletTransactionEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));

			if (event.getApplicationName().toUpperCase()
					.equals(Application.APPLICATION_NAME_GO_AHEAD.toString().toUpperCase())
					|| event.getApplicationName().toUpperCase()
							.equals(Application.APPLICATION_NAME_CINETREATS.toString().toUpperCase())) {
				walletTransactionEntity.setStatus("PENDING");
			} else {
				if (!isSuccess) {
					walletTransactionEntity.setStatus(WalletTransactionStatus.FAILED.toString());
				} else {
					walletTransactionEntity.setStatus(WalletTransactionStatus.SUCCESS.toString());
				}
			}

			daoWalletTransaction.createOrUpdate(walletTransactionEntity);
		} catch (Exception exc) {
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.WALLET_PAY_OUT, realm.getId(), LogStatus.ERROR,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_PAY_OUT + " "
							+ Application.WALLET_STATUS_UPDATE + " " + Application.WALLET_TRANSACTION_ACTIVITY_ABORTED
							+ " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return false;
		}

		return isSuccess;
	}

	public boolean isWalletPayoutRequestValid(RealmEntity realm, UserEventEntity event,
			boolean isEventFromUserThatWasInviting) {
		boolean isValid = false;

		try {
			// topup user balance with reward value provided in the event object
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_TRANSACTION_ACTIVITY_CHECK + " "
							+ Application.WALLET_PAY_OUT + " " + " validating wallet payout request for user: "
							+ event.getEmail() + " " + event.getPhoneNumberExt() + " " + event.getPhoneNumber()
							+ " application name: " + event.getApplicationName() + " rewardTypeName: "
							+ event.getRewardTypeName() + " reward name: " + event.getRewardName() + " internalT: "
							+ event.getInternalTransactionId());

			/*
			 * //first attempt to check if this transaction is not already in
			 * the system - if so mark it as validated as it could be left in
			 * the queue WalletTransactionEntity existingWalletTransaction =
			 * daoWalletTransaction.findByInternalTransactionId(event.
			 * getInternalTransactionId()); if(existingWalletTransaction !=
			 * null) {
			 * Application.getElasticSearchLogger().indexLog(Application.
			 * REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
			 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
			 * Application.WALLET_TRANSACTION_ACTIVITY_CHECK + " " +
			 * Application.WALLET_PAY_OUT + " " +
			 * " validated transaction already in the list of system transactions - setting it as correctly validated: "
			 * + event.getEmail() + " " + event.getPhoneNumberExt() + " " +
			 * event.getPhoneNumber() + " application name: " +
			 * event.getApplicationName() + " rewardTypeName: " +
			 * event.getRewardTypeName() + " reward name: " +
			 * event.getRewardName() + " internalT: " +
			 * event.getInternalTransactionId()); return false; }
			 */

			// get wallet attached to user - if null - create one
			WalletDataEntity wallet = daoWalletData.findByUserId(event.getUserId());
			if (wallet == null) {
				wallet = new WalletDataEntity();
				wallet.setIsoCurrencyCode(event.getRewardIsoCurrencyCode());
				wallet.setBalance(0);
				wallet.setUserId(event.getUserId());
			}

			// process payout for all reward types except go ahead ones
			// go ahead deducts from wallet only after offer is processed on
			// GoAhead side
			if (event.getApplicationName().toUpperCase()
					.equals(Application.APPLICATION_NAME_GO_AHEAD.toString().toUpperCase())
					|| event.getApplicationName().toUpperCase()
							.equals(Application.APPLICATION_NAME_CINETREATS.toString().toUpperCase())) {
			} else {
				double[] payoutCalculation = calculateRewardPayoutBasedOnCarrier(event.getRewardTypeName(),
						event.getRewardValue());
				double payoutThatGoesToUser = payoutCalculation[0];
				double restThatGoesToWallet = payoutCalculation[1];
				if (wallet.getBalance() >= payoutThatGoesToUser && payoutThatGoesToUser > 0) {
					isValid = true;
				} else {
					event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
					event.setRewardRequestStatusMessage(
							"Request aborted - not enought funds in the wallet, balance: " + wallet.getBalance());
					Application.getElasticSearchLogger().indexLog(Application.WALLET_PAY_OUT, realm.getId(),
							LogStatus.ERROR,
							Application.WALLET_TRANSACTION_ACTIVITY + " "
									+ Application.WALLET_TRANSACTION_ACTIVITY_CHECK + " " + Application.WALLET_PAY_OUT
									+ " " + Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " "
									+ Application.WALLET_TRANSACTION_ACTIVITY_ABORTED_NOT_ENOUGH_FUNDS + " "
									+ " status: " + RespStatusEnum.FAILED
									+ "  reason: not enough funds in the wallet for user with id: " + event.getUserId()
									+ " email: " + event.getEmail() + " phone: " + event.getPhoneNumber()
									+ " payout value: " + payoutThatGoesToUser + " internalT: "
									+ event.getInternalTransactionId());
					isValid = false;
					// update event
					daoUserEvent.createOrUpdate(event, 1);
				}
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.WALLET_PAY_OUT, realm.getId(), LogStatus.ERROR,
					Application.WALLET_TRANSACTION_ACTIVITY + " " + Application.WALLET_TRANSACTION_ACTIVITY_CHECK + " "
							+ Application.WALLET_PAY_OUT + " " + Application.WALLET_TRANSACTION_ACTIVITY_ABORTED
							+ " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return false;
		}

		return isValid;
	}

	private UserEventEntity updateUserEvent(UserEventEntity originalEvent, UserEventEntity currentEvent, int type) {

		String phoneNumber = currentEvent.getPhoneNumber();
		logger.info("Updating user event for internalT: " + originalEvent.getInternalTransactionId()
				+ "current phone number: " + phoneNumber + " original phone number: " + originalEvent.getPhoneNumber()
				+ " type: " + type);
		currentEvent.setPhoneNumber(originalEvent.getPhoneNumber());
		currentEvent = daoUserEvent.createOrUpdate(currentEvent, type);
		currentEvent.setPhoneNumber(phoneNumber);
		return currentEvent;
	}

	// ---------------------- handle request to rewarding system
	// -----------------------
	public String requestRewardMode(UserEventEntity event, InvitationEntity invitation,
			boolean isEventFromUserThatWasInviting) {

		boolean isPushToModeAllowed = false;
		EventQueueEntity eventQueueElement = null;
		RealmEntity realm = null;
		UserEventEntity originalEvent = null;
		try {
			originalEvent = (UserEventEntity) event.clone();
			logger.info("Original event phone number: " + originalEvent.getPhoneNumber());
			realm = daoRealm.findById(event.getRealmId());

			/*
			 * // =================================== testing mode
			 * ============================================== // if testing is
			 * enabled - send notification and abort real mode if
			 * (testManager.isTestModeEnabledForRewardType(realm, event)) {
			 * requestTestRewardMode(event); //need to notify supersonic that
			 * event was received OK (otherwise it will attempt to resend it to
			 * us again) return ""; }
			 */

			// ======================= handle request to mode
			// =========================
			if (realm.isModeQueueing()) {
				// identify if we can push that event directly to mode
				isPushToModeAllowed = eventQueueManager.validateEventPushToMode(event);
				if (isPushToModeAllowed) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
							LogStatus.OK,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
									+ Application.EVENT_QUEUE_ACTIVITY + " push to mode allowed for internalT: "
									+ event.getInternalTransactionId() + " issuing credit request for payout: "
									+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue()
									+ " phone: " + event.getPhoneNumber() + " rewardType: " + event.getRewardTypeName()
									+ " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode() + " friend phone: "
									+ event.getFriendPhoneNumber());

					// do nothing here - let request to be made instantly via
					// the remaining code in this method
					eventQueueElement = daoEventQueue.findByEventId(event.getId());
					if (eventQueueElement != null) { // update the status of the
														// event that will be
														// now pushed to
														// rewarding system
						Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
								LogStatus.OK,
								Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
										+ Application.EVENT_QUEUE_ACTIVITY
										+ " updating queue element status with issue date for internalT: "
										+ event.getInternalTransactionId() + " issuing credit request for payout: "
										+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue()
										+ " phone: " + event.getPhoneNumber() + " rewardType: "
										+ event.getRewardTypeName() + " reward: " + event.getRewardValue()
										+ " reward currency: " + event.getRewardIsoCurrencyCode() + " friend phone: "
										+ event.getFriendPhoneNumber());

						eventQueueElement.setRewardingSystemIssueDate(new Timestamp(System.currentTimeMillis()));
						eventQueueElement.setPushedToRewardingSystem(true);
						eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);
					} else {
						// add the event data to queue table as it was not added
						// before
						eventQueueElement = new EventQueueEntity();
						eventQueueElement.setEventId(event.getId());
						eventQueueElement.setGenerationDate(new Timestamp(System.currentTimeMillis()));
						eventQueueElement.setPhoneNumberExtension(event.getPhoneNumberExt());
						eventQueueElement.setPhoneNumber(event.getPhoneNumber());
						eventQueueElement.setUserId(event.getUserId());
						eventQueueElement.setRewardingSystemIssueDate(new Timestamp(System.currentTimeMillis()));
						eventQueueElement.setPushedToRewardingSystem(true);

						// persist in the event queue
						eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);

						Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
								LogStatus.OK,
								Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
										+ Application.EVENT_QUEUE_ACTIVITY
										+ " added new request to queue - sending it instantly to mode for internalT: "
										+ event.getInternalTransactionId() + " issuing credit request for payout: "
										+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue()
										+ " phone: " + event.getPhoneNumber() + " rewardType: "
										+ event.getRewardTypeName() + " reward: " + event.getRewardValue()
										+ " reward currency: " + event.getRewardIsoCurrencyCode() + " friend phone: "
										+ event.getFriendPhoneNumber());

					}
				} else { // end reward requesting at this stage as we need to
							// queue the event and process it later
					// add the event data to queue table
					eventQueueElement = new EventQueueEntity();
					eventQueueElement.setEventId(event.getId());
					eventQueueElement.setGenerationDate(new Timestamp(System.currentTimeMillis()));
					eventQueueElement.setPhoneNumberExtension(event.getPhoneNumberExt());
					eventQueueElement.setPhoneNumber(event.getPhoneNumber());

					eventQueueElement.setUserId(event.getUserId());
					// persist in the event queue
					eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);

					// update event status and set it to queued
					event.setQueueStatus(EventQueueStatus.QUEUED.toString());

					event = this.updateUserEvent(originalEvent, event, 2);
					// event = daoUserEvent.createOrUpdate(event, 2);

					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
							LogStatus.OK,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
									+ Application.EVENT_QUEUE_ACTIVITY
									+ " added element to queue for later processing for internalT: "
									+ event.getInternalTransactionId() + " issuing credit request for payout: "
									+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue()
									+ " phone: " + event.getPhoneNumber() + " friend phone : "
									+ event.getFriendPhoneNumber() + " rewardType: " + event.getRewardTypeName()
									+ " reward: " + event.getRewardValue() + " reward currency: "
									+ event.getRewardIsoCurrencyCode());

					// need to notify supersonic that event was received OK
					// (otherwise it will attempt to resend it to us again)
					return "{\"response\":\" status: " + event.getTransactionId() + ":OK" + " code: "
							+ RespCodesEnum.OK_NO_CONTENT + "\"}";
				}
			}

			// push directly to mode
			String carrierName = "Unknown";
			try {
				UserEventEntity recentClickEvent = daoUserEvent.findMostRecentOfferClickEvent(event.getUserId(),
						"INSTALL");
				if (recentClickEvent != null) {
					carrierName = recentClickEvent.getCarrierName();
					if (carrierName == null || carrierName.length() == 0) {
						carrierName = "Unknown";
					}
				}

				if (event.getFriendPhoneNumber() != null) {
					UserFriendEntity userFriend = userFriendManager.getUserFriendWithPhoneNumber(event.getUserId(),
							event.getFriendPhoneNumber());
					WalletPayoutCarrierEntity carrier = this.daoWalletPayoutCarrier
							.findById(userFriend.getPayoutCarrierId());
					if (carrier != null) {
						carrierName = carrier.getName();
					}
				}

			} catch (Exception exc) {
				logger.severe("error when retrieving most recent install event from user with id: " + event.getUserId()
						+ " error: " + exc.toString());
				exc.printStackTrace();
			}

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
							+ Application.REWARD_REQUEST_IDENTIFIED + " internalT: " + event.getInternalTransactionId()
							+ " issuing credit request for payout: " + event.getOfferPayout() + " (in target currency: "
							+ event.getRevenueValue() + " phone: " + event.getPhoneNumber() + " rewardType: "
							+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
							+ event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName
							+ " friend phone number: " + event.getFriendPhoneNumber()
							+ " original phone number(user phone number): " + originalEvent.getPhoneNumber());

			// extract mode configuration
			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();
			String url = realm.getModeCreditUrl();

			// if test is enabled - use mockup mode endpoint
			if (testManager.isTestModeEnabledForRewardType(realm, event)) {
				url = realm.getModeMockupUrl();
			} else { // use production mode url
				url = realm.getModeCreditUrl();
			}

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(realm.getConnectionTimeout() * 1000);
			con.setReadTimeout(realm.getReadTimeout() * 1000);
			// add reuqest header
			con.setRequestMethod("POST");

			// make sure that phone number does not contain 0 at the beginning
			// and if so - remove it

			event = updateFriendPhone(event);
			logger.info("Current event after update friend phone number: " + event.getPhoneNumber());
			
			
			String urlParameters = "MSISDN=" + event.getPhoneNumberExt() + event.getPhoneNumber()
					+ "&OriginTransactionID=" + event.getId() + // "&OriginTransactionID="+event.getInternalTransactionId()+
					"&Reward=" + event.getRewardValue() + "&ISOCurrCode=" + event.getRewardIsoCurrencyCode() + "&User="
					+ bpUser + "&Password=" + bpPass + "&Operator=" + carrierName;

			// only to display params
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
							+ " credit successfully requested for internalT: " + event.getInternalTransactionId()
							+ " reward value: " + event.getRewardValue() + " rewardType: " + event.getRewardTypeName()
							+ " offerPayout: " + event.getOfferPayout() + " offer payout currency: "
							+ event.getOfferPayoutIsoCurrencyCode() + " original phone number(user phone number): "
							+ originalEvent.getPhoneNumber()
							// + " rewardUrl: " + url + " urlParams: " +
							// urlParameters
							+ " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			// System.out.println("\nSending 'POST' request to URL : " + url);
			// System.out.println("Post parameters : " + urlParameters);
			// System.out.println("Response Code : " + responseCode);

			// optimistically update event before call and only set it to faulty
			// one after (when we 100% know that transaction will not be
			// processed)
			// this is to avoid situation when we overwrite reward response with
			// reward request object data

			event.setPhoneNumber(originalEvent.getPhoneNumber());

			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			// update event status and set it to queued
			event.setQueueStatus(EventQueueStatus.SENT.toString());
			// event = daoUserEvent.createOrUpdate(event, 2);
			event = this.updateUserEvent(originalEvent, event, 2);
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

				/*
				 * The immediate response send back on calling mode api is in
				 * the format :- {"Status":0,"Msg":
				 * "Request received successfully" ,"OriginTransactionID":89289}
				 * So in that case you should have received :-
				 * {"Status":1,"Msg":"missing parameters"}
				 * 
				 * And so if status is 0, wait for response, if 1 means the
				 * request failed.
				 */
				// TODO validate against this
				String responseStatus = "UNKNOWN";
				String STATUS_SUCCESS = "\"Status\":0"; // missing params
				String STATUS_FAILED = "\"Status\":1"; // missing params
				statusMessage = "Unable to parse";
				try {
					if (responseString.contains(STATUS_SUCCESS)) {
						responseStatus = RespStatusEnum.SUCCESS.toString();
					} else if (responseString.contains(STATUS_FAILED)) {
						responseStatus = RespStatusEnum.FAILED.toString();
					} else {
						responseStatus = RespStatusEnum.UNKNOWN.toString();
					}

					statusMessage = responseString.substring(responseString.indexOf("Msg\":") + 6,
							responseString.indexOf(",\"OriginTransactionID") - 1);
				} catch (Exception exc) {
					logger.severe("Error parsing response from MODE: " + exc.toString());
					exc.toString();
				}

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " credit successfully requested for internalT: " + event.getInternalTransactionId()
								+ " reward value: " + event.getRewardValue() + " rewardType: "
								+ event.getRewardTypeName() + " offerPayout: " + event.getOfferPayout()
								+ " offer payout currency: "
								// + event.getOfferPayoutIsoCurrencyCode() +
								// " rewardUrl: " + url + " urlParams: " +
								// urlParameters + " status: "
								+ " responseStatus: " + responseStatus + " responseString: " + responseString
								+ " Status message: " + statusMessage + " original phone number(user phone number): "
								+ originalEvent.getPhoneNumber());

				// send correctly formatted response for supersonic (read manual
				// about callbacks:
				// http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: " + event.getTransactionId() + ":OK"
						+ " code: " + RespCodesEnum.OK_NO_CONTENT + "\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK, Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " sent successful response: " + successfulResponseStatus);

				if (realm.isModeQueueing()) {
					// update queue element
					eventQueueElement.setRewardingSystemSendStatus(responseStatus);
					eventQueueElement.setRewardingSystemSendStatusMessage(statusMessage); // eventQueueElement.setRewardingSystemSendStatusMessage("OK");
					eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);
				}

				event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
				event.setRewardRequestStatus(responseStatus);
				event.setRewardRequestStatusMessage(statusMessage);
				event.setQueueStatus(EventQueueStatus.SENT.toString());
				// event = daoUserEvent.createOrUpdate(event, 2);
				event = this.updateUserEvent(originalEvent, event, 2);
				return successfulResponseStatus;
			} else {
				if (responseCode == 403) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
							LogStatus.ERROR,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
									+ " error during credit request: authentication failure (please make sure that login/pass to mode are correctly set) for event: "
									+ event.getUserId() + " phone: " + event.getPhoneNumber() + " internalT: "
									+ event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED + " code: "
									+ RespCodesEnum.ERROR_AUTHENTICATION_FAILURE);
					// update event
					event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
					event.setRewardRequestStatusMessage(
							"Authentication failure - please make sure that login/pass to mode are correctly set");
					// event = daoUserEvent.createOrUpdate(event, 3);
					event = this.updateUserEvent(originalEvent, event, 3);
					if (realm.isModeQueueing()) {
						// update queue element
						eventQueueElement.setRewardingSystemSendStatus(RespStatusEnum.FAILED.toString());
						eventQueueElement.setRewardingSystemSendStatusMessage(
								"Authentication failure - please make sure that login/pass to mode are correctly set");
						eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);
					}

					// update conversion history to store information about
					// failed reward attempt
					updateUserConversionHistory(event);

					return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " + event.getTransactionId() + ":OK"
							+ " code: " + RespCodesEnum.ERROR_AUTHENTICATION_FAILURE + "\"}";
				} else {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
							LogStatus.ERROR,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
									+ " error during credit request: unknown response code: " + responseCode
									+ " for event: " + event.getUserId() + " rewardType: " + event.getRewardTypeName()
									+ " phone: " + event.getPhoneNumber() + " internalT: "
									+ event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED + " code: "
									+ RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);

					// update event
					event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
					event.setRewardRequestStatusMessage(
							"Unknown response code: " + responseCode + " was expecting 200 or 403");
					// event = daoUserEvent.createOrUpdate(event, 4);
					event = this.updateUserEvent(originalEvent, event, 4);
					if (realm.isModeQueueing()) {
						// update queue element
						eventQueueElement.setRewardingSystemSendStatus(RespStatusEnum.FAILED.toString());
						eventQueueElement.setRewardingSystemSendStatusMessage(
								"Unknown response code: " + responseCode + " was expecting 200 or 403");
						eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);
					}

					// update conversion history to store information about
					// failed reward attempt
					updateUserConversionHistory(event);

					return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " + event.getTransactionId() + ":OK"
							+ " code: " + RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE + "\"}";
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " internalT: "
							+ event.getInternalTransactionId() + " error crediting user: " + exc.toString()
							+ " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: " + exc.toString());
			// event = daoUserEvent.createOrUpdate(event, 7);
			event = this.updateUserEvent(originalEvent, event, 7);
			if (realm.isModeQueueing()) {
				// update queue element
				eventQueueElement.setRewardingSystemSendStatus(RespStatusEnum.FAILED.toString());
				eventQueueElement.setRewardingSystemSendStatusMessage("Internal server error: " + exc.toString());
				eventQueueElement = daoEventQueue.createOrUpdate(eventQueueElement);
			}

			// update conversion history to store information about failed
			// reward attempt
			updateUserConversionHistory(event);

			return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " + event.getTransactionId() + ":OK"
					+ " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	private UserEventEntity updateFriendPhone(UserEventEntity event) {
		logger.info("******** UPDATING PHONE NUMBER BEFORE MODE REQUEST *******");
		if (event != null) {
			if (event.getFriendPhoneNumber() != null) {
				logger.info("Replaced phone number " + event.getPhoneNumber() + " to " + event.getFriendPhoneNumber());
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ "Replaced phone number " + event.getPhoneNumber() + " to "
								+ event.getFriendPhoneNumber());
				event.setPhoneNumber(event.getFriendPhoneNumber());
			} else {
				logger.info("Didnt replaced phone number because friend phone number is null");
			}
		}

		return event;
	}

	// ---------------------- handle request to rewarding system
	// -----------------------
	public void requestTestRewardMode(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());

			logger.info("in test mode - aborting real reward system request - faking reward generation...");
			// send reward notification
			notificationManager.sendRewardNotification(event, true, false);

			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("TEST OK");
			daoUserEvent.createOrUpdate(event, 2);

			// reward response fake processing
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
					LogStatus.WARNING,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
							+ " in test mode - aborting real reward system request - faking reward generation..."
							+ " reward date already set for internal trans id: " + event.getInternalTransactionId()
							+ ": " + " internalT: " + event.getInternalTransactionId());

			event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());

			// update reward date
			event.setRewardResponseStatusMessage("TEST OK");
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			if (event.getRewardResponseStatus().equals(RewardStatus.SUCCESS.toString())) {
				event.setApproved(true);
			} else {
				event.setApproved(false);
			}
			daoUserEvent.createOrUpdate(event, 8);

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
							+ " in test mode - aborting real reward system request - faking reward generation..."
							+ " successfully rewarded event with internal transaction id: "
							+ event.getInternalTransactionId() + " internalT: " + event.getInternalTransactionId()
							+ " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);

			// update conversion history (needed to filter out already
			// clicked offers for particular user)
			updateUserConversionHistory(event);

			if (event.getRewardResponseStatus().equals(RewardStatus.SUCCESS.toString())) { // send
																							// only
																							// successful
																							// notification
				// internal method to handle notifications
				notificationManager.sendRewardNotification(event, true, false);
			} else {
				notificationManager.sendRewardNotification(event, false, false);
			}

			if (event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.INVITATION_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
								+ "in test mode - aborting real reward system request - faking reward generation..."
								+ " successfully rewarded event with internal transaction id: "
								+ event.getInternalTransactionId() + " internalT: " + event.getInternalTransactionId()
								+ " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " internalT: "
							+ event.getInternalTransactionId() + " error crediting user: " + exc.toString()
							+ " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: " + exc.toString());
			daoUserEvent.createOrUpdate(event, 7);

			// update conversion history to store information about failed
			updateUserConversionHistory(event);
		}
	}

	// ------------------------------- issue reward request to GoAhead
	// ------------------------
	public String requestRewardGoAhead(UserEventEntity event, InvitationEntity invitation,
			boolean isEventFromUserThatWasInviting) {
		System.out.println("reuqestRewardGoAhead");
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());

			// // if testing is enabled - send notification and abort real mode
			// // testing
			// if (realm.isTestMode()) {
			// sendRewardNotification(event);
			// logger.info("in test mode - aborting real reward system
			// request...");
			// return "";
			// }

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
							+ Application.REWARD_REQUEST_IDENTIFIED + " internalT: " + event.getInternalTransactionId()
							+ " issuing credit request for payout: " + event.getOfferPayout() + " (in target currency: "
							+ event.getRevenueValue() + " phone: " + event.getPhoneNumber() + " rewardType: "
							+ event.getRewardTypeName() + " reward: " + event.getRewardValue() + " reward currency: "
							+ event.getRewardIsoCurrencyCode());

			AppUserEntity user = daoAppUser.findByPhoneNumber(event.getPhoneNumber());
			RealmEntity realmEntity = daoRealm.findById(user.getRealmId());

			// everything is ok, send ticket and email
			String userName = user.getFullName();
			String userEmail = user.getEmail();
			String subject = "GoAhead wallet payout offer";
			String content = "Automatically generated message. This is GoAhead reward info." + " User: "
					+ user.getFullName() + " phone number: " + user.getPhoneNumber() + " phone number extension: "
					+ user.getPhoneNumberExtension() + " email: " + user.getEmail() + " rewardName: "
					+ event.getRewardName() + " offer value: " + event.getRewardValue()
					+ " Please set this ticket to solved status after rewarding this user.";

			String zendeskUrl = realmEntity.getSupportSystemUrl();
			String zendeskUser = realmEntity.getSupportSystemUserName();
			String zendeskPassword = realmEntity.getSupportSystemPassword();
			String title = "GoAhead";

			// System.out.println("***************");
			// System.out.println("***************");
			System.out.println(event.getRewardValue());

			List<String> adminRecipents = new ArrayList<String>();
			adminRecipents.add("jakub.homlala@bluepodmedia.com");
			adminRecipents.add("mariusz.jacyno@bluepodmedia.com");
			adminRecipents.add("sam.armour@bluepodmedia.com");

			if (event.getRewardTypeName().equals(Application.REWARD_PROVIDER_CINETREATS)
					|| event.getRewardTypeName().equals(Application.REWARD_PROVIDER_CINETREATS_AU)) {
				zendeskUrl = "https://cinetreatshelp.zendesk.com/";
				zendeskUser = "sam.armour@bluepodmedia.com";
				zendeskPassword = "cantona";
				title = "Cinetreats";

				subject = "Cinetreats wallet payout offer";
				content = "Automatically generated message. This is Cinetreats reward info." + " User: "
						+ user.getFullName() + " phone number: " + user.getPhoneNumber() + " phone number extension: "
						+ user.getPhoneNumberExtension() + " email: " + user.getEmail() + " rewardName: "
						+ event.getRewardName() + " offer value: " + event.getRewardValue()
						+ " Please set this ticket to solved status after rewarding this user.";

				adminRecipents.add("support@cinetreats.co.uk ");
			}

			List<String> tags = new ArrayList<String>();
			tags.add("payoutoffer");

			Ticket ticket = zendeskManager.createTicket(userName, userEmail, subject, content, zendeskUrl, zendeskUser,
					zendeskPassword, title);

			zendeskManager.setTicketTags(ticket, tags, zendeskUrl, zendeskUser, zendeskPassword, title);

			WalletTransactionEntity walletTransaction = daoWalletTransaction
					.findByInternalTransactionId(event.getInternalTransactionId());
			if (walletTransaction != null) {
				walletTransaction.setTicketId(ticket.getId());
			}

			String userMessage = "You have redeemed following offer: " + event.getRewardName() + " for "
					+ event.getRewardValue() + "credits. We are now processing it and will shortly get back to you.";

			String adminMessage = content + " Please visit zendesk panel to process offer request.";

			/*
			 * mailManager.sendEmail(realmEntity, "jhomlala@gmail.com",
			 * "Admin message", adminMessage);
			 * mailManager.sendEmail(realmEntity,
			 * "mariusz.jacyno@bluepodmedia.com", "Admin message",
			 * adminMessage); mailManager.sendEmail(realmEntity,
			 * "welcome@airrewardz.net", "Admin message", adminMessage);
			 */

			for (String admin : adminRecipents) {
				mailManager.sendEmail(realmEntity, admin, "Admin message", adminMessage);
			}

			mailManager.sendEmail(realmEntity, user.getEmail(), "User message", userMessage);

			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event, 2);

			if (invitation != null) { // if we have invitation object included
										// (referral programme) update status as
										// well
				Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
								+ Application.REWARD_REQUEST_IDENTIFIED + " internalT: "
								+ event.getInternalTransactionId() + " issuing credit request for payout: "
								+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue()
								+ " phone: " + event.getPhoneNumber() + " rewardType: " + event.getRewardTypeName()
								+ " reward: " + event.getRewardValue() + " reward currency: "
								+ event.getRewardIsoCurrencyCode());// +"
																	// urlParameters:
																	// "+urlParameters);

				if (isEventFromUserThatWasInviting) {
					invitation.setInvitingRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					invitation.setInvitingRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
					invitation.setInvitingRewardRequestStatusMessage("OK");
				} else {
					invitation.setInvitedRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					invitation.setInvitedRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
					invitation.setInvitedRewardRequestStatusMessage("OK");
				}
				daoInvitation.createOrUpdate(invitation);
			}

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
							+ " credit successfully requested for internalT: " + event.getInternalTransactionId()
							+ " reward value: " + event.getRewardValue() + " rewardType: " + event.getRewardTypeName()
							+ " offerPayout: " + event.getOfferPayout() + " offer payout currency: "
							+ event.getOfferPayoutIsoCurrencyCode() + " status: " + RespStatusEnum.SUCCESS + " code: "
							+ RespCodesEnum.OK_NO_CONTENT);

			updateUserConversionHistory(event);

			String successfulResponseStatus = "{\"response\":\" status: " + event.getTransactionId() + ":OK" + " code: "
					+ RespCodesEnum.OK_NO_CONTENT + "\"}";
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
							+ " sent successful response: " + successfulResponseStatus);

			return successfulResponseStatus;
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " internalT: "
							+ event.getInternalTransactionId() + " error crediting user: " + exc.toString()
							+ " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: " + exc.toString());
			daoUserEvent.createOrUpdate(event, 7);

			if (invitation != null) { // if we have invitation object included
										// (referral programme) update status as
										// well
				if (isEventFromUserThatWasInviting) {
					invitation.setInvitingRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					invitation.setInvitingRewardRequestStatus(RespStatusEnum.FAILED.toString());
					invitation.setInvitingRewardRequestStatusMessage("Internal server error: " + exc.toString());
				} else {
					invitation.setInvitedRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					invitation.setInvitedRewardRequestStatus(RespStatusEnum.FAILED.toString());
					invitation.setInvitedRewardRequestStatusMessage("Internal server error: " + exc.toString());
				}
				daoInvitation.createOrUpdate(invitation);
			}

			// update conversion history to store information about failed
			// reward attempt
			updateUserConversionHistory(event);

			return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " + event.getTransactionId() + ":OK"
					+ " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	// ------------------------------------ issue reward request to Radius
	// --------------------------------
	public String requestRewardRadius(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());
			String phoneNumber = event.getPhoneNumber();

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
							+ Application.REWARD_REQUEST_IDENTIFIED + " internalT: " + event.getInternalTransactionId()
							+ " issuing credit request for payout: " + event.getOfferPayout() + " (in target currency: "
							+ event.getRevenueValue() + " phone: " + event.getPhoneNumber() + " adjusted: ("
							+ phoneNumber + ")" + " rewardType: " + event.getRewardTypeName() + " reward: "
							+ event.getRewardValue() + " reward currency: " + event.getRewardIsoCurrencyCode());

			// --------------------------- handle request to rewarding system
			// ---------------------------------
			CloudtraxConfigurationEntity ctraxConfig = daoCloudtraxConfiguration
					.findByNetworkName(event.getAfaNetworkName());
			RadiusConfigurationEntity radiusConfig = daoRadiusConfiguration.findById(ctraxConfig.getRadiusServer1Id());
			RadiusProvider radiusProvider = new RadiusProvider(radiusConfig);
			AppUserEntity appUser = daoAppUser.findById(event.getUserId());

			// if reached this stage - indicate that reward request was made
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event, 2);

			// issue reward request
			RespStatusEnum rewardStatus = radiusProvider.addTime(appUser.getEmail(), (int) event.getRewardValue()); // assume
																													// reward
																													// value
																													// in
																													// this
																													// case
																													// corresponds
																													// to
																													// minutes

			if (event.getRewardDate() != null) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
						LogStatus.WARNING,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
								+ " reward date already set for internal trans id: " + event.getInternalTransactionId()
								+ ": " + " internalT: " + event.getInternalTransactionId()
								+ event.getRewardDate().toString() + " udpating with new time");
			}

			if (rewardStatus.equals(RespStatusEnum.SUCCESS.toString())) { // update
																			// user
																			// event
																			// data
				event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				event.setRewardResponseStatusMessage(RespStatusEnum.SUCCESS.toString());
				event.setApproved(true);
			} else {
				event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				event.setRewardResponseStatusMessage(RespStatusEnum.FAILED.toString());
				event.setApproved(false);
			}

			// --------------------------------- Reward Response handling starts
			// ---------------------------
			String dataContent = "intercepted reward notification: " + "internalTransactionId: "
					+ event.getInternalTransactionId() + " status: [" + rewardStatus.toString() + "]"
					+ " statusMessage: [ Radius response: " + rewardStatus.toString() + "]";
			logger.info(dataContent);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " "
							+ Application.REWARD_RESPONSE_IDENTIFIED + " " + dataContent);

			// update reward date
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 8);

			// update conversion history (needed to filter out already clicked
			// offers for particular user)
			updateUserConversionHistory(event);

			// notify user via sms if reward was successful
			String smsMessageContent = "You have been rewarded " + event.getRewardValue()
					+ " minutes of free Internet access";
			notificationManager.sendRewardNotificationSMS(event, realm, smsMessageContent);

			// send correctly formatted response for supersonic (read manual
			// about callbacks:
			// http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
			return "{\"response\":\" status: " + event.getTransactionId() + ":OK" + " code: "
					+ RespCodesEnum.OK_NO_CONTENT + "\"}";

		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
							+ " error during reward request for internal trans id: " + event.getInternalTransactionId()
							+ ": " + " user phone number: " + event.getPhoneNumber() + " error: " + exc.toString());

			event.setRewardResponseStatus(RewardStatus.FAILED.toString());
			event.setRewardResponseStatusMessage(exc.toString());
			event.setApproved(false);
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event, 8);

			return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: "
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	// ------------------------------------ issue reward request to GoAhead
	// --------------------------------
	public String requestRewardGoAheadDeprecated(UserEventEntity event) {
		try {

			RealmEntity realm = daoRealm.findById(event.getRealmId());
			String phoneNumber = event.getPhoneNumber();

			if (event.getPhoneNumber().startsWith("44")) // do nothing as user
															// already provided
															// country code
			{
				phoneNumber = event.getPhoneNumber();
			} else if (event.getPhoneNumber().startsWith("0")) // cut 0 and add
																// 44
			{
				phoneNumber = "44" + event.getPhoneNumber().substring(1);
			}

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " "
							+ Application.REWARD_REQUEST_IDENTIFIED + " internalT: " + event.getInternalTransactionId()
							+ " issuing credit request for payout: " + event.getOfferPayout() + " (in target currency: "
							+ event.getRevenueValue() + " phone: " + event.getPhoneNumber() + " adjusted: ("
							+ phoneNumber + ")" + " rewardType: " + event.getRewardTypeName() + " reward: "
							+ event.getRewardValue() + " reward currency: " + event.getRewardIsoCurrencyCode());

			// logger.info("credit request for user: "+event.getUserId()+
			// " internalTransaction: "+event.getInternalTransactionId()+
			// " currency: "+event.getOfferPayoutIsoCurrencyCode()+" payout:
			// "+event.getOfferPayout());

			// --------------------------- handle request to rewarding system
			// ---------------------------------
			String smsHashCode = "sdlfjwlj3l23j4lkjsdlfjsdf";
			String smsMessageContent = "You have been rewarded \u00A3" + event.getRewardValue()
					+ " off your next journey on Brighton and Hove bus travel - "
					+ "Please just use the following code to top up your ticket wallet in the Brighton and Hove M Tickets app"
					+ " - https://appsto.re/gb/MV_5I.i" + " Code: " + smsHashCode;
			// smsMessageContent = URLEncoder.encode(smsMessageContent,
			// "UTF-8");
			smsMessageContent = URLEncoder.encode(smsMessageContent, "ISO-8859-1");
			// setup clickatell url to call
			String urlToCall = "http://api.clickatell.com/http/sendmsg?user=Bluepodmedia&"
					+ "password=VIgLTKVHCZXdAN&api_id=3538043&to=" + phoneNumber + "&text=" + smsMessageContent;

			// execute call
			HttpURLConnection urlConnection = null;
			BufferedReader in = null;
			String reqResponse = "";

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
			} finally {
				if (in != null) {
					in.close();
				}
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
			}

			int responseCode = urlConnection.getResponseCode();
			logger.info("SMS gateway response code: " + responseCode + " Response content: " + reqResponse
					+ " phone number: " + event.getPhoneNumber());

			// optimistically update event before call and only set it to faulty
			// one after (when we 100% know that transaction will not be
			// processed)
			// this is to avoid situation when we overwrite reward response with
			// reward request object data
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event, 2);

			String responseString = reqResponse;
			String statusMessage = responseString;
			String status = "";
			// TODO if we get error - notify AR about problem with reward ask
			// Rodgers if we only need 200 response to know that request was
			// successful)
			if (responseCode == 200) {
				status = "SUCCESS";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " credit successfully requested for internalT: " + event.getInternalTransactionId()
								+ " reward value: " + event.getRewardValue() + " offerPayout: " + event.getOfferPayout()
								+ " offer payout currency: " + event.getOfferPayoutIsoCurrencyCode() + " rewardType: "
								+ event.getRewardTypeName() +
								// " rewardUrl: "+urlToCall+
								" status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);

				// --------------------------------- Reward Response handling
				// starts ---------------------------
				// send correctly formatted response for supersonic (read manual
				// about callbacks:
				// http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: " + event.getTransactionId() + ":OK"
						+ " code: " + RespCodesEnum.OK_NO_CONTENT + "\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK, Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " sent successful response: " + successfulResponseStatus);

				// store successful reward response - normally this should be
				// interecepted in RewardTrackingService
				String dataContent = "intercepted reward notification: " + "internalTransactionId: "
						+ event.getInternalTransactionId() + " status: [" + responseCode + "]" + " statusMessage: ["
						+ responseString + "]";
				logger.info(dataContent);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " "
								+ Application.REWARD_RESPONSE_IDENTIFIED + " " + dataContent);

				// logger.info("REWARD_DATA_UPDATE updating user event with
				// reward notification data");
				// generate event object and persist it in db
				if (event.getRewardDate() != null) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
							LogStatus.WARNING,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
									+ " reward date already set for internal trans id: "
									+ event.getInternalTransactionId() + ": " + " internalT: "
									+ event.getInternalTransactionId() + event.getRewardDate().toString()
									+ " udpating with new time");
				}
				// update reward info in db
				if (status.toUpperCase().equals(RewardStatus.SUCCESS.toString())) {
					event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				} else if (status.toUpperCase().equals(RewardStatus.FAILED.toString())) {
					event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				} else if (status.toUpperCase().equals(RewardStatus.PENDING.toString())) {
					event.setRewardResponseStatus(RewardStatus.PENDING.toString());
				} else {
					event.setRewardResponseStatus(RewardStatus.UNKNOWN.toString());

					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
							LogStatus.ERROR,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " "
									+ Application.REWARD_RESPONSE_FAILED + " Unknown reward status code" + " status: "
									+ RespStatusEnum.FAILED + " code: "
									+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);

					// update reward date
					event.setRewardResponseStatusMessage(responseString);
					event.setRewardDate(new Timestamp(System.currentTimeMillis()));
					event.setApproved(true);
					daoUserEvent.createOrUpdate(event, 8);

					// update conversion history (needed to filter out already
					// clicked offers for particular user)
					updateUserConversionHistory(event);

					return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE + "\"}";
				}

				// update reward date
				event.setRewardResponseStatusMessage(responseString);
				event.setRewardDate(new Timestamp(System.currentTimeMillis()));
				event.setApproved(true);
				daoUserEvent.createOrUpdate(event, 8);

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
								+ " successfully rewarded event with internal transaction id: "
								+ event.getTransactionId() + " internalT: " + event.getInternalTransactionId()
								+ " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);

				// update conversion history to store information about
				// successful reward attempt
				updateUserConversionHistory(event);

				return successfulResponseStatus;
				// --------------------------------- Reward Response handling
				// ends---------------------------
			} else {
				status = "FAILED";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.ERROR,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " error during credit request: unknown response code: " + responseCode
								+ " for event: " + event.getUserId() + " rewardType: " + event.getRewardTypeName()
								+ " phone: " + event.getPhoneNumber() + " internalT: "
								+ event.getInternalTransactionId() + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);

				// update event
				event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
				event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
				event.setRewardRequestStatusMessage(
						"Unknown response code: " + responseCode + " was expecting 200 or 403");
				daoUserEvent.createOrUpdate(event, 4);

				// update conversion history to store information about failed
				// reward attempt
				updateUserConversionHistory(event);

				// --------------------------------- Reward Response handling
				// starts ---------------------------
				// send correctly formatted response for supersonic (read manual
				// about callbacks:
				// http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: " + event.getTransactionId() + ":OK"
						+ " code: " + RespCodesEnum.OK_NO_CONTENT + "\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(),
						LogStatus.OK, Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY
								+ " sent successful response: " + successfulResponseStatus);

				// store successful reward response - normally this should be
				// interecepted in RewardTrackingService
				String dataContent = "intercepted reward notification: " + "internalTransactionId: "
						+ event.getInternalTransactionId() + " status: [" + responseCode + "]" + " statusMessage: ["
						+ responseString + "]";
				logger.info(dataContent);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " "
								+ Application.REWARD_RESPONSE_IDENTIFIED + " " + dataContent);

				// logger.info("REWARD_DATA_UPDATE updating user event with
				// reward notification data");
				// generate event object and persist it in db
				if (event.getRewardDate() != null) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
							LogStatus.WARNING,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
									+ " reward date already set for internal trans id: "
									+ event.getInternalTransactionId() + ": " + " internalT: "
									+ event.getInternalTransactionId() + event.getRewardDate().toString()
									+ " udpating with new time");
				}
				// update reward info in db
				if (status.toUpperCase().equals(RewardStatus.SUCCESS.toString())) {
					event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				} else if (status.toUpperCase().equals(RewardStatus.FAILED.toString())) {
					event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				} else if (status.toUpperCase().equals(RewardStatus.PENDING.toString())) {
					event.setRewardResponseStatus(RewardStatus.PENDING.toString());
				} else {
					event.setRewardResponseStatus(RewardStatus.UNKNOWN.toString());

					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(),
							LogStatus.ERROR,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " "
									+ Application.REWARD_RESPONSE_FAILED + " Unknown reward status code" + " status: "
									+ RespStatusEnum.FAILED + " code: "
									+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);

					// update reward date
					event.setRewardResponseStatusMessage(responseString);
					event.setRewardDate(new Timestamp(System.currentTimeMillis()));
					event.setApproved(true);
					daoUserEvent.createOrUpdate(event, 8);

					// update conversion history (needed to filter out already
					// clicked offers for particular user)
					updateUserConversionHistory(event);

					return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE + "\"}";
				}

				// update reward date
				event.setRewardResponseStatusMessage(responseString);
				event.setRewardDate(new Timestamp(System.currentTimeMillis()));
				event.setApproved(true);
				daoUserEvent.createOrUpdate(event, 8);

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
								+ " successfully rewarded event with internal transaction id: "
								+ event.getTransactionId() + " internalT: " + event.getInternalTransactionId()
								+ " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);

				return successfulResponseStatus;
				// --------------------------------- Reward Response handling
				// ends---------------------------
			}
			// --------------------------- handle request to rewarding system
			// ---------------------------------

		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY + " internalT: "
							+ event.getInternalTransactionId() + " rewardType: " + event.getRewardTypeName()
							+ " error crediting user: " + exc.toString() + " status: " + RespStatusEnum.FAILED
							+ " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			// update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: " + exc.toString());
			daoUserEvent.createOrUpdate(event, 7);

			// update conversion history to store information about failed
			// reward attempt
			updateUserConversionHistory(event);

			return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: "
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

	public double[] calculateRewardPayoutBasedOnCarrier(String rewardType, double rewardValue) {
		double[] rewardPayout = new double[2];
		rewardPayout[0] = 0;
		rewardPayout[1] = 0;

		// we calculate payouts only from 10
		if (rewardValue >= 10) {
			double baseValue = Math.floor(rewardValue / 10);
			double restValue = rewardValue - baseValue * 10;
			rewardPayout[0] = baseValue * 10;
			rewardPayout[1] = restValue;
		}

		if (rewardType.equals("AirRewardz-SouthAfrica")) {
			if (rewardValue >= 5) {
				double baseValue = Math.floor(rewardValue / 5);
				double restValue = rewardValue - baseValue * 5;
				rewardPayout[0] = baseValue * 5;
				rewardPayout[1] = restValue;
			}
		}
		return rewardPayout;
	}

	/*
	 * // deduce from wallet // TODO use carrier settings to identify what
	 * reward value can be // sent to carrier! String carrierName =
	 * event.getCarrierName(); if (carrierName == null || carrierName.length()
	 * == 0) { Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " + " invalid carrier for: user: " +
	 * event.getEmail() + " " + event.getPhoneNumberExt() + " " +
	 * event.getPhoneNumber() + " application name: " +
	 * event.getApplicationName() + " rewardTypeName: " +
	 * event.getRewardTypeName() + " reward name: " + event.getRewardName() +
	 * " internalT: " + event.getInternalTransactionId()); }
	 * 
	 * WalletPayoutCarrierEntity carrierEntity = daoWalletPayoutCarrier
	 * .findByCarrierName(carrierName);
	 * 
	 * if (carrierEntity == null) {
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " + " invalid carrier " + carrierName +
	 * " for: user: " + event.getEmail() + " " + event.getPhoneNumberExt() + " "
	 * + event.getPhoneNumber() + " application name: " +
	 * event.getApplicationName() + " rewardTypeName: " +
	 * event.getRewardTypeName() + " reward name: " + event.getRewardName() +
	 * " internalT: " + event.getInternalTransactionId()); }
	 * 
	 * // critical part
	 * 
	 * // minimal value to payout double minValueToPayout =
	 * carrierEntity.getMinValueToPayout(); double payoutGap =
	 * carrierEntity.getPayoutGap(); // payout gap double walletBalance =
	 * wallet.getBalance(); // user balance double rewardValue =
	 * event.getRewardValue(); // reward value double totalAmountToPayout = 0;
	 * // payout value double rewardValueC = 0; // rest
	 * 
	 * // we check: // 1. Min value to payout is greater than 0 // 2. Payout gap
	 * is greater than 0 // 3. Wallet balance is greater than 0 // 4. Min value
	 * to payout is greater than wallet balance // 5. Wallet balance is greater
	 * than rewardValue
	 * 
	 * if (minValueToPayout <= 0 || payoutGap <= 0 || walletBalance <= 0 ||
	 * minValueToPayout > walletBalance || walletBalance < rewardValue) {
	 * 
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " +
	 * "insufficient wallet balance for reward or " +
	 * "invalid carrier configuration:" + " event id:" + event.getId() +
	 * " minValueToPayout:" + minValueToPayout + " walletBalance: " +
	 * walletBalance + " rewardValue: " + rewardValue);
	 * 
	 * } else {
	 * 
	 * // 6. RewardValue is greater than minimal value to payout // its splitted
	 * because of elastic log info if (rewardValue < minValueToPayout) {
	 * Application .getElasticSearchLogger() .indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " +
	 * "reward value is less than min value from carrier" + " event id:" +
	 * event.getId() + " minValueToPayout:" + minValueToPayout +
	 * " walletBalance: " + walletBalance + " rewardValue: " + rewardValue); }
	 * else {
	 * 
	 * // we know here that reward value is greater than min value // to payout
	 * 
	 * rewardValueC = rewardValue; totalAmountToPayout = minValueToPayout;
	 * rewardValueC = rewardValueC - minValueToPayout;
	 * 
	 * double multipler = Math.floor(rewardValueC / payoutGap);
	 * totalAmountToPayout = totalAmountToPayout + multipler payoutGap;
	 * rewardValueC = rewardValueC - multipler * payoutGap;
	 * 
	 * // now rewardValueC is rest } }
	 * 
	 * // lets check // if amount to payout + rest == reward value if
	 * ((totalAmountToPayout + rewardValueC) != rewardValue) {
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " + "total amount to payout is invalid" +
	 * " event id:" + event.getId() + " minValueToPayout:" + minValueToPayout +
	 * " walletBalance: " + walletBalance + " rewardValue: " + rewardValue);
	 * 
	 * // there is problem with payout so we set totalAmountToPayout to // 0
	 * totalAmountToPayout = 0; }
	 * 
	 * if (rewardValueC > payoutGap) {
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, realm.getId(), LogStatus.ERROR,
	 * Application.WALLET_TRANSACTION_ACTIVITY + " " +
	 * Application.WALLET_PAY_OUT + " " + "rest is greater than payoutgap" +
	 * " event id:" + event.getId() + " minValueToPayout:" + minValueToPayout +
	 * " walletBalance: " + walletBalance + " rewardValue: " + rewardValue +
	 * " payoutGap: " + payoutGap + " rest: " + rewardValueC);
	 * 
	 * // there is problem with payout so we set totalAmountToPayout to // 0
	 * totalAmountToPayout = 0; } wallet.setBalance(totalAmountToPayout);
	 */

	/*
	 * // ---------------------- handle request to rewarding system
	 * ----------------------- public String requestRewardMode(UserEventEntity
	 * event, InvitationEntity invitation, boolean
	 * isEventFromUserThatWasInviting) { try { RealmEntity realm =
	 * daoRealm.findById(event.getRealmId());
	 * 
	 * // =================================== testing mode
	 * ============================================== // if testing is enabled -
	 * send notification and abort real mode if
	 * (testManager.isTestModeEnabledForRewardType(realm, event)) {
	 * requestTestRewardMode(event, invitation, isEventFromUserThatWasInviting);
	 * return ""; }
	 * 
	 * // ======================= handle request to mode
	 * ========================= String carrierName = "Unknown"; try {
	 * UserEventEntity recentClickEvent =
	 * daoUserEvent.findMostRecentOfferClickEvent(event.getUserId(), "INSTALL");
	 * if (recentClickEvent != null) { carrierName =
	 * recentClickEvent.getCarrierName(); } } catch (Exception exc) {
	 * logger.severe (
	 * "error when retrieving most recent install event from user with id: " +
	 * event.getUserId() + " error: " + exc.toString()); exc.printStackTrace();
	 * }
	 * 
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " " + Application.REWARD_REQUEST_IDENTIFIED + " internalT: " +
	 * event.getInternalTransactionId() + " issuing credit request for payout: "
	 * + event.getOfferPayout() + " (in target currency: " +
	 * event.getRevenueValue() + " phone: " + event.getPhoneNumber() +
	 * " rewardType: " + event.getRewardTypeName() + " reward: " +
	 * event.getRewardValue() + " reward currency: " +
	 * event.getRewardIsoCurrencyCode() + " carrierName: " + carrierName);
	 * 
	 * // logger.info("credit request for user: "+event.getUserId()+ //
	 * " internalTransaction: "+event.getInternalTransactionId()+ //
	 * " currency: " +event.getOfferPayoutIsoCurrencyCode()+" payout: "
	 * +event.getOfferPayout ());
	 * 
	 * // extract mode configuration String bpUser = realm.getModeBPUser();
	 * String bpPass = realm.getModeBPPassword(); String url =
	 * realm.getModeCreditUrl();
	 * 
	 * URL obj = new URL(url); HttpURLConnection con = (HttpURLConnection)
	 * obj.openConnection(); con.setConnectTimeout(realm.getConnectionTimeout()
	 * * 1000); con.setReadTimeout(realm.getReadTimeout() * 1000); // add
	 * reuqest header con.setRequestMethod("POST"); String urlParameters =
	 * "MSISDN=" + event.getPhoneNumberExt() + event.getPhoneNumber() +
	 * "&OriginTransactionID=" + event.getId() + //
	 * "&OriginTransactionID="+event.getInternalTransactionId()+ "&Reward=" +
	 * event.getRewardValue() + "&ISOCurrCode=" +
	 * event.getRewardIsoCurrencyCode() + "&User=" + bpUser + "&Password=" +
	 * bpPass + "&Operator=" + carrierName;
	 * 
	 * // Send post request con.setDoOutput(true); DataOutputStream wr = new
	 * DataOutputStream(con.getOutputStream()); wr.writeBytes(urlParameters);
	 * wr.flush(); wr.close(); int responseCode = con.getResponseCode();
	 * 
	 * // System.out.println("\nSending 'POST' request to URL : " + url); //
	 * System.out.println("Post parameters : " + urlParameters); //
	 * System.out.println("Response Code : " + responseCode);
	 * 
	 * // optimistically update event before call and only set it to faulty //
	 * one after (when we 100% know that transaction will not be // processed)
	 * // this is to avoid situation when we overwrite reward response with //
	 * reward request object data event.setRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
	 * event.setRewardRequestStatusMessage("OK");
	 * daoUserEvent.createOrUpdate(event, 2);
	 * 
	 * if (invitation != null) { // if we have invitation object included //
	 * (referral programme) update status as // well
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.INVITATION_ACTIVITY, event.getRealmId(), LogStatus.OK,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " " + Application.REWARD_REQUEST_IDENTIFIED + " internalT: " +
	 * event.getInternalTransactionId() + " issuing credit request for payout: "
	 * + event.getOfferPayout() + " (in target currency: " +
	 * event.getRevenueValue() + " phone: " + event.getPhoneNumber() +
	 * " rewardType: " + event.getRewardTypeName() + " reward: " +
	 * event.getRewardValue() + " reward currency: " +
	 * event.getRewardIsoCurrencyCode());// +" urlParameters: "+urlParameters);
	 * 
	 * if (isEventFromUserThatWasInviting) {
	 * invitation.setInvitingRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitingRewardRequestStatus
	 * (RespStatusEnum.SUCCESS.toString());
	 * invitation.setInvitingRewardRequestStatusMessage("OK"); } else {
	 * invitation.setInvitedRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitedRewardRequestStatus
	 * (RespStatusEnum.SUCCESS.toString());
	 * invitation.setInvitedRewardRequestStatusMessage("OK"); }
	 * daoInvitation.createOrUpdate(invitation); }
	 * 
	 * String responseString = "OK"; String statusMessage = responseString;
	 * 
	 * if (responseCode == 200) { BufferedReader in = new BufferedReader(new
	 * InputStreamReader(con.getInputStream())); String inputLine; StringBuffer
	 * response = new StringBuffer();
	 * 
	 * while ((inputLine = in.readLine()) != null) { response.append(inputLine);
	 * } in.close();
	 * 
	 * responseString = response.toString(); // code 200 and status 0 - request
	 * to credit user received // successfully // code 200 and status 1 -
	 * request with similar transaction id // already exists // code 403 -
	 * authentication failure String STATUS_FAILED = "\"Status\":1"; // request
	 * with similar // transaction id // already exists statusMessage =
	 * "Unable to parse"; try { statusMessage =
	 * responseString.substring(responseString.indexOf("Msg\":") + 6,
	 * responseString.indexOf(",\"OriginTransactionID") - 1); } catch (Exception
	 * exc) { }
	 * 
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " credit successfully requested for internalT: " +
	 * event.getInternalTransactionId() + " reward value: " +
	 * event.getRewardValue() + " rewardType: " + event.getRewardTypeName() +
	 * " offerPayout: " + event.getOfferPayout() + " offer payout currency: " +
	 * event.getOfferPayoutIsoCurrencyCode() // + " rewardUrl: "+url+ //
	 * " urlParams: " + urlParameters + " status: " + RespStatusEnum.SUCCESS +
	 * " code: " + RespCodesEnum.OK_NO_CONTENT);
	 * 
	 * // send correctly formatted response for supersonic (read manual // about
	 * callbacks: //
	 * http://documents.supersonicads.com/SupersonicAds%20-%20Publisher
	 * %20Integration.pdf) String successfulResponseStatus =
	 * "{\"response\":\" status: " + event.getTransactionId() + ":OK" +
	 * " code: " + RespCodesEnum.OK_NO_CONTENT + "\"}";
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " sent successful response: " + successfulResponseStatus);
	 * 
	 * return successfulResponseStatus; } else { if (responseCode == 403) {
	 * Application .getElasticSearchLogger() .indexLog(
	 * Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.ERROR,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " error during credit request: authentication failure (please make sure that login/pass to mode are correctly set) for event: "
	 * + event.getUserId() + " phone: " + event.getPhoneNumber() +
	 * " internalT: " + event.getInternalTransactionId() + " status: " +
	 * RespStatusEnum.FAILED + " code: " +
	 * RespCodesEnum.ERROR_AUTHENTICATION_FAILURE); // update event
	 * event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
	 * event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
	 * event.setRewardRequestStatusMessage(
	 * "Authentication failure - please make sure that login/pass to mode are correctly set"
	 * ); daoUserEvent.createOrUpdate(event, 3);
	 * 
	 * if (invitation != null) { // if we have invitation object // included
	 * (referral programme) // update status as well Application
	 * .getElasticSearchLogger() .indexLog( Application.INVITATION_ACTIVITY,
	 * event.getRealmId(), LogStatus.ERROR, Application.REWARD_ACTIVITY + " " +
	 * Application.REWARD_REQUEST_ACTIVITY +
	 * " error during credit request: authentication failure (please make sure that login/pass to mode are correctly set) for event: "
	 * + event.getUserId() + " phone: " + event.getPhoneNumber() +
	 * " internalT: " + event.getInternalTransactionId() + " status: " +
	 * RespStatusEnum.FAILED + " code: " +
	 * RespCodesEnum.ERROR_AUTHENTICATION_FAILURE);
	 * 
	 * if (isEventFromUserThatWasInviting) {
	 * invitation.setInvitingRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitingRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString()); invitation
	 * .setInvitingRewardRequestStatusMessage(
	 * "Authentication failure - please make sure that login/pass to mode are correctly set"
	 * ); } else { invitation.setInvitedRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitedRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString()); invitation
	 * .setInvitedRewardRequestStatusMessage(
	 * "Authentication failure - please make sure that login/pass to mode are correctly set"
	 * ); } daoInvitation.createOrUpdate(invitation); }
	 * 
	 * // update conversion history to store information about // failed reward
	 * attempt updateUserConversionHistory(event);
	 * 
	 * return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " +
	 * event.getTransactionId() + ":OK" + " code: " +
	 * RespCodesEnum.ERROR_AUTHENTICATION_FAILURE + "\"}"; } else {
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.ERROR,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " error during credit request: unknown response code: " + responseCode +
	 * " for event: " + event.getUserId() + " rewardType: " +
	 * event.getRewardTypeName() + " phone: " + event.getPhoneNumber() +
	 * " internalT: " + event.getInternalTransactionId() + " status: " +
	 * RespStatusEnum.FAILED + " code: " +
	 * RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);
	 * 
	 * // update event event.setRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
	 * event.setRewardRequestStatusMessage("Unknown response code: " +
	 * responseCode + " was expecting 200 or 403");
	 * daoUserEvent.createOrUpdate(event, 4);
	 * 
	 * if (invitation != null) { // if we have invitation object // included
	 * (referral programme) // update status as well if
	 * (isEventFromUserThatWasInviting) {
	 * invitation.setInvitingRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitingRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString());
	 * invitation.setInvitingRewardRequestStatusMessage (
	 * "Unknown response code: " + responseCode + " was expecting 200 or 403");
	 * } else { invitation.setInvitedRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitedRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString());
	 * invitation.setInvitedRewardRequestStatusMessage("Unknown response code: "
	 * + responseCode + " was expecting 200 or 403"); }
	 * daoInvitation.createOrUpdate(invitation); }
	 * 
	 * // update conversion history to store information about // failed reward
	 * attempt updateUserConversionHistory(event);
	 * 
	 * return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " +
	 * event.getTransactionId() + ":OK" + " code: " +
	 * RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE + "\"}"; } } } catch (Exception
	 * exc) { exc.printStackTrace(); logger.severe(exc.toString());
	 * Application.getElasticSearchLogger().indexLog(
	 * Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.REWARD_ACTIVITY + " " + Application.REWARD_REQUEST_ACTIVITY +
	 * " internalT: " + event.getInternalTransactionId() +
	 * " error crediting user: " + exc.toString() + " status: " +
	 * RespStatusEnum.FAILED + " code: " +
	 * RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR); // update event
	 * event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
	 * event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
	 * event.setRewardRequestStatusMessage("Internal server error: " +
	 * exc.toString()); daoUserEvent.createOrUpdate(event, 7);
	 * 
	 * if (invitation != null) { // if we have invitation object included //
	 * (referral programme) update status as // well if
	 * (isEventFromUserThatWasInviting) {
	 * invitation.setInvitingRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitingRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString());
	 * invitation.setInvitingRewardRequestStatusMessage (
	 * "Internal server error: " + exc.toString()); } else {
	 * invitation.setInvitedRewardRequestDate(new
	 * Timestamp(System.currentTimeMillis()));
	 * invitation.setInvitedRewardRequestStatus
	 * (RespStatusEnum.FAILED.toString());
	 * invitation.setInvitedRewardRequestStatusMessage("Internal server error: "
	 * + exc.toString()); } daoInvitation.createOrUpdate(invitation); }
	 * 
	 * // update conversion history to store information about failed // reward
	 * attempt updateUserConversionHistory(event);
	 * 
	 * return "{\"response\":\" status: " + RespStatusEnum.FAILED + " " +
	 * event.getTransactionId() + ":OK" + " code: " +
	 * RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}"; } }
	 */
}
