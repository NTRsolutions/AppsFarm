package is.ejb.bl.offerWall.external;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;


@Stateless
public class ExternalOfferWallManager {

	@Inject
	private Logger logger;
	@Inject
	private DAOAppUser daoAppUser;
	@Inject
	private DAORealm daoRealm;
	@Inject
	private DAOUserEvent daoUserEvent;
	@Inject
	private DAODenominationModel daoDenominationModel;
	@Inject
	private RewardManager rewardManager;

	

	private void indexUserEvent(UserEventEntity userEvent) {
		try{
		Application.getElasticSearchLogger().indexUserClick(userEvent.getRealmId(), userEvent.getPhoneNumber(), "",
				userEvent.getDeviceType(), userEvent.getOfferId(), userEvent.getOfferSourceId().toLowerCase(),
				userEvent.getOfferTitle(), userEvent.getAdProviderCodeName(), userEvent.getRewardTypeName(),
				userEvent.getOfferPayoutInTargetCurrency(), userEvent.getRewardValue(),
				userEvent.getRewardIsoCurrencyCode(), userEvent.getProfitValue(), "BPM", "",
				UserEventType.conversion.toString(), userEvent.getInternalTransactionId(), "",
				UserEventCategory.INSTALL.toString(), "", "", "", userEvent.getCountryCode(), userEvent.isInstant(),
				userEvent.getApplicationName(), userEvent.getAdvertisingId(), // gaid
				userEvent.getIdfa(), // idfa
				userEvent.isTestMode(), userEvent.getCustomRewardValue(), userEvent.getCustomRewardCurrencyCode());
		}
		catch(Exception exc){
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Indexing event: " + userEvent + " failed: " + ExceptionUtils.getFullStackTrace(exc));
		}
	}


	public String generateInternalTransactionId(AppUserEntity user) {
		String internalTransactionId = DigestUtils
				.sha1Hex(user.getId() + Math.random() * 100000 + System.currentTimeMillis() + user.getPhoneNumber()
						+ user.getPhoneNumberExtension() + user.getEmail());
		return internalTransactionId;
	}

	private Timestamp getCurrentTime() {
		return new Timestamp(new Date().getTime());
	}

	private void log(String tag, LogStatus logStatus, String message) {
		logger.info("[ " + tag + " ][ " + logStatus + " ] " + message);
		Application.getElasticSearchLogger().indexLog(tag, -1, logStatus, tag + " " + message);
	}

	private AppUserEntity selectAppUser(String userId) {
		try {
			int userIdInt = Integer.valueOf(userId);
			return daoAppUser.findById(userIdInt);
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}
	
	private DenominationModelEntity selectDenominationModel(String rewardTypeName, int realmId){
		try{
			return daoDenominationModel.findByRewardTypeNameAndRealmId(rewardTypeName, realmId).get(0);
		}catch (Exception exc){
			exc.printStackTrace();
			return null;
		}
	}

	public void saveConversion(FyberCallbackDetails details) {
		try {
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processing fyber event: " + details.toString());
			AppUserEntity appUser = null;
			if (details.getUid() != null) {
				appUser = selectAppUser(details.getUid());
			}
			if (appUser == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select user for: " + details.toString());
				return;
			}
			DenominationModelEntity denominationModel = this.selectDenominationModel(appUser.getRewardTypeName(), appUser.getRealmId());
			if (denominationModel == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select denomination model for: "
						+ details.toString() + " appuser: " + appUser.toString());
				return;
			}
			Timestamp timestamp = getCurrentTime();
			UserEventEntity userEvent = 
			setupEvent(details, appUser, denominationModel, timestamp);
			daoUserEvent.create(userEvent);
			indexUserEvent(userEvent);
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processed event: " + userEvent.toString()
					+ " for supersonic details:" + details.toString() + " appuser: " + appUser.toString());
			rewardManager.updateUserConversionHistory(userEvent);
			RealmEntity realm = daoRealm.findById(appUser.getRealmId());

			rewardManager.issueReward(realm, userEvent, null, false);
		} catch (Exception exc) {
			exc.printStackTrace();
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Processing supersonic event: " + details.toString()
					+ " failed exc: " + ExceptionUtils.getFullStackTrace(exc));
		}
		
	}

	private UserEventEntity setupEvent(FyberCallbackDetails details, AppUserEntity appUser,
			DenominationModelEntity denominationModel, Timestamp timestamp) {
		UserEventEntity userEvent = new UserEventEntity();
		userEvent.setAdProviderCodeName("Fyber");
		userEvent.setAdvertisingId(appUser.getAdvertisingId());
		userEvent.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
		userEvent.setApplicationName(appUser.getApplicationName());
		userEvent.setClickDate(timestamp);
		userEvent.setConversionDate(timestamp);
		userEvent.setCountryCode(appUser.getCountryCode());
		userEvent.setCustomRewardCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		Double rewardValue = Double.valueOf(details.getAmount());
		userEvent.setCustomRewardValue(rewardValue);
		userEvent.setDeviceId(appUser.getDeviceId());
		userEvent.setDeviceType(appUser.getDeviceType());
		userEvent.setEmail(appUser.getEmail());
		userEvent.setIdfa(appUser.getIdfa());
		userEvent.setInternalTransactionId(generateInternalTransactionId(appUser));
		userEvent.setIosDeviceToken(appUser.getiOSDeviceToken());
		userEvent.setOfferId(details.getSid());

		userEvent.setOfferPayout(
				denominateValue(denominationModel, rewardValue));
		userEvent.setOfferPayoutInTargetCurrency(
				denominateValue(denominationModel, rewardValue));
		//System.out.println("rewardValue: " + rewardValue +  " native: " + denominationModel.getNativeMultipler() + " multipler: " + denominationModel.getMultiplier());
		userEvent.setRewardValue(
				 rewardValue);
		userEvent.setProfilSplitFraction(denominationModel.getCommisionPercentage()/100);
		double denominatedRewardValue = userEvent.getRewardValue();
		userEvent.setProfitValue(denominatedRewardValue * userEvent.getProfilSplitFraction());
		userEvent.setRewardValue(denominatedRewardValue - userEvent.getProfitValue());
		userEvent.setProfitValue(round(userEvent.getProfitValue(), 4));
		userEvent.setRewardValue(round(userEvent.getRewardValue(), 4));
		userEvent.setRewardIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		userEvent.setOfferPayoutInTargetCurrencyIsoCurrencyCode(denominationModel.getSourcePayoutCurrencyCode());
		userEvent.setOfferPayoutIsoCurrencyCode(userEvent.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
		userEvent.setUserId(appUser.getId());
		userEvent.setRewardDate(timestamp);
		userEvent.setRewardTypeName(appUser.getRewardTypeName());
		userEvent.setUserEventCategory(UserEventCategory.INSTALL.toString());
		userEvent.setTransactionId(details.getSid());
		userEvent.setOfferTitle("Fyber offer");
		userEvent.setOfferSourceId(details.getSid());
		userEvent.setPhoneNumber(appUser.getPhoneNumber());
		userEvent.setPhoneNumberExt(appUser.getPhoneNumberExtension());
		userEvent.setRealmId(appUser.getRealmId());
		return userEvent;
	}
	private double denominateValue(DenominationModelEntity model, double value){
		return round(value / model.getMultiplier(), 4);
	}
	
	public double round(double value, int places) {

		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}
	
	public void saveConversion(TrialpayCallbackDetails details) {
		try {
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processing trialpay event: " + details.toString());
			AppUserEntity appUser = null;
			if (details.getSid() != null) {
				appUser = selectAppUser(details.getSid());
			}
			if (appUser == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select user for: " + details.toString());
				return;
			}
			DenominationModelEntity denominationModel = this.selectDenominationModel(appUser.getRewardTypeName(), appUser.getRealmId());
			if (denominationModel == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select denomination model for: "
						+ details.toString() + " appuser: " + appUser.toString());
				return;
			}
			Timestamp timestamp = getCurrentTime();
			UserEventEntity userEvent = 
			setupEvent(details, appUser, denominationModel, timestamp);
			daoUserEvent.create(userEvent);
			indexUserEvent(userEvent);
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processed event: " + userEvent.toString()
					+ " for supersonic details:" + details.toString() + " appuser: " + appUser.toString());
			rewardManager.updateUserConversionHistory(userEvent);
			RealmEntity realm = daoRealm.findById(appUser.getRealmId());

			rewardManager.issueReward(realm, userEvent, null, false);
		} catch (Exception exc) {
			exc.printStackTrace();
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Processing trialpay event: " + details.toString()
					+ " failed exc: " + ExceptionUtils.getFullStackTrace(exc));
		}
		
	}

	private UserEventEntity setupEvent(TrialpayCallbackDetails details, AppUserEntity appUser,
			DenominationModelEntity denominationModel, Timestamp timestamp) {
		UserEventEntity userEvent = new UserEventEntity();
		userEvent.setAdProviderCodeName("Fyber");
		userEvent.setAdvertisingId(appUser.getAdvertisingId());
		userEvent.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
		userEvent.setApplicationName(appUser.getApplicationName());
		userEvent.setClickDate(timestamp);
		userEvent.setConversionDate(timestamp);
		userEvent.setCountryCode(appUser.getCountryCode());
		userEvent.setCustomRewardCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		Double rewardValue = Double.valueOf(details.getRewardAmount());
		userEvent.setCustomRewardValue(rewardValue);
		userEvent.setDeviceId(appUser.getDeviceId());
		userEvent.setDeviceType(appUser.getDeviceType());
		userEvent.setEmail(appUser.getEmail());
		userEvent.setIdfa(appUser.getIdfa());
		userEvent.setInternalTransactionId(generateInternalTransactionId(appUser));
		userEvent.setIosDeviceToken(appUser.getiOSDeviceToken());
		userEvent.setOfferId(details.getOid());

		userEvent.setOfferPayout(
				denominateValue(denominationModel, rewardValue));
		userEvent.setOfferPayoutInTargetCurrency(
				denominateValue(denominationModel, rewardValue));
		//System.out.println("rewardValue: " + rewardValue +  " native: " + denominationModel.getNativeMultipler() + " multipler: " + denominationModel.getMultiplier());
		userEvent.setRewardValue(
				 rewardValue);
		userEvent.setProfilSplitFraction(denominationModel.getCommisionPercentage()/100);
		double denominatedRewardValue = userEvent.getRewardValue();
		userEvent.setProfitValue(denominatedRewardValue * userEvent.getProfilSplitFraction());
		userEvent.setRewardValue(denominatedRewardValue - userEvent.getProfitValue());
		userEvent.setProfitValue(round(userEvent.getProfitValue(), 4));
		userEvent.setRewardValue(round(userEvent.getRewardValue(), 4));
		userEvent.setRewardIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		userEvent.setOfferPayoutInTargetCurrencyIsoCurrencyCode(denominationModel.getSourcePayoutCurrencyCode());
		userEvent.setOfferPayoutIsoCurrencyCode(userEvent.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
		userEvent.setUserId(appUser.getId());
		userEvent.setRewardDate(timestamp);
		userEvent.setRewardTypeName(appUser.getRewardTypeName());
		userEvent.setUserEventCategory(UserEventCategory.INSTALL.toString());
		userEvent.setTransactionId(details.getOid());
		userEvent.setOfferTitle("Trialpay offer");
		userEvent.setOfferSourceId(details.getOid());
		userEvent.setPhoneNumber(appUser.getPhoneNumber());
		userEvent.setPhoneNumberExt(appUser.getPhoneNumberExtension());
		userEvent.setRealmId(appUser.getRealmId());
		return userEvent;
	}


	public void saveConversion(AdGateCallbackDetails details) {
		try {
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processing adgate event: " + details.toString());
			AppUserEntity appUser = null;
			if (details.getS1() != null) {
				appUser = selectAppUser(details.getS1());
			}
			if (appUser == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select user for: " + details.toString());
				return;
			}
			DenominationModelEntity denominationModel = this.selectDenominationModel(appUser.getRewardTypeName(), appUser.getRealmId());
			if (denominationModel == null) {
				log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Couldnt select denomination model for: "
						+ details.toString() + " appuser: " + appUser.toString());
				return;
			}
			Timestamp timestamp = getCurrentTime();
			UserEventEntity userEvent = 
			setupEvent(details, appUser, denominationModel, timestamp);
			daoUserEvent.create(userEvent);
			indexUserEvent(userEvent);
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.OK, "Processed event: " + userEvent.toString()
					+ " for supersonic details:" + details.toString() + " appuser: " + appUser.toString());
			rewardManager.updateUserConversionHistory(userEvent);
			RealmEntity realm = daoRealm.findById(appUser.getRealmId());

			rewardManager.issueReward(realm, userEvent, null, false);
		} catch (Exception exc) {
			exc.printStackTrace();
			log(Application.EXTERNAL_OFFER_WALL, LogStatus.ERROR, "Processing adgate event: " + details.toString()
					+ " failed exc: " + ExceptionUtils.getFullStackTrace(exc));
		}
		
	}


	private UserEventEntity setupEvent(AdGateCallbackDetails details, AppUserEntity appUser,
			DenominationModelEntity denominationModel, Timestamp timestamp) {
		UserEventEntity userEvent = new UserEventEntity();
		userEvent.setAdProviderCodeName("Adgate");
		userEvent.setAdvertisingId(appUser.getAdvertisingId());
		userEvent.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
		userEvent.setApplicationName(appUser.getApplicationName());
		userEvent.setClickDate(timestamp);
		userEvent.setConversionDate(timestamp);
		userEvent.setCountryCode(appUser.getCountryCode());
		userEvent.setCustomRewardCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		Double rewardValue = Double.valueOf(details.getPoints());
		userEvent.setCustomRewardValue(rewardValue);
		userEvent.setDeviceId(appUser.getDeviceId());
		userEvent.setDeviceType(appUser.getDeviceType());
		userEvent.setEmail(appUser.getEmail());
		userEvent.setIdfa(appUser.getIdfa());
		userEvent.setInternalTransactionId(generateInternalTransactionId(appUser));
		userEvent.setIosDeviceToken(appUser.getiOSDeviceToken());
		userEvent.setOfferId(details.getOfferId());

		userEvent.setOfferPayout(
				denominateValue(denominationModel, rewardValue));
		userEvent.setOfferPayoutInTargetCurrency(
				denominateValue(denominationModel, rewardValue));
		//System.out.println("rewardValue: " + rewardValue +  " native: " + denominationModel.getNativeMultipler() + " multipler: " + denominationModel.getMultiplier());
		userEvent.setRewardValue(
				 rewardValue);
		userEvent.setProfilSplitFraction(denominationModel.getCommisionPercentage()/100);
		double denominatedRewardValue = userEvent.getRewardValue();
		userEvent.setProfitValue(denominatedRewardValue * userEvent.getProfilSplitFraction());
		userEvent.setRewardValue(denominatedRewardValue - userEvent.getProfitValue());
		userEvent.setProfitValue(round(userEvent.getProfitValue(), 4));
		userEvent.setRewardValue(round(userEvent.getRewardValue(), 4));
		userEvent.setRewardIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
		userEvent.setOfferPayoutInTargetCurrencyIsoCurrencyCode(denominationModel.getSourcePayoutCurrencyCode());
		userEvent.setOfferPayoutIsoCurrencyCode(userEvent.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
		userEvent.setUserId(appUser.getId());
		userEvent.setRewardDate(timestamp);
		userEvent.setRewardTypeName(appUser.getRewardTypeName());
		userEvent.setUserEventCategory(UserEventCategory.INSTALL.toString());
		userEvent.setTransactionId(details.getTransactionId());
		userEvent.setOfferTitle(details.getOfferName());
		userEvent.setOfferSourceId(details.getOfferId());
		userEvent.setPhoneNumber(appUser.getPhoneNumber());
		userEvent.setPhoneNumberExt(appUser.getPhoneNumberExtension());
		userEvent.setRealmId(appUser.getRealmId());
		return userEvent;
	}
	
	
	

}