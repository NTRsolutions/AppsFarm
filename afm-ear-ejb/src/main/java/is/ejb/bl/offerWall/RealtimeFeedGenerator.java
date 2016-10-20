package is.ejb.bl.offerWall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.aarki.AarkiAPIManager;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.clickey.ClickeyAPIManager;
import is.ejb.bl.offerProviders.clickey.ClickeyProviderConfig;
import is.ejb.bl.offerProviders.clickey.SerDeClickeyProviderConfiguration;
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
import is.ejb.bl.offerProviders.hasoffersExt.HasoffersExtAPIManager;
import is.ejb.bl.offerProviders.hasoffersExt.HasoffersExtProviderConfig;
import is.ejb.bl.offerProviders.hasoffersExt.SerDeHasoffersExtProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobAPIManager;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicAPIManager;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerProviders.trialpay.SerDeTrialPayProviderConfiguration;
import is.ejb.bl.offerProviders.trialpay.TrialPayAPIManager;
import is.ejb.bl.offerProviders.trialpay.TrialPayProviderConfig;
import is.ejb.bl.offerProviders.woobi.SerDeWoobiProviderConfiguration;
import is.ejb.bl.offerProviders.woobi.WoobiAndroidAPIManager;
import is.ejb.bl.offerProviders.woobi.WoobiIOSAPIManager;
import is.ejb.bl.offerProviders.woobi.WoobiProviderConfig;
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
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AdProviderEntity;
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
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;


//dynamic java class generation from json:
//http://jsongen.byingtondesign.com/
//https://javafromjson.dashingrocket.com/

@Stateless
public class RealtimeFeedGenerator {

	@Inject
	private Logger logger;

	@Inject
	private OfferPersistenceManager managerOfferPersistence;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOAdProvider daoAdProvider;

	@Inject
	private DAOOfferWall daoOfferWall;

	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;

	@Inject
	private SerDeOfferWallConfiguration serDeOfferWallConfiguration;

	//------------------------------ serde ------------------------------------
	@Inject
	private SerDeTrialPayProviderConfiguration serDeTrialPayProviderConfiguration;

	@Inject
	private SerDeFyberProviderConfiguration serDeFyberProviderConfiguration;
	//------------------------------ offer managers ------------------------------------
	@Inject
	private TrialPayAPIManager trialPayAPIManager;
	
	@Inject
	private FyberAPIManager fyberAPIManager;
	//--------------- offer filter managers -------------------
	@Inject
	private OfferFilterManager offerFilterManager;

	@Inject
	private OfferRewardCalculationManager offerRewardCalculationManager;

	/*
	 * real-time offers feed generator  
	 */
	public OfferWallContent composeOfferWall(OfferWallEntity offerWall,
			RealtimeFeedDataHolder realmtimeFeedDataHolder,
			boolean stickToExistingOffers) throws Exception {
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.COMPOSITE_OFFER_WALL_GENERATION_IDENTIFIED+ " generating composite offer wall: "+offerWall.getName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());

		//reset offer wall stats as we calculate them during each offer walls regeneration
		offerWall.resetOfferWallStats();
		
		//initialise offer filter manager
		offerFilterManager.init(offerWall.getRealm());
		offerRewardCalculationManager.init(offerWall.getRealm());
		
		OfferWallContent offerWallContent = null;
		ArrayList<IndividualOfferWall> listIndividualOfferWalls = null;
		
		if(stickToExistingOffers) { //we only add more offers to the existing pre-generated feed
			offerWallContent = (OfferWallContent)serDeOfferWallContent.deserialize(offerWall.getContent());
			listIndividualOfferWalls = offerWallContent.getOfferWalls();
			logger.info("1 number of existing offer walls: "+offerWallContent.getOfferWalls().size());
		} else {
			offerWallContent = new OfferWallContent(); //we create real-time feed only
			offerWallContent.setCompositeOfferWallName(offerWall.getName());
			offerWallContent.setGenerationTimestamp(System.currentTimeMillis());
			offerWallContent.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
			//generate unique key as combination of offer wall combo name + realmId + timestamp
			String sha1HashW = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+offerWall.getRealm().getId()+System.currentTimeMillis());
			offerWallContent.setId(sha1HashW);
			listIndividualOfferWalls = new ArrayList<IndividualOfferWall>(); //aggregate offer walls
		}
		
		//deserialise config 
		try {
			OfferWallConfiguration offerWallConfiguration = serDeOfferWallConfiguration.deserialize(offerWall.getConfiguration());
			ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = offerWallConfiguration.getConfigurations();
			for(int z=0;z<listSingleOfferWallConfigurations.size();z++) {
				SingleOfferWallConfiguration singleOfferWallConfig = listSingleOfferWallConfigurations.get(z);
				AdProviderEntity adProvider = daoAdProvider.findByName(singleOfferWallConfig.getAdProviderConfigurationName());
				//retrieve specific ad provider configurations to handle api call

				//----------------------------------- TrialPay ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())) {
					try {
						//retrieve offers via api call
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						TrialPayProviderConfig adProviderConfig = serDeTrialPayProviderConfiguration.deserialize(adProvider.getConfiguration());
						ArrayList<Offer> listAllIndividualOffers = 
								trialPayAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										realmtimeFeedDataHolder,
										adProviderConfig);
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.TRIALPAY.toString());
						individualOfferWall.setOffers(listAllIndividualOffers);
						//generate unique key as combination of offer wall combo name + individual offer name + realmId + timestamp 
						String sha1Hash = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+singleOfferWallConfig.getName()+offerWall.getRealm().getId()+System.currentTimeMillis());
						individualOfferWall.setId(sha1Hash);

						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.OK, 
								Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED+ " created single offer wall: "+individualOfferWall.getOfferWallName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());

						listIndividualOfferWalls.add(individualOfferWall);
					} catch(Exception exc) {
						exc.printStackTrace();
						logger.severe(exc.toString());
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.ERROR, 
								Application.SINGLE_OFFER_WALL_GENERATION_FAILED+" error generating single offer walls for offer wall: "+offerWall.getName()+" offer provider: "+adProvider.getCodeName()+" realId: "+offerWall.getRealm().getId()+" error: "+exc.toString());

						//if this is triggered -> multi offer wall will fail which we don't want
						//throw new Exception(exc.toString());
					}
				}
				//----------------------------------- Fyber ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())) {
					try {
						//retrieve offers via api call
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						FyberProviderConfig adProviderConfig = serDeFyberProviderConfiguration.deserialize(adProvider.getConfiguration());
						ArrayList<Offer> listAllIndividualOffers = 
								fyberAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										realmtimeFeedDataHolder,
										adProviderConfig);
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.FYBER.toString());
						individualOfferWall.setOffers(listAllIndividualOffers);
						//generate unique key as combination of offer wall combo name + individual offer name + realmId + timestamp 
						String sha1Hash = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+singleOfferWallConfig.getName()+offerWall.getRealm().getId()+System.currentTimeMillis());
						individualOfferWall.setId(sha1Hash);

						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.OK, 
								Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED+ " created single offer wall: "+individualOfferWall.getOfferWallName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());

						listIndividualOfferWalls.add(individualOfferWall);
					} catch(Exception exc) {
						exc.printStackTrace();
						logger.severe(exc.toString());
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.ERROR, 
								Application.SINGLE_OFFER_WALL_GENERATION_FAILED+" error generating single offer walls for offer wall: "+offerWall.getName()+" offer provider: "+adProvider.getCodeName()+" realId: "+offerWall.getRealm().getId()+" error: "+exc.toString());

						//if this is triggered -> multi offer wall will fail which we don't want
						//throw new Exception(exc.toString());
					}
				}
				
			}

			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()){
				offerFilterManager.getOfferDuplicatesDetector().rejectDuplicateOffersMarkedForRemoval(offerWall, listIndividualOfferWalls);
			}

			//filter out blocked offers
			if(offerFilterManager.getOfferFilterConfiguration().isRejectBlockedOffers()){
				offerFilterManager.filterOutBlockedOffers(offerWall, listIndividualOfferWalls);
			}
			
			//sort offers
			if(offerWall.isSortOffers()) {
				offerFilterManager.sortOffers(offerWall, listIndividualOfferWalls);
			}

			//generate single offer wall listing
			if(offerWall.isGenerateSingleWallListing()) {
				listIndividualOfferWalls = offerFilterManager.generateSingleOfferWallListing(offerWall, listIndividualOfferWalls);
				//append manual offers positioning
				if(offerWall.isAppendPositioning()) {
					offerFilterManager.appendOffersPositioning(offerWall, listIndividualOfferWalls);
				}
			}

			//remove empty offer walls
			if(offerWall.isRemoveEmptyOfferWalls()) {
				offerFilterManager.removeEmptyOfferWalls(offerWall, listIndividualOfferWalls);
			}

			//append offer wall numbering 
			if(offerWall.isAppendOfferNumbering()) {
				offerFilterManager.appendOffersIndexes(offerWall, listIndividualOfferWalls);
			}

			//TODO make sure we only store trialpay offers in db
			//persist offer walls in db 
			//managerOfferPersistence.persistOffers(offerWall, listIndividualOfferWalls, OfferProviderCodeNames.TRIALPAY.toString());

			//set all aggreate offer walls and serialise it into final content for storage
			offerWallContent.setOfferWalls(listIndividualOfferWalls);
			String strOfferWallContent = serDeOfferWallContent.serialize(offerWallContent);
			offerWall.setContent(strOfferWallContent);
			offerWall.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			//daoOfferWall.createOrUpdate(offerWall); //don't persist this as those offers are generated on the fly for specific user!
			
			//return strOfferWallContent;
			logger.info("2 number of existing offer walls: "+offerWallContent.getOfferWalls().size());
			return offerWallContent;
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					"OFFER_WALL_GENERATION error generating offer walls for offer: "+offerWall.getId()+" realId: "+offerWall.getRealm().getId()+" error: "+exc.toString());
			//propagate further as UI is also calling this method
			throw new Exception (exc.toString());
		}
	}

}


/*
public void generateOfferWallsForAllRealms() {
	try {
		List<RealmEntity> listRealms = daoRealm.findAll();
		Iterator i = listRealms.iterator();
		OfferWallEntity offerWall = null;

		while(i.hasNext()) {
			RealmEntity realm = (RealmEntity)i.next();
			try {
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, realm.getId(), 
						LogStatus.OK, 
						" generating offer walls for realm: "+realm.getName()+" id: "+realm.getId());
				
				List<OfferWallEntity> listOfferWalls = daoOfferWall.findAllByRealmId(realm.getId());
				Iterator it = listOfferWalls.iterator();
				while(it.hasNext()) {
					offerWall = (OfferWallEntity)it.next();
					if(offerWall.isActive()) { //process only active offer walls
						generateOfferWall(offerWall);
					}
				}
			} catch(Exception exc) {
				exc.printStackTrace();
				logger.severe(exc.toString());
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
						LogStatus.ERROR, 
						"OFFER_WALL_GENERATION error generating offer walls for realm: "+realm.getName()+" id: "+realm.getId()+" error: "+exc.toString());
			}
		}
		
	} catch(Exception exc) {
		exc.printStackTrace();
		logger.severe(exc.toString());
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
				LogStatus.ERROR, 
				"OFFER_WALL_GENERATION error generating offer walls, error: "+exc.toString());
	}
}
*/
