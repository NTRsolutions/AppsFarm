package is.ejb.bl.offerProviders.snapdeal;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOSnapdealOffers;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.SnapdealOffersEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ejb.Singleton;

@Singleton
@Startup
public class SnapdealTopProductsManager {
	@Inject
	private DAOUserEvent daoUserEvent;
	@Inject
	private SnapdealManager snapdealManager;

	@Inject
	private DAOSnapdealOffers daoSnapdealOffers;

	private List<ProductsEntry> topProducts;

	@Inject
	private Logger logger;

	@PostConstruct
	public void init() {
		topProducts = new ArrayList<ProductsEntry>();
	}

	private List<UserEventEntity> loadEventsInRange(Date startTime, Date endTime) {
		logger.info("Load events in range startTime: " + startTime + " endTime: " + endTime);
		List<UserEventEntity> userEvents = new ArrayList<UserEventEntity>();
		try {
			userEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardTypeNotRewarded(
					UserEventCategory.SNAPDEAL, startTime, endTime, "AirRewardz-India");
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.ERROR,
					Application.SNAPDEAL_TOPLIST_MANAGER + " load events in range startTime: " + startTime
							+ " endtime: " + endTime + " exception: " + exception.getMessage());
		}
		logger.info("Returning events count: " + userEvents.size());
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + " load events in range startTime: " + startTime + " endtime: "
						+ endTime + " events: " + userEvents);

		return userEvents;
	}

	public void loadTopProducts() {
		logger.info("****Loading top snapdeal products*****");
		List<ProductsEntry> loadedProducts = new ArrayList<ProductsEntry>();
		Date endTime = new Date();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Date startTime = cal.getTime();

		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + " loading top products from: " + startTime + " to : " + endTime);
		List<UserEventEntity> events = loadEventsInRange(startTime, endTime);
		if (events != null) {
			Map<String, Integer> topProductsMap = prepareTopProductsMap(events);
			Map<String, Integer> topProductsMapSorted = sortByComparator(topProductsMap);
			List<ProductsEntry> allProductsList = getAllProducts();

			logger.info("Calculating topProducts...");
			for (String productId : topProductsMapSorted.keySet()) {
				logger.info("Element from toplist: " + productId + " count: " + topProductsMapSorted.get(productId));
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
						Application.SNAPDEAL_TOPLIST_MANAGER + "top products from: " + startTime + " to : " + endTime
								+ " Element from toplist: " + productId + " count: "
								+ topProductsMapSorted.get(productId));

				for (ProductsEntry product : allProductsList) {
					String offerId = String.valueOf(product.getId());
					if (offerId.equals(productId)) {
						logger.info("Adding product with title: " + product.getTitle() + " to toplist");
						Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
								Application.SNAPDEAL_TOPLIST_MANAGER + " top products from: " + startTime + " to : "
										+ endTime + " adding product: " + product.getTitle());
						loadedProducts.add(product);
					}
				}
			}

		}

		topProducts.clear();
		topProducts.addAll(loadedProducts);
		logger.info("Top products contains now " + topProducts.size() + " elements");
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + "top products from: " + startTime + " to : " + endTime
						+ " Top products contains now " + topProducts.size() + " elements");
	}

	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (int i = list.size() - 1; i >= 0; i--) {
			Entry<String, Integer> entry = list.get(i);
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private List<ProductsEntry> getAllProducts() {
		List<ProductsEntry> allProductsList = new ArrayList<ProductsEntry>();
		try {
			List<SnapdealOffersEntity> snapdealOffersList = daoSnapdealOffers.findAll();
			for (SnapdealOffersEntity offersEntity : snapdealOffersList) {
				List<ProductsEntry> currentOffersEntity = snapdealManager.loadProductsFromSnapdealOffers(offersEntity);
				allProductsList.addAll(currentOffersEntity);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.ERROR,
					Application.SNAPDEAL_TOPLIST_MANAGER + "Get all products exception " + exception.getMessage() );
		}
		
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + "Get all products returns " + allProductsList.size() + " products" );
		logger.info("Get all products returns " + allProductsList.size() + " products");
		return allProductsList;
	}

	private HashMap<String, Integer> prepareTopProductsMap(List<UserEventEntity> events) {

		logger.info("Preparing top products map from " + events.size() + " events");
		
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + "Preparing top products map from " + events.size() + " events" );
		HashMap<String, Integer> topProductsMap = new HashMap<String, Integer>();
		for (UserEventEntity event : events) {
			if (topProductsMap.containsKey(event.getOfferId())) {

				topProductsMap.put(event.getOfferId(), topProductsMap.get(event.getOfferId()) + 1);
				logger.info("Adding count to : " + event.getOfferId() + " " + topProductsMap.get(event.getOfferId()));
			} else {
				logger.info("Putting : " + event.getOfferId());
				topProductsMap.put(event.getOfferId(), 1);
			}
		}
		logger.info("Returning top products map with " + topProductsMap.size() + " elements");
		
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.OK,
				Application.SNAPDEAL_TOPLIST_MANAGER + "Returning top products map with " + topProductsMap.size() + " elements : " + topProductsMap  );
		return topProductsMap;
	}

	public List<ProductsEntry> getTopProducts() {
		return topProducts;
	}

}
