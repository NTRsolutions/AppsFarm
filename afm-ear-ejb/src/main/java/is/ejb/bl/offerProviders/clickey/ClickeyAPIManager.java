package is.ejb.bl.offerProviders.clickey;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.clickey.getOffers.CreativeEntry;
import is.ejb.bl.offerProviders.clickey.getOffers.GetOffers;
import is.ejb.bl.offerProviders.clickey.getOffers.OffersEntry;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.OfferWallEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

public class ClickeyAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										ClickeyProviderConfig adProviderConfig,
										int numberOfOffersToSelect) throws Exception {

		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);
        
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        //http://cpactions.com/api/v1.0/feed/public/offers?site_id=7334&hash=6d653eb58cbfd8f7abf9fdae395af1ec61030617&items_per_page=1000&traffic_type=1&country=GB&os=android&filters[os][or][]=ios
        String requestUrl = ""; 
       	requestUrl = adProviderConfig.getApiUrl();
       	//add target country filter to end of request
       	if(offerWall.getTargetDevicesFilter().toLowerCase().equals("android")) {
           	requestUrl = requestUrl + "&filters[os][and][]=android";	 
       	} else if(offerWall.getTargetDevicesFilter().toLowerCase().equals("ios")) {
           	requestUrl = requestUrl + "&filters[os][or][]=ios&filters[os][or][]=ios";	 
       	}  
 
       	//add country filter to end of request
       	requestUrl = requestUrl + "&country="+offerWall.getTargetCountriesFilter().toLowerCase();	 

       	//add conversion filter
       	//requestUrl = requestUrl + "&filters[avg_cr][gt]=0.5&filters[avg_cr][lt]=100";
       	       	
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.CLICKKY+" requesting url: "+requestUrl);
		logger.info("requesting url: "+requestUrl);
		
    	try {
    		URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
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
    	
    	//logger.info(reqResponse);

    	ArrayList<OffersEntry> listPulledOffers = new ArrayList<OffersEntry>();
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>(); 
        //serialise response into object
//		ObjectMapper mapper = new ObjectMapper();
//		GetOffers offersHolder = mapper.readValue(reqResponse, GetOffers.class);
//		List offersEntry = offersHolder.getOffers();
//		for(int i=0;i<offersEntry.size();i++) {
//			OffersEntry offer = (OffersEntry)offersEntry.get(i);
//			logger.info("got offer: "+offer.getName()+" "+offer.getLink()+" "+offer.getDescription()+" "+offer.getIcon()+" "+offer.getInstructions());
//		}
		
     	try {
	        //serialise response into object
			ObjectMapper mapper = new ObjectMapper();
			GetOffers offersHolder = mapper.readValue(reqResponse, GetOffers.class);
			List<OffersEntry> listReturnedOffers = offersHolder.getOffers();

			List offersEntry = offersHolder.getOffers();
			for(int i=0;i<offersEntry.size();i++) {
				OffersEntry offer = (OffersEntry)offersEntry.get(i);
				logger.info("got offer: "+offer.getName()+" "+offer.getLink()+" "+offer.getDescription()+" "+offer.getIcon()+" "+offer.getInstructions());
				listPulledOffers.add(offer);
			}

    		//------------------------------------------------------------------------------------------
    		//------------------------------ fill list of offers ---------------------------------------
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.CLICKKY+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());
            
    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.CLICKKY+" total pulled offers: "+listPulledOffers.size());
    		
    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.CLICKKY+
    						" no offers left to pick- possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listPulledOffers.size());
    			OffersEntry selectedOffer = listPulledOffers.get(randomNumber);
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			
    			offerToAdd.setSourceId(selectedOffer.getOffer_id()+"");
            	//generate unique offer id used by our system to track offer conversion
            	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
            										offerWall.getNumberOfOffers()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										randomNumber+
            										selectedOffer.getName()+
            										selectedOffer.getLink()+
            										numberOfOffersToSelect+"");
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setTitle(selectedOffer.getName());
            	offerToAdd.setDescription(selectedOffer.getDescription());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.CLICKKY.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getLink());
            	offerToAdd.setUrl(selectedOffer.getLink());
            	offerToAdd.setCurrency("USD"); //assume its in USD
            	offerToAdd.setPayout(round(selectedOffer.getPayout(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setCallToAction(selectedOffer.getInstructions());
            	
            	String trafficType = selectedOffer.getTraffic_type();
            	if(trafficType.toLowerCase().equals("incentive")) {
            		offerToAdd.setIncentivised(true);
            	} else {
            		offerToAdd.setIncentivised(false);
            	}
            	
            	logger.info("dupa2");
				//apply geo filtering
				ArrayList<String> targetedCountries = new ArrayList<String>();
				targetedCountries.add(offerWall.getTargetCountriesFilter()); //use target countries filter from offer wall
				offerToAdd.setSupportedCountryCodes(targetedCountries);
				//supported devices
            	ArrayList<String> supportedDevices = new ArrayList<String>();
            	supportedDevices.add(offerWall.getTargetDevicesFilter());
            	offerToAdd.setSupportedTargetDevices(supportedDevices);
    			//get images
            	logger.info("dupa3");
            	HashMap<String,String> imagesMap = new HashMap<String,String>();
            	if(selectedOffer.getIcon() != null) {
        			imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getIcon());
            	}

            	if(selectedOffer.getCreative() != null) {
	    			List<CreativeEntry> listCreatives = selectedOffer.getCreative();
	    			for(int i=0;i<listCreatives.size();i++) {
	    				CreativeEntry creative = listCreatives.get(i);
	    				if(creative != null && creative.getUrl() != null) {
	        				imagesMap.put(ThumbnailQuality.Image+"-"+(i+2), creative.getUrl());
	        				logger.info("adding images: "+creative.getUrl());
	    				}
	    			}
				}

            	offerToAdd.setImage(imagesMap);

            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.CLICKKY);
    			if(isOfferAcceptedByGlobalFilter) {
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.CLICKKY, 
    							offerToAdd.getSupportedCountryCodes());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.CLICKKY, 
    							supportedDevices);
    				}
    			}

    			//------------------------- check for duplicates starts ----------------
    			//check for duplicate offers and select one with higher payout
    			boolean isOfferAcceptedByDuplicatesFilter = true;
    			boolean isOfferDuplicate = false;
    			boolean isOfferDuplicateWithHigherPayout = false;
    			logger.info("dupa4");
    			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()) { //if filter for rejecting duplicate offers is enabled - trigger it
        			if(isOfferAcceptedByGlobalFilter && 
        					isOfferAcceptedByDeviceFilter && 
        							isOfferAcceptedByGeoFilter) { //process only if offer is accepted by previous filters!
        				//calculate reward value/profit/split and add to offer data
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.CLICKKY);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.CLICKKY);
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
    						OfferProviderCodeNames.CLICKKY+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.CLICKKY+
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
