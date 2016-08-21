package is.ejb.bl.testing;

import is.ejb.bl.reporting.ReportingManager;
import is.ejb.dl.entities.RealmEntity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

public class TestHashing {
	protected static final Logger logger = Logger.getLogger(TestHashing.class.getName());

	public static void main(String[] args) {
		new TestHashing();
	}
	
	public TestHashing() {
		//http://104.155.72.76:8080/ab/svc/v1/loginUser?deviceType=Android&miscData=&password=aaaaaa&phoneNumber=7897897897&phoneNumberExt=&systemInfo=Device name: Xamarin, Device model: Nexus 4 (KitKat), System name: REL, System version: 4.4.2, App version: 1.0.366&hashkey=5dd30e5227db4f293d8846b150ab3a2c3f66add8
    	//5dd30e5227db4f293d8846b150ab3a2c3f66add8

		String apiKey = "_AIrReWar8z";
		String urlRequestString = "http://104.155.72.76:8080/ab/svc/v1/loginUser?deviceType=Android&miscData=&password=aaaaaa&phoneNumber=7897897897&phoneNumberExt=&systemInfo=Device name: Xamarin, Device model: Nexus 4 (KitKat), System name: REL, System version: 4.4.2, App version: 1.0.366";
		//testRequestValid(apiKey, urlRequestString, "","","","");
		
		urlRequestString = "http://104.155.72.76:8080/ab/svc/v1/loginUser?deviceType=Android&miscData=&password=aaaaaa&phoneNumber=7897897897&phoneNumberExt=&systemInfo=Device name: Xamarin, Device model: Nexus 4 (KitKat), System name: REL, System version: 4.4.2, App version: 1.0.366ąęśćźńół&hashkey=9a5a468e67e36fb0618c3b29bea544b9e1c3235d";
		isRequestValid(apiKey, urlRequestString, "","","","");
	}

    private boolean isRequestValid(String apiKey,
    		String urlRequestString,
    		String phoneNumber,String phoneNumberExt, String systemInfo, String miscData) {

    	String retrievedHashKey=urlRequestString.substring(urlRequestString.indexOf("&hashkey")+9, urlRequestString.length());
    	String requestWithoutHashKey=urlRequestString.substring(0,urlRequestString.indexOf("&hashkey"));
    	
    	System.out.println("retrieved hash key: "+retrievedHashKey);

    	String requestUrlWithApiKey = requestWithoutHashKey+apiKey;
    	String sha1RequestUrl = DigestUtils.sha1Hex(requestUrlWithApiKey);
    	/*
    	byte[] sha1hash;
    	MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
	    	sha1hash = new byte[40];
			md.update(requestUrlWithApiKey.getBytes("UTF-8"), 0, requestUrlWithApiKey.length());
	    	sha1hash = md.digest();
	    	
	    	StringBuffer sb = new StringBuffer();
	    	for (int i = 0; i < sha1hash.length; i++) {
	    		sb.append(Integer.toString((sha1hash[i] & 0xff) + 0x100, 16).substring(1));
	    	}
	    	sha1RequestUrl = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
    	System.out.println("validating request with key: "+apiKey);
    	System.out.println("request without hash key:    "+requestWithoutHashKey);
    	System.out.println("request with hash key:       "+urlRequestString);
    	System.out.println("generating hash for request: "+requestUrlWithApiKey);
    	System.out.println("generated hash for request:  "+sha1RequestUrl);
    	System.out.println("expected  hash:              "+retrievedHashKey);
    	
    	if(urlRequestString != null && urlRequestString.length()>0){
    		return true;
    	} else {
        	return true;
    	}
    }

    private boolean testRequestValid(String apiKey,
    		String urlRequestString,
    		String phoneNumber,String phoneNumberExt, String systemInfo, String miscData) {

    	String requestUrlWithApiKey = urlRequestString;//+apiKey;
    	String sha1RequestUrl = DigestUtils.sha1Hex(requestUrlWithApiKey);
    	
    	byte[] sha1hash;
    	MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
	    	sha1hash = new byte[40];
			md.update(requestUrlWithApiKey.getBytes("iso-8859-1"), 0, requestUrlWithApiKey.length());
	    	sha1hash = md.digest();
	    	
	    	StringBuffer sb = new StringBuffer();
	    	for (int i = 0; i < sha1hash.length; i++) {
	    		sb.append(Integer.toString((sha1hash[i] & 0xff) + 0x100, 16).substring(1));
	    	}
	    	sha1RequestUrl = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("validating request with key: "+apiKey);
    	System.out.println("raw request content:         "+urlRequestString);
    	System.out.println("request content with apiKey: "+requestUrlWithApiKey);
    	System.out.println("generated hash: "+sha1RequestUrl);
    	System.out.println("expected  hash: b5ac14dce9b3e1e74ba3811864c7081b2037e862");
    	if(urlRequestString != null && urlRequestString.length()>0){
    		return true;
    	} else {
        	return true;
    	}
    }


}
