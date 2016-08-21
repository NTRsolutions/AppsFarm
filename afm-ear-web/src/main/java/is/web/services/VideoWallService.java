package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.gamification.GamificationManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.video.VideoManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;

@Path("/")
public class VideoWallService {

	@Inject
	private Logger logger;
	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private VideoManager videoManager;

	@Inject
	private GamificationManager gamificationManager;

	@Inject
	private DAORewardType daoRewardType;

	@GET
	@Produces("application/json")
	@Path("/v1/registerVideoAdClick/")
	public String registerVideoAdClick(
			@QueryParam("userId") String userId,
			@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("countryCode") String countryCode,
			@QueryParam("deviceType") String deviceType, // values: iOS
			@QueryParam("deviceId") String deviceId, // required for android //
														// tracking
			@QueryParam("phoneId") String phoneId, // required for androi
			@QueryParam("advertisingId") String advertisingId, // required fo
			@QueryParam("idfa") String idfa, // required for ios tracking
			@QueryParam("iosDeviceToken") String iosDeviceToken, @QueryParam("androidDeviceToken") String androidDeviceToken, @QueryParam("afaNetworkName") String afaNetworkName, @QueryParam("internalNetworkId") int internalNetworkId, @QueryParam("offerId") String offerId,
			@QueryParam("offerTitle") String offerTitle, @QueryParam("offerSourceId") String offerSourceId, @QueryParam("rewardType") String rewardType, @QueryParam("rewardValue") double rewardValue, @QueryParam("rewardCurrency") String rewardCurrency, @QueryParam("carrierName") String carrierName,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String dataContent = " userId: " + userId + " deviceId: " + deviceId + " phoneId: " + phoneId + " idfa: " + idfa + " advertisingId: " + advertisingId + " phoneNumber: " + phoneNumber + " phoneNumberExt: " + phoneNumberExt + " iosDeviceToken: " + iosDeviceToken + " andriodDeviceToken: "
				+ androidDeviceToken + " countryCode: " + countryCode + " miscData: " + miscData + " offer title: " + offerTitle + " offer id: " + offerId + " offer source id: " + offerSourceId + " offer reward type: " + rewardType + " offer reward currency: " + rewardCurrency
				+ " offer reward value: " + rewardValue + " miscData: " + miscData + " systemInfo: " + systemInfo + " carrier: " + carrierName;

		System.out.println("*******************");
		System.out.println("*******************");
		System.out.println("*******************");
		System.out.println("REGISTER AD CLICK VIDEO");
		System.out.println(dataContent);

		// this ws is executed when user started watching video ad
		// currently not used , but impl for future plans

		return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT + "\"}";
	}

	@GET
	@Path("/v1/rewardVideoAdClick/")
	public String rewardVideoAdClick(@QueryParam("email") String email, // user
			@QueryParam("rewardValue") String rewardValue, // for example 5
			@QueryParam("eventId") String eventId, // unique supersonic video
													// view id
			@QueryParam("itemName") String itemName, // currency name for
														// example airtime
			@QueryParam("offerTitle") String offerTitle, // video title
			@QueryParam("applicationId") String applicationId, // our app id
																// (not
																// important)
			@QueryParam("adProvider") String adProvider, // ad provider
															// (supersonic , ad
															// colony...)
			@QueryParam("custom_rwt") String rewardTypeName) {

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}

		String dataContent = "email:" + email + " rewardValue:" + rewardValue + " eventId: " + eventId + " itemName:" + itemName + " offerTite:" + offerTitle + " appicationId: " + applicationId + " adProvder: " + adProvider + " rewardTypeName: " + rewardTypeName + " ipAddress: " + ipAddress;

		// supersonic ips:
		// 176.34.224.39

		System.out.println("*******************");
		System.out.println("*******************");
		System.out.println("*******************");
		System.out.println("REWARD AD CLICK VIDEO");
		System.out.println(dataContent);
		Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK, Application.VIDEO_REWARD_ACTIVITY + " " + " video reward : " + dataContent);
		try {
			System.out.println("V1");

			AppUserEntity user = null;
			if (email == null || email.length() == 0) {
				// failed ws
				return null;
			} else {
				user = daoAppUser.findByEmail(email);
			}
			System.out.println("V2");
			// need to check is user is valid
			if (user == null)
				return null;

			// check values
			if (adProvider.toLowerCase().contains("adcolony")) {
				rewardTypeName = user.getRewardTypeName();
				offerTitle = "AdColony offer";
				itemName = "AdColony offer";
			}

			System.out.println("V3");

			if (rewardValue == null || rewardValue.length() == 0 || eventId == null || eventId.length() == 0 || itemName == null || itemName.length() == 0 || offerTitle == null || offerTitle.length() == 0 || applicationId == null || applicationId.length() == 0 || adProvider == null
					|| adProvider.length() == 0) {
				return null;
			}

			System.out.println("V4");
			RealmEntity realm = daoRealm.findById(user.getRealmId());

			dataContent = "email:" + email + " rewardValue:" + rewardValue + " eventId: " + eventId + " itemName:" + itemName + " offerTite:" + offerTitle + " appicationId: " + applicationId + " adProvder: " + adProvider + " rewardTypeName: " + rewardTypeName + " ipAddress: " + ipAddress;

			// check if request is originating from the correct ip - if not
			// fraud exception is thrown
			videoManager.validateVideoRewardRequest(realm, email, rewardValue, eventId, itemName, offerTitle, applicationId, adProvider, rewardTypeName, ipAddress);

			System.out.println("V5");
			// handle reward request (create user event, conversion entry in es,
			// request reward via wallet topup)
			videoManager.processVideoRewardRequest(realm, email, rewardValue, eventId, itemName, offerTitle, applicationId, adProvider, rewardTypeName, ipAddress, user);

			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK, Application.VIDEO_REWARD_ACTIVITY + " " + " video reward - created  user event : " + dataContent);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.ERROR, Application.VIDEO_REWARD_ACTIVITY_ABORTED + " " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " video reward error:" + exc.toString() + "datacontent" + dataContent);
			return eventId + ":OK";
		}
		// Supersonic want return eventId:OK for confirm
		return eventId + ":OK";
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getVideoRewardValueAndCurrency/")
	public String getVideoRewardValueAndCurrency(@QueryParam("applicationName") String applicationName, @QueryParam("email") String email, @QueryParam("rewardType") String rewardType, @QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String rewardCurrency = "";
		String rewardValue = "";

		try {
			AppUserEntity user = daoAppUser.findByEmail(email);
			rewardCurrency = videoManager.getRewardCurrencyCodeByGeo(user.getCountryCode());
			rewardValue = videoManager.getVideoPayoutValueByGeo(user.getCountryCode())[1] + "";

			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT + "\"," + "\"rewardCurrency\":\"" + rewardCurrency + "\"," + "\"rewardValue\":\"" + rewardValue + "\"" + "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.ERROR, Application.VIDEO_REWARD_ACTIVITY_ABORTED + " " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " video reward error:" + exc.toString());

			return "{\"status\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT + "\"," + "\"rewardCurrency\":\"" + rewardCurrency + "\"," + "\"rewardValue\":\"" + rewardValue + "\"" + "}";
		}

	}

	@GET
	@Path("/v1/checkGamificationForVideo/")
	public String checkGamificationForVideo(@QueryParam("email") String email, @QueryParam("applicationName") String applicationName) {
		try {
			if (email != null && email.length() > 0) {
				AppUserEntity appUser = daoAppUser.findByEmail(email);
				if (appUser != null) {
					boolean gamificationState = gamificationManager.checkIfUserCanWatchVideo(appUser);
					String rewardTypeName = appUser.getRewardTypeName();
					if (rewardTypeName != null) {
						RewardTypeEntity rewardType = daoRewardType.findByRealmIdAndName(appUser.getRealmId(), rewardTypeName);
						if (rewardType != null) {
							int rewardTypeInstallVG = rewardType.getInstallCounterVG();
							int rewardTypeVideoVG = rewardType.getVideoCounterVG();
							return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"," + "\"gamificationState\":\"" + gamificationState + "\"," + "\"gamificationInstallVG\":\"" + rewardTypeInstallVG + "\"," + "\"gamificationVideoVG\":\"" + rewardTypeVideoVG
									+ "\"" + "}";
						} else {
							return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";

						}
					} else {
						return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";

					}

				} else {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";

				}
			} else {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";

			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"" + "}";

	}

}
