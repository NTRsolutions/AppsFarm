package is.ejb.bl.timers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferCommand;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.crf.CRFManager;
import is.ejb.bl.monitoring.server.ServerStats;

import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.Mailer;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.PropertyEntity;
import is.ejb.dl.entities.RealmEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

//http://docs.oracle.com/javaee/6/tutorial/doc/gipvi.html
//@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@Startup
public class TimerCRF {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	//http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
    @Resource
    TimerService timerService;

	@Inject 
	private DAOProperty daoProperty;

    private Timer crfTimer = null;
    private int crfTriggerInterval = 24; //in h
    private String masterServerIp = ""; //we let generate offers ony by master server within the cluster
    private CRFManager crfManager = null;

	@Inject
	private DAOBlockedOffers daoBlockedOffers;
	@Inject
	private SerDeBlockedOffers serDeBlockedOffers;

    @PostConstruct
    public void initialize(){
		try {
			//sleep to let ES loggers initialise
			Thread.sleep(TimerStartupConfig.InitWaitTime);
			crfTriggerInterval = getPropertyInt("crfIntervals", "15");
			masterServerIp = getProperty("masterServerIp", "127.0.0.1");
			if(isMasterServer(masterServerIp)) {
				//init device health monitoring timer
		    	TimerConfig tc = new TimerConfig(TimerType.TIMER_CRF, false);
		        ScheduleExpression expression = new ScheduleExpression();
				//set monitoring interval
			    expression.minute("*").minute("*/"+ crfTriggerInterval +"/").hour("*");
			    //using calendar expression
			    crfTimer = timerService.createCalendarTimer(expression,tc);
			    crfManager = new CRFManager(Application.getLogServerAddress(), Application.getLogServerName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
    }

    //http://stackoverflow.com/questions/14441366/error-invoking-timeout-for-timer-could-not-obtain-lock-within-5minutes-at-ejb
    @Timeout
    @AccessTimeout(value = 20, unit = TimeUnit.MINUTES)
    public void timeout(Timer timer) {
        logger.info("----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

        if(timer.getInfo().equals(TimerType.TIMER_CRF)) {
        	if(Application.getCRFEnabled()) {
                logger.info("!!!!----TIMER INVOKED timer type: "+timer.getInfo()+" next timeout: "+timer.getTimeRemaining());

            	Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
            			LogStatus.OK, 
            			Application.CRF_TRIGGER_ACTIVITY+" TIMER CRF");
        		try {
        			List<RealmEntity> listRealms = daoRealm.findAll();
        			Iterator i = listRealms.iterator();

        			while(i.hasNext()) {
        				RealmEntity realm = (RealmEntity)i.next();
        				try {
        					if(realm.isCrfEnabled()) {
            					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, realm.getId(), 
            							LogStatus.OK, 
            							Application.CRF_TRIGGER_ACTIVITY+" TIMER triggering crf for realm: "+realm.getName()+" id: "+realm.getId());
            					
            					logger.info("triggering crf for realm: "+realm.getName());
            					//identify offers to block
            					ArrayList<BlockedOffer> listOffersToBlock = crfManager.filterOffers(realm.getCrfMinimalClickRate(), realm.getCrfCRThreshold(), realm.getId());
            					//get already blocked offers list and update it with new offers to block
            					BlockedOffersEntity blockedOffersEntity = daoBlockedOffers.findByRealmId(realm.getId());
            					BlockedOffers blockedOffers = serDeBlockedOffers.deserialize(blockedOffersEntity.getContent());
            					ArrayList<BlockedOffer> listBlockedOffers = blockedOffers.getListBlockedOffers();
            					for(int x=0;x<listOffersToBlock.size();x++) {
            						BlockedOffer offerToBlock = listOffersToBlock.get(x);
            						blockOffer(listBlockedOffers, offerToBlock, realm.getId());
            					}
            					//persist updated blocked offers list
            					logger.info("persisting configuration");
            					blockedOffersEntity = daoBlockedOffers.findByRealmId(realm.getId());
            					blockedOffers = new BlockedOffers();
            					blockedOffers.setListBlockedOffers(listBlockedOffers);

            					String strBlockedOffers = serDeBlockedOffers.serialize(blockedOffers);
            					blockedOffersEntity.setContent(strBlockedOffers);
            					blockedOffersEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
            					blockedOffersEntity = daoBlockedOffers.createOrUpdate(blockedOffersEntity);
        					}
        				} catch(Exception exc) {
        					exc.printStackTrace();
        					logger.severe(exc.toString());
        					Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, realm.getId(), 
        							LogStatus.ERROR, 
        							Application.CRF_TRIGGER_ACTIVITY+" TIMER error triggering crf for realm: "+realm.getName()+" id: "+realm.getId()+" error: "+exc.toString());
        				}
        			}
        			
        		} catch(Exception exc) {
        			exc.printStackTrace();
        			logger.severe(exc.toString());
					Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, -1, 
							LogStatus.ERROR, 
							Application.CRF_TRIGGER_ACTIVITY+" TIMER error triggering crf - error: "+exc.toString());
        		}
            } else {
            	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, -1, 
            			LogStatus.OK, 
            			Application.CRF_TRIGGER_ACTIVITY+" TIMER crf disabled");
            }
        }
    }
    
    //if offer was blocked but its status changed to unblock - unblock it and remove from the list
    //if offer blocked and status changed to block - update it
    //if offer not blocked and status changed to block - add it to the list
	private void blockOffer(ArrayList<BlockedOffer> listBlockedOffers, BlockedOffer offerToBlock, int realmId) {
		boolean addOfferToBlockList = true;
		
		for(int i=0;i<listBlockedOffers.size();i++) {
			BlockedOffer alreadyBlockedOffer = listBlockedOffers.get(i);
			if(alreadyBlockedOffer.getSourceId().equals(offerToBlock.getSourceId()) &&
					alreadyBlockedOffer.getAdProviderCodeName().equals(offerToBlock.getAdProviderCodeName()) &&
						offerToBlock.getCommand().equals(BlockedOfferCommand.BLOCK.toString())) { //only update existing blocked offer settings

				addOfferToBlockList = false;
				//logger.info("<<<<<<< updating already blocked offer : "+offerToBlock.getSourceId()+" "+ offerToBlock.getTitle()+" "+offerToBlock.getAdProviderCodeName());
	        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
	        			realmId, 
	        			LogStatus.OK, 
	        			Application.CRF_BLOCK_ACTIVITY+" "+
	        			Application.CRF_UPDATE_BLOCK+
	        			" updating already blocked offer: "+offerToBlock.getTitle()+ 
	        			" conversions: "+offerToBlock.getSumConversions()+
	        			" clicks: "+offerToBlock.getSumClicks()+
	        			" cvr: "+offerToBlock.getConvRatio());
	
				alreadyBlockedOffer.setSumClicks(offerToBlock.getSumClicks());
				alreadyBlockedOffer.setSumConversions(offerToBlock.getSumConversions());
				alreadyBlockedOffer.setConvRatio(offerToBlock.getConvRatio());
				alreadyBlockedOffer.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
				
				return; 
			} else if(alreadyBlockedOffer.getSourceId().equals(offerToBlock.getSourceId()) &&
					alreadyBlockedOffer.getAdProviderCodeName().equals(offerToBlock.getAdProviderCodeName()) &&
					offerToBlock.getCommand().equals(BlockedOfferCommand.UNBLOCK.toString()) &&
					!alreadyBlockedOffer.getBlockType().equals(BlockedOfferType.MANUAL.toString())) { //remove from blocked list only crf-based offers

				//logger.info("!!!!!!!!!! removing already blocked offer : "+offerToBlock.getSourceId()+" "+ offerToBlock.getTitle()+" "+offerToBlock.getAdProviderCodeName());
	        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
	        			realmId, 
	        			LogStatus.OK, 
	        			Application.CRF_BLOCK_ACTIVITY+" "+
	        			Application.CRF_REMOVE_FROM_BLOCK+
	        			" removing offer from blocked list: "+offerToBlock.getTitle()+ 
	        			" conversions: "+offerToBlock.getSumConversions()+
	        			" clicks: "+offerToBlock.getSumClicks()+
	        			" cvr: "+offerToBlock.getConvRatio());
				listBlockedOffers.remove(i);
				return;
			}
		}

		//if offer was not found on the list of already blocked offers
		if(offerToBlock.getCommand().equals(BlockedOfferCommand.BLOCK.toString())) {
			addOfferToBlockList = true;
		} else if(offerToBlock.getCommand().equals(BlockedOfferCommand.UNBLOCK.toString())) { 
			addOfferToBlockList = false;
		}
		
		if(addOfferToBlockList) {
			logger.info(">>>>>>>> adding new offer to block: "+offerToBlock.getSourceId()+" "+ offerToBlock.getTitle()+" "+offerToBlock.getAdProviderCodeName());
        	Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY, 
        			realmId, 
        			LogStatus.OK, 
        			Application.CRF_BLOCK_ACTIVITY+" "+
        			Application.CRF_ADD_TO_BLOCK+
        			" adding offer to blocked list: "+offerToBlock.getTitle()+ 
        			" conversions: "+offerToBlock.getSumConversions()+
        			" clicks: "+offerToBlock.getSumClicks()+
        			" cvr: "+offerToBlock.getConvRatio());

			offerToBlock.setTimestamp(new Timestamp(System.currentTimeMillis()));
			offerToBlock.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
			offerToBlock.setBlockType(BlockedOfferType.CRF.toString());
			listBlockedOffers.add(offerToBlock);
			
		}
	}
	
    //this is used to read monitoring interval parameter from db (cannot do it from Application class as it is load later than this one)
    private int getPropertyInt(String name, String defvalue) {
        String p = getProperty(name, defvalue);
        try {
            if (p == null || p.equals("")) {
                return Integer.parseInt(defvalue);
            }
            return Integer.parseInt(p);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getProperty(String name, String defvalue) {
        try {
            PropertyEntity pl = daoProperty.findByPK(0, PropertyEntity.TYPE_APPLICATION, name);
            return pl.getValue();
        } catch (Exception ex) {
            return defvalue;
        }
    }

    private boolean isMasterServer(String masterServerIp) {
        Enumeration<NetworkInterface> n;
		try {
			n = NetworkInterface.getNetworkInterfaces();
	        for (; n.hasMoreElements();)
	        {
	            NetworkInterface e = n.nextElement();
	            Enumeration<InetAddress> a = e.getInetAddresses();
	            for (; a.hasMoreElements();)
	            {
	                InetAddress addr = a.nextElement();
	                logger.info("detected host address:  " + addr.getHostAddress());
					Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
							LogStatus.OK, 
							"MASTER_ELECTION "+TimerType.TIMER_OFFER_WALL_GENERATION+" detected host address: "+addr.getHostAddress());
					if(addr.getHostAddress().equals(masterServerIp)) {
						logger.info("MASTER_ELECTION SELECTED "+TimerType.TIMER_OFFER_WALL_GENERATION+" server with IP: "+masterServerIp+" elected as a master server");
						Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
								LogStatus.OK, 
								"MASTER_ELECTION SELECTED "+TimerType.TIMER_OFFER_WALL_GENERATION+" server with IP: "+masterServerIp+" elected as a master server");
						return true;
					}
	            }
	        }
	 	} catch (SocketException e1) {
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, 
					LogStatus.ERROR, 
					"MASTER_ELECTION "+TimerType.TIMER_OFFER_WALL_GENERATION+" error during master server election: "+e1.toString());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
    	return false;
    }


}