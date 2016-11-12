package is.ejb.bl.offerProviders.adgate;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.adgate.getAdGateOffers.GetAdGateOffers;
import is.ejb.bl.offerProviders.adgate.getAdGateOffers.OffersEntry;
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

public class AdGateAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										AdGateProviderConfig providerConfig,
										int numberOfOffersToSelect) throws Exception {
		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);

		//offer wall stats
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        String requestUrl = "";
        
        String targetDevice = offerWall.getTargetDevicesFilter().toLowerCase();
        String requiredDevice = "";
        String requiredCategory = "";
        if(targetDevice.equals("android")) {
        	requiredDevice = "android";
        	requiredCategory = "1";
        } else if(targetDevice.equals("ios")) {
        	requiredDevice = "iphone";
        	requiredCategory = "11";
        }
        
        
    	requestUrl = "https://api.adgatemedia.com/v1/offers?aff="+providerConfig.getAffiliateNumber()+
        		"&api_key="+providerConfig.getApiKey()+
        		"&ua="+requiredDevice+ //android or iphone
        		"&categories="+requiredCategory+ //1=android or 11=iphone
        		"&country="+offerWall.getTargetCountriesFilter().toLowerCase();

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.TRIALPAY+" requesting url: "+requestUrl);

		logger.info("request url: "+requestUrl);
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
    	
    	//logger.info("request response: "+reqResponse);
//    	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
//		LogStatus.OK, 
//		" "+OfferProviderCodeNames.TRIALPAY+
//		" req status: "+urlConnection.getResponseCode()+
//		" rest response: "+reqResponse);
    	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
		LogStatus.OK, 
		" "+OfferProviderCodeNames.ADGATE+
		" req url: "+requestUrl+
		" req status: "+urlConnection.getResponseCode()+
		" rest response length: "+reqResponse.length());
    	
    	System.out.println(" req url: "+requestUrl+
    			" req status: "+urlConnection.getResponseCode()+
    			" rest response length: "+reqResponse);
    	
    	reqResponse = "{\"offers\": "+reqResponse+"}";
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>();
    	try {
        	ArrayList<OffersEntry> listPulledOffers = new ArrayList<OffersEntry>();
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetAdGateOffers offersDH = mapper.readValue(reqResponse, GetAdGateOffers.class);
    		List<OffersEntry> arrayOfOffers = offersDH.getOffers();
    		for(int i=0;i<arrayOfOffers.size();i++){
    			OffersEntry agOffer = arrayOfOffers.get(i);
    			if(agOffer != null && agOffer.getId() != null &&
    					agOffer.getCategory() != null && 
    					agOffer.getName() != null && agOffer.getName().length()>0 &&
    					agOffer.getTracking_url() != null) {
        			listPulledOffers.add(agOffer);
        			
        			logger.info("found ag offer id: "+agOffer.getId());
        			logger.info(" name: "+agOffer.getName());
        			logger.info(" country: "+agOffer.getCountry());
        			logger.info(" category: "+agOffer.getCategory());
        			logger.info(" requirements: "+agOffer.getRequirements());
        			logger.info(" tracking url: "+agOffer.getTracking_url());
        			logger.info(" type: "+agOffer.getType());
        			logger.info(" link: "+agOffer.getUa());
        			logger.info(" payout: "+agOffer.getPayout());
        			logger.info(" anchor: "+agOffer.getAnchor());
        			
//                	logger.info("found trialpay offer id: "+tpOffer.getId()+
//                			" title: "+tpOffer.getTitle()+
//                			" reward name: "+tpOffer.getReward_name()+
//                			" reward amount: "+tpOffer.getVc_amount()+
//                			" image url: "+tpOffer.getImage_url()+
//                			" impression url: "+tpOffer.getImpression_url()+
//                			//" instructions: "+tpOffer.getInstructions()+
//                			" link: "+tpOffer.getLink()+
//                			" categories: "+tpOffer.getCategory().toString());
    			}
    		}

            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.TRIALPAY+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);

            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());
            
    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.TRIALPAY+" total pulled offers: "+listPulledOffers.size());
        	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
        			LogStatus.OK, 
        			" "+OfferProviderCodeNames.TRIALPAY+
        			" total pulled offers: "+listPulledOffers.size());
    		
    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.TRIALPAY+" no offers left to pick from Hasoffers - possibly most were rejected");
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
            										selectedOffer.getTracking_url()+
            										numberOfOffersToSelect+"");
            	
            	offerToAdd.setIncentivised(true);
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setSourceId(selectedOffer.getId()+"");
            	offerToAdd.setTitle(selectedOffer.getName());
            	offerToAdd.setDescription(selectedOffer.getRequirements());
            	if(selectedOffer.getRequirements().length()>240) {
                	offerToAdd.setCallToAction(selectedOffer.getRequirements().substring(0,230));
            	} else {
            		offerToAdd.setCallToAction(selectedOffer.getRequirements());
            	}
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.ADGATE.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getPreview_url());
            	offerToAdd.setUrl(selectedOffer.getTracking_url());
            	offerToAdd.setCurrency("USD");
            	offerToAdd.setPayout(round(selectedOffer.getPayout(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setTrackingRequirements(new ArrayList<String>());
    			//get images
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
    			imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getIcon());
            	offerToAdd.setImage(imagesMap);

            	//filters - set to true as we don't filter this offer (its automatically filtered by the provider based on request data)
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = true;
    			boolean isOfferAcceptedByDeviceFilter = true;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, 
    					offerWall, OfferProviderCodeNames.ADGATE);
    			/*
    			if(isOfferAcceptedByGlobalFilter) {
    				//apply geo filtering
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    						offerWall, OfferProviderCodeNames.ADGATE, selectedOffer.getCountries());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, offerWall, OfferProviderCodeNames.ADGATE, selectedOffer.getDevice_targeting());
    				}
    			}
    			*/

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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.ADGATE);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.ADGATE);
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
    						OfferProviderCodeNames.ADGATE+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
    		exc.printStackTrace();
            throw new Exception(OfferProviderCodeNames.ADGATE+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider");//+reqResponse);
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
