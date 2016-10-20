package is.ejb.bl.offerProviders.fyber;

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
import is.ejb.bl.offerProviders.supersonic.getOffers.ImagesEntry;
import is.ejb.bl.offerProviders.trialpay.TrialPayProviderConfig;
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
import org.codehaus.jackson.map.ObjectMapper;

public class FyberAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<Offer> getOffers(OfferWallEntity offerWall,
			IndividualOfferWall individualOfferWall,
			OfferFilterManager offerFilterManager,
			OfferRewardCalculationManager offerRewardCalculationManager,
			RealtimeFeedDataHolder realtimeFeedDH,
			TrialPayProviderConfig providerConfig) throws Exception {
		
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();
		
		String ip = "31.61.138.94";
		long unixTime = System.currentTimeMillis() / 1000L;
		//String requestUrl = "appid="+appId+"&device_id=2b6f0cc904d137be2e1730235f5664094b831186&timestamp="+unixTime+"&uid=player1";
		//String requestUrlWithApiKey = "appid="+appId+"&device_id=2b6f0cc904d137be2e1730235f5664094b831186&timestamp="+unixTime+"&uid=player1&"+apiKey;

		//String requestUrl = "appid="+appId+"&device_id=111&timestamp="+unixTime+"&uid=testUser";
		//String requestUrlWithApiKey = "appid="+appId+"&device_id=111&timestamp="+unixTime+"&uid=testUser&"+apiKey;
		
		//String requestUrl = "appid="+appId+"&device_id=111&locale=pl&offer_types=101,112&timestamp="+unixTime+"&uid=testUser";
		//String requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=pl&offer_types=101,112&timestamp="+unixTime+"&uid=testUser&"+apiKey;

		//String requestUrl = "appid="+appId+"&device_id=111&locale=pl&timestamp="+unixTime+"&uid=testUser";
		//String requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=pl&timestamp="+unixTime+"&uid=testUser&"+apiKey;
		String requestUrl = "";
		String requestUrlWithApiKey = "";
		
//		if(offerTypes != null && offerTypes.length()>0) {
//			requestUrl = "appid="+appId+"&device_id=111&locale=EN&offer_types="+offerTypes+"&timestamp="+unixTime+"&uid=testUser";
//			requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=EN&offer_types="+offerTypes+"&timestamp="+unixTime+"&uid=testUser&"+apiKey;
//		} else {
//			requestUrl = "appid="+appId+"&device_id=111&locale=EN&timestamp="+unixTime+"&uid=testUser";
//			requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=EN&timestamp="+unixTime+"&uid=testUser&"+apiKey;
//		}

		//http://developer.fyber.com/content/android/offer-wall/offer-api/
        String sha1RequestUrl = DigestUtils.sha1Hex(requestUrlWithApiKey); 
        //System.out.println("sha1: "+sha1RequestUrl);
        requestUrl = "http://api.sponsorpay.com/feed/v1/offers.json?&"+requestUrl+"&hashkey="+sha1RequestUrl;
        //System.out.println("full req: "+requestUrl);
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "FYBER getOffers rest method called, content: "+requestUrl);
		
		requestUrl = new FyberTest().getFyberTestUrl();

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.FYBER+" requesting url: "+requestUrl);
		
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
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

    	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
		LogStatus.OK, 
		" "+OfferProviderCodeNames.FYBER+
		" req url: "+requestUrl+
		" req status: "+urlConnection.getResponseCode()+
		" rest response length: "+reqResponse.length());

    	int numberOfOffersToSelect = providerConfig.getNumberOfPulledOffers();
    	ArrayList<OffersEntry> listPulledOffers = new ArrayList<OffersEntry>();
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>();

        //serialise response into object
		ObjectMapper mapper = new ObjectMapper();
		GetOffers obj = mapper.readValue(reqResponse, GetOffers.class);
    	try {

			List<OffersEntry> listOffers = obj.getOffers();
	        Iterator it = listOffers.iterator();
	        while(it.hasNext()) {
	        	OffersEntry fyberNativeOffer = (OffersEntry)it.next();
	        	listPulledOffers.add(fyberNativeOffer);
	        }
	        
	        //report retrieved offer pool size
	        Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
					offerWall.getRealm().getId(), LogStatus.OK, 
					Application.OFFER_POOL_SIZE+" "+
					OfferProviderCodeNames.FYBER+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
	
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

	        	System.out.println("");
	        	System.out.println("fyber offer id: "+selectedOffer.getOffer_id());
	        	System.out.println("fyber offer title: "+selectedOffer.getTitle());
	        	System.out.println("fyber offer link: "+selectedOffer.getLink());
	        	System.out.println("fyber offer teaser: "+selectedOffer.getTeaser());
	        	System.out.println("fyber offer payout: "+selectedOffer.getPayout());
	        	System.out.println("fyber offer thumbnail: "+selectedOffer.getThumbnail().toString());
				
				offerToAdd.setSourceId(selectedOffer.getOffer_id()+"");
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
	        	offerToAdd.setSourceId(selectedOffer.getOffer_id()+"");
	        	offerToAdd.setTitle(selectedOffer.getTitle());
	        	offerToAdd.setDescription(selectedOffer.getTeaser());
	        	if(selectedOffer.getRequired_actions().length()>240) {
	            	offerToAdd.setCallToAction(selectedOffer.getRequired_actions().substring(0,230));
	        	} else {
	        		offerToAdd.setCallToAction(selectedOffer.getRequired_actions());
	        	}
	        	
	        	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.FYBER.toString());
	        	offerToAdd.setPreviewUrl(selectedOffer.getLink());
	        	offerToAdd.setUrl(selectedOffer.getLink());
	        	offerToAdd.setCurrency("USD");
	        	offerToAdd.setPayout(round((double)(selectedOffer.getPayout()/(double)100),2));
	        	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
	        	offerToAdd.setTrackingRequirements(new ArrayList<String>());
				//get images
	        	
    			HashMap<String,String> imagesMap = new HashMap<String,String>();
	        	if(selectedOffer.getThumbnail() != null && 
	    			selectedOffer.getThumbnail().getLowres() != null &&
	    					selectedOffer.getThumbnail().getLowres().length() >0 ) {
	    			//get lowres
					imagesMap.put(ThumbnailQuality.Image+"-"+(1), selectedOffer.getThumbnail().getLowres());
	    			//get hires
		        	if(selectedOffer.getThumbnail().getHires() != null && selectedOffer.getThumbnail().getHires().length() >0 ) {
						imagesMap.put(ThumbnailQuality.Image+"-"+(2), selectedOffer.getThumbnail().getHires());
	            	}
		        	offerToAdd.setImage(imagesMap);
	        	}
	        	
	        	//filters - set to true as we don't filter this offer (its automatically filtered by the provider based on request data)
	        	boolean isOfferAcceptedByGlobalFilter = false;
				boolean isOfferAcceptedByGeoFilter = true;
				boolean isOfferAcceptedByDeviceFilter = true;
	
				isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, 
						offerWall, OfferProviderCodeNames.FYBER);

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
	    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.FYBER);
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
					offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.FYBER);
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
							OfferProviderCodeNames.FYBER+" successfully created offer "
									+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
				} else { //if below threshold - remove that offer id from the pool of offer ids as well
					listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
				}
			}
		} catch(Exception exc) {
			exc.printStackTrace();
	        throw new Exception(OfferProviderCodeNames.FYBER+
	        		" Error: "+exc.toString()+" when retrieving/processing data from offer provider");//+reqResponse);
		}
	
	    //update offer stats
	    offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
	    individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());
	
		return listSelectedIndividualOffers;
	}

	public ArrayList<Offer> selectOffers(OfferWallEntity offerWall, ArrayList<Offer> listAllOffers, int numberOfOffersToSelect) throws Exception {
		ArrayList<Offer> listSelectedOffers = new ArrayList<Offer>();
		
		boolean offerAlreadyAdded = false;
		int safetyCounter = 10000;
		int currentCounter = 0;
		
		//generate offer wall
		if(listAllOffers.size() < numberOfOffersToSelect) {
			numberOfOffersToSelect = listAllOffers.size();
		} 

		//pick offers randomly and make sure they do not repeate in the offer wall
		while(listSelectedOffers.size() < (numberOfOffersToSelect)) {
			currentCounter = currentCounter + 1;
			if(currentCounter > safetyCounter) {
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.ERROR, "FYBER safety counter exceeded when selecting offer ids to pick");
			}
			int randomNumber = (int)(Math.random()*listAllOffers.size());
			//System.out.println("got random number: "+randomNumber);
			Offer selectedOffer = listAllOffers.get(randomNumber);
			
			for(int i=0;i<listSelectedOffers.size();i++) {
				if(listSelectedOffers.get(i).getTitle().equals(selectedOffer.getTitle())) {
					offerAlreadyAdded = true;
					break;
				}
			}
			
			//add offer if it was not added before
			if(!offerAlreadyAdded) {
				//System.out.println("adding selected offer: "+selectedOffer.getTitle());
				listSelectedOffers.add(selectedOffer);
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.OK, 
						Application.SINGLE_OFFER_CREATED+" FYBER successfully created offer "
								+selectedOffer.getTitle()+" offerId: "+selectedOffer.getId());
			} else {
				offerAlreadyAdded = false;
			}
		}
		
		return listSelectedOffers;
	}

	public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }
	
}
