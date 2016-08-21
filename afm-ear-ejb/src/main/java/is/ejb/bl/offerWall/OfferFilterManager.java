package is.ejb.bl.offerWall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.positioning.OfferPositioningEntry;
import is.ejb.bl.offerWall.positioning.OfferWallPositioningDataHolder;
import is.ejb.bl.offerWall.positioning.SerDeOfferPositioning;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.inject.Inject;

public class OfferFilterManager {

	@Inject
	private Logger logger;

	//--------------- Offer filters -----------------------------
	@Inject
	private DAOOfferFilter daoOfferFilter;
	@Inject
	private DAOCurrencyCode daoCurrencyCode;

	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;

	private RealmEntity realm;
	private CurrencyCodeEntity currencyCode;
	
	private OfferFilterEntity offerFilterConfiguration = null; 
	private CurrencyCodes currencyCodesConfiguration = null; 

	@Inject
	private OfferDuplicatesDetector offerDuplicatesDetector;// = new OfferDuplicatesDetector();

	@Inject
	private DAOBlockedOffers daoBlockedOffers;
	
	@Inject
	private SerDeBlockedOffers serDeBlockedOffers;
	
	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private SerDeOfferPositioning serDeOfferPositioning;

	public void init(RealmEntity realm) throws Exception {
		this.realm = realm;
		//retrieve offer filter entity
		offerFilterConfiguration = daoOfferFilter.findByRealmId(realm.getId());
		currencyCode = daoCurrencyCode.findByRealmId(realm.getId());
		//get currency codes config
		currencyCodesConfiguration = serDeCurrencyCode.deserialize(currencyCode.getSupportedCurrencies());
		if(currencyCodesConfiguration.getListCodes().size()==0) {
			throw new Exception("Aborting composite offer wall generation - no currency codes defined - please define supported currency codes before generating composite offer wall");
		} 
		logger.info("OfferFilterManager initialised for realm: "+realm.getId()+", number of supported currency codes: "+currencyCodesConfiguration.getListCodes().size());
	}

	//------------------------------- global filter ------------------------------------------------
	public boolean isOfferAcceptedBasedOnGlobalFilters(Offer selectedOffer, OfferWallEntity offerWall, OfferProviderCodeNames offerProviderCodeName) {
		if(offerFilterConfiguration.isRejectOfferWithMissingCurrency()) {
			if(selectedOffer.getCurrency() == null || selectedOffer.getCurrency().equals("null")) {
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NO_CURRENCY_DEFINED+" "+offerProviderCodeName+
						" rejected offer with no payout currency code, offer currency code: "+selectedOffer.getCurrency(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			}
		}

		if(offerFilterConfiguration.isRejectNotIncentivisedOffers()) {
			if(!selectedOffer.isIncentivised()) {
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NOT_INCENTIVISED+" "+offerProviderCodeName+
						" rejected offer as it is not incentivised: "+selectedOffer.isIncentivised(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			}
		}

		if(offerFilterConfiguration.isRejectOfferWithMissingImage()) {
			if(selectedOffer.getAdProviderCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())) {
				//we let nativex to be processed as images are added at the later stage
			} else if(selectedOffer.getImage()==null) {
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NO_IMAGE_DEFINED+" "+offerProviderCodeName+
						" rejected offer with no image defined: "+selectedOffer.getImage(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			} else {
				boolean isImageEmpty = false;
				int emptyImageCount = 0;
				HashMap<String,String> imagesMap = (HashMap)selectedOffer.getImage();
				Set<String> keys = imagesMap.keySet();
				Iterator it = keys.iterator();
				while(it.hasNext()) {
					String key = (String)it.next();
					String image = imagesMap.get(key);
					if(image == null || image.length() ==0){
						isImageEmpty = true;
						emptyImageCount++;
					}
				}
				if(emptyImageCount == imagesMap.size() && isImageEmpty) {
					String message = Application.SINGLE_OFFER_REJECTED+" "+
							Application.SINGLE_OFFER_NO_IMAGE_DEFINED+" "+offerProviderCodeName+
							" rejected offer with no image defined: "+selectedOffer.getImage(); 
					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
							offerWall.getRealm().getId(), 
							LogStatus.WARNING, 
							message);
					return false;
				}
			}
		}

		if(offerFilterConfiguration.isRejectOfferWithMissingPayout()) {
			if(selectedOffer.getPayout() <= 0) { //assume missing payout is when the value <=0
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NO_PAYOUT_DEFINED+" "+offerProviderCodeName+
						" rejected offer with no payout value defined: "+selectedOffer.getPayout(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			}
		}

		if(offerFilterConfiguration.isRejectOfferWithMissingUrl()) {
			if(selectedOffer.getUrl() == null || selectedOffer.getUrl().length()==0) {
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NO_URL_DEFINED+" "+offerProviderCodeName+
						" rejected offer with no url value defined: "+selectedOffer.getUrl(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			}
		}
		
		//check if offer has supported payout currency and its payout value is above configured threshold
		try {
			boolean isOfferSupportingConfiguredCurrencyCode = false;
			RewardTypeEntity rewardType = daoRewardType.findByRealmIdAndName(realm.getId(), offerWall.getRewardTypeName());

			ArrayList<CurrencyCode> listSupportedCurrencyCodes = currencyCodesConfiguration.getListCodes();
			for(int i=0;i<listSupportedCurrencyCodes.size();i++) {
				CurrencyCode supportedCurrencyCode = listSupportedCurrencyCodes.get(i);
				if(selectedOffer.getCurrency()!=null && 
						selectedOffer.getCurrency().toUpperCase().equals(supportedCurrencyCode.getCode().toUpperCase())) {
					
					isOfferSupportingConfiguredCurrencyCode = true;
					//if(selectedOffer.getPayout() < supportedCurrencyCode.getPayoutTreshold()) { //deprecated
					if(selectedOffer.getPayout() < rewardType.getMinimalOfferPayoutThresholdInSourceCurrency()) {
						String message = Application.SINGLE_OFFER_REJECTED+" "+
								Application.SINGLE_OFFER_PAYOUT_BELOW_TRESHOLD+" "+offerProviderCodeName+
								" offer with payout lower than configured threshold, offer payout: "+selectedOffer.getPayout()+
								" payout treshold: "+rewardType.getMinimalOfferPayoutThresholdInSourceCurrency(); 
								//" payout treshold: "+supportedCurrencyCode.getPayoutTreshold();
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
								offerWall.getRealm().getId(), 
								LogStatus.WARNING, 
								message);
						return false;
					}
				}
			}
			
			if(!isOfferSupportingConfiguredCurrencyCode) {
				String message = Application.SINGLE_OFFER_REJECTED+" "+
						Application.SINGLE_OFFER_NO_SUPPORTED_PAYOUT_CURRENCY_DEFINED+" "+offerProviderCodeName+
						" rejected offer with not supported payout currency code: "+selectedOffer.getCurrency(); 
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						message);
				return false;
			}

		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					" error: "+exc.toString());

			return false;
		}

		return true;
	}

	//------------------------------- geo filter (for array of geo codes) -----------------------------------------
	public boolean isOfferAcceptedBasedOnGeoFilter(Offer selectedOffer, OfferWallEntity offerWall, 
			OfferProviderCodeNames offerProviderCodeName,
			List<String> listSourceOfferSupportedCountryCodes) {

		int numberOfRequiredCountryCodes = 0;
		ArrayList<String> listApprovedSupportedCountryCodes = new ArrayList<String>();
		String requiredCountryCodes = offerWall.getTargetCountriesFilter();
		
		
		if(requiredCountryCodes == null || requiredCountryCodes.length()==0) {
//			String message = Application.SINGLE_OFFER_GEO_FILTERING+" "+
//			Application.SINGLE_OFFER_GEO_FILTERING_ACCEPTED_NO_FILTER+" "+offerProviderCodeName+
//			" accepted offer as no geo filter constraints were defined by AdBroker - accepted by AB geo countries: "+offerWall.getTargetCountriesFilter()+
//			" offer name: "+selectedOffer.getTitle(); 
//			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
//					offerWall.getRealm().getId(), 
//					LogStatus.OK, 
//					message);
			return true;
		}

		if(listSourceOfferSupportedCountryCodes == null || listSourceOfferSupportedCountryCodes.size()==0) {
			if(offerProviderCodeName.equals(OfferProviderCodeNames.AARKI.toString())) {
				listApprovedSupportedCountryCodes.add("*ALL*");
				selectedOffer.setSupportedCountryCodes(listApprovedSupportedCountryCodes);
				return true; //aarki null means all accepted
			} else {
				String message = Application.SINGLE_OFFER_GEO_FILTERING+" "+
						Application.SINGLE_OFFER_GEO_FILTERING_ERROR_NO_COUNTRY_CODES_SUPPLIED_BY_OFFER_PROVIDER+" "+offerProviderCodeName+
						" rejected offer as offer provider did not supply any information about geo filtering, offer name: "+selectedOffer.getTitle(); 
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
								offerWall.getRealm().getId(), 
								LogStatus.WARNING, 
								message);
						return false;
			}
		}

		//logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		StringTokenizer st = new StringTokenizer(requiredCountryCodes, ",");
		numberOfRequiredCountryCodes = st.countTokens();
		
		while(st.hasMoreElements()) {
			String requiredCountryCode = ((String)st.nextElement()).trim();
			
			//logger.info("++ required contry code: "+requiredCountryCode+" supported by offer codes: "+listSourceOfferSupportedCountryCodes.toString());
			for(int i=0;i<listSourceOfferSupportedCountryCodes.size();i++) {
				String code = listSourceOfferSupportedCountryCodes.get(i).trim();
				//logger.info("comparing country code: "+code.toUpperCase()+" "+requiredCountryCode.toUpperCase());

				if(code.toUpperCase().equals(requiredCountryCode.toUpperCase())) {
					listApprovedSupportedCountryCodes.add(code);
					//logger.info("!!!!!!!! found matching code: "+code+" required country codes: "+numberOfRequiredCountryCodes);
				}
			}
		}

		selectedOffer.setSupportedCountryCodes(listApprovedSupportedCountryCodes);
		if(listApprovedSupportedCountryCodes.size() == numberOfRequiredCountryCodes){
			return true;
		} else {
			return false;
		}
	}
	
	//------------------------------- target device filter (for array of accepted devices) -------------------------------------
	public boolean isOfferAcceptedBasedOnTargetPlatformFilter(Offer selectedOffer, OfferWallEntity offerWall, 
			OfferProviderCodeNames offerProviderCodeName,
			List<String> listSourceOfferSuppportedDevices) {

		int numberOfRequiredDevices = 0;
		ArrayList<String> listApprovedSupportedDevices = new ArrayList<String>();
		String requiredTargetDevices = offerWall.getTargetDevicesFilter();
		if(requiredTargetDevices.equals("iOS")) { //add other categories to this filter
			//requiredTargetDevices = requiredTargetDevices+", iphone, ipad, ipod";
			requiredTargetDevices = requiredTargetDevices+", iphone";
			
		}
		
		if(requiredTargetDevices == null || requiredTargetDevices.length()==0) {
//			String message = Application.SINGLE_OFFER_TARGET_DEVICE_FILTERING+" "+
//			Application.SINGLE_OFFER_TARGET_DEVICE_FILTERING_ACCEPTED_NO_FILTER_DEFINED+" "+offerProviderCodeName+
//			" accepted offer as no target device filter was defined by AdBroker - accepted by AB target devices: "+offerWall.getTargetDevicesFilter()+
//			" offer name: "+selectedOffer.getTitle(); 
//			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
//					offerWall.getRealm().getId(), 
//					LogStatus.OK, 
//					message);
			return true;
		}

		if(listSourceOfferSuppportedDevices == null || listSourceOfferSuppportedDevices.size()==0) {
			if(offerProviderCodeName.toString().equals(OfferProviderCodeNames.AARKI.toString())) {
				listApprovedSupportedDevices.add("*ALL*");
				selectedOffer.setSupportedTargetDevices(listApprovedSupportedDevices);
				return true; //aarki null means all accepted
			} else {
				String message = Application.SINGLE_OFFER_TARGET_DEVICE_FILTERING+" "+
						Application.SINGLE_OFFER_TARGET_DEVICE_FILTERING_ERROR_NO_TARGET_DEVICES_SUPPLIED_BY_OFFER_PROVIDER+" "+offerProviderCodeName+
						" rejected offer as offer provider did not supply any information about supported devices: "+listSourceOfferSuppportedDevices+", offer name: "+selectedOffer.getTitle(); 
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
								offerWall.getRealm().getId(), 
								LogStatus.WARNING, 
								message);
						return false;
			}
		}

		//logger.info("==================================================");
		StringTokenizer st = new StringTokenizer(requiredTargetDevices, ",");
		numberOfRequiredDevices = st.countTokens();
		
		while(st.hasMoreElements()) {
			String requiredDevice = ((String)st.nextElement()).trim();
			
			logger.info("++ required device: "+requiredDevice+" available devices: "+listSourceOfferSuppportedDevices.toString()+" op: "+offerProviderCodeName+" "+selectedOffer.getTitle()+" sourceId: "+selectedOffer.getSourceId());
			for(int i=0;i<listSourceOfferSuppportedDevices.size();i++) {
				String code = listSourceOfferSuppportedDevices.get(i).trim();

				if(code.toUpperCase().contains(requiredDevice.toUpperCase())) {
					listApprovedSupportedDevices.add(code);
					logger.info("!!!!!!!! found matching device: "+code+" required devices number: "+numberOfRequiredDevices+" already found: "+listApprovedSupportedDevices.size()+" "+selectedOffer.getTitle()+" sourceId: "+selectedOffer.getSourceId());
				}
			}
		}

		//override supported target platforms on the display to those that match our criteria
		selectedOffer.setSupportedTargetDevices(listApprovedSupportedDevices);
		/*
		if(listApprovedSupportedDevices.size() == numberOfRequiredDevices){
			return true;
		} else {
			return false;
		}
		*/
		
		//accept if we found required device (we only request single specific device type in offer wall filter!)
		if(listApprovedSupportedDevices.size()>0){
			return true;
		} else {
			return false;
		}
	}

	//-------------------------------------------- offer wall sorting -------------------------------------------
	public void sortOffers(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		for(int j=0;j<listIndividualOfferWalls.size();j++) {
			IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
			//sort inside each offer wall
			ArrayList<Offer> listOfferWallOffers = individualOfferWall.getOffers();
			for(int z = 0; z<listOfferWallOffers.size();z++) {
				for(int k=0;k<listOfferWallOffers.size()-1;k++) {
					Offer offer1 = listOfferWallOffers.get(k);
					Offer offer2 = listOfferWallOffers.get(k+1);
					if(offer1.getRewardValue() < offer2.getRewardValue()) {
						listOfferWallOffers.set(k, offer2);
						listOfferWallOffers.set(k+1, offer1);
					}
				}
			}
		}
	}

	//-------------------------------------------- remove empty offer walls -------------------------------------------
	public void removeEmptyOfferWalls(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		//remove offer walls that have no offers inside
		for(int j=listIndividualOfferWalls.size()-1;j>=0;j--) {
			IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
			if(individualOfferWall.getOffers().size() == 0) {
				listIndividualOfferWalls.remove(j);
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						Application.EMPTY_OFFER_WALL_REMOVED+" removed offer wall name: "+individualOfferWall.getOfferWallName()+
						" offer provider: "+individualOfferWall.getAdProviderCodeName());
			} 
		}
	}

	//-------------------------------------------- offer wall index appending -------------------------------------------
	public void appendOffersIndexes(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		int nonEmptyOfferWallCounter = 1;
		
		for(int j=0;j<listIndividualOfferWalls.size();j++) {
			IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
		
			individualOfferWall.setOfferWallName(individualOfferWall.getOfferWallName().trim()+" "+nonEmptyOfferWallCounter);
			nonEmptyOfferWallCounter++;
		}
	}

	//-------------------------------------------- sing offer wall listing -------------------------------------------
	public ArrayList<IndividualOfferWall> generateSingleOfferWallListing(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		IndividualOfferWall mainOfferWall = null;
		if(listIndividualOfferWalls.size()==0) {
			return listIndividualOfferWalls;
		}
		
		//assume first wall is the main offer wall to which we add all other offers from other walls
		mainOfferWall = listIndividualOfferWalls.get(0);
		
		for(int j=listIndividualOfferWalls.size()-1;j>=1;j--) {
			IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
			ArrayList<Offer> listOfferWallOffers = individualOfferWall.getOffers();
			for(int z = 0; z<listOfferWallOffers.size();z++) {
				Offer offer = listOfferWallOffers.get(z);
				mainOfferWall.getOffers().add(offer);
			}
			listIndividualOfferWalls.remove(j);
		}
		
		return listIndividualOfferWalls;
	}
	
	//-------------------------------------------- offer wall positioning -------------------------------------------
	public ArrayList<IndividualOfferWall> appendOffersPositioning(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		if(offerWall.getPositioning() == null || offerWall.getPositioning().length() == 0 || listIndividualOfferWalls.size() > 1){
			return listIndividualOfferWalls;
		}

		if(listIndividualOfferWalls.size() == 0) {
			return listIndividualOfferWalls;
		}

		try {
			logger.info("===================== positioning wall : "+offerWall.getName()+" ==============================");
			//deserialise
			OfferWallPositioningDataHolder offerWallPositioningDataHolder = serDeOfferPositioning.deserialize(offerWall.getPositioning());
			logger.info("got offers positionined: "+offerWallPositioningDataHolder.getListOfferPositioningEntries().size());
			ArrayList<OfferPositioningEntry> listOfferPositioningEntries = offerWallPositioningDataHolder.getListOfferPositioningEntries();

			IndividualOfferWall mainOfferWall = listIndividualOfferWalls.get(0);
			ArrayList<Offer> listOfferWallOffers = mainOfferWall.getOffers();
			
			//go from the end of the positoning list and start adding found offers to the top
			//as we keep adding more - the first added to the top will be the last ones
			//if unidentified in the list offer gets on the wall - push it automatically to the end of the list (enforce new offers to be placed at the  bottom)
			boolean isOfferIdentified = false;

			for(int i=listOfferPositioningEntries.size()-1;i>=0; i--) {
				isOfferIdentified = false;
				OfferPositioningEntry ope = listOfferPositioningEntries.get(i);
				logger.info("==> ofer positioining entry at index: "+i+" "+ope.getOfferTitle());
				for(int z = 0; z < listOfferWallOffers.size();z++) {
					Offer o = listOfferWallOffers.get(z);

					if(o.getTitle().equals(ope.getOfferTitle())) {
						//store details in offer about its type (promo|normal) and if it was indicated as positioned
						o.setType(ope.getOfferType());
						logger.info("=> setting as positioned: "+o.isPositioned()+" ope: "+ope.isOfferPosioned());
						o.setPositioned(ope.isOfferPosioned());
						
						logger.info("=> found offer - placing offer: "+o.getTitle()+" to top of the list");
						isOfferIdentified = true;
						//remove offer from wall as we move it to the top of the list
						listOfferWallOffers.remove(z);
						//put it at the end of the list
						listOfferWallOffers.add(0, o);
						break;
					}
				}
			}				

			//scan across the list to identify offers that were not on the positioning list - these would be new offers that recently appeared 
			//push them to the bottom of the list
//			if(!isOfferIdentified){ //for offer that is new and hence not on the positoining template - put it at the end of the list
//				logger.info("placing new offer to end of the list");
//				//remove offer from wall as we move it to the top of the list
//				listOfferWallOffers.remove(z);
//				//put it at the end of the list
//				listOfferWallOffers.add(o);
//			}

			//override original offers order
			mainOfferWall.setOffers(listOfferWallOffers);
			listIndividualOfferWalls.set(0, mainOfferWall);

			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.OK, 
					Application.OFFERS_POSITIONING+
					" successfully positioned offers for wall: "+offerWall.getName());
		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					Application.OFFERS_POSITIONING+
					" unable to position offers for wall: "+offerWall.getName()+" error: "+exc.toString());
		}
		
		return listIndividualOfferWalls;
	}

	//-------------------------------------------- filtering out blocked offers -------------------------------------------
	public void filterOutBlockedOffers(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		BlockedOffersEntity blockedOffersEntity = null;
		BlockedOffers blockedOffers = null;
		ArrayList<BlockedOffer> listBlockedOffers = null;

		try {
			blockedOffersEntity = daoBlockedOffers.findByRealmId(offerWall.getRealm().getId());
			blockedOffers = serDeBlockedOffers.deserialize(blockedOffersEntity.getContent());
			listBlockedOffers = blockedOffers.getListBlockedOffers();
			 
			for(int j=0;j<listIndividualOfferWalls.size();j++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
				//sort inside each offer wall
				ArrayList<Offer> listOfferWallOffers = individualOfferWall.getOffers();
				for(int z = listOfferWallOffers.size()-1; z>=0; z--) {
					Offer offerToVerify = listOfferWallOffers.get(z);
					//check if its on blocked list
					boolean isOfferBlockedManually = isOfferBlocked(offerToVerify, listBlockedOffers);
					//check if its on blocked keywords list
					boolean isOfferBlockedByKeyword = isOfferBlockedByKeyword(offerToVerify, offerWall);
					if(isOfferBlockedManually || isOfferBlockedByKeyword) {
						listOfferWallOffers.remove(z);
						if(isOfferBlockedManually) {
							Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
									offerWall.getRealm().getId(), 
									LogStatus.WARNING, 
									Application.SINGLE_OFFER_BLOCKED_OFFER_REJECTED+
									" rejected manually blocked offer: "+offerToVerify.getTitle()+" provider: "+offerToVerify.getAdProviderCodeName());
						} else {
							Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
									offerWall.getRealm().getId(), 
									LogStatus.WARNING, 
									Application.SINGLE_OFFER_BLOCKED_OFFER_REJECTED+
									" rejected keyword blocked offer: "+offerToVerify.getTitle()+" provider: "+offerToVerify.getAdProviderCodeName());
						}
					} 
				}
			}

		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					Application.SINGLE_OFFER_BLOCKED_OFFER_REJECTED+
					" Error rejecting blocked offers: "+exc.toString());
		}
	}

	//-------------------------------------------- filtering out stale offers -------------------------------------------
	public void filterOutStaleOffers(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
		offerWall.getRealm().getId(), 
		LogStatus.OK, 
		Application.STALE_OFFER_WALL_FILTERING+
		" filtering offer wall for stale offers: "+offerWall.getName()+
		" wall lifetime: "+offerWall.getRealm().getWallLifetime()+" minutes");
		
		try {
			int wallLifetime = offerWall.getRealm().getWallLifetime(); //in minutes
			 
			for(int j=listIndividualOfferWalls.size()-1;j>=0;j--) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
				
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = format.parse(individualOfferWall.getGenerationTime());
				Timestamp tsCurrentTime = new Timestamp(System.currentTimeMillis());
				Timestamp tsIndividualOfferWallGenerationDate = new Timestamp(date.getTime());
				long staleInSecs = (tsCurrentTime.getTime()-tsIndividualOfferWallGenerationDate.getTime())/1000;
				long staleInMins = (tsCurrentTime.getTime()-tsIndividualOfferWallGenerationDate.getTime())/(1000*60);
				
				//System.out.println("!!!!!!!!!!!!!!!! dupa: "+individualOfferWall.getGenerationTime()+" "+tsIndividualOfferWallGenerationDate.toString()+" mins: "+staleInMins+" secs: "+staleInSecs);
				if(staleInMins > wallLifetime) {
					listIndividualOfferWalls.remove(j);
					System.out.println("rejecting stale offer...");
					
					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.WARNING,
					Application.STALE_OFFER_WALL_FILTERING+" "+
					Application.STALE_OFFER_WALL_REJECTED+
					" rejected stale offer: "+individualOfferWall.getOfferWallName()+
					" provider: "+individualOfferWall.getAdProviderCodeName()+
					" outdated by: "+staleInMins+" minutes");
				}
			}

		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), 
					LogStatus.ERROR,
					Application.STALE_OFFER_WALL_FILTERING+" "+
					" Error rejecting stale offers: "+exc.toString());
		}
	}

	private boolean isOfferBlocked(Offer offer, ArrayList<BlockedOffer> listBlockedOffers) {
		for(int i=0;i<listBlockedOffers.size();i++) {
			BlockedOffer bo = listBlockedOffers.get(i);
			//logger.info("-");
			//logger.info("********** inspecting if offer is to block: "+bo.isActive());
			//logger.info("********** inspecting if offer is to block: "+bo.getTitle()+"-"+offer.getTitle());
			//logger.info("********** inspecting if offer is to block: "+bo.getSourceId()+"-"+offer.getSourceId());
			//logger.info("********** inspecting if offer is to block: "+bo.getRewardType()+"-"+offer.getRewardType());
			//logger.info("********** inspecting if offer is to block: "+bo.getAdProviderCodeName()+"-"+offer.getAdProviderCodeName());
			
			//bo.getSourceId() != null && bo.getSourceId().length()>0 && //this is no longer needed
			//bo.getSourceId().toLowerCase().equals(offer.getSourceId().toLowerCase()) && 

			if(bo.isActive() &&
					bo.getSourceId() != null && bo.getSourceId().length()>0 && //this is no longer needed
					bo.getRewardType()!=null &&
					bo.getTitle().toLowerCase().equals(offer.getTitle().toLowerCase()) &&
					bo.getRewardType().toLowerCase().equals(offer.getRewardType().toLowerCase()) && //to block offers that are specific to given reward type
					bo.getAdProviderCodeName().toLowerCase().equals(offer.getAdProviderCodeName().toLowerCase()) ){
				logger.info("!!! ********** removing blocked offer from feed: "+bo.getTitle()+" "+offer.getTitle()+" rewardType: "+offer.getRewardType());
				return true;
			}
		}
		
		return false;
	}

	private boolean isOfferBlockedByKeyword(Offer offer, OfferWallEntity offerWall) {
		String blockKeywords = offerWall.getBlockedKeywords();
		//logger.info("********** inspecting if offer is to be block by following keywords: "+blockKeywords);
		if(blockKeywords != null && blockKeywords.length() > 0) {
			StringTokenizer st = new StringTokenizer(blockKeywords);
			while(st.hasMoreElements()) {
				String key = (String)st.nextElement();
				key = key.toLowerCase();
				//logger.info(" >>> inspecting if offer is to be block by following keyword: "+key);
				if((offer.getTitle() !=null && offer.getTitle().toLowerCase().contains(key)) 
						|| (offer.getDescription() != null && offer.getDescription().toLowerCase().contains(key))) {
					logger.info(" >>> *** blocking by following keyword: "+key+" offer title: "+offer.getTitle());
					return true;
				}
			}
		}
		
		return false;
	}
	
	public OfferDuplicatesDetector getOfferDuplicatesDetector() {
		return offerDuplicatesDetector;
	}

	public void setOfferDuplicatesDetector(
			OfferDuplicatesDetector offerDuplicatesDetector) {
		this.offerDuplicatesDetector = offerDuplicatesDetector;
	}

	public OfferFilterEntity getOfferFilterConfiguration() {
		return offerFilterConfiguration;
	}

	public void setOfferFilterConfiguration(
			OfferFilterEntity offerFilterConfiguration) {
		this.offerFilterConfiguration = offerFilterConfiguration;
	}
	
}
