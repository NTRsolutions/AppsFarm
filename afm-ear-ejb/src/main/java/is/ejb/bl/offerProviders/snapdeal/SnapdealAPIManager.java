package is.ejb.bl.offerProviders.snapdeal;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferWallStats;
import is.ejb.bl.business.ThumbnailQuality;
import is.ejb.bl.offerProviders.snapdeal.SnapdealProviderConfig;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.CategoryOffers;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.timers.TimerSnapdealGetOffers;
import is.ejb.dl.dao.DAOSnapdealOffers;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.SnapdealOffersEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.CharSet;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
public class SnapdealAPIManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOSnapdealOffers daoSnapdealOffers;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void startProcessing() {
		// this is set to unblock timer from executing another request
		logger.info(OfferProviderCodeNames.SNAPDEAL + " setting timer busy to true: current timer status before set: "
				+ TimerSnapdealGetOffers.busy);
		TimerSnapdealGetOffers.busy = true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void endProcessing() {
		// this is set to unblock timer from executing another request
		logger.info(OfferProviderCodeNames.SNAPDEAL + " setting timer busy to false: current timer status before set: "
				+ TimerSnapdealGetOffers.busy);
		TimerSnapdealGetOffers.busy = false;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.READ)
	public void getOffers(String offerCategoryUrl, OfferWallEntity offerWall, IndividualOfferWall individualOfferWall,
			OfferFilterManager offerFilterManager, OfferRewardCalculationManager offerRewardCalculationManager,
			SnapdealProviderConfig adProviderConfig) throws Exception {

		try {
			// for each category feed url retrieve offers and store them in db
			String categoryOffersOutputInJson = getCategoryOffersUsingPost(offerCategoryUrl,
					adProviderConfig.getToken(), adProviderConfig.getId());
			ObjectMapper mapperForOffers = new ObjectMapper();
			CategoryOffers categoryOffersHolder = mapperForOffers.readValue(categoryOffersOutputInJson,
					CategoryOffers.class);
			System.out.println("out: " + categoryOffersHolder.getProducts().size());
			List<ProductsEntry> listOffers = categoryOffersHolder.getProducts();

			String categoryName = "Unknown";
			for (int i = 0; i < listOffers.size(); i++) {
				ProductsEntry product = listOffers.get(i);
				categoryName = product.getCategoryName();
				/*
				 * System.out.println(product.getAvailability()+" "+
				 * product.getBrand()+" "+ product.getCategoryName()+" "+
				 * product.getImageLink()+" "+ product.getSubCategoryName()+" "+
				 * product.getTitle()+" "+ product.getCategoryId()+" "+
				 * product.getCategoryId()+" "+ product.getEffectivePrice()+" "+
				 * product.getOfferPrice()+" "+ product.getSubCategoryId()+" "+
				 * product.getMrp()+" "+ product.getOfferPrice()+" "+
				 * product.getDescription() );
				 */
				break;
			}

			// create snapdeal entity
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.OK,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ " persisting offers for category: " + categoryName + " number of offers: "
							+ listOffers.size());
			logger.info("persisting offers for category: " + categoryName + " number of offers: " + listOffers.size());

			/**
			 * No commission will be paid for any Transaction for products from
			 * following categories: Precious Jewellery, Jewellery, Automobiles,
			 * TV Shop, Snapdeal Select, Digital Gift Cards, Real Estate, Click
			 * & Collect
			 */
			if (categoryName.equals("Fashion Jewellery") || categoryName.equals("TVs, Audio \u0026 Video")
					|| categoryName.equals("Precious Jewellery") || categoryName.equals("Jewellery")
					|| categoryName.equals("Automobiles") || categoryName.equals("TV Shop")
					|| categoryName.equals("Snapdeal Select") || categoryName.equals("Digital Gift Cards")
					|| categoryName.equals("Click and Collect") || categoryName.equals("Real Estate")) {

			} else { // add offers and its category only if they do not belong
						// to the above categories
				SnapdealOffersEntity snapdealEntity = new SnapdealOffersEntity();
				snapdealEntity.setCategoryName(categoryName);
				snapdealEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
				snapdealEntity.setNumberOfOffers(listOffers.size());
				snapdealEntity.setOffersJson(categoryOffersOutputInJson);
				snapdealEntity.setRealm(offerWall.getRealm());
				daoSnapdealOffers.create(snapdealEntity);
			}

		} catch (Exception exc) {
			Application.getElasticSearchLogger()
					.indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
							Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
									+ " Error: " + exc.toString()
									+ " when retrieving/processing data from offer provider");
		}
	}

	public void getDealsOfDaysOffers(OfferWallEntity offerWall, SnapdealProviderConfig adProviderConfig) {
		try {
			
			System.out.println("Selecting offers from DoD");
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.OK,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ " loading deals of the day");
			CloseableHttpClient httpClient = null;
			HttpPost httpPost = null;
			CloseableHttpResponse response = null;
			String categoryOffersOutputInJson = null;

			httpClient = HttpClients.createDefault();
			httpPost = new HttpPost(
					"http://affiliate-feeds.snapdeal.com/feed/api/dod/offer");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("Snapdeal-Affiliate-Id", adProviderConfig.getId()));
			nvps.add(new BasicNameValuePair("Snapdeal-Token-Id", adProviderConfig.getToken()));
			for (NameValuePair h : nvps) {
				httpPost.addHeader(h.getName(), h.getValue());
			}

			response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			System.out.println("Output from Server .... \n");

			String inputLine;
			StringBuffer resp = new StringBuffer();

			while ((inputLine = br.readLine()) != null) {
				resp.append(inputLine);
			}
			categoryOffersOutputInJson = resp.toString();
			ObjectMapper mapperForOffers = new ObjectMapper();
			CategoryOffers categoryOffersHolder = mapperForOffers.readValue(categoryOffersOutputInJson,
					CategoryOffers.class);
			System.out.println("out: " + categoryOffersHolder.getProducts().size());
			List<ProductsEntry> listOffers = categoryOffersHolder.getProducts();
			System.out.println("DOD OFFERS: " + listOffers.size());
			
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.OK,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ " persisting offers for category: Deals of the Day" + " number of offers: "
							+ listOffers.size());
			
			
			
			System.out.println("********** DOD **********");
			SnapdealOffersEntity snapdealEntity = new SnapdealOffersEntity();
			
			snapdealEntity.setCategoryName("Deals Of The Day");
			snapdealEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			snapdealEntity.setNumberOfOffers(listOffers.size());
			snapdealEntity.setOffersJson(categoryOffersOutputInJson);
			snapdealEntity.setRealm(offerWall.getRealm());
			daoSnapdealOffers.create(snapdealEntity);
			System.out.println("********** DOD SAVED total " + listOffers.size() + " **********");
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.OK,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ " deals of the day saved : " + snapdealEntity);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ArrayList<String> getFeedCategories(OfferWallEntity offerWall) throws IOException {
		ArrayList<String> categoryUrlList = new ArrayList<String>();
		ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();

		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		String reqResponse = "";

		// get categories

		// pull offers from all categories by following the category links

		// persist data in offer wall (it contains category info)
		String requestUrl = "http://affiliate-feeds.snapdeal.com/feed/88743.json";

		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
				offerWall.getRealm().getId(), LogStatus.OK, Application.OFFER_WALL_GENERATION_ACTIVITY + " "
						+ OfferProviderCodeNames.SNAPDEAL + " requesting url: " + requestUrl);
		logger.info("requesting url: " + requestUrl);
		try {
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(offerWall.getRealm().getConnectionTimeout() * 1000 * 3);
			urlConnection.setReadTimeout(offerWall.getRealm().getReadTimeout() * 1000 * 3);
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				reqResponse = inputLine;
			}
		} catch (Exception exc) {

		} finally {
			if (in != null) {
				in.close();
			}
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		logger.info(reqResponse);

		try {
			// serialise response into object
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(reqResponse);
			Iterator<JsonNode> it = actualObj.getElements();
			while (it.hasNext()) {
				JsonNode node = it.next();
				System.out.println("-> node: " + node.toString());
				Iterator<JsonNode> it1 = node.getElements();
				while (it1.hasNext()) {
					JsonNode node1 = it1.next();
					System.out.println("--> " + node1.toString());
					Iterator<JsonNode> it2 = node1.getElements();
					while (it2.hasNext()) {
						JsonNode node2 = it2.next();
						System.out.println("---> " + node2.toString());
						Iterator<JsonNode> it3 = node2.getElements();
						while (it3.hasNext()) {
							JsonNode node3 = it3.next();
							System.out.println("----> " + node3.toString());
							String categoryFeedUrl = node3.toString();
							categoryFeedUrl = categoryFeedUrl.substring(categoryFeedUrl.indexOf("get") + 6,
									categoryFeedUrl.length() - 4);
							System.out.println("-----> url: " + categoryFeedUrl);
							categoryUrlList.add(categoryFeedUrl);
						}
					}
				}
			}
		} catch (Exception exc) {
			Application.getElasticSearchLogger()
					.indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
							Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
									+ " Error: " + exc.toString()
									+ " when retrieving/processing data from offer provider");
		}

		return categoryUrlList;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.READ)
	public void deleteOldOffersFeed(OfferWallEntity offerWall) {
		// delete previous snapdeal offers from db as we keep only the latest
		// results
		try {

			logger.info("deleting all snapdeal old offers data...");
			/*
			 * List<SnapdealOffersEntity> listPersistedOffers =
			 * daoSnapdealOffers.findAll(); for(int
			 * i=0;i<listPersistedOffers.size();i++) { SnapdealOffersEntity
			 * offerEntity = listPersistedOffers.get(i); logger.info(
			 * "deleting all snapdeal old data for: "
			 * +offerEntity.getCategoryName());
			 * daoSnapdealOffers.delete(offerEntity); }
			 */
			int deletedCount = daoSnapdealOffers.deleteAll();
			logger.info("successfully deleted all snapdeal old offers data, count: " + deletedCount);
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.OK,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ "successfully deleted all snapdeal old offers data, rows count: " + deletedCount);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY,
					offerWall.getRealm().getId(), LogStatus.ERROR,
					Application.OFFER_WALL_GENERATION_ACTIVITY + " " + OfferProviderCodeNames.SNAPDEAL
							+ " error when removing previously persisted offers: " + exc.toString());
			logger.severe("error when removing previously persisted offers: " + exc.toString());
		}
	}

	public String getCategoryOffersUsingPost(String categoryUrl, String token, String id) throws IOException {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		CloseableHttpResponse response = null;
		String categoryOffersOutputInJson = null;

		try {
			System.out.println("Making a url request for: " + categoryUrl);
			httpClient = HttpClients.createDefault();
			httpPost = new HttpPost(categoryUrl);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			// nvps.add(new BasicNameValuePair("content-type",
			// "application/json"));
			nvps.add(new BasicNameValuePair("Snapdeal-Affiliate-Id", id));
			nvps.add(new BasicNameValuePair("Snapdeal-Token-Id", token));

			// StringEntity input = new StringEntity("{\"username\":
			// \"dummyuser\",\"password\": \"dummypassword\"}");
			// input.setContentType("application/json");
			// httpPost.setEntity(input);

			for (NameValuePair h : nvps) {
				httpPost.addHeader(h.getName(), h.getValue());
			}

			response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			System.out.println("Output from Server .... \n");
			String output = null;

			String inputLine;
			StringBuffer resp = new StringBuffer();

			while ((inputLine = br.readLine()) != null) {
				resp.append(inputLine);
			}
			categoryOffersOutputInJson = resp.toString();
			/*
			 * while ((output = br.readLine()) != null) { if(output != null) {
			 * categoryOffersOutputInJson = output; System.out.println(output);
			 * } }
			 */
		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {
			try {
				response.close();
				httpClient.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return categoryOffersOutputInJson;
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

}
/*
 * @Lock(LockType.READ) public void getOffers(OfferWallEntity offerWall,
 * IndividualOfferWall individualOfferWall, OfferFilterManager
 * offerFilterManager, OfferRewardCalculationManager
 * offerRewardCalculationManager, SnapdealProviderConfig adProviderConfig)
 * throws Exception {
 * 
 * //this is set to block timer from executing another request
 * TimerSnapdeal.busy = true; logger.info(OfferProviderCodeNames.SNAPDEAL+
 * " setting timer busy to false: current timer status after set: "
 * +TimerSnapdeal.busy);
 * 
 * //offer wall stats
 * //offerWall.setNumberOfRequestedOffers(offerWall.getNumberOfRequestedOffers()
 * ); //individualOfferWall.getListOfferStats().add(OfferWallStats.
 * number_of_requested_offers+":"+numberOfOffersToSelect);
 * 
 * ArrayList<Offer> listFoundOffers = new ArrayList<Offer>();
 * 
 * HttpURLConnection urlConnection = null; BufferedReader in = null; String
 * reqResponse = "";
 * 
 * //get categories
 * 
 * //pull offers from all categories by following the category links
 * 
 * //persist data in offer wall (it contains category info) String requestUrl =
 * "http://affiliate-feeds.snapdeal.com/feed/88743.json";
 * 
 * Application.getElasticSearchLogger().indexLog(Application.
 * OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
 * Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
 * OfferProviderCodeNames.SNAPDEAL+" requesting url: "+requestUrl); logger.info(
 * "requesting url: "+requestUrl); try { URL url = new URL(requestUrl);
 * urlConnection = (HttpURLConnection)url.openConnection();
 * urlConnection.setConnectTimeout(offerWall.getRealm().getConnectionTimeout() *
 * 1000 * 3); urlConnection.setReadTimeout(offerWall.getRealm().getReadTimeout()
 * * 1000 * 3); in = new BufferedReader( new InputStreamReader(
 * urlConnection.getInputStream())); String inputLine; while ((inputLine =
 * in.readLine()) != null) { reqResponse = inputLine; } } finally { if(in !=
 * null) { in.close(); } if(urlConnection != null) { urlConnection.disconnect();
 * } }
 * 
 * logger.info(reqResponse);
 * 
 * 
 * try { ArrayList<String> categoryUrlList = new ArrayList<String>();
 * 
 * //serialise response into object ObjectMapper mapper = new ObjectMapper();
 * JsonNode actualObj = mapper.readTree(reqResponse); Iterator<JsonNode> it =
 * actualObj.getElements(); while(it.hasNext()) { JsonNode node = it.next();
 * System.out.println("-> node: "+node.toString()); Iterator<JsonNode> it1 =
 * node.getElements(); while(it1.hasNext()) { JsonNode node1 = it1.next();
 * System.out.println("--> "+node1.toString()); Iterator<JsonNode> it2 =
 * node1.getElements(); while(it2.hasNext()) { JsonNode node2 = it2.next();
 * System.out.println("---> "+node2.toString()); Iterator<JsonNode> it3 =
 * node2.getElements(); while(it3.hasNext()) { JsonNode node3 = it3.next();
 * System.out.println("----> "+node3.toString()); String categoryFeedUrl =
 * node3.toString(); categoryFeedUrl =
 * categoryFeedUrl.substring(categoryFeedUrl.indexOf("get")+6,
 * categoryFeedUrl.length()-4); System.out.println("-----> url: "
 * +categoryFeedUrl); categoryUrlList.add(categoryFeedUrl); } } } }
 * 
 * //delete previous snapdeal offers from db as we keep only the latest results
 * try { logger.info("deleting all snapdeal old offers data...");
 * List<SnapdealOffersEntity> listPersistedOffers = daoSnapdealOffers.findAll();
 * for(int i=0;i<listPersistedOffers.size();i++) { SnapdealOffersEntity
 * offerEntity = listPersistedOffers.get(i); logger.info(
 * "deleting all snapdeal old data for: "+offerEntity.getCategoryName());
 * daoSnapdealOffers.delete(offerEntity); } logger.info(
 * "successfully deleted all snapdeal old offers data..."); } catch(Exception
 * exc) { exc.printStackTrace();
 * Application.getElasticSearchLogger().indexLog(Application.
 * OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
 * Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
 * OfferProviderCodeNames.SNAPDEAL+
 * " error when removing previously persisted offers: "+exc.toString());
 * logger.severe("error when removing previously persisted offers: "
 * +exc.toString()); }
 * 
 * //for each category feed url retrieve offers and store them in db for(int
 * x=0;x<categoryUrlList.size();x++) { String categoryOffersOutputInJson =
 * getCategoryOffersUsingPost(categoryUrlList.get(x),
 * adProviderConfig.getToken(), adProviderConfig.getId()); ObjectMapper
 * mapperForOffers = new ObjectMapper(); CategoryOffers categoryOffersHolder =
 * mapperForOffers.readValue(categoryOffersOutputInJson, CategoryOffers.class);
 * System.out.println("out: "+categoryOffersHolder.getProducts().size());
 * List<ProductsEntry> listOffers = categoryOffersHolder.getProducts();
 * 
 * String categoryName = "Unknown"; for(int i=0;i<listOffers.size();i++) {
 * ProductsEntry product = listOffers.get(i); categoryName =
 * product.getCategoryName(); System.out.println(product.getAvailability()+" "+
 * product.getBrand()+" "+ product.getCategoryName()+" "+
 * product.getImageLink()+" "+ product.getSubCategoryName()+" "+
 * product.getTitle()+" "+ product.getCategoryId()+" "+ product.getCategoryId()+
 * " "+ product.getEffectivePrice()+" "+ product.getOfferPrice()+" "+
 * product.getSubCategoryId()+" "+ product.getMrp()+" "+
 * product.getOfferPrice()+" "+ product.getDescription() ); break; }
 * 
 * //create snapdeal entity
 * Application.getElasticSearchLogger().indexLog(Application.
 * OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
 * Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
 * OfferProviderCodeNames.SNAPDEAL+" persisting offers for category: "
 * +categoryName+" number of offers: "+listOffers.size()); logger.info(
 * "persisting offers for category: "+categoryName+" number of offers: "
 * +listOffers.size());
 * 
 * SnapdealOffersEntity snapdealEntity = new SnapdealOffersEntity();
 * snapdealEntity.setCategoryName(categoryName);
 * snapdealEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
 * snapdealEntity.setNumberOfOffers(listOffers.size());
 * snapdealEntity.setOffersJson(categoryOffersOutputInJson);
 * snapdealEntity.setRealm(offerWall.getRealm());
 * daoSnapdealOffers.create(snapdealEntity); }
 * 
 * } catch(Exception exc) {
 * Application.getElasticSearchLogger().indexLog(Application.
 * OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK,
 * Application.OFFER_WALL_GENERATION_ACTIVITY+" "+
 * OfferProviderCodeNames.SNAPDEAL+ " Error: "+exc.toString()+
 * " when retrieving/processing data from offer provider"); } finally { //this
 * is set to unblock timer from executing another request
 * logger.info(OfferProviderCodeNames.SNAPDEAL+
 * " setting timer busy to false: current timer status after set: "
 * +TimerSnapdeal.busy); TimerSnapdeal.busy = false; } }
 * 
 */
