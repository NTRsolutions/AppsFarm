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
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
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
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

@Stateless
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

	public void issueReward(VideoCallbackData data) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY + "Issuing reward for video: " + data.toString());

			boolean validation = validateRequest(data);
			if (!validation) {
				Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.ERROR,
						Application.VIDEO_REWARD_ACTIVITY + "Validation failed for: " + data.toString());
				return;
			}
			AppUserEntity appUser = daoAppUser.findById(Integer.parseInt(data.getUserId()));
			System.out.println("appUser: " + appUser);
			System.out.println("username : " + appUser.getUsername());
			System.out.println("email: " + appUser.getEmail());
			System.out.println("realmid: " + appUser.getRealmId());
			UserEventEntity event = createVideoEvent(appUser, data);

			RealmEntity realm = daoRealm.findById(event.getId());
			daoUserEvent.createOrUpdate(event, 0);
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY + "Issuing reward for video: " + data.toString()
							+ " created event: " + event);
			logEvent(event,realm);
			rewardManager.issueReward(realm, event, null, false);

		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY + "Error occured while issuing reward for: " + data.toString()
							+ " error:" + exc.toString());
			exc.printStackTrace();
		}
	}

	private boolean validateRequest(VideoCallbackData data) {
		try {
			AppUserEntity appUser = daoAppUser.findById(Integer.parseInt(data.getUserId()));
			if (appUser != null && appUser.getUsername().equals(data.getUsername())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	private UserEventEntity createVideoEvent(AppUserEntity user, VideoCallbackData data) {
		UserEventEntity event = new UserEventEntity();
		event.setIosDeviceToken(user.getiOSDeviceToken());
		event.setAndroidDeviceToken(user.getAndroidDeviceToken());
		event.setUserId(user.getId());
		event.setOfferId("FYBER:" + data.getUid());
		System.out.println(user.getDeviceType());
		event.setAdProviderCodeName("FYBER");
		event.setDeviceType(user.getDeviceType());
		event.setInternalTransactionId("FYBER:" + data.getUid());
		event.setPhoneNumber(user.getPhoneNumber());
		event.setPhoneNumberExt(user.getPhoneNumberExtension());
		event.setRewardTypeName(user.getRewardTypeName()); // needed
		event.setRealmId(user.getRealmId());
		event.setOfferTitle("VIDEO OFFER");
		event.setOfferPayout(data.getAmount() * 2);
		event.setOfferPayoutIsoCurrencyCode("GBP");
		event.setOfferPayoutInTargetCurrency(data.getAmount());
		event.setRewardIsoCurrencyCode(data.getCurrencyName());
		event.setRewardValue(data.getAmount());
		event.setRevenueValue(50);
		event.setProfitValue(data.getAmount());
		event.setProfilSplitFraction(0);
		event.setClickDate(new Timestamp(System.currentTimeMillis()));
		event.setConversionDate(new Timestamp(System.currentTimeMillis()));
		event.setCountryCode(user.getCountryCode());
		event.setUserEventCategory(UserEventCategory.VIDEO.toString());
		event.setEmail(user.getEmail());
		event.setInstant(false);
		return event;
	}

	private void logEvent(UserEventEntity event, RealmEntity realm) {
		// add conversion event to conversion index in es (imitate click so that
		// conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "",
				event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
				event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
				event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
				UserEventType.click.toString(), event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", "", event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

		// add conversion event to conversion index in es (imitate conversion so
		// that conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "",
				event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
				event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
				event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
				UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", "", event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

	}

	public boolean validateVideoRewardRequest(RealmEntity realmEntity, String email, String rewardValue, String eventId,
			String itemName, String offerTitle, String applicationId, String adProvider, String rewardTypeName,
			String ipAddress) throws Exception {

		String dataContent = "email:" + email + " rewardValue:" + rewardValue + " eventId: " + eventId + " itemName:"
				+ itemName + " offerTite:" + offerTitle + " appicationId: " + applicationId + " adProvder: "
				+ adProvider + " rewardTypeName: " + rewardTypeName + " ipAddress: " + ipAddress;

		boolean isWhiteListed = false;
		if (externalServerManager.isServerAddressListed(ipAddress, ExternalServerType.SUPERSONIC)) {
			isWhiteListed = true;
		}

		if (isWhiteListed) {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_REWARD_ACTIVITY + " video is from valid source" + dataContent);

			return true;
		}

		else {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.VIDEO_REWARD_ACTIVITY + " " + Application.VIDEO_REWARD_ACTIVITY_ABORTED
							+ Application.ERROR_SERVER_NOT_WHITE_LISTED + " status: " + RespStatusEnum.FAILED + " "
							+ dataContent);

			throw new Exception(Application.ERROR_SERVER_NOT_WHITE_LISTED);
		}
	}

	public void processVideoRewardRequest(RealmEntity realm, String email, String rewardValue, String eventId,
			String itemName, String offerTitle, String applicationId, String adProvider, String rewardTypeName,
			String ipAddress, AppUserEntity user) throws Exception {

		// determine payout and currency code via rewardType and denomination
		// model settings
		double originalCurrencyOfferPayout = 0.01; // in USD - we assume all
													// video rewards have the
													// same payout value in USD
		String userCountryCode = user.getCountryCode();
		double payoutTargetCurrencyValue = getVideoPayoutValueByGeo(userCountryCode)[0];
		double rewardTargetCurrencyValue = getVideoPayoutValueByGeo(userCountryCode)[1];
		double profitTargetCurrencyValue = getVideoPayoutValueByGeo(userCountryCode)[2];
		String rewardTargetCurrencyCode = getRewardCurrencyCodeByGeo(userCountryCode);

		String internalTransactionId = DigestUtils
				.sha1Hex(user.getId() + Math.random() * 100000 + System.currentTimeMillis() + user.getPhoneNumber()
						+ user.getPhoneNumberExtension() + user.getEmail());

		// generate event object and pesrsist it in db
		UserEventEntity event = new UserEventEntity();
		event.setIosDeviceToken(user.getiOSDeviceToken());
		event.setAndroidDeviceToken(user.getAndroidDeviceToken());
		event.setUserId(user.getId());
		event.setOfferId(eventId);
		System.out.println("************");
		System.out.println(user.getDeviceType());
		System.out.println("************");
		event.setAdProviderCodeName(adProvider);
		event.setDeviceType(user.getDeviceType());
		event.setInternalTransactionId(internalTransactionId);
		event.setPhoneNumber(user.getPhoneNumber());
		event.setPhoneNumberExt(user.getPhoneNumberExtension());
		event.setRewardTypeName(rewardTypeName); // needed
		event.setRealmId(realm.getId());
		event.setOfferTitle("VIDEO-" + offerTitle);
		event.setOfferPayout(originalCurrencyOfferPayout);
		event.setOfferPayoutIsoCurrencyCode("USD");
		event.setOfferPayoutInTargetCurrency(payoutTargetCurrencyValue);
		event.setRewardIsoCurrencyCode(rewardTargetCurrencyCode);
		event.setRewardValue(rewardTargetCurrencyValue);
		event.setRevenueValue(profitTargetCurrencyValue);
		event.setProfitValue(profitTargetCurrencyValue);
		event.setProfilSplitFraction(0);
		event.setClickDate(new Timestamp(System.currentTimeMillis()));
		event.setConversionDate(new Timestamp(System.currentTimeMillis()));
		event.setCountryCode(user.getCountryCode());
		event.setUserEventCategory(UserEventCategory.VIDEO.toString());
		event.setEmail(user.getEmail());
		event.setInstant(false); // not instant as we recharge the wallet

		try {
			RewardTypeEntity rewardTypeEntity = daoRewardType.findByRealmIdAndName(realm.getId(), rewardTypeName);
			event.setApplicationName(rewardTypeEntity.getApplicationType());
		} catch (Exception exc) {
			// create event representing conversion
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.ERROR,
					Application.VIDEO_REWARD_ACTIVITY_ERROR + " " + " email: " + user.getEmail() + " country code: "
							+ userCountryCode + " rewardTypeName: " + rewardTypeName + " internalT: "
							+ internalTransactionId + " rewardTargetCurrencyValue: " + event.getRewardValue()
							+ " rewardTargetCurrencyCode: " + event.getOfferPayoutIsoCurrencyCode() + " error: "
							+ exc.toString());
		}

		// NOT USED AS IT WOULD SLOW DOWN THE SYSTEM calculate video offer
		// reward based on denomination model
		// event = offerRewardCalculationManager.calculateOfferReward(event);

		// create event representing conversion
		Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
				Application.VIDEO_REWARD_ACTIVITY + " " + " email: " + user.getEmail() + " country code: "
						+ userCountryCode + " rewardTypeName: " + rewardTypeName + " internalT: "
						+ internalTransactionId + " rewardTargetCurrencyValue: " + event.getRewardValue()
						+ " rewardTargetCurrencyCode: " + event.getOfferPayoutIsoCurrencyCode());

		event = daoUserEvent.createOrUpdate(event, 1);
		// rewardManager.createUserConversionHistory(event);

		// add conversion event to conversion index in es (imitate click so that
		// conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "",
				event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
				event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
				event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
				UserEventType.click.toString(), event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", ipAddress, event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

		// add conversion event to conversion index in es (imitate conversion so
		// that conversion rate is in balance)
		Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "",
				event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(), event.getOfferTitle(),
				event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
				event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
				UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
				UserEventCategory.VIDEO.toString(), "", "", ipAddress, event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), "", // gaid
				"", // idfa
				event.isTestMode(), 0, "");

		// issue wallet topup
		rewardManager.issueReward(realm, event, null, false); // issue reward
	}

	// TODO in future expose UI for adjusting payouts for different geos
	public String getRewardCurrencyCodeByGeo(String countryCode) {
		if (countryCode.equals(CountryCode.KE.toString())) {
			return "KSH";
		} else if (countryCode.equals(CountryCode.IN.toString())) {
			return "INR";
		} else if (countryCode.equals(CountryCode.ZA.toString())) {
			return "ZAR";
		} else if (countryCode.equals(CountryCode.GB.toString())) {
			return "GBP";
		} else if (countryCode.equals(CountryCode.PL.toString())) {
			return "ZL";
		} else
			return CountryCode.UNKNOWN.toString();
	}

	// TODO in future expose UI for adjusting payouts for different geos
	public double[] getVideoPayoutValueByGeo(String countryCode) {
		if (countryCode.equals(CountryCode.KE.toString())) {
			return new double[] { 0.92, 0.5, 0.42 }; // payout|reward|revenue
														// (in target currency)
		} else if (countryCode.equals(CountryCode.IN.toString())) {
			return new double[] { 0.63, 0.1, 0.53 }; // payout|reward|revenue
		} else if (countryCode.equals(CountryCode.ZA.toString())) {
			return new double[] { 0.12, 0.05, 0.07 }; // payout|reward|revenue
		} else if (countryCode.equals(CountryCode.GB.toString())) {
			return new double[] { 0.01, 0.05, 0.05 };
		} else
			return new double[] { 0.01, 0.01, 0.01 }; // payout|reward|revenue
	}

}
