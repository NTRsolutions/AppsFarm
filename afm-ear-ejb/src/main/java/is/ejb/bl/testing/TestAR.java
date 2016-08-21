package is.ejb.bl.testing;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

public class TestAR {

	public static void main(String[] args) throws Exception {
		new TestAR();
	}
	
	public TestAR() throws Exception {
		//testCOWRegenerationNotification();
		//testConversionNotification();
		testRewardNotification();
	}
	
	public void testCOWRegenerationNotification() throws Exception {
		String urlString = "http://188.226.242.215/server/api/updateofferwalls";
		
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
    	try {
			URL url = new URL(urlString);
	        urlConnection = (HttpURLConnection)url.openConnection();
	        urlConnection.setConnectTimeout(10*1000);
	        urlConnection.setReadTimeout(10*1000);
			//add reuqest header
	        urlConnection.setRequestMethod("POST");
			String urlParameters = "";
		
			// Send post request
			urlConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = urlConnection.getResponseCode();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

	        
	        in = new BufferedReader(
	                                new InputStreamReader(
	                                urlConnection.getInputStream()));
	        String reqResponse = "";
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

	}
	
	public void testConversionNotification() throws Exception {
		String urlString = "http://188.226.242.215/server/api/registerconversion";
		
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
    	try {
			URL url = new URL(urlString);
	        urlConnection = (HttpURLConnection)url.openConnection();
	        urlConnection.setConnectTimeout(10*1000);
	        urlConnection.setReadTimeout(10*1000);
			//add reuqest header
	        urlConnection.setRequestMethod("POST");
	        urlConnection.setRequestProperty("Content-Type","application/json");   
			//String urlParameters = "{\"phoneNumber\":\"4433221100\", \"providerCodeName\":\"HASOFFERS\", \"sourceId\":\"134\"}";
			String urlParameters = "{\"phoneNumber\":\"4433221100\", "
					+ "\"providerCodeName\":\"HASOFFERS\", "
					+ "\"sourceId\":\"134\", "
					+ "\"transactionId\":\"abcdef\"}";
			// Send post request
			urlConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = urlConnection.getResponseCode();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);
	        
	        in = new BufferedReader(
	                                new InputStreamReader(
	                                urlConnection.getInputStream()));
	        String reqResponse = "";
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
	}

	public void testRewardNotification() throws Exception {
		String urlString = "http://188.226.242.215/server/api/notifyreward";
		
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
    	try {
			URL url = new URL(urlString);
	        urlConnection = (HttpURLConnection)url.openConnection();
	        urlConnection.setConnectTimeout(10*1000);
	        urlConnection.setReadTimeout(10*1000);
			//add reuqest header
	        urlConnection.setRequestMethod("POST");
	        urlConnection.setRequestProperty("Content-Type","application/json");
	        
	        //String urlParameters = "{\"phoneNumber\":\"0786641885\", \"applicationName\":\"Castle Clash - Android- Kenya\", \"rewardStatus\":\"SUCCESS\", \"rewardMessage\":\"OK\", \"transactionId\":\"20c598fc61072d6756453c2084edcb0534203b86\"}";
			String urlParameters = "{\"phoneNumber\":\"1959562100\", "
					+ "\"applicationName\":\"Castle Clash - Android- Kenya\", "
					+ "\"rewardStatus\":\"SUCCESS\", "
					+ "\"rewardMessage\":\"OK\", "
					+ "\"transactionId\":\"20c598fc61072d6756453c2084edcb0534203b86\"}";
			// Send post request
			urlConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = urlConnection.getResponseCode();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);
	        
	        in = new BufferedReader(
	                                new InputStreamReader(
	                                urlConnection.getInputStream()));
	        String reqResponse = "";
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
	}

}
