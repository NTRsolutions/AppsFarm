package is.ejb.dl.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.alert.AlertLevel;
import is.ejb.bl.monitoring.alert.AlertStatus;
import is.ejb.bl.monitoring.server.ServerStats;
import is.ejb.bl.monitoring.server.SingleDiskSpaceStatsHolder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram.Bucket;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


@Stateless
public class DAOServerStats {
	
	private Logger logger = Logger.getLogger(DAOServerStats.class.getName());
	private ESClient esClient = null;
	private String host;
	private int port;

	public DAOServerStats() {
		try {
			this.host = Application.getLogServerAddress();	
		} catch(Exception exc) {
			this.host = "localhost";
			//exc.printStackTrace();
		}
		
		
		this.port = 9300;
		initClient();
	}
	
	public void initClient() {
		// on startup
		esClient = new ESClient(host,port);		
	}

	public void create(String indexName, String typeName, ServerStats stats) throws Exception {
		esClient.connect();
		Client client = esClient.getClient();
		
		XContentBuilder xcb = jsonBuilder().startObject();
		xcb.field("hostName", stats.getHostName());
		xcb.field("hostIpAddress", stats.getHostIpAddress());
		xcb.field("memTotalUsedPercentage", stats.getMemTotalUsedPercentage());
		xcb.field("memUsed", stats.getMemUsed());
		xcb.field("memJavaTotalAllocated", stats.getMemJavaTotalAllocated());
		xcb.field("memJavaUsageRatio", stats.getMemJavaUsageRatio());
		xcb.field("cpuUtilisation", stats.getCpuUtilisation());
		xcb.field("gcCount", stats.getGcCount());
		xcb.field("gcTime", stats.getGcTime());
		xcb.field("requestsCWMP", stats.getRequestsCWMP());
		xcb.field("requestsDB", stats.getRequestsDB());
		xcb.field("requestsMonitoring", stats.getRequestsMonitoring());
		xcb.field("netDownload", stats.getNetDownload());
		xcb.field("netUpload", stats.getNetUpload());
		xcb.field("logsWarning", stats.getLogsWarning());
		xcb.field("logsSevere", stats.getLogsSevere());
		xcb.field("diskReads", stats.getDiskReads());
		xcb.field("diskWrites", stats.getDiskWrites());
		xcb.field("@timestamp", new Date());
		xcb.field("@time", System.currentTimeMillis());
		
		//server info
		xcb.field("serverCurrentTime", stats.getCurrentTime());
		xcb.field("serverUpTime", stats.getUpTime());
		xcb.field("loadAverage", stats.getLoadAverage());
		xcb.field("loadAverage", stats.getLoadAverage());
		xcb.field("currentFqdn", stats.getCurrentFqdn());
		xcb.field("javaVendor", stats.getJavaVendor());
		xcb.field("javaVersion", stats.getJavaVersion());
		xcb.field("osArch", stats.getOsArch());
		xcb.field("osName", stats.getOsName());
		xcb.field("osCodeName", stats.getOsCodeName());
		xcb.field("osMachine", stats.getOsMachine());
		xcb.field("osVendor", stats.getOsVendor());
		xcb.field("osVendorVersion", stats.getOsVendorVersion());
		xcb.field("osPatchLevel", stats.getOsPatchLevel());
		
		//network info
		xcb.field("networkDefaultGateway", stats.getNetworkDefaultGateway());
		xcb.field("networkDomainName", stats.getNetworkDomainName());
		xcb.field("networkHostName", stats.getNetworkHostName());
		xcb.field("networkPrimaryDns", stats.getNetworkPrimaryDns());
		xcb.field("networkSecondaryDns", stats.getNetworkSecondaryDns());
		xcb.field("networkPrimaryInterface", stats.getNetworkPrimaryInterface());
		xcb.field("networkPrimaryIpAddress", stats.getNetworkPrimaryIpAddress());
		xcb.field("networkPrimaryMacAddress", stats.getNetworkPrimaryMacAddress());
		xcb.field("networkPrimaryNetmask", stats.getNetworkPrimaryNetmask());
		
		xcb.startArray("disks");
		for(int j=0;j<stats.getDisksStats().size();j++){
			SingleDiskSpaceStatsHolder hd = stats.getDisksStats().get(j);
	 		xcb.startObject();
			xcb.field("disk_id", j);
	    	xcb.field("mount_path", hd.getFileSystemMountPath());
			xcb.field("main_disk", hd.isMainDrive());
	    	xcb.field("file_system_name", hd.getFileSystemName());
	    	xcb.field("file_system_type", hd.getFileSystemType());
	    	xcb.field("size_total", hd.getSizeTotal());
	    	xcb.field("size_available", hd.getSizeAvailable());
	    	xcb.field("size_used", hd.getSizeUsed());
	    	xcb.field("size_usage_ratio", hd.getSizeUsageRatio());
	    	xcb.endObject();
		}
		xcb.endArray();
        //xcb.field("_ttl", 120)
		xcb.endObject();

		//store index with date suffix
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
		DecimalFormat mFormat= new DecimalFormat("00");
		mFormat.format(Double.valueOf(year));
		mFormat.setRoundingMode(RoundingMode.DOWN);
		String dates =  mFormat.format(Double.valueOf(year)) + "-" +  mFormat.format(Double.valueOf(month)) + "-" +  mFormat.format(Double.valueOf(day));
		String indexNameWithDate = indexName + "-"+dates;
		
		IndexResponse response = client.prepareIndex(indexNameWithDate, typeName).setSource(xcb).execute().actionGet();
		esClient.close();
	}

	public SearchResponse calculateParameterValues(String index, String type,
			String monitoredParameterName,
			int minutesInterval,
			Date startDate,
			Date endDate) {
		
		SearchResponse response = null;
		try {
			esClient.connect();
			QueryBuilder query;

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(startDate).to(endDate));
            query = boolQueryBuilder;
	        
	        response = esClient.getClient().prepareSearch(index+"*")
	                .setTypes(type)
	                .setQuery(query)
	                .addAggregation(
	                        AggregationBuilders.dateHistogram("by_minutes")
	                        		.extendedBounds(startDate.getTime(), endDate.getTime())
	                                .field("@timestamp")	                                
	                                .interval(DateHistogram.Interval.minutes(minutesInterval)
	                        ).minDocCount(0).
	                        subAggregation(AggregationBuilders.avg("AVG").field(monitoredParameterName))
	                )
	                .setFrom(0)
	                .execute().actionGet();

	        
	        //System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
	        //System.out.println("elasticsearch response: {}: " + response.toString());
	        /*
        	InternalDateHistogram  agHistogram = response.getAggregations().get("by_minutes");
        	Collection agBuckets = agHistogram.getBuckets();
        	Iterator agIt = agBuckets.iterator();
	        while(agIt.hasNext()) {
	        	Bucket hist = (Bucket)agIt.next();
	        	InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
	        	System.out.println("  key=> "+hist.getKey()+" "+hist.getDocCount()+" avg: "+avgMetrics.getValue());
	        }
	        */
		} catch(Exception exc) {
			exc.toString();
			logger.severe(exc.toString());
			throw exc;
		} finally {
			esClient.close();
		}
		
		return response;
	}	

	//used to extract event logs that exist between two time intervals (to show logs generated during given event)
	public SearchHit[] getStats(String index, String type, long interval){
		SearchHit[] results = null;
		try {
			esClient.connect();
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-interval)).to(new Date(System.currentTimeMillis())));
			SearchResponse response = esClient.getClient().prepareSearch(index+"*")
			        .setQuery(qb)
			        .setFrom(0).setSize(10000)
			        .addSort("@timestamp",SortOrder.ASC)
			        .execute().actionGet();
			results = response.getHits().getHits();
			
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));  // effectively GetField.getValue()
				String currentTime = (String)result.get("serverCurrentTime");
				//widgetDeviceEventLog
				//singleDevice-growlDeviceEventLog
				//System.out.println("date: "+date.toString()+" ts: "+timestamp+" server current time: "+currentTime);
				//System.out.println(result.toString());
			}
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		} finally {
			esClient.close();
		}
		
		return results;
	}	

	//used to extract event logs that exist between two time intervals (to show logs generated during given event)
	public SearchHit[] getLatestStats(String index, String type, long interval){
		SearchHit[] results = null;
		try {
			esClient.connect();
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			qb.must(QueryBuilders.rangeQuery("@timestamp").from(new Date(System.currentTimeMillis()-interval)).to(new Date(System.currentTimeMillis())));
			SearchResponse response = esClient.getClient().prepareSearch(index+"*")
			        .setQuery(qb)
			        .setFrom(0).setSize(1)
			        .addSort("@timestamp",SortOrder.DESC)
			        .execute().actionGet();
			results = response.getHits().getHits();
			
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));  // effectively GetField.getValue()
				String currentTime = (String)result.get("serverCurrentTime");
				//widgetDeviceEventLog
				//singleDevice-growlDeviceEventLog
				System.out.println("date: "+date.toString()+" ts: "+timestamp+" server current time: "+currentTime);
				//System.out.println(result.toString());
			}
		} catch(Exception exc) {
			logger.severe(exc.toString());
			throw exc;
		} finally {
			esClient.close();
		}
		
		return results;
	}	
	
	public static void main(String[] args) {
		DAOServerStats dao = new DAOServerStats();
//		SearchHit[] results = dao.searchDocument("data", "inform_data", "SN", "CPE1", 1000*1800);
//		SearchHit[] results = dao.searchDocument("sentinel", "log", "SN", "CPE1", 1000*1800);
//		SearchHit[] results = dao.searchDocument("sentinel", "log", "SN", "CPE1", "@logStatus", "OK", 1000*1800);
//		SearchHit[] results = dao.searchDocument("sentinel", "log", "SN", "CPE1", "message", "response log server", 1000*1800);
//		SearchHit[] results = dao.searchDocument("sentinel", "log", "message", "CPE1", "message", "EVENT", new Date(System.currentTimeMillis()-1000*1800), new Date(System.currentTimeMillis()));
//		SearchHit[] results = dao.searchEventLogs("sentinel", "log", "message", "CPE1", "message", "EVENT", new Date(System.currentTimeMillis()-1000*1800), 1);
		//SearchHit[] results = dao.extractEventLogs("sentinel", "log", "SN", "CPE1", new Date(System.currentTimeMillis()-1000*2000), new Date(System.currentTimeMillis()));
		//SearchHit[] results = dao.searchDocument("sentinel", "log", "message", "CPE1", "message", "EVENT", new Date(System.currentTimeMillis()-1000*10*2), new Date(System.currentTimeMillis()));
//		SearchHit[] results = dao.searchDocument("data", "inform_data", "SN", "CPE2");
		SearchHit[] results = dao.getStats("serverstats", "serverstats_data", 1000*20);
		
		dao.calculateParameterValues("serverstats", "serverstats_data",	"cpuUtilisation",
		10, //interval in minutes 		
		new Date(System.currentTimeMillis()-1000*3600), new Date(System.currentTimeMillis()));

	}
	
   public class ESClient {

		private Client client = null;
		private String host;
		private int port;
		
		public ESClient(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public Client getClient() {
			return client;
		}
		
		public void connect() {
			// on startup
			client = new TransportClient()
	        .addTransportAddress(new InetSocketTransportAddress(host, port));
			logger.info("ElasticSearch connection successfull to host: "+host+" port: "+port);
		}
		
		public void close() {
			// on shutdown
			client.close();
			logger.info("ElasticSearch client closed connection closed to host: "+host+" port: "+port);
		}
	}
   
}

