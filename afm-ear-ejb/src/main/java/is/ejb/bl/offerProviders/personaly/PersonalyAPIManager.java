package is.ejb.bl.offerProviders.personaly;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.personaly.getOffers.BannersEntry;
import is.ejb.bl.offerProviders.personaly.getOffers.CountriesEntry;
import is.ejb.bl.offerProviders.personaly.getOffers.GetOffers;
import is.ejb.bl.offerProviders.personaly.getOffers.OffersEntry;
import is.ejb.bl.offerProviders.personaly.getOffers.PlatformsEntry;
import is.ejb.bl.offerProviders.personaly.getOffers.Store_app_idsEntry;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.OfferWallEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.CharSet;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class PersonalyAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										PersonalyProviderConfig adProviderConfig,
										int numberOfOffersToSelect) throws Exception {

		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);
        
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        String devicePlatform = "android";
        if(offerWall.getTargetDevicesFilter().toLowerCase().equals("ios")) {
        	devicePlatform = "iphone";        	
        } else if(offerWall.getTargetDevicesFilter().toLowerCase().equals("android")) {
        	devicePlatform = "android";
        }
        	
        
        String requestUrl = "http://api.persona.ly/serverside/v1/offer/listOffers/?app_hash="+adProviderConfig.getAppHash()+
        						"&country="+offerWall.getTargetCountriesFilter()+"&platform="+devicePlatform+
        						"&records_per_page="+adProviderConfig.getRecordsPerPage()+"&page=1&order=date_add_asc";
        
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.PERSONALY+" requesting url: "+requestUrl);
		logger.info("requesting url: "+requestUrl);
    	try {
    		URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(offerWall.getRealm().getConnectionTimeout() * 1000);
            urlConnection.setReadTimeout(offerWall.getRealm().getReadTimeout() * 1000);
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
    	
//    	logger.info(reqResponse);
    	ArrayList<OffersEntry> listPulledOffers = new ArrayList<OffersEntry>();
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>(); 

     	try {
        	logger.info("response: "+reqResponse);
        	
    		ObjectMapper mapper = new ObjectMapper();
    		GetOffers offersHolder = mapper.readValue(reqResponse, GetOffers.class);

    		List<OffersEntry> listOffers = offersHolder.getOffers();
    		for(int i=0;i<listOffers.size();i++) {
    			logger.info("============================= "+listOffers.size());
    			OffersEntry offer = listOffers.get(i);
    			logger.info(offer.getName());
    			logger.info(offer.getUrl());
    			logger.info(offer.getPayment()+"");
    			logger.info(offer.getCountries().toString());
    			List<CountriesEntry> listCountries = offer.getCountries();
    			for(int j=0;j<listCountries.size();j++) {
    				logger.info("--c > "+listCountries.get(j).getName());
    			}
    			logger.info(offer.getPlatforms().toString());
    			List<PlatformsEntry> listPlats = offer.getPlatforms();
    			for(int j=0;j<listPlats.size();j++) {
    				logger.info("--p > "+listPlats.get(j).getName());
    			}

    			logger.info(offer.getDescription());
    			logger.info(offer.getGuidelines());
    			logger.info(offer.getMobile_guidelines());
    			logger.info(offer.getBanners().toString());
    			logger.info(offer.getFeatured()+"");
    			logger.info(offer.getHas_multiple_lead()+"");
    			logger.info(offer.getId()+"");
    			logger.info(offer.getIs_daily()+"");
    			if(offer.getStore_app_ids()!=null) {
    				List<Store_app_idsEntry> listAppIds = offer.getStore_app_ids();
    				for(int x =0 ;x< listAppIds.size();x++) {
    					logger.info("--> app id: "+listAppIds.get(x).getApp_id()+" "+listAppIds.get(x).getStore_id());	
    				}
    			}
    			
    			logger.info(offer.getTags().toString());
    			listPulledOffers.add(offer);
    		}
	
    		//------------------------------------------------------------------------------------------
    		//------------------------------ fill list of offers ---------------------------------------
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.PERSONALY+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());
            
    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.PERSONALY+" total pulled offers: "+listPulledOffers.size());
    		
    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.PERSONALY+
    						" no offers left to pick- possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listPulledOffers.size());
    			OffersEntry selectedOffer = listPulledOffers.get(randomNumber);
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			
    			offerToAdd.setSourceId(selectedOffer.getId()+"");
            	//generate unique offer id used by our system to track offer conversion
            	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
            										offerWall.getNumberOfOffers()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										randomNumber+
            										selectedOffer.getName()+
            										selectedOffer.getUrl()+
            										numberOfOffersToSelect+"");
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setTitle(selectedOffer.getName());
            	offerToAdd.setDescription(selectedOffer.getDescription()+" "+selectedOffer.getGuidelines());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.PERSONALY.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getUrl());
            	offerToAdd.setUrl(selectedOffer.getUrl());
            	offerToAdd.setCurrency("USD");
            	offerToAdd.setPayout(round(selectedOffer.getPayment(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setCallToAction(selectedOffer.getGuidelines());
            	offerToAdd.setIncentivised(true); //TODO make sure all offers are incent
            	ArrayList<String> listTargetDevices = new ArrayList<String>();
            	listTargetDevices.add(offerWall.getTargetDevicesFilter());
            	offerToAdd.setSupportedTargetDevices(listTargetDevices);
    			//get images
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
    			List<BannersEntry> listBanners = selectedOffer.getBanners();
				for(int z = 0;z<listBanners.size();z++) {
					BannersEntry image = listBanners.get(z);
					//logger.info("-> image: "+z+" "+image.getUrl()+" h: "+image.getHeight()+" w: "+image.getWidth());
					imagesMap.put(ThumbnailQuality.Image+"-"+(z+1), image.getUrl());
				}
            	offerToAdd.setImage(imagesMap);
            	
            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.PERSONALY);
    			if(isOfferAcceptedByGlobalFilter) {
    				//apply geo filtering
    				ArrayList<String> targetedCountries = new ArrayList<String>(); //fake as we filter it during request - so put country filter provided on offer wall 
    				targetedCountries.add(offerWall.getTargetCountriesFilter());
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.PERSONALY, 
    							targetedCountries);
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.PERSONALY, 
    							listTargetDevices);
    				}
    			}

    			//------------------------- check for duplicates starts ----------------
    			//check for duplicate offers and select one with higher payout
    			boolean isOfferAcceptedByDuplicatesFilter = true;
    			boolean isOfferDuplicate = false;
    			boolean isOfferDuplicateWithHigherPayout = false;
    			
    			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()) { //if filter for rejecting duplicate offers is enabled - trigger it
        			if(isOfferAcceptedByGlobalFilter && 
        					isOfferAcceptedByDeviceFilter && 
        							isOfferAcceptedByGeoFilter) { //process only if offer is accepted by previous filters!
        				//calculate reward value/profit/split and add to offer data
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.PERSONALY);
        				isOfferDuplicate = offerFilterManager.getOfferDuplicatesDetector().isOfferDuplicate(offerToAdd, offerWall);
        				if(isOfferDuplicate) {
            				isOfferDuplicateWithHigherPayout = offerFilterManager.getOfferDuplicatesDetector().isOfferDuplicateAndWithHigherPayout(offerToAdd, offerWall);
            				if(isOfferDuplicateWithHigherPayout) { //we add this offer and mark previously added duplicate for rejection as the current offer has higher payout 
            					offerFilterManager.getOfferDuplicatesDetector().markExistingOfferForRejection(offerToAdd, offerWall);
            					offerFilterManager.getOfferDuplicatesDetector().rememberAddedOffer(offerToAdd, offerWall);
            				} else { //offer we want to add is duplicate and has lower payout than already added offer - reject the current offer
        						String message = Application.SINGLE_OFFER_DUPLICATES_FILTERING+" "+
        								Application.SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_REJECTED+" "+
        								" rejecting following offer as it is a duplicate: "+
        								" offer title: "+offerToAdd.getTitle()+" payout in target currency: "+offerToAdd.getPayoutInTargetCurrency()+" "+offerToAdd.getAdProviderCodeName(); 
        						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
        								offerWall.getRealm().getId(), 
        								LogStatus.WARNING, 
        								message);

            					isOfferAcceptedByDuplicatesFilter = false;
            				}
        				} else {
        					offerFilterManager.getOfferDuplicatesDetector().rememberAddedOffer(offerToAdd, offerWall);
        				}
        			}
    			} else if(isOfferAcceptedByGlobalFilter && 
    						isOfferAcceptedByDeviceFilter && 
    							isOfferAcceptedByGeoFilter){ //if not checking duplicates
    				//calculate reward value/profit/split and add to offer data
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.PERSONALY);
    			}
    			//-------------------------- check for duplicates ends ----------------
    			
    			//logger.info("=====> Accepted offer: "+selectedOffer.getName()+" global filter: "+isOfferAcceptedByGlobalFilter+" geo filter: "+isOfferAcceptedByGeoFilter+" device filter: "+isOfferAcceptedByDeviceFilter);
    			if(isOfferAcceptedByGlobalFilter && 
    					isOfferAcceptedByDeviceFilter && 
    						isOfferAcceptedByGeoFilter &&
    							isOfferAcceptedByDuplicatesFilter) {//add offer to the wall

    				listSelectedIndividualOffers.add(offerToAdd); //add to list
    				listPulledOffers.remove(randomNumber); //remove successfully added offer from the pool that we select from potential offers
    				
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.OK, 
    						Application.SINGLE_OFFER_CREATED+" "+
    						OfferProviderCodeNames.PERSONALY+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.PERSONALY+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);
    	}

        //update offer stats
        offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());

		return listSelectedIndividualOffers;
	}
	
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }
}
