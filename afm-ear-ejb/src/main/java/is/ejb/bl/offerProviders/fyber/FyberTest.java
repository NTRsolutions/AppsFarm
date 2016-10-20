package is.ejb.bl.offerProviders.fyber;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.GetOffers;
import is.ejb.bl.offerProviders.fyber.serde.getOffers.OffersEntry;
import is.ejb.bl.offerProviders.supersonic.getOffers.SupersonicGetOffers;
import is.ejb.bl.system.logging.LogStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class FyberTest {

	public static void main(String[] args) {
		new FyberTest();
	}
	
	public FyberTest(String fake) {
	}
	
	public FyberTest() {
		String baseAddress = getFyberTestUrl();
		System.out.println(baseAddress);
		//make request, get json content and convert to raw response
		convertToObjects(baseAddress);
	}
	public String getFyberTestUrl() {
		//http://api.fyber.com/feed/v1/offers.json?appid=[APP_ID]&uid=[USER_ID]
		//&ip=[IP_ADDRESS]&locale=[LOCALE]&device_id=[DEVICE_ID]&ps_time=[TIMESTAMP]
		//&pub0=[CUSTOM]&timestamp=[UNIX_TIMESTAMP]&offer_types=[OFFER_TYPES]
		//&google_ad_id=[GAID]&google_ad_id_limited_tracking_enabled=[GAID ENABLED]&hashkey=[HASHKEY]
		long unixTime = System.currentTimeMillis() / 1000L;
		
		String baseAddress = "http://api.fyber.com/feed/v1/offers.json?";
		String address = "";
		address += "appid=47543"; //app id in config
		
		address += "&google_ad_id=0da9523d-89f9-4b91-8563-cf7918f036ec";
		address += "&google_ad_id_limited_tracking_enabled=false";
		//address += "&ip=212.45.111.17";
		address += "&locale=en";
		address += "&os_version=6.0.1";
		//address += "&page=1";
		//address += "&pub0=test";
		address += "&timestamp="+unixTime;
		address += "&uid=23335";
		
		
		String copyAddress = new String(address);
		
		copyAddress += "&fa943c53f6ee72d53015fb918f916dc1a1043b9a";
		System.out.println(copyAddress);
		String hash = DigestUtils.sha1Hex(copyAddress);
		System.out.println(hash);
		
		baseAddress += address + "&hashkey="+hash; //api key in config

		return baseAddress;
	}
	
	public static void convertToObjects(String requestUrl){
		try {
	    	HttpURLConnection urlConnection = null;
	    	BufferedReader in = null;
	        String reqResponse = "";

	    	try {
	    		URL url = new URL(requestUrl);
	            urlConnection = (HttpURLConnection)url.openConnection();
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
	    	
	    	System.out.println("got raw response: "+reqResponse);
	    	
	        //serialise response into object
			ObjectMapper mapper = new ObjectMapper();
			GetOffers offersHolder = mapper.readValue(reqResponse, GetOffers.class);
			List<OffersEntry> listOffers = offersHolder.getOffers();
			for(int i=0;i<listOffers.size();i++) {
				OffersEntry offer = listOffers.get(i);
				System.out.println("====================================");
				System.out.println("Link: "+offer.getLink());
				System.out.println("Required actions: "+offer.getRequired_actions());
				System.out.println("Teaser: "+offer.getTeaser());
				System.out.println("Title: "+offer.getTitle());
				System.out.println("Payout (int): "+offer.getPayout());
				System.out.println("Time to payout: "+offer.getTime_to_payout().getAmount());
				System.out.println("Thumbnail(HiRes): "+offer.getThumbnail().getHires());
				System.out.println("Thumbnail(LowRes): "+offer.getThumbnail().getLowres());
				System.out.println("OfferTypes: "+offer.getOffer_types().toString());
			}
			
			

		} catch(Exception exc){ 
			exc.printStackTrace();
		}
	}

}
