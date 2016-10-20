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
import is.ejb.bl.offerProviders.hasoffersNativex.HasoffersNativexAPIManager;
import is.ejb.bl.offerProviders.hasoffersNativex.HasoffersNativexProviderConfig;
import is.ejb.bl.offerProviders.hasoffersNativex.SerDeHasoffersNativexProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffersVC.HasoffersVCAPIManager;
import is.ejb.bl.offerProviders.hasoffersVC.HasoffersVCProviderConfig;
import is.ejb.bl.offerProviders.hasoffersVC.SerDeHasoffersVCProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobAPIManager;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.personaly.PersonalyAPIManager;
import is.ejb.bl.offerProviders.personaly.PersonalyProviderConfig;
import is.ejb.bl.offerProviders.personaly.SerDePersonalyProviderConfiguration;
import is.ejb.bl.offerProviders.snapdeal.SerDeSnapdealProviderConfiguration;
import is.ejb.bl.offerProviders.snapdeal.SnapdealAPIManager;
import is.ejb.bl.offerProviders.snapdeal.SnapdealProviderConfig;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicAPIManager;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;


//dynamic java class generation from json:
//http://jsongen.byingtondesign.com/
//https://javafromjson.dashingrocket.com/

@Stateless
public class OfferWallGenerator {

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
	private SerDeClickeyProviderConfiguration serDeClickky;
	@Inject
	private SerDeWoobiProviderConfiguration serDeWoobi;
	@Inject
	private SerDeSupersonicProviderConfiguration serDeSupersonic;
	@Inject
	private SerDeAarkiProviderConfiguration serDeAarki;
	@Inject
	private SerDeMinimobProviderConfiguration serDeMinimob;
	@Inject
	private SerDeHasoffersProviderConfiguration serDeHasoffers;
	@Inject
	private SerDeFyberProviderConfiguration serDeFyber;
	@Inject
	private SerDeHasoffersExtProviderConfiguration serDeHasoffersExt;
	@Inject
	private SerDeHasoffersNativexProviderConfiguration serDeHasoffersNativex;
	@Inject
	private SerDeHasoffersVCProviderConfiguration serDeHasoffersVC;
	@Inject
	private SerDePersonalyProviderConfiguration serDePersonaly;
	@Inject
	private SerDeSnapdealProviderConfiguration serDeSnapdeal;

	//------------------------------ offer managers ------------------------------------
	@Inject
	private ClickeyAPIManager clickeyAPIManager;
	@Inject
	private WoobiIOSAPIManager woobiIOSAPIManager;
	@Inject
	private WoobiAndroidAPIManager woobiAndroidAPIManager;
	@Inject
	private SupersonicAPIManager supersonicAPIManager;
	@Inject
	private AarkiAPIManager aarkiAPIManager;
	@Inject
	private MinimobAPIManager minimobAPIManager;
	@Inject
	private HasoffersAPIManager hasoffersAPIManager;
	@Inject
	private FyberAPIManager fyberAPIManager;
	@Inject
	private HasoffersExtAPIManager hasoffersExtAPIManager;	
	@Inject
	private HasoffersNativexAPIManager hasoffersNativexAPIManager;	
	@Inject
	private HasoffersVCAPIManager hasoffersVCAPIManager;	
	@Inject
	private PersonalyAPIManager personalyAPIManager;	
	@Inject
	private SnapdealAPIManager snapdealAPIManager;	
		
	//--------------- offer filter managers -------------------
	@Inject
	private OfferFilterManager offerFilterManager;

	@Inject
	private OfferRewardCalculationManager offerRewardCalculationManager;

	//------------------------------- generate multi-offer-wall --------------------------------------
	public String generateOfferWall(OfferWallEntity offerWall) throws Exception {
		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
				LogStatus.OK, 
				Application.COMPOSITE_OFFER_WALL_GENERATION_IDENTIFIED+ " generating composite offer wall: "+offerWall.getName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());

		//reset offer wall stats as we calculate them during each offer walls regeneration
		offerWall.resetOfferWallStats();
		
		//initialise offer filter manager
		offerFilterManager.init(offerWall.getRealm());
		offerRewardCalculationManager.init(offerWall.getRealm());
		
		OfferWallContent offerWallContent = new OfferWallContent(); //global object in which we store found offers
		offerWallContent.setCompositeOfferWallName(offerWall.getName());
		offerWallContent.setGenerationTimestamp(System.currentTimeMillis());
		offerWallContent.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
		//generate unique key as combination of offer wall combo name + realmId + timestamp
		String sha1HashW = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+offerWall.getRealm().getId()+System.currentTimeMillis());
		offerWallContent.setId(sha1HashW);

		ArrayList<IndividualOfferWall> listIndividualOfferWalls = new ArrayList<IndividualOfferWall>(); //aggregate offer walls
		//deserialise config 
		try {
			OfferWallConfiguration offerWallConfiguration = serDeOfferWallConfiguration.deserialize(offerWall.getConfiguration());
			ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = offerWallConfiguration.getConfigurations();
			for(int z=0;z<listSingleOfferWallConfigurations.size();z++) {
				SingleOfferWallConfiguration singleOfferWallConfig = listSingleOfferWallConfigurations.get(z);
				AdProviderEntity adProvider = daoAdProvider.findByName(singleOfferWallConfig.getAdProviderConfigurationName());
				//retrieve specific ad provider configurations to handle api call

				//----------------------------------------------------------------------------------
				//----------------------------------- SNAPDEAL ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.SNAPDEAL.toString())) {
//					try {
//						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
//						//retrieve offers via api call
//						SnapdealProviderConfig adProviderConfig = serDeSnapdeal.deserialize(adProvider.getConfiguration());
//						
//						//or just don't generate failing offer wall?
//						snapdealAPIManager.getOffers(offerWall, 
//							individualOfferWall,
//							offerFilterManager,
//							offerRewardCalculationManager,
//							adProviderConfig);
//
//						//generate unique key as combination of offer wall combo name + individual offer name + realmId + timestamp 
//						String sha1Hash = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+singleOfferWallConfig.getName()+offerWall.getRealm().getId()+System.currentTimeMillis());
//						individualOfferWall.setId(sha1Hash);
//
//						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
//								LogStatus.OK, 
//								Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED+ " created single offer wall: "+individualOfferWall.getOfferWallName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());
//					} catch(Exception exc) {
//						exc.printStackTrace();
//						logger.severe(exc.toString());
//						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
//								LogStatus.ERROR, 
//								Application.SINGLE_OFFER_WALL_GENERATION_FAILED+" error generating single offer walls for offer wall: "+offerWall.getName()+" offer provider: "+adProvider.getCodeName()+" realId: "+offerWall.getRealm().getId()+" error: "+exc.toString());
//						
//						//if this is triggered -> multi offer wall will fail which we don't want
//						//throw new Exception(exc.toString());
//					}
				}

				//----------------------------------------------------------------------------------
				//----------------------------------- PERSONALY ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())) {
					try {
						//all offer ids from all ACTIVE!!! offer groups as we only take into account active groups
						ArrayList<Integer> listOfferIdsPool = new ArrayList<Integer>(); //this is the global pool of obtained offer Ids from which to select ones for offer wall
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						PersonalyProviderConfig adProviderConfig = serDePersonaly.deserialize(adProvider.getConfiguration());

						//or just don't generate failing offer wall?
						ArrayList<Offer> listAllIndividualOffers = 
								personalyAPIManager.getOffers(offerWall, 
								individualOfferWall,
								offerFilterManager,
								offerRewardCalculationManager,
								adProviderConfig,
								adProviderConfig.getRecordsPerPage());

						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.PERSONALY.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- HASOFFERS VC------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())) {
					try {
						//all offer ids from all ACTIVE!!! offer groups as we only take into account active groups
						ArrayList<Integer> listOfferIdsPool = new ArrayList<Integer>(); //this is the global pool of obtained offer Ids from which to select ones for offer wall
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						HasoffersVCProviderConfig adProviderConfig = serDeHasoffersVC.deserialize(adProvider.getConfiguration());

						//or just don't generate failing offer wall?
						ArrayList<Offer> listAllIndividualOffers = 
								hasoffersVCAPIManager.findAllOffers(offerWall, 
								individualOfferWall,
								offerFilterManager,
								offerRewardCalculationManager,
								adProviderConfig,
								singleOfferWallConfig);

						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS_VC.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- HASOFFERS NATIVEX------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())) {
					try {
						//all offer ids from all ACTIVE!!! offer groups as we only take into account active groups
						ArrayList<Integer> listOfferIdsPool = new ArrayList<Integer>(); //this is the global pool of obtained offer Ids from which to select ones for offer wall
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						HasoffersNativexProviderConfig adProviderConfig = serDeHasoffersNativex.deserialize(adProvider.getConfiguration());

						//or just don't generate failing offer wall?
						ArrayList<Offer> listAllIndividualOffers = 
								hasoffersNativexAPIManager.findAllOffers(offerWall, 
								individualOfferWall,
								offerFilterManager,
								offerRewardCalculationManager,
								adProviderConfig,
								singleOfferWallConfig);

						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- HASOFFERS EXT------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())) {
					try {
						//all offer ids from all ACTIVE!!! offer groups as we only take into account active groups
						ArrayList<Integer> listOfferIdsPool = new ArrayList<Integer>(); //this is the global pool of obtained offer Ids from which to select ones for offer wall
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						HasoffersExtProviderConfig adProviderConfig = serDeHasoffersExt.deserialize(adProvider.getConfiguration());

						//or just don't generate failing offer wall?
						ArrayList<Offer> listAllIndividualOffers = 
								hasoffersExtAPIManager.findAllOffers(offerWall, 
								individualOfferWall,
								offerFilterManager,
								offerRewardCalculationManager,
								adProviderConfig,
								singleOfferWallConfig);

						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS_EXT.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- CLICKKY ------------------------------------
				logger.info("processing ad provider configuration: "+adProvider.getName()+" "+adProvider.getCodeName()+" "+adProvider.getConfiguration());
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())) {
					try {
						//retrieve offers via api call
						ClickeyProviderConfig adProviderConfig = serDeClickky.deserialize(adProvider.getConfiguration());
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						ArrayList<Offer> listAllIndividualOffers = 
								clickeyAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig,
										singleOfferWallConfig.getNumberOfOffers());
						//ArrayList<Offer> listSelectedIndividualOffers = moceanAPIManager.selectOffers(listAllIndividualOffers,singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.CLICKKY.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- WOOBI_IOS ------------------------------------
				logger.info("processing ad provider configuration: "+adProvider.getName()+" "+adProvider.getCodeName()+" "+adProvider.getConfiguration());
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())) {
					try {
						//retrieve offers via api call
						WoobiProviderConfig adProviderConfig = serDeWoobi.deserialize(adProvider.getConfiguration());
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						ArrayList<Offer> listAllIndividualOffers = 
								woobiIOSAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig,
										singleOfferWallConfig.getNumberOfOffers());
						//ArrayList<Offer> listSelectedIndividualOffers = moceanAPIManager.selectOffers(listAllIndividualOffers,singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.WOOBI_IOS.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- WOOBI_ANDROID ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())) {
					try {
						//retrieve offers via api call
						WoobiProviderConfig adProviderConfig = serDeWoobi.deserialize(adProvider.getConfiguration());
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						ArrayList<Offer> listAllIndividualOffers = 
								woobiAndroidAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig,
										singleOfferWallConfig.getNumberOfOffers());
						//ArrayList<Offer> listSelectedIndividualOffers = moceanAPIManager.selectOffers(listAllIndividualOffers,singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.WOOBI_ANDROID.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- SUPERSONIC ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())) {
					try {
						//retrieve offers via api call
						SupersonicProviderConfig adProviderConfig = serDeSupersonic.deserialize(adProvider.getConfiguration());
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						ArrayList<Offer> listAllIndividualOffers = 
								supersonicAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig,
										singleOfferWallConfig.getNumberOfOffers());
						//ArrayList<Offer> listSelectedIndividualOffers = moceanAPIManager.selectOffers(listAllIndividualOffers,singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.SUPERSONIC.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- AARKI ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.AARKI.toString())) {
					try {
						//retrieve offers via api call
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						AarkiProviderConfig adProviderConfig = serDeAarki.deserialize(adProvider.getConfiguration());
						ArrayList<Offer> listAllIndividualOffers = 
								aarkiAPIManager.getOffers(offerWall,
										individualOfferWall,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig.getPlacementId(),
										adProviderConfig.getNumberOfPulledOffers(),
										singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.AARKI.toString());
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

				//----------------------------------------------------------------------------------
				//----------------------------------- HASOFFERS ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())) {
					try {
						//all offer ids from all ACTIVE!!! offer groups as we only take into account active groups
						ArrayList<Integer> listOfferIdsPool = new ArrayList<Integer>(); //this is the global pool of obtained offer Ids from which to select ones for offer wall
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						HasoffersProviderConfig adProviderConfig = serDeHasoffers.deserialize(adProvider.getConfiguration());
						//get credentials for connecting with ad provider api
						String networkId = adProviderConfig.getNetworkId();
						String networkToken = adProviderConfig.getNetworkToken();

						ArrayList<DataEntry> listFoundOfferGroups = 
								hasoffersAPIManager.findAllOfferGroups(offerWall, networkId, networkToken,"findAllOfferGroups");
				        for(int i=0;i<listFoundOfferGroups.size();i++) {
				            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), LogStatus.OK, "HASOFFERS Found group: id: "+listFoundOfferGroups.get(i).getOfferGroup().getId()+" name: "+listFoundOfferGroups.get(i).getOfferGroup().getName()+" status: "+listFoundOfferGroups.get(i).getOfferGroup().getStatus());
				        	System.out.println("Found group: id: "+listFoundOfferGroups.get(i).getOfferGroup().getId()+" name: "+listFoundOfferGroups.get(i).getOfferGroup().getName()+" status: "+listFoundOfferGroups.get(i).getOfferGroup().getStatus());

				            if(listFoundOfferGroups.get(i).getOfferGroup().getStatus().equals("active")
				            		&& listFoundOfferGroups.get(i).getOfferGroup().getName().equals(adProviderConfig.getOfferGroupName()) ) {
					        	System.out.println("Pulling offers from group: id: "+listFoundOfferGroups.get(i).getOfferGroup().getId()+" name: "+listFoundOfferGroups.get(i).getOfferGroup().getName()+" status: "+listFoundOfferGroups.get(i).getOfferGroup().getStatus());
				            	
					        	ArrayList<Integer> listFoundOfferIds = hasoffersAPIManager.findAllOfferGroupOfferIds(offerWall,
					        			individualOfferWall,
					        			networkId, 
					        			networkToken,
					        			"findAllOfferGroupOfferIds",
					        			listFoundOfferGroups.get(i).getOfferGroup().getId());
					        	for(int j=0;j<listFoundOfferIds.size();j++) {
					        		listOfferIdsPool.add(listFoundOfferIds.get(j));
					        	}
				        	}
				        }

			            Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
			            		offerWall.getRealm().getId(), 
			            		LogStatus.OK, 
			            		"HASOFFERS identified total number of offers to chose from: "+listOfferIdsPool.size());
				        
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.HASOFFERS.toString());
						//generate unique key as combination of offer wall combo name + individual offer name + realmId + timestamp 
						String sha1Hash = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+singleOfferWallConfig.getName()+offerWall.getRealm().getId()+System.currentTimeMillis());
						individualOfferWall.setId(sha1Hash);

						ArrayList<Offer> listIndividualOffers = new ArrayList<Offer>();
						
						//select offers that will be displayed on offer wall
						ArrayList<Offer> listSelectedIndividualOffers = 
								hasoffersAPIManager.selectOffers(offerWall,
										individualOfferWall,
										adProviderConfig,
										offerFilterManager,
										offerRewardCalculationManager,
										listOfferIdsPool, 
										singleOfferWallConfig.getNumberOfOffers(), 
										networkId, 
										networkToken);
						individualOfferWall.setOffers(listSelectedIndividualOffers);
						listIndividualOfferWalls.add(individualOfferWall);
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.OK, 
								Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED+ " created single offer wall: "+individualOfferWall.getOfferWallName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());
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
				
				//----------------------------------------------------------------------------------
				//----------------------------------- MINIMOB ------------------------------------
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())) {
					try {
						//retrieve offers via api call
						MinimobProviderConfig adProviderConfig = serDeMinimob.deserialize(adProvider.getConfiguration());
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						ArrayList<Offer> listAllIndividualOffers = 
								minimobAPIManager.getOffers(offerWall,
										individualOfferWall,
										adProviderConfig,
										offerFilterManager,
										offerRewardCalculationManager,
										adProviderConfig.getApiKey(), 
										singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.MINIMOB.toString());
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
				
				//----------------------------------------------------------------------------------
				//----------------------------------- FYBER (not used here as we have dynamic feed - but left here until testing is completed) ------------------------------------
				/*
				if(adProvider.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())) {
					try {
						IndividualOfferWall individualOfferWall = new IndividualOfferWall();
						//retrieve offers via api call
						FyberProviderConfig adProviderConfig = serDeFyber.deserialize(adProvider.getConfiguration());
						ArrayList<Offer> listAllIndividualOffers = fyberAPIManager.getOffers(offerWall, 
								adProviderConfig.getApiId(), 
								adProviderConfig.getApiKey(), 
								adProviderConfig.getOfferTypes(),
								individualOfferWall,
								offerFilterManager,
								offerRewardCalculationManager,
								adProviderConfig);
						ArrayList<Offer> listSelectedIndividualOffers = fyberAPIManager.selectOffers(offerWall, listAllIndividualOffers,singleOfferWallConfig.getNumberOfOffers());
						individualOfferWall.setOfferWallName(singleOfferWallConfig.getName());
						//individualOfferWall.setGenerationTimestamp(System.currentTimeMillis() / 1000L);
						individualOfferWall.setGenerationTime(new Timestamp(System.currentTimeMillis()).toString());
						individualOfferWall.setOffers(listSelectedIndividualOffers);
						individualOfferWall.setAdProviderCodeName(OfferProviderCodeNames.FYBER.toString());
						//generate unique key as combination of offer wall combo name + individual offer name + realmId + timestamp 
						String sha1Hash = DigestUtils.sha1Hex(offerWallContent.getCompositeOfferWallName()+singleOfferWallConfig.getName()+offerWall.getRealm().getId()+System.currentTimeMillis());
						individualOfferWall.setId(sha1Hash);
						
						listIndividualOfferWalls.add(individualOfferWall);
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.OK, 
								Application.SINGLE_OFFER_WALL_GENERATION_IDENTIFIED+ " created single offer wall: "+individualOfferWall.getOfferWallName()+" offer provider: "+offerWall.getProviderCodeName()+" network: "+offerWall.getRealm().getName());
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
				*/
				//-------------------------------------- another offer provider ----------------------------------
			}

			if(offerFilterManager.getOfferFilterConfiguration().isRejectDuplicateOffers()){
				offerFilterManager.getOfferDuplicatesDetector().rejectDuplicateOffersMarkedForRemoval(offerWall, listIndividualOfferWalls);
			}

			//filter out blocked offers
			if(offerFilterManager.getOfferFilterConfiguration().isRejectBlockedOffers()){
				offerFilterManager.filterOutBlockedOffers(offerWall, listIndividualOfferWalls);
			}

			//filter out stale offer walls
			if(offerFilterManager.getOfferFilterConfiguration().isRejectStaleOffers())
			{
				offerFilterManager.filterOutStaleOffers(offerWall, listIndividualOfferWalls);
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

			//persist offer walls in db
			managerOfferPersistence.persistOffers(offerWall, listIndividualOfferWalls);
			
			//set all aggreate offer walls and serialise it into final content for storage
			offerWallContent.setOfferWalls(listIndividualOfferWalls);
			String strOfferWallContent = serDeOfferWallContent.serialize(offerWallContent);
			offerWall.setContent(strOfferWallContent);
			offerWall.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			daoOfferWall.createOrUpdate(offerWall);
			
			return strOfferWallContent;
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
