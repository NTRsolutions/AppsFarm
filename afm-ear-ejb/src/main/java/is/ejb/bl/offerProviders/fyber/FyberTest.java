package is.ejb.bl.offerProviders.fyber;

import org.apache.commons.codec.digest.DigestUtils;

public class FyberTest {

	public static void main(String[] args) {
	
		//http://api.fyber.com/feed/v1/offers.json?appid=[APP_ID]&uid=[USER_ID]
		//&ip=[IP_ADDRESS]&locale=[LOCALE]&device_id=[DEVICE_ID]&ps_time=[TIMESTAMP]
		//&pub0=[CUSTOM]&timestamp=[UNIX_TIMESTAMP]&offer_types=[OFFER_TYPES]
		//&google_ad_id=[GAID]&google_ad_id_limited_tracking_enabled=[GAID ENABLED]&hashkey=[HASHKEY]
		long unixTime = System.currentTimeMillis() / 1000L;
		
		String baseAddress = "http://api.fyber.com/feed/v1/offers.json?";
		String address = "";
		address += "appid=47543";
		
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
		
		baseAddress += address + "&hashkey="+hash;
		System.out.println(baseAddress);
		
	}

}
