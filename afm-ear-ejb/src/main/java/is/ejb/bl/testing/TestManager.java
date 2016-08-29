package is.ejb.bl.testing;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationNameEnum;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.WalletTransactionStatus;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.rewardSystems.radius.RadiusProvider;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.ejb.dl.entities.WalletPayoutOfferTransactionEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.bcel.generic.ISUB;
import org.apache.commons.codec.digest.DigestUtils;
import org.zendesk.client.v2.model.Ticket;

@Stateless
public class TestManager {

	@Inject
	private Logger logger;

	@Inject
	private DAORewardType daoRewardType;

    public boolean isTestModeEnabledForRewardType(RealmEntity realm, UserEventEntity event) {
    	try {
    		boolean isTestModeEnabledForRewardType = false;
    		if(event.getRewardTypeName() != null && event.getRewardTypeName().length() >0) {
    			RewardTypeEntity rewardTypeEntity = daoRewardType.findByRealmIdAndName(realm.getId(), event.getRewardTypeName());
    			if(rewardTypeEntity != null && rewardTypeEntity.getName() != null && rewardTypeEntity.getName().equals(event.getRewardTypeName())) {
    				isTestModeEnabledForRewardType = rewardTypeEntity.isTestMode();	
    			}
        	}
    		
    		return isTestModeEnabledForRewardType;
    	} catch(Exception exc) {
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(), 
					LogStatus.ERROR, 
					Application.CLICK_ACTIVITY+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());
			return false;
    	}
    }

    public void triggerTestModeForUserOfferClick(RealmEntity realm, UserEventEntity event) {
    	try {
        	String providerTransactionId = DigestUtils.sha1Hex(event.getOfferId()+
            										event.getUserId()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										event.getPhoneNumber());
        	
        	HttpURLConnection urlConnection = null;
        	BufferedReader in = null;
        	try {
    			//String urlParameters = realm.getTestModeUrl()+"/ab/svc/v1/conversion/"+event.getInternalTransactionId()+"/"+providerTransactionId;
    			String urlParameters = realm.getTestModeUrl()+"/ab/svc/v1/conversion?internalTransactionId="+event.getInternalTransactionId()+"&offerProviderTransactionId="+providerTransactionId;
    			
    			URL testUrl = new URL(urlParameters);
    	        urlConnection = (HttpURLConnection)testUrl.openConnection();
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
    	} catch(Exception exc) {
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.CLICK_ACTIVITY, realm.getId(), 
					LogStatus.ERROR, 
					Application.CLICK_ACTIVITY+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());
    	}
    }

}
