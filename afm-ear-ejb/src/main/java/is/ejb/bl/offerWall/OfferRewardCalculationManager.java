package is.ejb.bl.offerWall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignment;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.denominationModels.DenominationModelTable;
import is.ejb.bl.denominationModels.SerDeCustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.SerDeDenominationModelTable;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAOCustomDenominationModel;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.CustomDenominationModelEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

public class OfferRewardCalculationManager {

	@Inject
	private Logger logger;

	//--------------- Offer filters -----------------------------
    @Inject
	private DAODenominationModel daoDenominationModel;

	@Inject
	private SerDeDenominationModelTable serDeDenominationModelTable; 

	private RealmEntity realm;
	
	//---------------------- custom dms --------------------------
	@Inject
	private DAOCustomDenominationModel daoCustomDenominationModel;
	@Inject
	private SerDeCustomDenominationModelAssignments serDeCustomDenominationModelAssignments;
	private CustomDenominationModelEntity customDenominationModelEntity = null;
	private CustomDenominationModelAssignments customDenominationModelAssignmentsDataHolder = null;
	private ArrayList<CustomDenominationModelAssignment> listCustomDenominationModelAssignments = null;

	@Inject
	private DAOCurrencyCode daoCurrencyCode;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;

	private CurrencyCodeEntity currencyCode;
	
	private OfferFilterEntity offerFilterConfiguration = null; 
	private CurrencyCodes currencyCodesConfiguration = null; 

	public void init(RealmEntity realm) throws Exception {
		this.realm = realm;
		
		try {
			//read data here (do not repeat for every offer)
			customDenominationModelEntity = daoCustomDenominationModel.findByRealmId(realm.getId());
			customDenominationModelAssignmentsDataHolder = serDeCustomDenominationModelAssignments.deserialize(customDenominationModelEntity.getContent());
			listCustomDenominationModelAssignments = customDenominationModelAssignmentsDataHolder.getListCustomDenominationModelAssignments();
			
			currencyCode = daoCurrencyCode.findByRealmId(realm.getId());
			//get currency codes config
			currencyCodesConfiguration = serDeCurrencyCode.deserialize(currencyCode.getSupportedCurrencies());
			if(currencyCodesConfiguration.getListCodes().size()==0) {
				throw new Exception("Aborting composite offer wall generation - no currency codes defined - please define supported currency codes before generating composite offer wall");
			} 
		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
		}
	}
	
	private DenominationModelEntity getCustomAssignedDenominationModel(Offer selectedOffer, OfferWallEntity offerWall, OfferProviderCodeNames offerProviderCodeName) {
		try {
			for(int i=0;i<listCustomDenominationModelAssignments.size();i++){
				CustomDenominationModelAssignment cDM = listCustomDenominationModelAssignments.get(i);
				if(cDM.getOfferSourceId().equals(selectedOffer.getSourceId()) &&
						cDM.getAdProviderCodeName().equals(selectedOffer.getAdProviderCodeName())) {
					logger.info("identified custom denomination model for offer: "+selectedOffer.getTitle()+" "+selectedOffer.getAdProviderCodeName()+" custom dm name: "+cDM.getDenominationModelName());
					return daoDenominationModel.findById(cDM.getDenominationModelId());
				}
			}
			return null;
		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			
        	String errorMessage = Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
					" "+offerProviderCodeName+
					" offer: "+selectedOffer.getTitle()+"  status: "+RespStatusEnum.FAILED+
					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL+
					" error: unable to retrieve custom denomiantion model for payout currency code: "+selectedOffer.getCurrency();
        	
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR,
					errorMessage);
			return null;
		}
	}
	
	public Offer calculateOfferReward(Offer selectedOffer, OfferWallEntity offerWall, OfferProviderCodeNames offerProviderCodeName) throws Exception {
		selectedOffer.setRewardType(offerWall.getRewardTypeName());	//set reward type name assigned to offer wall

		//first attempt to identify custom dm
		DenominationModelEntity denominationModel = getCustomAssignedDenominationModel(selectedOffer, 
				offerWall, 
				offerProviderCodeName);
		if(denominationModel == null) {
			//logger.info("custom denomination model not found");
			//it is assumed that only single global model can exist for a given reward type and this one will be picked based on reward type name
			String rewardTypeName = selectedOffer.getRewardType();
			logger.info("looking for denomination model assinged to reward type: "+rewardTypeName);
	        denominationModel = daoDenominationModel.findByRewardTypeNameAndRealmId(
	        			true,
	        			rewardTypeName, 
		        		offerWall.getRealm().getId());
	        
            if(denominationModel == null) { //if no luck - fail as no DM exists for this offer
            	String errorMessage = Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
    					" "+offerProviderCodeName+
    					" offer: "+selectedOffer.getTitle()+"  status: "+RespStatusEnum.FAILED+
    					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL+
    					" error: unable to identify matching denomination model for reward type: "+rewardTypeName+
    					" and payout currency code: "+selectedOffer.getCurrency();
            	
    			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
    					LogStatus.ERROR,
    					errorMessage);
    			
    			throw new Exception(errorMessage);
            }
		}
        
        //logger.info("identified denomination model entity: name: "+denominationModel.getName()+
        //		" cc: "+denominationModel.getSourcePayoutCurrencyCode()+
        //		" reward type: "+denominationModel.getRewardTypeName());

		//set currency code
		logger.info("identified denomination model assinged to reward type: "+selectedOffer.getRewardType()+" with currency code: "+denominationModel.getTargetPayoutCurrencyCode());
		selectedOffer.setRewardCurrency(denominationModel.getTargetPayoutCurrencyCode());

		//all calculated in target currency
		double payoutValueInTargetCurrency = round(selectedOffer.getPayout() * denominationModel.getMultiplier(),2);
        double revenueSplitValue = denominationModel.getCommisionPercentage();
        double rewardValue = round(selectedOffer.getPayout() * ((double)(100-denominationModel.getCommisionPercentage())/(double)100) * denominationModel.getMultiplier(),2);
        double profitValue = round(selectedOffer.getPayout() * (denominationModel.getCommisionPercentage()/(double)100) * denominationModel.getMultiplier(),2);
        selectedOffer.setRewardValue(rewardValue);
        selectedOffer.setPayoutInTargetCurrency(payoutValueInTargetCurrency);
        selectedOffer.setProfitValue(profitValue);
        selectedOffer.setRevenueSplitValue(revenueSplitValue);

        return selectedOffer;
	}

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

	public UserEventEntity calculateOfferReward(UserEventEntity event) throws Exception {
		//logger.info("custom denomination model not found");
		//it is assumed that only single global model can exist for a given reward type and this one will be picked based on reward type name
		logger.info("looking for denomination model assinged to reward type: "+event.getRewardTypeName());
		DenominationModelEntity denominationModel = daoDenominationModel.findByRewardTypeNameAndRealmId(
        			true,
	        		event.getRewardTypeName(), 
	        		event.getRealmId());
        
        if(denominationModel == null) { //if no model found matching source payout currency - identify one with target payout currency
            if(denominationModel == null) { //if no luck - fail as no DM exists for this offer
            	String errorMessage = Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
    					" offer: "+event.getOfferTitle()+"  status: "+RespStatusEnum.FAILED+
    					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL+
    					" error: unable to identify matching denomination model for reward type: "+event.getRewardTypeName();
            	
    			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, event.getRealmId(), 
    					LogStatus.ERROR,
    					errorMessage);
    			
    			throw new Exception(errorMessage);
            }
        }
        
     	//set currency code
		logger.info("identified denomination model assinged to reward type: "+event.getRewardTypeName()+" with currency code: "+denominationModel.getTargetPayoutCurrencyCode());
		event.setRewardIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());

		//all calculated in target currency
		double payoutValueInTargetCurrency = round(event.getOfferPayout() * denominationModel.getMultiplier(),2);
        double revenueSplitValue = denominationModel.getCommisionPercentage();
        double rewardValue = round(event.getOfferPayout() * ((double)(100-denominationModel.getCommisionPercentage())/(double)100) * denominationModel.getMultiplier(),2);
        double profitValue = round(event.getOfferPayout() * (denominationModel.getCommisionPercentage()/(double)100) * denominationModel.getMultiplier(),2);
        
        event.setOfferPayoutInTargetCurrency(payoutValueInTargetCurrency);
        event.setOfferPayoutInTargetCurrencyIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
        event.setRewardValue(rewardValue);
        event.setProfitValue(profitValue);
        
        return event;
	}

}
