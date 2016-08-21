package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserErrorCategoryEnum;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.zendesk.client.v2.model.Ticket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple REST service which is able to say hello to someone using HelloService Please take a look at the web.xml where JAX-RS
 * is enabled
 */

@Path("/")
public class ReportingService {
	@Inject
	private Logger logger;

    @Inject
	private DAORealm daoRealm;

    @Inject
	private DAOAppUser daoAppUser;

	@Context
	private HttpServletRequest httpRequest;

    @Inject
	private ZendeskManager zendeskManager;

    @Inject
	private HashValidationManager hashValidationManager;

    /**
     * this error is reported during exception handling on mobile app  
     */
    @GET
    @Produces("application/json") 
    @Path("/v1/reportError/")
    public String reportErrorWithQueryRouting(@QueryParam("userId") int userId,
    		@QueryParam("phoneNumber") String phoneNumber,
    		@QueryParam("phoneNumberExt") String phoneNumberExtension,
    		@QueryParam("fullName") String fullName,
    		@QueryParam("email") String email,
    		@QueryParam("phoneId") String phoneId,
    		@QueryParam("deviceId") String deviceId,
    		@QueryParam("mac,") String mac,
    		@QueryParam("countryCode") String countryCode,
    		@QueryParam("locale") String locale,
    		@QueryParam("deviceType") String deviceType,
    		@QueryParam("networkName") String networkName,
    		@QueryParam("action") String action,
    		@QueryParam("errorMessage") String errorMessage,
    		@QueryParam("systemInfo") String systemInfo,
    		@QueryParam("miscData") String miscData) 
    {

    	String responseMessage = "";
    	int realmId = -1;
		
    	try {
    		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");  
    		if (ipAddress == null) {  
    			ipAddress = httpRequest.getRemoteAddr();  
    		}

    		
    		String dataContent = " userId: "+userId+
					" action: "+action+
					" miscData: "+miscData+
                    " systemInfo: "+systemInfo+
                    " ip: "+ipAddress+
					" errorMessage: "+errorMessage;
    		logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.ERROR_REPORTING_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.ERROR_REPORTING_ACTIVITY+" received error report: "+dataContent);

			//create user support request log
			Application.getElasticSearchLogger().indexMobileFault(fullName, 
					email, 
					phoneNumberExtension,
					phoneNumber,
					locale, 
					systemInfo, 
					deviceType,
					networkName,
					errorMessage,
					miscData,
					action,
					ipAddress);

			//return user object
			Response responseObject = new Response();
			responseObject.setCode(RespCodesEnum.OK_NO_CONTENT.toString());
			responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
			
			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);

			//return "{\"result\":\" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
			return jsonResponseContent;//"{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+ "\"code\":\""+RespCodesEnum.OK+"\", "+ "\"userId\":\""+appUser.getId()+"\"}";
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realmId, 
					LogStatus.ERROR, 
					Application.ERROR_REPORTING_ACTIVITY+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());
			//return user object
			Response responseObject = new Response();
			responseObject.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			responseObject.setStatus(RespStatusEnum.FAILED.toString());
			
			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);

    		//return "{\"result\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString()+"\"}";
			return jsonResponseContent;//"{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+ "\"code\":\""+RespCodesEnum.OK+"\", "+ "\"userId\":\""+appUser.getId()+"\"}";
    	}
    }
    
    /**
     * this error is reported directly by mobile app user 
     */
    @GET
    @Produces("application/json") 
    @Path("/v1/reportUserError/")
    public String reportUserErrorWithQueryRouting(@QueryParam("errorDescription") String errorDescription,
    		@QueryParam("errorCategory") String errorCategory,
    		@QueryParam("phoneNumber") String phoneNumber,
    		@QueryParam("phoneNumberExt") String phoneNumberExt,
    		@QueryParam("fullName") String fullName,
    		@QueryParam("email") String email,
    		@QueryParam("phoneId") String phoneId,
    		@QueryParam("deviceId") String deviceId,
    		@QueryParam("mac") String mac,
    		@QueryParam("countryCode") String countryCode,
    		@QueryParam("locale") String locale,
    		@QueryParam("deviceType") String deviceType,
			@QueryParam("hashkey") String hashkey,
    		@QueryParam("networkName") String networkName,
    		@QueryParam("systemInfo") String systemInfo,
    		@QueryParam("miscData") String miscData,
    		@QueryParam("applicationName") String applicationName) { 
    	String responseMessage = "";
    	int realmId = -1;
		
    	//errorCategory should match one of the strings from UserErrorCategoryEnum
    	try {
    		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");  
    		if (ipAddress == null) {  
    			ipAddress = httpRequest.getRemoteAddr();  
    		}

    		String dataContent = " errorDesciption: "+errorDescription+
					" errorCategory: "+errorCategory+
					" phoneNumber: "+phoneNumber+
					" phoneNumberExtension: "+phoneNumberExt+
					" fullName: "+fullName+
					" email: "+email+
					" phoneId: "+phoneId+
					" deviceId: "+deviceId+
					" mac: "+mac+
					" countryCode: "+countryCode+
					" locale: "+locale+
					" systemInfo: "+systemInfo+
					" deviceType: "+deviceType+
                    " miscData: "+miscData+
                    " systemInfo: "+systemInfo+
                    " ip: "+ipAddress+
					" networkName: "+networkName+
					" applicationName " + applicationName;
    		
    		logger.info(dataContent);
    		System.out.println(dataContent);

			//ConversionHistoryEntry [rewardTypeName=AirRewardz-India, clickTimestamp=Sep 28, 2015 8:27:09 AM, sourceOfferId=142826, approved=false, internalTranscationId=6cdf0322a1b77b2e0fd7333366d9412fdf6f9f1d, rewardCurrency=INR, successful=false, adProviderCodeName=null, userEventCategory=INSTALL, offerId=4736c31d9e9a5371120e3fe0df1c244ad16138c1, offerTitle=Quikr Free Local Classifieds, conversionTimestamp=Sep 28, 2015 8:27:09 AM, rewardTimestamp=null, rewardStatusMessage=null, rewardValue=10.0, isWalletEntry=false]
			Application.getElasticSearchLogger().indexLog(Application.ERROR_REPORTING_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.USER_ERROR_REPORTING_ACTIVITY+" received error report: "+dataContent);
    		
			//identify user
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			RealmEntity realm = daoRealm.findById(appUser.getRealmId());
			
			//validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey, 
					hashValidationManager.getFullURL(httpRequest), 
					phoneNumber, phoneNumberExt, systemInfo, miscData, ipAddress);
			if(!isRequestValid) {
				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED+"\", "+ "\"userId\":\"-1\"}";
			} 
			
			//possible categories are:
			//PROBLEM_NO_INSTANT_REWARD
			//PROBLEM_NO_VIDEO_REWARD
			//PROBLEM_NO_WALLET_RECHARGE
			//PROBLEM_NO_WALLET_PAYOUT
			//PROBLEM_OFFER_NOT_WORKING
			String supportQuestionContent = "Support question category: "+errorCategory+
					" question content: "+errorDescription;
			
			//check if conversion was generated
			String internalTransactionId = "";
			String ticketSubject = "User support question ("+errorCategory+")";
			if(errorCategory != null && errorCategory.length() >0 && errorCategory.equals(UserErrorCategoryEnum.PROBLEM_NO_INSTANT_REWARD.toString())){
				try {
					logger.info("identified user support question related to: "+UserErrorCategoryEnum.PROBLEM_NO_INSTANT_REWARD.toString());
					int startIndex = supportQuestionContent.indexOf("internalTranscationId");
					internalTransactionId = supportQuestionContent.substring(startIndex+22);
					int endIndex = supportQuestionContent.indexOf(",");
					internalTransactionId = internalTransactionId.substring(0, endIndex);
					logger.info("-> extracted transaction id: "+internalTransactionId);
					Application.getElasticSearchLogger().indexLog(Application.ERROR_REPORTING_ACTIVITY, -1, 
							LogStatus.OK, 
							Application.USER_ERROR_REPORTING_ACTIVITY+" extracted transaction id: "+internalTransactionId);
					
					if(!supportQuestionContent.contains("conversionTimestamp")){
						ticketSubject = "User support question ("+errorCategory+")[Missing conversion callback]";
					} 
				} catch(Exception exc) {
					logger.severe(exc.toString());
					exc.printStackTrace();
				}
			}
			
			//integrate with zendesk
			Ticket ticket = zendeskManager.createTicket(fullName,email, 
					ticketSubject, 
					supportQuestionContent, 
					realm.getSupportSystemUrl(),
					realm.getSupportSystemUserName(), 
					realm.getSupportSystemPassword(),
					applicationName);
			
			//TODO add id tag to the ticket (extract
//			ArrayList<String> tagsList = new ArrayList<String>();
//			tagsList.add("id:"+internalTransactionId);
//			zendeskManager.setTicketTags(ticket, tagsList,  
//					realm.getSupportSystemUrl(),
//					realm.getSupportSystemUserName(), 
//					realm.getSupportSystemPassword(),
//					applicationName);
			
			Application.getElasticSearchLogger().indexLog(Application.ERROR_REPORTING_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.USER_ERROR_REPORTING_ACTIVITY+" error report: "+dataContent+" successfully issued to support system, ticket id: "+ticket.getId()+
					" date: "+ticket.getCreatedAt().toString()+
					" status: "+ticket.getStatus().toString());

			//create user support request log
			Application.getElasticSearchLogger().indexUserSupportRequest(errorCategory,
					fullName, 
					email, 
					phoneNumberExt,
					phoneNumber,
					locale, 
					systemInfo,
					miscData,
					ipAddress,
					deviceType,
					networkName,
					errorDescription);
			
			//return user object
			Response responseObject = new Response();
			responseObject.setCode(RespCodesEnum.OK_NO_CONTENT.toString());
			responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
			
			
			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);

			//return "{\"result\":\" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
			return jsonResponseContent;//"{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+ "\"code\":\""+RespCodesEnum.OK+"\", "+ "\"userId\":\""+appUser.getId()+"\"}";
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.USER_ERROR_REPORTING_ACTIVITY, realmId, 
					LogStatus.ERROR, 
					Application.USER_ERROR_REPORTING_ACTIVITY+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());
			//return user object
			Response responseObject = new Response();
			responseObject.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			responseObject.setStatus(RespStatusEnum.FAILED.toString());
			
			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);

    		//return "{\"result\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString()+"\"}";
			return jsonResponseContent;//"{\"status\":\""+RespStatusEnum.SUCCESS+"\", "+ "\"code\":\""+RespCodesEnum.OK+"\", "+ "\"userId\":\""+appUser.getId()+"\"}";
    	}
    }

}
