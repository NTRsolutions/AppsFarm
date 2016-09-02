package is.ejb.bl.system.mail;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.email.EmailHolder;
import is.ejb.bl.monitoring.operation.SystemOpsAnalyser;
import is.ejb.bl.monitoring.operation.SystemOpsStatsHolder;
import is.ejb.bl.monitoring.server.ServerStats;

import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.aarki.AarkiAPIManager;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.fyber.FyberAPIManager;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersAPIManager;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.DataEntry;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.FindAllOfferGroups;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.Request;
import is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups.Response;
import is.ejb.bl.offerProviders.minimob.MinimobAPIManager;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicAPIManager;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerProviders.woobi.SerDeWoobiProviderConfiguration;
import is.ejb.bl.offerProviders.woobi.WoobiAndroidAPIManager;
import is.ejb.bl.offerProviders.woobi.WoobiIOSAPIManager;
import is.ejb.bl.offerProviders.woobi.WoobiProviderConfig;
import is.ejb.bl.offerWall.OfferFilterManager;
import is.ejb.bl.offerWall.OfferRewardCalculationManager;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.bl.offerWall.persistence.OfferPersistenceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOMonitoringSetup;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.client.Client;

@Stateless
public class MailManager {

	@Inject
	private Logger logger;

	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/MailQueue")
	private Queue queue;

	@Inject
	private DAOMonitoringSetup daoMonitoringSetup;

	@Inject
	private MailTemplatesHolder mailTemplatesHolder;

	private MonitoringSetupEntity mailBoxSetup;
	private Mailer mailer = new Mailer();

	public boolean sendEmail(RealmEntity realm, EmailHolder holder) throws Exception {

		mailBoxSetup = daoMonitoringSetup.findByRealmId(realm.getId());
		if (mailBoxSetup == null) {
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, LogStatus.WARNING,
					Application.SYSTEM_OPS_MONITORING + " aborting system monitoring for realm: " + realm.getId()
							+ " name: " + realm.getName() + " no monitoring setup defined for this realm");
			return false;
		}


		try {
			MailDataHolder mailDataHolder = new MailDataHolder();
			mailDataHolder.setMailboxSetup(mailBoxSetup);
			mailDataHolder.setEmailAddress(holder.getRecipent());
			mailDataHolder.setEmailContent(holder.getContent());
			mailDataHolder.setEmailFromAddress(mailBoxSetup.getMailFromAddress());
			mailDataHolder.setEmailSubject(holder.getTitle());
			mailDataHolder.setEmailType("NORMAL_EMAIL");
			dispatchMail(mailDataHolder);

			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, realm.getId(), LogStatus.OK,
					Application.MAILER + " successfully sent email: " + holder);
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, realm.getId(), LogStatus.ERROR,
					Application.MAILER + " error sending status report for monitoring setup for realm: "
							+ realm.getName() + " error: " + exc.toString() + " for holder: " + holder);
			return false;
		}
	}

	private void dispatchMail(MailDataHolder mail) {
		Connection connection = null;

		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(queue);
			connection.start();
			ObjectMessage message = session.createObjectMessage();
			message.setObject(mail);
			messageProducer.send(message);
		} catch (JMSException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, LogStatus.ERROR,
					Application.SYSTEM_MONITORING + " TIMER error sending email to JMS queue: " + e.toString());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
}
