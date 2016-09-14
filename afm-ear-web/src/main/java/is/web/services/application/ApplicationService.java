package is.web.services.application;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.offerRewardTypes.ImageBannerEntity;
import is.web.beans.offerRewardTypes.OfferRewardTypesBean;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIValidator;
import is.web.services.application.validators.VersionValidator;
import is.web.services.wall.validators.ApplicationValidator;
import is.web.services.wall.validators.RequestValidator;
import is.web.services.wall.validators.RewardTypeValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
	private DAODenominationModel daoDenominationModel;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private ApplicationValidator applicationValidator;
	@Inject
	private RewardTypeValidator rewardTypeValidator;
	@Inject
	private RequestValidator requestValidator;
	@Inject
	private VersionValidator versionValidator;

	@Inject
	private APIHelper apiHelper;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/application/configuration")
	public String getApplicationConfiguration(final APIRequestDetails details) {
		ApplicationConfigurationResponse response = new ApplicationConfigurationResponse();
		try {
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.OK, Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " received request: " + details
							+ " from ipAddress: " + apiHelper.getIpAddressFromHttpRequest(httpRequest));
			logger.info("received request: " + details + " from ipAddress: "
					+ apiHelper.getIpAddressFromHttpRequest(httpRequest));
			HashMap<String, Object> parameters = details.getParameters();
			for (APIValidator validator : getApplicationConfigurationValidators()) {
				if (validator.validate(parameters)) {
					logger.info("Validator: " + validator.getClass() + " OK");
				} else {
					logger.info("Validator: " + validator.getClass() + " FAILED");
					Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY,
							-1, LogStatus.OK, Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + "Request: " + details
									+ " failed on validator: " + validator.getClass());
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					return apiHelper.getGson().toJson(response);

				}
			}

			ApplicationConfiguration configuration = setupConfiguration(parameters);
			response.setApplicationConfiguration(configuration);
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.OK, Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " Result: " + response + " for request:" + details);
			return apiHelper.getGson().toJson(response);
			

		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			Application.getElasticSearchLogger().indexLog(Application.APPLICATION_GET_CONFIGURATION_ACTIVITY, -1,
					LogStatus.OK, Application.APPLICATION_GET_CONFIGURATION_ACTIVITY + " Error occured : " + exc.toString() + " for request:" + details);
			return apiHelper.getGson().toJson(response);
		}
	}

	private ApplicationConfiguration setupConfiguration(HashMap<String, Object> parameters) {
		ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
		try {
			String rewardTypeName = (String) parameters.get("rewardType");
			RewardTypeEntity rewardType = daoRewardType.findByName(rewardTypeName);
			applicationConfiguration.setAttendanceValue(rewardType.getAttendanceValue());
			applicationConfiguration.setCurrencyCode(rewardType.getCountryCode());
			applicationConfiguration.setVideoRewardAmount(getVideoReward(rewardTypeName));
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return applicationConfiguration;

	}

	private List<APIValidator> getApplicationConfigurationValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(applicationValidator);
		validators.add(rewardTypeValidator);
		validators.add(requestValidator);
		validators.add(versionValidator);
		return validators;
	}

	private double getVideoReward(String rewardType){
		double reward = 0;
		try {
			 List<DenominationModelEntity> denomModel = daoDenominationModel.findByRewardTypeNameAndRealmId(rewardType, 4);
			 DenominationModelEntity model = denomModel.get(0);
			 double profitSplitFraction = model.getVideoCommisonPercentage() / 100;
			 reward = model.getVideoPayout() * model.getVideoPointsMultipler() * (1-profitSplitFraction);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		return reward;
	}
	
}
