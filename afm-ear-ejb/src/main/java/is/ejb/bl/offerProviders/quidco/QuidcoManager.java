package is.ejb.bl.offerProviders.quidco;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.bluepodmedia.sdk.quidco.offer.dto.ActivityState;

@Stateless
public class QuidcoManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private MailManager mailManager;

	public void processQuidcoEvent(ActivityState quidcoEvent) {
		try {
			logger.info("Processing quidco event: " + quidcoEvent);
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
					"Processing quidco event: " + quidcoEvent.toString());

			UserEventEntity event = daoUserEvent
					.findByInternalTransactionId("QUIDCO_" + quidcoEvent.getTransactionId());
			if (event != null) {
				updateEvent(quidcoEvent, event);
			} else {
				createEvent(quidcoEvent);
			}

		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					"Processing quidco event: " + quidcoEvent.toString() + " exception: " + exception.toString());
			exception.printStackTrace();
		}
	}

	private void updateEvent(ActivityState quidcoEvent, UserEventEntity event) {
		logger.info("Updating event with transactionId: " + event.getInternalTransactionId());
		Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
				"Updating event with id: " + event.getInternalTransactionId());

		if (event.getRewardRequestStatus().toLowerCase().equals(QuidcoEventStatus.success.toString())
				|| quidcoEvent.getStatus().equals(event.getRewardRequestStatus())) {
			logger.info("Event with transactionId " + event.getInternalTransactionId()
					+ " didn't change or is success. Skipping.");
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
					"Event with transactionId " + event.getInternalTransactionId()
							+ " didn't change or is success. Skipping.");

		} else {
			logger.info("Event with transactionId " + event.getInternalTransactionId() + "changed. Processing..");
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
					"Event with transactionId " + event.getInternalTransactionId() + "changed. Processing..");
			UserEventEntity processedEvent = processUpdate(quidcoEvent, event);
			indexQuidcoEvent(processedEvent);
		}
	}

	private void indexQuidcoEvent(UserEventEntity event) {
		try{
			RealmEntity realm = daoRealm.findById(event.getRealmId());
			
			String eventStatus = "";
			if (event.getRewardRequestStatus()!= null){
				if (event.getRewardRequestStatus().equals("paid")){
					eventStatus = "QUIDCO_PAID";
				}
				if (event.getRewardRequestStatus().equals("tracked")){
					eventStatus = "QUIDCO_TRACKED";
				}
				if (event.getRewardRequestStatus().equals("declined")){
					eventStatus = "QUIDCO_DECLINED";
				}
				
			}
			
			Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "",
					event.getDeviceType(), event.getOfferId(), event.getRewardRequestStatus(),
					event.getOfferTitle(), event.getAdProviderCodeName(), event.getRewardTypeName(),
					event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
					event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
					UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
					eventStatus, "", "", "", event.getCountryCode(),
					event.isInstant(), event.getApplicationName(), event.getAdvertisingId(), // gaid
					event.getIdfa(), // idfa
					event.isTestMode(), event.getCustomRewardValue(), event.getCustomRewardCurrencyCode());
		}
		catch (Exception exception){
			exception.printStackTrace();
		}
	}

	private UserEventEntity processUpdate(ActivityState quidcoEvent, UserEventEntity event) {
		try {
			logger.info("Processing update for event with transactionId: " + event.getInternalTransactionId());

			String quidcoEventStatus = quidcoEvent.getStatus();
			if (quidcoEventStatus.toLowerCase().equals(QuidcoEventStatus.tracked.toString())) {
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
						"Processing update event with transactionId " + event.getInternalTransactionId()
								+ ". New status is tracked.");
				event.setApproved(false);
				event.setRewardRequestStatus("tracked");
				event.setRewardRequestStatusMessage("");
				event.setRewardResponseStatus("");
				event.setRewardRequestStatusMessage("");
				daoUserEvent.createOrUpdate(event, 0);
				updateUserConversionHistory(event, "TRACKED", null);

			}
			if (quidcoEventStatus.toLowerCase().equals(QuidcoEventStatus.paid.toString())) {
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
						"Processing update event with transactionId " + event.getInternalTransactionId()
								+ ". New status is paid.");
				event.setApproved(true);
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
				DateFormat formatWithoutT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
				Timestamp conversionTimestamp = null;
				if (quidcoEvent.getUpdated().contains("T")){
					conversionTimestamp = new Timestamp(format.parse(quidcoEvent.getUpdated()).getTime());
				}else{
					conversionTimestamp = new Timestamp(formatWithoutT.parse(quidcoEvent.getUpdated()).getTime());
				}
				event.setConversionDate(conversionTimestamp);
				event.setRewardRequestStatus("paid");
				event.setRewardRequestStatusMessage("SUCCESS");
				event.setRewardResponseStatus("SUCCESS");
				event.setRewardRequestStatusMessage("SUCCESS");
				daoUserEvent.createOrUpdate(event, 0);
				RealmEntity realm = getRealm();
				logger.info("Issue reward in reward manager...");
				rewardManager.issueReward(realm, event, null, false);
				updateUserConversionHistory(event, "SUCCESS", conversionTimestamp);
				AppUserEntity appUser = daoAppUser.findById(event.getUserId());
				sendQuidcoRewardAvailableToSpendEmail(event, appUser);

			} else if (quidcoEventStatus.toLowerCase().equals(QuidcoEventStatus.declined.toString())) {
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
						"Processing update event with transactionId " + event.getInternalTransactionId()
								+ ". New status is declined.");
				event.setApproved(false);
				event.setRewardRequestStatus("declined");
				event.setRewardRequestStatusMessage("FAILED");
				event.setRewardResponseStatus("FAILED");
				event.setRewardRequestStatusMessage("FAILED");
				daoUserEvent.createOrUpdate(event, 0);
				updateUserConversionHistory(event, "FAILED", null);
			}

		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					"Processing update event with transactionId " + event.getInternalTransactionId() + " failed. "
							+ exception.toString());

			exception.printStackTrace();
		}
		
		return event;

	}

	private void createEvent(ActivityState quidcoEvent) {
		try {
			logger.info("Creating event from  quidco event: " + quidcoEvent);
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
					"Creating event from quidcoEvent: " + quidcoEvent.toString());

			UserEventEntity event = new UserEventEntity();
			event.setAdProviderCodeName("Quidco");
			event.setApplicationName("GoAhead");
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
			if (quidcoEvent.getDate() != null) {
				event.setClickDate(new Timestamp(format.parse(quidcoEvent.getDate()).getTime()));
			} else {
				event.setClickDate(new Timestamp(new Date().getTime()));
			}
			event.setInternalTransactionId("QUIDCO_" + quidcoEvent.getTransactionId());
			event.setOfferTitle(quidcoEvent.getMerchantName());
			event.setRewardRequestStatus(quidcoEvent.getStatus());
			event.setOfferPayout(quidcoEvent.getUserCommission());
			
			event.setRealmId(4);
			event.setOfferId(quidcoEvent.getMerchantName() + " offer");
			event.setUserEventCategory(UserEventCategory.QUIDCO.toString());
			event.setTransactionId("QUIDCO_" + quidcoEvent.getTransactionId());
			event.setOfferPayoutInTargetCurrency(quidcoEvent.getUserCommission());
			event.setDeviceType("Android");
			event.setDeviceId("");
			event.setCountryCode("GBR");
			event.setAdvertisingId("");
			event.setEmail("");
			event.setOfferSourceId(quidcoEvent.getMerchantName() + " offer");
			event.setRewardTypeName("Trippa-GB");
			event.setOfferTitle(quidcoEvent.getMerchantName() + " offer");
			event.setAndroidDeviceToken("");
			event.setCarrierName("");

			double[] rewardsCalculatedBasedOnCommision = calculateQuidcoRewardsBasedOnCommisionPercentage(
					quidcoEvent.getUserCommission());

			event.setCustomRewardValue(this.denominateRewardValue(rewardsCalculatedBasedOnCommision[1]));
			event.setCustomRewardCurrencyCode("Trippa Points");
			event.setRewardValue(rewardsCalculatedBasedOnCommision[1]);
			event.setProfitValue(rewardsCalculatedBasedOnCommision[0]);
			event.setProfilSplitFraction(quidcoEvent.getAmount());
			int userId = quidcoEvent.getUserId();

			AppUserEntity appUser = getAppUserWithQuidcoUserId(userId);
			if (appUser != null) {
				event.setUserId(appUser.getId());
				event.setPhoneNumber(appUser.getPhoneNumber());
				event.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
				event.setDeviceId(appUser.getDeviceId());
				event.setCountryCode(appUser.getCountryCode());
				event.setEmail(appUser.getEmail());

			}
			if (event.getRewardRequestStatus() == null || event.getRewardRequestStatus().length() == 0) {
				event.setRewardRequestStatus(QuidcoEventStatus.tracked.toString());
			}

			event = daoUserEvent.createOrUpdate(event, 0);
			logger.info("Creating event: " + event);
			createUserConversionHistory(event);

			sendQuidcoRewardCashbackTrackedEmail(event, appUser);

			if (event.getRewardRequestStatus().equals(QuidcoEventStatus.paid.toString())
					|| event.getRewardRequestStatus().equals(QuidcoEventStatus.declined.toString())) {
				this.processUpdate(quidcoEvent, event);
			}

		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					"Creating event from quidcoEvent: " + quidcoEvent.toString() + " failed: " + exception.toString());
			exception.printStackTrace();
		}
	}

	private RealmEntity getRealm() {
		try {
			return daoRealm.findById(4);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private double[] calculateQuidcoRewardsBasedOnCommisionPercentage(double rewardValue) {

		Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
				"Calculating quidco rewards based on commision percentage for rewardValue : " + rewardValue);
		logger.info("Calculating quidco rewards based on commision percentage for rewardValue : " + rewardValue);
		RealmEntity realm = getRealm();
		if (realm != null) {
			double commision = realm.getQuidcoPercentageCommision();
			double[] result = new double[2];
			result[0] = rewardValue * commision;
			result[1] = rewardValue - result[0];
			logger.info("Calculation result:" + result[0] + " , " + result[1]);
			return result;
		} else {
			return new double[2];
		}
	}

	private AppUserEntity getAppUserWithQuidcoUserId(int quidcoUserId) {
		try {
			return daoAppUser.findByQuidcoUserId(quidcoUserId);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public double calculatePotentialQuidcoRewardsForUser(int userId) {
		logger.info("Calculating potential quidco rewards for userId:" + userId);
		double potentialQuidcoReward = 0;
		List<UserEventEntity> quidcoEventList = daoUserEvent.findQuidcoEventsByUserId(userId);
		if (quidcoEventList != null) {
			for (UserEventEntity userEvent : quidcoEventList) {
				if (!(userEvent.getRewardRequestStatus().toLowerCase().equals(QuidcoEventStatus.declined.toString())
						|| userEvent.getRewardRequestStatus().toLowerCase().equals(QuidcoEventStatus.paid.toString())
						|| userEvent.getRewardRequestStatus().toLowerCase()
								.equals(QuidcoEventStatus.success.toString()))) {
					potentialQuidcoReward += userEvent.getCustomRewardValue();
				}
			}
		}
		logger.info("Calculated potential reward for quidco: " + potentialQuidcoReward);
		return potentialQuidcoReward;
	}

	private void createUserConversionHistory(UserEventEntity event) {
		try {

			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.QUIDCO_MANAGER_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE + " "
							+ " adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId());
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
			if (event.getApplicationName().toLowerCase().contains("goahead")
					|| event.getApplicationName().toLowerCase().contains("cine")) {
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
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.QUIDCO_MANAGER_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE + " "
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE
							+ " error adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
		}
	}

	private void updateUserConversionHistory(UserEventEntity event, String resultType, Timestamp conversionTimestamp) {
		try {

			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
			List<ConversionHistoryEntry> historyList = conversionHistoryHolder.getListConversionHistoryEntries();
			ConversionHistoryEntry eventHistory = null;
			for (ConversionHistoryEntry history : historyList) {
				if (history.getInternalTransactionId().equals(event.getInternalTransactionId())) {
					eventHistory = history;
					break;
				}
			}

			if (eventHistory != null) {
				logger.info("Updating history event with id: " + eventHistory.getOfferId());
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
						"Updating history event with id: " + eventHistory.getOfferId());
				eventHistory.setRewardStatus(resultType);
				eventHistory.setConversionTimestamp(conversionTimestamp);
			} else {
				logger.info("Upadting history event failed - event history is null");
				Application.getElasticSearchLogger().indexLog(Application.QUIDCO_MANAGER_ACTIVITY, 4, LogStatus.OK,
						"Upadting history event failed - event history is null");
			}

			daoConversionHistory.createOrUpdate(conversionHistory);

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	// denomination for quidco is 1 GBP = 100 Trippa Points
	private double denominateRewardValue(double rewardValue) {
		return rewardValue * 100;
	}

	private RealmEntity getRealmWithId(int id) {
		try {
			return daoRealm.findById(id);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public void sendQuidcoCreditCardRegistrationEmail(AppUserEntity appUser) {
		try {
			if (appUser == null) {
				logger.info("Couldn't send email because appUser is null");
			}
			logger.info("Sending credit card registration email to user: " + appUser.getId());
			RealmEntity realm = getRealmWithId(appUser.getRealmId());
			if (realm == null) {
				realm = getRealm();
			}
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.TRIPPA_QUIDCO_CREDIT_CARD_REGISTRATION);
			logger.info("Email with credit card registration has been sent.");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void sendQuidcoRewardCashbackTrackedEmail(UserEventEntity event, AppUserEntity appUser) {
		try {
			if (appUser == null) {
				logger.info("Couldn't send email because appUser is null");
				return;
			}
			logger.info("Sending quidco reward cashback tracked email to user: " + appUser.getId());
			RealmEntity realm = getRealmWithId(appUser.getRealmId());
			if (realm == null) {
				realm = getRealm();
			}
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailParamsHolder.setRetailer(event.getOfferTitle());
			mailParamsHolder.setPurchaseAmount("" + event.getProfilSplitFraction());
			mailParamsHolder.setCashbackAmount("" + event.getCustomRewardValue());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.TRIPPA_QUIDCO_REWARD_CASHBACK_TRACKED);
			logger.info("Email with quidco reward cashback tracked has been sent.");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void sendQuidcoRewardAvailableToSpendEmail(UserEventEntity event, AppUserEntity appUser) {
		try {
			if (appUser == null) {
				logger.info("Couldn't send email because appUser is null");
				return;
			}
			logger.info("Sending quidco reward available to spend email to user: " + appUser.getId());
			RealmEntity realm = getRealmWithId(appUser.getRealmId());
			if (realm == null) {
				realm = getRealm();
			}
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailParamsHolder.setRetailer(event.getOfferTitle());
			mailParamsHolder.setPurchaseAmount("" + event.getProfilSplitFraction());
			mailParamsHolder.setCashbackAmount("" + event.getCustomRewardValue());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.TRIPPA_QUIDCO_REWARD_AVAIBLE_TO_SPEND);
			logger.info("Email with quidco reward available to spend email has been sent.");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

}
