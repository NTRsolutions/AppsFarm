package is.ejb.bl.offerProviders.fyber;

import is.ejb.bl.business.Application;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.GetOffers;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.OffersEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroupOfferIds.FindAllOfferGroupOfferIds;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.DataEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.FindAllOfferGroups;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferById.Data;
import is.ejb.bl.offerProviders.hasoffers.serde.findOfferById.FindOfferById;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.OfferWallEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
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
										String appId, String apiKey, String offerTypes, String offerProviderCodeName) throws Exception {
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
		
		if(offerTypes != null && offerTypes.length()>0) {
			requestUrl = "appid="+appId+"&device_id=111&locale=EN&offer_types="+offerTypes+"&timestamp="+unixTime+"&uid=testUser";
			requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=EN&offer_types="+offerTypes+"&timestamp="+unixTime+"&uid=testUser&"+apiKey;
		} else {
			requestUrl = "appid="+appId+"&device_id=111&locale=EN&timestamp="+unixTime+"&uid=testUser";
			requestUrlWithApiKey = "appid="+appId+"&device_id=111&locale=EN&timestamp="+unixTime+"&uid=testUser&"+apiKey;
		}

		//http://developer.fyber.com/content/android/offer-wall/offer-api/
        String sha1RequestUrl = DigestUtils.sha1Hex(requestUrlWithApiKey); 
        //System.out.println("sha1: "+sha1RequestUrl);
        requestUrl = "http://api.sponsorpay.com/feed/v1/offers.json?&"+requestUrl+"&hashkey="+sha1RequestUrl;
        //System.out.println("full req: "+requestUrl);
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "FYBER getOffers rest method called, content: "+requestUrl);
		
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

        Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "FYBER rest response: "+reqResponse);
        System.out.println("resp: "+reqResponse);
        //serialise response into object
		ObjectMapper mapper = new ObjectMapper();
		GetOffers obj = mapper.readValue(reqResponse, GetOffers.class);

		List<OffersEntry> listOffers = obj.getOffers();
        Iterator it = listOffers.iterator();
        while(it.hasNext()) {
        	OffersEntry offer = (OffersEntry)it.next();
        	
        	Offer foundOffer = new Offer();
        	foundOffer.setTitle(offer.getTitle());
        	//generate unique offer id used by our system to track offer conversion
        	String sha1Id = DigestUtils.sha1Hex(offerWall.getRealm().getId()+
        										offerWall.getNumberOfOffers()+
        										Math.random()*100000+
        										System.currentTimeMillis()+
        										offer.getTitle()+"");
        	foundOffer.setId(sha1Id);
        	foundOffer.setSourceId(offer.getOffer_id()+"");
        	foundOffer.setAdProviderCodeName(offerProviderCodeName);
        	foundOffer.setUrl(offer.getLink());
        	foundOffer.setPreviewUrl(offer.getLink());
        	foundOffer.setImage(offer.getThumbnail());
        	foundOffer.setPayout(offer.getPayout());
			foundOffer.setInternalNetworkId(offerWall.getRealm().getId());

        	listFoundOffers.add(foundOffer);
        	
        	System.out.println("");
        	System.out.println("offer id: "+offer.getOffer_id());
        	System.out.println("offer title: "+offer.getTitle());
        	System.out.println("offer link: "+offer.getLink());
        	System.out.println("offer teaser: "+offer.getTeaser());
        	System.out.println("offer payout: "+offer.getPayout());
        	System.out.println("offer thumbnail: "+offer.getThumbnail().toString());
        	
        }
		return listFoundOffers;
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
}
