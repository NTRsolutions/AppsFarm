package is.ejb.bl.offerProviders.hasoffers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroupOfferIds.FindAllOfferGroupOfferIds;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.DataEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.FindAllOfferGroups;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended.Data;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended.FindOfferByIdExtended;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended.Offer;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended.Thumbnail;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.GetOfferThumbnail;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.OfferWallEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class HasoffersAPIManager {

	@Inject
	private Logger logger;

	public ArrayList<DataEntry> findAllOfferGroups(OfferWallEntity offerWall, String networkId, String networkToken, String methodCalled) throws Exception {
		ArrayList<DataEntry> listOfferGroups = new ArrayList<DataEntry>();
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+
				"&Target=Application&Method="+methodCalled+"&NetworkToken="+networkToken;
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, OfferProviderCodeNames.HASOFFERS+" "+methodCalled+" rest method called, content: "+requestUrl);

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.HASOFFERS+" requesting url: "+requestUrl);

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

    	//logger.info(reqResponse);
     	try {
            //Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
     		//	offerWall.getRealm().getId(), LogStatus.OK, 
     		//	OfferProviderCodeNames.HASOFFERS+" "+reqResponse);
     		
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		FindAllOfferGroups obj = mapper.readValue(reqResponse, FindAllOfferGroups.class);

    		Map<String, DataEntry> map = obj.getResponse().getData();
            Set<String> keys = map.keySet();
            Iterator it = keys.iterator();
            while(it.hasNext()) {
            	String key = (String)it.next();
            	DataEntry de = (DataEntry)map.get(key);
            	listOfferGroups.add(de);
            	//logger.info("found group id: "+de.getOfferGroup().getId()+" group name: "+de.getOfferGroup().getName());
            }
            
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.HASOFFERS+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);
    	}
     	
		return listOfferGroups;
	}

	public ArrayList<Integer> findAllOfferGroupOfferIds(OfferWallEntity offerWall, 
			IndividualOfferWall individualOfferWall,
			String networkId, String networkToken, String methodCalled, int groupId) throws Exception {
		ArrayList<Integer> listOfferIds = new ArrayList<Integer>();
		
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Application&Method="+methodCalled+"&NetworkToken="+networkToken+"&id="+groupId;
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, 
				OfferProviderCodeNames.HASOFFERS+" "+ methodCalled);//+" rest method called, content: "+requestUrl);
		
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

     	try {
         	//Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
         	//offerWall.getRealm().getId(), 
         	//LogStatus.OK, OfferProviderCodeNames.HASOFFERS+" rest response: "+reqResponse);
            
         	//serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		FindAllOfferGroupOfferIds obj = mapper.readValue(reqResponse, FindAllOfferGroupOfferIds.class);

            List<Integer> list = obj.getResponse().getData();
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK,
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.HASOFFERS+" offer pool size: "+ list.size());//+" rest method called, content: "+requestUrl);

            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+list.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+list.size());

            Iterator it = list.iterator();
            while(it.hasNext()) {
            	int id = (int)it.next();
            	listOfferIds.add(id);
            	//logger.info("found offer id: "+id);
            }
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.HASOFFERS+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);

    	}

		return listOfferIds;
	}

	public ArrayList<is.ejb.bl.offerWall.content.Offer> selectOffers(OfferWallEntity offerWall,
						IndividualOfferWall individualOfferWall,
						HasoffersProviderConfig adProviderConfig,
						OfferFilterManager offerFilterManager,
						OfferRewardCalculationManager offerRewardCalculationManager,
						ArrayList<Integer> listOfferIdsPool, 
						int numberOfOffersToSelect, 
						String networkId, 
						String networkToken) throws Exception {

		
		//offer wall stats
		offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()+numberOfOffersToSelect);
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_requested_offers+":"+numberOfOffersToSelect);

		ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>(); 
		ArrayList<Integer> listSelectedOfferIds = new ArrayList<Integer>();
		
		//generate offer wall
		if(listOfferIdsPool.size() < numberOfOffersToSelect) {
			numberOfOffersToSelect = listOfferIdsPool.size();
		} 

		//pick offers randomly and make sure they do not repeat in the offer wall
		while(listSelectedOfferIds.size() < (numberOfOffersToSelect)) {
			if(listOfferIdsPool.size() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.WARNING, 
						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+
						OfferProviderCodeNames.HASOFFERS+" no offers left to pick from Hasoffers - possibly most were rejected");
				break;
			}
			
			int randomNumber = (int)(Math.random()*listOfferIdsPool.size());
			int selectedOfferId = listOfferIdsPool.get(randomNumber);
			//TODO add sleep timeout here - this should be a configurable parameter passed in hasoffers configuration so we could adjust it
			
			//get offer from Hasoffers
			Data selectedOfferData = findOfferById(offerWall, networkId, networkToken,"findById", selectedOfferId);
			
			//sleep between calls as offer provider may reject large number of requests made within a short period of time
			Thread.sleep(adProviderConfig.getServiceQueryInterval());

			//transform it into our internal format
			is.ejb.bl.offerWall.content.Offer offerToAdd = new is.ejb.bl.offerWall.content.Offer();
			Offer selectedOffer = selectedOfferData.getOffer();
			offerToAdd.setSourceId(selectedOffer.getId()+"");
        	//generate unique offer id used by our system to track offer conversion
        	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
        										offerWall.getNumberOfOffers()+
        										Math.random()*100000+
        										System.currentTimeMillis()+
        										randomNumber+
        										selectedOffer.getName()+
        										selectedOffer.getOffer_url()+
        										numberOfOffersToSelect+"");
        	offerToAdd.setId(sha1Id);
        	offerToAdd.setAffiliateId(adProviderConfig.getAffiliateId());
        	offerToAdd.setTitle(selectedOffer.getName());
        	offerToAdd.setDescription(selectedOffer.getDescription());
        	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS.toString());
        	offerToAdd.setPreviewUrl(selectedOffer.getPreview_url());
        	offerToAdd.setUrl(selectedOffer.getOffer_url());
        	offerToAdd.setCurrency(selectedOffer.getCurrency());
        	offerToAdd.setPayout(round(selectedOffer.getDefault_payout(),2));
        	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
        	//set supported countries based on offer wall settings (as HO offers are configured on HO side to match required criteria
        	ArrayList<String> supportedCountries = new ArrayList<String>();
        	supportedCountries.add(offerWall.getTargetCountriesFilter());
        	offerToAdd.setSupportedCountryCodes(supportedCountries);
        	//set supported target devices based on offer wall settings (as HO offers are configured on HO side to match required criteria
        	ArrayList<String> supportedDevices = new ArrayList<String>();
        	supportedDevices.add(offerWall.getTargetDevicesFilter());
        	offerToAdd.setSupportedTargetDevices(supportedDevices);
        	
			//request images
			Thumbnail thumbnail = selectedOfferData.getThumbnail();
			if(thumbnail != null) {
				HashMap<String,String> imagesMap = new HashMap<String,String>();
				//imagesMap.put(ThumbnailQuality.hires.toString(), thumbnail.getUrl());
				imagesMap.put(ThumbnailQuality.Image+"-"+1, thumbnail.getUrl());
				imagesMap.put(ThumbnailQuality.Image+"-"+2, thumbnail.getThumbnail());
				offerToAdd.setImage(imagesMap);
			} else {
				offerToAdd.setImage(null);
			}

			boolean isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS);
			
			//------------------------- check for duplicates starts ----------------
			//check for duplicate offers and select one with higher payout

			boolean isOfferAcceptedByDuplicatesFilter = true;
			boolean isOfferDuplicate = false; 
			boolean isOfferDuplicateWithHigherPayout = false;
			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()) { //if filter for rejecting duplicate offers is enabled - trigger it
				if(isOfferAcceptedByGlobalFilter) { //process only if offer is accepted by previous filters!
					//calculate reward value/profit/split and add to offer data
					offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS);
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
			} else if(isOfferAcceptedByGlobalFilter){ //if not checking duplicates
				//calculate reward value/profit/split and add to offer data
				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS);
			}
			//-------------------------- check for duplicates ends ----------------
			
			if(isOfferAcceptedByGlobalFilter && isOfferAcceptedByDuplicatesFilter) {//add offer to the wall
				listSelectedIndividualOffers.add(offerToAdd); //add to list
				listSelectedOfferIds.add(selectedOfferId); 
				listOfferIdsPool.remove(randomNumber); //remove successfully added offer id from the pool of ids that we select from potential offers
				
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
						offerWall.getRealm().getId(), 
						LogStatus.OK, 
						Application.SINGLE_OFFER_CREATED+" "+
						OfferProviderCodeNames.HASOFFERS+" successfully created offer "+
						offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
			} else { //if below threshold - remove that offer id from the pool of offer ids as well
				listOfferIdsPool.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
			}
		}

        //update offer stats
        offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());

		return listSelectedIndividualOffers;
	}

	public Data findOfferById(OfferWallEntity offerWall, String networkId, String networkToken, String methodCalled, int selectedOfferId) throws Exception {

		Data data = null;
		String requestUrl = "https://api.hasoffers.com/Api/json?NetworkId="+networkId+
				"&NetworkToken="+networkToken+
				"&Target=Offer&Method="+methodCalled+"&id="+selectedOfferId+
				"&contain%5B%5D=Thumbnail";

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				OfferProviderCodeNames.HASOFFERS+" "+methodCalled+" rest method called, content: "+requestUrl);

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

     	try {
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
            		offerWall.getRealm().getId(), 
            		LogStatus.OK, 
            		OfferProviderCodeNames.HASOFFERS+" rest response: "+reqResponse);
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		FindOfferByIdExtended obj = mapper.readValue(reqResponse, FindOfferByIdExtended.class);

            data = obj.getResponse().getData();
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.HASOFFERS+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);
    	}

        return data;
	}

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

}
