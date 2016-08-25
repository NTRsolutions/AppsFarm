package is.ejb.bl.offerProviders.aarki;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.aarki.getOffers.AarkiOffer;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.GetOffers;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.OffersEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroupOfferIds.FindAllOfferGroupOfferIds;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.DataEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.FindAllOfferGroups;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferById.Data;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferById.FindOfferById;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.Thumbnail;
import is.ejb.bl.offerProviders.minimob.getOfferById.CreativesEntry;
import is.ejb.bl.offerProviders.minimob.getOfferById.MinimobOffer;
import is.ejb.bl.offerProviders.minimob.getOffersIds.MinimobOfferId;
import is.ejb.bl.offerProviders.minimob.getOffersIds.MinimobOfferIdsList;
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

public class AarkiAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										String placementId,
										int numberOfOffersToPull, //how many offers we get from offer provider -1 = all available
										int numberOfOffersToSelect) throws Exception {
		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);
        
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        String requestUrl = ""; 
        if(numberOfOffersToPull >0) {
        	//requestUrl = "http://ar.aarki.net/adpick/campaign_list.json?src="+placementId+"&limit="+numberOfOffersToPull;
        	requestUrl = "http://a.archyads.net/adpick/campaign_list.json?src="+placementId+"&limit="+numberOfOffersToPull;	
        } else {
        	//requestUrl = "http://ar.aarki.net/adpick/campaign_list.json?src="+placementId;
        	requestUrl = "http://a.archyads.net/adpick/campaign_list.json?src="+placementId;
        }

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.AARKI+" requesting url: "+requestUrl);

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

//    	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
//		LogStatus.OK, 
//		" "+OfferProviderCodeNames.AARKI+ 
//		" rest response: "+reqResponse);
    	
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>();
    	try {
        	ArrayList<AarkiOffer> listPulledOffers = new ArrayList<AarkiOffer>();
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		AarkiOffer[] arrayOfOffers = mapper.readValue(reqResponse, AarkiOffer[].class);
    		//MinimobOfferIdsList obj = mapper.readValue(reqResponse, MinimobOfferIdsList.class);
    		for(int i=0;i<arrayOfOffers.length;i++){
    			AarkiOffer aarkiOffer = arrayOfOffers[i];
    			listPulledOffers.add(aarkiOffer);
            	//logger.info("found offer id: "+idObject.getId()+" "+idObject.getIncentivized()+" "+idObject.getPayoutCurrency()+" "+idObject.getPayout()+" "+idObject.getTargetedCountries());
    		}

    		//------------------------------------------------------------------------------------------
    		//------------------------------ fill list of offers ---------------------------------------
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.AARKI+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());
            
    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.AARKI+" total pulled offers: "+listPulledOffers.size());
    		
    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.AARKI+" no offers left to pick from Hasoffers - possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listPulledOffers.size());
    			AarkiOffer selectedOffer = listPulledOffers.get(randomNumber);
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			
    			offerToAdd.setSourceId(selectedOffer.getId());
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
            	offerToAdd.setDescription(selectedOffer.getAd_copy());
            	offerToAdd.setCallToAction(selectedOffer.getIncentive_action());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.AARKI.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getUrl());
            	offerToAdd.setUrl(selectedOffer.getUrl());
            	offerToAdd.setCurrency(selectedOffer.getPayout_currency());
            	offerToAdd.setPayout(round(selectedOffer.getPayout_amount(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setCallToAction(selectedOffer.getIncentive_action());
            	offerToAdd.setTrackingRequirements(selectedOffer.getMatching_requirements());
    			//get images
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
    			imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getImage_url());
            	offerToAdd.setImage(imagesMap);

            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, 
    					offerWall, OfferProviderCodeNames.AARKI);
    			if(isOfferAcceptedByGlobalFilter) {
    				//apply geo filtering
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    						offerWall, OfferProviderCodeNames.AARKI, selectedOffer.getCountries());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, offerWall, OfferProviderCodeNames.AARKI, selectedOffer.getDevice_targeting());
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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.AARKI);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.AARKI);
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
    						OfferProviderCodeNames.AARKI+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.AARKI+
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
