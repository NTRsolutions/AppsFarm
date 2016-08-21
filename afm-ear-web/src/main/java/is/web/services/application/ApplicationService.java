package is.web.services.application;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.offerRewardTypes.ImageBannerEntity;
import is.web.beans.offerRewardTypes.OfferRewardTypesBean;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Path("/")
public class ApplicationService {

	@Inject
	private Logger logger;
	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOMobileApplicationType daoMobileApplicationType;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAORewardType daoRewardType;

	@GET
	@Produces("application/json")
	@Path("/v1/applicationConfiguration/")
	public String getApplicationConfiguration(@QueryParam("applicationName") String applicationName,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("email") String email, @QueryParam("rewardType") String rewardType) {

		logger.info("Producing application configuration.");
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			String dataContent = "email:" + email + " systemInfo: " + systemInfo + " miscData: " + miscData
					+ " applicationName: " + applicationName + " rewardType: " + rewardType + " ip: " + ipAddress;

			logger.info("Received request:" + dataContent);
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.OK,
					Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " received request: " + dataContent);

			if (rewardType == null || rewardType.length() == 0) {

				AppUserEntity appUser = getUserWithEmail(email);
				if (appUser != null) {
					rewardType = appUser.getRewardTypeName();
				} else {
					rewardType = "";
				}
			}

			RewardTypeEntity rewardTypeEntity = daoRewardType.findByName(rewardType);
			if (rewardTypeEntity == null) {
				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " reward type not found" + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_REWARD_TYPE + "\"}";
			}

			ApplicationConfiguration config = prepareApplicationConfiguration(rewardTypeEntity);
			config = loadUserActivationInformation(config, email);
			config = loadImageBanners(config, rewardType);

			String jsonResult = new Gson().toJson(config);
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.OK, Application.APPLICATION_GET_CONFIGURATION_ACTIVITY
							+ " end configuration for user with email: " + email + " :" + jsonResult);

			logger.info(jsonResult);
			return jsonResult;

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.ERROR,
					Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " error occured: " + exc.getStackTrace());
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}

	}

	private AppUserEntity getUserWithEmail(String email) {
		AppUserEntity appUser = null;
		try {
			if (email != null) {
				appUser = daoAppUser.findByEmail(email);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return appUser;
	}

	private ApplicationConfiguration loadImageBanners(ApplicationConfiguration config, String rewardType) {
		try {
			String imageBannerJson = "";
			if (rewardType != null && rewardType.length() > 0) {
				RewardTypeEntity rewardTypeEntity = daoRewardType.findByName(rewardType);
				imageBannerJson = rewardTypeEntity.getImageBannerContent();
				List<ImageBannerEntity> imageBannerList = new Gson().fromJson(imageBannerJson,
						new TypeToken<List<ImageBannerEntity>>() {
						}.getType());
				config.setImageBannerList(imageBannerList);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return config;
	}

	private ApplicationConfiguration loadUserActivationInformation(ApplicationConfiguration config, String email) {
		if (email != null && email.length() > 0) {
			if (!checkIfUserActivatedAccount(email)) {
				String referMessage = "In order to send referral invitations and receive rewards both inviting and invited users need to activate referral via the link provided in registration email";
				config.setReferMessage(referMessage);
				config.setReferStatus(false);
			}
		}
		return config;
	}

	private ApplicationConfiguration prepareApplicationConfiguration(RewardTypeEntity rewardType) {

		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setStatus(RespStatusEnum.SUCCESS);
		config.setCode(RespCodesEnum.OK);
		String wallMessage = "This service is currently unavailable.";
		String walletMessage = "This service is currently unavailable.";
		String videoMessage = "This service is currently unavailable.";
		String referMessage = "This service is currently unavailable.";
		String spinnerMessage = "This service is currently unavailable.";
		if (rewardType != null) {
			if (rewardType.getWallStatusMessage() != null && rewardType.getWallStatusMessage().length() > 0) {
				wallMessage = rewardType.getWallStatusMessage();
			}
			if (rewardType.getWalletStatusMessage() != null && rewardType.getWalletStatusMessage().length() > 0) {
				walletMessage = rewardType.getWalletStatusMessage();
			}
			if (rewardType.getVideoStatusMessage() != null && rewardType.getVideoStatusMessage().length() > 0) {
				videoMessage = rewardType.getVideoStatusMessage();
			}
			if (rewardType.getReferStatusMessage() != null && rewardType.getReferStatusMessage().length() > 0) {
				referMessage = rewardType.getReferStatusMessage();
			}
			if (rewardType.getSpinnerStatusMessage() != null && rewardType.getSpinnerStatusMessage().length() > 0) {
				spinnerMessage = rewardType.getSpinnerStatusMessage();
			}
		}

		boolean wallStatus = rewardType.isWallStatus();
		boolean videoStatus = rewardType.isVideoStatus();
		boolean walletStatus = rewardType.isWalletStatus();
		boolean referStatus = rewardType.isReferStatus();
		boolean spinnerStatus = rewardType.isSpinnerStatus();

		config.setWallMessage(wallMessage);
		config.setWalletMessage(walletMessage);
		config.setVideoMessage(videoMessage);
		config.setReferMessage(referMessage);
		config.setSpinnerMessage(spinnerMessage);

		config.setWallStatus(wallStatus);
		config.setWalletStatus(walletStatus);
		config.setVideoStatus(videoStatus);
		config.setReferStatus(referStatus);
		config.setSpinnerStatus(spinnerStatus);

		config.setQuidcoCommisionPercentage(getQuidcoCommisionPercentage());
		return config;
	}

	private double getQuidcoCommisionPercentage(){
		try{
			RealmEntity realm = daoRealm.findById(4);
			return realm.getQuidcoPercentageCommision();
		}
		catch (Exception exception){
			exception.printStackTrace();
			return 0.5;
		}
	}
	
	private boolean checkIfUserActivatedAccount(String email) {
		try {
			logger.info("checking if user have activated account with email:" + email);
			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser != null) {
				logger.info("found user with email:" + email);
				String activationCode = appUser.getActivationCode();
				logger.info("activation code:" + activationCode);
				if (activationCode != null && activationCode.length() > 0) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception exc) {
			exc.getStackTrace();

		}

		return false;
	}

	@GET
	@Produces("application/json")
	@Path("/v1/versionCheck/")
	public String versionCheck(@QueryParam("deviceType") String deviceType,
			@QueryParam("versionNumber") String versionNumber, @QueryParam("networkName") String networkName,
			@QueryParam("locale") String locale, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("applicationName") String applicationName) {
		String dataContent = "";
		try {

			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = "deviceType: " + deviceType + " versionNumber: " + versionNumber + " locale:" + locale
					+ " systemInfo: " + systemInfo + " miscData: " + miscData + " networkName: " + networkName
					+ " applicationName: " + applicationName + " ip: " + ipAddress;

			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1, LogStatus.OK,
					Application.APPLICATION_VERSION_ACTIVITY + " received request: " + dataContent);

			if (networkName == null || networkName.length() == 0) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid network: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";

			}

			RealmEntity realmEntity = daoRealm.findByName(networkName);

			if (realmEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid realm: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";

			}

			MobileApplicationTypeEntity mobileApplication = null;
			if (applicationName == null) {
				// backward compatibility
				/*
				 * Application.getElasticSearchLogger().indexLog(
				 * Application.APPLICATION_VERSION_ACTIVITY, -1,
				 * LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY +
				 * " invalid application name " + dataContent);
				 * 
				 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " +
				 * "\"code\":\"" + RespCodesEnum.ERROR_INVALID_APPLICATION +
				 * "\"}";
				 */

				mobileApplication = new MobileApplicationTypeEntity();
				mobileApplication.setMinimumVersion("1.0.0");
				mobileApplication.setVersionCheck(true);
				mobileApplication.setVersionErrorMessage("Wrong version!");

			} else {
				mobileApplication = daoMobileApplicationType.findByName(applicationName);
				if (mobileApplication == null) {

					Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
							LogStatus.ERROR,
							Application.APPLICATION_VERSION_ACTIVITY + " application list is empty: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

				}

			}

			// return success if version check is disabled
			if (mobileApplication.isVersionCheck() == false) {
				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.OK, Application.APPLICATION_VERSION_ACTIVITY
								+ " version checking is disabled, success for all apps: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT
						+ "\"}";
			}

			if (versionNumber == null || versionNumber.length() == 0) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
						+ mobileApplication.getVersionErrorMessage() + "\"}";
			}

			// we accept version in format: xx.xx.xxx

			if (versionNumber.split("\\.").length != 3) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
						+ mobileApplication.getVersionErrorMessage() + "\"}";
			}

			String minimumVersion = mobileApplication.getMinimumVersion();
			String[] minimumVersionArray = minimumVersion.split("\\.");
			int[] minimumVersionNumberArray = new int[minimumVersionArray.length];
			for (int i = 0; i < minimumVersionArray.length; i++) {

				if (minimumVersionArray[i].length() > 3)
					minimumVersionNumberArray[i] = Integer.parseInt(minimumVersionArray[i].substring(0, 3));
				else

					minimumVersionNumberArray[i] = Integer.parseInt(minimumVersionArray[i]);
			}

			String[] userVersionArray = versionNumber.split("\\.");
			int[] userVersionNumberArray = new int[userVersionArray.length];
			for (int i = 0; i < userVersionArray.length; i++) {

				if (userVersionArray[i].length() > 3)
					userVersionNumberArray[i] = Integer.parseInt(userVersionArray[i].substring(0, 3));
				else
					userVersionNumberArray[i] = Integer.parseInt(userVersionArray[i]);
			}

			// a.bb.ccc
			// checking "a"
			if (minimumVersionNumberArray[0] > userVersionNumberArray[0]) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
						+ mobileApplication.getVersionErrorMessage() + "\"}";
			} else if (minimumVersionNumberArray[0] < userVersionNumberArray[0]) {
				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.OK, Application.APPLICATION_VERSION_ACTIVITY + " valid version: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT
						+ "\"}";

			}

			// checking "bb"
			if (minimumVersionNumberArray[1] > userVersionNumberArray[1]) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
						+ mobileApplication.getVersionErrorMessage() + "\"}";
			} else if (minimumVersionNumberArray[1] < userVersionNumberArray[1]) {
				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.OK, Application.APPLICATION_VERSION_ACTIVITY + " valid version: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT
						+ "\"}";

			}

			// checking "ccc"
			if (minimumVersionNumberArray[2] > userVersionNumberArray[2]) {

				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.ERROR, Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
						+ mobileApplication.getVersionErrorMessage() + "\"}";
			} else if (minimumVersionNumberArray[2] <= userVersionNumberArray[2]) {
				Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1,
						LogStatus.OK, Application.APPLICATION_VERSION_ACTIVITY + " valid version: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT
						+ "\"}";

			}

			// if ifs failed lets return error.
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1, LogStatus.ERROR,
					Application.APPLICATION_VERSION_ACTIVITY + " invalid version: " + dataContent);
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INVALID_VERSION + "\", " + "\"errorMessage\":\""
					+ mobileApplication.getVersionErrorMessage() + "\"}";

		} catch (Exception exc) {
			System.out.println(exc.getClass());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_VERSION_ACTIVITY, -1, LogStatus.ERROR,
					Application.APPLICATION_VERSION_ACTIVITY + " error checking version: " + dataContent + " "
							+ exc.toString());
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}

	}
}
