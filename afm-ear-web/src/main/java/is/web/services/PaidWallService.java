package is.web.services;

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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple REST service which is able to say hello to someone using HelloService Please take a look at the web.xml where JAX-RS
 * is enabled
 */

@Path("/")
public class PaidWallService {
	  
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

    /**
     * This method returns ids of invetivised walls that are targeted for specific country and device type
     * device types are: Android, iOS, Windows
     * coutries are: 2character codes: US, GB, PL, KE
     */
    @GET
    @Produces("application/json")
    @Path("/v1/getTargetedWallIds/")
    public ResponseOWIds getTargetedWallIds(@QueryParam("userId") int userId,
			@QueryParam("phoneNumberExt") String phoneNumberExt,
			@QueryParam("phoneNumber") String phoneNumber,
    		@QueryParam("deviceType") String deviceType,    //device type can be: [Android, iOS]
    		@QueryParam("rewardType") String rewardType,
    		@QueryParam("hashkey") String hashkey,
    		@QueryParam("systemInfo") String systemInfo,
    		@QueryParam("miscData") String miscData) {
    	
		ResponseOWIds response = new ResponseOWIds();
    	try {
    		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");  
    		if (ipAddress == null) {  
    			ipAddress = httpRequest.getRemoteAddr();  
    		}

			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.COW_SELECTION_ACTIVITY+" "+
					Application.COW_IDS_SELECTION+" "+
					" identifying available offer walls for user with id: "+userId+
					" reward type: "+rewardType+
					" miscData: "+miscData+
					" systemInfo: "+systemInfo+
					" phoneNumber: "+phoneNumber+
					" phoneNumberExtension: "+phoneNumberExt+
					" ip: "+ipAddress+
					" device type: "+deviceType);

    		//get user by id
			AppUserEntity appUser = null;
			appUser = daoAppUser.findById(userId);
			if(appUser == null) { //user already registered!
				Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_IDS_SELECTION+" "+
						"aborting as no user found under following id: "+userId);

				response.setErrorMessage("Error: "+RespCodesEnum.ERROR_USER_WITH_GIVEN_ID_NOT_FOUND);
				response.setStatus(RespStatusEnum.FAILED.toString());
				response.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

				return response;
			} 
    		
    		//get realm by key
    		RealmEntity realm = daoRealm.findById(appUser.getRealmId());
    		if(realm == null) {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_IDS_SELECTION+
						" Unable to identify network, please make sure that network with provided name is correct "+
						" status: "+RespStatusEnum.FAILED.toString()+
						" error code: "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
    		
				response.setErrorMessage("Unable to identify network, please make sure that network with provided name is correct");
				response.setCode(RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
    			response.setStatus(RespStatusEnum.FAILED.toString());
    			
    			return response;
    		}

			//validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey, 
					hashValidationManager.getFullURL(httpRequest), 
					phoneNumber, phoneNumberExt, systemInfo, miscData, ipAddress);
			if(!isRequestValid) {
				response.setIdList(null);
				response.setCode(RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED.toString());
				response.setErrorMessage("");
				response.setNetworkName(realm.getName());
				response.setStatus(RespStatusEnum.FAILED.toString());
				
				return response;
			} 

			//get all walls that match network and deviceType
    		//TODO offer wall should have a deviceType included (ddl with three possible options: Android, Windows, iOS)
			List<OfferWallEntity> listOffers = 
					daoOfferWall.findAllByRealmIdAndActiveAndCountryAndDevice(realm.getId(),
							true, 
							appUser.getCountryCode(),
							deviceType,
							rewardType);//use device type that was dynamically supplied via request - appUser.getDeviceType());
			
			int[] ids = new int[listOffers.size()];
			for(int i=0;i<listOffers.size();i++) {
				ids[i]=listOffers.get(i).getId();
			}
			
			response.setIdList(ids);
			response.setCode(RespCodesEnum.OK.toString());
			response.setErrorMessage("");
			response.setNetworkName(realm.getName());
			response.setStatus(RespStatusEnum.SUCCESS.toString());
			
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(), 
					LogStatus.OK, 
					Application.COW_SELECTION_ACTIVITY+" "+
					Application.COW_IDS_SELECTION+" "+
					Application.COW_IDS_SELECTION_IDENTIFIED+" "+
					" returning existing offer wall ids for realm: "+ realm.getName()+
					" network id: "+realm.getId()+
					" country: "+appUser.getCountryCode()+ 
					" device: "+appUser.getDeviceType());
			
    		return response;

    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.COW_SELECTION_ACTIVITY+" "+
					Application.COW_IDS_SELECTION+" Error selecting offer: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED.toString()+
					" error code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

			response.setErrorMessage("Error: "+exc.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			response.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			
    		return response;
    	}
    }

//  : userId=8156&deviceId=xabc&phoneId=ddddd&fullName=fullName&phoneNumber=148507540290&offerWallId=17&idfa=idfaValue&gaid=57b29fdba6991a262cc90083868c2%20c4477e12b82031fc01c1ca22478e48%20b8ed0&deviceType=iOS&ua=ua&systemInfo=systemInfo&miscData=miscData
//	: http://127.0.0.1:8080/ab/svc/v1/getTargetedWall?userId=8156&deviceId=xabc&phoneId=ddddd&fullName=fullName&phoneNumber=148507540290&offerWallId=17&idfa=idfaValue&gaid=57b29fdba6991a262cc90083868c2%20c4477e12b82031fc01c1ca22478e48%20b8ed0&deviceType=iOS&ua=ua&systemInfo=systemInfo&miscData=miscData

    /**
     * This method returns offer walls that are personalized for individual user 
     * based on his previous download history 
     */
    @GET
    @Produces("application/json")
    @Path("/v1/getTargetedWall/")
    public String getCompositeOfferWallContent(@QueryParam("userId") int userId,
							    		@QueryParam("phoneNumber") String phoneNumber,
							    		@QueryParam("phoneNumberExt") String phoneNumberExt,
							    		@QueryParam("fullName") String fullName,
							    		@QueryParam("email") String email,
    									@QueryParam("offerWallId") int offerId, 
    						    		@QueryParam("deviceType") String deviceType,
    						    		@QueryParam("locale") String locale,
    						    		@QueryParam("gaid") String gaid, //tracking for android
    						    		@QueryParam("idfa") String idfa, //tracking for ios
    						    		@QueryParam("ua") String ua,
    						    		@QueryParam("hashkey") String hashkey,    						    		
    						    		@QueryParam("systemInfo") String systemInfo,
    	    				    		@QueryParam("miscData") String miscData) { 

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
			ipAddress = httpRequest.getRemoteAddr();  
		}
		Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
				LogStatus.OK, 
				Application.COW_SELECTION_ACTIVITY+" "+
				Application.COW_SELECTION_BY_ID+
				" Retrieving offer wall with id: "+offerId+
				" for user with id: "+userId+
				" phone number ext: "+phoneNumberExt+
				" phone number: "+phoneNumber+
				" full name: "+fullName+
				" email: "+email+
				" deviceType: "+deviceType+
				" locale: "+locale+
				" gaid: "+gaid+
				" idfa: "+idfa+
				" ua: "+ua+
				" miscData: "+miscData+
				" systemInfo: "+systemInfo+
				" ip: "+ipAddress);

		logger.info("got request: "+httpRequest);
		logger.info("got ip: "+ipAddress);
		
    	try {
    		//get user by id
			AppUserEntity appUser = null;
			appUser = daoAppUser.findById(userId);
			if(appUser == null) { //user already registered!
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Unable to identify offer wall with provided id: "+offerId+" please make sure that offer id is correct"+
								" status: "+RespStatusEnum.FAILED.toString()+
								" error code: "+RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
				
				//return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				
				return jsonResponseContent;
			} 
    		
    		//get realm by key
    		RealmEntity realm = daoRealm.findById(appUser.getRealmId());
    		if(realm == null) {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_SELECTION_BY_ID+
						" Unable to identify network, please make sure that network with provided name is correct "+
						" status: "+RespStatusEnum.FAILED.toString()+
						" error code: "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());

				//return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
    		}

			//validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey, 
					hashValidationManager.getFullURL(httpRequest), 
					phoneNumber, phoneNumberExt, systemInfo, miscData, ipAddress);
			if(!isRequestValid) {
				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED+"\", "+ "\"userId\":\"-1\"}";
			} 
    		
    		OfferWallEntity offerWall = daoOfferWall.findById(offerId);
			OfferWallContent offerWallContent = null;
			if(offerWall != null) {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(), 
						LogStatus.OK, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_SELECTION_BY_ID+" "+
						Application.COW_SELECTION_BY_ID_IDENTIFIED+" "+
						" selecting offer wall with id: "+offerId+
						" userId: "+userId+
						" userName: "+fullName+
						" userEmail: "+email+
						" deviceType: "+deviceType+
						" for realm: "+ realm.getName()+
						" network id: "+realm.getId());
    			logger.info("COW_SELECTION selecting offer wall with id: "+offerId+ " for realm: "+ realm.getName());

        		//create wall selection log
    			logger.info("Indexing wall selection");
        		Application.getElasticSearchLogger().indexWallSelection(realm.getName(),
        				userId,
        				email,
        				phoneNumber, 
        				phoneNumberExt,
        				offerId,
        				offerWall.getRewardTypeName(),
        				offerWall.getTargetCountriesFilter(),
        				offerWall.getTargetDevicesFilter(),
        				locale,
        				ua,
        				ipAddress,
        				systemInfo,
        				miscData);
        		logger.info("Indexed wall selection");
				//filter offer wall and remove offers that are already converted by user
    			//offerWallContent = filterOutAlreadyConvertedByUserApps(appUser, offerWall);
    			//if offer wall is empty - return it and device should not display it! 
    			/**
    			 * dynamically attach real-time feed (offer wall contains only anchor to include that feed but its generated in real time during this request
    			 */
    			//https://geo.tp-cdn.com/api/offer/v1/?vic=f53667b888d5a293ebbc42723926aaf9&sid=528340&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+8_3+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12F70&ip=86.154.102.160&;idfa=5E2ECC3A-83AD-4A26-A65C-8A65EB31EB5B&num_offers=100
    			RealtimeFeedDataHolder realtimeFeedDataHolder = new RealtimeFeedDataHolder();
    			//overriden values for testing
//    			ua = "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+8_3+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12F70";
//    			ipAddress="86.154.102.160";
//    			idfa="5E2ECC3A-83AD-4A26-A65C-8A65EB31EB5B";
    			
    			realtimeFeedDataHolder.setGaid(URLEncoder.encode(gaid, "UTF-8"));
    			realtimeFeedDataHolder.setIdfa(URLEncoder.encode(idfa, "UTF-8"));
    			realtimeFeedDataHolder.setIp(URLEncoder.encode(ipAddress, "UTF-8"));
    			realtimeFeedDataHolder.setUa(URLEncoder.encode(ua, "UTF-8"));
    			realtimeFeedDataHolder.setUserId(URLEncoder.encode(userId+"", "UTF-8"));
    			realtimeFeedDataHolder.setDeviceType(URLEncoder.encode(deviceType, "UTF-8"));
    			offerWallContent = realtimeFeedGenerator.composeOfferWall(offerWall, realtimeFeedDataHolder, true);

				//filter offer wall and remove offers that are already converted by user
    			offerWallContent = filterOutAlreadyConvertedByUserApps(appUser, offerWall);

    			
    			//count how many offers are returned
    			int returnedOffers = 0;
    			for(int i=0;i<offerWallContent.getOfferWalls().size();i++){
    				IndividualOfferWall wall = offerWallContent.getOfferWalls().get(i);
    				if(wall != null) {
    					returnedOffers = returnedOffers + wall.getOffers().size();
    				}
    			}
    			
    			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
    					LogStatus.OK, 
    					Application.COW_SELECTION_ACTIVITY+" "+
    					Application.COW_SELECTION_BY_ID+
    					" returning wall: "+offerWallContent.getCompositeOfferWallName()+
    					" individual walls number: "+offerWallContent.getOfferWalls().size()+
    					" returned_offers: "+returnedOffers+
    					" for user with id: "+userId+
    					" phone number: "+phoneNumber+
    					" full name: "+fullName+
    					" email: "+email+
    					" deviceType: "+deviceType+
    					" locale: "+locale+
    					" gaid: "+gaid+
    					" idfa: "+idfa+
    					" ua: "+ua+
    					" ip: "+ipAddress);
    			
    			//return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setMultiOfferWall(offerWallContent);

				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
			} else { //no offer wall with id found
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Unable to identify offer wall with provided id: "+offerId+" please make sure that offer id is correct"+
								" status: "+RespStatusEnum.FAILED.toString()+
								" error code: "+RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());

				//return user object
				ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
				responseObject.setCode(RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setMultiOfferWall(null);

				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);
				return jsonResponseContent;
			}
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Error selecting offer: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED.toString()+
					" error code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

			//return user object
			ResponseMultiOfferWall responseObject = new ResponseMultiOfferWall();
			responseObject.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			responseObject.setStatus(RespStatusEnum.FAILED.toString());
			responseObject.setMultiOfferWall(null);

			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);
			return jsonResponseContent;
    	}
    }

    private OfferWallContent filterOutAlreadyConvertedByUserApps(AppUserEntity appUser, OfferWallEntity offerWall) {
    	OfferWallContent offerWallContent = null;
    	try {
    		//filter through offers
			offerWallContent = serDeOfferWallContent.deserialize(offerWall.getContent());
    		//retrieve download history for this user
    		ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(appUser.getId());
    		if(conversionHistory == null) {
    			return offerWallContent; //exit as user has not generated conversion history yet
    		}
    		ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
    		
    		ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries = conversionHistoryHolder.getListConversionHistoryEntries(); 
			ArrayList<IndividualOfferWall> listIndividualOfferWalls = offerWallContent.getOfferWalls();
			for(int i=0;i<listIndividualOfferWalls.size();i++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(i);
				ArrayList<Offer> listOffers = individualOfferWall.getOffers();
				for(int k=listOffers.size()-1;k>=0;k--) {
					Offer offer = listOffers.get(k);
					//logger.info("checking offer: "+offer.getTitle());
					if(isOfferConvertedByUser(offerWall.getRealm().getId(), offer, listUserConversionHistoryEntries)) {
						listOffers.remove(k);
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Error selecting offer: "+e.toString()+
					" status: "+RespStatusEnum.FAILED.toString()+
					" error code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
		}
    	return offerWallContent;
    }
    
    private boolean isOfferConvertedByUser(int realmId,
    		Offer offer, 
    		ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries) {
    	
    	for(int i=0;i< listUserConversionHistoryEntries.size();i++) {
    		ConversionHistoryEntry conversionEntry = listUserConversionHistoryEntries.get(i);
    		//logger.info("comparing: "+offer.getTitle()+" "+offer.getSourceId()+" "+offer.getAdProviderCodeName()+" --- "+
    		//	conversionEntry.getOfferTitle()+" "+conversionEntry.getSourceOfferId()+" "+conversionEntry.getAdProviderCodeName());
    		//logger.info("converison entry: "+conversionEntry);
    		try {
        		if(conversionEntry != null && conversionEntry.getConversionTimestamp()!=null &&
        				conversionEntry.getSourceOfferId() != null &&
        				conversionEntry.getSourceOfferId().equals(offer.getSourceId()) &&
        				conversionEntry.getAdProviderCodeName() != null &&
        				conversionEntry.getAdProviderCodeName().equals(offer.getAdProviderCodeName())) {

    				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realmId, 
    						LogStatus.OK, 
    						Application.COW_SELECTION_ACTIVITY+" "+
    						Application.COW_SELECTION_BY_ID+" "+
    						Application.OFFER_REJECTED_AS_ALREADY_CONVERTED+" "+
    						" rejected offer: "+offer.getTitle()+ " add provder: "+offer.getAdProviderCodeName()+
    						" as it was already converted by used at time: "+conversionEntry.getConversionTimestamp().toString());

        			return true;
        		}
    		} catch(Exception exc) {
    			logger.severe("Error in conversion entry when identifying already converted offers when comparing: "+offer.getTitle()+" "+offer.getSourceId()+" "+offer.getAdProviderCodeName()+" --- "+
        			conversionEntry.getOfferTitle()+" "+conversionEntry.getSourceOfferId()+" "+conversionEntry.getAdProviderCodeName()+
        			" "+exc.toString());
    			//exc.printStackTrace();
    			
    			return true;
    		}
    	}
    	
    	return false;
    }
 
}