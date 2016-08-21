package is.ejb.bl.offerProviders.snapdeal;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.CategoryOffers;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


public class GetCategoriesTest {

//	public static void main(String[] args) throws IOException {
//		// TODO Auto-generated method stub
//		new GetCategoriesTest();
//	}
	
	public GetCategoriesTest() throws IOException {
		getCategories();
	}
	public ArrayList<String> getCategories() throws IOException {
		System.out.println("Getting categories...");
		ArrayList<String> categoryUrlList = new ArrayList<String>();
	      //persist data in offer wall (it contains category info)
        String requestUrl = "http://affiliate-feeds.snapdeal.com/feed/88743.json";
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
        String reqResponse = "";
        
		System.out.println("requesting url: "+requestUrl);
    	try {
    		URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(10100);
            urlConnection.setReadTimeout(10100);
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
    	
    	System.out.println(reqResponse);
        //serialise response into object
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(reqResponse);
		Iterator<JsonNode> it = actualObj.getElements();
		while(it.hasNext()) {
			JsonNode node = it.next();
			System.out.println("-> node: "+node.toString());
			Iterator<JsonNode> it1 = node.getElements();
    		while(it1.hasNext()) {
    			JsonNode node1 = it1.next();
    			System.out.println("--> "+node1.toString());
    			Iterator<JsonNode> it2 = node1.getElements();
        		while(it2.hasNext()) {
        			JsonNode node2 = it2.next();
        			System.out.println("---> "+node2.toString());
        			Iterator<JsonNode> it3 = node2.getElements();
            		while(it3.hasNext()) {
            			JsonNode node3 = it3.next();
            			System.out.println("----> "+node3.toString());
            			String categoryFeedUrl = node3.toString();
            			categoryFeedUrl = categoryFeedUrl.substring(categoryFeedUrl.indexOf("get")+6, categoryFeedUrl.length()-4);
            			System.out.println("-----> url: "+categoryFeedUrl);
            			categoryUrlList.add(categoryFeedUrl);
            		}
        		}
    		}
		}
		
		String categoryOffersOutputInJson = getCategoryOffersUsingPost(categoryUrlList.get(0), "700e338e91da7397893a497b6c0f45", "88743");
		ObjectMapper mapperForOffers = new ObjectMapper();
		CategoryOffers categoryOffersHolder = mapperForOffers.readValue(categoryOffersOutputInJson, CategoryOffers.class);
		System.out.println("out: "+categoryOffersHolder.getProducts().size());
		List<ProductsEntry> listOffers = categoryOffersHolder.getProducts();
		for(int i=0;i<listOffers.size();i++) {
			ProductsEntry product = listOffers.get(i);
			System.out.println(product.getAvailability()+" "+
					product.getBrand()+" "+
					product.getCategoryName()+" "+
					product.getImageLink()+" "+
					product.getSubCategoryName()+" "+
					product.getTitle()+" "+
					product.getCategoryId()+" "+
					product.getCategoryId()+" "+
					product.getEffectivePrice()+" "+
					product.getOfferPrice()+" "+
					product.getSubCategoryId()+" "+
					product.getMrp()+" "+
					product.getOfferPrice()+" "+
					product.getDescription()

			);
			
			
		}
		
		//produce list of category names and store in db rows each category + json content separately.
		
		return categoryUrlList;
	}

	public String getCategoryOffersUsingPost(String categoryUrl, String token, String id) throws IOException {
		CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        String categoryOffersOutputInJson = null;
        
        try {
        	System.out.println("Making a url request for: "+categoryUrl);
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(categoryUrl);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            //nvps.add(new BasicNameValuePair("content-type", "application/json"));
            nvps.add(new BasicNameValuePair("Snapdeal-Affiliate-Id", id));
            nvps.add(new BasicNameValuePair("Snapdeal-Token-Id", token));

             //StringEntity input = new StringEntity("{\"username\": \"dummyuser\",\"password\": \"dummypassword\"}");
             //input.setContentType("application/json");
             //httpPost.setEntity(input);

            for (NameValuePair h : nvps)
            {
                httpPost.addHeader(h.getName(), h.getValue());
            }

            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            System.out.println("Output from Server .... \n");
            String output = null;

			String inputLine;
			StringBuffer resp = new StringBuffer();

			while ((inputLine = br.readLine()) != null) {
				resp.append(inputLine);
			}
			categoryOffersOutputInJson = resp.toString();
			/*
            while ((output = br.readLine()) != null) {
            	if(output != null) {
            		categoryOffersOutputInJson = output;
                    System.out.println(output);
            	}
            }
            */
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            try{
                response.close();
                httpClient.close();
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return categoryOffersOutputInJson;
	}

	public void getCategoryOffers(String categoryUrl, String token, String id) throws IOException {
		try {
			URL obj = new URL(categoryUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(11000);
			con.setReadTimeout(11000);
			// add reuqest header
			con.setRequestMethod("POST");
			String urlParameters = "Snapdeal-Affiliate-Id=" + id
					+ "&Snapdeal-Token-Id:=" + token;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			String responseString = "OK";
			String statusMessage = responseString;

			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				responseString = response.toString();
				// code 200 and status 0 - request to credit user received
				// successfully
				// code 200 and status 1 - request with similar transaction id
				// already exists
				// code 403 - authentication failure
				System.out.println("Response: "+responseString);
			}			
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	

}
