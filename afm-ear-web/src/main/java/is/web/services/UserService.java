package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.crashReport.CrashReportManager;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.NotificationType;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.referral.ReferralManager;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.system.security.KeyGenerator;
import is.ejb.bl.system.support.donky.DonkyForwardRequest;
import is.ejb.bl.system.support.donky.DonkyManager;
import is.ejb.bl.uiStateManager.UIStateHolder;
import is.ejb.bl.uiStateManager.UIStateManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.util.WebResources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.mail.handlers.message_rfc822;

/**
 * A simple REST service which is able to say hello to someone using
 * HelloService Please take a look at the web.xml where JAX-RS is enabled
 */

@Path("/")
public class UserService {
	@Inject
	private Logger logger;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private MailManager mailManager;

	@Inject
	private HashValidationManager hashValidationManager;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	ReferralManager referralManager;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private UIStateManager uiStateManager;

	@Inject
	private NotificationManager notManager;

	@Inject
	private CrashReportManager crashReportManager;

	@Inject
	private SpinnerManager spinnerManager;

	private String generateInvitationCode() {
		return KeyGenerator.generateKey(20);
	}

	private final String abUrl = "http://mode-rewardz.com/ab";
	// private final String abUrl = "http://test.adjockey.net/ab";

	// ---------------------------------------------- direct user handling ws
	// methods -----------------------------------------
	@GET
	@Produces("application/json")
	@Path("/v1/registerUser/")
	public String registerUserWithQueryRouting(@QueryParam("fullName") String fullName,
			@QueryParam("email") String email, @QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("password") String password,
			@QueryParam("secretQuestion") String secretQuestion, @QueryParam("securityAnswer") String securityAnswer,
			@QueryParam("locale") String locale, @QueryParam("ageRange") String ageRange,
			@QueryParam("male") boolean male, @QueryParam("gender") String gender, @QueryParam("mac") String mac,
			@QueryParam("idfa") String idfa, @QueryParam("iosDeviceToken") String iosDeviceToken,
			@QueryParam("androidDeviceToken") String androidDeviceToken, @QueryParam("phoneId") String phoneId,
			@QueryParam("deviceId") String deviceId, @QueryParam("advertisingId") String advertisingId,
			@QueryParam("deviceType") String deviceType, @QueryParam("countryCode") String countryCode,
			@QueryParam("networkName") String networkName, @QueryParam("applicationName") String applicationName,
			@QueryParam("altabelUserId") int altabelUserId, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("referralCode") String referralCode, @QueryParam("rewardTypeName") String rewardTypeName,
			@QueryParam("overEighteen") String overEighteen) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			// check if the referral code have prefix "ADBROKER_"
			if (referralCode != null && !referralCode.isEmpty()) {
				String prefix = "ADBROKER_";
				if (referralCode.startsWith(prefix)) {
					referralCode = referralCode.replaceFirst(prefix, "");
				}
			}
			// ---

			dataContent = " fullName: " + fullName + " email: " + email + " phoneNumber: " + phoneNumber
					+ " phoneNumberExtension: " + phoneNumberExt + " countryCode: " + countryCode + " mac: " + mac
					+ " idfa: " + idfa + " iosDeviceToken: " + iosDeviceToken + " androidDeviceToken: "
					+ androidDeviceToken + " locale: " + locale + " ageRange: " + ageRange + " male: " + male
					+ " gender: " + gender + " phoneId: " + phoneId + " deviceId: " + deviceId + " advertisingId: "
					+ advertisingId + " altabelUserId: " + altabelUserId + " deviceType: " + deviceType
					+ " applicationName: " + applicationName + " referralCode: " + referralCode + " miscData: "
					+ miscData + " systemInfo: " + systemInfo + " rewardTypeName: " + rewardTypeName + " ip: "
					+ ipAddress + " networkName: " + networkName + " overeighteen: " + overEighteen;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
					Application.USER_REGISTRATION_ACTIVITY + " received request: " + dataContent);

			// get realm
			RealmEntity realm = daoRealm.findByName(networkName);

			// validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
					hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo, miscData,
					ipAddress);
			if (!isRequestValid) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"-1\"}";
			}

			if (realm == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";
			}

			if (email == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_EMAIL + "\"}";

			}
			if (password == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_PASSWORD + "\"}";

			}

			if (phoneNumber == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER + "\"}";
			}
			AppUserEntity appUser = null;
			if (applicationName != null && applicationName.length() > 0 && applicationName.contains("AFA")) {
				if (mac != null && mac.length() > 0) {
					appUser = daoAppUser.findByMac(mac);
					if (appUser != null) {
						return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
								+ RespCodesEnum.ERROR_USER_UNDER_GIVEN_MAC_ALREADY_REGISTERED + "\"}";

					}
				} else {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_USER_UNDER_GIVEN_MAC_ALREADY_REGISTERED + "\"}";
				}
			}

			appUser = daoAppUser.findByEmail(email);
			if (appUser != null) { // user already registered!
				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.USER_REGISTRATION_ACTIVITY + " " + Application.USER_ALREADY_REGISTERED + " "
								+ "aborting registration - email " + email + " already registered");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_UNDER_GIVEN_EMAIL_ALREADY_REGISTERED + "\"}";
			}
			appUser = null;
			appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser != null) { // user already registered!
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
								Application.USER_REGISTRATION_ACTIVITY + " " + Application.USER_ALREADY_REGISTERED + " "
										+ "aborting registration - phone number: " + phoneNumber
										+ " already registered");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_UNDER_GIVEN_PHONE_NUMBER_ALREADY_REGISTERED + "\"}";
			} else {
				String passwordHash;
				if (applicationName != null && (applicationName.toLowerCase().contains("trippa")
						|| applicationName.toLowerCase().contains("goahead"))) {
					passwordHash = password;
				} else {
					passwordHash = getPasswordHash(password);
				}


				appUser = new AppUserEntity();
				appUser.setPhoneNumber(phoneNumber);
				appUser.setFullName(fullName);
				appUser.setEmail(email);
				appUser.setPassword(passwordHash);
				appUser.setAgeRange(ageRange);
				appUser.setMale(male);
				appUser.setGender(gender);

				appUser.setPhoneNumberExtension(phoneNumberExt);
				appUser.setSecretQuestion(secretQuestion);
				appUser.setSecurityAnswer(securityAnswer);
				appUser.setMac(mac);
				appUser.setIdfa(idfa);
				appUser.setLocale(locale);
				appUser.setSystemInfo(systemInfo);
				appUser.setPhoneId(phoneId);
				appUser.setDeviceId(deviceId);
				appUser.setAdvertisingId(advertisingId);
				appUser.setiOSDeviceToken(iosDeviceToken);
				appUser.setAndroidDeviceToken(androidDeviceToken);
				appUser.setRealmId(realm.getId());
				appUser.setDeviceType(deviceType);
				appUser.setCountryCode(countryCode);
				appUser.setAltabelUserId(altabelUserId);
				appUser.setApplicationName(applicationName);
				appUser.setRewardTypeName(rewardTypeName);
				appUser.setRegistrationTime(new Timestamp(System.currentTimeMillis()));
				appUser.setNumberOfSuccessfulInvitations(0);
				appUser.setReferralCode(referralCode);

				if (overEighteen != null && overEighteen.length() > 0 && overEighteen.toLowerCase().equals("true"))
					appUser.setOverEighteen(true);
				else
					appUser.setOverEighteen(false);

				String activationCode = this.generateInvitationCode();
				appUser.setActivationCode(activationCode);

				appUser = daoAppUser.createOrUpdate(appUser);

				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
						Application.USER_REGISTRATION_ACTIVITY + " " + Application.USER_SUCCESSFULLY_REGISTERED + " "
								+ "phone number: " + phoneNumber + " successfully registered");

				// create registration log
				Application.getElasticSearchLogger().indexUserRegistration(fullName, email, phoneNumberExt, phoneNumber,
						locale, systemInfo, miscData, ipAddress, ageRange, male, deviceType, networkName, countryCode,
						referralCode, rewardTypeName, advertisingId, idfa, applicationName);

				// --------------------------- send successful registration
				// e-mail

				String activationLink = abUrl + "/svc/v1/activateAccount?email=" + email + "&activationCode="
						+ activationCode;

				MailParamsHolder mailParamsHolder = new MailParamsHolder();
				mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
				mailParamsHolder.setEmailRecipientFullName(appUser.getFullName());
				mailParamsHolder.setActivationLink(activationLink);

				if (applicationName.toLowerCase().contains("goahead")) {
					mailManager.sendEmail(realm, mailParamsHolder, EmailType.REGISTRATION_GOAHEAD);
				} else if (applicationName.toLowerCase().contains("cine")) {
					mailManager.sendEmail(realm, mailParamsHolder, EmailType.REGISTRATION_CINETREATS);
				} else {
					mailManager.sendEmail(realm, mailParamsHolder, EmailType.REGISTRATION_REWARDZ);
				}

				boolean isReferralCodeValidationSuccessful = false;
				// ------------------------ REGISTRATION WITH REFERRAL CODE
				// --------------------------
				if (referralCode != null && referralCode.length() > 0) {
					referralManager.persistReferralDataForInvitedUser(realm, referralCode, email, phoneNumber,
							phoneNumberExt);
					// if (isReferralCodeValidationSuccessful) {
					// referralManager.processReferralRewardRequest(realm,
					// referralCode, email, phoneNumber, phoneNumberExt);
					// }
				}

				// ----------------------- GENERATE RESPONSE
				// -------------------------
				// for app user object hide some data that we do not want to
				// push to the mobile app
				// set UI state controlling values
				UIStateHolder uiStateHolder = uiStateManager.getUIStateHolder(appUser, realm);

				appUser.setPassword("");
				appUser.setSystemInfo("");
				// return user object
				ResponseLogin responseObject = new ResponseLogin();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setUiStateHolder(uiStateHolder);

				responseObject.setUserData(appUser);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				// spinnerManager.checkDailyBonus(miscData,appUser);
				// return "{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+
				// "\"code\":\""+RespCodesEnum.OK+"\", "+
				// "\"userId\":\""+appUser.getId()+"\"}";
				return jsonResponseContent;// "{\"status\":\""+RespStatusEnum.SUCCESS+"\",
											// "+
											// "\"code\":\""+RespCodesEnum.OK+"\",
											// "+
											// "\"userId\":\""+appUser.getId()+"\"}";
			}

		} catch (Exception exc) {
			System.out.println(exc.getClass());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_REGISTRATION_ACTIVITY + " error creating new appUser: " + dataContent + " "
							+ exc.toString());
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/updateUser/")
	public String updateUserWithQueryRouting(@QueryParam("fullName") String fullName, @QueryParam("email") String email,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("password") String password, @QueryParam("secretQuestion") String secretQuestion,
			@QueryParam("securityAnswer") String securityAnswer, @QueryParam("locale") String locale,
			@QueryParam("ageRange") String ageRange, @QueryParam("male") boolean male,
			@QueryParam("gender") String gender, @QueryParam("mac") String mac, @QueryParam("idfa") String idfa,
			@QueryParam("phoneId") String phoneId, @QueryParam("deviceId") String deviceId,
			@QueryParam("deviceType") String deviceType, @QueryParam("advertisingId") String advertisingId,
			@QueryParam("hashkey") String hashkey, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("androidDeviceToken") String androidDeviceToken) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " fullName: " + fullName + " email: " + email + " phoneNumber: " + phoneNumber
					+ " phoneNumberExtension: " + phoneNumberExt + " mac: " + mac + " idfa: " + idfa + " locale: "
					+ locale + " systemInfo: " + systemInfo + " secretQuestion: " + secretQuestion + " securityAnswer: "
					+ securityAnswer +
					// " password: "+password+
					" ageRange: " + ageRange + " male: " + male + " gender: " + gender + " deviceType: " + deviceType
					+ " phoneId: " + phoneId + " deviceId: " + deviceId + " miscData: " + miscData + " systemInfo: "
					+ systemInfo + " ip: " + ipAddress + " advertisingId: " + advertisingId + " androidDeviceToken: "
					+ androidDeviceToken;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.OK,
					Application.USER_UPDATE_ACTIVITY + " received user edit request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
								Application.USER_UPDATE_ACTIVITY + " " + Application.USER_TO_UPDATE_NOT_FOUND + " "
										+ "aborting user update- user with phone number: " + phoneNumber
										+ " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";
			} else {
				// validate request
				RealmEntity realm = daoRealm.findById(appUser.getRealmId());
				// validate request hash
				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);
				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				if (fullName != null && fullName.length() > 0) {
					appUser.setFullName(fullName);
				}
				if (email != null && email.length() > 0) {
					appUser.setEmail(email);
				}
				if (ageRange != null && ageRange.length() > 0) {
					appUser.setAgeRange(ageRange);
				}

				appUser.setMale(male);

				if (phoneNumberExt != null && phoneNumberExt.length() > 0) {
					appUser.setPhoneNumberExtension(phoneNumberExt);
				}
				if (secretQuestion != null && secretQuestion.length() > 0) {
					appUser.setSecretQuestion(secretQuestion);
				}

				if (securityAnswer != null && securityAnswer.length() > 0) {
					appUser.setSecurityAnswer(securityAnswer);
				}

				if (mac != null && mac.length() > 0) {
					appUser.setMac(mac);
				}
				if (gender != null && gender.length() > 0) {
					appUser.setGender(gender);
				}
				if (idfa != null && idfa.length() > 0) {
					appUser.setIdfa(idfa);
				}
				if (locale != null && locale.length() > 0) {
					appUser.setLocale(locale);
				}
				if (systemInfo != null && systemInfo.length() > 0) {
					appUser.setSystemInfo(systemInfo);
				}
				if (phoneId != null && phoneId.length() > 0) {
					appUser.setPhoneId(phoneId);
				}
				if (deviceId != null && deviceId.length() > 0) {
					appUser.setDeviceId(deviceId);
				}
				if (deviceType != null && deviceType.length() > 0) {
					appUser.setDeviceType(deviceType);
				}
				if (advertisingId != null && advertisingId.length() > 0) {
					appUser.setAdvertisingId(advertisingId);
				}
				if (androidDeviceToken != null && androidDeviceToken.length() > 0) {
					appUser.setAndroidDeviceToken(androidDeviceToken);
				}

				appUser = daoAppUser.createOrUpdate(appUser);

				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
						Application.USER_UPDATE_ACTIVITY + " " + Application.USER_SUCCESSFULLY_UPDATED + " "
								+ "phone number: " + phoneNumber + " successfully updated");

				// for app user object hide some data that we do not want to
				// push to the mobile app
				appUser.setPassword("");
				appUser.setSystemInfo("");
				// return user object
				ResponseLogin responseObject = new ResponseLogin();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setUserData(appUser);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				// return "{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+
				// "\"code\":\""+RespCodesEnum.OK+"\", "+
				// "\"userId\":\""+appUser.getId()+"\"}";
				return jsonResponseContent;// "{\"status\":\""+RespStatusEnum.SUCCESS+"\",
											// "+
											// "\"code\":\""+RespCodesEnum.OK+"\",
											// "+
											// "\"userId\":\""+appUser.getId()+"\"}";
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_UPDATE_ACTIVITY + " error updating appUser with following data: " + dataContent
							+ " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/loginUser/")
	public String loginUserWithQueryRouting(@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("password") String password,
			@QueryParam("iosDeviceToken") String iosDeviceToken,
			@QueryParam("androidDeviceToken") String androidDeviceToken, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " iosDeviceToken: " + iosDeviceToken
					+ " androidDeviceToken: " + androidDeviceToken + " ip: " + ipAddress + " phoneNumberExt: "
					+ phoneNumberExt + " phoneNumber: " + phoneNumber;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " received user login request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
						Application.USER_LOGIN_ACTIVITY + " " + Application.USER_TO_LOGIN_NOT_FOUND + " "
								+ "aborting user login-user with phone number: " + phoneNumber + " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";
			} else {
				if (isAccountDetailsInvalid(appUser)) {
					appUser = fixAccoutDetails(appUser);
				}

				// validate request
				RealmEntity realm = daoRealm.findById(appUser.getRealmId());
				// validate request hash

				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);
				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				String passwordHash = getPasswordHash(password);
				// String passwordHash = password;
				if (appUser.getPassword().equals(passwordHash)) {
					// return success with user details
					Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
							Application.USER_LOGIN_ACTIVITY + " " + Application.USER_SUCCESSFULLY_LOGGED + " "
									+ "phone number: " + phoneNumber + " successfully logged in");

					// update android and ios device tokens
					appUser.setiOSDeviceToken(iosDeviceToken);
					appUser.setAndroidDeviceToken(androidDeviceToken);
					daoAppUser.createOrUpdate(appUser);

					// for app user object hide some data that we do not want to
					// push to the mobile app
					appUser.setPassword("");
					appUser.setSystemInfo("");

					// set UI state controlling values
					UIStateHolder uiStateHolder = uiStateManager.getUIStateHolder(appUser, realm);
					// return user object
					ResponseLogin responseObject = new ResponseLogin();
					responseObject.setCode(RespCodesEnum.OK.toString());
					responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
					responseObject.setUserData(appUser);
					responseObject.setUiStateHolder(uiStateHolder);

					// serialize into string
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponseContent = gson.toJson(responseObject);

					spinnerManager.checkDailyBonus(miscData, appUser);
					if (appUser.getDeviceType() == null) {
						updateDeviceType(appUser);
					}
					return jsonResponseContent;

				} else {
					// user with given password not found
					Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
							Application.USER_LOGIN_ACTIVITY + " " + Application.USER_PASSWORD_DOES_NOT_MATCH + " "
									+ "password for phone number: " + phoneNumber + " does not match");

					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PASSWORD_NOT_FOUND + "\", " + "\"userId\":\""
							+ appUser.getId() + "\"}";
				}
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_LOGIN_ACTIVITY + " error updating appUser with following data: " + dataContent
							+ " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	private AppUserEntity fixAccoutDetails(AppUserEntity appUser) {
		logger.info("Fixing appUser with id: " + appUser.getId());
		try {
			if (appUser.getPhoneNumberExtension() == null || appUser.getPhoneNumberExtension().length() == 0) {
				appUser.setPhoneNumberExtension("91");
				if (appUser.getRewardTypeName() != null) {
					if (appUser.getRewardTypeName().equals("AirRewardz-Kenya")) {
						appUser.setPhoneNumberExtension("254");
					}
					if (appUser.getRewardTypeName().equals("AirRewardz-SouthAfrica")) {
						appUser.setPhoneNumberExtension("27");
					}
				}

				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
						Application.USER_DETAILS_FIX + " fixed rewardType for user: " + appUser.getId()
								+ " phone number ext: " + appUser.getPhoneNumberExtension());
			}

			if (appUser.getApplicationName() == null || appUser.getApplicationName().length() == 0) {
				if (appUser.getPhoneNumberExtension().contains("91")
						|| appUser.getPhoneNumberExtension().contains("254")
						|| appUser.getPhoneNumberExtension().contains("27"))
					appUser.setApplicationName("AirRewardz");

				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
						Application.USER_DETAILS_FIX + " fixed applicationName for user: " + appUser.getId());
			}
			if (appUser.getRewardTypeName() == null || appUser.getRewardTypeName().length() == 0) {
				if (appUser.getPhoneNumberExtension().contains("91")) {
					appUser.setRewardTypeName("AirRewardz-India");
				}
				if (appUser.getPhoneNumberExtension().contains("254")) {
					appUser.setRewardTypeName("AirRewardz-Kenya");
				}
				if (appUser.getPhoneNumberExtension().contains("27")) {
					appUser.setRewardTypeName("AirRewardz-SouthAfrica");
				}

				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
						Application.USER_DETAILS_FIX + " fixed rewardType for user: " + appUser.getId()
								+ " reward type: " + appUser.getRewardTypeName());
				appUser = daoAppUser.createOrUpdate(appUser);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_DETAILS_FIX + " error during fixing details for user: " + appUser.getId()
							+ exception.toString());

		}

		return appUser;

	}

	private boolean isAccountDetailsInvalid(AppUserEntity appUser) {
		if (appUser.getApplicationName() == null || appUser.getApplicationName().length() == 0) {
			return true;
		}
		if (appUser.getRewardTypeName() == null || appUser.getRewardTypeName().length() == 0) {
			return true;
		}
		if (appUser.getPhoneNumberExtension() == null || appUser.getPhoneNumberExtension().length() == 0) {
			return true;
		}

		return false;
	}

	private void updateDeviceType(AppUserEntity appUser) {
		try {
			appUser.setDeviceType("Android");
			daoAppUser.createOrUpdate(appUser);
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_LOGIN_ACTIVITY + " " + "cant update device type : " + exception.toString());

		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/changePassword/")
	public String changeUserPasswordWithQueryRouting(@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("oldPassword") String oldPassword,
			@QueryParam("newPassword") String newPassword, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " ip: " + ipAddress
					+ " phoneNumberExt: " + phoneNumberExt + " phoneNumber: " + phoneNumber;
			// " oldPassword: "+oldPassword+
			// " newPassword: "+newPassword;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.OK,
					Application.USER_UPDATE_ACTIVITY + " " + Application.USER_PASSWORD_UPDATE_ACTIVITY
							+ " received user edit request: " + dataContent);

			if (newPassword == null || newPassword.length() == 0) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_CURRENT_PASSWORD_MISMATCH + "\"}";
			}

			String oldPasswordHash = getPasswordHash(oldPassword);

			// String oldPasswordHash = oldPassword;
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
								Application.USER_UPDATE_ACTIVITY + " " + Application.USER_TO_UPDATE_NOT_FOUND + " "
										+ "aborting user update- user with phone number: " + phoneNumber
										+ " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";
			} else if (!oldPasswordHash.equals(appUser.getPassword())) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_CURRENT_PASSWORD_MISMATCH + "\"}";
			} else {
				// validate request
				RealmEntity realm = daoRealm.findById(appUser.getRealmId());
				// validate request hash
				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);
				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				String newPasswordHash = getPasswordHash(newPassword); // hash
																		// password
				// String newPasswordHash = newPassword;
				appUser.setPassword(newPasswordHash);
				appUser = daoAppUser.createOrUpdate(appUser);

				// --------------------------- send password change e-mail
				MailParamsHolder mailParamsHolder = new MailParamsHolder();
				mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
				mailParamsHolder.setEmailRecipientFullName(appUser.getFullName());
				mailParamsHolder.setEmailRecipientNewPassword(newPassword);
				mailManager.sendEmail(daoRealm.findById(appUser.getRealmId()), mailParamsHolder,
						EmailType.PASSWORD_CHANGE);

				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
						Application.USER_UPDATE_ACTIVITY + " " + Application.USER_SUCCESSFULLY_UPDATED + " "
								+ "phone number: " + phoneNumber + " successfully updated");
				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"}";
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_UPDATE_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_UPDATE_ACTIVITY + " error updating appUser with following data: " + dataContent
							+ " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/restorePassword/")
	public String restoreUserPasswordWithQueryRouting(@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " ip: " + ipAddress
					+ " phoneNumber: " + phoneNumber;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_RESTORE_PASSWORD_ACTIVITY, -1, LogStatus.OK,
					Application.USER_RESTORE_PASSWORD_ACTIVITY + " received user edit request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.USER_RESTORE_PASSWORD_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.USER_RESTORE_PASSWORD_ACTIVITY + " "
								+ "aborting password restore - user with phone number: " + phoneNumber
								+ " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";
			} else {
				// validate request
				RealmEntity realm = daoRealm.findById(appUser.getRealmId());
				// validate request hash
				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);
				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				/**
				 * code to restore password and send it via mail goes here
				 */
				String newPassword = System.currentTimeMillis() + "";
				newPassword = newPassword.substring(newPassword.length() - 5, newPassword.length());

				String newPasswordHash = getPasswordHash(newPassword); // hash
																		// password
				appUser.setPassword(newPasswordHash);
				appUser = daoAppUser.createOrUpdate(appUser);

				// --------------------------- send password restore e-mail
				MailParamsHolder mailParamsHolder = new MailParamsHolder();
				mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
				mailParamsHolder.setEmailRecipientFullName(appUser.getFullName());
				mailParamsHolder.setEmailRecipientNewPassword(newPassword); // send
																			// unhashed
																			// password
																			// here
				mailManager.sendEmail(daoRealm.findById(appUser.getRealmId()), mailParamsHolder,
						EmailType.PASSWORD_RECOVERY);

				Application.getElasticSearchLogger()
						.indexLog(Application.USER_RESTORE_PASSWORD_ACTIVITY, -1, LogStatus.OK,
								Application.USER_RESTORE_PASSWORD_ACTIVITY + " "
										+ "password for user with phone number: " + phoneNumber
										+ " successfully sent via e-mail");

				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"}";
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_RESTORE_PASSWORD_ACTIVITY, -1,
					LogStatus.ERROR, Application.USER_RESTORE_PASSWORD_ACTIVITY
							+ " error updating appUser with following data: " + dataContent + " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getClickHistory/")
	public String getClickHistory(@QueryParam("userId") int userId, @QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("email") String email,
			@QueryParam("deviceType") String deviceType, @QueryParam("locale") String locale,
			@QueryParam("hashkey") String hashkey, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " ip: " + ipAddress
					+ " phoneNumberExt: " + phoneNumberExt + " phoneNumber: " + phoneNumber + " email: " + email
					+ " deviceType: " + deviceType + " locale: " + locale + " systemInfo: " + systemInfo + " miscData: "
					+ miscData;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.USER_CLICK_HISTORY_REQUEST_ACTIVITY, -1,
					LogStatus.OK,
					Application.USER_CLICK_HISTORY_REQUEST_ACTIVITY + " received user login request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findById(userId);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.USER_CLICK_HISTORY_REQUEST_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.USER_LOGIN_ACTIVITY + " " + Application.USER_TO_LOGIN_NOT_FOUND + " "
								+ "aborting user with phone number: " + phoneNumber + " and id: " + userId
								+ " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_ID_NOT_FOUND + "\"}";
			} else {
				// validate request
				RealmEntity realm = daoRealm.findById(appUser.getRealmId());

				// validate request hash
				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);
				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(appUser.getId());
				ConversionHistoryHolder conversionHistoryHolder = null;
				if (conversionHistory != null) {
					conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
				}

				ResponseConversionHistory responseObject = new ResponseConversionHistory();

				if (conversionHistory == null) {
					responseObject.setCode(RespCodesEnum.OK.toString());
					responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
					responseObject.setConversionHistoryHolder(null);
				} else {
					responseObject.setCode(RespCodesEnum.OK.toString());
					responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
					responseObject.setConversionHistoryHolder(conversionHistoryHolder);
				}

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_CLICK_HISTORY_REQUEST_ACTIVITY, -1,
					LogStatus.ERROR, Application.USER_LOGIN_ACTIVITY + " error updating appUser with following data: "
							+ dataContent + " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}

	}

	public String getPasswordHash(String password) {
		String saltValue = "Salt string";
		return DigestUtils.sha1Hex(password + saltValue);
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/v1/ACRAError/")
	public Response handleAcraError(MultivaluedMap<String, String> formParams) {

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}

		Map<String, List<String>> paramsMap = new HashMap<String, List<String>>();
		paramsMap.putAll(formParams);
		crashReportManager.persistCrashReport(formParams, ipAddress);

		return Response.ok().build();

	}

	/*
	@GET
	@Produces("application/json")
	@Path("/v1/enableMobile/")
	public String enableMobile(@HeaderParam("user-agent") String userAgent,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("password") String password,
			@QueryParam("androidDeviceToken") String androidDeviceToken,
			@QueryParam("advertisingId") String advertisingId, @QueryParam("phoneId") String phoneId,
			@QueryParam("deviceId") String deviceId, @QueryParam("idfa") String idfa,
			@QueryParam("iOSDeviceToken") String iOSDeviceToken) {
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			String dataContent = "ipAddress:" + ipAddress + " userAgent: " + userAgent + " phoneNumber:" + phoneNumber
					+ " password:" + password + " androidDeviceToken:" + androidDeviceToken + " advertisingId: "
					+ advertisingId + " phoneId:" + phoneId + " deviceId:" + deviceId + " idfa:" + idfa
					+ " iOSDeviceToken: " + iOSDeviceToken;

			if (phoneNumber == null || phoneNumber.length() < 10) {
				Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1, LogStatus.ERROR,
						Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY + " " + " "
								+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + " (1)for data:"
								+ dataContent);
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";

			}

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1, LogStatus.ERROR,
						Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY + " " + " "
								+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + " (2)for data:"
								+ dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND + "\"}";
			} else {
				// validate request hash
				String passwordHash = getPasswordHash(password);
				// String passwordHash = password;
				if (appUser.getPassword().equals(passwordHash)) {
					MobileDetails details = DeviceInfoParser.parseUserAgent(userAgent,
							httpRequest.getServletContext().getResourceAsStream("/WEB-INF/regexes.yaml"));

					details.setAndroidDeviceToken(androidDeviceToken);
					details.setAdvertisingId(advertisingId);
					details.setDeviceId(deviceId);
					details.setPhoneId(phoneId);
					details.setIdfa(idfa);
					details.setiOSDeviceToken(iOSDeviceToken);
					details.setEnabled(true);

					if (details.getDeviceModel() == null) {
						Application.getElasticSearchLogger()
								.indexLog(Application.AFA_ENABLING_ACTIVITY, -1, LogStatus.ERROR,
										Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY
												+ " " + " " + RespCodesEnum.ERROR_INVALID_APPLICATION + " for data:"
												+ dataContent);
						return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
								+ RespCodesEnum.ERROR_INVALID_APPLICATION + "\"}";
					}
					SerDeMobileDetails serDeMobileDetails = new SerDeMobileDetails();

					List<MobileDetails> mobileDetailsList = serDeMobileDetails.deserialize(appUser.getMobileDetails());
					if (mobileDetailsList == null) {
						mobileDetailsList = new ArrayList<MobileDetails>();
					} else {
						for (MobileDetails mobileDetail : mobileDetailsList) {

							if (mobileDetail.getDeviceModel().equals(details.getDeviceModel())
									&& mobileDetail.getDeviceOSVersion().equals(details.getDeviceOSVersion())) {
								Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1,
										LogStatus.ERROR,
										Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY
												+ " " + " " + RespCodesEnum.ERROR_DEVICE_ALREADY_LISTED + " for data:"
												+ dataContent);
								return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
										+ RespCodesEnum.ERROR_DEVICE_ALREADY_LISTED + "\"}";
							}

						}

					}

					List<MobileDetails> mobileDetailsArrayList = new ArrayList<MobileDetails>();
					for (MobileDetails mdetails : mobileDetailsList) {
						mobileDetailsArrayList.add(mdetails);
					}

					mobileDetailsArrayList.add(details);

					String json = serDeMobileDetails.serialize(mobileDetailsArrayList);
					appUser.setMobileDetails(json);
					if (appUser.getAndroidDeviceToken() == null)
						appUser.setAndroidDeviceToken(androidDeviceToken);

					if (appUser.getAdvertisingId() == null)
						appUser.setAdvertisingId(advertisingId);

					if (appUser.getDeviceId() == null)
						appUser.setDeviceId(deviceId);

					if (appUser.getPhoneId() == null)
						appUser.setPhoneId(phoneId);

					if (appUser.getIdfa() == null)
						appUser.setIdfa(idfa);

					if (appUser.getiOSDeviceToken() == null)
						appUser.setiOSDeviceToken(iOSDeviceToken);

					daoAppUser.createOrUpdate(appUser);

					Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1, LogStatus.OK,
							Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY + " " + " "
									+ RespCodesEnum.OK + " for data:" + dataContent);
					return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK
							+ "\"}";
				} else {
					Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1,
							LogStatus.ERROR,
							Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY + " " + " "
									+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PASSWORD_NOT_FOUND + " for data:"
									+ dataContent);
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_USER_WITH_GIVEN_PASSWORD_NOT_FOUND + "\"}";
				}

			}
		} catch (Exception exc) {

			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.AFA_ENABLING_ACTIVITY, -1, LogStatus.ERROR,
					Application.AFA_ENABLING_ACTIVITY + " " + Application.AFA_ENABLING_ACTIVITY + " " + " "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + Arrays.toString(exc.getStackTrace()));
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}
	}
	*/

	@GET
	@Produces("application/json")
	@Path("/v1/loginUserWithDeviceInfo/")
	public String loginUserWithDeviceInfoWithQueryRouting(@QueryParam("deviceId") String deviceId,
			@QueryParam("phoneId") String phoneId, @QueryParam("mac") String mac,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("hashkey") String hashkey, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("androidDeviceToken") String androidDeviceToken,
			@QueryParam("applicationName") String applicationName) {

		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " phoneId: " + phoneId
					+ " deviceId: " + deviceId + " mac:" + mac + " applicationName:" + applicationName + " ipAddress: "
					+ ipAddress;
			logger.info(dataContent);
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
					Application.USER_LOGIN_ACTIVITY + " received user login request: " + dataContent);

			if (deviceId == null && phoneId == null && mac == null && applicationName == null) {
				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
						Application.USER_LOGIN_ACTIVITY + " " + Application.USER_TO_LOGIN_NOT_FOUND + " "
								+ "aborting user login-user " + dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"}";

			}
			

			
			AppUserEntity appUser = null;
			
			deviceId = deviceId.replaceAll("\\s+","");
			phoneId = phoneId.replaceAll("\\s+","");
			
			Application.getElasticSearchLogger().indexLog(Application.GET_DEVICE_DATA, -1, LogStatus.OK,
					Application.GET_DEVICE_DATA + "deviceId: " + deviceId + " length: " + deviceId.length() + 
					"phoneId: " +phoneId  +" length: " + phoneId.length() );
			
			if (deviceId != null && deviceId.length() > 0 && appUser == null) {
				appUser = daoAppUser.findByDeviceIdAndApplicationName(deviceId, applicationName);
				Application.getElasticSearchLogger().indexLog(Application.GET_DEVICE_DATA, -1, LogStatus.OK,
						Application.GET_DEVICE_DATA + " (LOGIN) Searching with deviceId. Data : (" + dataContent
								+ ") Result user: " + (appUser != null ? appUser.getId() : " empty"));
			}
			if (phoneId != null && phoneId.length() > 0  && appUser == null) {
				appUser = daoAppUser.findByPhoneIdAndApplicationName(phoneId, applicationName);
				Application.getElasticSearchLogger().indexLog(Application.GET_DEVICE_DATA, -1, LogStatus.OK,
						Application.GET_DEVICE_DATA + " (LOGIN) Searching with phoneId. Data : (" + dataContent
								+ ") Result user: " + (appUser != null ? appUser.getId() : " empty"));
			}

			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
						Application.USER_LOGIN_ACTIVITY + " " + Application.USER_TO_LOGIN_NOT_FOUND + " "
								+ "aborting user login-user with phone number: " + phoneNumber + " not found in DB");

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_NOT_FOUND + "\"}";
			} else {

				RealmEntity realm = daoRealm.findById(appUser.getRealmId());
				boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
						hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo,
						miscData, ipAddress);

				if (!isRequestValid) {
					return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
							+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"" + appUser.getId()
							+ "\"}";
				}

				Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.OK,
						Application.USER_LOGIN_ACTIVITY + " " + Application.USER_SUCCESSFULLY_LOGGED + " "
								+ "phone number: " + phoneNumber + " successfully logged in");

				appUser.setAndroidDeviceToken(androidDeviceToken);
				daoAppUser.createOrUpdate(appUser);

				appUser.setSystemInfo("");

				UIStateHolder uiStateHolder = uiStateManager.getUIStateHolder(appUser, realm);
				ResponseLogin responseObject = new ResponseLogin();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setUserData(appUser);
				responseObject.setUiStateHolder(uiStateHolder);

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;

			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_LOGIN_ACTIVITY, -1, LogStatus.ERROR,
					Application.USER_LOGIN_ACTIVITY + " error updating appUser with following data: " + dataContent
							+ " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/isUserRegisteredWithDevice/")
	public String isUserRegisteredWithDevice(@QueryParam("deviceId") String deviceId,
			@QueryParam("phoneId") String phoneId, @QueryParam("mac") String mac,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("androidDeviceToken") String androidDeviceToken,
			@QueryParam("applicationName") String applicationName) {

		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " miscData: " + miscData + " systemInfo: " + systemInfo + " phoneId: " + phoneId
					+ " deviceId: " + deviceId + " mac:" + mac + " applicationName:" + applicationName + " ipAddress: "
					+ ipAddress;
			logger.info(dataContent);
			System.out.println(dataContent);
			Application.getElasticSearchLogger().indexLog(Application.USER_DEVICE_REGISTRATION_CHECK, -1, LogStatus.OK,
					Application.USER_DEVICE_REGISTRATION_CHECK + " received user device registration check request: "
							+ dataContent);

			if ((deviceId == null || deviceId.length() == 0) && (phoneId == null || phoneId.length() == 0)
					&& (mac == null || mac.length() == 0)
					&& (applicationName == null || applicationName.length() == 0)) {
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_DEVICE_REGISTRATION_CHECK, -1, LogStatus.ERROR,
								Application.USER_DEVICE_REGISTRATION_CHECK + " " + Application.USER_TO_LOGIN_NOT_FOUND
										+ " " + "aborting device checking as no tracking details identified: "
										+ dataContent);

				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_INVALID_USER_DATA + "\"}";
			}

			AppUserEntity appUser = null;
			if (deviceId != null && deviceId.length() > 0 && appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.GET_DEVICE_DATA, -1, LogStatus.OK,
						Application.GET_DEVICE_DATA + " (REG_CHECK) Searching with deviceId. Data : (" + dataContent
								+ ") Result user: " + (appUser != null ? appUser.getId() : " empty"));
				appUser = daoAppUser.findByDeviceIdAndApplicationName(deviceId, applicationName);
			}
			if (phoneId != null && phoneId.length() > 0 && appUser == null) {
				appUser = daoAppUser.findByPhoneIdAndApplicationName(phoneId, applicationName);
				Application.getElasticSearchLogger().indexLog(Application.GET_DEVICE_DATA, -1, LogStatus.OK,
						Application.GET_DEVICE_DATA + " (REG_CHECK) Searching with phoneId. Data : (" + dataContent
								+ ") Result user: " + (appUser != null ? appUser.getId() : " empty"));
			}
			/*
			 * if (mac != null && mac.length() > 0 && appUser == null) { appUser
			 * = daoAppUser.findByMacAndApplicationName(mac, applicationName);
			 * Application.getElasticSearchLogger().indexLog(Application.
			 * GET_DEVICE_DATA, -1, LogStatus.OK, Application.GET_DEVICE_DATA +
			 * " (REG_CHECK) Searching with mac. Data : (" + dataContent +
			 * ") Result user: " + (appUser != null ? appUser.getId() : " empty"
			 * )); }
			 */
			if (appUser == null) {
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_DEVICE_REGISTRATION_CHECK, -1, LogStatus.OK,
								Application.USER_DEVICE_REGISTRATION_CHECK + " "
										+ Application.USER_DEVICE_REGISTRATION_CHECK + " "
										+ " there is no user with device details ");
				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"}";
			} else {
				Application.getElasticSearchLogger()
						.indexLog(Application.USER_DEVICE_REGISTRATION_CHECK, -1, LogStatus.ERROR,
								Application.USER_DEVICE_REGISTRATION_CHECK + " "
										+ Application.USER_DEVICE_REGISTRATION_CHECK + " "
										+ " aborting, there is user with device details ");
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_USER_UNDER_GIVEN_PHONE_NUMBER_ALREADY_REGISTERED + "\"}";
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_DEVICE_REGISTRATION_CHECK, -1,
					LogStatus.ERROR, Application.USER_DEVICE_REGISTRATION_CHECK + " error  with following data: "
							+ dataContent + " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}
	}

	@GET
	@Produces("text/html")
	@Path("/v1/activateAccount/")
	public String activateAccount(@QueryParam("email") String email, @QueryParam("activationCode") String code) {
		String message = "";
		String dataContent = "";
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = "email: " + email + " activationCode:" + code + " ipAddress:" + ipAddress;
			if (email != null && email.length() > 0) {
				AppUserEntity appUser = daoAppUser.findByEmail(email);
				if (appUser != null) {
					if (appUser.getActivationCode() != null && appUser.getActivationCode().length() > 0) {
						if (appUser.getActivationCode().equals(code)) {
							message = "Account activated. You can use refer option now.";
							appUser.setActivationCode("");
							daoAppUser.createOrUpdate(appUser);
						} else {
							message = "Invalid code. Please try again.";
						}
					} else {
						message = "Your account is already activated.";
					}
				} else {
					message = "Invalid account. Please try again.";
				}

			} else {
				message = "Invalid account.Please try again.";
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			message = "Unexpected error occured. Please try again later";
		}

		Application.getElasticSearchLogger().indexLog(Application.USER_ACCOUNT_ACTIVATION_ACTIVITY, -1, LogStatus.OK,
				Application.USER_ACCOUNT_ACTIVATION_ACTIVITY + "actvation for: " + dataContent + " result: " + message);

		return message;
	}

}