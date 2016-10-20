package is.ejb.bl.offerProviders.trialpay;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.trialpay.getOffers.TrialPayOffer;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.RealtimeFeedDataHolder;
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

public class TrialPayAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										RealtimeFeedDataHolder realtimeFeedDH,
										TrialPayProviderConfig providerConfig) throws Exception {
		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+providerConfig.getNumberOfPulledOffers());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+providerConfig.getNumberOfPulledOffers());
        
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        String requestUrl = "";
        if(realtimeFeedDH.getIdfa()!=null && realtimeFeedDH.getIdfa().length()>0) {
        	requestUrl = "https://geo.tp-cdn.com/api/offer/v1/?vic="+providerConfig.getVic()+
            		"&sid="+realtimeFeedDH.getUserId()+
            		"&idfa_en=1"+
            		"&ua="+realtimeFeedDH.getUa()+
            		"&ip="+realtimeFeedDH.getIp()+
            		"&idfa="+realtimeFeedDH.getIdfa()+
            		"&num_offers="+providerConfig.getNumberOfPulledOffers();	
        } else {
        	requestUrl = "https://geo.tp-cdn.com/api/offer/v1/?vic="+providerConfig.getVic()+
            		"&sid="+realtimeFeedDH.getUserId()+
            		"&gaid_en=1"+
            		"&ua="+realtimeFeedDH.getUa()+
            		"&ip="+realtimeFeedDH.getIp()+
            		"&gaid="+realtimeFeedDH.getGaid()+
            		"&num_offers="+providerConfig.getNumberOfPulledOffers();	
        }
        	

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
		" "+OfferProviderCodeNames.TRIALPAY+
		" req url: "+requestUrl+
		" req status: "+urlConnection.getResponseCode()+
		" rest response length: "+reqResponse.length());

    	int numberOfOffersToSelect = providerConfig.getNumberOfPulledOffers();
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>();
    	try {
        	ArrayList<TrialPayOffer> listPulledOffers = new ArrayList<TrialPayOffer>();
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		TrialPayOffer[] arrayOfOffers = mapper.readValue(reqResponse, TrialPayOffer[].class);
    		for(int i=0;i<arrayOfOffers.length;i++){
    			TrialPayOffer tpOffer = arrayOfOffers[i];
    			if(tpOffer != null && tpOffer.getId() != null && tpOffer.getId().length()>0 &&
    					tpOffer.getCategory() != null && 
    					tpOffer.getReward_name() != null && tpOffer.getReward_name().length()>0 &&
    					tpOffer.getImage_url() != null && tpOffer.getImage_url().length()>0 &&
    					tpOffer.getImpression_url() != null && tpOffer.getImpression_url().length()>0 &&
    					tpOffer.getLink() != null && tpOffer.getLink().length()>0 
    					) {
        			listPulledOffers.add(tpOffer);
        			
//        			logger.info("found trialpay offer id: "+tpOffer.getId());
//        			logger.info(" title: "+tpOffer.getTitle());
//        			logger.info(" reward name: "+tpOffer.getReward_name());
//        			logger.info(" reward amount: "+tpOffer.getVc_amount());
//        			logger.info(" image url: "+tpOffer.getImage_url());
//        			logger.info(" impression url: "+tpOffer.getImpression_url());
//        			logger.info(" instructions: "+tpOffer.getInstructions());
//        			logger.info(" link: "+tpOffer.getLink());
//        			logger.info(" categories: "+tpOffer.getCategory().toString());
        			
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
    			TrialPayOffer selectedOffer = listPulledOffers.get(randomNumber);
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			
    			offerToAdd.setSourceId(selectedOffer.getId());
            	//generate unique offer id used by our system to track offer conversion
            	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
            										offerWall.getNumberOfOffers()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										randomNumber+
            										selectedOffer.getTitle()+
            										selectedOffer.getLink()+
            										numberOfOffersToSelect+"");
            	
            	offerToAdd.setIncentivised(true);
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setSourceId(selectedOffer.getId());
            	offerToAdd.setTitle(selectedOffer.getTitle());
            	offerToAdd.setDescription(selectedOffer.getDescription());
            	if(selectedOffer.getInstructions().length()>240) {
                	offerToAdd.setCallToAction(selectedOffer.getInstructions().substring(0,230));
            	} else {
            		offerToAdd.setCallToAction(selectedOffer.getInstructions());
            	}
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.TRIALPAY.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getImpression_url());
            	offerToAdd.setUrl(selectedOffer.getLink());
            	offerToAdd.setCurrency(selectedOffer.getReward_name());
            	offerToAdd.setPayout(round(selectedOffer.getVc_amount()/(double)100,2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setTrackingRequirements(new ArrayList<String>());
    			//get images
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
    			imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getImage_url());
            	offerToAdd.setImage(imagesMap);

            	//filters - set to true as we don't filter this offer (its automatically filtered by the provider based on request data)
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = true;
    			boolean isOfferAcceptedByDeviceFilter = true;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, 
    					offerWall, OfferProviderCodeNames.TRIALPAY);
    			/*
    			if(isOfferAcceptedByGlobalFilter) {
    				//apply geo filtering
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    						offerWall, OfferProviderCodeNames.TRIALPAY, selectedOffer.getCountries());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, offerWall, OfferProviderCodeNames.TRIALPAY, selectedOffer.getDevice_targeting());
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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.TRIALPAY);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.TRIALPAY);
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
    						OfferProviderCodeNames.TRIALPAY+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
    		exc.printStackTrace();
            throw new Exception(OfferProviderCodeNames.TRIALPAY+
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
