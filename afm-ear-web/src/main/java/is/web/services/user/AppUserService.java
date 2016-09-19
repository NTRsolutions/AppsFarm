package is.web.services.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

import is.ejb.bl.attendance.AttendanceManager;
import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIResponse;
import is.web.services.APIValidator;
import is.web.services.user.validators.AdvertisingIdValidator;
import is.web.services.user.validators.ApplicationValidator;
import is.web.services.user.validators.CountryCodeValidator;
import is.web.services.user.validators.DeviceTokenValidator;
import is.web.services.user.validators.DeviceTypeValidator;
import is.web.services.user.validators.EmailDBValidator;
import is.web.services.user.validators.EmailValidator;
import is.web.services.user.validators.PasswordValidator;
import is.web.services.user.validators.UsernameDBValidator;
import is.web.services.user.validators.UsernameValidator;
import is.web.services.wall.validators.DeviceValidator;
import is.web.services.wall.validators.PhoneValidator;
import is.web.services.wall.validators.RequestValidator;
import is.web.services.wallet.validators.UsernamePasswordCombinationValidator;

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
	private DeviceValidator deviceValidator;
	@Inject
	private PhoneValidator phoneValidator;
	@Inject
	private DeviceTokenValidator deviceTokenValidator;
	@Inject
	private UsernamePasswordCombinationValidator usernamePasswordCombinationValidator;
	@Inject
	private RequestValidator requestValidator;
	@Inject
	private APIHelper apiHelper;
	@Inject
	private AttendanceManager attendanceManager;

	@Path("/user/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String registerUser(final APIRequestDetails apiRequestDetails) {
		logger.info("Received register request:" + apiRequestDetails);
		Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
				Application.USER_REGISTRATION_ACTIVITY + " Received register request: " + apiRequestDetails);

		RegisterUserResponse response = new RegisterUserResponse();
		validateRegisterRequest(apiRequestDetails, response);
		if (response.getStatus() == null || !response.getStatus().equals(RespStatusEnum.FAILED)) {
			AppUserEntity insertedUser = insertUser(apiRequestDetails);
			if (insertedUser != null) {
				apiHelper.setupSuccessResponse(response);
				insertedUser.setPassword("");
				response.setAppUserEntity(insertedUser);
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);

			}
		}
		Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
				Application.USER_REGISTRATION_ACTIVITY + "response: " + response + " for request: "
						+ apiRequestDetails);
		return new Gson().toJson(response);
	}

	private void validateRegisterRequest(APIRequestDetails apiRequestDetails, APIResponse response) {
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		logger.info("** Validating register request: **");
		for (APIValidator validator : getRegisterValidators()) {
			if (!validator.validate(parameters)) {
				logger.info("-> Validator: " + validator.getClass() + " FAILED");
				apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.USER_REGISTRATION_ACTIVITY + " Validation failed in : " + validator.getClass()
								+ " cause: " + validator.getInvalidValueErrorCode() + " for request: "
								+ apiRequestDetails);
				return;
			} else {
				logger.info("-> Validator: " + validator.getClass() + " OK");
			}
		}
	}

	private AppUserEntity insertUser(APIRequestDetails apiRequestDetails) {
		try {
			logger.info("** Inserting user to database **");
			AppUserEntity appUser = prepareAppUserFromParameters(apiRequestDetails);
			if (appUser == null) {
				logger.info("Could not create user from parameters.");
				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1,
						LogStatus.ERROR, Application.USER_REGISTRATION_ACTIVITY
								+ " Error occured when creating user object from details: " + apiRequestDetails);
				return null;
			}
			daoAppUser.create(appUser);
			logger.info("** User inserted ** ");
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
					Application.USER_REGISTRATION_ACTIVITY + " User inserted to database: " + appUser.toString());
			return appUser;
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_REGISTRATION_ACTIVITY + " Error occured when inserting user: " + exc.toString()
							+ " for details: " + apiRequestDetails);

			exc.printStackTrace();
			return null;
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
			appUser.setDeviceId((String) parameters.get("deviceId"));
			appUser.setPhoneId((String) parameters.get("phoneId"));
			appUser.setAndroidDeviceToken((String) parameters.get("androidDeviceToken"));
			appUser.setAttendanceLastBonusTime(new Timestamp(new Date().getTime()));

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
		validators.add(phoneValidator);
		validators.add(deviceValidator);
		validators.add(deviceTokenValidator);
		validators.add(requestValidator);
		return validators;
	}

	private String getRewardType(String applicationName, String countryCode) {
		String rewardType = "";
		if (applicationName.equals("AppsFarm")) {
			if (countryCode.equals("GB")) {
				rewardType = "AppsFarm-GB";
			}
			if (countryCode.equals("US")) {
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
		Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
				Application.USER_LOGIN_ACTIVITY + " received user login request: " + apiRequestDetails);
		LoginUserResponse response = new LoginUserResponse();
		validateLoginRequest(apiRequestDetails, response);
		if (response.getStatus() == null || !response.getStatus().equals(RespStatusEnum.FAILED)) {
			executeUserLogin(apiRequestDetails, response);
		}

		Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
				Application.USER_LOGIN_ACTIVITY + " result: " + response + "for request: " + apiRequestDetails);
		return new Gson().toJson(response);
	}

	private void validateLoginRequest(APIRequestDetails apiRequestDetails, LoginUserResponse response) {
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		logger.info("** Validating login request: **");
		for (APIValidator validator : getLoginValidators()) {
			if (!validator.validate(parameters)) {
				logger.info("-> Validator: " + validator.getClass() + " FAILED");
				apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
						Application.USER_LOGIN_ACTIVITY + " validation failed for request: " + apiRequestDetails
								+ " in: " + validator.getClass() + " cause: " + validator.getInvalidValueErrorCode());
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
		validators.add(deviceTokenValidator);
		validators.add(deviceValidator);
		validators.add(phoneValidator);
		validators.add(requestValidator);
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

		AppUserEntity appUser = getUser(username);
		if (appUser == null) {
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_LOGIN_ACTIVITY + " there is no user for request: " + apiRequestDetails);
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INVALID_USER);
		} else {
			String hashedPassword = getPasswordHash(password);
			if (appUser.getPassword().equals(hashedPassword)) {
				appUser = updateAppUserDetailsIfNecessary(appUser, apiRequestDetails);
				attendanceManager.checkAttendance(appUser);
				appUser.setPassword("");
				apiHelper.setupSuccessResponse(response);
				response.setAppUserEntity(appUser);
				
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_USER_INVALID_PASSWORD);
			}
		}
	}

	private AppUserEntity updateAppUserDetailsIfNecessary(AppUserEntity appUser, APIRequestDetails apiRequestDetails) {
		boolean isUpdate = false;
		HashMap<String, Object> parameters = apiRequestDetails.getParameters();
		String advertisingId = (String) parameters.get("advertisingId");
		String androidDeviceToken = (String) parameters.get("androidDeviceToken");
		String deviceId = (String) parameters.get("deviceId");
		String phoneId = (String) parameters.get("phoneId");
		if (appUser.getAdvertisingId() == null || !appUser.getAdvertisingId().equals(advertisingId)) {
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " updating advertisingId for request : " + apiRequestDetails
							+ " for appuser: " + appUser);
			appUser.setAdvertisingId(advertisingId);
			logger.info("Updated advertisingId for appUser: " + appUser);
			isUpdate = true;
		}
		if (appUser.getAndroidDeviceToken() == null || !appUser.getAndroidDeviceToken().equals(androidDeviceToken)) {
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " updating android device token for request : "
							+ apiRequestDetails + " for appuser: " + appUser);
			appUser.setAndroidDeviceToken(androidDeviceToken);
			logger.info("Updated android device token for appUser: " + appUser);
			isUpdate = true;
		}

		if (appUser.getDeviceId()  == null|| !appUser.getDeviceId().equals(deviceId)) {
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " updating device id for request : " + apiRequestDetails
							+ " for appuser: " + appUser);
			appUser.setDeviceId(deviceId);
			logger.info("Updated device id for appUser: " + appUser);
			isUpdate = true;
		}

		if (appUser.getPhoneId() == null || !appUser.getPhoneId().equals(phoneId)) {
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " updating phone id for request : " + apiRequestDetails
							+ " for appuser: " + appUser);
			appUser.setPhoneId(phoneId);
			logger.info("Updated phone id for appUser: " + appUser);
			isUpdate = true;
		}

		if (isUpdate) {
			daoAppUser.createOrUpdate(appUser);
		}
		return appUser;
	}

	@Path("/user/update")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateUser(final APIRequestDetails apiRequestDetails) {
		APIResponse response = new APIResponse();
		try {
			logger.info("Received update request:" + apiRequestDetails);
			Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.OK,
					Application.USER_UPDATE_ACTIVITY + " received user update request: " + apiRequestDetails);
			HashMap<String, Object> parameters = apiRequestDetails.getParameters();
			logger.info("** Validating user update request: **");
			for (APIValidator validator : getUserUpdateValidators()) {
				if (!validator.validate(parameters)) {
					logger.info("-> Validator: " + validator.getClass() + " FAILED");
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.ERROR,
							Application.USER_UPDATE_ACTIVITY + " validation failed for request: " + apiRequestDetails
									+ " in: " + validator.getClass() + " cause: "
									+ validator.getInvalidValueErrorCode());
					return apiHelper.getGson().toJson(response);
				} else {
					logger.info("-> Validator: " + validator.getClass() + " OK");
				}
			}

			boolean result = updateUser(parameters);
			if (result){
				apiHelper.setupSuccessResponse(response);
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			}
			return apiHelper.getGson().toJson(response);
		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return apiHelper.getGson().toJson(response);
		}
	}

	private boolean updateUser(HashMap<String, Object> parameters) {
		try {
			boolean isUpdate = false;
			AppUserEntity appUser = daoAppUser.findByUsername((String) parameters.get("username"));
			if (parameters.containsKey("androidDeviceToken")) {
				appUser.setAndroidDeviceToken((String) parameters.get("androidDeviceToken"));
				isUpdate = true;
			}
			if (parameters.containsKey("ageRange")) {
				appUser.setAgeRange((String) parameters.get("ageRange"));
				isUpdate = true;
			}
			if (parameters.containsKey("gender")) {
				appUser.setGender((String) parameters.get("gender"));
				isUpdate = true;
			}
			if (parameters.containsKey("newPassword")){
				String newPassword = (String) parameters.get("newPassword");
				if (newPassword != null && newPassword.length() > 5){
					appUser.setPassword(this.getPasswordHash(newPassword));
					isUpdate = true;
				}
			}
			if (parameters.containsKey("email")){
				String email = (String) parameters.get("email");
				appUser.setEmail(email);
				isUpdate = true;
			}
			
			if (isUpdate){
				daoAppUser.createOrUpdate(appUser);
				Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.ERROR,
						Application.USER_UPDATE_ACTIVITY + " Updated appUser: " + appUser);
				logger.info("Updated appUser: " + appUser);
			}
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	private List<APIValidator> getUserUpdateValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(emailDBValidator);
		validators.add(usernamePasswordCombinationValidator);
		validators.add(requestValidator);
		return validators;
	}

}
