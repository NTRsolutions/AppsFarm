package is.ejb.bl.system.mail;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.alert.AlertLevel;
import is.ejb.bl.monitoring.alert.AlertStatus;
import is.ejb.bl.offerWall.OfferWallGenerator;
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
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;



//http://docs.oracle.com/cd/E19644-01/817-5049/demdb.html
@MessageDriven(name = "MailMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/MailQueue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName="maxSession", propertyValue="20") })
@Pool("mdb-mailQueue-strict-max-pool")
public class MailMDB implements MessageListener {

	//private final static Logger logger = Logger.getLogger(CPEStatusEventConsumer.class.toString());
	@Inject
	private Logger logger;

	private Mailer mailer;
	
	//In the ejb-jar.xml the Bean name was different from the Bean class so I had to give the "name" property to the @MessageDriven annotation.
	//Once I did this the @PostConstruct method was always called as expected. :
	//http://www.java.net/node/680771
	
	public void ejbCreate() throws EJBException {
		logger.info("++++++++++++++++++ post construct for MDB "+Thread.currentThread().getName()+"++++++++++++++++++");
		try {
			mailer = new Mailer();
		} catch (Exception exc) {  
		}  
	}  
	
	public void ejbRemove() throws EJBException {
		logger.info("++++++++++++++++++ pre destroy for MDB "+Thread.currentThread().getName()+"++++++++++++++++++");
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
				MailDataHolder mail = (MailDataHolder)message.getObject();
				logger.info("mail object: "+mail.getEmailAddress()+" "+mail.getMailboxSetup());
				Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, 
							mail.getMailboxSetup().getRealm().getId(), 
							LogStatus.OK, "MDB: "+Thread.currentThread().getName()+
							" sending email to address: "+mail.getEmailAddress()+
							" from: "+mail.getEmailFromAddress()+
							" email type: "+mail.getEmailType()+
							" subject: "+mail.getEmailSubject());
				//System.out.println("MDB: "+Thread.currentThread().getName()+" generating generating composite offer wall from offer wall entity: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName());
				//connect to rServe and perform forecasting
            	mailer.send(mail.getMailboxSetup(), mail.getEmailAddress(), mail.getEmailSubject(), mail.getEmailContent(), mail.getEmailFromAddress());
			}
		} catch (JMSException e) {
			logger.severe(e.toString());
			//throw new RuntimeException(e);
			//TODO shall we persist hazelcast connection within MDB - if so create failover in case the connection was lost (intercept in try/catch and renegotiate connection when error takes place)
		} 
	}

	@PostConstruct
	public void initialize() {
		try {  
		} catch (Exception exc) {  
			exc.printStackTrace();
		}  
	}
	  
	@PreDestroy
	public void cleanup() {
		try {  
		} catch (Exception exc) {  
		}  
	}

}
