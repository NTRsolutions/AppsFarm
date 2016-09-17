package is.ejb.bl.system.security;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.*;

@Stateless
public class HashValidationManager {

	@Inject
	private Logger logger;
	
	@Inject
	private DAORealm daoRealm;
	
	
	public boolean isAPIRequestValid(HashMap<String, Object> parameters) {
		logger.info("Validating hash map with " + parameters.size() + " parameters");
		String hashCodeFromRequest = (String) parameters.remove("hashCode");
		System.out.println(parameters);
		int hashCode = parameters.hashCode();
		RealmEntity realm = getRealmWithId(4);
		String generatedHash = DigestUtils.sha1Hex(hashCode + realm.getApiKey());
		logger.info("Hash code from request: " + hashCodeFromRequest + " generated: " + hashCode);
		if (hashCodeFromRequest.equals(generatedHash)) {
			return true;
		} else {
			return false;
		}
	}
	/*
	 * object should not contains "hashKey" value, so it should be null!
	 */
	public boolean isPostRequestValid(String apiKey,String receivedHash, Object object){
		ObjectMapper mapper = new ObjectMapper();
		String sortedJson;
		try {
			sortedJson = mapper.writeValueAsString(object);
			mapper.setSerializationInclusion(Include.NON_NULL);
			String hash = DigestUtils.sha1Hex(sortedJson+apiKey);
			logger.info(sortedJson+apiKey);
			logger.info(hash);
			if (receivedHash != null && receivedHash.equals(hash))
				return true;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		return false;
		
	}
	
	public RealmEntity getRealmWithId(int id){
		RealmEntity realm = null;
		try{
			realm = daoRealm.findById(id);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		return realm;
	}

    public boolean isRequestValid(String apiKey,
    		String retrievedHashKey,
    		String urlRequestString,
    		String phoneNumber,String phoneNumberExt, 
    		String systemInfo, String miscData, String ipAddress) throws UnsupportedEncodingException {

		if(retrievedHashKey != null && retrievedHashKey.length() > 0) {
	    	urlRequestString = java.net.URLDecoder.decode(urlRequestString, "UTF-8");
	    	String requestWithoutHashKey=urlRequestString.substring(0,urlRequestString.indexOf("&hashkey"));
	    	String requestUrlWithApiKey = requestWithoutHashKey+apiKey+phoneNumber;
	    	String generatedHashKey = DigestUtils.sha1Hex(requestUrlWithApiKey);

	    	Application.getElasticSearchLogger().indexLog(Application.HASH_VALIDATION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.HASH_VALIDATION_ACTIVITY+
					" validating request with key: "+apiKey+
					" request without hash key: "+requestWithoutHashKey+
					" request with hash key: "+urlRequestString+
					" generating hash for request: "+requestUrlWithApiKey+
					" generated hash for request: "+generatedHashKey+
					" expected  hash: "+retrievedHashKey
					);

	    	if(urlRequestString != null && urlRequestString.length()> 0 && generatedHashKey.equals(retrievedHashKey)) {
				Application.getElasticSearchLogger().indexLog(Application.HASH_VALIDATION_ACTIVITY, -1, 
						LogStatus.OK, 
						Application.HASH_VALIDATION_SUCCESSFUL+
						" for user with phone: "+phoneNumber+
						" ext: "+phoneNumberExt+
						" systemInfo: "+systemInfo+
						" miscData: "+miscData+
						" ip: "+ipAddress+
						" url: "+urlRequestString);

	    		return true;
	    	} else {
	        	return false;
	    	}
		} else {
			Application.getElasticSearchLogger().indexLog(Application.HASH_VALIDATION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.HASH_VALIDATION_ABORTED+
					" aborthing hash validation for: "+urlRequestString+
					" ip address: "+ipAddress+
					" obtained hashkey from url: "+retrievedHashKey);
			return true;
		}
    	
    }

    public boolean isRequestValida(String apiKey,
    		String urlRequestString,
    		String phoneNumber,String phoneNumberExt, String systemInfo, String miscData) throws UnsupportedEncodingException {

    	urlRequestString = java.net.URLDecoder.decode(urlRequestString, "UTF-8");
    	String retrievedHashKey=urlRequestString.substring(urlRequestString.indexOf("&hashkey")+9, urlRequestString.length());
    	String requestWithoutHashKey=urlRequestString.substring(0,urlRequestString.indexOf("&hashkey"));
    	//System.out.println("retrieved hash key: "+retrievedHashKey);

    	String requestUrlWithApiKey = requestWithoutHashKey+apiKey+phoneNumber;
    	String generatedHashKey = DigestUtils.sha1Hex(requestUrlWithApiKey);

    	Application.getElasticSearchLogger().indexLog(Application.HASH_VALIDATION_ACTIVITY, -1, 
				LogStatus.OK, 
				Application.HASH_VALIDATION_ACTIVITY+
				" validating request with key: "+apiKey+
				" request without hash key: "+requestWithoutHashKey+
				" request with hash key: "+urlRequestString+
				" generating hash for request: "+requestUrlWithApiKey+
				" generated hash for request: "+generatedHashKey+
				" expected  hash: "+retrievedHashKey
				);

    	if(urlRequestString != null && urlRequestString.length()>0 &&
    			generatedHashKey.equals(retrievedHashKey)){
    		return true;
    	} else {
        	return false;
    	}
    }

	public String getFullURL(HttpServletRequest request) {
	    StringBuffer requestURL = request.getRequestURL();
	    String queryString = request.getQueryString();

	    if (queryString == null) {
	        return requestURL.toString();
	    } else {
	        return requestURL.append('?').append(queryString).toString();
	    }
	}

    /*
    private boolean isRequestValid(String apiKey,
    		String urlRequestString,
    		String phoneNumber,String phoneNumberExt, String systemInfo, String miscData) throws UnsupportedEncodingException {

    	urlRequestString = java.net.URLDecoder.decode(urlRequestString, "UTF-8");
    	String retrievedHashKey=urlRequestString.substring(urlRequestString.indexOf("&hashkey")+9, urlRequestString.length());
    	String requestWithoutHashKey=urlRequestString.substring(0,urlRequestString.indexOf("&hashkey"));
    	
    	//System.out.println("retrieved hash key: "+retrievedHashKey);

    	String requestUrlWithApiKey = requestWithoutHashKey+apiKey+phoneNumber;
    	String generatedHashKey = DigestUtils.sha1Hex(requestUrlWithApiKey);

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

		Application.getElasticSearchLogger().indexLog(Application.HASH_VALIDATION_ACTIVITY, -1, 
				LogStatus.OK, 
				Application.HASH_VALIDATION_ACTIVITY+
				" validating request with key: "+apiKey+
				" request without hash key: "+requestWithoutHashKey+
				" request with hash key: "+urlRequestString+
				" generating hash for request: "+requestUrlWithApiKey+
				" generated hash for request: "+generatedHashKey+
				" expected  hash: "+retrievedHashKey
				);

    	if(urlRequestString != null && urlRequestString.length()>0 &&
    			generatedHashKey.equals(retrievedHashKey)){
    		return true;
    	} else {
        	return false;
    	}
    }
	*/
}
