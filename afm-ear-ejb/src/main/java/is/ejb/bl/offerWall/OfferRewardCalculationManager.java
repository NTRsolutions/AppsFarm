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
			if(selectedOffer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())) {
				rewardTypeName = "AirRewardz-India(vc)";
				logger.info("detected custom VCommission offer - applying custom rewardType denomination model: "+rewardTypeName);
			}
	        denominationModel = daoDenominationModel.findByRewardTypeNameAndRealmId(
	        			true,
	        			rewardTypeName, 
		        		offerWall.getRealm().getId());
	        
	        if(denominationModel == null) { //if no model found matching source payout currency - identify one with target payout currency
	        	/*
	        	 * deprecated as we assume that only single denomination model exists for given rewardType name
	            denominationModel = daoDenominationModel.findByTargetPayoutCurrencyCodeAndRewardTypeNameAndRealmId(
	            		true,
	            		selectedOffer.getRewardCurrency(), 
	            		rewardType, 
	            		offerWall.getRealm().getId());
	            */
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
		}
        
        //logger.info("identified denomination model entity: name: "+denominationModel.getName()+
        //		" cc: "+denominationModel.getSourcePayoutCurrencyCode()+
        //		" reward type: "+denominationModel.getRewardTypeName());

		//set currency code
		logger.info("identified denomination model assinged to reward type: "+selectedOffer.getRewardType()+" with currency code: "+denominationModel.getTargetPayoutCurrencyCode());
		selectedOffer.setRewardCurrency(denominationModel.getTargetPayoutCurrencyCode());

		DenominationModelTable modelTable = serDeDenominationModelTable.deserialize(denominationModel.getContent());
		ArrayList<DenominationModelRow> listDenominationModelRows = modelTable.getRows();
		//here split based on whether we process based on source payout currency or destination payout currency 
		DenominationModelRow rewardDenominationModelRow = null;

		//event.setOfferPayoutIsoCurrencyCode("KSHa"); //for testing
		if(selectedOffer.getCurrency().toUpperCase().equals(denominationModel.getSourcePayoutCurrencyCode().toUpperCase())) {
			//get denomination model based on source cc
			rewardDenominationModelRow = lookUpMatchingDenominationModel(selectedOffer, offerWall, offerProviderCodeName, listDenominationModelRows, true);
			/*
			//for testing lookup
			double payout = -1.0;
			for(int i=0;i<650;i++) {
				event.setOfferPayout(payout);
				rewardDenominationModelRow = lookUpMatchingDenominationModel(event, listDenominationModelRows, true);
				System.out.print("["+event.getOfferPayout()+"->"+rewardDenominationModelRow.getAirtimePayoff()+"] "); 
				payout = payout + 0.01;
				payout = round(payout, 2);
			}
			*/
		} else if(selectedOffer.getCurrency().toUpperCase().equals(denominationModel.getTargetPayoutCurrencyCode().toUpperCase())) {
			//get denomination model based on target cc
			rewardDenominationModelRow = lookUpMatchingDenominationModel(selectedOffer, offerWall, offerProviderCodeName, listDenominationModelRows, false);
			/*
			//for testing lookup
			double payout = -1.0;
			for(int i=0;i<600;i++) {
				event.setOfferPayout(payout);
				rewardDenominationModelRow = lookUpMatchingDenominationModel(event, listDenominationModelRows, false);
				System.out.print("["+event.getOfferPayout()+"->"+rewardDenominationModelRow.getAirtimePayoff()+"] "); 
				payout = payout + 1;
				payout = round(payout, 2);
			}
			*/
		} else { //error as currency code does not match any of defined types!
			String errorString = "Error 2: unable to identify matching denomination model for reward type: "+selectedOffer.getRewardType()+
					" and payout value: "+selectedOffer.getPayout()+" payout currency code: "+selectedOffer.getCurrency();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
					" "+offerProviderCodeName+
					" offer: "+selectedOffer.getTitle()+"  status: "+RespStatusEnum.FAILED+
					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL+
					" error 2: unable to identify matching denomination model for reward type: "+selectedOffer.getRewardType()+
					" and payout currency code: "+selectedOffer.getCurrency());
			throw new Exception(errorString);
		}


		/*
		DenominationModelRow lowestValueDenominationModelRow = modelTable.getRows().get(0);
		logger.info("res: "+lowestValueDenominationModelRow.getSourceOfferPayoffValue()+" payout: "+selectedOffer.getPayout());
		if(selectedOffer.getPayout() < lowestValueDenominationModelRow.getSourceOfferPayoffValue()) {
			selectedOffer.setRewardPaymentInstant(false);
			selectedOffer.setRewardPaymentTopUp(true);
		} else {
			selectedOffer.setRewardPaymentInstant(true);
			selectedOffer.setRewardPaymentTopUp(true);
		}
		*/

		//all calculated in target currency
		double payoutValueInTargetCurrency = rewardDenominationModelRow.getTargetOfferPayoffValue();
        double revenueSplitValue = rewardDenominationModelRow.getRevenueSpit();
        double rewardValue = rewardDenominationModelRow.getAirtimePayoff();
        double profitValue = round(payoutValueInTargetCurrency - rewardValue, 2);
        selectedOffer.setRewardValue(rewardValue);
        selectedOffer.setPayoutInTargetCurrency(payoutValueInTargetCurrency);
        selectedOffer.setProfitValue(profitValue);
        selectedOffer.setRevenueSplitValue(revenueSplitValue);
        
		/*** setup instant or topup reward recharge flags based on set thresholds ***/
        RewardTypeEntity rewardType = daoRewardType.findByRealmIdAndName(realm.getId(), selectedOffer.getRewardType());
        if(rewardType != null) {
        	//handle VC offers manually as we don't know its payout in USD (minimal instant thresholds are set based on USD values)
			if(selectedOffer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())) {
				selectedOffer.setRewardPaymentInstant(false);
				selectedOffer.setRewardPaymentTopUp(true);
			} else { //handle based on minimal instant reward threshold configuration
				if(selectedOffer.getPayout() < rewardType.getMinimalInstantRewardThresholdInTargetCurrency()) {
					selectedOffer.setRewardPaymentInstant(false);
					selectedOffer.setRewardPaymentTopUp(true);
				} else {
					selectedOffer.setRewardPaymentInstant(true);
					selectedOffer.setRewardPaymentTopUp(true);
				}
			}
        } else { //signal there was error setting up what types of rewards (instant/wallet) can be executed on this offer
			selectedOffer.setRewardPaymentInstant(true);
			selectedOffer.setRewardPaymentTopUp(false);

			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
					" "+offerProviderCodeName+
					" offer: "+selectedOffer.getTitle()+"  status: "+RespStatusEnum.FAILED+
					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_REWARD_TYPE+
					" error 2: unable to identify matching rewardType to identify types of rewards (instant/wallet recharge) that will be set to this offer, found reward type: "+selectedOffer.getRewardType()+
					" and payout currency code: "+selectedOffer.getCurrency());
        }

//		ArrayList<CurrencyCode> listSupportedCurrencyCodes = currencyCodesConfiguration.getListCodes();
//		for(int i=0;i<listSupportedCurrencyCodes.size();i++) {
//			CurrencyCode supportedCurrencyCode = listSupportedCurrencyCodes.get(i);
//			if(selectedOffer.getCurrency()!=null && 
//					selectedOffer.getCurrency().toUpperCase().equals(supportedCurrencyCode.getCode().toUpperCase())) {
//				
//				if(selectedOffer.getPayout() < supportedCurrencyCode.getInstantRewardTreshold()) {
//					selectedOffer.setRewardPaymentInstant(false);
//					selectedOffer.setRewardPaymentTopUp(true);
//				} else {
//					selectedOffer.setRewardPaymentInstant(true);
//					selectedOffer.setRewardPaymentTopUp(true);
//				}
//			}
//		}

        return selectedOffer;
	}

	public DenominationModelRow lookUpMatchingDenominationModel(
			Offer selectedOffer, OfferWallEntity offerWall, OfferProviderCodeNames offerProviderCodeName, ArrayList<DenominationModelRow> listRows, boolean isSourcePayoutCurrency) {
		DenominationModelRow rewardDenominationModelRow = null;			

		if(isSourcePayoutCurrency) { //calculate based on source payout currency
			for(int i=0;i<listRows.size()-1;i++) {
				DenominationModelRow r0 = listRows.get(i);
				DenominationModelRow r1 = listRows.get(i+1);
				//System.out.println(event.getOfferPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
				if(selectedOffer.getPayout() >= r0.getSourceOfferPayoffValue() && selectedOffer.getPayout() < r1.getSourceOfferPayoffValue()) {
					//System.out.println("! "+selectedOffer.getPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
					rewardDenominationModelRow = r0;
					break;
				}
				//System.out.println("target value: "+r.getTargetOfferPayoffValue()+" delta: "+delta+" minDelta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
			}
		
			if(rewardDenominationModelRow == null) {
				DenominationModelRow firstRow = listRows.get(0);
				//identify min value not existing in table
				if(selectedOffer.getPayout() < firstRow.getSourceOfferPayoffValue()) {
					rewardDenominationModelRow = firstRow;
				}
				//identify max value not existing in table (or at its end)
				DenominationModelRow lastRow = listRows.get(listRows.size()-1);
				if(selectedOffer.getPayout() >= lastRow.getSourceOfferPayoffValue()) {
					rewardDenominationModelRow = lastRow;
				}
			}
		} else { //calculate based on target payout currency
			for(int i=0;i<listRows.size()-1;i++) {
				DenominationModelRow r0 = listRows.get(i);
				DenominationModelRow r1 = listRows.get(i+1);
				//System.out.println(event.getOfferPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
				if(selectedOffer.getPayout() >= r0.getTargetOfferPayoffValue() && selectedOffer.getPayout() < r1.getTargetOfferPayoffValue()) {
					//System.out.println("! "+selectedOffer.getPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
					rewardDenominationModelRow = r0;
					break;
				}
				//System.out.println("target value: "+r.getTargetOfferPayoffValue()+" delta: "+delta+" minDelta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
			}
		
			if(rewardDenominationModelRow == null) {
				DenominationModelRow firstRow = listRows.get(0);
				//identify min value not existing in table
				if(selectedOffer.getPayout() < firstRow.getTargetOfferPayoffValue()) {
					rewardDenominationModelRow = firstRow;
				}
				//identify max value not existing in table (or at its end)
				DenominationModelRow lastRow = listRows.get(listRows.size()-1);
				if(selectedOffer.getPayout() >= lastRow.getTargetOfferPayoffValue()) {
					rewardDenominationModelRow = lastRow;
				}
			}
		}

		double airtimeRewardValue = rewardDenominationModelRow.getAirtimePayoff();
		//System.out.println("=> value: "+" delta: "+minDelta+" minDeltaIndex: "+minDeltaIndex+" airtime payout: "+listRows.get(minDeltaIndex).airtimePayoff);
		//System.out.println("["+event.getOfferPayout()+"=>"+airtimeRewardValue+"]"); //value: "+" delta: "+minDelta+" minDeltaIndex: "+minDeltaIndex+" airtime payout: "+listRows.get(minDeltaIndex).airtimePayoff);
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
				" "+offerProviderCodeName+
				" offer: "+selectedOffer.getTitle()+"  status: "+RespStatusEnum.SUCCESS+
				" code: "+RespCodesEnum.OK+
				" "+offerProviderCodeName+
				" identified reward value: "+airtimeRewardValue+" for offer payout: "+
				selectedOffer.getPayout()+" offer currency code: "+selectedOffer.getCurrency()+
				" reward currency: "+selectedOffer.getRewardCurrency());
		
		return rewardDenominationModelRow;
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
        
        //logger.info("identified denomination model entity: name: "+denominationModel.getName()+
        //		" cc: "+denominationModel.getSourcePayoutCurrencyCode()+
        //		" reward type: "+denominationModel.getRewardTypeName());

		//set currency code
		logger.info("identified denomination model assinged to reward type: "+event.getRewardTypeName()+" with currency code: "+denominationModel.getTargetPayoutCurrencyCode());
		event.setRewardIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());

		DenominationModelTable modelTable = serDeDenominationModelTable.deserialize(denominationModel.getContent());
		ArrayList<DenominationModelRow> listDenominationModelRows = modelTable.getRows();
		//here split based on whether we process based on source payout currency or destination payout currency 
		DenominationModelRow rewardDenominationModelRow = null;

		if(event.getOfferPayoutIsoCurrencyCode().toUpperCase().equals(denominationModel.getSourcePayoutCurrencyCode().toUpperCase())) {
			//get denomination model based on source cc
			rewardDenominationModelRow = lookUpMatchingDenominationModel(event.getOfferPayout(), listDenominationModelRows, true);
		} else if(event.getOfferPayoutIsoCurrencyCode().toUpperCase().equals(denominationModel.getTargetPayoutCurrencyCode().toUpperCase())) {
			//get denomination model based on target cc
			rewardDenominationModelRow = lookUpMatchingDenominationModel(event.getOfferPayout(), listDenominationModelRows, false);
		} else { //error as currency code does not match any of defined types!
			String errorString = "Error 2: unable to identify matching denomination model for reward type: "+event.getRewardTypeName()+
					" and payout value: "+event.getOfferPayout()+" payout currency code: "+event.getOfferPayoutIsoCurrencyCode();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, event.getRealmId(), 
					LogStatus.ERROR, 
					Application.OFFER_WALL_GENERATION_ACTIVITY+" "+Application.REWARD_CREDIT_CALCULATION+
					" offer: "+event.getOfferTitle()+"  status: "+RespStatusEnum.FAILED+
					" code: "+RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL+
					" error 2: unable to identify matching denomination model for reward type: "+event.getRewardTypeName()+
					" and payout currency code: "+event.getOfferPayoutIsoCurrencyCode());
			throw new Exception(errorString);
		}

		//all calculated in target currency
		double payoutValueInTargetCurrency = rewardDenominationModelRow.getTargetOfferPayoffValue();
        double revenueSplitValue = rewardDenominationModelRow.getRevenueSpit();
        double rewardValue = rewardDenominationModelRow.getAirtimePayoff();
        double profitValue = round(payoutValueInTargetCurrency - rewardValue, 2);
        
        event.setOfferPayoutInTargetCurrency(payoutValueInTargetCurrency);
        event.setOfferPayoutInTargetCurrencyIsoCurrencyCode(denominationModel.getTargetPayoutCurrencyCode());
        event.setRewardValue(rewardValue);
        event.setProfitValue(profitValue);
        
        return event;
	}

	public DenominationModelRow lookUpMatchingDenominationModel(
			double payout, ArrayList<DenominationModelRow> listRows, boolean isSourcePayoutCurrency) {
		DenominationModelRow rewardDenominationModelRow = null;			

		if(isSourcePayoutCurrency) { //calculate based on source payout currency
			for(int i=0;i<listRows.size()-1;i++) {
				DenominationModelRow r0 = listRows.get(i);
				DenominationModelRow r1 = listRows.get(i+1);
				//System.out.println(event.getOfferPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
				if(payout >= r0.getSourceOfferPayoffValue() && payout < r1.getSourceOfferPayoffValue()) {
					//System.out.println("! "+selectedOffer.getPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
					rewardDenominationModelRow = r0;
					break;
				}
				//System.out.println("target value: "+r.getTargetOfferPayoffValue()+" delta: "+delta+" minDelta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
			}
		
			if(rewardDenominationModelRow == null) {
				DenominationModelRow firstRow = listRows.get(0);
				//identify min value not existing in table
				if(payout < firstRow.getSourceOfferPayoffValue()) {
					rewardDenominationModelRow = firstRow;
				}
				//identify max value not existing in table (or at its end)
				DenominationModelRow lastRow = listRows.get(listRows.size()-1);
				if(payout >= lastRow.getSourceOfferPayoffValue()) {
					rewardDenominationModelRow = lastRow;
				}
			}
		} else { //calculate based on target payout currency
			for(int i=0;i<listRows.size()-1;i++) {
				DenominationModelRow r0 = listRows.get(i);
				DenominationModelRow r1 = listRows.get(i+1);
				//System.out.println(event.getOfferPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
				if(payout >= r0.getTargetOfferPayoffValue() && payout < r1.getTargetOfferPayoffValue()) {
					//System.out.println("! "+selectedOffer.getPayout()+" "+r0.getTargetOfferPayoffValue()+" "+r1.getTargetOfferPayoffValue());
					rewardDenominationModelRow = r0;
					break;
				}
				//System.out.println("target value: "+r.getTargetOfferPayoffValue()+" delta: "+delta+" minDelta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
			}
		
			if(rewardDenominationModelRow == null) {
				DenominationModelRow firstRow = listRows.get(0);
				//identify min value not existing in table
				if(payout < firstRow.getTargetOfferPayoffValue()) {
					rewardDenominationModelRow = firstRow;
				}
				//identify max value not existing in table (or at its end)
				DenominationModelRow lastRow = listRows.get(listRows.size()-1);
				if(payout >= lastRow.getTargetOfferPayoffValue()) {
					rewardDenominationModelRow = lastRow;
				}
			}
		}

		double airtimeRewardValue = rewardDenominationModelRow.getAirtimePayoff();
		
		return rewardDenominationModelRow;
	}

}
