package is.ejb.bl.offerProviders.hasoffersVC;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.currencyCodes.CurrencyCodeConverter;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.GetOfferThumbnail;
import is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail.Thumbnail;
import is.ejb.bl.offerProviders.hasoffersExt.getOffer.GetOffer;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.DataEntry;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.GetOfferFileInfo;
import is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo.OfferFile;
import is.ejb.bl.offerProviders.hasoffersExt.getPayoutDetails.GetPayoutDetails;
import is.ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer.GetRuleTargetingForOffer;
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

public class HasoffersVCAPIManager {

	@Inject
	private Logger logger;
	private ArrayList<OfferFile> listOfferFileData = new ArrayList<OfferFile>();
	
	public ArrayList<Offer> findAllOffers(OfferWallEntity offerWall,
			IndividualOfferWall individualOfferWall,
			OfferFilterManager offerFilterManager,
			OfferRewardCalculationManager offerRewardCalculationManager,
			HasoffersVCProviderConfig adProviderConfig,
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
				OfferProviderCodeNames.HASOFFERS_VC+" requesting url: "+requestUrl);

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

    	logger.info("request url is: "+requestUrl);
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
        			
        			//{"Offer":{"id":"2053","description":"WilliamHill casino is world most trusted casino brand.<br><br>\r\n\r\n<strong>Converts on:<\/strong>Minimum Deposit<br><br>\r\n\r\n<strong>Conversion Flow:<\/strong><br>\r\n1. User Opens a New Account.<br>\r\n2. User Deposits USD\/Euro\/GBP 10.<br>\r\n3. Lead is Counted & Credited.<br><br>\r\n\r\n<strong>Allowed Media:<\/strong> Display Banners, Facebook PPC, Social Media, Contextual & Pop Traffic.<br><br>\r\n\r\n<strong>Disallowed Media:<\/strong> Text Mailer, Email\/Newsletter, PPC & Incent.<br><br>\r\n\r\n<strong>Special Instructions:<\/strong> <br>\r\n1. SEM & Brand Bidding is prohibited.<br>\r\n2. Promotion allowed only through the materials provided by Advertiser.<br>\r\n3. <font color=\"blue\">USA Not Allowed.<\/font><br><br>",
        			//"currency":"USD"}, 
        			//"OfferCategory":{"71":{"id":"71","name":"Gambling"}},
        			//"Thumbnail":{"id":"69975","offer_id":"2053","display":"williamhill.png","filename":"williamhill.png","size":"7849","status":"active","type":"offer thumbnail","width":"100","height":"50","code":null,"flash_vars":null,"interface":"network","account_id":null,"is_private":"0","created":"2015-08-20 02:35:19","modified":"0000-00-00 00:00:00","url":"http:\/\/media.vcommission.com\/brand\/files\/vcm\/2053\/williamhill.png","thumbnail":"http:\/\/media.vcommission.com\/brand\/files\/vcm\/2053\/thumbnails_100\/williamhill.png"}}

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

                			//logger.info("----> "+strOfferCategoryContent);
                			//logger.info("----> "+strOfferThumbnailContent);
                			//logger.info("----> "+strOfferConuntryContent);

                			//incent offer targeting
                			strOfferContent = strOfferContent.substring(0, startCatIndex-1)+"}";
                			logger.info("---> "+strOfferContent);
                			
                    		//process if offer is incent
                    		String allowedMediaStartStr = "Allowed Media:";
                    		String allowedMediaEndStr = "<br><br>";
                    		int allowedMediaStartIndex = strOfferContent.indexOf(allowedMediaStartStr);
                    		boolean isOfferIncent = false;
                    		String allowedMediaStr = "";
                    		
                    		try {
                        		allowedMediaStr = strOfferContent.substring(allowedMediaStartIndex);//error
                        		int allowedMediaEndIndex = allowedMediaStr.indexOf(allowedMediaEndStr);
                        		allowedMediaStr = allowedMediaStr.substring(0, allowedMediaEndIndex);
                        		isOfferIncent = false;
                        		if(allowedMediaStr != null && allowedMediaStr.length() > 0 &&
                        				allowedMediaStr.contains("Incent")) {
                        			isOfferIncent = true;
                        		}
                    		} catch(Exception exc) {
                    			logger.severe("Error when processing allowed media for vcommision: "+exc.toString());
                    			exc.printStackTrace();
                    		}

                    		if(isOfferIncent) {
                    			//device targeting
                    			//TODO need to read offer id in order to make the below method work
                    			//getOfferTargetingRule(offerWall,adProviderConfig.getNetworkId(), adProviderConfig.getNetworkToken(), offer.getOffer().getId());
                    			
                    			String identifiedTargetDevice = "";
                    			boolean matchesTargetDevice = false;
                        		String descStartStr = "description";
                        		String descEndStr = "<br><br>";
                        		int descStartIndex = strOfferContent.indexOf(descStartStr);
                        		String descStr = strOfferContent.substring(descStartIndex);
                        		int descEndIndex = descStr.indexOf(descEndStr);
                        		descStr = descStr.substring(0, descEndIndex);
                    			
                    			String androidApp = "Android App from";
                    			String androidApp2 = "via apk file";
                    			String iosApp = "iOS App from";
                    			
                    			if(descStr.toLowerCase().contains(androidApp.toLowerCase())) {
                    				identifiedTargetDevice = "Android";
                    			} else if(descStr.toLowerCase().contains(iosApp.toLowerCase())) {
                        				identifiedTargetDevice = "iOS";
                        		}
                    			
                    			if(offerWall.getTargetDevicesFilter().equals(identifiedTargetDevice)) {
                    				matchesTargetDevice = true;
                        			logger.info("identified target device: "+identifiedTargetDevice);
                    			} else {
                    				//logger.info("unable to identify target device: "+identifiedTargetDevice);
                    			}
                    			
                    			//VC offer provider works only for IN geo - assume all offers are from IN
                    			boolean matchesTargetGeoLocation = true;
                    			//if(strOfferCategoryContent.contains(adProviderConfig.getCategoryName())
                        		//		&& strOfferConuntryContent.contains("\""+offerWall.getTargetCountriesFilter()+"\"")) {
                    			//	matchesTargetGeoLocation = true;
                    			//}
                    			//just to override GB
                    			//if(offerWall.getTargetCountriesFilter().equals("GB") &&
                    			//		strOfferCategoryContent.contains(adProviderConfig.getCategoryName())
                    			//		&& strOfferConuntryContent.contains("\"UK\"")) {
                    			//	matchesTargetGeoLocation = true;
                    			//}
                    			
                    			if(matchesTargetDevice && matchesTargetGeoLocation) 
                    			{
                            		ObjectMapper mapperOffers = new ObjectMapper();
                        			//"OfferCategory":{"4":{"id":"4","name":"IOS"}}}},"
                            		mapperOffers = new ObjectMapper();
                            		GetOffer of = mapperOffers.readValue(strOfferContent, GetOffer.class);
                        			
                            		logger.info("======>OK allowed media: "+allowedMediaStr);
                            		logger.info("O=====> "+of.getOffer().getId()+" "+of.getOffer().getName());
                            		GetThumbnail t = mapperOffers.readValue(strOfferThumbnailContent, GetThumbnail.class);
                            		of.getOffer().setThumbnail(t.getThumbnail());
                            		if(of.getOffer().getThumbnail() != null) {
                                		logger.info("T=====> "+of.getOffer().getThumbnail().getUrl());
                            		} 
                            		//set currency to INR for all offers
                            		of.getOffer().setCurrency("INR");
                            		
                            		//get payout value
                            		of.getOffer().setDefaultPayout(getOfferPayout(offerWall,adProviderConfig.getNetworkId(), adProviderConfig.getNetworkToken(), of.getOffer().getId()));
                            		
                            		//remove description part that is for publisher only
                            		descEndStr = "<br><br>";
                            		String desc = of.getOffer().getDescription();
                            		desc = desc.substring(0, desc.indexOf(descEndStr));
                            		of.getOffer().setDescription(desc);
                            		
                            		//add offer to the list
                        			listPulledOffers.add(of.getOffer());
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
    				OfferProviderCodeNames.HASOFFERS_VC+" offer pool size: "+ listPulledOffers.size());//+" rest method called, content: "+requestUrl);
            //update offer stats
            offerWall.setNumberOfOffersInSelectionPool(offerWall.getNumberOfOffersInSelectionPool()+listPulledOffers.size());
            individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_offers_in_selection_pool+":"+listPulledOffers.size());

    		//generate offer wall
    		if(listPulledOffers.size() < numberOfOffersToSelect) {
    			numberOfOffersToSelect = listPulledOffers.size();
    		} 
    		
    		logger.info(OfferProviderCodeNames.HASOFFERS_VC+" total pulled offers: "+listPulledOffers.size());

    		//pick offers randomly and make sure they do not repeat in the offer wall
    		while(listSelectedIndividualOffers.size() < (numberOfOffersToSelect)) {
    			if(listPulledOffers.size() == 0) {
    				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
    						offerWall.getRealm().getId(), 
    						LogStatus.WARNING, 
    						Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT+" "+OfferProviderCodeNames.HASOFFERS_VC+
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
            	offerToAdd.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS_VC.toString());
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
    				logger.info("!!!!!!!!!!!!!!: "+selectedOffer.getThumbnail().getUrl());
    				HashMap<String,String> imagesMap = new HashMap<String,String>();
    				imagesMap.put(ThumbnailQuality.Image+"-"+1, selectedOffer.getThumbnail().getUrl());
    				//imagesMap.put(ThumbnailQuality.Image+"-"+2, selectedOffer.getThumbnail().getThumbnail());
    				offerToAdd.setImage(imagesMap);
    			} else { //retrieve images via api call
    				offerToAdd.setImage(null);
    			}
            	
            	//filters
            	boolean isOfferAcceptedByGlobalFilter = false;
    			boolean isOfferAcceptedByGeoFilter = false;
    			boolean isOfferAcceptedByDeviceFilter = false;

    			isOfferAcceptedByGlobalFilter = offerFilterManager.isOfferAcceptedBasedOnGlobalFilters(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_VC);
    			if(isOfferAcceptedByGlobalFilter) {
    				isOfferAcceptedByGeoFilter = offerFilterManager.isOfferAcceptedBasedOnGeoFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.HASOFFERS_VC, 
    							offerToAdd.getSupportedCountryCodes());
    				if(isOfferAcceptedByGeoFilter) {
    					//apply device filtering
    					isOfferAcceptedByDeviceFilter = offerFilterManager.isOfferAcceptedBasedOnTargetPlatformFilter(offerToAdd, 
    							offerWall, 
    							OfferProviderCodeNames.HASOFFERS_VC, 
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
        				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_VC);
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
    				offerToAdd = offerRewardCalculationManager.calculateOfferReward(offerToAdd, offerWall, OfferProviderCodeNames.HASOFFERS_VC);
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
    						OfferProviderCodeNames.HASOFFERS_VC+" successfully created offer "
    								+offerToAdd.getTitle()+" offerId: "+offerToAdd.getId());
    			} else { //if below threshold - remove that offer id from the pool of offer ids as well
    				listPulledOffers.remove(randomNumber); //also remove offer id from the pool as it is below payoff threshold
    			}
    		}

            
    	} catch(Exception exc) {
    		exc.printStackTrace();
            throw new Exception(OfferProviderCodeNames.HASOFFERS_VC+" Error: "+exc.toString()+" when retrieving/processing data from offer provider");
            //throw new Exception(OfferProviderCodeNames.HASOFFERS+" Error: "+exc.toString()+" when retrieving/processing data from offer provider"+reqResponse);
    	}
    	
        //update offer stats
        offerWall.setNumberOfGeneratedOffers(offerWall.getNumberOfGeneratedOffers()+listSelectedIndividualOffers.size());
        individualOfferWall.getListOfferStats().add(OfferWallStats.number_of_generated_offers+":"+listSelectedIndividualOffers.size());

        return listSelectedIndividualOffers;
	}

	public double getOfferPayout(OfferWallEntity offerWall,
			String networkId, 
			String networkToken,
			int offerId) throws Exception { //if fails then particular offer generation fails as well 

		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Affiliate_Offer&Method=getPayoutDetails&api_key="+networkToken+"&offer_id="+offerId;
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
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetPayoutDetails obj = mapper.readValue(reqResponse, GetPayoutDetails.class);
    		double payoutValue = obj.getResponse().getData().getOffer_payout().getPayout();
			logger.info("payout: "+payoutValue);
			return payoutValue;
    	} catch(Exception exc) {
    		logger.severe("Unable to retrieve offer payout, error code: "+exc.toString()+" for offer provider: "+OfferProviderCodeNames.HASOFFERS_VC+" offer id: "+offerId);
    		return -1;
    	}
	}

	public void getOfferTargetingRule(OfferWallEntity offerWall,
			String networkId, 
			String networkToken,
			int offerId) throws Exception { //if fails then particular offer generation fails as well 

		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+networkId+"&Target=Affiliate_Offer&Method=getRuleTargetingForOffer&api_key="+networkToken+"&offer_id="+offerId;
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
            //serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetRuleTargetingForOffer obj = mapper.readValue(reqResponse, GetRuleTargetingForOffer.class);
    		List<is.ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer.DataEntry> listRules = obj.getResponse().getData();
			logger.info("list size: "+listRules.size());
    	} catch(Exception exc) {
    		logger.severe("Unable to retrieve offer payout, error code: "+exc.toString()+" for offer provider: "+OfferProviderCodeNames.HASOFFERS_VC+" offer id: "+offerId);
    	}
	}

	public String getTrackingLink(OfferWallEntity offerWall, 
			HasoffersVCProviderConfig adProviderConfig, 
			int offerId) throws Exception {
		
		//add network token!
		String requestUrl = "https://api.hasoffers.com/Apiv3/json?NetworkId="+adProviderConfig.getNetworkId()+
				"&Target=Affiliate_Offer&Method=generateTrackingLink&api_key="+adProviderConfig.getNetworkToken()
				+"&offer_id="+offerId;
		logger.info("getting tracking link for offer with id: "+offerId+" using url: "+requestUrl);
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, 
				OfferProviderCodeNames.HASOFFERS_VC.toString());//+" rest method called, content: "+requestUrl);
		
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
         	//LogStatus.OK, OfferProviderCodeNames.HASOFFERS_VC+" rest response: "+reqResponse);

     		//serialise response into object
    		ObjectMapper mapper = new ObjectMapper();
    		GetTrackingLink obj = mapper.readValue(reqResponse, GetTrackingLink.class);

    		if(obj!=null) {
    			return obj.getResponse().getData().getClick_url();
    		} else {
    			return "";
    		}
    	} catch(Exception exc) {
            throw new Exception(OfferProviderCodeNames.HASOFFERS_VC+
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
