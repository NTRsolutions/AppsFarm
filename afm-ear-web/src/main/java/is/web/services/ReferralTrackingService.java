package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.denominationModels.DenominationModelTable;
import is.ejb.bl.denominationModels.SerDeDenominationModelTable;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.rewardSystems.radius.RadiusProvider;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOCloudtraxConfiguration;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.CloudtraxConfigurationEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.hibernate.validator.constraints.br.CNPJ;

/**
 * A simple REST service which is able to say hello to someone using HelloService Please take a look at the web.xml where JAX-RS
 * is enabled
 */

@Path("/")
public class ReferralTrackingService {
	@Inject
	private Logger logger;

    @Inject
	private DAORealm daoRealm;

    @Inject
    private DAOUserEvent daoUserEvent;

    @Inject
    private DAOAppUser daoAppUser;

    @Inject
	private DAOConversionHistory daoConversionHistory;

    @Inject
	private DAOCloudtraxConfiguration daoCloudtraxConfiguration;

    @Inject
	private DAORadiusConfiguration daoRadiusConfiguration;

    @Context 
    private ServletContext context;

	@Context
	private HttpServletRequest httpRequest;
	
    //example call url: http://127.0.0.1:8080/ab/svc/v1/referral?internalTransactionId=aaaab&offerProviderTransactionId=2sfsdfsafaaa
    @GET
    @Produces("application/json")
    @Path("/v1/referral/")
    public String registerEventUsingQueryRouting(@QueryParam("internalTransactionId") String internalTransactionId,
    							@QueryParam("offerProviderTransactionId") String offerProviderTransactionId) {
		int realmId = -1;

    	try {
    	    String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR"); 
            if (ipAddress == null) { 
                ipAddress = httpRequest.getRemoteAddr(); 
            }

    		String dataContent = "retrieved successful conversion event: "+
					" internalT: "+internalTransactionId+
					" ip: "+ipAddress+
					" offerProviderTransactionId: "+offerProviderTransactionId;
    		logger.info(dataContent);
			Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.INVITATION_ACTIVITY+" "+Application.CONVERSION_IDENTIFIED+" received request: "+dataContent);

			UserEventEntity event = daoUserEvent.findByInternalTransactionId(internalTransactionId);
			if(event == null) {
				Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, 
						LogStatus.OK, 
						Application.INVITATION_ACTIVITY+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_TRANSACTION);
				return "{\"response\":\" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_TRANSACTION+"\"}";
			}
			
			realmId = event.getRealmId();
			RealmEntity realm = daoRealm.findById(realmId);
			
			//check if this conversion was not already identified - if so reject it
			if(event.getConversionDate() != null) {
				Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, 
						LogStatus.ERROR, 
						Application.CONVERSION_ACTIVITY+" status: "+RespStatusEnum.FAILED+
						" code: "+RespCodesEnum.ERROR_DUPLICATE_CONVERSION_IDENTIFIED+
						" internalT: "+internalTransactionId+" aborting conversion registration - user already triggered reward request at: "+event.getRewardRequestDate().toString()+
						" internal transaction id: "+event.getInternalTransactionId()+
						" event category: "+event.getUserEventCategory()+
						" phone number: "+event.getPhoneNumber()+
						" userId: "+event.getUserId());
					logger.severe("CONVERSION_REGISTRATION aborting conversion registration - conversion already identified for this transaction id request at: "+event.getConversionDate().toString()+" internal transaction id: "+event.getInternalTransactionId()+" phone number: "+event.getPhoneNumber()+" userId: "+event.getUserId()+" transaction id: "+event.getInternalTransactionId());
					return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_DUPLICATE_CONVERSION_IDENTIFIED+"\"}";
			} else { 
				//------------------------ record successful conversion -----------------------------
				event.setTransactionId(offerProviderTransactionId);
				event.setConversionDate(new Timestamp(System.currentTimeMillis()));
				//update event
				daoUserEvent.createOrUpdate(event,1);
				Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, 
						LogStatus.OK, 
						Application.CONVERSION_ACTIVITY+" "+
						Application.CONVERSION_ACTIVITY_SUCCESS+
						" successfully persisted conversion: internalT: "+internalTransactionId+" phone number: "+event.getPhoneNumber()+" userId: "+event.getUserId());

				/*
				//create user conversion log
				Application.getElasticSearchLogger().indexUserClick(realmId, event.getPhoneNumber(), 
						"", event.getDeviceType(),  
						event.getOfferId(), 
						event.getOfferSourceId().toLowerCase(), 
						event.getOfferTitle(), 
						event.getAdProviderCodeName(), 
						event.getRewardTypeName(), 
						event.getOfferPayoutInTargetCurrency(), 
						event.getRewardValue(), 
						event.getRewardIsoCurrencyCode(),
						event.getProfitValue(),
						realm.getName(),
						"",
						UserEventType.conversion.toString(),
						event.getInternalTransactionId(),
						"",
						UserEventCategory.INSTALL.toString(),
						"",
						"",
						ipAddress,
						event.getCountryCode());
				*/
				
				//update conversion history (needed to filter out already clicked offers for particular user)
				updateUserConversionHistory(event);
				
				//------------------------ send reward request to reward partner -----------------------------
				if(event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AFA.toString())) {
					//------------------------ send reward request to radius -----------------------------
					return requestRewardRadius(event);//for testing: return requestRewardLocalTesting(event); 
				} 
				else if(event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_INDIA.toString())) {
					//------------------------ send conversion notification to AR -----------------------------
				    //sendConversionNotificationToAB(event);
					//------------------------ send reward request to mode -----------------------------
					return requestRewardMode(event);//for testing: return requestRewardLocalTesting(event); 
				}  
				else if(event.getRewardTypeName().equals(Application.REWARD_PROVIDER_AIR_REWARDZ_KENYA.toString())) {
					//------------------------ send conversion notification to AR -----------------------------
				    //sendConversionNotificationToAB(event);
					//------------------------ send reward request to mode -----------------------------
					return requestRewardMode(event);//for testing: return requestRewardLocalTesting(event); 
				} else if(event.getRewardTypeName().equals(Application.REWARD_PROVIDER_GO_AHEAD_BRIGHTON_HOVE_UK.toString())) {
						//------------------------ send reward request to reward provider -----------------------------
						return requestRewardGoAhead(event); 
				} else { //if no matching reward provider identifed - trigger error
					Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, 
							LogStatus.ERROR, 
							Application.CONVERSION_ACTIVITY+" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_NO_REWARD_TYPE_NAME_DEFINED);
					return "{\"result\":\" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_NO_REWARD_TYPE_NAME_DEFINED+"\"}";
				}
				//return requestRewardLocalTesting(event); 
			}
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, 
					LogStatus.ERROR, 
					Application.CONVERSION_ACTIVITY+" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());

    		return "{\"result\":\" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString()+"\"}";
    	}
    }

    /**  
     * update conversion history for this specific user and offer 
     * entry for this specific offer was already created during click event 
     * just need to update it with conversion date
     */
    private void updateUserConversionHistory(UserEventEntity event) {
    	try {
    		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, event.getRealmId(), 
    				LogStatus.OK, 
    				Application.CONVERSION_ACTIVITY+" "+
    				Application.DOWNLOAD_HISTORY_UPDATE+" "+
    				Application.DOWNLOAD_HISTORY_CONVERSION_UPDATE+" "+
    				" updating conversion history for user with id: "+event.getUserId()+
    				" event: "+event.getOfferTitle()+
    				" offer provider: "+event.getAdProviderCodeName()+
    				" internalT: "+event.getInternalTransactionId());
    		ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
    		if(conversionHistory==null) {
    			throw new Exception("Unable to identify conversion history for this user!");
    		}
    		
    		//only update conversionTime entry that matches specific offer id for which this conversion was identified
    		ConversionHistoryEntry conversionHistoryEntryToUpdate = daoConversionHistory.getConversionEntryToUpdate(event, conversionHistory);
    		if(conversionHistoryEntryToUpdate==null) {
    			throw new Exception("Unable to idetnify conversion entry for offer with id: "+event.getOfferId()+
    					" internalT: "+event.getInternalTransactionId());
    		}
    		
    		//update entity 
    		conversionHistoryEntryToUpdate.setConversionTimestamp(event.getConversionDate());
    		conversionHistoryEntryToUpdate.setRewardTimestamp(event.getRewardDate());
    		conversionHistoryEntryToUpdate.setRewardStatusMessage(event.getRewardResponseStatusMessage());
    		conversionHistoryEntryToUpdate.setRewardStatus(event.getRewardRequestStatus());//if reward req failed - store it in reward status
    		conversionHistory.setGenerationTime(new Timestamp(System.currentTimeMillis()));//if reward req failed - store it in reward status
    		//persist in db (dao takes care of json serialisation)
    		daoConversionHistory.createOrUpdate(conversionHistory);
    	} catch(Exception exc) {
    		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(), 
    				LogStatus.ERROR, 
    				Application.CONVERSION_ACTIVITY+" "+
    				Application.DOWNLOAD_HISTORY_UPDATE+
    				" error adding conversion history for user with id: "+event.getUserId()+
    				" event: "+event.getOfferTitle()+
    				" offer provider: "+event.getAdProviderCodeName()+
    				" internalT: "+event.getInternalTransactionId()+
    				" error: "+exc.toString());
    	}
    }
        
    //------------------------------------ issue reward request to mode --------------------------------
    private String requestRewardMode(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId()); 

			//if testing is enabled - send notification and abort real mode testing
			if(realm.isTestMode()) {
				sendRewardNotification(event);
				logger.info("in test mode - aborting real reward system request...");
				return "";
			}
			
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+" "+
					Application.REWARD_REQUEST_IDENTIFIED+
					" internalT: "+event.getInternalTransactionId()+
					" issuing credit request for payout: "+event.getOfferPayout()+
					" (in target currency: "+event.getRevenueValue()+
					" phone: "+event.getPhoneNumber()+
					" rewardType: "+event.getRewardTypeName()+
					" reward: "+event.getRewardValue()+
					" reward currency: "+event.getRewardIsoCurrencyCode());

			//logger.info("credit request for user: "+event.getUserId()+
            //		" internalTransaction: "+event.getInternalTransactionId()+
            //		" currency: "+event.getOfferPayoutIsoCurrencyCode()+" payout: "+event.getOfferPayout());
			
			//--------------------------- handle request to rewarding system ---------------------------------
			//extract mode configuration
			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();
			String url = realm.getModeCreditUrl();

			//add request header
            //TODO following values set for testing 
            //event.setRewardValue(5.0);
			//event.setPhoneNumber("0786641885");
            
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(realm.getConnectionTimeout()*1000);
            con.setReadTimeout(realm.getReadTimeout()*1000);
			//add reuqest header
			con.setRequestMethod("POST");
			String urlParameters = "MSISDN="+event.getPhoneNumber()+
					"&OriginTransactionID="+event.getId()+ //"&OriginTransactionID="+event.getInternalTransactionId()+
					"&Reward="+event.getRewardValue()+
					"&ISOCurrCode="+event.getRewardIsoCurrencyCode()+
					"&User="+bpUser+
					"&Password="+bpPass;
		
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			//System.out.println("\nSending 'POST' request to URL : " + url);
			//System.out.println("Post parameters : " + urlParameters);
			//System.out.println("Response Code : " + responseCode);

			//optimistically update event before call and only set it to faulty one after (when we 100% know that transaction will not be processed)
			//this is to avoid situation when we overwrite reward response with reward request object data
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event,2);

			String responseString = "OK";
			String statusMessage = responseString;
			
			//TODO if we get error - notify AR about problem with reward ask Rodgers if we only need 200 response to know that request was successful)
			if(responseCode == 200) {
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
		 
				responseString = response.toString();
				//code 200 and status 0 - request to credit user received successfully
				//code 200 and status 1 - request with similar transaction id already exists
				//code 403 - authentication failure
				String STATUS_FAILED="\"Status\":1"; //request with similar transaction id already exists
				statusMessage = "Unable to parse";
				try {
					statusMessage = responseString.substring(responseString.indexOf("Msg\":")+6, responseString.indexOf(",\"OriginTransactionID")-1);
				} catch(Exception exc) {}
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" credit successfully requested for internalT: "+
						event.getInternalTransactionId()+ " reward value: "+event.getRewardValue()+
							" rewardType: "+event.getRewardTypeName()+
							" offerPayout: "+event.getOfferPayout()+" offer payout currency: "+event.getOfferPayoutIsoCurrencyCode()+
							" rewardUrl: "+url+
							" urlParams: "+urlParameters+
							" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);
				
				//send correctly formatted response for supersonic (read manual about callbacks: http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" sent successful response: "+successfulResponseStatus);
				
				return successfulResponseStatus;
			} else {
				if(responseCode == 403) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
							LogStatus.ERROR, 
							Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+" error during credit request: authentication failure (please make sure that login/pass to mode are correctly set) for event: "+event.getUserId()+" phone: "+event.getPhoneNumber()+" internalT: "+event.getInternalTransactionId()+ 
							" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_AUTHENTICATION_FAILURE);
					//update event
					event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
					event.setRewardRequestStatusMessage("Authentication failure - please make sure that login/pass to mode are correctly set");
					daoUserEvent.createOrUpdate(event,3);

					//update conversion history to store information about failed reward attempt
					updateUserConversionHistory(event);

					return "{\"response\":\" status: "+RespStatusEnum.FAILED+" "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.ERROR_AUTHENTICATION_FAILURE+"\"}";
				} else {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
							LogStatus.ERROR, 
							Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
							" error during credit request: unknown response code: "+responseCode +
							" for event: "+event.getUserId()+
							" rewardType: "+event.getRewardTypeName()+
							" phone: "+event.getPhoneNumber()+
							" internalT: "+event.getInternalTransactionId()+
							" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);

					//update event
					event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
					event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
					event.setRewardRequestStatusMessage("Unknown response code: "+responseCode+" was expecting 200 or 403");
					daoUserEvent.createOrUpdate(event,4);
					
					//update conversion history to store information about failed reward attempt
					updateUserConversionHistory(event);
					
					return "{\"response\":\" status: "+RespStatusEnum.FAILED+" "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE+"\"}";
				}
			}
			//--------------------------- handle request to rewarding system ---------------------------------

		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
					" internalT: "+event.getInternalTransactionId()+" error crediting user: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: "+exc.toString());
			daoUserEvent.createOrUpdate(event,7);
			
			//update conversion history to store information about failed reward attempt
			updateUserConversionHistory(event);
			
			return "{\"response\":\" status: "+RespStatusEnum.FAILED+" "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }

    //------------------------------------ issue reward request to Radius --------------------------------
    private String requestRewardRadius(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId());
			String phoneNumber = event.getPhoneNumber();
			
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+" "+
					Application.REWARD_REQUEST_IDENTIFIED+
					" internalT: "+event.getInternalTransactionId()+
					" issuing credit request for payout: "+event.getOfferPayout()+
					" (in target currency: "+event.getRevenueValue()+
					" phone: "+event.getPhoneNumber()+" adjusted: ("+phoneNumber+")"+
					" rewardType: "+event.getRewardTypeName()+
					" reward: "+event.getRewardValue()+
					" reward currency: "+event.getRewardIsoCurrencyCode());
			
			//--------------------------- handle request to rewarding system ---------------------------------
			CloudtraxConfigurationEntity ctraxConfig = daoCloudtraxConfiguration.findByNetworkName(event.getAfaNetworkName());
			RadiusConfigurationEntity radiusConfig = daoRadiusConfiguration.findById(ctraxConfig.getRadiusServer1Id());
			RadiusProvider radiusProvider = new RadiusProvider(radiusConfig);
			AppUserEntity appUser = daoAppUser.findById(event.getUserId());

			//if reached this stage - indicate that reward request was made
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event,2);

			//issue reward request
			RespStatusEnum rewardStatus = radiusProvider.addTime(appUser.getEmail(), (int)event.getRewardValue()); //assume reward value in this case corresponds to minutes

			if(event.getRewardDate() != null) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
						LogStatus.WARNING, 
						Application.REWARD_ACTIVITY+" "+
						Application.REWARD_RESPONSE_ACTIVITY+
						" reward date already set for internal trans id: "+event.getInternalTransactionId()+": "+
						" internalT: "+event.getInternalTransactionId()+
						event.getRewardDate().toString()+" udpating with new time");
			} 

			if(rewardStatus.equals(RespStatusEnum.SUCCESS.toString())) { //update user event data
				event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				event.setRewardResponseStatusMessage(RespStatusEnum.SUCCESS.toString());
				event.setApproved(true);
			} else {
				event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				event.setRewardResponseStatusMessage(RespStatusEnum.FAILED.toString());
				event.setApproved(false);
			}

			//--------------------------------- Reward Response handling starts ---------------------------
    		String dataContent = "intercepted reward notification: "+
					"internalTransactionId: "+event.getInternalTransactionId()+
					" status: ["+rewardStatus.toString()+"]"+
    				" statusMessage: [ Radius response: "+rewardStatus.toString()+"]";
    		logger.info(dataContent);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+
					Application.REWARD_RESPONSE_ACTIVITY+" "+
					Application.REWARD_RESPONSE_IDENTIFIED+" "+dataContent);

			//update reward date
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event,8);

			//update conversion history (needed to filter out already clicked offers for particular user)
			updateUserConversionHistory(event);

			//notify user via sms if reward was successful
			String smsMessageContent = "You have been rewarded "+event.getRewardValue()+
					" minutes of free Internet access";
			sendRewardNotificationSMS(event, realm, smsMessageContent);

			//send correctly formatted response for supersonic (read manual about callbacks: http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
			return "{\"response\":\" status: "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
			
		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.REWARD_ACTIVITY+" "+
					Application.REWARD_REQUEST_ACTIVITY+
					" error during reward request for internal trans id: "+event.getInternalTransactionId()+": "+
					" user phone number: "+event.getPhoneNumber()+
					" error: "+exc.toString());
			
			event.setRewardResponseStatus(RewardStatus.FAILED.toString());
			event.setRewardResponseStatusMessage(exc.toString());
			event.setApproved(false);
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event,8);

			return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }

    //------------------------------------ issue reward request to GoAhead --------------------------------
    private String requestRewardGoAhead(UserEventEntity event) {
		try {
			
			RealmEntity realm = daoRealm.findById(event.getRealmId());
			String phoneNumber = event.getPhoneNumber();
			
			if(event.getPhoneNumber().startsWith("44")) //do nothing as user already provided country code 
			{
				phoneNumber=event.getPhoneNumber();
			}
			else if(event.getPhoneNumber().startsWith("0")) //cut 0 and add 44 
			{
				phoneNumber="44"+event.getPhoneNumber().substring(1);
			}
			
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+" "+
					Application.REWARD_REQUEST_IDENTIFIED+
					" internalT: "+event.getInternalTransactionId()+
					" issuing credit request for payout: "+event.getOfferPayout()+
					" (in target currency: "+event.getRevenueValue()+
					" phone: "+event.getPhoneNumber()+" adjusted: ("+phoneNumber+")"+
					" rewardType: "+event.getRewardTypeName()+
					" reward: "+event.getRewardValue()+
					" reward currency: "+event.getRewardIsoCurrencyCode());

			//logger.info("credit request for user: "+event.getUserId()+
            //		" internalTransaction: "+event.getInternalTransactionId()+
            //		" currency: "+event.getOfferPayoutIsoCurrencyCode()+" payout: "+event.getOfferPayout());
			
			//--------------------------- handle request to rewarding system ---------------------------------
			String smsHashCode = "sdlfjwlj3l23j4lkjsdlfjsdf";
			String smsMessageContent = "You have been rewarded \u00A3"+event.getRewardValue()+
											" off your next journey on Brighton and Hove bus travel - "
											+ "Please just use the following code to top up your ticket wallet in the Brighton and Hove M Tickets app"
											+ " - https://appsto.re/gb/MV_5I.i"+
											" Code: "+smsHashCode;
			//smsMessageContent = URLEncoder.encode(smsMessageContent, "UTF-8");
			smsMessageContent = URLEncoder.encode(smsMessageContent, "ISO-8859-1");
			//setup clickatell url to call
			String urlToCall = "http://api.clickatell.com/http/sendmsg?user=Bluepodmedia&"
					+ "password=VIgLTKVHCZXdAN&api_id=3538043&to="
					+ phoneNumber
					+ "&text="+smsMessageContent;

			//execute call
	    	HttpURLConnection urlConnection = null;
	    	BufferedReader in = null;
	        String reqResponse = "";

		 	try {
	    		URL url = new URL(urlToCall);
	            urlConnection = (HttpURLConnection)url.openConnection();
	            urlConnection.setRequestProperty("Accept", "application/json");
	            urlConnection.setConnectTimeout(realm.getConnectionTimeout() * 1000);
	            urlConnection.setReadTimeout(realm.getReadTimeout() * 1000);
	            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	            String inputLine;
	            while ((inputLine = in.readLine()) != null) {
	            	reqResponse = inputLine;
	            }
	    	} finally {
	    		if(in != null) {
	    			in.close();
	    		}
	    		if(urlConnection != null) {
	    			urlConnection.disconnect();
	    		}
	    	}
	    	
			int responseCode = urlConnection.getResponseCode();
	    	logger.info("SMS gateway response code: "+responseCode+" Response content: "+reqResponse+" phone number: "+event.getPhoneNumber());

			//optimistically update event before call and only set it to faulty one after (when we 100% know that transaction will not be processed)
			//this is to avoid situation when we overwrite reward response with reward request object data
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event,2);

			
			String responseString = reqResponse;
			String statusMessage = responseString;
			String status = "";
			//TODO if we get error - notify AR about problem with reward ask Rodgers if we only need 200 response to know that request was successful)
			if(responseCode == 200) {
				status = "SUCCESS";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" credit successfully requested for internalT: "+
						event.getInternalTransactionId()+ " reward value: "+event.getRewardValue()+
							" offerPayout: "+event.getOfferPayout()+
							" offer payout currency: "+event.getOfferPayoutIsoCurrencyCode()+
							" rewardType: "+event.getRewardTypeName()+
							" rewardUrl: "+urlToCall+
							" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);

				//--------------------------------- Reward Response handling starts ---------------------------
				//send correctly formatted response for supersonic (read manual about callbacks: http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" sent successful response: "+successfulResponseStatus);

				//store successful reward response - normally this should be interecepted in RewardTrackingService
	    		String dataContent = "intercepted reward notification: "+
						"internalTransactionId: "+event.getInternalTransactionId()+
						" status: ["+responseCode+"]"+
	    				" statusMessage: ["+responseString+"]";
	    		logger.info(dataContent);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+
						Application.REWARD_RESPONSE_ACTIVITY+" "+
						Application.REWARD_RESPONSE_IDENTIFIED+" "+dataContent);

				//logger.info("REWARD_DATA_UPDATE updating user event with reward notification data");
				//generate event object and persist it in db
				if(event.getRewardDate() != null) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
							LogStatus.WARNING, 
							Application.REWARD_ACTIVITY+" "+
							Application.REWARD_RESPONSE_ACTIVITY+
							" reward date already set for internal trans id: "+event.getInternalTransactionId()+": "+
							" internalT: "+event.getInternalTransactionId()+
							event.getRewardDate().toString()+" udpating with new time");
				} 
				//update reward info in db
				if(status.toUpperCase().equals(RewardStatus.SUCCESS.toString())) {
					event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				} else if(status.toUpperCase().equals(RewardStatus.FAILED.toString())) {
					event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				} else if(status.toUpperCase().equals(RewardStatus.PENDING.toString())) {
					event.setRewardResponseStatus(RewardStatus.PENDING.toString());
				} else {
					event.setRewardResponseStatus(RewardStatus.UNKNOWN.toString());
					
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
							LogStatus.ERROR, 
							Application.REWARD_ACTIVITY+" "+
							Application.REWARD_RESPONSE_ACTIVITY+" "+
							Application.REWARD_RESPONSE_FAILED+
							" Unknown reward status code"+
							" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);

					//update reward date
					event.setRewardResponseStatusMessage(responseString);
					event.setRewardDate(new Timestamp(System.currentTimeMillis()));
					event.setApproved(true);
					daoUserEvent.createOrUpdate(event,8);

					//update conversion history (needed to filter out already clicked offers for particular user)
					updateUserConversionHistory(event);
					
					return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE+"\"}";
				}
				
				//update reward date
				event.setRewardResponseStatusMessage(responseString);
				event.setRewardDate(new Timestamp(System.currentTimeMillis()));
				event.setApproved(true);
				daoUserEvent.createOrUpdate(event,8);

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
						LogStatus.OK,
						Application.REWARD_ACTIVITY+" "+Application.REWARD_RESPONSE_ACTIVITY+
						" successfully rewarded event with internal transaction id: "+event.getTransactionId()+
						" internalT: "+event.getInternalTransactionId()+
						" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);

				
				//update conversion history to store information about successful reward attempt
				updateUserConversionHistory(event);
				
				return successfulResponseStatus;
				//--------------------------------- Reward Response handling ends---------------------------
			} else {
				status = "FAILED";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.ERROR, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" error during credit request: unknown response code: "+responseCode +
						" for event: "+event.getUserId()+
						" rewardType: "+event.getRewardTypeName()+
						" phone: "+event.getPhoneNumber()+
						" internalT: "+event.getInternalTransactionId()+
						" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);

				//update event
				event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
				event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
				event.setRewardRequestStatusMessage("Unknown response code: "+responseCode+" was expecting 200 or 403");
				daoUserEvent.createOrUpdate(event,4);
				
				//update conversion history to store information about failed reward attempt
				updateUserConversionHistory(event);

				//--------------------------------- Reward Response handling starts ---------------------------
				//send correctly formatted response for supersonic (read manual about callbacks: http://documents.supersonicads.com/SupersonicAds%20-%20Publisher%20Integration.pdf)
				String successfulResponseStatus = "{\"response\":\" status: "+event.getTransactionId()+":OK"+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
						" sent successful response: "+successfulResponseStatus);

				//store successful reward response - normally this should be interecepted in RewardTrackingService
	    		String dataContent = "intercepted reward notification: "+
						"internalTransactionId: "+event.getInternalTransactionId()+
						" status: ["+responseCode+"]"+
	    				" statusMessage: ["+responseString+"]";
	    		logger.info(dataContent);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
						LogStatus.OK, 
						Application.REWARD_ACTIVITY+" "+
						Application.REWARD_RESPONSE_ACTIVITY+" "+
						Application.REWARD_RESPONSE_IDENTIFIED+" "+dataContent);

				//logger.info("REWARD_DATA_UPDATE updating user event with reward notification data");
				//generate event object and persist it in db
				if(event.getRewardDate() != null) {
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
							LogStatus.WARNING, 
							Application.REWARD_ACTIVITY+" "+
							Application.REWARD_RESPONSE_ACTIVITY+
							" reward date already set for internal trans id: "+event.getInternalTransactionId()+": "+
							" internalT: "+event.getInternalTransactionId()+
							event.getRewardDate().toString()+" udpating with new time");
				} 
				//update reward info in db
				if(status.toUpperCase().equals(RewardStatus.SUCCESS.toString())) {
					event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
				} else if(status.toUpperCase().equals(RewardStatus.FAILED.toString())) {
					event.setRewardResponseStatus(RewardStatus.FAILED.toString());
				} else if(status.toUpperCase().equals(RewardStatus.PENDING.toString())) {
					event.setRewardResponseStatus(RewardStatus.PENDING.toString());
				} else {
					event.setRewardResponseStatus(RewardStatus.UNKNOWN.toString());
					
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
							LogStatus.ERROR, 
							Application.REWARD_ACTIVITY+" "+
							Application.REWARD_RESPONSE_ACTIVITY+" "+
							Application.REWARD_RESPONSE_FAILED+
							" Unknown reward status code"+
							" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);

					//update reward date
					event.setRewardResponseStatusMessage(responseString);
					event.setRewardDate(new Timestamp(System.currentTimeMillis()));
					event.setApproved(true);
					daoUserEvent.createOrUpdate(event,8);

					//update conversion history (needed to filter out already clicked offers for particular user)
					updateUserConversionHistory(event);
					
					return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE+"\"}";
				}
				
				//update reward date
				event.setRewardResponseStatusMessage(responseString);
				event.setRewardDate(new Timestamp(System.currentTimeMillis()));
				event.setApproved(true);
				daoUserEvent.createOrUpdate(event,8);

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realm.getId(), 
						LogStatus.OK,
						Application.REWARD_ACTIVITY+" "+Application.REWARD_RESPONSE_ACTIVITY+
						" successfully rewarded event with internal transaction id: "+event.getTransactionId()+
						" internalT: "+event.getInternalTransactionId()+
						" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);

				return successfulResponseStatus;
				//--------------------------------- Reward Response handling ends---------------------------
			}
			//--------------------------- handle request to rewarding system ---------------------------------

		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
					" internalT: "+event.getInternalTransactionId()+
					" rewardType: "+event.getRewardTypeName()+
					" error crediting user: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: "+exc.toString());
			daoUserEvent.createOrUpdate(event,7);
			
			//update conversion history to store information about failed reward attempt
			updateUserConversionHistory(event);
			
			return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }

    
    //++++++++++++++++++++++++++++++++++ testing method ++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++ testing method ++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++ testing method ++++++++++++++++++++++++++++++++++++++++++++++++++++
    private String requestRewardLocalTesting(UserEventEntity event) {
		try {
			RealmEntity realm = daoRealm.findById(event.getRealmId()); 

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+" "+Application.REWARD_REQUEST_IDENTIFIED+" internalT: "+event.getInternalTransactionId()+" issuing credit request for user: "+event.getUserId()+" phone: "+event.getPhoneNumber());
            logger.info("credit request for user: "+event.getUserId()+" phone: "+event.getPhoneNumber()+" internalTransaction: "+event.getInternalTransactionId()+" currency: "+event.getOfferPayoutIsoCurrencyCode()+" payout: "+event.getOfferPayout());

			//--------------------------- handle request to rewarding system ---------------------------------
			//extract mode configuration
			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();

			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage("OK");
			daoUserEvent.createOrUpdate(event,2);

			//add request header
            //TODO following values set for testing 
            event.setRewardValue(5.0);
			event.setPhoneNumber("0786641885");
			//add reuqest header
			String urlParameters = "http://localhost:8080/ab/svc/v1/reward/"+event.getId()+"/"+RespStatusEnum.SUCCESS.toString()+"/OK";
			System.out.println("sending request: "+urlParameters);            
			
        	HttpURLConnection urlConnection = null;
        	BufferedReader in = null;
	        String reqResponse = "";
        	try {
    			URL url = new URL(urlParameters);
    	        urlConnection = (HttpURLConnection)url.openConnection();
    	        urlConnection.setConnectTimeout(realm.getConnectionTimeout() * 1000);
    	        urlConnection.setReadTimeout(realm.getReadTimeout() * 1000);
    	        in = new BufferedReader(
    	                                new InputStreamReader(
    	                                urlConnection.getInputStream()));
    	        String inputLine;
    	        while ((inputLine = in.readLine()) != null) {
    	        	reqResponse = inputLine;
    	        }
        	} finally {
        		if(in != null) {
        			in.close();
        		}
        		if(urlConnection != null) {
        			urlConnection.disconnect();
        		}
        	}

	        
			//System.out.println("\nSending 'POST' request to URL : " + url);
			//System.out.println("Post parameters : " + urlParameters);
			//System.out.println("Response Code : " + responseCode);

			//optimistically update event before call and only set it to faulty one after (when we 100% know that transaction will not be processed)
			//this is to avoid situation when we overwrite reward response with reward request object data

			String responseString = "OK";
			String statusMessage = responseString;

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
					" credit successfully requested for internalT: "+
					event.getInternalTransactionId()+ " reward value: "+event.getRewardValue()+
						" offerPayout: "+event.getOfferPayout()+" offer payout currency: "+event.getOfferPayoutIsoCurrencyCode()+
						" urlParams: "+urlParameters+
						" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);
			return "{\"response\":\" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";
			//--------------------------- handle request to rewarding system ---------------------------------

		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.REWARD_ACTIVITY+" "+Application.REWARD_REQUEST_ACTIVITY+
					" internalT: "+event.getInternalTransactionId()+" error crediting user: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: "+exc.toString());
			daoUserEvent.createOrUpdate(event,7);
			
			return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }

    private String requestRewardTest(UserEventEntity event) {
		try {
			//extract mode configuration
			RealmEntity realm = daoRealm.findById(event.getRealmId()); 
			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();

			//cut off + sign from mobile number
			String mobileNumber = event.getPhoneNumber();
			mobileNumber = mobileNumber.replace("+", "");

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					"MODE_CREDIT_ACTIVITY internalT: "+event.getInternalTransactionId()+" credit request for user: "+event.getUserId()+" phone: "+event.getPhoneNumber());
            logger.info("credit request for user: "+event.getUserId()+" phone: "+event.getPhoneNumber()+" internalTransaction: "+event.getInternalTransactionId()+" currency: "+event.getOfferPayoutIsoCurrencyCode()+" payout: "+event.getOfferPayout());

			String responseString = "empty";
			String statusMessage = responseString;
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					"MODE_REQUEST_CREDIT_ACTIVITY credit successfully requested for internalT: "+
					event.getInternalTransactionId()+ " reward value: "+event.getRewardValue()+
						" offerPayout: "+event.getOfferPayout()+" offer payout currency: "+event.getOfferPayoutIsoCurrencyCode()+" status message: "+statusMessage);
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.SUCCESS.toString());
			event.setRewardRequestStatusMessage(statusMessage);
			daoUserEvent.createOrUpdate(event,7);
			
			//add reuqest header
			String urlParameters = "http://localhost:8080/ab/svc/v1/reward/"+event.getInternalTransactionId()+"/"+RespStatusEnum.SUCCESS.toString()+"/OK";

        	HttpURLConnection urlConnection = null;
        	BufferedReader in = null;
        	try {
    			URL url = new URL(urlParameters);
    	        urlConnection = (HttpURLConnection)url.openConnection();
    	        urlConnection.setConnectTimeout(realm.getConnectionTimeout() * 1000);
    	        urlConnection.setReadTimeout(realm.getReadTimeout() * 1000);
    	        in = new BufferedReader(
    	                                new InputStreamReader(
    	                                urlConnection.getInputStream()));
    	        String reqResponse = "";
    	        String inputLine;
    	        while ((inputLine = in.readLine()) != null) {
    	        	reqResponse = inputLine;
    	        }
        	} finally {
        		if(in != null) {
        			in.close();
        		}
        		if(urlConnection != null) {
        			urlConnection.disconnect();
        		}
        	}

	        
			return "{\"response\":\" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT+"\"}";

		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, 
					LogStatus.ERROR, 
					"MODE_REQUEST_CREDIT_ACTIVITY internalT: "+event.getInternalTransactionId()+" error crediting user: "+exc.toString());
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: "+exc.toString());
			daoUserEvent.createOrUpdate(event,7);
			
			return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }
    
    private void sendRewardNotification(UserEventEntity event){
    	try {
        	String notificationPayload = "You have been rewarded "+event.getRewardIsoCurrencyCode()+
        			" "+event.getRewardValue();

    		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_ACTIVITY+" "+
					Application.REWARD_NOTIFICATION_ACTIVITY+" "+
					Application.REWARD_NOTIFICATION_SUCCESS+" "+
					" attempting to send reward notification for device type: "+event.getDeviceType()+
					" status: "+RespStatusEnum.SUCCESS+
					" phone number: "+event.getPhoneNumber()+
					" internalT: "+event.getInternalTransactionId());

        	if(event.getDeviceType().equals(DeviceType.iOS.toString())) {
        		//push notification
        		String rootDir = new java.io.File( "." ).getCanonicalPath();
        		IOSNotificationSender iOSNotificationSender = new IOSNotificationSender();
        		String resultPayload = iOSNotificationSender.pushNotification(rootDir, event.getIosDeviceToken(),
        				notificationPayload);
    			
        		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
    					LogStatus.OK, 
    					Application.REWARD_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_SUCCESS+" "+
    					" successfully issued reward notification for device type: "+event.getDeviceType()+
    					" status: "+RespStatusEnum.SUCCESS+
    					" phone number: "+event.getPhoneNumber()+
    					" iOS device token: "+event.getIosDeviceToken()+
    					" internalT: "+event.getInternalTransactionId()+
    					" rootDir: "+rootDir+
    					" payload content: "+resultPayload);
        		
    			//update notification status
    	        event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
    			event.setMobileAppNotificationStatusMessage(resultPayload);
    			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
    			daoUserEvent.createOrUpdate(event,8);
        	} else if(event.getDeviceType().equals(DeviceType.Android.toString())) {
        		//push notification
        		String googleNotificationSenderAccessKey = daoRealm.findById(event.getRealmId()).getGoogleNotificationsAccessKey();
        		GoogleNotificationSender gns = new GoogleNotificationSender(googleNotificationSenderAccessKey);
    			String strSendStatus = gns.sendMessage(event.getAndroidDeviceToken(), notificationPayload);
        		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
    					LogStatus.OK, 
    					Application.REWARD_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_SUCCESS+" "+
    					" successfully issued reward notification for device type: "+event.getDeviceType()+
    					" status: "+RespStatusEnum.SUCCESS+
    					" phone number: "+event.getPhoneNumber()+
    					" android device token: "+event.getAndroidDeviceToken()+
    					" google notification access key: "+googleNotificationSenderAccessKey+
    					" internalT: "+event.getInternalTransactionId()+
    					" payload content: "+strSendStatus);
    			//update notification status
    	        event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
    			event.setMobileAppNotificationStatusMessage(strSendStatus);
    			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
    			daoUserEvent.createOrUpdate(event,8);
        	} else if(event.getDeviceType().equals(DeviceType.Windows.toString())) {
    			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
    					LogStatus.ERROR,
    					Application.REWARD_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_FAILURE+" "+
    					" phone number: "+event.getPhoneNumber()+
    					" error sending notification: notification for device: "+event.getDeviceType()+" not supported"+
    					" internalT: "+event.getInternalTransactionId());
        	} else {
    			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
    					LogStatus.ERROR,
    					Application.REWARD_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_ACTIVITY+" "+
    					Application.REWARD_NOTIFICATION_FAILURE+" "+
    					" phone number: "+event.getPhoneNumber()+
    					" error sending notification: notification for device: "+event.getDeviceType()+" not supported"+
    					" internalT: "+event.getInternalTransactionId());
        	}
		} catch(Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, event.getRealmId(), 
					LogStatus.ERROR, 
					Application.REWARD_ACTIVITY+" "+
					Application.REWARD_NOTIFICATION_ACTIVITY+
					Application.REWARD_NOTIFICATION_FAILURE+" "+
					" error sending notification: notification for device: "+event.getDeviceType()+" not supported"+
					" internalT: "+event.getInternalTransactionId()+
					" error: "+exc.toString());
			//update notification status
	        event.setMobileAppNotificationStatus(RespStatusEnum.FAILED.toString());
			event.setMobileAppNotificationStatusMessage(exc.toString());
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event,8);
    	} 
    }
    
    public void sendRewardNotificationSMS(UserEventEntity event, 
    		RealmEntity realm, String smsMessageContent) {
		try {
			smsMessageContent = URLEncoder.encode(smsMessageContent, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.toString());
		}
		//setup clickatell url to call
		String urlToCall = "http://api.clickatell.com/http/sendmsg?user=Bluepodmedia&"
				+ "password=VIgLTKVHCZXdAN&api_id=3538043&to="
				+ event.getPhoneNumber()
				+ "&text="+smsMessageContent;

		//execute call
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        int responseCode = -1;
        
	 	try {
    		URL url = new URL(urlToCall);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(realm.getConnectionTimeout() * 1000);
            urlConnection.setReadTimeout(realm.getReadTimeout() * 1000);
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
            	reqResponse = inputLine;
            }
            responseCode = urlConnection.getResponseCode();
    	} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_NOTIFICATION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.REWARD_NOTIFICATION_ACTIVITY+" "+Application.REWARD_NOTIFICATION_ACTIVITY+
					" internalT: "+event.getInternalTransactionId()+
					" rewardType: "+event.getRewardTypeName()+
					" error crediting user: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			//update event
			event.setRewardRequestDate(new Timestamp(System.currentTimeMillis()));
			event.setRewardRequestStatus(RespStatusEnum.FAILED.toString());
			event.setRewardRequestStatusMessage("Internal server error: "+exc.toString());
			daoUserEvent.createOrUpdate(event,7);
	 	} finally {
    		if(in != null) {
    			try {
        			in.close();
    			} catch (Exception exc) {
    				logger.severe(exc.toString());
    			}
    		}
    		if(urlConnection != null) {
    			urlConnection.disconnect();
    		}
    	}
    	
    	logger.info("SMS gateway response code: "+responseCode+" Response content: "+reqResponse+" phone number: "+event.getPhoneNumber());
		String responseString = reqResponse;
		String statusMessage = responseString;
		String status = "";
		//TODO if we get error - notify AR about problem with reward ask Rodgers if we only need 200 response to know that request was successful)
		if(responseCode == 200) {
			status = "SUCCESS";
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.OK, 
					Application.REWARD_NOTIFICATION_ACTIVITY+" "+Application.REWARD_NOTIFICATION_ACTIVITY+
					" reward notification successfully issued for internalT: "+
					event.getInternalTransactionId()+ " reward value: "+event.getRewardValue()+
						" offerPayout: "+event.getOfferPayout()+
						" offer payout currency: "+event.getOfferPayoutIsoCurrencyCode()+
						" rewardType: "+event.getRewardTypeName()+
						" rewardUrl: "+urlToCall+
						" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);
			//update event
			event.setMobileAppNotificationStatus(RespStatusEnum.SUCCESS.toString());
			event.setMobileAppNotificationStatusMessage(responseString);
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event,8);
		} else {
			status = "FAILED";
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), 
					LogStatus.ERROR, 
					Application.REWARD_NOTIFICATION_ACTIVITY+" "+Application.REWARD_NOTIFICATION_ACTIVITY+
					" error during reward notification: unknown response code: "+responseCode +
					" for event: "+event.getUserId()+
					" rewardType: "+event.getRewardTypeName()+
					" phone: "+event.getPhoneNumber()+
					" internalT: "+event.getInternalTransactionId()+
					" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE);
			//update event
			event.setMobileAppNotificationStatus(RespStatusEnum.FAILED.toString());
			event.setMobileAppNotificationStatusMessage(responseString);
			event.setMobileAppNotificationDate(new Timestamp(System.currentTimeMillis()));
			daoUserEvent.createOrUpdate(event,4);
		}
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

}
