package is.ejb.bl.rewardSystems.mode;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.offerProviders.fyber.FyberAPIManager;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersAPIManager;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.DataEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.FindAllOfferGroups;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.Request;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.Response;
import is.ejb.bl.offerProviders.minimob.MinimobAPIManager;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.bl.offerWall.persistence.OfferPersistenceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


//dynamic java class generation from json:
//http://jsongen.byingtondesign.com/
//https://javafromjson.dashingrocket.com/

public class TestModeManager {

	public void requestRewardMode(int internalT, String phoneNumber, String rewardValue) {
		try {
			//--------------------------- handle request to rewarding system ---------------------------------
			//extract mode configuration
			String bpUser = "bpuser";
			String bpPass = "6_t89j^2Ht";
			String url = "http://130.211.67.26:9090/mode/bluepodapi/v1/credit/";

			//add request header
            //TODO following values set for testing 
            //event.setRewardValue(5.0);
			//event.setPhoneNumber("0786641885");
            
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
			//add reuqest header
			con.setRequestMethod("POST");
			String urlParameters = "MSISDN="+phoneNumber+
					"&OriginTransactionID="+internalT+ //"&OriginTransactionID="+event.getInternalTransactionId()+
					"&Reward="+rewardValue+
					"&ISOCurrCode=KSH"+
					"&User="+bpUser+
					"&Password="+bpPass;
		
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);


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
					System.out.println("Status message: "+statusMessage);
				} catch(Exception exc) {}
			} else {
				if(responseCode == 403) {
					//return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_AUTHENTICATION_FAILURE+"\"}";
				} else {
					//return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_UNKNOWN_RESPONSE_CODE+"\"}";
				}
			}
			//--------------------------- handle request to rewarding system ---------------------------------
		} catch(Exception exc) {
			exc.printStackTrace();
			//return "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}
    }

	public static void main(String[] args) {
		new TestModeManager();
	}
	
	public TestModeManager() {
		//requestRewardMode(1, "0701835931", "10");
		//requestRewardMode(22222, "0700702247", "10");
		requestRewardMode(2, "0723674099", "10");
		
	}
	
}
