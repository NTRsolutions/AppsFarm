package is.ejb.bl.offerProviders.hasoffersNativex;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.GetOfferThumbnail;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.Thumbnail;
import is.ejb.bl.offerProviders.hasoffersExt.getOffer.GetOffer;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.DataEntry;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.GetOfferFileInfo;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.OfferFile;
import is.ejb.bl.offerProviders.hasoffersExt.getThumbnail.GetThumbnail;
import is.ejb.bl.offerProviders.hasoffersExt.getTrackingLink.GetTrackingLink;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class HasoffersNativexAPIManager {

	@Inject
	private Logger logger;
	private ArrayList<OfferFile> listOfferFileData = new ArrayList<OfferFile>();
	
	public ArrayList<Offer> findAllOffers(OfferWallEntity offerWall,
			IndividualOfferWall individualOfferWall,
			OfferFilterManager offerFilterManager,
			OfferRewardCalculationManager offerRewardCalculationManager,
			HasoffersNativexProviderConfig adProviderConfig,
			SingleOfferWallConfiguration singleOfferWallConfig) throws Exception {

		int numberOfOffersToSelect = singleOfferWallConfig.getNumberOfOffers();
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>(); 

		//adProviderConfig.setCategoryName("incentivised offers");
		
//		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+adProviderConfig.getNetworkId()+""
//				+ "&Target=Affiliate_Application"
//				+ "&Method=findAllOfferCategories"
//				+ "&api_key=2e0c0c9c08b3c0e864fd9bafda116ce5eb295e374ea92f108875ccbe62198dda"
//				+ "&filters%5Bname%5D=incentivised+offers";
				
		//String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId=nativex&Target=Affiliate_Offer&Method=findAll&api_key=8202723cf6cb67bb77efe3d7047e1ccdd449838540ec42671e44ead9f1ef91be&fields[]=conversion_cap&fields[]=payout_cap&fields[]=name&fields[]=description&fields[]=use_target_rules&fields[]=id&contain[1]=OfferCategory";
		//String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId=nativex&Target=Affiliate_Offer&Method=findAll&api_key=8202723cf6cb67bb77efe3d7047e1ccdd449838540ec42671e44ead9f1ef91be&fields[]=conversion_cap&fields[]=payout_cap&fields[]=name&fields[]=description&fields[]=use_target_rules&fields[]=id&contain[0]=OfferCategory&contain[1]=Thumbnail&contain[2]=Country";		
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+adProviderConfig.getNetworkId()+"&Target=Affiliate_Offer&Method=findAll&api_key="+adProviderConfig.getNetworkToken()+"&fields[]=conversion_cap&fields[]=payout_cap&fields[]=name&fields[]=description&fields[]=use_target_rules&fields[]=id&fields[]=currency&fields[]=preview_url&fields[]=default_payout&contain[0]=OfferCategory&contain[1]=Thumbnail&contain[2]=Country";

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
				offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
				OfferProviderCodeNames.HASOFFERS_NATIVEX+" requesting url: "+requestUrl);

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

    	//logger.info("=== respn ====");
    	//logger.info(reqResponse);
    	ArrayList<is.ejb.bl.offerProviders.hasoffersExt.getOffer.Offer> listPulledOffers = new ArrayList<is.ejb.bl.offerProviders.hasoffersExt.getOffer.Offer>();
    	ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers = new ArrayList<is.ejb.bl.offerWall.content.Offer>(); 

    	try {
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		//GetOffers obj = mapper.readValue(reqResponse, GetOffers.class);
    		JsonNode actualObj = mapper.readTree(reqResponse);
    		Iterator<JsonNode> it = actualObj.getElements();
    		while(it.hasNext()) {
    			JsonNode node = it.next();
    			//logger.info("-> node: "+node.toString());
    			Iterator<JsonNode> it1 = node.getElements();
        		while(it1.hasNext()) {
        			JsonNode node1 = it1.next();
        			//logger.info("--> "+node1.toString());

        			Iterator<JsonNode> it2 = node1.getElements();
            		while(it2.hasNext()) {
            			JsonNode node2 = it2.next();
            			String strOfferContent = node2.toString();
            			//logger.info("---> "+node2.toString());
            			if(strOfferContent.startsWith("{\"Offer\"")){
                			int startCatIndex = strOfferContent.indexOf("\"OfferCategory");
                			int endCatIndex = strOfferContent.lastIndexOf("Thumbnail");
                			String strOfferCategoryContent = strOfferContent.substring(startCatIndex, endCatIndex-1);
                			int startThumbIndex = endCatIndex-1;
                			int endThumbIndex = strOfferContent.lastIndexOf("Country")-2;
                			String strOfferThumbnailContent = strOfferContent.substring(startThumbIndex, endThumbIndex);
                			strOfferThumbnailContent = "{"+strOfferThumbnailContent+"}";
                			int startCountryIndex = endThumbIndex+2;
                			int endCountryIndex = strOfferContent.lastIndexOf("}");
                			String strOfferConuntryContent = strOfferContent.substring(startCountryIndex, endCountryIndex);
                			strOfferConuntryContent = "{"+strOfferConuntryContent+"}";

                			//device targeting
                			String identifiedTargetDevice = "";
                			boolean matchesTargetDevice = false;
                			if(strOfferCategoryContent.contains("Android") || 
                				strOfferCategoryContent.contains("android")) {
                				identifiedTargetDevice = "Android";
                			} else if(strOfferCategoryContent.contains("iOS") ||
                					strOfferCategoryContent.contains("IOS") ||
                					strOfferCategoryContent.contains("iphone") ||
                					strOfferCategoryContent.contains("ipod") ||
                    				strOfferCategoryContent.contains("ipad")) {
                    				identifiedTargetDevice = "iOS";
                    		}
                			
                			//logger.info("identified target device: "+identifiedTargetDevice);
                			if(offerWall.getTargetDevicesFilter().equals(identifiedTargetDevice)) {
                				matchesTargetDevice = true;
                			}
                			
                			boolean matchesTargetGeoLocation = false;
                			if(strOfferCategoryContent.contains(adProviderConfig.getCategoryName())
                    				&& strOfferConuntryContent.contains("\""+offerWall.getTargetCountriesFilter()+"\"")) {
                				matchesTargetGeoLocation = true;
                			}

                			//just to override GB
                			if(offerWall.getTargetCountriesFilter().equals("GB") &&
                					strOfferCategoryContent.contains(adProviderConfig.getCategoryName())
                    				&& strOfferConuntryContent.contains("\"UK\"")) {
                				matchesTargetGeoLocation = true;
                			}
                			//TODO override geo as it will be set based on category name in HO:
                			matchesTargetDevice = true;
                			
//                			//if(matchesTargetDevice) 
//                			if(matchesTargetGeoLocation)
//                			{
//                    			//logger.info("start index: "+startIndex+" end index:" +endIndex);
//                    			strOfferContent = strOfferContent.substring(0, startCatIndex-1)+"}";
//                    			logger.info("----> "+strOfferContent);
//                    			logger.info("C----> "+strOfferCategoryContent);
//                    			logger.info("T----> "+strOfferThumbnailContent);
//                    			logger.info("C----> "+strOfferConuntryContent);
//                			}
                			
                			if(matchesTargetDevice && matchesTargetGeoLocation) 
                			{
                    			//logger.info("start index: "+startIndex+" end index:" +endIndex);
                    			strOfferContent = strOfferContent.substring(0, startCatIndex-1)+"}";
//                    			logger.info("----> "+strOfferContent);
//                    			logger.info("C----> "+strOfferCategoryContent);
//                    			logger.info("T----> "+strOfferThumbnailContent);
//                    			logger.info("C----> "+strOfferConuntryContent);

                        		ObjectMapper mapperOffers = new ObjectMapper();
                    			//"OfferCategory":{"4":{"id":"4","name":"IOS"}}}},"
                        		mapperOffers = new ObjectMapper();
                        		GetOffer of = mapperOffers.readValue(strOfferContent, GetOffer.class);
                        		logger.info("O=====> "+of.getOffer().getName());
                        		GetThumbnail t = mapperOffers.readValue(strOfferThumbnailContent, GetThumbnail.class);
                        		of.getOffer().setThumbnail(t.getThumbnail());
                        		if(of.getOffer().getThumbnail() != null) {
                            		logger.info("T====> "+of.getOffer().getThumbnail().getUrl());
                        		}
                        		//add offer that matches geo and device targeting and is 
                        		if(!of.getOffer().getName().contains("Non-Incent")) {
                        			listPulledOffers.add(of.getOffer());
                        		} else {
                        			logger.info("-> removed non-incent offer: "+of.getOffer().getName());
                        		}
                			}
            			}
            		}
        		}
    		}

    		//------------------------------------------------------------------------------------------
    		//------------------------------ fill list of offers ---------------------------------------
            //report retrieved offer pool size
            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    				offerWall.getRealm().getId(), LogStatus.OK, 
    				Application.OFFER_POOL_SIZE+" "+
    				OfferProviderCodeNames.HASOFFERS_NATIVEX+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());

    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.HASOFFERS_NATIVEX+" total pulled offers: "+listPulledOffers.size());

    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.HASOFFERS_NATIVEX+
    						" no offers left to pick- possibly most were rejected");
    				break;
    			}
    			
    			int randomNumber = (int)(Math.random()*listPulledOffers.size());
    			is.ejb.bl.offerProviders.hasoffersExt.getOffer.Offer selectedOffer = listPulledOffers.get(randomNumber);
    			
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
            										selectedOffer.getDescription()+
            										numberOfOffersToSelect+"");
            	offerToAdd.setId(sha1Id);
            	offerToAdd.setTitle(selectedOffer.getName());
            	offerToAdd.setDescription(selectedOffer.getDescription());
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString());
            	offerToAdd.setPreviewUrl("");
            	offerToAdd.setUrl(getTrackingLink(offerWall, adProviderConfig, selectedOffer.getId()));
            	if(selectedOffer.getCurrency() == null) {
            		offerToAdd.setCurrency("USD");
            	} else {
                	offerToAdd.setCurrency(selectedOffer.getCurrency());
            	}
            	offerToAdd.setPayout(round(selectedOffer.getDefaultPayout(),2));
            	offerToAdd.setInternalNetworkId(offerWall.getRealm().getId());
            	offerToAdd.setCallToAction(""); //TODO check if call to action is available?
        		offerToAdd.setIncentivised(true); //assume its incentivised
        		
        		//apply geo filtering
				ArrayList<String> targetedCountries = new ArrayList<String>();  
				targetedCountries.add(offerWall.getTargetCountriesFilter());
				offerToAdd.setSupportedCountryCodes(targetedCountries);
				//supported devices
            	ArrayList<String> supportedDevices = new ArrayList<String>();
            	supportedDevices.add(offerWall.getTargetDevicesFilter());
            	offerToAdd.setSupportedTargetDevices(supportedDevices);
    			//request images
    			if(selectedOffer.getThumbnail() != null) {
    				HashMap<String,String> imagesMap = new HashMap<String,String>();
    				//imagesMap.put(ThumbnailQuality.hires.toString(), thumbnail.getUrl());
    				imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getThumbnail().getUrl());
    				imagesMap.put(ThumbnailQuality.Image+"-"+2, selectedOffer.getThumbnail().getThumbnail());
    				offerToAdd.setImage(imagesMap);
    			} else { //retrieve images via api call
    				offerToAdd.setImage(null);
    			}
            	
            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_NATIVEX);
    			if(isOfferAcceptedByGlobalFilter) {
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.HASOFFERS_NATIVEX, 
    							offerToAdd.getSupportedCountryCodes());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.HASOFFERS_NATIVEX, 
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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_NATIVEX);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_NATIVEX);
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
    						OfferProviderCodeNames.HASOFFERS_NATIVEX+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}

            
    	} catch(Exception exc) {
    		exc.printStackTrace();
            throw new Exception(OfferProviderCodeNames.HASOFFERS_NATIVEX+" Error: "+exc.toString()+" when retrieving/processing data from offer provider");
            //throw new Exception(OfferProviderCodeNames.HASOFFERS+" Error: "+exc.toString()+" when retrieving/processing data from offer provider"+reqResponse);
    	}
    	
        //update offer stats
        offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());

		//------------------------------------------------------------------------------------------
		//get all offers file info based on which we extrac thumbnail data
		getAllOffersFileInfo(offerWall, adProviderConfig.getNetworkId(), adProviderConfig.getNetworkToken(), listSelectedIndividualOffers);

        return listSelectedIndividualOffers;
	}

	public void getAllOffersFileInfo(OfferWallEntity offerWall, 
			String networkId, 
			String networkToken,
			ArrayList<is.ejb.bl.offerWall.content.Offer> listSelectedIndividualOffers) throws Exception { //if fails then particular offer generation fails as well 

		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Affiliate_OfferFile&Method=findAll&api_key="+networkToken;
		//String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Affiliate_Offer&Method=getThumbnail&api_key="+networkToken+"&ids[]="+selectedOfferId;
		logger.info("retrieving all offers file info: "+requestUrl);
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
    		//logger.info("image request: "+requestUrl);
    		//logger.info("image response: "+reqResponse);
            //serialise response into object
//    		ObjectMapper mapper = new ObjectMapper();
//    		GetOfferFileInfo obj = mapper.readValue(reqResponse, GetOfferFileInfo.class);
//    		List<DataEntry> listDataEntry = obj.getResponse().getData().getData();
//    		for(int i=0;i<listDataEntry.size();i++) {
//    			logger.info(i+" "+listDataEntry.get(i).getOfferFile().getUrl());
//    		}

            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		JsonNode actualObj = mapper.readTree(reqResponse);
    		Iterator<JsonNode> it = actualObj.getElements();
    		while(it.hasNext()) {
    			JsonNode node = it.next();
    			String nodeStr = node.toString();
    			if(nodeStr.length() > 100) {
        			//logger.info("-> node a: "+nodeStr.substring(0, 100));
    			} else {
    				//logger.info("-> node a: "+nodeStr.toString());
    			}
    			Iterator<JsonNode> it1 = node.getElements();
    			
        		while(it1.hasNext()) {
        			JsonNode node1 = it1.next();
        			nodeStr = node1.toString();
        			if(nodeStr.length() > 100) {
            			//logger.info("-> node b: "+nodeStr.substring(0, 100));
        			} else {
        				//logger.info("-> node b: "+nodeStr.toString());
        			}

        			Iterator<JsonNode> it2 = node1.getElements();
            		while(it2.hasNext()) {
            			JsonNode node2 = it2.next();
            			String strOfferContent = node2.toString();
            			nodeStr = node2.toString();
            			if(nodeStr.length() > 100) {
                			//logger.info("-> node c: "+nodeStr.substring(0, 100));
            			} else {
            				//logger.info("-> node c: "+nodeStr.toString());
            			}
            			
            			if(nodeStr.contains("OfferFile")) {
            				Iterator<JsonNode> it3 = node2.getElements();
                    		while(it3.hasNext()) {
                    			JsonNode node3 = it3.next();
                    			String offerStr = node3.toString();
                    			//logger.info("str: "+offerStr);
                    			try {
                        			if(offerStr !=null && offerStr.length() > 0) {
                        				int indexIdStart = offerStr.indexOf("offer_id");
                        				int indexIdEnd = offerStr.indexOf("display");
                        				String offerId = offerStr.substring(indexIdStart+11,indexIdEnd-3);
                        				
                        				int indexThStart = offerStr.indexOf("thumbnail");
                        				String thumbnailUrl = offerStr.substring(indexThStart+12);
                        				int indexThEnd = thumbnailUrl.indexOf("}");
                        				thumbnailUrl = thumbnailUrl.substring(0, indexThEnd-1);
                        				//logger.info(offerId+ " th: "+thumbnailUrl);
                        				
                        				//create OfferFile and fill with offerId and thumbnail data
                        				OfferFile of = new OfferFile();
                        				of.setOffer_id(Integer.parseInt(offerId));
                        				of.setThumbnail(thumbnailUrl);
                        				listOfferFileData.add(of);
                        			} 
                    			} catch(Exception exc){
                    				exc.printStackTrace();
                    			}
                    		}            				
            			}
                	}
        		}
    		}
    		
    		//set thumbnail urls for each offer that we selected to add to the wall
    		for(int i=0;i<listSelectedIndividualOffers.size();i++) {
    			is.ejb.bl.offerWall.content.Offer offerToAdd = listSelectedIndividualOffers.get(i); 
    			//logger.info("***** checking offer: "+offerToAdd.getSourceId());
    			for(int j=0;j<listOfferFileData.size();j++) {
    				OfferFile offerFileData = listOfferFileData.get(j);
    				if(offerToAdd.getSourceId().equals(String.valueOf(offerFileData.getOffer_id()))) {
    					//logger.info("*** identified matching id: "+offerFileData.getOffer_id());
    	    			if(offerFileData.getThumbnail() != null) {
    	    				HashMap<String,String> imagesMap = new HashMap<String,String>();
    	    				imagesMap.put(ThumbnailQuality.Image+"-"+1, offerFileData.getThumbnail());
    	    				//imagesMap.put(ThumbnailQuality.Image+"-"+2, selectedOffer.getThumbnail().getThumbnail());
    	    				offerToAdd.setImage(imagesMap);
    	    			} else { //retrieve images via api call
    	    				offerToAdd.setImage(null);
    	    			}
    	    			break;
    				}
    			}
    		}
    	} catch(Exception exc) {
    		logger.severe("Unable to retrieve all offers file info, error code: "+exc.toString()+" for offer provider: "+OfferProviderCodeNames.HASOFFERS_NATIVEX);
    	}
	}
	
	public String getTrackingLink(OfferWallEntity offerWall, 
			HasoffersNativexProviderConfig adProviderConfig, 
			int offerId) throws Exception {
		
		//add network token!
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+adProviderConfig.getNetworkId()+
				"&Target=Affiliate_Offer&Method=generateTrackingLink&api_key="+adProviderConfig.getNetworkToken()
				+"&offer_id="+offerId;
		logger.info("getting tracking link for offer with id: "+offerId+" using url: "+requestUrl);
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, 
				OfferProviderCodeNames.HASOFFERS_NATIVEX.toString());//+" rest method called, content: "+requestUrl);
		
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
         	//LogStatus.OK, OfferProviderCodeNames.HASOFFERS_NATIVEX+" rest response: "+reqResponse);

     		//serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetTrackingLink obj = mapper.readValue(reqResponse, GetTrackingLink.class);

    		if(obj!=null) {
    			return obj.getResponse().getData().getClick_url();
    		} else {
    			return "";
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.HASOFFERS_NATIVEX+
            		" Error: "+exc.toString()+" when retrieving/processing data from offer provider: "+reqResponse);
    	}
	}
	
	/*
	public Thumbnail requestImages(OfferWallEntity offerWall,
			int selectedOfferId,
			String offerName,
			String networkId, String networkToken, String methodCalled) throws Exception { //if fails then particular offer generation fails as well 

		Thumbnail thumbnail = null;
		//request images
		//String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Offer&Method=getThumbnail&NetworkToken="+networkToken+"&id="+selectedOfferId;
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Affiliate_Offer&Method=getThumbnail&api_key="+networkToken+"&ids[]="+selectedOfferId;
		logger.info("retrieving thumbnail: "+requestUrl);
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

    	//if thumbnail is not properly configured on HO or it doesn't exist - return null (offer will get rejected because it has no thumbnil)
    	try {
    		logger.info("image request: "+requestUrl);
    		logger.info("image response: "+reqResponse);
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetOfferThumbnail obj = mapper.readValue(reqResponse, GetOfferThumbnail.class);
    		thumbnail = obj.getResponse().getData().getThumbnail();
    	} catch(Exception exc) {
    		logger.severe("Unable to retrieve thumbnail, error code: "+exc.toString()+" for offer provider: "+OfferProviderCodeNames.HASOFFERS+" offer name: "+offerName+" with source id: "+selectedOfferId);
    		thumbnail = null;
    	}

    	return thumbnail;
	}
	*/
	
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

}
