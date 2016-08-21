package is.web.services.quidco;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.offerProviders.quidco.QuidcoManager;
import is.ejb.bl.offerProviders.quidco.QuidcoTransactionReader;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.services.Response;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.bluepodmedia.sdk.quidco.MobileHeaders;
import com.bluepodmedia.sdk.quidco.error.QuidcoException;
import com.bluepodmedia.sdk.quidco.user.UserManager;
import com.bluepodmedia.sdk.quidco.utils.QDevice;
import com.google.gson.Gson;

@Path("/")
public class QuidcoService {

	@Inject
	private DAOAppUser daoAppUser;

	private Gson gson;

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@Inject
	private Logger logger;

	@Inject
	private QuidcoManager quidcoManager;

	@Inject
	private QuidcoTransactionReader quidcoTransactionReader;

	@Inject
	private DAORealm daoRealm;

	@GET
	@Produces("application/json")
	@Path("/v1/quidcoTransaction/")
	public String quidcoTransactionReadTest() {
		quidcoTransactionReader.loadTransactions();
		return "{}";
	}

	@GET
	@Produces("application/json")
	@Path("/v1/registerQuidcoData/")
	public String registerQuidcoData(@QueryParam("email") String email, @QueryParam("quidcoUserId") String quidcoUserId,
			@QueryParam("quidcoUserIdReference") String quidcoUserIdReference) {

		String dataContent = "email: " + email + " quidcoUserId: " + quidcoUserId + " quidcoUserIdReference: "
				+ quidcoUserIdReference;

		try {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_SERVICE_REGISTER_QUIDCO_DATA, -1,
					LogStatus.OK,
					Application.QUIDCO_SERVICE_REGISTER_QUIDCO_DATA + " registering quidco data for : " + dataContent);

			AppUserEntity appUser = daoAppUser.findByEmail(email);
			if (appUser != null && quidcoUserId != null) {
				appUser.setQuidcoUserId(quidcoUserId);
				daoAppUser.createOrUpdate(appUser);
				return gson.toJson(new Response().getSuccessResponse(), Response.class);
			} else {
				Response errorResponse = new Response();
				errorResponse.setStatus(RespStatusEnum.FAILED.toString());
				errorResponse.setCode(RespCodesEnum.ERROR_INVALID_USER_DATA.toString());
				return gson.toJson(errorResponse, Response.class);
			}

		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_SERVICE_REGISTER_QUIDCO_DATA, -1,
					LogStatus.OK, Application.QUIDCO_SERVICE_REGISTER_QUIDCO_DATA + " " + exception.toString()
							+ " for data: " + dataContent);
			Response errorResponse = new Response();
			errorResponse.setStatus(RespStatusEnum.FAILED.toString());
			errorResponse.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			return gson.toJson(errorResponse, Response.class);
		}

	}

	@GET
	@Produces("application/json")
	@Path("/v1/registerQuidcoCreditCard/")
	public String registerQuidcoCreditCard(@QueryParam("userId") String userId,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {
		try {

			String dataContent = "userId: " + userId + " systemInfo: " + systemInfo + " miscData: " + miscData;
			logger.info("Received register quidco credit card: " + dataContent);

			AppUserEntity appUser = daoAppUser.findById(Integer.parseInt(userId));
			if (appUser != null) {
				quidcoManager.sendQuidcoCreditCardRegistrationEmail(appUser);
				Response successResponse = new Response().getSuccessResponse();
				return gson.toJson(successResponse);
			} else {
				Response errorResponse = new Response();
				errorResponse.setStatus(RespStatusEnum.FAILED.toString());
				errorResponse.setCode(RespCodesEnum.ERROR_INVALID_USER.toString());
				return gson.toJson(errorResponse);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Response errorResponse = new Response();
			errorResponse.setStatus(RespStatusEnum.FAILED.toString());
			errorResponse.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			return gson.toJson(errorResponse);
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/registerQuidcoClick/")
	public String registerQuidcoClick(@QueryParam("userId") String userId, @QueryParam("offerTitle") String offerTitle,
			@QueryParam("offerId") String offerId, @QueryParam("merchantName") String merchantName,
			@QueryParam("rewardType") String rewardType, @QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("hashkey") String hashkey, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("link") String link) {
		try {
			if (userId != null) {
				AppUserEntity appUser = daoAppUser.findById(Integer.valueOf(userId));
				RealmEntity realmEntity = daoRealm.findById(appUser.getRealmId());
				if (realmEntity != null && appUser != null) {
					logger.info("Indexing quidco conversion");

					Application.getElasticSearchLogger().indexUserClick(realmEntity.getId(), phoneNumber,
							appUser.getEmail(), appUser.getDeviceType(), offerId, merchantName, offerTitle,
							UserEventCategory.QUIDCO.toString(), rewardType, 0, 0, "GBP", 0, realmEntity.getName(),
							link, UserEventType.click.toString(), null, null, UserEventCategory.QUIDCO.toString(),
							miscData, systemInfo, null, "GB", false, appUser.getApplicationName(),
							appUser.getAdvertisingId(), appUser.getIdfa(), realmEntity.isTestMode(), 0, "GBP");

				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		Response response = new Response().getSuccessResponse();
		return gson.toJson(response);

	}

}
