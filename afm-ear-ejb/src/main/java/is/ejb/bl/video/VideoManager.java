package is.ejb.bl.video;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.external.ExternalServerManager;
import is.ejb.bl.external.ExternalServerType;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.ExternalServerAddressEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

@Startup
@Singleton
public class VideoManager {

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private ExternalServerManager externalServerManager;

	@Inject
	private DAODenominationModel daoDenominationModel;
	
	private List<VideoCallbackData> dataToProcessList = Collections
			.synchronizedList(new ArrayList<VideoCallbackData>());

	public synchronized void addData(VideoCallbackData data) {
		synchronized (dataToProcessList) {
			logger.info("Added data: " + data);
			dataToProcessList.add(data);
			logger.info("There is " + dataToProcessList.size()
					+ " after inserting");
		}
	}

	public synchronized List<VideoCallbackData> getData() {
		List<VideoCallbackData> dataProcessList;
		synchronized (dataToProcessList) {
			logger.info("There is " + dataToProcessList.size()
					+ " elements to process");
			dataProcessList = new ArrayList<VideoCallbackData>(
					dataToProcessList);
			Iterator<VideoCallbackData> iter = dataToProcessList.iterator();
			while (iter.hasNext()) {
				VideoCallbackData data = iter.next();
				logger.info("Removing data: " + data);
				iter.remove();
			}
		}
		logger.info("Returning elements to process:" + dataProcessList.size());
		return dataProcessList;

	}

	public void issueReward(VideoCallbackData data) {
		try {
			Application.getElasticSearchLogger().indexLog(
					Application.VIDEO_REWARD_ACTIVITY,
					-1,
					LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY
							+ "Issuing reward for video: " + data.toString());

			boolean validation = validateRequest(data);
			if (!validation) {
				Application.getElasticSearchLogger().indexLog(
						Application.VIDEO_REWARD_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.VIDEO_REWARD_ACTIVITY
								+ "Validation failed for: " + data.toString());
				return;
			}
			AppUserEntity appUser = daoAppUser.findById(Integer.parseInt(data
					.getUserId()));
			logger.info("appUser: " + appUser);
			logger.info("username : " + appUser.getUsername());
			logger.info("email: " + appUser.getEmail());
			logger.info("realmid: " + appUser.getRealmId());
			UserEventEntity event = createVideoEvent(appUser, data);

			RealmEntity realm = daoRealm.findById(appUser.getRealmId());
			daoUserEvent.create(event);
			Application.getElasticSearchLogger().indexLog(
					Application.VIDEO_REWARD_ACTIVITY,
					-1,
					LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY
							+ "Issuing reward for video: " + data.toString()
							+ " created event: " + event);
			logEvent(event, realm);
			rewardManager.issueReward(realm, event, null, false);

		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(
					Application.VIDEO_REWARD_ACTIVITY,
					-1,
					LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY
							+ "Error occured while issuing reward for: "
							+ data.toString() + " error:" + exc.toString());
			exc.printStackTrace();
		}
	}

	private boolean validateRequest(VideoCallbackData data) {
		try {
			AppUserEntity appUser = daoAppUser.findById(Integer.parseInt(data
					.getUserId()));
			if (appUser != null
					&& appUser.getUsername().equals(data.getUsername())) {
				if (daoUserEvent.findByInternalTransactionIdSafe(data
						.getTransactionId()) == null) {
					logger.info("Transaction id unique");
					return true;
				} else {
					logger.info("Transaction id not unique");
					return false;
				}

			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	private String generateInternalTransactionId(VideoCallbackData data) {
		String internalTransactionId = "";
		
			internalTransactionId = DigestUtils.sha1Hex(data.getCurrencyName() + Math.random() * 100000 + System.currentTimeMillis()
					+ data.getTransactionId() + data.getUid());
			if (internalTransactionId.length() > 32) {
				internalTransactionId = internalTransactionId.substring(0, 31);
			}

		return internalTransactionId;
	}
	
	private RewardTypeEntity getRewardType(String rewardTypeName){
		RewardTypeEntity rewardType = null;
		try{
			rewardType = daoRewardType.findByName(rewardTypeName);
		}catch (Exception exc){
			exc.printStackTrace();
		}
		return rewardType;
	}

	private UserEventEntity createVideoEvent(AppUserEntity user,
			VideoCallbackData data) {
		
		DenominationModelEntity model = getDenominationModel(user.getRewardTypeName());
		UserEventEntity event = new UserEventEntity();
		event.setIosDeviceToken(user.getiOSDeviceToken());
		event.setAndroidDeviceToken(user.getAndroidDeviceToken());
		event.setUserId(user.getId());
		System.out.println(user.getDeviceType());
		event.setAdProviderCodeName("FYBER");
		event.setDeviceType(user.getDeviceType());
		event.setTransactionId("FYBER:" + data.getTransactionId());
		event.setInternalTransactionId(generateInternalTransactionId(data));
		event.setPhoneNumber(user.getPhoneNumber());
		event.setPhoneNumberExt(user.getPhoneNumberExtension());
		event.setRewardTypeName(user.getRewardTypeName()); // needed
		event.setRealmId(user.getRealmId());
		event.setOfferTitle("VIDEO OFFER");
		double profitSplitFraction = model.getCommisionPercentage() / 100;
		double reward = model.getVideoPayout() * model.getVideoPointsMultipler() * (1-profitSplitFraction);
		double profit = model.getVideoPayout() * model.getVideoPointsMultipler() * profitSplitFraction;
		double revenue = model.getVideoPayout() * model.getVideoCommisonPercentage() - reward;
		double payoutInTargetCC = model.getVideoPayout() * model.getVideoCommisonPercentage();
		event.setOfferPayout(model.getVideoPayout());
		event.setOfferPayoutIsoCurrencyCode(model.getVideoSourcePayoutCurrencyCode());
		event.setOfferPayoutInTargetCurrency(payoutInTargetCC);
		event.setRewardIsoCurrencyCode(model.getTargetPayoutCurrencyCode());
		event.setRewardValue(reward);
		event.setRevenueValue(revenue);
		event.setProfitValue(profit);
		event.setProfilSplitFraction(profitSplitFraction);
		event.setClickDate(new Timestamp(System.currentTimeMillis()));
		event.setConversionDate(new Timestamp(System.currentTimeMillis()));
		event.setCountryCode(user.getCountryCode());
		event.setUserEventCategory(UserEventCategory.VIDEO.toString());
		event.setEmail(user.getEmail());
		event.setInstant(false);

		System.out.println("Event: " + event.toString());
		return event;
	}
	
	private DenominationModelEntity getDenominationModel(String rewardType){
		DenominationModelEntity model = null;
		try {
			 List<DenominationModelEntity> denomModel = daoDenominationModel.findByRewardTypeNameAndRealmId(rewardType, 4);
			 model = denomModel.get(0);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		return model;
	}

	private void logEvent(UserEventEntity event, RealmEntity realm) {
		// add conversion event to conversion index in es (imitate click so that
		// conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(),
				event.getPhoneNumber(), "", event.getDeviceType(),
				event.getOfferId(), event.getOfferSourceId(),
				event.getOfferTitle(), event.getAdProviderCodeName(),
				event.getRewardTypeName(),
				event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
				event.getRewardIsoCurrencyCode(), event.getProfitValue(),
				realm.getName(), "", UserEventType.click.toString(),
				event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", "",
				event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

		// add conversion event to conversion index in es (imitate conversion so
		// that conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(),
				event.getPhoneNumber(), "", event.getDeviceType(),
				event.getOfferId(), event.getOfferSourceId(),
				event.getOfferTitle(), event.getAdProviderCodeName(),
				event.getRewardTypeName(),
				event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
				event.getRewardIsoCurrencyCode(), event.getProfitValue(),
				realm.getName(), "", UserEventType.conversion.toString(),
				event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", "",
				event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

	}



	public void processData() {
		logger.info("***** PROCESSING VIDEO REWARDS *****");
		List<VideoCallbackData> data = this.getData();
		logger.info("Processing :" + data.size() + " elements");
		for (VideoCallbackData callbackData : data) {
			logger.info("Processing data: " + data);
			this.issueReward(callbackData);
		}

	}

}
