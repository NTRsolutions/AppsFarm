package is.ejb.bl.monitoring.operation;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.system.logging.LogStatus;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.criterion.Order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


//this class is used to analyse data / database to retrieve requested KPI's
@Stateless
public class SystemOpsAnalyser {
	
	private Logger logger = Logger.getLogger(SystemOpsAnalyser.class.getName());

	//errors
	public long getErrorsCount(int timeInterval, int realmId) { //interval in minutes
		PT searchParams = new PT();
		searchParams.put("@logStatus",LogStatus.ERROR.toString() );
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getWarningsCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("@logStatus",LogStatus.WARNING.toString() );
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//-------------------------------------------------------------------
	//cow ids selection count
	public long getCOWIdsSelectCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COW_SELECTION_ACTIVITY);
		searchParams.put("message",Application.COW_IDS_SELECTION);
		searchParams.put("message",Application.COW_IDS_SELECTION_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//cow ids failed selection count
	public long getCOWIdsSelectFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COW_SELECTION_ACTIVITY);
		searchParams.put("message",Application.COW_IDS_SELECTION);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//cow by id selection count
	public long getCOWSelectByIdCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COW_SELECTION_ACTIVITY);
		searchParams.put("message",Application.COW_SELECTION_BY_ID);
		searchParams.put("message",Application.COW_SELECTION_BY_ID_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//cow by id selection count
	public long getCOWSelectByIdFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COW_SELECTION_ACTIVITY);
		searchParams.put("message",Application.COW_SELECTION_BY_ID);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//-------------------------------------------------------------------
	//individual offer generation
	
	public long getOffersSingleOfferGenerationCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_CREATED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getOffersRejectedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_REJECTED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//-------------------------------------------------------------------
	//single offer wall generation
	public long getSingleOffersGeneratedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_CREATED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersPayoutBelowTresholdCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_PAYOUT_BELOW_TRESHOLD);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersNoCurrencyDefinedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_NO_CURRENCY_DEFINED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//new
	public long getSingleOffersNoImageDefinedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_NO_IMAGE_DEFINED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersNoGeoFilteringDataSuppliedByOfferProviderCout(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_GEO_FILTERING_ERROR_NO_COUNTRY_CODES_SUPPLIED_BY_OFFER_PROVIDER);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersNoTargetDeviceFilteringDataSuppliedByOfferProviderCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_TARGET_DEVICE_FILTERING_ERROR_NO_TARGET_DEVICES_SUPPLIED_BY_OFFER_PROVIDER);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersNoSupportedPayoutCurrencyCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_NO_SUPPORTED_PAYOUT_CURRENCY_DEFINED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getSingleOffersDuplicatesRejected(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_REJECTED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//-------------------------------------------------------------------
	//COW generation
	
	public long getOffersCompositeWallGenerationCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COMPOSITE_OFFER_WALL_GENERATION_IDENTIFIED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getOffersCompositeWallGenerationFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.COMPOSITE_OFFER_WALL_GENERATION_FAILED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getOffersInsufficientCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.OFFERS_GENERATION_OFFERS_INSUFFICIENT);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getOffersSIngleWallGenerationCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getOffersSIngleWallGenerationFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.SINGLE_OFFER_WALL_GENERATION_FAILED);
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}


	//------------------------------------ clicks --------------------------------------
	public long getClicksIdentifiedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CLICK_ACTIVITY);
		searchParams.put("message",Application.CLICK_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getClicksSuccessfulCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CLICK_ACTIVITY);
		searchParams.put("message",RespStatusEnum.SUCCESS.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getClicksFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CLICK_ACTIVITY);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//conversions
	public long getConversionsIdentifiedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CONVERSION_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getConversionsSuccessfulCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CONVERSION_ACTIVITY);
		searchParams.put("message",Application.CONVERSION_ACTIVITY_SUCCESS);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getConversionsFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.CONVERSION_IDENTIFIED);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//reward requests
	public long getRewardRequestsIdentifiedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_REQUEST_ACTIVITY);
		searchParams.put("message",Application.REWARD_REQUEST_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardRequestsSuccessfulCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_REQUEST_ACTIVITY);
		searchParams.put("message",RespStatusEnum.SUCCESS.toString());
		
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardRequestsFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_REQUEST_ACTIVITY);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//reward responses
	public long getRewardResponsesIdentifiedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_IDENTIFIED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardResponsesSuccessfulCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_ACTIVITY);
		searchParams.put("message",RespStatusEnum.SUCCESS.toString());
		searchParams.put("message",RespCodesEnum.OK_NO_CONTENT.toString());

		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardResponsesFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_FAILED);
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	//reward responses with success status from reward service
	public long getRewardResponsesWithSuccessStatusIdentifiedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_ACTIVITY);
		searchParams.put("message",Application.REWARD_RESPONSE_IDENTIFIED);
		searchParams.put("message","["+RewardStatus.SUCCESS.toString()+"]");
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardNotificationRequestsSuccessCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_NOTIFICATION_ACTIVITY);
		searchParams.put("message",RespStatusEnum.SUCCESS.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}

	public long getRewardNotificationRequestsFailedCount(int timeInterval, int realmId) {
		PT searchParams = new PT();
		searchParams.put("message",Application.REWARD_ACTIVITY);
		searchParams.put("message",Application.REWARD_NOTIFICATION_ACTIVITY);
		searchParams.put("message",RespStatusEnum.FAILED.toString());
		return countDocuments(searchParams, realmId, 60*1000*timeInterval);
	}
	
//	public long getTotalOffersPoolSize(int timeInterval, int realmId) { //total pool of offers across all offer providers
//		int totalCount = 0;
//		
//		PT searchParams = new PT();
//		searchParams.put("message",Application.REWARD_ACTIVITY);
//		searchParams.put("message",Application.REWARD_NOTIFICATION_ACTIVITY);
//		searchParams.put("message",RespStatusEnum.FAILED.toString());
//		return searchDocuments(searchParams, realmId, 60*1000*timeInterval);
//	}

	//use hashtable for this
	private SearchHit[] searchDocuments(PT searchParams, int realmId,  
			long millisInterval) {
		SearchHit[] results = null;
		try {
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			//qb.must(QueryBuilders.matchQuery("realmId", realmId));
			ArrayList<Param> listParams = searchParams.getListParams();
			for(int i=0;i<listParams.size();i++) {
				String key = listParams.get(i).getKey();
				String value = listParams.get(i).getValue();
				logger.info("searching by: key: "+key+" value: "+value);
				qb.must(QueryBuilders.matchQuery(key, value));
			}
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-millisInterval)).to(new Date()));
			SearchResponse response = Application.getESClient().prepareSearch(Application.esLogIndexName+"*")
			        .setQuery(qb)
			        .setFrom(0).setSize(100000)
			        .addSort("@timestamp",SortOrder.ASC)
			        .execute().actionGet();
			results = response.getHits().getHits();
			
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));
				String sn = (String)result.get("SN");
				String logStatus = (String)result.get("@logStatus");
				String logContent = (String)result.get("message");
				System.out.println("ts: "+timestamp+" sn: "+sn+" status: "+logStatus+" msg: "+logContent+" time: "+date.getTime());
			}
			
			
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		}
		
		return results;
	}	

	//use hashtable for this
	private long countDocuments(PT searchParams,
			int realmId,
			long millisInterval) {
		try {
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			//qb.must(QueryBuilders.matchQuery("realmId", realmId));
			ArrayList<Param> listParams = searchParams.getListParams();
			for(int i=0;i<listParams.size();i++) {
				String key = listParams.get(i).getKey();
				String value = listParams.get(i).getValue();
				//logger.info("searching by: key: "+key+" value: "+value);
				qb.must(QueryBuilders.matchQuery(key, value));
			}
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-millisInterval)).to(new Date()));
			//logger.info(qb.toString());
			CountResponse response = Application.getESClient().prepareCount(Application.esLogIndexName+"*")
			        .setQuery(qb)
			        .execute().actionGet();
			return response.getCount();
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		}
	}	

	private SearchHit[] searchDocument(String field, String value, 
			long millisInterval) {
		SearchHit[] results = null;
		try {
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			qb.must(QueryBuilders.matchQuery(field, value));
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-millisInterval)).to(new Date()));
			SearchResponse response = Application.getESClient().prepareSearch(Application.esLogIndexName+"*")
			        .setQuery(qb)
			        .setFrom(0).setSize(100000)
			        .addSort("@timestamp",SortOrder.ASC)
			        .execute().actionGet();
			results = response.getHits().getHits();
			
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));
				String sn = (String)result.get("SN");
				String logStatus = (String)result.get("@logStatus");
				String logContent = (String)result.get("message");
				System.out.println("ts: "+timestamp+" sn: "+sn+" status: "+logStatus+" msg: "+logContent+" time: "+date.getTime());
			}
			
			
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		}
		
		return results;
	}	

	public static void main(String[] args) {
		new SystemOpsAnalyser("a");
	}
	
	public SystemOpsAnalyser() {
	}

	public SystemOpsAnalyser(String a) {
		test();
	}
	
	public void test() {
		Settings settings; 
		// once we find one node in the cluster ask about the others      
		Builder settingsBuilder = 
		ImmutableSettings.settingsBuilder().put("client.transport.sniff", true); 
		settingsBuilder.put("cluster.name", "elasticsearch"); 
		settingsBuilder.put("client.transport.ping_timeout", "10s");
		settingsBuilder.put("http.enabled", "false");
		settingsBuilder.put("transport.tcp.port", "9300-9400");
		settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
		settingsBuilder.put("discovery.zen.ping.unicast.hosts", "127.0.0.1");
		settings = settingsBuilder.build(); 

		TransportClient esClient = new TransportClient(settings).
				addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

		int millisInterval = 1000*60*15;
		PT searchParams = new PT();
//		searchParams.put("message",Application.GENERIC_USER_CLICK_ACTIVITY);
//		searchParams.put("message","received");
//		searchParams.put("message","SUCCESS");
//		searchParams.put("message",Application.GENERIC_USER_CLICK_ACTIVITY);
//		searchParams.put("message",Application.CLICK_IDENTIFIED);

		try {
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			//qb.must(QueryBuilders.matchQuery("realmId", 4));
			ArrayList<Param> listParams = searchParams.getListParams();
			for(int i=0;i<listParams.size();i++) {
				String key = listParams.get(i).getKey();
				String value = listParams.get(i).getValue();
				logger.info("searching by: key: "+key+" value: "+value);
				qb.must(QueryBuilders.matchQuery(key, value));
			}
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-millisInterval)).to(new Date()));
			System.out.println("dupa: "+qb.toString());
			CountResponse response = esClient.prepareCount(Application.esLogIndexName+"*")
			        .setQuery(qb)
			        .execute().actionGet();
			System.out.println("got count: "+response.getCount());

			//----------------
			qb = QueryBuilders.boolQuery();
			//qb.must(QueryBuilders.matchQuery("realmId", 4));
			listParams = searchParams.getListParams();
			for(int i=0;i<listParams.size();i++) {
				String key = listParams.get(i).getKey();
				String value = listParams.get(i).getValue();
				logger.info("searching by: key: "+key+" value: "+value);
				qb.must(QueryBuilders.matchQuery(key, value));
			}
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-millisInterval)).to(new Date()));
			SearchResponse sR = esClient.prepareSearch(Application.esLogIndexName+"*")
			        .setQuery(qb)
			        .setFrom(0).setSize(100000)
			        .addSort("@timestamp",SortOrder.ASC)
			        .execute().actionGet();
			SearchHit[] results = sR.getHits().getHits();
			
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));
				String sn = (String)result.get("SN");
				String logStatus = (String)result.get("@logStatus");
				String logContent = (String)result.get("message");
				System.out.println("ts: "+timestamp+" sn: "+sn+" status: "+logStatus+" msg: "+logContent+" time: "+date.getTime());
			}

			
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		} finally {
			esClient.close();
		}

	}
	
	public class Param {
		String key;
		String value;
		
		public Param(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public class PT {
		ArrayList<Param> listParams=new ArrayList<Param>();
		public void put(String key, String value) {
			listParams.add(new Param(key,value));
		}
		public ArrayList<Param> getListParams() {
			return listParams;
		}
		public void setListParams(ArrayList<Param> listParams) {
			this.listParams = listParams;
		}
		
	}
}
