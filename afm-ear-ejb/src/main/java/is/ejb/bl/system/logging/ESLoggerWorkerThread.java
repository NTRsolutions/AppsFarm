package is.ejb.bl.system.logging;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class ESLoggerWorkerThread extends Thread{
	private String indexName;
	private String typeName;

	protected static final Logger logger = Logger.getLogger(ESLoggerWorkerThread.class.getName());

	//list of batches
	ArrayList<ArrayList<XContentBuilder>> queueBatch = new ArrayList<ArrayList<XContentBuilder>>();
	
	Client client = null;
	Client clientBackup = null; //to stream the same logs to a separate ES instance
	boolean isBackupClientActive = false;
	
	int pushTimeInterval = 5000;
	int batchSizeLimit = 1000;
	int batchNumberLimit = 25; 
	int statusQuoLimit = 2;
	
	int queueElementsLeftHistory = 0;
	int queueElementsLeftRecent = 0;
	int statusQuoCounter = 0;

	String threadName = null;
	
	int rejectedBatchFlushes = 0;
	int rejectedBatchFlushesLimit = 10;
	
	public ESLoggerWorkerThread(Client client,
				Client clientBackup,
				String indexName,
				String typeName,
				int pushTimeInterval, 
				int batchSizeLimit, 
				int batchNumberLimit,
				int statusQuoLimit) {
		this.client = client;
		this.clientBackup = clientBackup;
		if(clientBackup == null) {
			isBackupClientActive = false;
		} else {
			isBackupClientActive = true;
		}
		
		this.indexName = indexName;
		this.typeName = typeName;
		this.pushTimeInterval = pushTimeInterval;
		this.batchSizeLimit = batchSizeLimit;
		this.batchNumberLimit = batchNumberLimit;
		this.statusQuoLimit = statusQuoLimit;
	
		threadName = ""+(int)ESLogger.threadCounter;
		ESLogger.threadCounter = ESLogger.threadCounter+1;
		
		System.out.println("Initialised ESLogger thread: "+threadName+" with following details: ");
		System.out.println("index name: "+indexName);
		System.out.println("type name: "+typeName);
		System.out.println("pushTimeInterval: "+pushTimeInterval);
		System.out.println("batchSizeLimit: "+batchSizeLimit);
		System.out.println("batchNumberLimit: "+batchNumberLimit);
		System.out.println("statusQuoLimit: "+statusQuoLimit);

		setDaemon(true);
		start();
	}
	
	public boolean isQueueNotFull() {
		if(queueBatch.size() < batchNumberLimit) {
			return true;
		} else {
			return false;
		}
	}
	
	public void addToQueue(XContentBuilder log) {
		if(queueBatch.size() > batchNumberLimit) {
			//System.out.println("-> !!!!!!!!!!!!!!!!!!!!! ESLoggerWorkerThread: "+threadName+" exceeded max queue limit: "+batchNumberLimit);
		} else {
			ArrayList<XContentBuilder> queueToAddTo = null;
			if(queueBatch.size() == 0) {
				queueToAddTo = new ArrayList<XContentBuilder>();
				queueBatch.add(queueToAddTo);
			} else {
				queueToAddTo = queueBatch.get(queueBatch.size()-1);
				if(queueToAddTo.size() >= batchSizeLimit) { //if batch is full - add new one
					queueToAddTo = new ArrayList<XContentBuilder>();
					queueBatch.add(queueToAddTo);
				}
			}
			
			//add to queue
			queueToAddTo.add(log);
			//rember the size of the queue
			queueElementsLeftHistory = queueToAddTo.size();
		}
	}

	public void indexLogsBulk() {
		try {
			int batchNumber = 0;
			boolean batchFull = false;
			ArrayList<XContentBuilder> queue = queueBatch.get(0); //always from the oldest
			if(queue.size()==batchSizeLimit) {
				batchFull = true;
				queueBatch.remove(0);
			} else if(queue.size() > 0 && queueBatch.size() > 1) {
				System.out.println("-> ESLoggerWorkerThread: Batch is not fully populated but there are further queues allocated - flushing the not fully allocated batch queue of size: "+queue.size());
				batchFull = true;
				queueBatch.remove(0);
			} else {
				System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" processing aborted, batch number: "+batchNumber+" is not fully populated elements left in queue: "+queue.size());
				queueElementsLeftRecent = queue.size();
				if(queue.size() == queueElementsLeftHistory) {
					statusQuoCounter++;
					System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" incrementing status quo counter: "+statusQuoCounter);
					if(statusQuoCounter >= statusQuoLimit) {
						System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" flusing remaining elements from the queue as status quo counter exceeded value: "+statusQuoLimit);
						//enforce flush anyway
						batchFull = true;
						queueBatch.remove(0);
						statusQuoCounter = 0;
					}
				} else { //force flush after 10 attempts
					rejectedBatchFlushes++;
					if(rejectedBatchFlushes >= rejectedBatchFlushesLimit) {
						System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" forced flush after rejected flushes: "+rejectedBatchFlushes+", batch number: "+batchNumber+" is not fully populated elements left in queue: "+queue.size());
						queueElementsLeftHistory = queue.size(); 
						rejectedBatchFlushes = 0;
					}
				}
				//mzj possibly add this to remove starvation
				//add counter that if exceeds several tries (e.g, 10 - then flush queue anyway)
				//queueElementsLeftHistory = queue.size();
			}
			
			while(batchFull) {
				int counter = 0;
				batchNumber++;
				BulkRequestBuilder bulkRequest = client.prepareBulk();
				BulkRequestBuilder bulkRequestBackup = null;
				if(isBackupClientActive) {
					bulkRequestBackup = clientBackup.prepareBulk();
				}
				
				for(int i=0;i<queue.size();i++) {
					//System.out.println("p "+i);
					try {
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
						
						//check if this index already exists - and not create it and dynamically apply mapping to it
						//-------------- user clicks queue ------------------------
						if(indexName.equals(ESIndexName.ab_clicks.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
								logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserClicks(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
								logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserClicks(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_registrations.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserRegistrations(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserRegistrations(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_support_requests.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserSupportRequests(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexUserSupportRequests(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_mobile_faults.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexMobileFaults(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexMobileFaults(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_wall_selections.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexWallSelections(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexWallSelections(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_wallet_transactions.toString())) {
							//handle primary es system
						    if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexWalletTransactions(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
						    if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexWalletTransactions(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						if(indexName.equals(ESIndexName.ab_crash_reports.toString())) {
							//handle primary es system
							if(!isIndexExist(client, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexCrashReports(client, indexNameWithDate, typeName);
							    getMappings(client, indexNameWithDate, typeName);
						    }
							//handle backup es system
							if(isBackupClientActive && !isIndexExist(clientBackup, indexNameWithDate)) {
						    	logger.info("index: "+indexNameWithDate+" type: "+typeName+" not found, creating new one...");
						        createIndexCrashReports(clientBackup, indexNameWithDate, typeName);
							    getMappings(clientBackup, indexNameWithDate, typeName);
						    }
						}
						
						//get ready for sending for primary es
						bulkRequest.add(client.prepareIndex(indexNameWithDate, typeName).setSource(queue.get(i)));
						//get ready for sending for backup es
						if(isBackupClientActive) {
							bulkRequestBackup.add(clientBackup.prepareIndex(indexNameWithDate, typeName).setSource(queue.get(i)));
						}
						counter++;	
					} catch(Exception exc) {
						exc.printStackTrace();
					}
				}
				queue.clear();
				
				//stream logs to primary ES
				System.out.println("->!!! ESLoggerWorkerThread (primary): "+threadName+" "+indexName+":"+typeName+" processing batch number: "+batchNumber+" bulk operation send: "+counter+" (left queue size: "+queue.size()+") batch size: "+queueBatch.size()+" index: "+indexName+" type: "+typeName);
				BulkResponse bResp = bulkRequest.execute().actionGet();
				System.out.println("->!!! ESLoggerWorkerThread (primary): "+threadName+" "+indexName+":"+typeName+" processing batch number: "+batchNumber+" bulk operation success: "+!bResp.hasFailures()+" index: "+indexName+" type: "+typeName);

				if(isBackupClientActive) {
					//stream logs to backup ES
					System.out.println("->!!! ESLoggerWorkerThread (backup): "+threadName+" "+indexName+":"+typeName+" processing batch number: "+batchNumber+" bulk operation send: "+counter+" (left queue size: "+queue.size()+") batch size: "+queueBatch.size()+" index: "+indexName+" type: "+typeName);
					BulkResponse bRespBackup = bulkRequestBackup.execute().actionGet();
					System.out.println("->!!! ESLoggerWorkerThread (backup): "+threadName+" "+indexName+":"+typeName+" processing batch number: "+batchNumber+" bulk operation success: "+!bRespBackup.hasFailures()+" index: "+indexName+" type: "+typeName);
				}

				//take another batch
				if(queueBatch.size() > 0){ 
					queue = queueBatch.get(0); //always from the oldest
					if(queue.size()==batchSizeLimit) {
						batchFull = true;
						queueBatch.remove(0);
					} else {
						batchFull = false;
					}
				} else {
					batchFull = false;
				}
		
			}
		} catch(Exception exc) {
			exc.toString();
			exc.printStackTrace();
			//System.out.println("Error when connecting and sending logs to ElasticSearch");
		}
	}
	
	public void run() {
		//TODO in here we should place the initialisation of node client
		
		while(!Thread.currentThread().isInterrupted()) {
			try {
				if(queueBatch.size() >0) {
					System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" pushing logs queue batch size: "+queueBatch.size()+" index: "+indexName+" type: "+typeName);
					indexLogsBulk();
				} else {
					System.out.println("-> ESLoggerWorkerThread: "+threadName+" "+indexName+":"+typeName+" pushing logs stopped, queue batch size: "+queueBatch.size()+" index: "+indexName+" type: "+typeName);
				}
				Thread.sleep((long)pushTimeInterval);
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		}
	}
	
	public void getMappings(Client clientRef, String indexName, String typeName) {
	    logger.info("current mapping structure before data insertion for: "+indexName+" "+typeName);

	    ClusterState clusterState = clientRef.admin().cluster().prepareState().setIndices(indexName).execute().actionGet().getState();
	    IndexMetaData inMetaData = clusterState.getMetaData().index(indexName);
	    MappingMetaData metad = inMetaData.mapping(typeName);

	    if (metad != null) {
	        try {
	            String structure = metad.getSourceAsMap().toString();
	            logger.info(structure);

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

	private void createIndexUserClicks(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("rewardType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("offerName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("offerProviderName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("networkName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("redirectUrl")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("eventType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("internalTransactionId")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("countryCode")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
 		            .startObject("carrierName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("userEventCategory")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("profit")
		                .field("type", "double")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		            .startObject("instantReaward")
		                .field("type", "boolean")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("applicationName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
   		            .startObject("gaid")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
   		            .startObject("idfa")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
		            .startObject("testMode")
		                .field("type", "boolean")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
  	                .startObject("customRewardValueCurrency")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
		        .endObject();
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
		    clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create UserClicks index...");
	    }
	    //try put mapping after index creation
	    /*
	     * PutMappingResponse response = null; try { response =
	     * client.admin().indices() .preparePutMapping(index) .setType(type)
	     * .setSource(typemapping.string()) .execute().actionGet(); } catch
	     * (ElasticSearchException e) { e.printStackTrace(); } catch
	     * (IOException e) { e.printStackTrace(); }
	     */
	} 

	private void createIndexUserRegistrations(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("systemInfo")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumber")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumberExtension")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("email")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("locale")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ageRange")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("male")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("countryCode")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("referralCode")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("rewardTypeName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("gaid")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("idfa")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("applicationName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		             
		           .endObject()
		        .endObject();
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
		    clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create UserRegistrations index...");
	    }

	    //try put mapping after index creation
	    /*
	     * PutMappingResponse response = null; try { response =
	     * client.admin().indices() .preparePutMapping(index) .setType(type)
	     * .setSource(typemapping.string()) .execute().actionGet(); } catch
	     * (ElasticSearchException e) { e.printStackTrace(); } catch
	     * (IOException e) { e.printStackTrace(); }
	     */
	} 

	private void createIndexUserSupportRequests(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("errorCategory")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumber")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumberExtension")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("email")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("locale")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("networkName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		           .endObject()
		        .endObject();
		        
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
	    	clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();	    
	    } catch(Exception exc) {
	    	logger.severe("Unable to create UserSupportRequests index...");
	    }

	    //try put mapping after index creation
	    /*
	     * PutMappingResponse response = null; try { response =
	     * client.admin().indices() .preparePutMapping(index) .setType(type)
	     * .setSource(typemapping.string()) .execute().actionGet(); } catch
	     * (ElasticSearchException e) { e.printStackTrace(); } catch
	     * (IOException e) { e.printStackTrace(); }
	     */
	} 

	private void createIndexMobileFaults(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("phoneNumber")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumberExtension")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("email")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("locale")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("networkName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		           .endObject()
		        .endObject();
		        
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
		    clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create MobileFaults index...");
	    }

	    //try put mapping after index creation
	    /*
	     * PutMappingResponse response = null; try { response =
	     * client.admin().indices() .preparePutMapping(index) .setType(type)
	     * .setSource(typemapping.string()) .execute().actionGet(); } catch
	     * (ElasticSearchException e) { e.printStackTrace(); } catch
	     * (IOException e) { e.printStackTrace(); }
	     */
	} 

	private void createIndexWallSelections(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("serverName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("systemInfo")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("miscData")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("wallRewardType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("wallRewardType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("wallGeo")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("wallDeviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumberExt")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("wallId")
		                .field("type", "integer")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("userId")
		                .field("type", "integer")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("networkName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumber")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumberExtension")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("email")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("locale")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		           .endObject()
		        .endObject();
		        
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
	    	clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create WallSelections index...");
	    }

	    //try put mapping after index creation
	    /*
	     * PutMappingResponse response = null; try { response =
	     * client.admin().indices() .preparePutMapping(index) .setType(type)
	     * .setSource(typemapping.string()) .execute().actionGet(); } catch
	     * (ElasticSearchException e) { e.printStackTrace(); } catch
	     * (IOException e) { e.printStackTrace(); }
	     */
	} 

	private void createIndexWalletTransactions(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("rewardType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("offerName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("offerProviderName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("networkName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("redirectUrl")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("eventType")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("internalTransactionId")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("countryCode")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
 		            .startObject("carrierName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("userEventCategory")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("profit")
		                .field("type", "double")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		            .startObject("instantReaward")
		                .field("type", "boolean")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("applicationName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
		            .startObject("testMode")
		                .field("type", "boolean")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		           .endObject()
		        .endObject();
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
		    clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create WalletTransactions index...");
	    }

	} 
	
	private void createIndexCrashReports(Client clientRef, String indexName, String indexType) {
		logger.info("creating index for index name: "+indexName+" type: "+indexType);
	    String mappingstring = null;
	    XContentBuilder builder = null;

	    try {
		    try {
		        builder = XContentFactory.jsonBuilder();
		        builder.startObject()
		        .startObject("properties")
		            .startObject("phoneNumberExtension")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("phoneNumber")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceInfo")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("deviceVersion")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("applicationName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("applicationVersion")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("breadcrumb")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("stackTrace")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("ipAddress")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		            .startObject("serverName")
		                .field("type", "string")
		                .field("store", "yes")
		                .field("index", "not_analyzed")
		             .endObject()
		        .endObject();
		        mappingstring = builder.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	        logger.severe(e1.toString());
	    }

	    try {
		    clientRef.admin().indices().create(new CreateIndexRequest(indexName).mapping(indexType, builder)).actionGet();
	    } catch(Exception exc) {
	    	logger.severe("Unable to create CrashReports index...");
	    }
	} 
		
	private void deleteIndex(Client clientRef, String indexName) {
	    try {
	    	logger.info("deleting index: "+indexName);
	        DeleteIndexResponse delete = clientRef.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
	        if (!delete.isAcknowledged()) {
	        } else {
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	logger.severe(e.toString());
	    }
	} 
	
	private boolean isIndexExist(Client clientRef, String indexName) {
	    ActionFuture<IndicesExistsResponse> exists = clientRef.admin().indices().exists(new IndicesExistsRequest(indexName));
	    IndicesExistsResponse actionGet = exists.actionGet();

	    return actionGet.isExists();
	} 

}
