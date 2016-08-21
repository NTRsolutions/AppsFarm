package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.crashReport.CrashReportManager;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.NotificationType;
import is.ejb.bl.offerProviders.snapdeal.SnapdealCategoriesCommissionHolder;
import is.ejb.bl.offerProviders.snapdeal.SnapdealCategoryCommision;
import is.ejb.bl.offerProviders.snapdeal.SnapdealManager;
import is.ejb.bl.offerProviders.snapdeal.SnapdealReportResponse;
import is.ejb.bl.offerProviders.snapdeal.SnapdealReportType;
import is.ejb.bl.offerProviders.snapdeal.SnapdealTopProductsManager;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.CategoryOffers;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.referral.ReferralManager;
import is.ejb.bl.reporting.ReportDH;
import is.ejb.bl.reporting.ReportPeriodName;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.system.security.KeyGenerator;
import is.ejb.bl.system.support.donky.DonkyForwardRequest;
import is.ejb.bl.system.support.donky.DonkyManager;
import is.ejb.bl.timers.TimerReporting;
import is.ejb.bl.uiStateManager.UIStateHolder;
import is.ejb.bl.uiStateManager.UIStateManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOSnapdealOffers;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.SnapdealOffersEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.util.WebResources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.Startup;
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
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.mail.handlers.message_rfc822;

/**
 * A simple REST service which is able to say hello to someone using
 * HelloService Please take a look at the web.xml where JAX-RS is enabled
 */

@Path("/")
public class SnapdealService {
	@Inject
	private Logger logger;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private HashValidationManager hashValidationManager;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private DAOSnapdealOffers daoSnapdealOffers;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private SnapdealManager snapdealManager;

	@Inject
	private SnapdealCategoriesCommissionHolder snapdealCategoriesCommissionHolder;

	@Inject
	private SnapdealTopProductsManager snapdealTopProductsManager;

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealCategoriesTest/")
	public String getSnapdealCategoriesTest() {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " ip: " + ipAddress;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORIES + " received request: " + dataContent);

			// return categories in json format
			List<SnapdealOffersEntity> listAllOffers = daoSnapdealOffers.findAll();
			ArrayList<String> listAllCategories = new ArrayList<String>();
			for (int i = 0; i < listAllOffers.size(); i++) {
				SnapdealOffersEntity offer = listAllOffers.get(i);
				listAllCategories.add(offer.getCategoryName());
			}
			RealmEntity realm = daoRealm.findByName("BPM");
			// filter ( promote categories)
			String categoryConfiguration = realm.getSnapdealCategoryConfiguration();
			logger.info("Category configuration " + categoryConfiguration);
			if (categoryConfiguration != null && categoryConfiguration.length() > 0) {
				List<String> promotedCategoriesList = new Gson().fromJson(categoryConfiguration,
						new TypeToken<List<String>>() {
						}.getType());
				if (promotedCategoriesList != null && promotedCategoriesList.size() > 0) {
					ArrayList<String> originalList = (ArrayList<String>) listAllCategories.clone();
					logger.info("Filtering promoted categories ); " + promotedCategoriesList.size());
					ArrayList<String> filteredCategoriesList = new ArrayList<String>();
					for (String promotedCategory : promotedCategoriesList)
						for (String category : listAllCategories) {
							{
								if (promotedCategory.toLowerCase().equals(category.toLowerCase())) {
									logger.info("Adding filtered category: " + category);
									filteredCategoriesList.add(category);
									originalList.remove(category);
								}
							}
						}

					filteredCategoriesList.addAll(originalList);
					listAllCategories = filteredCategoriesList;
				}
			}

			// return user object
			SnapdealGetCategories responseObject = new SnapdealGetCategories();
			responseObject.setCode(RespCodesEnum.OK.toString());
			responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
			responseObject.setCategoriesList(listAllCategories);

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
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORIES + " error with following data: " + dataContent + " "
							+ exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealCategories/")
	public String getSnapdealCategories(@QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " phoneNumber: " + phoneNumber + " phoneNumberExtension: " + phoneNumberExt + " systemInfo: "
					+ systemInfo + " miscData: " + miscData + " systemInfo: " + systemInfo + " ip: " + ipAddress;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORIES + " received request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_GET_CATEGORIES + " " + "aborting user with phone number: " + phoneNumber
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

				// return categories in json format
				List<SnapdealOffersEntity> listAllOffers = daoSnapdealOffers.findAll();
				ArrayList<String> listAllCategories = new ArrayList<String>();
				for (int i = 0; i < listAllOffers.size(); i++) {
					SnapdealOffersEntity offer = listAllOffers.get(i);
					listAllCategories.add(offer.getCategoryName());
				}

				// filter ( promote categories)
				String categoryConfiguration = realm.getSnapdealCategoryConfiguration();
				logger.info("Category configuration");
				if (categoryConfiguration != null && categoryConfiguration.length() > 0) {
					List<String> promotedCategoriesList = new Gson().fromJson(categoryConfiguration,
							new TypeToken<List<String>>() {
							}.getType());
					if (promotedCategoriesList != null && promotedCategoriesList.size() > 0) {
						// ArrayList<String> originalList = (ArrayList<String>)
						// listAllCategories.clone();
						logger.info("Filtering promoted categories ); " + promotedCategoriesList.size());
						ArrayList<String> filteredCategoriesList = new ArrayList<String>();
						for (String category : listAllCategories) {
							for (String promotedCategory : promotedCategoriesList) {
								if (promotedCategory.toLowerCase().equals(category.toLowerCase())) {
									filteredCategoriesList.add(category);
									listAllOffers.remove(category);
								}
							}
						}

						filteredCategoriesList.addAll(listAllCategories);
						listAllCategories = filteredCategoriesList;
					}
				}

				// return user object
				SnapdealGetCategories responseObject = new SnapdealGetCategories();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setCategoriesList(listAllCategories);

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
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORIES + " error with following data: " + dataContent + " "
							+ exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealCategoryOffersTest/")
	public String getSnapdealCategoryOffersTest(@QueryParam("categoryName") String categoryName,
			@QueryParam("page") String page, @QueryParam("offersNumber") String offersNumber) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " ip: " + ipAddress + " categoryName: " + categoryName + " page: " + page + " offersNumber: "
					+ offersNumber;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " received request: " + dataContent);

			ArrayList<ProductsEntry> listOffers = new ArrayList<ProductsEntry>();
			if (categoryName.toLowerCase().equals("all")) {
				ArrayList<ProductsEntry> listOffersCache = new ArrayList<ProductsEntry>();
				List<SnapdealOffersEntity> snapdealOffersList = daoSnapdealOffers.findAll();
				for (SnapdealOffersEntity offersEntity : snapdealOffersList) {
					listOffersCache.addAll(snapdealManager.loadProductsFromSnapdealOffers(offersEntity));
				}
				listOffers = getOffersSubset(null, listOffersCache, Integer.parseInt(page),
						Integer.parseInt(offersNumber));
			} else {

				// return category offers in json format after applying paging
				SnapdealOffersEntity categoryOffersEntity = daoSnapdealOffers.findByCategory(categoryName);
				String categoryOffersOutputInJson = categoryOffersEntity.getOffersJson();
				listOffers = getOffersSubset(null, categoryOffersOutputInJson, Integer.parseInt(page),
						Integer.parseInt(offersNumber));
			}
			// return user object
			SnapdealGetCategoryOffers responseObject = new SnapdealGetCategoryOffers();
			responseObject.setCode(RespCodesEnum.OK.toString());
			responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
			responseObject.setRequestedCategoryName(categoryName);
			responseObject.setRequestedPage(Integer.parseInt(page));
			responseObject.setRequestedNumberOfOffers(Integer.parseInt(offersNumber));
			responseObject.setCategoryOffers(listOffers);

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
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " error with following data: " + dataContent + " "
							+ exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealCategoryOffers/")
	public String getSnapdealCategoryOffers(@QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("categoryName") String categoryName, @QueryParam("page") String page,
			@QueryParam("offersNumber") String offersNumber) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " phoneNumber: " + phoneNumber + " phoneNumberExtension: " + phoneNumberExt + " systemInfo: "
					+ systemInfo + " miscData: " + miscData + " systemInfo: " + systemInfo + " ip: " + ipAddress
					+ " categoryName: " + categoryName + " page: " + page + " offersNumber: " + offersNumber;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORIES + " received request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_GET_CATEGORY_OFFERS + " " + "aborting user with phone number: "
								+ phoneNumber + " not found in DB");

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

				// return category offers in json format after applying paging
				SnapdealOffersEntity categoryOffersEntity = daoSnapdealOffers.findByCategory(categoryName);
				String categoryOffersOutputInJson = categoryOffersEntity.getOffersJson();
				ArrayList<ProductsEntry> listOffers = getOffersSubset(appUser, categoryOffersOutputInJson,
						Integer.parseInt(page), Integer.parseInt(offersNumber));

				// return user object
				SnapdealGetCategoryOffers responseObject = new SnapdealGetCategoryOffers();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setRequestedCategoryName(categoryName);
				responseObject.setRequestedPage(Integer.parseInt(page));
				responseObject.setRequestedNumberOfOffers(Integer.parseInt(offersNumber));
				responseObject.setCategoryOffers(listOffers);

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
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " error with following data: " + dataContent + " "
							+ exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealCategoryOffersFiltered")
	public String getSnapdealCategoryOffersFiltered(@QueryParam("categoryName") String categoryName,
			@QueryParam("page") String page, @QueryParam("offersNumber") String offersNumber,
			@QueryParam("searchContent") String searchContent, @QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " ip: " + ipAddress + " categoryName: " + categoryName + " page: " + page + " offersNumber: "
					+ offersNumber + " searchContent: " + searchContent;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " received filtered request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_GET_CATEGORY_OFFERS + " " + "aborting user with phone number: "
								+ phoneNumber + " not found in DB");

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

				// return category offers in json format after applying paging
				List<ProductsEntry> categoryOffers = snapdealManager.filterOffers(categoryName, searchContent);
				ArrayList<ProductsEntry> listOffers = getOffersSubset(appUser, categoryOffers, Integer.parseInt(page),
						Integer.parseInt(offersNumber));

				// return user object
				SnapdealGetCategoryOffers responseObject = new SnapdealGetCategoryOffers();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setRequestedCategoryName(categoryName);
				responseObject.setRequestedPage(Integer.parseInt(page));
				responseObject.setRequestedNumberOfOffers(Integer.parseInt(offersNumber));
				responseObject.setCategoryOffers(listOffers);

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
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " error filtered with following data: " + dataContent
							+ " " + exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/getSnapdealToplist")
	public String getSnapdealTopList(@QueryParam("searchContent") String searchContent, @QueryParam("page") String page,
			@QueryParam("offersNumber") String offersNumber, @QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData) {

		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " ip: " + ipAddress + "  page: " + page + " offersNumber: " + offersNumber + " phoneNumber: "
					+ phoneNumber + " systemInfo: " + systemInfo + " miscData:" + miscData + " phoneNumberExt: "
					+ phoneNumberExt + " searchContent: " + searchContent;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_TOPLIST + " received filtered request: " + dataContent);

			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_TOPLIST + " " + "aborting user with phone number: " + phoneNumber
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

				List<ProductsEntry> toplistProducts = snapdealTopProductsManager.getTopProducts();
				logger.info("Selected toplist products: " + toplistProducts.size());

				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
						Application.SNAPDEAL_TOPLIST + " for data : " + dataContent + " selected toplist products "
								+ toplistProducts.size());

				if (searchContent != null && searchContent.length() > 0) {
					logger.info("Filtering toplist with elements: " + toplistProducts.size()
							+ " products with searchString: " + searchContent);

					Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
							Application.SNAPDEAL_TOPLIST + " for data : " + dataContent
									+ "Filtering toplist with elements: " + toplistProducts.size()
									+ " products with searchString: " + searchContent);

					List<ProductsEntry> productsFiltered = new ArrayList<ProductsEntry>();
					for (ProductsEntry product : toplistProducts) {
						if (product.getTitle().toLowerCase().contains(searchContent.toLowerCase())) {
							productsFiltered.add(product);
						}
					}
					logger.info("Filtered products : " + productsFiltered.size());
					Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
							Application.SNAPDEAL_TOPLIST + " for data : " + dataContent + "Filtered products : "
									+ productsFiltered.size());

					toplistProducts = productsFiltered;
				}

				ArrayList<ProductsEntry> listOffers = getOffersSubset(appUser, toplistProducts, Integer.parseInt(page),
						Integer.parseInt(offersNumber));

				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
						Application.SNAPDEAL_TOPLIST + " for data : " + dataContent
								+ "list offers after offers subset contains: " + listOffers.size());

				SnapdealGetCategoryOffers responseObject = new SnapdealGetCategoryOffers();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setRequestedCategoryName("All");
				responseObject.setRequestedPage(Integer.parseInt(page));
				responseObject.setRequestedNumberOfOffers(Integer.parseInt(offersNumber));
				responseObject.setCategoryOffers(listOffers);

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_TOPLIST + " error filtered with following data: " + dataContent + " "
							+ exc.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\""
					+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";
		}
	}

	private ArrayList<ProductsEntry> getOffersSubset(AppUserEntity appUser, List<ProductsEntry> listOffers,
			int pageNumber, int numberOfOffers) {
		ArrayList<ProductsEntry> listOffersToReturn = new ArrayList<ProductsEntry>();
		try {

			int startIndex = pageNumber * numberOfOffers;
			int endIndex = startIndex + numberOfOffers;

			if (endIndex > listOffers.size() - 1) {
				endIndex = listOffers.size() - 1;
				numberOfOffers = endIndex - startIndex + 1;
			}

			logger.info("Start index: " + startIndex);
			logger.info("End index:" + endIndex);
			logger.info("Offers size: " + listOffers.size());

			if (startIndex > listOffers.size() || endIndex > listOffers.size() - 1) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_GET_CATEGORY_OFFERS + " size of offers: " + listOffers.size()
								+ " smaller than calcualted start and end index: " + startIndex + " " + endIndex);
				return listOffersToReturn;
			}

			logger.info("returning offers from following index range: " + startIndex + "-" + endIndex + " page: "
					+ pageNumber + " numberOfOffers:" + numberOfOffers);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " getting offers for page: " + pageNumber
							+ " number of offers: " + numberOfOffers + " startIndex: " + startIndex + " endIndex: "
							+ endIndex);

			for (int i = 0; i < numberOfOffers; i++) {

				ProductsEntry product = listOffers.get(i + startIndex).clone();

				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
						Application.SNAPDEAL_TOPLIST + " offers subset for startIndex: " + startIndex + " endIndex: "
								+ endIndex + " offers size:" + listOffers.size() + " adding product with id: "
								+ product.getId());
				
				
				applyAffSubForProduct(product, appUser);
				listOffersToReturn.add(product);

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return listOffersToReturn;
	}

	private void applyAffSubForProduct(ProductsEntry product, AppUserEntity appUser) {
		String categoryName = "Unknown";
		categoryName = product.getCategoryName();

		// generate internal transaction id and add it to the product's
		// url
		String internalTransactionId = generateInternalTransactionId(appUser, product);
		String productClickLink = product.getLink();
		try {
			productClickLink = productClickLink + "&aff_sub=" + URLEncoder.encode(internalTransactionId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		product.setLink(productClickLink);
	}

	private ArrayList<ProductsEntry> getOffersSubset(AppUserEntity appUser, String categoryOffersOutputInJson,
			int pageNumber, int numberOfOffers) {
		ArrayList<ProductsEntry> listOffersToReturn = new ArrayList<ProductsEntry>();
		try {
			ObjectMapper mapperForOffers = new ObjectMapper();
			CategoryOffers categoryOffersHolder = mapperForOffers.readValue(categoryOffersOutputInJson,
					CategoryOffers.class);
			logger.info("got feed size: " + categoryOffersHolder.getProducts().size());
			List<ProductsEntry> listOffers = categoryOffersHolder.getProducts();

			int startIndex = pageNumber * numberOfOffers;
			int endIndex = startIndex + numberOfOffers;

			if (startIndex > listOffers.size() || endIndex > listOffers.size()) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
						Application.SNAPDEAL_GET_CATEGORY_OFFERS + " size of offers: " + listOffers.size()
								+ " smaller than calcualted start and end index: " + startIndex + " " + endIndex);
				return listOffersToReturn;
			}

			logger.info("returning offers from following index range: " + startIndex + "-" + endIndex + " page: "
					+ pageNumber + " numberOfOffers:" + numberOfOffers);
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " getting offers for page: " + pageNumber
							+ " number of offers: " + numberOfOffers + " startIndex: " + startIndex + " endIndex: "
							+ endIndex);

			for (int i = 0; i < numberOfOffers; i++) {
				ProductsEntry product = listOffers.get(i + startIndex);
				applyAffSubForProduct(product, appUser);
				listOffersToReturn.add(product);

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return listOffersToReturn;
	}

	private String generateInternalTransactionId(AppUserEntity appUser, ProductsEntry product) {
		String internalTransactionId = "";
		try {
			if (appUser == null) {
				if (product == null) {
					internalTransactionId = DigestUtils
							.sha1Hex(new Date().toString() + Math.random() * 10000 + System.currentTimeMillis());
				} else {
					internalTransactionId = DigestUtils.sha1Hex(product.getTitle() + Math.random() * 100000
							+ System.currentTimeMillis() + product.getCategoryId() + product.getId());
				}
			} else {
				if (product == null) {
					internalTransactionId = DigestUtils
							.sha1Hex(new Date().toString() + Math.random() * 10000 + System.currentTimeMillis());
				} else {
					internalTransactionId = DigestUtils
							.sha1Hex(product.getTitle() + appUser.getId() + Math.random() * 100000 + product.getId()
									+ product.getCategoryId() + System.currentTimeMillis() + appUser.getPhoneNumber()
									+ appUser.getPhoneNumberExtension());
				}
			}

		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_GET_CATEGORY_OFFERS + " generating internal transaction id failed for appUser:"
							+ appUser + " product: " + product + exception.getMessage());
			internalTransactionId = DigestUtils
					.sha1Hex(new Date().toString() + Math.random() * 10000 + System.currentTimeMillis());
			exception.printStackTrace();
		}
		if (internalTransactionId.length() > 32) {
			internalTransactionId = internalTransactionId.substring(0, 31);
		}
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
				Application.SNAPDEAL_GET_CATEGORY_OFFERS + " internalT: " + internalTransactionId
						+ " generated for user" + appUser + " and product: " + product);

		return internalTransactionId;
	}

	private void logSnapdealClickInvalidRequest(String dataContent, String errorCause) {
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
				Application.SNAPDEAL_CLICK + " received snapdeal click request: " + dataContent + errorCause);
	}

	@GET
	@Produces("application/json")
	@Path("/v1/snapdealClick/")
	public String executeSnapdealClick(@QueryParam("offerId") String offerId,
			@QueryParam("effectivePrice") String effectivePrice, @QueryParam("title") String title,
			@QueryParam("deviceType") String deviceType, @QueryParam("applicationName") String applicationName,
			@QueryParam("rewardType") String rewardType, @QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("hashkey") String hashkey, @QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData, @QueryParam("internalTransactionId") String internalTransactionId,
			@QueryParam("link") String link, @QueryParam("userId") String userId) {

		String dataContent = "";
		Gson gson = new Gson();
		String errorCause = "";
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = "userId: " + userId + " offerId: " + offerId + " effectivePrice: " + effectivePrice
					+ " title: " + title + " deviceType: " + deviceType + " applicationName: " + applicationName
					+ " rewardType: " + rewardType + " phoneNumber: " + phoneNumber + " systemInfo: " + systemInfo
					+ " miscData: " + miscData + " internalT: " + internalTransactionId + " link:" + link;

			logger.info("Received snapdeal click request: " + dataContent);

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_CLICK + " received snapdeal click request: " + dataContent);

			if (offerId == null || offerId.length() == 0) {
				errorCause = "offer id null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_OFFER_DATA));
			}
			if (title == null || title.length() == 0) {
				errorCause = "title null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_OFFER_DATA));
			}
			if (effectivePrice == null || effectivePrice.length() == 0) {
				errorCause = "offer id null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_OFFER_DATA));
			}
			if (internalTransactionId == null || internalTransactionId.length() == 0) {
				errorCause = "internalTransactionId null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_TRANSACTION_ID));
			}
			UserEventEntity checkedEvent = daoUserEvent.findByInternalTransactionId(internalTransactionId);
			if (checkedEvent != null) {
				errorCause = "event already exists with internalTransactionId";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_TRANSACTION_ID));
			}
			if (deviceType == null || deviceType.length() == 0) {
				errorCause = "device type null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_DEVICE_TYPE));
			}
			if (applicationName == null || applicationName.length() == 0) {
				errorCause = "application name null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_APPLICATION));
			}
			if (rewardType == null || rewardType.length() == 0) {
				errorCause = "reward type null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_REWARD_TYPE));
			}

			if (phoneNumber == null || phoneNumber.length() == 0) {
				errorCause = "phone number null or empty";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson
						.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER));
			}
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			if (appUser == null) {
				errorCause = "invalid app user";
				logSnapdealClickInvalidRequest(dataContent, errorCause);
				return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_USER_NOT_FOUND));
			}

			UserEventEntity event = prepareEventFromSnapdealOffer(internalTransactionId, offerId, effectivePrice, title,
					deviceType, applicationName, rewardType, phoneNumber, appUser);

			daoUserEvent.create(event);
			indexSnapdealEvent(event, link, miscData, systemInfo, ipAddress);

			logger.info("Successfully created event.");
			logger.info(event.toString());

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
					Application.SNAPDEAL_CLICK + " created event: " + event + " for data: " + dataContent);

			return gson.toJson(produceResponse(RespStatusEnum.SUCCESS, RespCodesEnum.OK_NO_CONTENT));

		} catch (Exception exc) {
			logger.info(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.ERROR,
					Application.SNAPDEAL_CLICK + " exception in snapdeal click ws: "
							+ Arrays.toString(exc.getStackTrace()) + " for data: " + dataContent);

			return gson.toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR));

		} finally {
			if (errorCause.length() > 0) {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_WS_REQUEST, -1, LogStatus.OK,
						Application.SNAPDEAL_CLICK + " webservice failed for request : " + dataContent + " cause: "
								+ errorCause);
			}

		}

	}

	private void indexSnapdealEvent(UserEventEntity event, String link, String miscData, String systemInfo,
			String ipAddress) {
		try {
			if (event != null) {
				RealmEntity realm = daoRealm.findById(event.getRealmId());
				if (realm != null) {
					logger.info("Indexing snapdeal event with id: " + event.getId());
					Application.getElasticSearchLogger().indexUserClick(event.getRealmId(), event.getPhoneNumber(),
							event.getEmail(), event.getDeviceType(), event.getOfferId(), event.getOfferSourceId(),
							event.getOfferTitle(), event.getAdProviderCodeName(), event.getRewardTypeName(),
							event.getOfferPayout(), event.getRewardValue(), event.getRewardIsoCurrencyCode(),
							event.getProfilSplitFraction(), realm.getName(), link, UserEventType.click.toString(),
							event.getInternalTransactionId(), event.getCarrierName(), event.getUserEventCategory(),
							miscData, systemInfo, ipAddress, event.getCountryCode(), event.isInstant(),
							event.getApplicationName(), event.getAdvertisingId(), event.getIdfa(), realm.isTestMode(),
							event.getCustomRewardValue(), event.getCustomRewardCurrencyCode());
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	private UserEventEntity prepareEventFromSnapdealOffer(String internalTransactionId, String offerId,
			String effectivePrice, String title, String deviceType, String applicationName, String rewardType,
			String phoneNumber, AppUserEntity appUser) {
		UserEventEntity event = new UserEventEntity();
		event.setAdProviderCodeName("SNAPDEAL");
		event.setApplicationName(applicationName);
		event.setClickDate(new Timestamp(new Date().getTime()));
		event.setCountryCode(appUser.getCountryCode());
		event.setDeviceId(appUser.getDeviceId());
		event.setAndroidDeviceToken(appUser.getAndroidDeviceToken());

		event.setDeviceType(deviceType);
		event.setEmail(appUser.getEmail());

		event.setInternalTransactionId(internalTransactionId);
		event.setIosDeviceToken(appUser.getiOSDeviceToken());
		event.setOfferId(offerId);
		// double effectivePriceD = Double.parseDouble(effectivePrice);
		event.setOfferPayout(0);
		event.setOfferPayoutInTargetCurrency(0);
		event.setOfferPayoutInTargetCurrencyIsoCurrencyCode(getIsoCurrencyCodeBasedOnRewardType(rewardType));

		event.setOfferSourceId("");
		event.setOfferTitle(title);
		event.setPhoneNumber(phoneNumber);
		event.setPhoneNumberExt(appUser.getPhoneNumberExtension());
		event.setProfilSplitFraction(0);
		event.setProfitValue(0);
		event.setRealmId(appUser.getRealmId());
		event.setRevenueValue(0);
		event.setRewardIsoCurrencyCode("INR");
		event.setRewardName("AirTime");
		event.setRewardTypeName(rewardType);
		event.setRewardValue(0);
		event.setTransactionId(internalTransactionId);
		event.setUserEventCategory(UserEventCategory.SNAPDEAL.toString());
		event.setUserId(appUser.getId());

		return event;
	}

	private String getIsoCurrencyCodeBasedOnRewardType(String rewardType) {
		String baseCurrency = "INR";
		if (rewardType != null) {
			if (rewardType.equals("AirRewardz-India")) {
				baseCurrency = "INR";
			}
			if (rewardType.equals("AirRewardz-Kenya")) {
				baseCurrency = "KEN";
			}
			if (rewardType.equals("AirRewardz-SouthAfrica")) {
				baseCurrency = "ZAR";
			}
		}

		return baseCurrency;

	}

	private is.web.services.Response produceResponse(RespStatusEnum status, RespCodesEnum code) {
		is.web.services.Response response = new is.web.services.Response();
		response.setCode(code.toString());
		response.setStatus(status.toString());
		return response;
	}

	@GET
	@Produces("application/json")
	@Path("/v1/snapdealCategoriesCommisionList/")
	public String getSnapdealCategoriesCommisionList() {
		try {
			List<SnapdealCategoryCommision> commisionList = this.snapdealCategoriesCommissionHolder
					.getCategoriesCommision();
			SnapdealCategoriesCommisionResponse response = new SnapdealCategoriesCommisionResponse();
			response.setCommisionList(commisionList);
			response.setStatus(RespStatusEnum.SUCCESS.toString());
			response.setCode(RespCodesEnum.OK.toString());

			return new Gson().toJson(response);
		} catch (Exception exception) {
			exception.printStackTrace();
			return new Gson().toJson(produceResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR));
		}
	}

	@GET
	@Produces("application/json")
	@Path("/v1/snapdealReport/")
	public String snapdealReport() {
		// SnapdealReportResponse response =
		// snapdealManager.getSnapdealReports(new Date(), new Date(),
		// SnapdealReportType.approved);
		// logger.info(response.toString());
		snapdealManager.updateSnapdealReports();
		return "{}";
	}

	@GET
	@Produces("application/json")
	@Path("/v1/snapdealApprovedOffers")
	public String snapdealApprovedReport() {
		// SnapdealReportResponse response =
		// snapdealManager.getSnapdealReports(new Date(), new Date(),
		// SnapdealReportType.approved);
		// logger.info(response.toString());
		snapdealManager.updateSnapdealApprovedOffers();
		return "{}";
	}

	@Inject
	ReportingManager manager;
	@Inject
	TimerReporting timer;

	@GET
	@Produces("application/json")
	@Path("/v1/reportTest/")
	public String reportTest() throws Exception {
		// SnapdealReportResponse response =
		// snapdealManager.getSnapdealReports(new Date(), new Date(),
		// SnapdealReportType.approved);
		// logger.info(response.toString());
		/*
		 * Date dateEnd = new Date(); Calendar c = Calendar.getInstance();
		 * c.setTime(dateEnd); c.add(Calendar.DAY_OF_MONTH, -1); Date dateStart
		 * = new Date(); dateStart.setTime(c.getTime().getTime()); RealmEntity
		 * realm = daoRealm.findByName("BPM"); ReportDH reportDH =
		 * manager.getReportData(realm, ReportPeriodName.LAST_DAY,
		 * "last 24 hours", dateStart, dateEnd, "AirRewardz-India", "BPM");
		 * 
		 * return "{}";
		 */

		return "{}";
	}

}