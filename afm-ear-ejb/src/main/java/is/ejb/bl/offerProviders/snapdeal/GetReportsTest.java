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


public class GetReportsTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new GetReportsTest();
	}
	
	public GetReportsTest() throws IOException {

		//getReports("approved", "2016-05-01", "2016-05-17", "700e338e91da7397893a497b6c0f45", "88743");
		getReports("cancelled", "2016-05-11", "2016-05-17", "700e338e91da7397893a497b6c0f45", "88743");
	}

	public String getReports(String offerStatus, //approved or cancelled
			String dateStart,
			String dateEnd,
			String token, 
			String id) throws IOException {
		CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        String reportResponseInJson = null;
        
		String reportingApiUrl = "http://affiliate-feeds.snapdeal.com/feed/api/order?startDate="+dateStart+"&endDate="+dateEnd+"&status="+offerStatus;
        
        try {
        	System.out.println("Making a url request for: "+reportingApiUrl);
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(reportingApiUrl);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            //nvps.add(new BasicNameValuePair("content-type", "application/json"));
            nvps.add(new BasicNameValuePair("Snapdeal-Affiliate-Id", id));
            nvps.add(new BasicNameValuePair("Snapdeal-Token-Id", token));
            nvps.add(new BasicNameValuePair("Accept", "application/json"));

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
			reportResponseInJson = resp.toString();
			System.out.println("got resposne: "+reportResponseInJson);
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
        
        return reportResponseInJson;
	}
	

}
