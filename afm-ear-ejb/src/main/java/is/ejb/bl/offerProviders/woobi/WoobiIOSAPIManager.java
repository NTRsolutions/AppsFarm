package is.ejb.bl.offerProviders.woobi;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.woobi.getIOSOffers.OffersEntry;
import is.ejb.bl.offerProviders.woobi.getIOSOffers.WoobiIOSGetOffers;
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

public class WoobiIOSAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
										IndividualOfferWall individualOfferWall,
										OfferFilterManager offerFilterManager,
										OfferRewardCalculationManager offerRewardCalculationManager,
										WoobiProviderConfig adProviderConfig,
										int numberOfOffersToSelect) throws Exception {

		//offer wall stats
        offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);
        
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
        String requestUrl = ""; 
       	requestUrl = adProviderConfig.getApiUrl();
       	//add target country filter to end of request
       	requestUrl = requestUrl + "&ctr="+offerWall.getTargetCountriesFilter();	 
       	
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.WOOBI_IOS+" requesting url: "+requestUrl);

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

     	try {
	        //serialise response into object
			ObjectMapper mapper = new ObjectMapper();
			WoobiIOSGetOffers offersHolder = mapper.readValue(reqResponse, WoobiIOSGetOffers.class);
			List<OffersEntry> listReturnedOffers = offersHolder.getOffers();
			Iterator i = listReturnedOffers.iterator();
			while(i.hasNext()) {
				OffersEntry offer = (OffersEntry)i.next();
/*				
				logger.info("=============================");
				logger.info(offer.getAdId()+"");
				//logger.info(offer.getAppId());
				logger.info(offer.getAppPublisher());
				logger.info("http:"+offer.getClickURL());
				logger.info(offer.getDeviceType());
				logger.info(offer.getGeoCode()+"");
				logger.info(offer.getIncent());
				logger.info(offer.getPayout()+"");
				logger.info(offer.getTitle());
				logger.info(offer.getTitle());
				logger.info(offer.getPriceCurrency());
				logger.info(offer.getSubtitle()); //call to action

				logger.info("http:"+offer.getArtworkIcon());
				logger.info("http:"+offer.getArtworkLong());
				logger.info("http:"+offer.getArtworkSqr());
				logger.info("http:"+offer.getArtworkWide());
				logger.info("http:"+offer.getThumbnail());
*/				
				listPulledOffers.add(offer);
			}

	
    		//------------------------------------------------------------------------------------------
    		//------------------------------ fill list of offers ---------------------------------------
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.WOOBI_IOS+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());
            
    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.WOOBI_IOS+" total pulled offers: "+listPulledOffers.size());
    		
    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.WOOBI_IOS+
    						" no offers left to pick- possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listPulledOffers.size());
    			OffersEntry selectedOffer = listPulledOffers.get(randomNumber);
    			
    			//transform it into our internal format
    			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
    			
    			offerToAdd.setSourceId(selectedOffer.getAdId()+"");
            	//generate unique offer id used by our system to track offer conversion
            	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
            										offerWall.getNumberOfOffers()+
            										Math.random()*100000+
            										System.currentTimeMillis()+
            										randomNumber+
            										selectedOffer.getTitle()+
            										selectedOffer.getClickURL()+
            										numberOfOffersToSelect+"");
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setTitle(selectedOffer.getTitle());
            	offerToAdd.setDescription(selectedOffer.getDescription());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.WOOBI_IOS.toString());
            	offerToAdd.setPreviewUrl(selectedOffer.getAppDomain());
            	offerToAdd.setUrl("http:"+selectedOffer.getClickURL());
            	offerToAdd.setCurrency(selectedOffer.getPriceCurrency());
            	offerToAdd.setPayout(round(selectedOffer.getPayout(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setCallToAction(selectedOffer.getSubtitle());
            	String strIncent = selectedOffer.getIncent();
            	if(selectedOffer.getIncent().toUpperCase().equals("YES")) {
            		offerToAdd.setIncentivised(true);
            	} else {
            		offerToAdd.setIncentivised(false);
            	}
				//apply geo filtering
				ArrayList<String> targetedCountries = new ArrayList<String>();  
				targetedCountries.add(selectedOffer.getGeoCode().get(0));
				offerToAdd.setSupportedCountryCodes(targetedCountries);
				//supported devices
				ArrayList<String> supportedDevices = new ArrayList<String>();
            	supportedDevices.add(selectedOffer.getDeviceType());
            	offerToAdd.setSupportedTargetDevices(supportedDevices);
    			//get images
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
				imagesMap.put(ThumbnailQuality.Image+"-"+1, "http:"+selectedOffer.getThumbnail());
				imagesMap.put(ThumbnailQuality.Image+"-"+2, "http:"+selectedOffer.getArtworkIcon());
				imagesMap.put(ThumbnailQuality.Image+"-"+3, "http:"+selectedOffer.getArtworkLong());
				imagesMap.put(ThumbnailQuality.Image+"-"+4, "http:"+selectedOffer.getArtworkSqr());
				imagesMap.put(ThumbnailQuality.Image+"-"+5, "http:"+selectedOffer.getArtworkWide());
				imagesMap.put(ThumbnailQuality.Image+"-"+6, "http:"+selectedOffer.getArtworkIcon());
            	offerToAdd.setImage(imagesMap);
            	
            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.WOOBI_IOS);
    			if(isOfferAcceptedByGlobalFilter) {
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.WOOBI_IOS, 
    							offerToAdd.getSupportedCountryCodes());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.WOOBI_IOS, 
    							supportedDevices);
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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.WOOBI_IOS);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.WOOBI_IOS);
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
    						OfferProviderCodeNames.WOOBI_IOS+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		throw new Exception(OfferProviderCodeNames.WOOBI_IOS+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider");//: "+reqResponse);
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
