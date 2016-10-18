package is.web.services.wall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.rewardSystems.mode.TestModeManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.testing.TestManager;
import is.ejb.bl.wallet.WalletManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIValidator;
import is.web.services.wall.validators.AdProviderValidator;
import is.web.services.wall.validators.AdvertisingValidator;
import is.web.services.wall.validators.ApplicationValidator;
import is.web.services.wall.validators.DenominationModelValidator;
import is.web.services.wall.validators.DeviceTypeValidator;
import is.web.services.wall.validators.DeviceValidator;
import is.web.services.wall.validators.OfferSourceValidator;
import is.web.services.wall.validators.OfferValidator;
import is.web.services.wall.validators.PayoutCurrencyValidator;
import is.web.services.wall.validators.PhoneValidator;
import is.web.services.wall.validators.RealmValidator;
import is.web.services.wall.validators.RewardCurrencyValidator;
import is.web.services.wall.validators.RewardTypeValidator;
import is.web.services.wall.validators.URLValidator;
import is.web.services.wall.validators.UserValidator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;

import com.gargoylesoftware.htmlunit.javascript.host.media.GainNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple REST service which is able to say hello to someone using
 * HelloService Please take a look at the web.xml where JAX-RS is enabled
 */

@Path("/")
public class ClickService {
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;


	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private TestManager testManager;

	@Inject
	private UserValidator userValidator;
	@Inject
	private URLValidator urlValidator;
	@Inject
	private RewardCurrencyValidator rewardCurrencyValidator;
	@Inject
	private PayoutCurrencyValidator payoutCurrencyValidator;
	@Inject
	private OfferValidator offerValidator;
	@Inject
	private OfferSourceValidator offerSourceValidator;
	@Inject
	private RealmValidator realmValidator;
	@Inject
	private RewardTypeValidator rewardTypeValidator;
	@Inject
	private AdProviderValidator adProviderValidator;
	@Inject
	private ApplicationValidator applicationValidator;
	@Inject
	private DeviceTypeValidator deviceTypeValidator;
	@Inject
	private PhoneValidator phoneValidator;
	@Inject
	private DeviceValidator deviceValidator;
	@Inject
	private AdvertisingValidator advertisingValidator;
	@Inject
	private DenominationModelValidator denominationModelValidator;
	@Inject
	private APIHelper apiHelper;



	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/offer/click/")
	public String registerClick(final APIRequestDetails apiRequestDetails) {

		ClickResponse response = new ClickResponse();
		int realmId = -1;
		try {
			String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, -1, LogStatus.OK,
					Application.CLICK_ACTIVITY + " Received click from ip: " + ipAddress + " : " + apiRequestDetails);
			logger.info(" Received click from ip: " + ipAddress + " : " + apiRequestDetails);

			HashMap<String, Object> parameters = apiRequestDetails.getParameters();
			for (APIValidator validator : getClickValidators()) {
				if (validator.validate(parameters)) {
					logger.info("Validator: " + validator.getClass() + " OK");
				} else {
					logger.info("Validator: " + validator.getClass() + " FAILED");

					Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, -1, LogStatus.OK,
							Application.CLICK_ACTIVITY + "Validator: " + validator.getClass().toString()
									+ " FAILED for request: " + apiRequestDetails);

					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					return apiHelper.getGson().toJson(response);
				}
			}

			Offer offer = setupOfferFromRequestParameters(parameters);
			RealmEntity realm = daoRealm.findById(offer.getInternalNetworkId());
			realmId = realm.getId();
			String internalTransactionId = generateInternalTransactionId(parameters);
			String saltedOfferUrl = setupSaltedOfferUrl(offer, parameters, internalTransactionId);
			
			UserEventEntity event = setupUserEvent(offer, parameters);
			event.setInternalTransactionId(internalTransactionId);
			event.setOfferRedirectUrl(saltedOfferUrl);
			event.setRealmId(realmId);
			event.setIpAddress(ipAddress);
			
			boolean isTestModeEnabled = testManager.isTestModeEnabledForRewardType(realm, event);
			logger.info("Checking test mode: " +realm + " result" +isTestModeEnabled);
			event.setTestMode(isTestModeEnabled);
			daoUserEvent.create(event);

			logClickInElastic(event, parameters, ipAddress);
			updateUserConversionHistory(event);

			if (isTestModeEnabled) {
				testManager.triggerTestModeForUserOfferClick(realm, event);
			}
			logClick(parameters, offer, realm, internalTransactionId, event);
			
			apiHelper.setupSuccessResponse(response);
			response.setInternalTransactionId(internalTransactionId);
			response.setUrl(saltedOfferUrl);

			return apiHelper.getGson().toJson(response);

		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, LogStatus.ERROR,
					Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());

			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return apiHelper.getGson().toJson(response);
		}
	}

	
	

	private List<APIValidator> getClickValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(urlValidator);
		validators.add(userValidator);
		validators.add(rewardCurrencyValidator);
		validators.add(payoutCurrencyValidator);
		validators.add(offerValidator);
		validators.add(offerSourceValidator);
		validators.add(realmValidator);
		validators.add(rewardTypeValidator);
		validators.add(adProviderValidator);
		validators.add(applicationValidator);
		validators.add(deviceTypeValidator);
		validators.add(phoneValidator);
		validators.add(deviceValidator);
		validators.add(advertisingValidator);
		validators.add(denominationModelValidator);
		return validators;
	}
	
	private void logClick(HashMap<String, Object> parameters, Offer offer, RealmEntity realm,
			String internalTransactionId, UserEventEntity event) {
		String clickEventLogContent = "internalT: " + internalTransactionId + " registered user click: networkId: "
				+ offer.getInternalNetworkId() + " userId: " + parameters.get("userId") + " phoneNumber: "
				+ parameters.get("phoneNumber") + " offerId: " + offer.getId() + " redirect url: "
				+ event.getOfferRedirectUrl();

		Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(), LogStatus.OK,
				Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.SUCCESS + " code: "
						+ RespCodesEnum.OK_NO_CONTENT + " content: " + clickEventLogContent);
		logger.info(clickEventLogContent);
	}

	private void logClickInElastic(UserEventEntity event, HashMap<String, Object> parameters, String ipAddress) {
		Application.getElasticSearchLogger().indexUserClick(event.getRealmId(), event.getPhoneNumber(),
				event.getEmail(), event.getDeviceType(), event.getOfferId(),
				event.getOfferSourceId().toLowerCase() + "", event.getOfferTitle(), event.getAdProviderCodeName(),
				event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
				event.getOfferPayoutInTargetCurrencyIsoCurrencyCode(), event.getProfitValue(), "" + event.getRealmId(),
				event.getOfferRedirectUrl(), UserEventType.click.toString(), event.getInternalTransactionId(),
				event.getCarrierName(), event.getUserEventCategory(), (String) parameters.get("applicationInfo"),
				(String) parameters.get("systemInfo"), ipAddress, event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), event.getAdvertisingId(), event.getIdfa(), event.isTestMode(),
				event.getCustomRewardValue(), event.getCustomRewardCurrencyCode());
	}

	private UserEventEntity setupUserEvent(Offer offer, HashMap<String, Object> parameters) {
		// generate event object and pesrsist it in db
		UserEventEntity event = new UserEventEntity();
		Integer userId = parameters.containsKey("userId") ? (Integer) parameters.get("userId") : null;
		String deviceType = parameters.containsKey("deviceType") ? (String) parameters.get("deviceType") : null;
		String deviceId = parameters.containsKey("deviceId") ? (String) parameters.get("deviceId") : null;
		String advertisingId = parameters.containsKey("advertisingId") ? (String) parameters.get("advertisingId")
				: null;
		String idfa = parameters.containsKey("idfa") ? (String) parameters.get("idfa") : null;
		String carrierName = parameters.containsKey("carrierName") ? (String) parameters.get("carrierName") : null;
		String iosDeviceToken = parameters.containsKey("iosDeviceToken") ? (String) parameters.get("iosDeviceToken")
				: null;
		String androidDeviceToken = parameters.containsKey("androidDeviceToken")
				? (String) parameters.get("androidDeviceToken") : null;
		String phoneNumber = parameters.containsKey("phoneNumber") ? (String) parameters.get("phoneNumber") : null;
		String phoneNumberExt = parameters.containsKey("phoneNumberExt") ? (String) parameters.get("phoneNumberExt")
				: null;
		String countryCode = parameters.containsKey("countryCode") ? (String) parameters.get("countryCode") : null;
		String applicationName = parameters.containsKey("applicationName") ? (String) parameters.get("applicationName")
				: null;
		String email = parameters.containsKey("email") ? (String) parameters.get("email") : null;
		Boolean instantReward = parameters.containsKey("instantReward") ? (Boolean) parameters.get("instantReward")
				: null;
		event.setUserId(userId);
		event.setDeviceType(deviceType);
		event.setDeviceId(deviceId);
		event.setAdvertisingId(advertisingId);
		event.setIdfa(idfa);
		event.setCarrierName(carrierName);
		event.setIosDeviceToken(iosDeviceToken);
		event.setAndroidDeviceToken(androidDeviceToken);
		// event.setInternalTransactionId(internalTransactionId);
		// event.setOfferRedirectUrl(saltedOfferUrl); // augment this url based
		// on offer provider!
		event.setPhoneNumber(phoneNumber);
		event.setPhoneNumberExt(phoneNumberExt);
		event.setRewardTypeName(offer.getRewardType());
		event.setAdProviderCodeName(offer.getAdProviderCodeName());
		event.setOfferTitle(offer.getTitle());
		event.setOfferId(offer.getId());
		event.setOfferSourceId(offer.getSourceId().toLowerCase());
		// event.setRealmId(realmId);

		event.setOfferPayout(offer.getPayout());
		event.setOfferPayoutInTargetCurrency(offer.getPayoutInTargetCurrency());
		event.setOfferPayoutIsoCurrencyCode(offer.getCurrency().toUpperCase());

		event.setRewardIsoCurrencyCode(offer.getRewardCurrency());
		event.setRewardValue(offer.getRewardValue());
		event.setProfilSplitFraction(offer.getRevenueSplitValue());
		event.setProfitValue(offer.getProfitValue());
		event.setRevenueValue(offer.getRevenueSplitValue());
		event.setClickDate(new Timestamp(System.currentTimeMillis()));
		event.setCountryCode(countryCode);
		event.setUserEventCategory(UserEventCategory.INSTALL.toString());
		event.setInstant(instantReward);
		// event.setIpAddress(ipAddress);
		event.setApplicationName(applicationName);
		event.setEmail(email);
		return event;
	}

	private String setupSaltedOfferUrl(Offer offer, HashMap<String, Object> parameters, String internalTransactionId)
			throws Exception {
		/**
		 * This is where url is augmented with additional parameters required by
		 * offer providers
		 */

		String saltedOfferUrl = "";
		Integer userId = (Integer) parameters.get("userId");
		String advertisingId = (String) parameters.get("advertisingId");
		String deviceType = (String) parameters.get("deviceType");
		String idfa = (String) parameters.get("idfa");
		String applicationName = (String) parameters.get("applicationName");
		String deviceId = (String) parameters.get("deviceId");
		String phoneId = (String) parameters.get("phoneId");

		if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())) {
			// http://tracking.vcommission.com/aff_c?offer_id=230&aff_id=41707&aff_sub=Your_SubID_Here
			saltedOfferUrl = offer.getUrl() + URLEncoder.encode(userId + "", "UTF-8");
			saltedOfferUrl = saltedOfferUrl + "&pub_id=" + URLEncoder.encode(internalTransactionId, "UTF-8");
			if (deviceType.toLowerCase().equals("android")) {
				saltedOfferUrl = saltedOfferUrl + "&google_aid=" + URLEncoder.encode(advertisingId, "UTF-8");
			} else {
				saltedOfferUrl = saltedOfferUrl + "&idfa=" + URLEncoder.encode(idfa, "UTF-8");
			}
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())) {
			// http://tracking.vcommission.com/aff_c?offer_id=230&aff_id=41707&aff_sub=Your_SubID_Here
			saltedOfferUrl = offer.getUrl() + "&aff_sub=" + URLEncoder.encode(internalTransactionId, "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())) {
			if (deviceType.equals("Android")) {
				// android
				// &aff_sub5=37498&google_aid={google_aid}&source={source}&aff_sub={transaction_id}&aff_sub2={adv_sub}
				saltedOfferUrl = offer.getUrl() + "&aff_sub5=" + URLEncoder.encode("37498", "UTF-8") + // aff_sub5=37498
						"&google_aid=" + URLEncoder.encode(advertisingId, "UTF-8") + // google_aid={google_aid}
						"&aff_sub={transaction_id}" + // &aff_sub={transaction_id}
						//"&source=" + URLEncoder.encode("BPM", "UTF-8") + // &source={source}
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8");
			} else {
				// ios
				// &aff_sub5=37498&ios_ifa={ios_ifa}&source={source}&aff_sub={transaction_id}&aff_sub2={adv_sub}
				saltedOfferUrl = offer.getUrl() + "&aff_sub5=" + URLEncoder.encode("37498", "UTF-8") + // aff_sub5=37498
						"&ios_ifa=" + URLEncoder.encode(idfa, "UTF-8") + // google_aid={google_aid}
						"&aff_sub={transaction_id}" + // &aff_sub={transaction_id}
						"&source=" + URLEncoder.encode("BPM", "UTF-8") + // &source={source}
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8");
			}
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())) {
			saltedOfferUrl = offer.getUrl() + "&sid=" + URLEncoder.encode(userId + "", "UTF-8") + "&transaction_id="
					+ URLEncoder.encode(internalTransactionId, "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())) {
			saltedOfferUrl = offer.getUrl() + "&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())) {
			saltedOfferUrl = offer.getUrl() + "&subid=" + URLEncoder.encode(internalTransactionId, "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())) {
			saltedOfferUrl = offer.getUrl() + "&ref=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&_src="
					+ URLEncoder.encode(offer.getRewardType(), "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())) {
			saltedOfferUrl = offer.getUrl() + "&ref=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&_src="
					+ URLEncoder.encode(offer.getRewardType(), "UTF-8");
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())) {
			saltedOfferUrl = offer.getUrl();
			// cater for user_id and supply our own user id
			saltedOfferUrl = saltedOfferUrl.replace("[USER_ID]",
					URLEncoder.encode(internalTransactionId + "", "UTF-8"));
			// cater for IDFA for iOS
			saltedOfferUrl = saltedOfferUrl.replace("{ifa}", URLEncoder.encode(idfa, "UTF-8"));
			// cater for advertising id
			saltedOfferUrl = saltedOfferUrl.replace("{advertisingid}", URLEncoder.encode(advertisingId, "UTF-8"));
		} else if (!applicationName.toLowerCase().equals("AirRewardz".toLowerCase()) // for
																						// AdJockey
				&& offer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())) {
			if (deviceType.toLowerCase().equals("android")) {
				saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
						+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id=" + URLEncoder.encode("2", "UTF-8")
						+ // URLEncoder.encode(offer.getAffiliateId(),
						// "UTF-8")+ //TODO
						// to fix as
						// affiliate id is
						// not passed
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&google_aid="
						+ URLEncoder.encode(advertisingId, "UTF-8");
			} else {
				saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
						+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id=" + URLEncoder.encode("2", "UTF-8")
						+ // URLEncoder.encode(offer.getAffiliateId(),
						// "UTF-8")+ //TODO
						// to fix as
						// affiliate id is
						// not passed
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&ios_ifa="
						+ URLEncoder.encode(idfa, "UTF-8");
			}
		} else if (applicationName.toLowerCase().equals("AirRewardz".toLowerCase()) // for
																					// AdBroker
				&& offer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())) {
			if (deviceType.toLowerCase().equals("android")) {
				saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
						+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id=" + URLEncoder.encode("2", "UTF-8")
						+ // URLEncoder.encode(offer.getAffiliateId(),
						// "UTF-8")+ //TODO
						// to fix as
						// affiliate id is
						// not passed
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&google_aid="
						+ URLEncoder.encode(advertisingId, "UTF-8");
			} else {
				saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
						+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id=" + URLEncoder.encode("2", "UTF-8")
						+ // URLEncoder.encode(offer.getAffiliateId(),
						// "UTF-8")+ //TODO
						// to fix as
						// affiliate id is
						// not passed
						"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&ios_ifa="
						+ URLEncoder.encode(idfa, "UTF-8");
			}
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.AARKI.toString())) {
			if (deviceType.toLowerCase().equals("android")) {
				saltedOfferUrl = offer.getUrl() + "&click_label=" + URLEncoder.encode(internalTransactionId, "UTF-8")
						+ "&device_id=" + URLEncoder.encode(deviceId, "UTF-8") + "&phone_id="
						+ URLEncoder.encode(phoneId, "UTF-8") + "&advertising_id="
						+ URLEncoder.encode(advertisingId, "UTF-8");
			} else {
				saltedOfferUrl = offer.getUrl() + "&click_label=" + URLEncoder.encode(internalTransactionId, "UTF-8")
						+ "&advertising_id=" + URLEncoder.encode(idfa, "UTF-8");
			}
		} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())) {
			saltedOfferUrl = offer.getUrl() + "&clickid=" + URLEncoder.encode(internalTransactionId, "UTF-8");
		}
		return saltedOfferUrl;
	}

	private String generateInternalTransactionId(HashMap<String, Object> parameters) {
		String internalTransactionId = "";
		boolean uniqueTransactionId = false;
		while (!uniqueTransactionId) {
			internalTransactionId = DigestUtils.sha1Hex((Integer) parameters.get("internalNetworkId")
					+ (Integer) parameters.get("userId") + Math.random() * 100000 + System.currentTimeMillis()
					+ (String) parameters.get("rewardType") + (String) parameters.get("offerId"));
			if (internalTransactionId.length() > 32) {
				internalTransactionId = internalTransactionId.substring(0, 31);
			}
			if (isInternalTransactionIdUnique(internalTransactionId)) {
				uniqueTransactionId = true;
			}
		}

		return internalTransactionId;
	}

	private boolean isInternalTransactionIdUnique(String internalTransactionId) {
		try {
			if (daoUserEvent.findByInternalTransactionId(internalTransactionId) == null) {
				return true;
			}
			return false;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}

	private Offer setupOfferFromRequestParameters(HashMap<String, Object> parameters) {
		// create offer object based on click data
		Offer offer = new Offer();
		Integer internalNetworkId = parameters.containsKey("internalNetworkId")
				? (Integer) parameters.get("internalNetworkId") : null;
		String offerId = parameters.containsKey("offerId") ? (String) parameters.get("offerId") : null;
		String offerSourceId = parameters.containsKey("offerSourceId") ? (String) parameters.get("offerSourceId")
				: null;
		String rewardType = parameters.containsKey("rewardType") ? (String) parameters.get("rewardType") : null;
		String rewardCurrency = parameters.containsKey("rewardCurrency") ? (String) parameters.get("rewardCurrency")
				: null;
		Double payoutValue = parameters.containsKey("payoutValue") ? (Double) parameters.get("payoutValue") : null;
		Double payoutInTargetCurrency = parameters.containsKey("payoutInTargetCurrency")
				? (Double) parameters.get("payoutInTargetCurrency") : null;
		String payoutCurrency = parameters.containsKey("payoutCurrency") ? (String) parameters.get("payoutCurrency")
				: null;
		Double rewardValue = parameters.containsKey("rewardValue") ? (Double) parameters.get("rewardValue") : null;
		Double revenueSplitValue = parameters.containsKey("revenueSplitValue")
				? (Double) parameters.get("revenueSplitValue") : null;
		Double profitValue = parameters.containsKey("profitValue") ? (Double) parameters.get("profitValue") : null;
		String url = parameters.containsKey("url") ? (String) parameters.get("url") : null;
		String adProviderCodeName = parameters.containsKey("adProviderCodeName")
				? (String) parameters.get("adProviderCodeName") : null;
		String offerTitle = parameters.containsKey("offerTitle") ? (String) parameters.get("offerTitle") : null;
		String affiliateId = parameters.containsKey("affiliateId") ? (String) parameters.get("affiliateId") : null;
		offer.setInternalNetworkId(internalNetworkId);
		offer.setId(offerId);
		offer.setSourceId(offerSourceId);
		offer.setRewardType(rewardType);
		offer.setRewardCurrency(rewardCurrency);
		offer.setPayout(payoutValue);
		offer.setPayoutInTargetCurrency(payoutInTargetCurrency);
		offer.setCurrency(payoutCurrency);
		offer.setRewardValue(rewardValue);
		offer.setRevenueSplitValue(revenueSplitValue);
		offer.setProfitValue(profitValue);
		offer.setUrl(url);
		offer.setAdProviderCodeName(adProviderCodeName);
		offer.setTitle(offerTitle);
		offer.setAffiliateId(affiliateId);
		return offer;
	}

	/**
	 * use ConversionHistory to store click events for specific user
	 */
	private void updateUserConversionHistory(UserEventEntity event) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.OK,
					Application.CLICK_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE + " "
							+ " adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId());
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
			if (conversionHistory == null) {
				conversionHistory = new ConversionHistoryEntity();
			}

			conversionHistory.setUserId(event.getUserId());
			conversionHistory.setRealmId(event.getRealmId());
			conversionHistory.setGenerationTime(new Timestamp(System.currentTimeMillis()));

			// create new entry for this conversion
			ConversionHistoryEntry newConversionHistoryEntry = new ConversionHistoryEntry();
			newConversionHistoryEntry.setAdProviderCodeName(event.getAdProviderCodeName());
			newConversionHistoryEntry.setApproved(false);
			newConversionHistoryEntry.setClickTimestamp(event.getClickDate());
			newConversionHistoryEntry.setInternalTransactionId(event.getInternalTransactionId());
			newConversionHistoryEntry.setOfferId(event.getOfferId());
			newConversionHistoryEntry.setOfferTitle(event.getOfferTitle());
			newConversionHistoryEntry.setRewardCurrency(event.getRewardIsoCurrencyCode());
			newConversionHistoryEntry.setRewardTypeName(event.getRewardTypeName());
			if (event.getApplicationName().toLowerCase().contains("goahead")
					|| event.getApplicationName().toLowerCase().contains("cine")) {
				newConversionHistoryEntry.setRewardValue(event.getCustomRewardValue());
				newConversionHistoryEntry.setRewardCurrency(event.getCustomRewardCurrencyCode());

			} else {
				newConversionHistoryEntry.setRewardValue(event.getRewardValue());

			}
			newConversionHistoryEntry.setSourceOfferId(event.getOfferSourceId());
			newConversionHistoryEntry.setUserEventCategory(UserEventCategory.INSTALL.toString());
			// add to the existing conversion history list of this user
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
			conversionHistoryHolder.getListConversionHistoryEntries().add(0, newConversionHistoryEntry);
			// persist in db (dao takes care of json serialisation)
			daoConversionHistory.createOrUpdate(conversionHistory);
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(),
					LogStatus.ERROR,
					Application.CLICK_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE + " "
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE
							+ " error adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
		}
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}
}
