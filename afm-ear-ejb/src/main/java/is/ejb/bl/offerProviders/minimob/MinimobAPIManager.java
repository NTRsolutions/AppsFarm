package is.ejb.bl.offerProviders.minimob;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
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

public class MinimobAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										MinimobProviderConfig adProviderConfig,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										String apiKey, 
										int numberOfOffersToSelect) throws Exception {

		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);

		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        String requestUrl = "http://dashboard.minimob.com/api/myoffers/?apikey="+apiKey;
        
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.MINIMOB+" requesting url: "+requestUrl);

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
        //Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "HASOFFERS rest response: "+reqResponse);

    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>(); 
     	try {
        	ArrayList<String> listOfferIdsPool = new ArrayList<String>();
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		MinimobOfferId[] arrayOfIdObjects = mapper.readValue(reqResponse, MinimobOfferId[].class);
    		
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.MINIMOB+" offer pool size: "+ arrayOfIdObjects.length);//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+arrayOfIdObjects.length);
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+arrayOfIdObjects.length);
    		
    		for(int i=0;i<arrayOfIdObjects.length;i++){
    			MinimobOfferId idObject = arrayOfIdObjects[i];
    			if(idObject.getIncentivized().equals("Incentivized")){
    				listOfferIdsPool.add(idObject.getId());
    			}
            	//logger.info("found offer id: "+idObject.getId()+" "+idObject.getIncentivized()+" "+idObject.getPayoutCurrency()+" "+idObject.getPayout()+" "+idObject.getTargetedCountries());
    		}

    		//------------------------------ fill list of offers ---------------------------------------
    		//generate offer wall
    		if(listOfferIdsPool.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listOfferIdsPool.size();
    		} 

    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listOfferIdsPool.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.MINIMOB+" no offers left to pick from Hasoffers - possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listOfferIdsPool.size());
    			String selectedOfferId = listOfferIdsPool.get(randomNumber);
    			MinimobOffer minimobOffer = getMinimobOffer(selectedOfferId, apiKey, offerWall);
    			
    			//sleep between calls as offer provider may reject large number of requests made within a short period of time
    			Thread.sleep(adProviderConfig.getServiceQueryInterval());
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			offerToAdd.setSourceId(minimobOffer.getId());
            	//generate unique offer id used by our system to track offer conversion
            	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
            										offerWall.getNumberOfOffers()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										randomNumber+
            										minimobOffer.getName()+
            										minimobOffer.getObjectiveUrl()+
            										numberOfOffersToSelect+"");
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setTitle(minimobOffer.getName());
            	offerToAdd.setDescription(minimobOffer.getAppDescription());
            	offerToAdd.setCallToAction(minimobOffer.getAcquisitionModelDescription());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.MINIMOB.toString());
            	offerToAdd.setPreviewUrl(minimobOffer.getAppPreviewLink());
            	offerToAdd.setUrl(minimobOffer.getObjectiveUrl());
            	offerToAdd.setCurrency(minimobOffer.getPayoutCurrency());
            	offerToAdd.setPayout(round(minimobOffer.getPayout(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
    			//get images
            	//logger.info(offerToAdd.getTitle()+" got image: "+minimobOffer.getCreatives());
            	List<CreativesEntry> listCreatives =  minimobOffer.getCreatives();
            	if(minimobOffer.getCreatives() != null) {
                	//logger.info(offerToAdd.getTitle()+" size: "+minimobOffer.getCreatives().size());

    				HashMap<String,String> imagesMap = new HashMap<String,String>();
    				for(int i=0;i<listCreatives.size();i++){
    					CreativesEntry creative = listCreatives.get(i);
    					imagesMap.put(ThumbnailQuality.Image+"-"+(i+1), creative.getPreviewUrl());
    					//logger.info(offerToAdd.getTitle()+" adding image-"+(i+1)+" "+creative.getPreviewUrl());
    				}
    	        	offerToAdd.setImage(imagesMap);
            	} else {
    				offerToAdd.setImage(null);
    			}
            	
            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.MINIMOB);
    			if(isOfferAcceptedByGlobalFilter) {
    				//apply geo filtering
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, offerWall, OfferProviderCodeNames.MINIMOB, minimobOffer.getTargetedCountries());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					ArrayList<String> listSupportedTargetPlatforms = new ArrayList<String>();
    					listSupportedTargetPlatforms.add(minimobOffer.getTargetPlatform());
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, offerWall, OfferProviderCodeNames.MINIMOB, listSupportedTargetPlatforms);
    				}
    			}

    			//------------------------- check for duplicates starts ----------------
    			//check for duplicate offers and select one with higher payout
    			boolean isOfferAcceptedByDuplicatesFilter = true;
    			boolean isOfferDuplicate = false; 
    			boolean isOfferDuplicateWithHigherPayout = false;
    			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()) { //if filter for rejecting duplicate offers is enabled - trigger it
        			if( isOfferAcceptedByGlobalFilter && 
        					isOfferAcceptedByDeviceFilter && 
        						isOfferAcceptedByGeoFilter) { //process only if offer is accepted by previous filters!
        				//calculate reward value/profit/split and add to offer data
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.MINIMOB);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.MINIMOB);
    			}
    			//-------------------------- check for duplicates ends ----------------

    			//logger.info("=====> Accepted offer: "+minimobOffer.getName()+" global filter: "+isOfferAcceptedByGlobalFilter+" geo filter: "+isOfferAcceptedByGeoFilter+" device filter: "+isOfferAcceptedByDeviceFilter);
    			if(isOfferAcceptedByGlobalFilter && 
    					isOfferAcceptedByDeviceFilter && 
    						isOfferAcceptedByGeoFilter &&
    							isOfferAcceptedByDuplicatesFilter) {//add offer to the wall
    				listSelectedIndividualOffers.add(offerToAdd); //add to list
    				listOfferIdsPool.remove(randomNumber); //remove successfully added offer id from the pool of ids that we select from potential offers
    				
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.OK, 
    						Application.SINGLE_OFFER_CREATED+" "+
    						OfferProviderCodeNames.MINIMOB+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listOfferIdsPool.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.MINIMOB+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);
    	}

        //update offer stats
        offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());

		return listSelectedIndividualOffers;
	}

	private MinimobOffer getMinimobOffer(String offerId, String apiKey, OfferWallEntity offerWall) throws JsonParseException, JsonMappingException, IOException {
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        String requestUrl = "http://dashboard.minimob.com/api/myoffers/?apikey="+apiKey+"&id="+offerId;
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
//    			LogStatus.OK, 
//    			" "+OfferProviderCodeNames.MINIMOB+ 
//    			" rest response: "+reqResponse);
    	
        //serialise response into object
		ObjectMapper mapper = new ObjectMapper();
		MinimobOffer offer = mapper.readValue(reqResponse, MinimobOffer.class);
		
		return offer;
	}
	
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }
}
