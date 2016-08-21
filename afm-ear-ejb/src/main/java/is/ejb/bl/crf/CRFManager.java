package is.ejb.bl.crf;

import static org.elasticsearch.node.NodeBuilder.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferCommand;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.notificationSystems.gcm.test.TestGoogleNotificationSender;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.ESIndexName;
import is.ejb.bl.system.logging.ESLoggerWorkerThread;
import is.ejb.bl.system.logging.ESTypeName;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.RealmEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse; 
import org.jboss.marshalling.TraceInformation.IndexType;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;


@Stateless
public class CRFManager {

	protected static final Logger logger = Logger.getLogger(CRFManager.class.getName());

	private Client client = null;
	private String hostName = "127.0.0.1";
	private String clusterName = "";
	private ArrayList<BlockedOffer> listBlockedOffers = null;

	public CRFManager(String hostName, String clusterName) {
		this.hostName = hostName;
		this.clusterName = clusterName;
		client = getClient(hostName);
	}
	
	private Client getClient(String hostName) {
		try {
			if(client == null) {
				// once we find one node in the cluster ask about the others      
				Builder settingsBuilder = 
				ImmutableSettings.settingsBuilder().put("client.transport.sniff", true); 
				settingsBuilder.put("cluster.name", clusterName); 
				settingsBuilder.put("client.transport.ping_timeout", "10s");
				settingsBuilder.put("http.enabled", "false");
				settingsBuilder.put("transport.tcp.port", "9300-9400");
				settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
				settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostName);
				Settings settings = settingsBuilder.build();

				client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostName, 9300));
				
				logger.info("ReportingManager ES client initialised with reference: "+client.toString()+" host: "+hostName+" cluster name: "+clusterName);
	        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, -1, 
	        			LogStatus.OK, 
	        			Application.CRF_TRIGGER_ACTIVITY+
	        			" ReportingManager ES client initialised with reference: "+client.toString()+" host: "+hostName+" cluster name: "+clusterName);
			} else {
				logger.info("ReportingManager ES client already initialised, reusing existing reference: "+client.toString()+" host: "+hostName+" cluster name: "+clusterName +" "+client.toString());
	        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, -1, 
	        			LogStatus.OK, 
	        			Application.CRF_TRIGGER_ACTIVITY+
	        			" ReportingManager ES client already initialised, reusing existing reference: "+client.toString()+" host: "+hostName+" cluster name: "+clusterName +" "+client.toString());
			}
			
			return client;
		} catch(Exception exc) {
			logger.severe(exc.toString());
        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, -1, 
        			LogStatus.ERROR, 
        			Application.CRF_TRIGGER_ACTIVITY+" error: "+exc.toString());
			return null;
		}
	}

	public ArrayList<BlockedOffer> filterOffers(int crfMinimalClickRate, double crfCRThreshold, int networkId) {
		ArrayList<BlockedOffer> listOffersToBlock = new ArrayList<BlockedOffer>();
		SearchResponse response = null;	        
		client = getClient(hostName);

		try {
			QueryBuilder query;

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", "BPM"));
            query = boolQueryBuilder;
	        response = client.prepareSearch("ab_clicks*")
	                .setTypes("clicks")
	                .setQuery(query)
	                .addAggregation(
	                        AggregationBuilders.terms("offerIdProvider").field("offerIdProvider").size(10000)
	                        .subAggregation(AggregationBuilders.terms("eventType").field("eventType").size(10000)
	                        ).order(Terms.Order.count(true))
	                ).setSize(0).execute().actionGet();

	        //System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
	        //System.out.println("elasticsearch response: {}: " + response.toString());
	     
	        //Terms  terms = response.getAggregations().get("offerName");
	        Terms  terms = response.getAggregations().get("offerIdProvider");
	        Collection<Terms.Bucket> buckets = terms.getBuckets();
	        Iterator it = buckets.iterator();
	        
	        //String offerName;
	        String offerIdProvider;
	        double sumClicks = 0;
	        double sumConversions = 0;
	        double conversionRatio = 0;
	        while(it.hasNext()) {
	        	Terms.Bucket b = (Terms.Bucket)it.next();
	        	offerIdProvider = b.getKey();
		        Terms termsSN = b.getAggregations().get("eventType");
		        Collection<Terms.Bucket> statusBuckets = termsSN.getBuckets();
	        	Iterator statusIterator = statusBuckets.iterator();
		   
	        	//reset values
	        	sumClicks = 0;
		        sumConversions = 0;
		        conversionRatio = 0;

		        while(statusIterator.hasNext()) {
		        	Terms.Bucket statusBucket = (Terms.Bucket)statusIterator.next();
		        	String eventType = statusBucket.getKey(); 
		        	if(eventType.equals(UserEventType.click.toString())) {
		        		sumClicks = statusBucket.getDocCount();
		        	}
		        	if(eventType.equals(UserEventType.conversion.toString())) {
		        		sumConversions = statusBucket.getDocCount();
		        	} 
		        }

		        //process and filter
            	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
            			networkId, 
            			LogStatus.OK, 
            			Application.CRF_TRIGGER_ACTIVITY+
            			" verifying offer with offer id (provider): "+offerIdProvider);

	        	try {
	        		conversionRatio = sumConversions/sumClicks;
	        		conversionRatio = round(conversionRatio, 2);
		        	if(sumClicks >= crfMinimalClickRate && conversionRatio < crfCRThreshold) {

		            	BlockedOffer bo = getOfferById(offerIdProvider);
		        		bo.setActive(true);
		        		bo.setRenderConversionStats(true);
		        		bo.setSumClicks((int)sumClicks);
		        		bo.setSumConversions((int)sumConversions);
		        		bo.setConvRatio(conversionRatio);
		        		bo.setCommand(BlockedOfferCommand.BLOCK.toString());
		        		listOffersToBlock.add(bo);
		        		
		            	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
		            			networkId, 
		            			LogStatus.OK, 
		            			Application.CRF_BLOCK_ACTIVITY+
		            			" setting offer to BLOCK state: "+bo.getTitle()+ " conversions: "+sumConversions +" clicks: "+sumClicks+" (th: "+crfMinimalClickRate+") cvr: "+conversionRatio+" (th: "+crfCRThreshold+")");
		        	} else {
		            	BlockedOffer bo = getOfferById(offerIdProvider);
		        		bo.setActive(true);
		        		bo.setRenderConversionStats(true);
		        		bo.setSumClicks((int)sumClicks);
		        		bo.setSumConversions((int)sumConversions);
		        		bo.setConvRatio(conversionRatio);
		        		bo.setCommand(BlockedOfferCommand.UNBLOCK.toString());
		        		listOffersToBlock.add(bo);
		        		
		            	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
		            			networkId, 
		            			LogStatus.OK, 
		            			Application.CRF_BLOCK_ACTIVITY+
		            			" setting offer to UNBLOCK state: "+bo.getTitle()+ " conversions: "+sumConversions +" clicks: "+sumClicks+" (th: "+crfMinimalClickRate+") cvr: "+conversionRatio+" (th: "+crfCRThreshold+")");
		        	}
	        	} catch(Exception exc) {
	        		conversionRatio = 0;
	            	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, networkId, 
	            			LogStatus.ERROR, 
	            			Application.CRF_TRIGGER_ACTIVITY+" error: "+exc.toString());
	        	}
	        }
	     
		} catch(Exception exc){
			logger.severe(exc.toString());
			exc.printStackTrace();
		}
		
		return listOffersToBlock;
	}

	public BlockedOffer getOfferById(String offerIdProvider) {
		SearchResponse response = null;	        
		client = getClient(hostName);

		try {
			QueryBuilder query;

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", "BPM"));
            boolQueryBuilder.must(QueryBuilders.matchQuery("offerIdProvider", offerIdProvider));
            
            query = boolQueryBuilder;
	        response = client.prepareSearch("ab_clicks*")
	                .setTypes("clicks")
	                .setQuery(query)
	                .setFrom(0)
	                .setSize(2)
	                .execute().actionGet();

	        //System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
	        //System.out.println("elasticsearch response: {}: " + response.toString());
	        
	        SearchHit[] results = response.getHits().getHits();
	        if(results.length == 0) {
	        	logger.info("!!! offer with id not found: "+offerIdProvider);
	        	return null; //no offer with given id was found
	        } else {
	        	logger.info("found results number: "+results.length);
	        	SearchHit hit = results[0];
				Map<String,Object> result = hit.getSource();
				String offerId = (String)result.get("offerId");
				String offerReward = (String)result.get("rewardType");
				String offerProviderName = (String)result.get("offerProviderName");
				String offerName = (String)result.get("offerName");
				
				BlockedOffer bo = new BlockedOffer();
				bo.setTitle(offerName);
				bo.setAdProviderCodeName(offerProviderName);
				bo.setRewardType(offerReward);
				bo.setSourceId(offerIdProvider);
				bo.setId(offerId);
				
				System.out.println("got offer: "+bo.getTitle()+" provider name: "+bo.getAdProviderCodeName()+" reward type: "+bo.getRewardType());

				return bo;
	        }
		} catch(Exception exc){
			logger.severe(exc.toString());
			exc.printStackTrace();
			return null;
		}
		
	}

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

	private void closeESClient() {
		client.close();
	}

	public static void main(String[] args) {
		new CRFManager();
	}
	
	public CRFManager() {
	}
}
