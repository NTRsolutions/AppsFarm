package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOSpinnerData;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;

import java.util.ArrayList;
import java.util.Arrays;
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

@Path("/")
public class SpinnerService {
	@Inject
	private Logger logger;
	@Context
	private HttpServletRequest httpRequest;
	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOSpinnerData daoSpinnerData;

	@Inject
	private SpinnerManager spinnerManager;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAORewardType daoRewardType;

	@GET
	@Produces("application/json")
	@Path("/v1/getSpinnerToplist/")
	public String getSpinnerToplist(@QueryParam("email") String email,
			@QueryParam("applicationName") String applicationName, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "ipAddress: " + ipAddress + " email:" + email + " applicationName:" + applicationName
					+ " systemInfo:" + systemInfo + " miscData:" + miscData;
			log(LogStatus.OK, "Received buy spinner uses for data:" + dataContent);

			if (email == null || email.length() < 0) {
				log(LogStatus.ERROR, "email is null or empty " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";
			}
			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser == null) {
				log(LogStatus.ERROR, "cant find user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";
			}

			List<UserEventEntity> topList = spinnerManager.getTopRewards(10, appUser.getRealmId(),
					appUser.getRewardTypeName());
			List<ToplistRow> topListData = new ArrayList<ToplistRow>();
			for (UserEventEntity event: topList){
				ToplistRow row = new ToplistRow();
				AppUserEntity toplistAppUser = daoAppUser.findById(event.getUserId());
				row.setPhoneNumber(toplistAppUser.getFullName());
				row.setRewardValue(event.getRewardValue());
				topListData.add(row);
			}
			
			TopListResponse response = new TopListResponse();
			response.setTopListData(topListData);
			response.setCode(RespCodesEnum.OK.toString());
			response.setStatus(RespStatusEnum.SUCCESS.toString());
			return new Gson().toJson(response);

		} catch (Exception exc) {

			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			exc.printStackTrace();
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"" + "}";
		}

	}

	@GET
	@Produces("application/json")
	@Path("/v1/bsu/")
	public String buySpinnerUses(@QueryParam("email") String email,
			@QueryParam("applicationName") String applicationName, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "ipAddress: " + ipAddress + " email:" + email + " applicationName:" + applicationName
					+ " systemInfo:" + systemInfo + " miscData:" + miscData;
			log(LogStatus.OK, "Received buy spinner uses for data:" + dataContent);

			if (email == null || email.length() < 0) {
				log(LogStatus.ERROR, "email is null or empty " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";
			}
			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser == null) {
				log(LogStatus.ERROR, "cant find user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";
			}
			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());
			if (spinnerData == null) {
				log(LogStatus.ERROR, "cant find spinner data for user " + dataContent
						+ ", creating entity in database, returning spinner no use ");
				spinnerManager.insertSpinnerData(appUser);

			}
			WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
			if (walletData == null) {
				log(LogStatus.ERROR, "cant find walletData for user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_WALLET_DATA + "\"" + "}";
			}

			RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
			if (rewardType == null) {
				log(LogStatus.ERROR, "cant find reward type for user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_REWARD_TYPE + "\"" + "}";
			}

			double rewardTypeSpinnerUseValue = rewardType.getSpinnerUseValue();
			if (walletData.getBalance() >= rewardTypeSpinnerUseValue) {
				double balanceAfter = walletData.getBalance() - rewardTypeSpinnerUseValue;
				walletData.setBalance(balanceAfter);
				daoWalletData.createOrUpdate(walletData);

				spinnerData.setAvailableUses(spinnerData.getAvailableUses() + 1);
				daoSpinnerData.createOrUpdate(spinnerData);

				log(LogStatus.OK,
						"Succesffully user:" + dataContent + " rewardTypeName: " + appUser.getRewardTypeName() + " bought spinner use for :" + rewardTypeSpinnerUseValue);
				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\""
						+ "}";
			} else {
				log(LogStatus.ERROR, "insufficient wallet balance for user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"" + "}";
			}

		} catch (Exception exc) {

			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			exc.printStackTrace();
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"" + "}";
		}

	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSpinnerData/")
	public String getSpinnnerData(@QueryParam("email") String email,
			@QueryParam("applicationName") String applicationName, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "ipAddress: " + ipAddress + " email:" + email + " applicationName:" + applicationName
					+ " systemInfo:" + systemInfo + " miscData:" + miscData;
			log(LogStatus.OK, " received get spinner data: " + dataContent);

			if (email == null || email.length() < 0) {
				log(LogStatus.ERROR, "email is null or empty " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";
			}
			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser == null) {
				log(LogStatus.ERROR, "cant find user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";
			}
			SpinnerDataEntity spinnerData = getSpinnerDataForUserWithId(appUser.getId());
			if (spinnerData == null) {
				log(LogStatus.ERROR, "cant find spinner data for user " + dataContent
						+ ", creating entity in database, returning spinner no use ");
				spinnerManager.insertSpinnerData(appUser);
				spinnerData = new SpinnerDataEntity();
				spinnerData.setAvailableUses(0);
				spinnerData.setTotalUses(0);
			}

			RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
			if (rewardType == null) {
				log(LogStatus.ERROR, "reward type is null or empty " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\"," + " " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";
			}

			log(LogStatus.OK, "Selected spinner data for " + dataContent);

			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\","
					+ "\"availableUses\":\"" + spinnerData.getAvailableUses() + "\"," + "\"totalUses\":\""
					+ spinnerData.getTotalUses() + "\"," + "\"useSpinnerValue\":\"" + rewardType.getSpinnerUseValue()
					+ "\"" + "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"" + "}";

		}
	}

	public SpinnerDataEntity getSpinnerDataForUserWithId(int userId) {
		try {
			log(LogStatus.OK, "Selecting spinner data for user id : " + userId);
			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(userId);
			return spinnerData;
		} catch (Exception exception) {
			exception.printStackTrace();
			log(LogStatus.ERROR, "Selecting spinner data for user id : " + userId + " failed. " + exception.toString());
			return null;
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/generateSpinnerReward/")
	public String generateSpinnerReward(@QueryParam("email") String email,
			@QueryParam("applicationName") String applicationName, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "ipAddress: " + ipAddress + " email:" + email + " applicationName:" + applicationName
					+ " systemInfo:" + systemInfo + " miscData:" + miscData;
			log(LogStatus.OK, " received spinner use: " + dataContent);

			if (email == null || email.length() < 0) {
				log(LogStatus.ERROR, "email is null or empty " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"" + "}";
			}
			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser == null) {
				log(LogStatus.ERROR, "cant find user " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_NOT_FOUND + "\"" + "}";
			}
			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());
			if (spinnerData == null) {
				log(LogStatus.ERROR, "cant find spinner data for user " + dataContent
						+ ", creating entity in database, returning spinner no use ");
				spinnerManager.insertSpinnerData(appUser);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_SPINNER_NO_USES + "\"" + "}";
			}
			if (spinnerData.getAvailableUses() == 0) {
				log(LogStatus.ERROR, " no available uses for " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_SPINNER_NO_USES + "\"" + "}";
			}
			log(LogStatus.OK, "User has: " + spinnerData.getAvailableUses() + " available uses");
			SpinnerRewardEntity spinnerReward = spinnerManager.generateReward(appUser);
			if (spinnerReward == null) {
				log(LogStatus.ERROR, "error generating spinner reward for " + dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_SPINNER_GENERATING_REWARD + "\"" + "}";
			}

			log(LogStatus.OK, "Generated " + spinnerReward + " for " + dataContent);
			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\","
					+ "\"rewardName\":\"" + spinnerReward.getRewardName() + "\"," + "\"rewardType\":\""
					+ spinnerReward.getRewardType() + "\"," + "\"rewardValue\":\"" + spinnerReward.getRewardValue()
					+ "\"" + "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"" + "}";

		}

	}

	private void log(LogStatus logStatus, String message) {
		logger.info(Application.SPINNER_SERVICE_ACTIVITY + " " + logStatus + " " + message);
		Application.getElasticSearchLogger().indexLog(Application.SPINNER_SERVICE_ACTIVITY, -1, logStatus,
				Application.SPINNER_SERVICE_ACTIVITY + message);
	}

}
