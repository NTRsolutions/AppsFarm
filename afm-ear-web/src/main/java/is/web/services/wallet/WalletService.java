package is.web.services.wallet;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationNameEnum;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.friends.UserFriendManager;
import is.ejb.bl.offerProviders.quidco.QuidcoManager;
import is.ejb.bl.offerProviders.snapdeal.SnapdealManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.bl.wallet.WalletManager;
import is.ejb.bl.wallet.WalletTransactionZendeskEntity;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.dao.DAOWalletPayoutOfferTransaction;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.ejb.dl.entities.WalletPayoutOfferTransactionEntity;
import is.ejb.dl.entities.WalletTransactionEntity;
import is.web.services.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.zendesk.client.v2.model.Ticket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class WalletService {

	@Inject
	private Logger logger;
	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAOWalletPayoutCarrier daoWalletPayoutCarrier;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	DAOWalletData daoWalletData;

	@Inject
	WalletManager walletManager;

	@Inject
	private ZendeskManager zendeskManager;

	@Inject
	private DAOWalletPayoutOfferTransaction daoWalletPayoutOfferTranscation;

	@Inject
	private DAOWalletTransaction daoWalletTransaction;

	@Inject
	private HashValidationManager hashValidationManager;

	@Inject
	private UserFriendManager userFriendManager;

	private Gson gson;

	@Inject
	private QuidcoManager quidcoManager;

	@Inject
	private SnapdealManager snapdealManager;

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getPayoutCarriers/")
	public String getWalletPayoutCarriers(@QueryParam("networkName") String networkName,
			@QueryParam("rewardType") String rewardType, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("hashkey") String hashkey)

	{
		String dataContent = null;
		try {

			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = "rewardType: " + rewardType + " systemInfo: " + systemInfo + " miscData: " + miscData
					+ " networkName: " + networkName + " ip: " + ipAddress;

			if (networkName == null || networkName.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_CARRIER_ACTIVITY + " invalid network: " + dataContent);

				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND);
				return gson.toJson(failedResult);
			}

			RealmEntity realmEntity = daoRealm.findByName(networkName);

			if (realmEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_CARRIER_ACTIVITY + " invalid realm: " + dataContent);

				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND);
				return gson.toJson(failedResult);
			}
			if (phoneNumber == null || phoneNumber.length() == 0) {
				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_INVALID_USER);
				return gson.toJson(failedResult);
			}

			AppUserEntity user = daoAppUser.findByPhoneNumber(phoneNumber);
			if (user == null) {
				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_INVALID_USER);
				return gson.toJson(failedResult);
			}

			/*
			 * boolean isRequestValid =
			 * hashValidationManager.isRequestValid(realmEntity.getApiKey(),
			 * hashkey, hashValidationManager.getFullURL(httpRequest),
			 * user.getPhoneNumber(), user.getPhoneNumberExtension(),
			 * systemInfo, miscData, ipAddress); if (!isRequestValid) {
			 * WalletPayoutCarriersResponse failedResult =
			 * prepareWalletPayoutCarriersResponse(
			 * RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED); return
			 * gson.toJson(failedResult); }
			 */

			if (rewardType == null || rewardType.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_CARRIER_ACTIVITY + " invalid rewardType: " + dataContent);

				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_INVALID_REWARD_TYPE);
				return gson.toJson(failedResult);

			}

			List<RewardTypeEntity> rewardTypeLists = daoRewardType.findAllByRealmId(realmEntity.getId());

			boolean isRewardTypeInRealm = false;
			for (RewardTypeEntity ent : rewardTypeLists) {
				if (ent.getName().equals(rewardType)) {
					isRewardTypeInRealm = true;
					break;
				}
			}

			if (!isRewardTypeInRealm) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_CARRIER_ACTIVITY + " invalid rewardType: " + dataContent);

				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_INVALID_REWARD_TYPE);
				return gson.toJson(failedResult);
			}
			RewardTypeEntity rewardTypeFromName = daoRewardType.findByName(rewardType);
			if (rewardTypeFromName == null) {
				WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
						RespCodesEnum.ERROR_INVALID_REWARD_TYPE);
				return gson.toJson(failedResult);
			}

			List<WalletPayoutCarrierEntity> walletPayoutCarrierList = daoWalletPayoutCarrier
					.findByRewardTypeId(rewardTypeFromName.getId());

			String jsonResponseContent = gson.toJson(walletPayoutCarrierList);

			Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_CARRIER_ACTIVITY + " success result: " + jsonResponseContent);

			WalletPayoutCarriersResponse successResult = new WalletPayoutCarriersResponse();
			successResult.setStatus(RespStatusEnum.SUCCESS.toString());
			successResult.setCode(RespCodesEnum.OK.toString());
			successResult.setWalletPayoutCarrierList(walletPayoutCarrierList);

			return gson.toJson(successResult);
		} catch (Exception exc) {

			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_CARRIER_ACTIVITY, -1, LogStatus.ERROR,
					Application.WALLET_CARRIER_ACTIVITY + " error wallet carrier: " + dataContent + " "
							+ exc.toString());
			WalletPayoutCarriersResponse failedResult = prepareWalletPayoutCarriersResponse(
					RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return gson.toJson(failedResult);

		}

	}

	public WalletPayoutCarriersResponse prepareWalletPayoutCarriersResponse(RespCodesEnum code) {
		WalletPayoutCarriersResponse failedResult = new WalletPayoutCarriersResponse();
		failedResult.setStatus(RespStatusEnum.FAILED.toString());
		failedResult.setCode(code.toString());
		return failedResult;
	}

	@GET
	@Produces("application/json")
	@Path("/v1/wd/")
	public String getWalletData(@QueryParam("networkName") String networkName,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("hashkey") String hashkey)

	{
		String dataContent = null;
		try {

			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = "phoneNumber " + phoneNumber + " systemInfo: " + systemInfo + " miscData: " + miscData
					+ " networkName: " + networkName + " ip: " + ipAddress;

			if (networkName == null || networkName.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_DATA_ACTIVITY + " invalid network: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}

			RealmEntity realmEntity = daoRealm.findByName(networkName);

			if (realmEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_DATA_ACTIVITY + " invalid realm: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}
			if (phoneNumber == null || phoneNumber.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_DATA_ACTIVITY + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}

			AppUserEntity user = daoAppUser.findByPhoneNumber(phoneNumber);
			if (user == null) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
						Application.WALLET_DATA_ACTIVITY + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}

			// validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realmEntity.getApiKey(), hashkey,
					hashValidationManager.getFullURL(httpRequest), user.getPhoneNumber(),
					user.getPhoneNumberExtension(), systemInfo, miscData, ipAddress);
			if (!isRequestValid) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"-1\"}";
			}

			WalletDataEntity walletData = daoWalletData.findByUserId(user.getId());

			String walletJsonData = null;
			if (walletData == null) {
				walletData = new WalletDataEntity();

			}

			double roundBalance = round(walletData.getBalance(), 2);
			walletData.setBalance(roundBalance);
			walletData.setUserId(user.getId());
			walletData.setPotentialQuidcoRewards(quidcoManager.calculatePotentialQuidcoRewardsForUser(user.getId()));
			walletData.setPotentialSnapdealRewards(snapdealManager.calculatePotentialRewardForUser(user.getId()));
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			walletJsonData = gson.toJson(walletData);

			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_DATA_ACTIVITY + " success result: " + dataContent);

			logger.info("Returning: " + "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\""
					+ RespCodesEnum.OK + "\", " + "\"walletData\":" + walletJsonData + "}");
			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\", "
					+ "\"walletData\":" + walletJsonData +

			"}";

		} catch (Exception exc) {
			System.out.println(exc.getClass());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
					Application.WALLET_DATA_ACTIVITY + " error wallet data: " + dataContent + " " + Arrays.toString(exc.getStackTrace()));
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
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

	/*
	 * @GET
	 * 
	 * @Produces("application/json")
	 * 
	 * @Path("/v1/wpccc/") public String
	 * getWalletPayoutCarriersConversionCheck(@QueryParam("networkName") String
	 * networkName,
	 * 
	 * @QueryParam("applicationName") String
	 * applicationName, @QueryParam("rewardTypeName") String rewardTypeName,
	 * 
	 * @QueryParam("rewardName") String rewardName, @QueryParam("carrierName")
	 * String carrierName,
	 * 
	 * @QueryParam("phoneNumber") String phoneNumber, @QueryParam("amount")
	 * String amount,
	 * 
	 * @QueryParam("systemInfo") String systemInfo, @QueryParam("miscData")
	 * String miscData,
	 * 
	 * @QueryParam("hashkey") String hashkey) {
	 * 
	 * String dataContent = null; try { System.out.println("*********");
	 * System.out.println("*********"); System.out.println("*********");
	 * System.out.println(carrierName); System.out.println("*********");
	 * System.out.println("*********"); String ipAddress =
	 * httpRequest.getHeader("X-FORWARDED-FOR"); if (ipAddress == null) {
	 * ipAddress = httpRequest.getRemoteAddr(); }
	 * 
	 * dataContent = "carrierName: " + carrierName + "phoneNumber: " +
	 * phoneNumber + " amount: " + amount + " systemInfo: " + systemInfo +
	 * " miscData: " + miscData + " networkName: " + networkName + " ip: " +
	 * ipAddress;
	 * 
	 * if (networkName == null || networkName.length() == 0) {
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid network: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}"; }
	 * 
	 * RealmEntity realmEntity = daoRealm.findByName(networkName);
	 * 
	 * if (realmEntity == null) {
	 * 
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid realm: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}"; } if (phoneNumber ==
	 * null || phoneNumber.length() == 0) {
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid userid: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INVALID_USER + "\"}"; }
	 * 
	 * AppUserEntity user = daoAppUser.findByPhoneNumber(phoneNumber); if (user
	 * == null) { Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid userid: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INVALID_USER + "\"}"; } // validate request hash
	 * boolean isRequestValid =
	 * hashValidationManager.isRequestValid(realmEntity.getApiKey(), hashkey,
	 * hashValidationManager.getFullURL(httpRequest), user.getPhoneNumber(),
	 * user.getPhoneNumberExtension(), systemInfo, miscData, ipAddress); if
	 * (!isRequestValid) { return "{\"status\":\"" + RespStatusEnum.FAILED +
	 * "\", " + "\"code\":\"" + RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED +
	 * "\", " + "\"userId\":\"-1\"}"; }
	 * 
	 * if (carrierName == null || carrierName.length() == 0) {
	 * 
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid carrier: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INVALID_WALLET_PAYOUT_CARRIER + "\"}"; }
	 * 
	 * WalletPayoutCarrierEntity carrier =
	 * daoWalletPayoutCarrier.findByCarrierName(carrierName);
	 * 
	 * if (carrier == null) {
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY + " invalid carrier: " +
	 * dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INVALID_WALLET_PAYOUT_CARRIER + "\"}"; }
	 * 
	 * WalletDataEntity walletData = daoWalletData.findByUserId(user.getId());
	 * if (walletData == null) {
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY +
	 * " invalid wallet data: " + dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INVALID_WALLET_DATA + "\"}"; }
	 * 
	 * if (amount == null || amount.length() == 0) {
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY +
	 * " insufficient wallet balance " + dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"}"; }
	 * 
	 * double balance = walletData.getBalance(); double carrierMinValue =
	 * carrier.getMinValueToPayout(); double carrierPayoutGap =
	 * carrier.getPayoutGap();
	 * 
	 * double amountD = Double.parseDouble(amount); WalletPayoutConversionCheck
	 * conversionCheck = new WalletPayoutConversionCheck(); if (balance <= 0 ||
	 * balance < carrierMinValue || balance < amountD) {
	 * 
	 * // if balance is 0 or less // or carrier min value is lower than balance
	 * 
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY +
	 * " insufficient wallet balance " + dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"}";
	 * 
	 * } else { if (Double.parseDouble(amount) < carrierMinValue) { // if amount
	 * is lower than carrierMinValue
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY +
	 * " insufficient amount " + dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
	 * + RespCodesEnum.ERROR_INSUFFICIENT_AMOUNT_FOR_PAYOUT + "\"}"; } else { //
	 * all processed ok - issue reward of type wallet / transfer double
	 * totalValue = amountD - (amountD - carrierMinValue); amountD = amountD -
	 * totalValue; while (amountD >= carrierPayoutGap) { totalValue = totalValue
	 * + carrierPayoutGap; amountD = amountD - carrierPayoutGap; }
	 * 
	 * conversionCheck.setCarrierName(carrier.getCarrierName());
	 * conversionCheck.setResultAmount((int) totalValue);
	 * conversionCheck.setRestAmount(amountD); } }
	 * 
	 * Gson gson = new GsonBuilder().setPrettyPrinting().create(); String
	 * jsonResponseContent = gson.toJson(conversionCheck);
	 * 
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_DATA_ACTIVITY, -1, LogStatus.OK, Application.WALLET_DATA_ACTIVITY
	 * + " success result: " + dataContent);
	 * 
	 * return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\""
	 * + RespCodesEnum.OK + "\", " + "\"walletPayoutCarrierConversion\":" +
	 * jsonResponseContent +
	 * 
	 * "}";
	 * 
	 * } catch (Exception exc) { System.out.println(exc.getClass());
	 * exc.printStackTrace();
	 * Application.getElasticSearchLogger().indexLog(Application.
	 * WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY, -1, LogStatus.ERROR,
	 * Application.WALLET_CARRIER_PAYOUT_CHECK_ACTIVITY +
	 * " error wallet payout conversion check: " + dataContent + " " +
	 * exc.toString()); return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
	 * + "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}"; } }
	 */

	@GET
	@Produces("application/json")
	@Path("/v1/wupo/")
	public String usePayoutOfferFromWallet(@QueryParam("networkName") String networkName,
			@QueryParam("applicationName") String applicationName, @QueryParam("rewardTypeName") String rewardTypeName,
			@QueryParam("rewardName") String rewardName, @QueryParam("value") double value,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("hashkey") String hashkey,
			@QueryParam("friendPhoneNumber") String friendPhoneNumber) {
		String dataContent = null;
		try {
			// TODO just for testing to let wallet payout be processed
			// irrespective of offer price
			// value = 0.1;

			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = "networkName: " + networkName + " applicatioName: " + applicationName + " rewardName: "
					+ rewardName + applicationName + " rewardTypeName: " + rewardTypeName + " phoneNumber: "
					+ phoneNumber + " systemInfo: " + systemInfo + " miscData: " + miscData + " friend phone number:"
					+ friendPhoneNumber + " ipAddress: " + ipAddress;

			if (networkName == null || networkName.length() == 0) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid network: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}

			RealmEntity realmEntity = daoRealm.findByName(networkName);
			if (realmEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid realm: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}

			if (phoneNumber == null || phoneNumber.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}

			AppUserEntity user = daoAppUser.findByPhoneNumber(phoneNumber);
			if (user == null) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}
			if (friendPhoneNumber != null && friendPhoneNumber.length() > 0) {
				if (!userFriendManager.isFriendInList(user.getId(), friendPhoneNumber)) {
					Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1,
							LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER
									+ " friend phone number is not in user friend list: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_FRIEND_IS_NOT_IN_LIST + "\"}";
				}
			}

			// validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realmEntity.getApiKey(), hashkey,
					hashValidationManager.getFullURL(httpRequest), user.getPhoneNumber(),
					user.getPhoneNumberExtension(), systemInfo, miscData, ipAddress);
			if (!isRequestValid) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"-1\"}";
			}

			// predefined offers
			if (rewardName == null || rewardName.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid reward name: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_WALLET_PAYOUT_OFFER + "\"}";
			}

			if (applicationName == null || applicationName.length() == 0) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid applicationName: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_APPLICATION_NAME + "\"}";

			}

			WalletDataEntity walletData = daoWalletData.findByUserId(user.getId());
			if (walletData == null) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER + " invalid wallet data: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_WALLET_DATA + "\"}";
			}

			// =============================== Cinetreats
			// ==================================
			if (applicationName.toLowerCase().contains("cine")) {

				double balance = walletData.getBalance();
				if (balance < value) {
					Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1,
							LogStatus.ERROR,
							Application.WALLET_USE_PAYOUT_OFFER + " rewardValue is more than balance: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"}";
				}

				walletManager.processWalletPayout(realmEntity, user, applicationName, rewardTypeName, rewardName, value,
						ipAddress, friendPhoneNumber);

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.OK,
						Application.WALLET_USE_PAYOUT_OFFER + " success result: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\" "
						+ "}";
			}

			// =============================== GoAhead
			// ==================================
			if (applicationName.toLowerCase().contains("goahead")) {

				double balance = walletData.getBalance();
				if (balance < value) {
					Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1,
							LogStatus.ERROR,
							Application.WALLET_USE_PAYOUT_OFFER + " rewardValue is more than balance: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"}";
				}

				walletManager.processWalletPayout(realmEntity, user, applicationName, rewardTypeName, rewardName, value,
						ipAddress, friendPhoneNumber);

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.OK,
						Application.WALLET_USE_PAYOUT_OFFER + " success result: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\" "
						+ "}";
			}

			// =============================== BR and AR
			// ===============================
			if (applicationName.toLowerCase().equals("rewardz") || applicationName.toLowerCase().equals("airrewardz")) {

				double rewardValue = value;
				double balance = walletData.getBalance();
				if (balance < rewardValue) {
					Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1,
							LogStatus.ERROR,
							Application.WALLET_USE_PAYOUT_OFFER + " rewardValue is more than balance: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_INSUFFICIENT_WALLET_BALANCE + "\"}";
				}

				double minimalWalletGeoPayout = getMinimalWalletPayoutValueByGeo(user.getCountryCode());

				if (minimalWalletGeoPayout > rewardValue) {

					Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1,
							LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER
									+ " minimal wallet payout geo is more than reward value: " + dataContent);

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_MINIMAL_GEO_PAYOUT_VALUE + "\" " + ",\"minimalValue\":\""
							+ minimalWalletGeoPayout + "\" " + ",\"currency\":\""
							+ getRewardCurrencyCodeByGeo(user.getCountryCode())

							+ "\"}";

				}

				// process wallet request
				walletManager.processWalletPayout(realmEntity, user, applicationName, rewardTypeName, rewardName,
						rewardValue, ipAddress, friendPhoneNumber);

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.OK,
						Application.WALLET_USE_PAYOUT_OFFER + " success result: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\" "
						+ "}";
			}
		} catch (Exception exc) {
			System.out.println(exc.getClass());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER, -1, LogStatus.ERROR,
					Application.WALLET_USE_PAYOUT_OFFER + " error wallet payout conversion check: " + dataContent + " "
							+ exc.toString());
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}

		return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
				+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
	}

	// only for GoAhead
	@GET
	@Path("/v1/wupoa/")
	public void payoutOfferFromWalletAccept(@QueryParam("content") String content) {
		String dataContent = null;
		try {
			dataContent = content;

			WalletTransactionZendeskEntity zendeskResponse = new Gson().fromJson(content,
					WalletTransactionZendeskEntity.class);
			// default is GoAhead
			String applicationName = "GoAhead";
			if (zendeskResponse.getFromAccount().toLowerCase().contains("cine")) {
				applicationName = "Cinetreats";
			}

			Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
					LogStatus.OK, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE + " received data from zendesk: "
							+ dataContent);

			// System.out.println(zendeskResponse.toString());
			WalletTransactionEntity walletTransactionEntity = daoWalletTransaction
					.findByTicketIdAndApplicationName(zendeskResponse.getTicketId(), applicationName);

			if (walletTransactionEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
						LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE
								+ " no transaction entity with ticketId: " + dataContent);
				return;

			}

			if (!walletTransactionEntity.getStatus().equals("PENDING")) {

				Application.getElasticSearchLogger()
						.indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1, LogStatus.ERROR,
								Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE
										+ " transaction entity is not pending: " + dataContent + " trans id: "
										+ walletTransactionEntity.getId());

				return;

			}

			if (!zendeskResponse.getStatus().toLowerCase().contains("solved")) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
						LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE
								+ " zendesk response status is not solved: " + dataContent + zendeskResponse);

				return;
			}
			AppUserEntity user = daoAppUser.findById(walletTransactionEntity.getUserId());
			if (user == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
						LogStatus.ERROR,
						Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE + " invalid user " + dataContent);

				return;
			}
			WalletDataEntity walletData = daoWalletData.findByUserId(user.getId());

			if (walletData.getBalance() < walletTransactionEntity.getPayoutValue()) {

				System.out.println("WALLET PAYOUT BALANCE");
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
						LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE
								+ " insufficient balance: " + dataContent + walletData);
				return;
			}

			RealmEntity realmEntity = daoRealm.findById(user.getRealmId());
			if (realmEntity == null) {

				System.out.println("REALM ENTITY ERROR");
				Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
						LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE + " realm not found: "
								+ dataContent + " trans id: " + walletTransactionEntity.getId());
				return;
			}

			// process wallet reward

			System.out.println("PROCCESS!!!!");
			System.out.println("PROCCESS!!!!");
			System.out.println("PROCCESS!!!!");

			walletManager.processWalletRewardForGoAhead(realmEntity, user, walletData, walletTransactionEntity,
					dataContent);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE, -1,
					LogStatus.ERROR, Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE + " error executing : "
							+ dataContent + " error:" + exc.getStackTrace());
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/wpoh/")
	public String getWalletPayoutOffersHistory(@QueryParam("networkName") String networkName,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("hashkey") String hashkey) {
		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}
		String dataContent = null;
		try {
			dataContent = "networkName: " + networkName + " phoneNumber: " + phoneNumber + " systemInfo: " + systemInfo
					+ " miscData: " + miscData + " ipAddress: " + ipAddress;

			if (networkName == null || networkName.length() == 0) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1,
						LogStatus.ERROR, Application.WALLET_PAYOUT_OFFER_HISTORY + " invalid network: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}

			RealmEntity realmEntity = daoRealm.findByName(networkName);

			if (realmEntity == null) {

				Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1,
						LogStatus.ERROR, Application.WALLET_PAYOUT_OFFER_HISTORY + " invalid realm: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}
			if (phoneNumber == null || phoneNumber.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1,
						LogStatus.ERROR, Application.WALLET_PAYOUT_OFFER_HISTORY + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}

			AppUserEntity user = daoAppUser.findByPhoneNumber(phoneNumber);
			if (user == null) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1,
						LogStatus.ERROR, Application.WALLET_PAYOUT_OFFER_HISTORY + " invalid userid: " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER + "\"}";
			}

			// validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realmEntity.getApiKey(), hashkey,
					hashValidationManager.getFullURL(httpRequest), user.getPhoneNumber(),
					user.getPhoneNumberExtension(), systemInfo, miscData, ipAddress);
			if (!isRequestValid) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"-1\"}";
			}

			// everything is ok

			List<WalletPayoutOfferTransactionEntity> historyList = daoWalletPayoutOfferTranscation
					.findByUserId(user.getId());

			String jsonContent = new Gson().toJson(historyList);
			Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1, LogStatus.OK,
					Application.WALLET_PAYOUT_OFFER_HISTORY + " success result " + dataContent);

			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\", "
					+ "\"walletPayoutOfferHistory\":" + jsonContent + "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_PAYOUT_OFFER_HISTORY, -1, LogStatus.ERROR,
					Application.WALLET_PAYOUT_OFFER_HISTORY + " error executing : " + dataContent + " error:"
							+ exc.getStackTrace());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}

	}

	// TODO in future expose UI for adjusting payouts for different geos
	private String getRewardCurrencyCodeByGeo(String countryCode) {
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
	private double getMinimalWalletPayoutValueByGeo(String countryCode) {
		if (countryCode.equals(CountryCode.KE.toString())) {
			return 5.0;
		} else if (countryCode.equals(CountryCode.IN.toString())) {
			return 10.0;
		} else if (countryCode.equals(CountryCode.ZA.toString())) {
			return 5.0;
		} else if (countryCode.equals(CountryCode.GB.toString())) {
			return 0.0;
		} else if (countryCode.equals(CountryCode.PL.toString())) {
			return 0.0;
		} else
			return 0.0;
	}

}
