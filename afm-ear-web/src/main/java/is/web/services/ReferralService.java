package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.referral.ReferralManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/")
public class ReferralService {

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private ReferralManager referralManager;

	@GET
	@Produces("application/json")
	@Path("/v1/getReferralInfo/")
	public String getReferralInfo(@QueryParam("phoneNumber") String phoneNumber, @QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "phoneNumber: " + phoneNumber + " miscData:  " + miscData + " ipAddress: " + ipAddress;

			Application.getElasticSearchLogger().indexLog(Application.REFERRAL_INFO_ACTIVITY, -1, LogStatus.OK, dataContent);
 
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER + "\"}";
			}
			double referralValue = 0;
			String referralCurrency = "";
			String countryCode = appUser.getCountryCode();
			
			int referralFirstThreshold = referralManager.getReferralRewardThreshold(countryCode, 1);
			int referralSecondThreshold = referralManager.getReferralRewardThreshold(countryCode, 2);
			double referralRewardAtFirstThreshold = referralManager.getReferralRewardValueByGeo(countryCode, 1);
			double referralRewardAtSecondThreshold = referralManager.getReferralRewardValueByGeo(countryCode, 2);
			double referralTotalValue = referralRewardAtFirstThreshold + referralRewardAtSecondThreshold;
			referralCurrency = referralManager.getRewardCurrencyCodeByGeo(countryCode);
            int referralMaxCount = referralManager.getMaxReferralCount(countryCode);
			
			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"," + "\"referralFirstThreshold\":\"" + referralFirstThreshold + "\","+ "\"referralSecondThreshold\":\"" + referralSecondThreshold + "\","+ "\"referralRewardAtFirstThreshold\":\"" + referralRewardAtFirstThreshold + "\"," + "\"referralRewardAtSecondThreshold\":\"" + referralRewardAtSecondThreshold + "\","
					+ "\"referralTotalValue\":\"" + referralTotalValue + "\"," + "\"referralCurrency\":\"" + referralCurrency + "\","
					+  "\"referralRewardAtFirstTreshold\":\"" + referralRewardAtFirstThreshold + "\"," + "\"referralRewardAtSecondTreshold\":\"" + referralRewardAtSecondThreshold + "\","
					+ "\"referralMaxCount\":\"" + referralMaxCount + "\""
			+ "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}
	}
}
