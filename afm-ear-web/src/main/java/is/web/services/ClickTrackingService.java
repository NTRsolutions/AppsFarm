package is.web.services;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.codec.digest.DigestUtils;

import com.gargoylesoftware.htmlunit.javascript.host.media.GainNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple REST service which is able to say hello to someone using
 * HelloService Please take a look at the web.xml where JAX-RS is enabled
 */

@Path("/")
public class ClickTrackingService {
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAODenominationModel daoDenominationModel;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private HashValidationManager hashValidationManager;

	@Inject
	private TestManager testManager;

	@GET
	@Produces("application/json")
	@Path("/v1/daotest/")
	public String testDao() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -100);
		Date startTime = cal.getTime();
		Date endTime = new Date();
		long totalClick = daoUserEvent.countTotalClicksForRewardTypeInDateRange(startTime, endTime, "AirRewardz-India");
		long totalUniqueClick = daoUserEvent.countTotalUniqueClicksForRewardTypeInDateRange(startTime, endTime,
				"AirRewardz-India");
		long totalConversions = daoUserEvent.countTotalConversionsForRewardTypeInDateRange(startTime, endTime,
				"AirRewardz-India");

		System.out.println("**********");
		System.out.println("TOTAL CLICK: " + totalClick );
		System.out.println("TOTAL UNIQUE CLICK: " + totalUniqueClick);
		System.out.println(" TOTAL CONVERSIONS: " + totalConversions);
		System.out.println("**********");
		return "OK";
	}

	@GET
	@Produces("application/json")
	@Path("/v1/click/")
	public String registerEventWithQueryRouting(@QueryParam("userId") int userId,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("countryCode") String countryCode, @QueryParam("deviceType") String deviceType,
			@QueryParam("deviceId") String deviceId, @QueryParam("phoneId") String phoneId,
			@QueryParam("advertisingId") String advertisingId, @QueryParam("idfa") String idfa,
			@QueryParam("iosDeviceToken") String iosDeviceToken,
			@QueryParam("androidDeviceToken") String androidDeviceToken,
			@QueryParam("afaNetworkName") String afaNetworkName, @QueryParam("internalNetworkId") int internalNetworkId,
			@QueryParam("offerId") String offerId, @QueryParam("offerTitle") String offerTitle,
			@QueryParam("offerSourceId") String offerSourceId, @QueryParam("applicationName") String applicationName,
			@QueryParam("rewardType") String rewardType, @QueryParam("payoutValue") double payoutValue,
			@QueryParam("payoutInTargetCurrency") double payoutInTargetCurrency,
			@QueryParam("payoutCurrency") String payoutCurrency, @QueryParam("rewardValue") double rewardValue,
			@QueryParam("rewardCurrency") String rewardCurrency,
			@QueryParam("revenueSplitValue") double revenueSplitValue, @QueryParam("profitValue") double profitValue,
			@QueryParam("url") String url, @QueryParam("adProviderCodeName") String adProviderCodeName,
			@QueryParam("affiliateId") String affiliateId, @QueryParam("carrierName") String carrierName,
			@QueryParam("instantReward") boolean instantReward, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("email") String email) {

		String responseMessage = "";
		int realmId = -1;
		if (rewardType.equals("Cinetreats-GB") || rewardType.equals("Cinetreats-AU")
				|| rewardType.toLowerCase().contains("cine")) {
			applicationName = "Cinetreats";
		}
		if (rewardType.equals("Trippa-GB") || rewardType.equals("Trippa-GB-iOS")
				|| rewardType.toLowerCase().contains("trippa")) {
			applicationName = "GoAhead";
		}

		// create offer object based on click data
		Offer offer = new Offer();
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

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			if (offerId == null || offerId.length() == 0 || offerSourceId == null || offerSourceId.length() == 0
					|| payoutCurrency == null || payoutCurrency.length() == 0 || rewardCurrency == null
					|| rewardCurrency.length() == 0 || url == null || url.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, -1, LogStatus.ERROR,
						Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_OFFER_NOT_FOUND + " userId: " + userId + " deviceId: " + deviceId
								+ " phoneId: " + phoneId + " idfa: " + idfa + " advertisingId: " + advertisingId
								+ " phoneNumber: " + phoneNumber + " phoneNumberExt: " + phoneNumberExt
								+ " iosDeviceToken: " + iosDeviceToken + " andriodDeviceToken: " + androidDeviceToken
								+ " countryCode: " + countryCode + " miscData: " + miscData + " offer title: "
								+ offerTitle + " offer id: " + offerId + " offer affiliate id: " + affiliateId
								+ " offer source id: " + offerSourceId + " offer reward type: " + rewardType
								+ " offer reward currency: " + rewardCurrency + " offer payout value: " + payoutValue
								+ " offer payout in target currency: " + payoutInTargetCurrency
								+ " offer payout currency: " + payoutCurrency + " offer reward value: " + rewardValue
								+ " offer ad provider: " + adProviderCodeName + " offer revenue split value: "
								+ revenueSplitValue + " offer provit value: " + profitValue + " offer url: " + url
								+ " applicationName: " + applicationName + " instantReward: " + instantReward
								+ " miscData: " + miscData + " systemInfo: " + systemInfo + " carrier: " + carrierName
								+ " email: " + email + " ip: " + ipAddress);

				responseMessage = "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_OFFER_NOT_FOUND + "\"}";
				return responseMessage;
			}

			// TODO bypass older app versions that still have no wallet
			if (applicationName == null || applicationName.length() == 0) {
				instantReward = true;
			}

			String dataContent = " networkId: " + offer.getInternalNetworkId() + " userId: " + userId + " userId: "
					+ userId + " deviceId: " + deviceId + " phoneId: " + phoneId + " idfa: " + idfa + " advertisingId: "
					+ advertisingId + " phoneNumber: " + phoneNumber + " phoneNumberExt: " + phoneNumberExt
					+ " iosDeviceToken: " + iosDeviceToken + " andriodDeviceToken: " + androidDeviceToken
					+ " countryCode: " + countryCode + " miscData: " + miscData + " offer title: " + offerTitle
					+ " offer id: " + offerId + " offer source id: " + offerSourceId + " offer reward type: "
					+ rewardType + " offer reward currency: " + rewardCurrency + " offer payout value: " + payoutValue
					+ " offer payout in target currency: " + payoutInTargetCurrency + " offer payout currency: "
					+ payoutCurrency + " offer reward value: " + rewardValue + " offer revenue split value: "
					+ revenueSplitValue + " offer provit value: " + profitValue + " offer ad provider: "
					+ adProviderCodeName + " offer affiliate id: " + affiliateId + " offer url: " + url
					+ " applicationName: " + applicationName + " instantReward: " + instantReward + " miscData: "
					+ miscData + " systemInfo: " + systemInfo + " email: " + email + " ip: " + ipAddress;

			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, -1, LogStatus.OK,
					Application.CLICK_ACTIVITY + " " + Application.CLICK_IDENTIFIED + " received request: "
							+ dataContent);

			// get realmId via apiKey
			RealmEntity realm = daoRealm.findById(offer.getInternalNetworkId());
			if (realm == null) {
				Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, LogStatus.ERROR,
						Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND
								+ " error: Please make sure that the provided networkId parameter value is correct");

				responseMessage = "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND + "\"}";

				return responseMessage; // "{\"response\":\" status:
										// "+RespStatusEnum.FAILED+" code:
										// "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND+"
										// error: Please make sure that the
										// provided networkId parameter value is
										// correct. \"}";
			}

			// validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
					hashValidationManager.getFullURL(httpRequest), phoneNumber, phoneNumberExt, systemInfo, miscData,
					ipAddress);
			if (!isRequestValid) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " + "\"userId\":\"-1\"}";
			}

			realmId = realm.getId();
			if (offer.getCurrency() == null || offer.getCurrency().length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, LogStatus.ERROR,
						Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_MISSING_OFFER_CURRENCY_CODE
								+ " error: Offer does not contain currency code, current currency code: "
								+ offer.getCurrency());
				responseMessage = "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_MISSING_OFFER_CURRENCY_CODE + "\"}";
				return responseMessage;// "{\"response\":\" status:
										// "+RespStatusEnum.FAILED+" code:
										// "+RespCodesEnum.ERROR_MISSING_OFFER_CURRENCY_CODE+"
										// error: Offer does not contain
										// currency code, current currency code:
										// "+offer.getPayoutIsoCurrencyCode()+"\"}";
			}

			// validate if provided rewardTypeName is correct and enlisted in
			// DenominationModel
			int numberOfRegisteredDenominationModels = daoDenominationModel
					.getRegisteredDenominationModelsNumberByRewardTypeNameAndRealmId(offer.getRewardType(), realmId);
			if (numberOfRegisteredDenominationModels <= 0) {
				Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, LogStatus.ERROR,
						Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_NO_DENOMINATION_MODEL_REGISTERED_FOR_GIVEN_REWARD_TYPE_NAME
								+ " error: Please make sure that there exists appropriate Denomination Model registered with reward type name matching: "
								+ offer.getRewardType());
				responseMessage = "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_NO_DENOMINATION_MODEL_REGISTERED_FOR_GIVEN_REWARD_TYPE_NAME + "\"}";
				return responseMessage; // "{\"response\":\" status:
										// "+RespStatusEnum.FAILED+" code:
										// "+RespCodesEnum.ERROR_NO_DENOMINATION_MODEL_REGISTERED_FOR_GIVEN_REWARD_TYPE_NAME+"
										// error: Please make sure that there
										// exists appropriate Denomination Model
										// registered with reward type name
										// matching: "+rewardTypeName+"\"}";
			}

			// generate unique internal transaction id used by our system to
			// track offer conversion
			String internalTransactionId = DigestUtils.sha1Hex(offer.getInternalNetworkId() + userId
					+ Math.random() * 100000 + System.currentTimeMillis() + phoneNumber + offer.getId());
			if (internalTransactionId.length() > 32) {
				internalTransactionId = internalTransactionId.substring(0, 31); // make
																				// sure
																				// that
																				// transaction
																				// id
																				// is
																				// no
																				// longer
																				// than
																				// 32
																				// characters
			}

			// make sure no other event exists with internal transactionId - if
			// error is found here - regenerate the id once more
			if (daoUserEvent.findByInternalTransactionId(internalTransactionId) != null) {
				Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(),
						LogStatus.ERROR,
						Application.CLICK_ACTIVITY + " internalT: " + internalTransactionId + " status: "
								+ RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_DUPLICATE_TRANSACTION_IDENTIFIED
								+ " error: System was unable to generate unique internal transaction id");
				responseMessage = "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
						+ RespCodesEnum.ERROR_DUPLICATE_TRANSACTION_IDENTIFIED + "\"}";
				return responseMessage;// "{\"response\":\" status:
										// "+RespStatusEnum.FAILED+" code:
										// "+RespCodesEnum.ERROR_DUPLICATE_TRANSACTION_IDENTIFIED+"
										// error: System was unable to generate
										// unique internal transaction id\"}";
			}

			/**
			 * This is where url is augmented with additional parameters
			 * required by offer providers
			 */
			String saltedOfferUrl = "";
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
							"&source=" + URLEncoder.encode("BPM", "UTF-8") + // &source={source}
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
							+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id="
							+ URLEncoder.encode("2", "UTF-8") + // URLEncoder.encode(offer.getAffiliateId(),
					// "UTF-8")+ //TODO
					// to fix as
					// affiliate id is
					// not passed
					"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&google_aid="
							+ URLEncoder.encode(advertisingId, "UTF-8");
				} else {
					saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
							+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id="
							+ URLEncoder.encode("2", "UTF-8") + // URLEncoder.encode(offer.getAffiliateId(),
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
							+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id="
							+ URLEncoder.encode("2", "UTF-8") + // URLEncoder.encode(offer.getAffiliateId(),
					// "UTF-8")+ //TODO
					// to fix as
					// affiliate id is
					// not passed
					"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&google_aid="
							+ URLEncoder.encode(advertisingId, "UTF-8");
				} else {
					saltedOfferUrl = "http://airrewardz.go2cloud.org/aff_c?offer_id="
							+ URLEncoder.encode(offer.getSourceId(), "UTF-8") + "&aff_id="
							+ URLEncoder.encode("2", "UTF-8") + // URLEncoder.encode(offer.getAffiliateId(),
					// "UTF-8")+ //TODO
					// to fix as
					// affiliate id is
					// not passed
					"&aff_sub2=" + URLEncoder.encode(internalTransactionId, "UTF-8") + "&ios_ifa="
							+ URLEncoder.encode(idfa, "UTF-8");
				}
			} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.AARKI.toString())) {
				if (deviceType.toLowerCase().equals("android")) {
					saltedOfferUrl = offer.getUrl() + "&click_label="
							+ URLEncoder.encode(internalTransactionId, "UTF-8") + "&device_id="
							+ URLEncoder.encode(deviceId, "UTF-8") + "&phone_id=" + URLEncoder.encode(phoneId, "UTF-8")
							+ "&advertising_id=" + URLEncoder.encode(advertisingId, "UTF-8");
				} else {
					saltedOfferUrl = offer.getUrl() + "&click_label="
							+ URLEncoder.encode(internalTransactionId, "UTF-8") + "&advertising_id="
							+ URLEncoder.encode(idfa, "UTF-8");
				}
			} else if (offer.getAdProviderCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())) {
				saltedOfferUrl = offer.getUrl() + "&clickid=" + URLEncoder.encode(internalTransactionId, "UTF-8");
			}

			// generate event object and pesrsist it in db
			UserEventEntity event = new UserEventEntity();
			event.setUserId(userId);
			event.setDeviceType(deviceType);
			event.setDeviceId(deviceId);
			event.setAdvertisingId(advertisingId);
			event.setIdfa(idfa);
			event.setCarrierName(carrierName);
			event.setIosDeviceToken(iosDeviceToken);
			event.setAndroidDeviceToken(androidDeviceToken);
			event.setInternalTransactionId(internalTransactionId);
			event.setOfferRedirectUrl(saltedOfferUrl); // augment this url based
														// on offer provider!
			event.setPhoneNumber(phoneNumber);
			event.setPhoneNumberExt(phoneNumberExt);
			event.setRewardTypeName(offer.getRewardType());
			event.setAdProviderCodeName(offer.getAdProviderCodeName());
			event.setOfferTitle(offer.getTitle());
			event.setOfferId(offer.getId());
			event.setOfferSourceId(offer.getSourceId().toLowerCase());
			event.setRealmId(realm.getId());

			event.setOfferPayout(offer.getPayout());
			event.setOfferPayoutInTargetCurrency(offer.getPayoutInTargetCurrency());
			event.setOfferPayoutIsoCurrencyCode(offer.getCurrency().toUpperCase());
			event.setAfaNetworkName(afaNetworkName);
			event.setRewardIsoCurrencyCode(offer.getRewardCurrency());
			event.setRewardValue(offer.getRewardValue());
			event.setProfilSplitFraction(offer.getRevenueSplitValue());
			event.setProfitValue(offer.getProfitValue());
			event.setRevenueValue(offer.getRevenueSplitValue());
			event.setClickDate(new Timestamp(System.currentTimeMillis()));
			event.setCountryCode(countryCode);
			event.setUserEventCategory(UserEventCategory.INSTALL.toString());
			event.setInstant(instantReward);
			event.setIpAddress(ipAddress);
			event.setApplicationName(applicationName);
			event.setEmail(email);

			// set custom reward value and currency
			if (applicationName.toLowerCase().equals("GoAhead".toLowerCase())
					|| applicationName.toLowerCase().equals("Cinetreats".toLowerCase())
					|| applicationName.toLowerCase().contains("goahead")
					|| applicationName.toLowerCase().contains("cine")) {
				// set custom reward valu and currency for notifications and for
				// zendesk communication

				event.setCustomRewardValue(offer.getRewardValue());
				event.setCustomRewardCurrencyCode(offer.getRewardCurrency());

				// calculate real reward / revenue values for used internally
				// for statistics
				try {
					double monetaryRewardValue = (event.getCustomRewardValue() / (double) 100) * (double) 1.4;
					monetaryRewardValue = round(monetaryRewardValue, 2);
					double monetaryRevenueValue = event.getOfferPayout() - monetaryRewardValue;
					monetaryRevenueValue = round(monetaryRevenueValue, 2);

					// offer payout remains in the same currency as the original
					// offer payout from provider (GBP)
					event.setOfferPayoutInTargetCurrency(event.getOfferPayout());
					event.setOfferPayoutInTargetCurrencyIsoCurrencyCode("USD");
					event.setRewardValue(monetaryRewardValue);
					event.setRevenueValue(monetaryRevenueValue);
					event.setRewardIsoCurrencyCode("USD");
					double profitVal = event.getOfferPayout() - event.getRewardValue();
					profitVal = round(profitVal, 2);
					event.setProfitValue(profitVal);

				} catch (Exception exc) {
					Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(),
							LogStatus.ERROR,
							Application.CLICK_ACTIVITY
									+ " error calculating real reward value / profit for trippa and cinetreats: "
									+ exc.toString());

					logger.severe("error calculating real reward value / profit for trippa and cinetreats: "
							+ exc.toString());
					exc.printStackTrace();
				}
			}

			// check if running in test mode
			boolean isTestModeEnabled = testManager.isTestModeEnabledForRewardType(realm, event);
			event.setTestMode(isTestModeEnabled);
			// create event
			daoUserEvent.create(event);

			// create user click log
			Application.getElasticSearchLogger().indexUserClick(realmId, phoneNumber, email, deviceType, offer.getId(),
					offer.getSourceId().toLowerCase() + "", offer.getTitle(), offer.getAdProviderCodeName(),
					offer.getRewardType(), event.getOfferPayoutInTargetCurrency(), event.getRewardValue(),
					event.getOfferPayoutInTargetCurrencyIsoCurrencyCode(), event.getProfitValue(), realm.getName(),
					event.getOfferRedirectUrl(), UserEventType.click.toString(), event.getInternalTransactionId(),
					event.getCarrierName(), event.getUserEventCategory(), miscData, systemInfo, ipAddress,
					event.getCountryCode(), instantReward, applicationName, advertisingId, idfa, event.isTestMode(),
					event.getCustomRewardValue(), event.getCustomRewardCurrencyCode());

			String clickEventLogContent = "internalT: " + internalTransactionId + " registered user click: networkId: "
					+ offer.getInternalNetworkId() + " userId: " + userId + " phoneNumber: " + phoneNumber
					+ " offerId: " + offer.getId() + " redirect url: " + event.getOfferRedirectUrl();

			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(), LogStatus.OK,
					Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.SUCCESS + " code: "
							+ RespCodesEnum.OK_NO_CONTENT + " content: " + clickEventLogContent);
			logger.info(clickEventLogContent);

			// update conversion history (needed to filter out already clicked
			// offers for particular user)
			updateUserConversionHistory(event);

			// ------------------- testing mode that triggers conversion
			// ------------------------
			if (isTestModeEnabled) {
				testManager.triggerTestModeForUserOfferClick(realm, event);
			}

			/*
			 * if(realm.isTestMode() ||
			 * event.getRewardTypeName().equals("Trippa-GB")) {
			 * if(realm.isTestMode() ||
			 * event.getRewardTypeName().equals("Trippa-GB")) { //generate
			 * unique internal transaction id used by our system to track offer
			 * conversion String providerTransactionId =
			 * DigestUtils.sha1Hex(offer.getInternalNetworkId()+ userId+
			 * Math.random()*100000+ System.currentTimeMillis()+ phoneNumber);
			 * 
			 * HttpURLConnection urlConnection = null; BufferedReader in = null;
			 * try { //String urlParameters =
			 * realm.getTestModeUrl()+"/ab/svc/v1/conversion/"
			 * +event.getInternalTransactionId()+"/"+providerTransactionId;
			 * String urlParameters = realm.getTestModeUrl()+
			 * "/ab/svc/v1/conversion?internalTransactionId="
			 * +event.getInternalTransactionId()+"&offerProviderTransactionId="+
			 * providerTransactionId;
			 * 
			 * URL testUrl = new URL(urlParameters); urlConnection =
			 * (HttpURLConnection)testUrl.openConnection();
			 * urlConnection.setConnectTimeout(realm.getConnectionTimeout() *
			 * 1000); urlConnection.setReadTimeout(realm.getReadTimeout() *
			 * 1000); in = new BufferedReader( new InputStreamReader(
			 * urlConnection.getInputStream())); String reqResponse = ""; String
			 * inputLine; while ((inputLine = in.readLine()) != null) {
			 * reqResponse = inputLine; } } finally { if(in != null) {
			 * in.close(); } if(urlConnection != null) {
			 * urlConnection.disconnect(); } } }
			 */
			// ------------------- testing ends ------------------------

			// return url to redirect user to
			responseMessage = "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK
					+ "\", " + "\"url\":\"" + event.getOfferRedirectUrl() + "\", " + "\"transactionId\":\""
					+ internalTransactionId + "\"}";

			return responseMessage;// "{\"response\":\" status:
									// "+RespStatusEnum.SUCCESS+" code:
									// "+RespCodesEnum.OK_NO_CONTENT+"\"}";

		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, LogStatus.ERROR,
					Application.CLICK_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());

			return "{\"result\":\" status: " + RespStatusEnum.FAILED + " code: "
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString() + "\"}";
		}
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
