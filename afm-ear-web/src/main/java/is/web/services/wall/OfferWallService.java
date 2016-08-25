package is.web.services.wall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.offerWall.RealtimeFeedDataHolder;
import is.ejb.bl.offerWall.RealtimeFeedGenerator;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIValidator;
import is.web.services.ResponseMultiOfferWall;
import is.web.services.ResponseOWIds;
import is.web.services.wall.validators.UserValidator;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class OfferWallService {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOOfferWall daoOfferWall;

	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private RealtimeFeedGenerator realtimeFeedGenerator;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private HashValidationManager hashValidationManager;

	@Inject
	private UserValidator userValidator;

	@Inject
	private APIHelper apiHelper;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/offerwalls/")
	public String getTargetedWallIds(final APIRequestDetails details) {
		OfferWallIdsResponse response = new OfferWallIdsResponse();
		try {

			String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " "
							+ " identifying available offer walls for request: " + details + "ipAddress: " + ipAddress);
			if (!userValidator.validate(details.getParameters())) {
				apiHelper.setupFailedResponseForError(response, userValidator.getInvalidValueErrorCode());
			} else {
				String userId = (String) details.getParameters().get("userId");
				AppUserEntity appUser = daoAppUser.findById(Integer.valueOf(userId));

				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1,
						LogStatus.ERROR, Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " "
								+ "aborting as no user found under following request: " + userId);

				List<OfferWallEntity> listOffers = daoOfferWall.findAllByRealmIdAndActiveAndCountryAndDevice(
						appUser.getRealmId(), true, appUser.getCountryCode(), appUser.getDeviceType(),
						appUser.getRewardTypeName());

				if (listOffers == null) {
					listOffers = new ArrayList<OfferWallEntity>();
				}
				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " "
								+ "selected offer walls:" + listOffers.size());

				apiHelper.setupSuccessResponse(response);
				response.setIds(getWallIds(listOffers));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}

		return apiHelper.getGson().toJson(response);

	}

	private List<Integer> getWallIds(List<OfferWallEntity> offerWallList) {
		List<Integer> idList = new ArrayList<Integer>();
		for (OfferWallEntity offerWall : offerWallList) {
			idList.add(offerWall.getId());
		}
		return idList;
	}

	/**
	 * This method returns offer walls that are personalized for individual user
	 * based on his previous download history
	 */
	@GET
	@Produces("application/json")
	@Path("/offerwall/{id}/")
	public String getCompositeOfferWallContent(@PathParam("id") String offerWallId, @QueryParam("userId") String userId,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("applicationInfo") String applicationInfo) {

		String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
		Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.OK,
				Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
						+ " Retrieving offer wall with id: " + offerWallId + " applicationInfo: " + applicationInfo
						+ " userId : " + userId + " systemInfo: " + systemInfo + " ip: " + ipAddress);

		logger.info("got request: " + httpRequest);
		logger.info("got ip: " + ipAddress);

		try {
			// get user by id
			AppUserEntity appUser = null;
			appUser = daoAppUser.findById(Integer.valueOf(userId));
			if (appUser == null) { // user already registered!
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
								+ " Unable to identify offer wall with provided id: " + offerWallId
								+ " please make sure that offer id is correct" + " status: "
								+ RespStatusEnum.FAILED.toString() + " error code: "
								+ RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());

				// return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;
			}

			// get realm by key
			RealmEntity realm = daoRealm.findById(appUser.getRealmId());
			if (realm == null) {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
								+ " Unable to identify network, please make sure that network with provided name is correct "
								+ " status: " + RespStatusEnum.FAILED.toString() + " error code: "
								+ RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());

				// return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
			}

			// validate request hash
			/*
			 * boolean isRequestValid =
			 * hashValidationManager.isRequestValid(realm.getApiKey(), hashkey,
			 * hashValidationManager.getFullURL(httpRequest), phoneNumber,
			 * phoneNumberExt, systemInfo, miscData, ipAddress); if
			 * (!isRequestValid) { return "{\"status\":\"" +
			 * RespStatusEnum.FAILED + "\", " + "\"code\":\"" +
			 * RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED + "\", " +
			 * "\"userId\":\"-1\"}"; }
			 */

			OfferWallEntity offerWall = daoOfferWall.findById(Integer.valueOf(offerWallId));
			OfferWallContent offerWallContent = null;
			if (offerWall != null) {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(),
						LogStatus.OK,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " "
								+ Application.COW_SELECTION_BY_ID_IDENTIFIED + " " + " selecting offer wall with id: "
								+ offerWallId + " userId: " + userId + " deviceType: " + appUser.getDeviceType()
								+ " for realm: " + realm.getName() + " network id: " + realm.getId());
				logger.info("COW_SELECTION selecting offer wall with id: " + offerWallId + " for realm: "
						+ realm.getName());

				// create wall selection log
				logger.info("Indexing wall selection");
				Application.getElasticSearchLogger().indexWallSelection(realm.getName(), Integer.valueOf(userId),
						appUser.getEmail(), appUser.getPhoneNumber(), appUser.getPhoneNumberExtension(),
						Integer.valueOf(offerWallId), offerWall.getRewardTypeName(),
						offerWall.getTargetCountriesFilter(), offerWall.getTargetDevicesFilter(), appUser.getLocale(),
						appUser.getIdfa(), ipAddress, systemInfo, applicationInfo);
				logger.info("Indexed wall selection");
				// filter offer wall and remove offers that are already
				// converted by user
				// offerWallContent =
				// filterOutAlreadyConvertedByUserApps(appUser, offerWall);
				// if offer wall is empty - return it and device should not
				// display it!
				/**
				 * dynamically attach real-time feed (offer wall contains only
				 * anchor to include that feed but its generated in real time
				 * during this request
				 */
				// https://geo.tp-cdn.com/api/offer/v1/?vic=f53667b888d5a293ebbc42723926aaf9&sid=528340&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+8_3+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12F70&ip=86.154.102.160&;idfa=5E2ECC3A-83AD-4A26-A65C-8A65EB31EB5B&num_offers=100
				RealtimeFeedDataHolder realtimeFeedDataHolder = new RealtimeFeedDataHolder();
				// overriden values for testing
				// ua =
				// "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+8_3+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12F70";
				// ipAddress="86.154.102.160";
				// idfa="5E2ECC3A-83AD-4A26-A65C-8A65EB31EB5B";

				realtimeFeedDataHolder.setGaid(URLEncoder.encode(appUser.getAdvertisingId(), "UTF-8"));
				realtimeFeedDataHolder.setIdfa(URLEncoder.encode("", "UTF-8"));
				realtimeFeedDataHolder.setIp(URLEncoder.encode(ipAddress, "UTF-8"));
				realtimeFeedDataHolder.setUa(URLEncoder.encode("", "UTF-8"));
				realtimeFeedDataHolder.setUserId(URLEncoder.encode(userId + "", "UTF-8"));
				realtimeFeedDataHolder.setDeviceType(URLEncoder.encode(appUser.getDeviceType(), "UTF-8"));
				offerWallContent = realtimeFeedGenerator.composeOfferWall(offerWall, realtimeFeedDataHolder, true);

				// filter offer wall and remove offers that are already
				// converted by user
				offerWallContent = filterOutAlreadyConvertedByUserApps(appUser, offerWall);

				// count how many offers are returned
				int returnedOffers = 0;
				for (int i = 0; i < offerWallContent.getOfferWalls().size(); i++) {
					IndividualOfferWall wall = offerWallContent.getOfferWalls().get(i);
					if (wall != null) {
						returnedOffers = returnedOffers + wall.getOffers().size();
					}
				}

				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.OK,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " returning wall: "
								+ offerWallContent.getCompositeOfferWallName() + " individual walls number: "
								+ offerWallContent.getOfferWalls().size() + " returned_offers: " + returnedOffers
								+ " for user with id: " + userId + " ip: " + ipAddress);

				// return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setMultiOfferWall(offerWallContent);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
			} else { // no offer wall with id found
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
						Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
								+ " Unable to identify offer wall with provided id: " + offerWallId
								+ " please make sure that offer id is correct" + " status: "
								+ RespStatusEnum.FAILED.toString() + " error code: "
								+ RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());

				// return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				// serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " Error selecting offer: " + exc.toString() + " status: "
							+ RespStatusEnum.FAILED.toString() + " error code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

			// return user object
			ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
			responseObject.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			responseObject.setStatus(RespStatusEnum.FAILED.toString());
			responseObject.setMultiOfferWall(null);

			// serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);
			return jsonResponseContent;
		}
	}

	private OfferWallContent filterOutAlreadyConvertedByUserApps(AppUserEntity appUser, OfferWallEntity offerWall) {
		OfferWallContent offerWallContent = null;
		try {
			// filter through offers
			offerWallContent = serDeOfferWallContent.deserialize(offerWall.getContent());
			// retrieve download history for this user
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(appUser.getId());
			if (conversionHistory == null) {
				return offerWallContent; // exit as user has not generated
											// conversion history yet
			}
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();

			ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries = conversionHistoryHolder
					.getListConversionHistoryEntries();
			ArrayList<IndividualOfferWall> listIndividualOfferWalls = offerWallContent.getOfferWalls();
			for (int i = 0; i < listIndividualOfferWalls.size(); i++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(i);
				ArrayList<Offer> listOffers = individualOfferWall.getOffers();
				for (int k = listOffers.size() - 1; k >= 0; k--) {
					Offer offer = listOffers.get(k);
					// logger.info("checking offer: "+offer.getTitle());
					if (isOfferConvertedByUser(offerWall.getRealm().getId(), offer, listUserConversionHistoryEntries)) {
						listOffers.remove(k);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " Error selecting offer: " + e.toString() + " status: " + RespStatusEnum.FAILED.toString()
							+ " error code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
		}
		return offerWallContent;
	}

	private boolean isOfferConvertedByUser(int realmId, Offer offer,
			ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries) {

		for (int i = 0; i < listUserConversionHistoryEntries.size(); i++) {
			ConversionHistoryEntry conversionEntry = listUserConversionHistoryEntries.get(i);
			// logger.info("comparing: "+offer.getTitle()+"
			// "+offer.getSourceId()+" "+offer.getAdProviderCodeName()+" --- "+
			// conversionEntry.getOfferTitle()+"
			// "+conversionEntry.getSourceOfferId()+"
			// "+conversionEntry.getAdProviderCodeName());
			// logger.info("converison entry: "+conversionEntry);
			try {
				if (conversionEntry != null && conversionEntry.getConversionTimestamp() != null
						&& conversionEntry.getSourceOfferId() != null
						&& conversionEntry.getSourceOfferId().equals(offer.getSourceId())
						&& conversionEntry.getAdProviderCodeName() != null
						&& conversionEntry.getAdProviderCodeName().equals(offer.getAdProviderCodeName())) {

					Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realmId,
							LogStatus.OK,
							Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " "
									+ Application.OFFER_REJECTED_AS_ALREADY_CONVERTED + " " + " rejected offer: "
									+ offer.getTitle() + " add provder: " + offer.getAdProviderCodeName()
									+ " as it was already converted by used at time: "
									+ conversionEntry.getConversionTimestamp().toString());

					return true;
				}
			} catch (Exception exc) {
				logger.severe("Error in conversion entry when identifying already converted offers when comparing: "
						+ offer.getTitle() + " " + offer.getSourceId() + " " + offer.getAdProviderCodeName() + " --- "
						+ conversionEntry.getOfferTitle() + " " + conversionEntry.getSourceOfferId() + " "
						+ conversionEntry.getAdProviderCodeName() + " " + exc.toString());
				// exc.printStackTrace();

				return true;
			}
		}

		return false;
	}

}