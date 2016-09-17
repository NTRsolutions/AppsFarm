package is.web.services.personalDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.reward.RewardTicketManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOPersonalDetails;
import is.ejb.dl.entities.PersonalDetailsEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIResponse;
import is.web.services.APIValidator;
import is.web.services.personalDetails.validators.CountryValidator;
import is.web.services.personalDetails.validators.HouseNumberValidator;
import is.web.services.personalDetails.validators.NameValidator;
import is.web.services.personalDetails.validators.PostCodeValidator;
import is.web.services.personalDetails.validators.StreetValidator;
import is.web.services.personalDetails.validators.SurnameValidator;
import is.web.services.user.validators.PasswordValidator;
import is.web.services.user.validators.UsernameValidator;
import is.web.services.wall.validators.RequestValidator;
import is.web.services.wallet.validators.UsernamePasswordCombinationValidator;

@Path("/")
public class PersonalDetailsService {

	@Inject
	private UsernameValidator usernameValidator;

	@Inject
	private PasswordValidator passwordValidator;

	@Inject
	private UsernamePasswordCombinationValidator usernamePasswordCombinationValidator;

	@Inject
	private CountryValidator countryValidator;
	@Inject
	private HouseNumberValidator houseNumbervalidator;
	@Inject
	private NameValidator nameValidator;
	@Inject
	private SurnameValidator surnameValidator;
	@Inject
	private StreetValidator streetValidator;
	@Inject
	private PostCodeValidator postCodeValidator;
	@Inject
	private RequestValidator requestValidator;
	@Inject
	private APIHelper apiHelper;

	@Inject
	private RewardTicketManager rewardTicketManager;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private Logger logger;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reward/personal/details/")
	public String getPersonalDetails(final APIRequestDetails details) {
		Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.OK,
				Application.PERSONAL_DETAILS + " Received personal details request: " + details + " from ip: "
						+ apiHelper.getIpAddressFromHttpRequest(httpRequest));
		logger.info("Personal details request: " + details);
		PersonalDetailsResponse response = new PersonalDetailsResponse();
		try {
			for (APIValidator validator : getUserDetailsSelectionValidators()) {
				if (!validator.validate(details.getParameters())) {
					Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.ERROR,
							Application.PERSONAL_DETAILS + " Request validation failed on: " + validator.toString()
									+ " for params: " + details);
					logger.info("Validator failed: " + validator.toString());
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					return apiHelper.getGson().toJson(response);
				}
			}
			PersonalDetailsEntity personalDetails = rewardTicketManager
					.getPersonalDetails((String) details.getParameters().get("username"));
			if (personalDetails == null) {
				personalDetails = new PersonalDetailsEntity();
			}
			logger.info(personalDetails.toString());

			apiHelper.setupSuccessResponse(response);
			response.setPersonalDetails(personalDetails);
			Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.OK,
					Application.PERSONAL_DETAILS + " Returned successfully personal data for request: " + details);
			return apiHelper.getGson().toJson(response);

		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.ERROR,
					Application.PERSONAL_DETAILS + " Exception occured for request: " + details + " error: "
							+ exc.toString());
			return apiHelper.getGson().toJson(response);

		}
	}

	private List<APIValidator> getUserDetailsSelectionValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(usernamePasswordCombinationValidator);
		validators.add(requestValidator);
		return validators;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reward/personal/details/update/")
	public String updatePersonalDetails(final APIRequestDetails details) {
		logger.info("Update details: " + details);
		APIResponse response = new APIResponse();
		Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.OK,
				Application.PERSONAL_DETAILS + " Received update personal details request: " + details + " from ip: "
						+ apiHelper.getIpAddressFromHttpRequest(httpRequest));
		try {
			for (APIValidator validator : getUserDetailsUpdateValidators()) {
				if (!validator.validate(details.getParameters())) {
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					logger.info("Validator " + validator.toString() + " failed");
					return apiHelper.getGson().toJson(response);
				}
			}

			rewardTicketManager.createOrUpdateUserPersonalDetails(details.getParameters());
			apiHelper.setupSuccessResponse(response);
			Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.OK,
					Application.PERSONAL_DETAILS + " Update success for details: " + details);
			return apiHelper.getGson().toJson(response);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.PERSONAL_DETAILS, -1, LogStatus.OK,
					Application.PERSONAL_DETAILS + " Update error: " + exc.toString() + " for details: " + details);
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return apiHelper.getGson().toJson(response);
		}

	}

	private List<APIValidator> getUserDetailsUpdateValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(usernamePasswordCombinationValidator);
		validators.add(houseNumbervalidator);
		validators.add(nameValidator);
		validators.add(postCodeValidator);
		validators.add(streetValidator);
		validators.add(surnameValidator);
		validators.add(countryValidator);
		validators.add(requestValidator);
		return validators;
	}

}
