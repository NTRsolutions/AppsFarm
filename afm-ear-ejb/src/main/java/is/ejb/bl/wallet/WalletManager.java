package is.ejb.bl.wallet;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.currencyCodes.CurrencyCodeConverter;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

@Stateless
public class WalletManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOConversionHistory daoConversionHistory;
	
	@Inject
	private RewardManager rewardManager;

	@Inject
	private MailManager mailManager;

	@Inject
	DAOWalletData daoWalletData;

	@Inject
	private DAOWalletTransaction daoWalletTransaction;

	@Inject
	private NotificationManager notificationManager;

	public void processWalletPayout(RealmEntity realm, 
			AppUserEntity user,
			String applicationName, //e.g, AirRewardz
			String rewardTypeName, //e.g, AirRewardz-India
			String rewardName, //e.g, airtime  
			double walletPayoutInTargetCurrency,
			String ipAddress, String friendPhoneNumber) {

		//TODO rewardType should have associated target currency value in AB UI - thanks to this 
		//getRewardCurrencyCodeByGeo would be obsolete
		//in rewardType UI the target currency should be selectable from SupportedCurrencies db data
		
		try {
			String walletPayoutTargetCurrencyCode = CurrencyCodeConverter.getRewardCurrencyCodeByGeo(user.getCountryCode());

			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.WALLET_TRANSACTION_ACTIVITY+" "+
					Application.WALLET_PAY_OUT+" "+				
					" email: "+user.getEmail()+
					" country code: "+user.getCountryCode()+
					" applicationName: "+applicationName+
					" rewardName: "+rewardName+
					" rewardTypeName: "+rewardTypeName+
					" walletPayoutInTargetCurrency: "+walletPayoutInTargetCurrency+
					" walletPayoutTargetCurrencyCode: "+walletPayoutTargetCurrencyCode);

	    	String internalTransactionId = DigestUtils.sha1Hex(user.getId()+
	    										Math.random()*100000+
	    										System.currentTimeMillis()+
	    										user.getPhoneNumber()+
	    										user.getPhoneNumberExtension()+
	    										user.getEmail());

	    	String offerId = DigestUtils.sha1Hex(user.getId()+
					Math.random()*100000+
					System.currentTimeMillis()+
					user.getPhoneNumber()+
					user.getPhoneNumberExtension()+
					user.getEmail());
 
	    	logger.info(" email: "+user.getEmail()+
					" country code: "+user.getCountryCode()+
					" applicationName: "+applicationName+
					" rewardName: "+rewardName+
					" rewardTypeName: "+rewardTypeName+
					" walletPayoutInTargetCurrency: "+walletPayoutInTargetCurrency+
					" walletPayoutTargetCurrencyCode: "+walletPayoutTargetCurrencyCode);
	    	
			UserEventEntity invitingUserEvent = new UserEventEntity();
			//generate event object and pesrsist it in db
			UserEventEntity event = new UserEventEntity();
			event.setUserId(user.getId());
			event.setOfferId(offerId);
			event.setDeviceType(user.getDeviceType());
			event.setInternalTransactionId(internalTransactionId);
			event.setPhoneNumber(user.getPhoneNumber());
			event.setPhoneNumberExt(user.getPhoneNumberExtension());
			event.setApplicationName(applicationName);
			event.setRewardName(rewardName);
			event.setRewardTypeName(rewardTypeName); //e.g, AirRewardz-India 
			event.setRealmId(realm.getId());
			event.setOfferTitle(UserEventCategory.WALLET_PAY_OUT.toString()+"-"+rewardName);
			event.setIosDeviceToken(user.getiOSDeviceToken());
			event.setAndroidDeviceToken(user.getAndroidDeviceToken());
			event.setOfferPayout(0.0); //needed for denomination model to calculate reward
			event.setOfferPayoutInTargetCurrency(0);
			event.setRewardIsoCurrencyCode(walletPayoutTargetCurrencyCode);
			event.setRewardValue(walletPayoutInTargetCurrency);
			/*if (applicationName.toLowerCase().contains("goahead") || applicationName.toLowerCase().contains("cine")){
			    event.setRewardIsoCurrencyCode("");
			    event.setRewardValue(0);
			    event.setCustomRewardCurrencyCode(walletPayoutTargetCurrencyCode);
			    event.setCustomRewardValue(walletPayoutInTargetCurrency);
			}
			else{
				
			}*/
			event.setProfilSplitFraction(0);
			event.setProfitValue(0);
			event.setRevenueValue(0);
			event.setClickDate(new Timestamp(System.currentTimeMillis()));
			event.setConversionDate(new Timestamp(System.currentTimeMillis()));
			event.setCountryCode(user.getCountryCode());
			event.setUserEventCategory(UserEventCategory.WALLET_PAY_OUT.toString());
			event.setEmail(user.getEmail());
			event.setInstant(true); //instant as we request reward instantly when paying out from the wallet
			event.setIpAddress(ipAddress);
			event.setFriendPhoneNumber(friendPhoneNumber);
			//System.out.println(event);
			
			daoUserEvent.create(event); //persist event in db

			
			double [] values = rewardManager.calculateRewardPayoutBasedOnCarrier(rewardTypeName,event.getRewardValue());
			event.setRewardValue(values[0]);
	        event.setCustomRewardValue(values[0]);
	        if (applicationName.toLowerCase().contains("goahead")){
	        	event.setCustomRewardCurrencyCode("Trippa Points");
	        	event.setRewardIsoCurrencyCode("Trippa Points");
	        }
	        if (applicationName.toLowerCase().contains("cine")){
	        	event.setCustomRewardCurrencyCode("stubs");
	        	event.setCustomRewardCurrencyCode("Trippa Points");
	        }
			
	        logger.info(event.toString());
	        
	        rewardManager.createUserConversionHistory(event); //update conversion history (needed to filter out already clicked offers for particular user)
			
			rewardManager.issueReward(realm, event, null, false); //issue reward 
		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.WALLET_TRANSACTION_ACTIVITY+" "+
					Application.WALLET_TRANSACTION_ACTIVITY_ABORTED+" "+
					RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+
					" error: "+exc.toString());
		}
	}

	//triggered when receiving response from zendesk
	public void processWalletRewardForGoAhead(RealmEntity realmEntity,
			AppUserEntity user,
			WalletDataEntity wallet,
			WalletTransactionEntity walletTransactionEntity,
			String dataContent) throws Exception { //triggered when zendesk response is received

		// everything is ok, now we can substract balance, send email
		wallet.setBalance(wallet.getBalance()-walletTransactionEntity.getPayoutValue());
		daoWalletData.createOrUpdate(wallet);
		walletTransactionEntity.setStatus("SUCCESS");

		daoWalletTransaction.createOrUpdate(walletTransactionEntity);
		walletTransactionEntity.getInternalTransactionId();

		//update user event
		UserEventEntity event = daoUserEvent.findByInternalTransactionId(walletTransactionEntity.getInternalTransactionId());
		event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
		//update reward date
		event.setRewardResponseStatusMessage(RespCodesEnum.OK.toString());
		event.setRewardDate(new Timestamp(System.currentTimeMillis()));
		event.setApproved(false);
		daoUserEvent.createOrUpdate(event,8);

		//create wallet payout es log - only when it is in success state as go ahead does not deduct funds if its in pending state
		Application.getElasticSearchLogger().indexWalletTransaction(realmEntity.getId(), event.getPhoneNumber(), 
				"", event.getDeviceType(),  
				event.getOfferId(), 
				event.getOfferSourceId(), 
				event.getOfferTitle(), 
				event.getAdProviderCodeName(), 
				event.getRewardTypeName(), 
				event.getOfferPayoutInTargetCurrency(), 
				event.getRewardValue(), 
				event.getRewardIsoCurrencyCode(),
				event.getProfitValue(),
				realmEntity.getName(),
				"",
				UserEventType.conversion.toString(),
				event.getInternalTransactionId(),
				"",
				UserEventCategory.WALLET_PAY_OUT.toString(),
				"",
				"",
				event.getIpAddress(),
				event.getCountryCode(),
				event.isInstant(),
				event.getApplicationName(),
				event.isTestMode());
		
		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmEntity.getId(), 
				LogStatus.OK,
				Application.REWARD_ACTIVITY+" "+Application.REWARD_RESPONSE_ACTIVITY+
				" successfully rewarded event "+
				" internalT: "+event.getInternalTransactionId()+
				" status: "+RespStatusEnum.SUCCESS+" code: "+RespCodesEnum.OK_NO_CONTENT);
		
		String userMessage = "You have been succesfully rewarded with: "
				+ walletTransactionEntity.getRewardName() + " for " + walletTransactionEntity.getPayoutValue()
				+ ". ";

		String adminMessage = "User " + user.getFullName() + " - "
				+ user.getEmail() + " " + user.getPhoneNumber()
				+ " has been sucessfully rewarded with:"
				+ walletTransactionEntity.getRewardName() + " for " + walletTransactionEntity.getPayoutValue()
				+ " credits.";

		List <String> adminRecipents = new ArrayList<String>();
		adminRecipents.add("sam.armour@bluepodmedia.com");
		adminRecipents.add("mariusz.jacyno@bluepodmedia.com");
		adminRecipents.add("jakub.homlala@bluepodmedia.com");
		
		if (event.getApplicationName().toLowerCase().contains("cine"))
		{
			adminRecipents.add("support@cinetreats.co.uk");
			
		}
		
		for (String admin: adminRecipents){
			mailManager.sendEmail(realmEntity,admin,"Admin message",adminMessage);
		}
		mailManager.sendEmail(realmEntity, user.getEmail(), "User message", userMessage);

		Application.getElasticSearchLogger().indexLog(
				Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE,
				-1,
				LogStatus.OK,
				Application.WALLET_USE_PAYOUT_OFFER_ZENDESK_RESPONSE
						+ " succsessfully rewarded: " + dataContent);

		//update conversion history
		rewardManager.updateUserConversionHistory(event);
		
		//issue notification
		notificationManager.sendRewardNotification(event, true, false);
	}
 
}
