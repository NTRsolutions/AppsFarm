package is.web.services.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIResponse;
import is.web.services.APIValidator;
import is.web.services.user.validators.AdvertisingIdValidator;
import is.web.services.user.validators.ApplicationValidator;
import is.web.services.user.validators.CountryCodeValidator;
import is.web.services.user.validators.DeviceTypeValidator;
import is.web.services.user.validators.EmailDBValidator;
import is.web.services.user.validators.EmailValidator;
import is.web.services.user.validators.PasswordValidator;
import is.web.services.user.validators.UsernameDBValidator;
import is.web.services.user.validators.UsernameValidator;

@Path("/")
public class AppUserService {

	@Inject
	private Logger logger;
	@Inject
	private DAOAppUser daoAppUser;
	@Inject
	private UsernameValidator usernameValidator;
	@Inject
	private PasswordValidator passwordValidator;
	@Inject
	private EmailValidator emailValidator;
	@Inject
	private CountryCodeValidator countryValidator;
	@Inject
	private UsernameDBValidator usernameDBValidator;
	@Inject
	private EmailDBValidator emailDBValidator;
	@Inject
	private AdvertisingIdValidator advertisingIdValidator;
	@Inject
	private DeviceTypeValidator deviceTypeValidator;
	@Inject
	private ApplicationValidator applicationValidator;
	@Inject
	private APIHelper apiHelper;
	
	@Path("/user/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String registerUser(final APIRequestDetails apiRequestDetails) {
		logger.info("Received register request:" + apiRequestDetails);
		APIResponse response = new APIResponse();
		validateRegisterRequest(apiRequestDetails, response);
		if (response.getStatus() == null || !response.getStatus().equals(RespStatusEnum.FAILED)) {
			if (insertUser(apiRequestDetails)) {
				apiHelper.setupSuccessResponse(response);
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			}
		}

		return new Gson().toJson(response);
	}

	private void validateRegisterRequest(APIRequestDetails apiRequestDetails, APIResponse response) {
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		logger.info("** Validating register request: **");
		for (APIValidator validator : getRegisterValidators()) {
			if (!validator.validate(parameters)) {
				logger.info("-> Validator: " + validator.getClass() + " FAILED");
				apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
				return;
			} else {
				logger.info("-> Validator: " + validator.getClass() + " OK");
			}
		}
	}

	private boolean insertUser(APIRequestDetails apiRequestDetails) {
		try {
			logger.info("** Inserting user to database **");
			AppUserEntity appUser = prepareAppUserFromParameters(apiRequestDetails);
			if (appUser == null) {
				logger.info("Could not create user from parameters.");
				return false;
			}
			daoAppUser.create(appUser);
			logger.info("** User inserted ** ");
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}

	}

	private AppUserEntity prepareAppUserFromParameters(APIRequestDetails apiRequestDetails) {
		try {
			HashMap<String, Object> parameters = apiRequestDetails.getParameters();
			AppUserEntity appUser = new AppUserEntity();
			appUser.setUsername((String) parameters.get("username"));
			appUser.setPassword(getPasswordHash((String) parameters.get("password")));
			appUser.setEmail((String) parameters.get("email"));
			appUser.setAdvertisingId((String) parameters.get("advertisingId"));
			String countryCode = (String) parameters.get("countryCode");
			String applicationName = (String) parameters.get("applicationName");
			String deviceType = (String) parameters.get("deviceType");
			appUser.setCountryCode(countryCode);
			appUser.setApplicationName(applicationName);
			appUser.setDeviceType(deviceType);
			appUser.setRewardTypeName(getRewardType(applicationName, countryCode));
			appUser.setRealmId(4);
			
			if (parameters.containsKey("firstName")) {
				appUser.setFirstName((String) parameters.get("firstName"));
			}
			if (parameters.containsKey("lastName")) {
				appUser.setLastName((String) parameters.get("lastName"));
			}
			if (parameters.containsKey("ageRange")) {
				appUser.setAgeRange((String) parameters.get("ageRange"));
			}
			if (parameters.containsKey("gender")) {
				appUser.setGender((String) parameters.get("gender"));
			}
			appUser.setRegistrationTime(new Timestamp(new Date().getTime()));
			return appUser;
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	private List<APIValidator> getRegisterValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(emailValidator);
		validators.add(countryValidator);
		validators.add(usernameDBValidator);
		validators.add(emailValidator);
		validators.add(emailDBValidator);
		validators.add(advertisingIdValidator);
		validators.add(applicationValidator);
		validators.add(deviceTypeValidator);
		return validators;
	}

	private String getRewardType(String applicationName, String countryCode){
		String rewardType = "";
		if (applicationName.equals("AppsFarm")){
			if (countryCode.equals("GB")){
				rewardType = "AppsFarm-GB";
			} 
			if (countryCode.equals("US")){
				rewardType = "AppsFarm-US";
			}
		}
		return rewardType;
	}
	

	public String getPasswordHash(String password) {
		String saltValue = "AppsfArm && S!S_salt stri!ng";
		return DigestUtils.sha1Hex(password + saltValue);
	}

	@Path("/user/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String loginUser(final APIRequestDetails apiRequestDetails) {
		logger.info("Received login request:" + apiRequestDetails);
		LoginUserResponse response = new LoginUserResponse();
		validateLoginRequest(apiRequestDetails, response);
		if (response.getStatus() == null || !response.getStatus().equals(RespStatusEnum.FAILED)) {
			executeUserLogin(apiRequestDetails, response);

		}

		return new Gson().toJson(response);
	}

	private void validateLoginRequest(APIRequestDetails apiRequestDetails, LoginUserResponse response) {
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		logger.info("** Validating login request: **");
		for (APIValidator validator : getLoginValidators()) {
			if (!validator.validate(parameters)) {
				logger.info("-> Validator: " + validator.getClass() + " FAILED");
				apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
				return;
			} else {
				logger.info("-> Validator: " + validator.getClass() + " OK");
			}
		}

	}

	private List<APIValidator> getLoginValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(advertisingIdValidator);
		return validators;
	}

	private AppUserEntity getUser(String username) {
		try {
			return daoAppUser.findByUsername(username);
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}

	}

	private void executeUserLogin(final APIRequestDetails apiRequestDetails, LoginUserResponse response) {
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		String username = (String) parameters.get("username");
		String password = (String) parameters.get("password");
		String advertisingId = (String) parameters.get("advertisingId");
		AppUserEntity appUser = getUser(username);
		if (appUser == null) {
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INVALID_USER);
		} else {
			String hashedPassword = getPasswordHash(password);
			if (appUser.getPassword().equals(hashedPassword)) {
				if (!appUser.getAdvertisingId().equals(advertisingId)) {
					updateAdvertisingId(appUser, advertisingId);
				}
				apiHelper.setupSuccessResponse(response);
				response.setAppUserEntity(appUser);
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_USER_INVALID_PASSWORD);
			}
		}
	}

	private boolean updateAdvertisingId(AppUserEntity appUser, String advertisingId) {
		try {
			appUser.setAdvertisingId(advertisingId);
			daoAppUser.createOrUpdate(appUser);
			logger.info("Updated appUser with id: " + appUser.getId() + " advertisingId: " + advertisingId);
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}

	}

}
