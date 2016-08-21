package is.ejb.bl.offerWall;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.alert.AlertLevel;
import is.ejb.bl.monitoring.alert.AlertStatus;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAODeviceAlertsConfiguration;
import is.ejb.dl.entities.DeviceAlertsConfigurationEntity;
import is.ejb.dl.entities.OfferWallEntity;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jboss.ejb3.annotation.Pool;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;



//http://docs.oracle.com/cd/E19644-01/817-5049/demdb.html
@MessageDriven(name = "OfferWallGenerationMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/OfferWallGenerationQueue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName="maxSession", propertyValue="20") })
@Pool("mdb-offerWallGenerationQueue-strict-max-pool")
public class OfferWallGeneratorMDB implements MessageListener {

	//private final static Logger logger = Logger.getLogger(CPEStatusEventConsumer.class.toString());
	@Inject
	private Logger logger;

	@Inject
	private OfferWallGenerator offerWallManager;

	//In the ejb-jar.xml the Bean name was different from the Bean class so I had to give the "name" property to the @MessageDriven annotation.
	//Once I did this the @PostConstruct method was always called as expected. :
	//http://www.java.net/node/680771
	@PostConstruct
	public void initialize() {
		//create connection to flume/hbase
		try {  
		} catch (Exception exc) {  
		}  
	}
	  
	@PreDestroy
	public void cleanup() {
		//create connection to flume/hbase
		try {  
		} catch (Exception exc) {  
		}  
	}
	
	/**
	 * @see MessageListener#onMessage(Message)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onMessage(Message rcvMessage) {
		//batch of alerts to send to es
		ArrayList<XContentBuilder> queueBatch = new ArrayList<XContentBuilder>();
		ObjectMessage message = null;
		try {
			if (rcvMessage instanceof ObjectMessage) {
				message = (ObjectMessage) rcvMessage;
				OfferWallEntity offerWall = (OfferWallEntity)message.getObject();
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "MDB: "+Thread.currentThread().getName()+" generating generating composite offer wall from offer wall entity: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName());
				System.out.println("MDB: "+Thread.currentThread().getName()+" generating generating composite offer wall from offer wall entity: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName());
				//connect to rServe and perform forecasting

				try {
	            	//generate offer wall for every  active offerwall configuration (on every realm)
	            	offerWallManager.generateOfferWall(offerWall);
				} catch(Exception exc) {
					logger.severe(exc.toString());
					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
							offerWall.getRealm().getId(), 
							LogStatus.ERROR, 
							Application.COMPOSITE_OFFER_WALL_GENERATION_FAILED+" MDB: "+Thread.currentThread().getName()+" error generating composite offer wall from offer wall entity: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+
							" error: "+exc.toString());
					exc.printStackTrace();
				}
			}
		} catch (JMSException e) {
			logger.severe(e.toString());
			//throw new RuntimeException(e);
			//TODO shall we persist hazelcast connection within MDB - if so create failover in case the connection was lost (intercept in try/catch and renegotiate connection when error takes place)
		} 
	}
	
	public void ejbCreate() throws EJBException {
		logger.info("++++++++++++++++++ post construct for MDB "+Thread.currentThread().getName()+"++++++++++++++++++");
		try {  
		} catch (Exception exc) {  
		}  
	}  
	
	public void ejbRemove() throws EJBException {
		logger.info("++++++++++++++++++ pre destroy for MDB "+Thread.currentThread().getName()+"++++++++++++++++++");
		try {  
		} catch (Exception exc) {  
		}  
	}
	
}
