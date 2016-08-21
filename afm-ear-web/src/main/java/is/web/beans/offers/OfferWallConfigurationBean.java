package is.web.beans.offers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.OfferType;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerWall.OfferWallGenerator;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.bl.offerWall.positioning.OfferPositioningEntry;
import is.ejb.bl.offerWall.positioning.OfferWallPositioningDataHolder;
import is.ejb.bl.offerWall.positioning.SerDeOfferPositioning;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.primefaces.context.RequestContext;

@ManagedBean(name="offerWallConfigurationBean")
@SessionScoped
public class OfferWallConfigurationBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;
	
	//now transform to OfferWall entitites all that is here!
	private OfferWallDataModelBean domainDataModel;
	private SingleOfferWallConfigurationDataModelBean offerWallConfigurationsDataModel;
	
	private SingleOfferConfigurationDataModelBean offerConfigurationDataModel;
	private ArrayList<Offer> listAllGeneratedOffers = new ArrayList<Offer>();

	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;

	@Inject
	private SerDeOfferPositioning serDeOfferPositioning;
	
	private List<OfferWallEntity> listDomains = new ArrayList<OfferWallEntity>();
	private List<AdProviderEntity> listAdProviders = new ArrayList<AdProviderEntity>();
	private ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
	
	private boolean offerWallGenerationEnabled = false;
	private int offerWallGenerationIntervals = 24; //in hours
	private String generatedRawOfferContent = "";
	
	@Inject
	private DAOOfferWall daoOfferWall;
	@Inject
	private DAOAdProvider daoAdProvider;

	@Inject
	private DAORealm daoRealm;
	
	@Inject
	private SerDeOfferWallConfiguration serDeOfferWallConfiguration;

	@Inject
	private SerDeMinimobProviderConfiguration serDeMocean;
	@Inject
	private SerDeFyberProviderConfiguration serDeFyber;
	@Inject
	private SerDeHasoffersProviderConfiguration serDeHasoffers;

	@Inject
	private OfferWallGenerator offerWallManager;

	private MinimobProviderConfig configMocean;
	private FyberProviderConfig configFyber;
	private HasoffersProviderConfig configHasoffers;

	private SingleOfferWallConfiguration editedSingleOfferWall = new SingleOfferWallConfiguration();
	private SingleOfferWallConfiguration createdSingleOfferWall = new SingleOfferWallConfiguration();
	
	private OfferWallEntity editedDomain = new OfferWallEntity();
	private OfferWallEntity createdDomain = new OfferWallEntity();
	
	private UserEntity customer;

	private boolean renderMocean = false;
	private boolean renderFyber = false;
	private boolean renderHasoffers = false;
	
	private String moceanAdUrl = "";
	private String fyberParam1 = "";
	private String hasoffersParam1 = "";

	@Inject
	private DAORewardType daoRewardType;
	private List<RewardTypeEntity> listRewardTypes = new ArrayList<RewardTypeEntity>();

	public OfferWallConfigurationBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

	   //create fake 
	   createdDomain = new OfferWallEntity();
	   createdDomain.setName(" ");
	   createdDomain.setDescription(" ");
	   
	   editedDomain = new OfferWallEntity();
	   editedDomain.setName(" ");
	   editedDomain.setDescription(" ");

	   ////fetch list of offer wall configurations
	   //offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);

	   createdSingleOfferWall = new SingleOfferWallConfiguration();
	   createdSingleOfferWall.setName(" ");
	   createdSingleOfferWall.setAdProviderCodeName(" ");
	   createdSingleOfferWall.setNumberOfOffers(1000);//by default set high number of offers
	   editedSingleOfferWall = new SingleOfferWallConfiguration();
	   editedSingleOfferWall.setName(" ");
	   editedSingleOfferWall.setAdProviderCodeName(" ");
	   
	   refresh();
   }

   
	public void refresh() {
		try {
			logger.info("refreshing OfferWall bean...");
		    //fetch list of offer wall configurations
		    offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);
			
			offerWallGenerationEnabled = Application.getGenerateOffers();
			offerWallGenerationIntervals = Application.getOfferWallIntervals();
			listAdProviders = (List<AdProviderEntity>)daoAdProvider.findAllByRealmId(loginBean.getUser().getRealm().getId(), true);
			List<OfferWallEntity>listDomains = (List<OfferWallEntity>)daoOfferWall.findAllByRealmId(loginBean.getUser().getRealm().getId());
			logger.info("identified domains: "+listDomains.size());
			domainDataModel = new OfferWallDataModelBean(listDomains);
			//reward types list
			listRewardTypes = daoRewardType.findAllByRealmId(loginBean.getUser().getRealm().getId());
			
			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idOfferWall");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			listDomains = new ArrayList<OfferWallEntity>();
			domainDataModel = new OfferWallDataModelBean(listDomains);

			//e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}
	
	public void update() {
		logger.info("updating OfferWall configuration: "+editedDomain.getName());
		try {
			if(editedDomain.getName().trim().length()==0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide name for configured Offer wall"));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				return;
			}

			//update configurations setup
			OfferWallConfiguration offerWallConfiguration = new OfferWallConfiguration();
			offerWallConfiguration.setConfigurations(listSingleOfferWallConfigurations);
			String strOfferWallConfiguration = serDeOfferWallConfiguration.serialize(offerWallConfiguration);
			editedDomain.setConfiguration(strOfferWallConfiguration);

			daoOfferWall.createOrUpdate(editedDomain);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall: "+editedDomain.getName()+" successfully updated"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			//todo growl display
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to update offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
		}
	}

	public void resetData() {
		logger.info("reset data called...");
		//reset values for next domain creation
		createdDomain = new OfferWallEntity();
		createdDomain.setName(" ");
		createdDomain.setDescription("");
		//reset 
		listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
		offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);
	}

	private void refreshListOfGeneratedOffersForPositioningDisplay(OfferWallEntity domain) {
		try {
			//extract list of offers generated by this offer wall
			listAllGeneratedOffers = new ArrayList<Offer>();
			OfferWallContent generatedOffers = serDeOfferWallContent.deserialize(domain.getContent());
			ArrayList<IndividualOfferWall> listIndividualOfferWalls = generatedOffers.getOfferWalls();
			logger.info("identified number of offer walls: "+generatedOffers.getOfferWalls());
			for(int i=0;i<listIndividualOfferWalls.size();i++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(i);
				ArrayList<Offer> listOffers = individualOfferWall.getOffers();
				for(int j=0;j<listOffers.size();j++) {
					Offer offer = listOffers.get(j);
					listAllGeneratedOffers.add(offer);
					logger.info("-> got offer: "+offer.getTitle());
				}
			}
			
			//create data model for generated offers
			offerConfigurationDataModel = new SingleOfferConfigurationDataModelBean(listAllGeneratedOffers);
			
			//update model in display
			RequestContext.getCurrentInstance().update("tabView:idPreviewOfferWallWithPositioning");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to open edit dialog for offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}
	
	public void setEditedDomain(OfferWallEntity domain) {
		logger.info("setting edited offer wall: "+domain.getName());
		generatedRawOfferContent = domain.getContent();
		OfferWallConfiguration offerWallConfiguration;
		try {
			offerWallConfiguration = serDeOfferWallConfiguration.deserialize(domain.getConfiguration());
			listSingleOfferWallConfigurations = offerWallConfiguration.getConfigurations();
			offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);
			this.editedDomain = domain;
			logger.info("number of individual offer wall configurations: "+listSingleOfferWallConfigurations.size());

			refreshListOfGeneratedOffersForPositioningDisplay(domain);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to open edit dialog for offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void create() {
		logger.info("creating offer wall: "+createdDomain.getName()+" desc: "+createdDomain.getName());
		
		try {
			if(createdDomain.getName().trim().length()==0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide name for configured Offer wall"));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				return;
			}
			
			boolean exists = false;
			//find if domain with given name inside existing realm does not already exist
			List<OfferWallEntity>listDomains = (List<OfferWallEntity>)daoOfferWall.findAllByRealmId(loginBean.getUser().getRealm().getId());
			for(OfferWallEntity de:listDomains) {
				if(de.getName().trim().equals(createdDomain.getName().trim())){
					exists = true;
				}
			}
			
			if(exists) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Offer wall: "+createdDomain.getName()+" with given name already exists. Please provide different name"));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			} else {
				//creating new offer
				createdDomain.setRealm(loginBean.getUser().getRealm());
				AdProviderEntity adProviderEntity = daoAdProvider.findByCodeName(createdDomain.getProviderCodeName());
				OfferWallConfiguration offerWallConfiguration = new OfferWallConfiguration();
				offerWallConfiguration.setConfigurations(listSingleOfferWallConfigurations);
				String strOfferWallConfiguration = serDeOfferWallConfiguration.serialize(offerWallConfiguration);
				createdDomain.setConfiguration(strOfferWallConfiguration);
				createdDomain = daoOfferWall.createOrUpdate(createdDomain);

				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall: "+createdDomain.getName()+" created."));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				
				//reset values for next domain creation
				createdDomain = new OfferWallEntity();
				createdDomain.setName(" ");
				createdDomain.setDescription("");
				//reset 
				//listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
				//offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);

				//todo growl message
				refresh();
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void createIndividualOfferWall() {
		logger.info("creating individual offer wall: "+createdSingleOfferWall.getName()+" ad provider: "+createdSingleOfferWall.getAdProviderCodeName()+" offers number: "+createdSingleOfferWall.getNumberOfOffers()+" individual offers number: "+listSingleOfferWallConfigurations.size());
		boolean offerAlreadyExists = false;
		try {
			for(int i=0;i<listSingleOfferWallConfigurations.size();i++) {
				if(listSingleOfferWallConfigurations.get(i).getName().trim().equals(createdSingleOfferWall.getName().trim())) {
					System.out.println("checking: "+listSingleOfferWallConfigurations.get(i).getName()+" "+createdSingleOfferWall.getName());
					offerAlreadyExists = true;	
					break;
				}
			}

//we allow to create offer walls with identical names as the system will append number to it			
//			if(!offerAlreadyExists) {
				//set the newly created single offer wall code name 
				AdProviderEntity adProvider = daoAdProvider.findByName(createdSingleOfferWall.getAdProviderConfigurationName());
				createdSingleOfferWall.setAdProviderCodeName(adProvider.getCodeName());
				createdSingleOfferWall.setAdProviderConfigurationName(adProvider.getName());
				listSingleOfferWallConfigurations.add(createdSingleOfferWall);
				//create new object
				createdSingleOfferWall = new SingleOfferWallConfiguration();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New offer wall configuration successfully added"));
//			} else {
//				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Offer with name: "+createdSingleOfferWall.getName()+" already exists. Please use different name."));
//			}
			
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to add new offer wall configuration: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void deleteIndividualOfferWall(String singleOfferWallConfigurationName) {
		logger.info("deleting individual offer wall: "+singleOfferWallConfigurationName);
		
		try {
			for(int i=0;i<listSingleOfferWallConfigurations.size();i++) {
				if(listSingleOfferWallConfigurations.get(i).getName().equals(singleOfferWallConfigurationName)) {
					listSingleOfferWallConfigurations.remove(i);
					System.out.println("removed...");
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall configuration successfully removed"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to remove offer wall configuration: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void moveUpIndividualOfferWall(String singleOfferWallConfigurationName) {
		logger.info("moving up individual offer wall: "+singleOfferWallConfigurationName);
		
		try {
			for(int i=0;i<listSingleOfferWallConfigurations.size();i++) {
				if(listSingleOfferWallConfigurations.get(i).getName().equals(singleOfferWallConfigurationName)) {
					if(i>0) {
						System.out.println("moving up...");
						SingleOfferWallConfiguration ofcToMove = listSingleOfferWallConfigurations.get(i);
						SingleOfferWallConfiguration ofc = listSingleOfferWallConfigurations.get(i-1);
						listSingleOfferWallConfigurations.set(i-1, ofcToMove);
						listSingleOfferWallConfigurations.set(i, ofc);
					} else {
						System.out.println("already up...");						
					}
					
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall configuration successfully removed"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to remove offer wall configuration: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void moveUpIndividualOffer(String offerName) {
		logger.info("moving up individual offer: "+offerName);
		
		try {
			for(int i=0;i<listAllGeneratedOffers.size();i++) {
				if(listAllGeneratedOffers.get(i).getTitle().equals(offerName)) {
					if(i>0 && listAllGeneratedOffers.size() > 2) {
						System.out.println("moving up...");
						Offer ofToMove = listAllGeneratedOffers.get(i);
						Offer of = listAllGeneratedOffers.get(i-1);
						listAllGeneratedOffers.set(i-1, ofToMove);
						listAllGeneratedOffers.set(i, of);
						//indicate that this offer was positioned
						ofToMove.setPositioned(true);
					} else {
						System.out.println("already up...");						
					}
					
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer: "+offerName+" successfully moved"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			//RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferConfigurationEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to move offer: "+offerName+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void moveToTopIndividualOffer(String offerName) {
		logger.info("moving to top individual offer: "+offerName);
		
		try {
			for(int i=0;i<listAllGeneratedOffers.size();i++) {
				if(listAllGeneratedOffers.get(i).getTitle().equals(offerName)) {
					if(i>0 && listAllGeneratedOffers.size() > 2) {
						System.out.println("moving to top...");
						Offer of = listAllGeneratedOffers.get(i);
						listAllGeneratedOffers.remove(i);
						listAllGeneratedOffers.add(0, of);
						//indicate that this offer was positioned
						of.setPositioned(true);
					} else {
						System.out.println("already up...");						
					}
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer: "+offerName+" successfully moved"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOfferConfigurationEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to move offer: "+offerName+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void setOfferType(String offerName) {
		logger.info("setting offer type for offer: "+offerName);
		
		try {
			for(int i=0;i<listAllGeneratedOffers.size();i++) {
				if(listAllGeneratedOffers.get(i).getTitle().equals(offerName)) {
					System.out.println("moving up...");
					Offer of = listAllGeneratedOffers.get(i);
					if(of.getType().equals(OfferType.STANDARD.toString())){
						of.setType(OfferType.PROMO.toString());
					} else {
						of.setType(OfferType.STANDARD.toString());
					}
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer: "+offerName+" type successfully updated"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			//RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferConfigurationEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to update offer type for offer: "+offerName+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void saveOfferWallPositioningSetup() {
		logger.info("saving offer wall positioning setup for offer wall: "+editedDomain.getName());
		try {
			if(listAllGeneratedOffers.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Positioning not saved as there were no offers generated for offer wall: "+editedDomain.getName()));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				return;
			}
			//fill in the list of positioning entries
			ArrayList<OfferPositioningEntry> listAllPositioningEntries = new ArrayList<OfferPositioningEntry>();
			for(int i=0;i<listAllGeneratedOffers.size();i++) {
				Offer of = listAllGeneratedOffers.get(i);
				listAllPositioningEntries.add(new OfferPositioningEntry(of.getTitle(), of.getType(), of.isPositioned()));
			}
			OfferWallPositioningDataHolder offerWallPositioningDataHolder = new OfferWallPositioningDataHolder(listAllPositioningEntries);

			//persist in offer wall table row 
			String strOfferWallPositioningDataHolder = serDeOfferPositioning.serialize(offerWallPositioningDataHolder);
			logger.info("serialised content: "+strOfferWallPositioningDataHolder);

			//persist in db
			editedDomain.setPositioning(strOfferWallPositioningDataHolder);
			editedDomain = daoOfferWall.createOrUpdate(editedDomain);
			
			//regenerate offer feed based on positioning settings
			logger.info("generating OfferWall configuration: "+editedDomain.getName()+" provider code name: "+editedDomain.getProviderCodeName());
			update();
			
			try {
				generatedRawOfferContent = offerWallManager.generateOfferWall(editedDomain);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Content of Offer wall: "+editedDomain.getName()+" successfully generated"));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				refresh();
				refreshListOfGeneratedOffersForPositioningDisplay(editedDomain);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						"OFFER_WALL_GENERATION error generating offer walls, error: "+e.toString());

				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to generate offer wall: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
				refresh();
			}
			
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Positioning setup for wall: "+editedDomain.getName()+" successfully saved"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			//RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsTable");
			RequestContext.getCurrentInstance().update("tabView:idOfferConfigurationEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to save positioning setup for wall: "+editedDomain.getName()+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}
	
	public void delete() {
		logger.info("deleting offer wall: "+editedDomain.getName()+" desc: "+editedDomain.getName());
		
		try {
			editedDomain = daoOfferWall.findById(editedDomain.getId());
			editedDomain.setRealm(null);
			editedDomain = daoOfferWall.createOrUpdate(editedDomain);
			logger.info("Deleting offer wall: "+editedDomain.getName()+" id: "+editedDomain.getId());
			daoOfferWall.delete(editedDomain);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall: "+editedDomain.getName()+" deleted."));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
		}
	}

	public void generateOfferWallContent() {
		logger.info("generating OfferWall configuration: "+editedDomain.getName()+" provider code name: "+editedDomain.getProviderCodeName());
		update();
		
		try {
			generatedRawOfferContent = offerWallManager.generateOfferWall(editedDomain);
			refreshListOfGeneratedOffersForPositioningDisplay(editedDomain);

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Content of Offer wall: "+editedDomain.getName()+" successfully generated"));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			RequestContext.getCurrentInstance().update("tabView:idPreviewOfferWallDialog");
			
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					"OFFER_WALL_GENERATION error generating offer walls, error: "+e.toString());

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to generate offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
		}
	}

	public void previewOfferWall() {
		logger.info("previewing OfferWall configuration: "+editedDomain.getName());
		try {
			generatedRawOfferContent = editedDomain.getContent();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall: "+editedDomain.getName()+" successfully displayed"));
			RequestContext.getCurrentInstance().update("tabView:idPreviewOfferWall");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			//todo growl display
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to display offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
		}
	}

	
	public void previewOfferWall(OfferWallEntity offerWall) {
		logger.info("previewing OfferWall configuration: "+offerWall.getName());
		try {
			generatedRawOfferContent = offerWall.getContent();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer wall: "+editedDomain.getName()+" successfully displayed"));
			RequestContext.getCurrentInstance().update("tabView:idPreviewOfferWall");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");

			//todo growl display
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to display offer wall: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
			refresh();
		}
	}

	public OfferWallDataModelBean getDomainDataModel() {
		return domainDataModel;
	}

	public void setDomainDataModel(OfferWallDataModelBean domainDataModel) {
		this.domainDataModel = domainDataModel;
	}

	public OfferWallEntity getCreatedDomain() {
		return createdDomain;
	}

	public void setCreatedDomain(OfferWallEntity createdDomain) {
		this.createdDomain = createdDomain;
	}

	public OfferWallEntity getEditedDomain() {
		return editedDomain;
	}
	
	public String getMoceanAdUrl() {
		return moceanAdUrl;
	}

	public void setMoceanAdUrl(String moceanAdUrl) {
		this.moceanAdUrl = moceanAdUrl;
	}

	public String getFyberParam1() {
		return fyberParam1;
	}

	public void setFyberParam1(String fyberParam1) {
		this.fyberParam1 = fyberParam1;
	}

	public String getHasoffersParam1() {
		return hasoffersParam1;
	}

	public void setHasoffersParam1(String hasoffersParam1) {
		this.hasoffersParam1 = hasoffersParam1;
	}

	public boolean isRenderMocean() {
		return renderMocean;
	}

	public void setRenderMocean(boolean renderMocean) {
		this.renderMocean = renderMocean;
	}

	public boolean isRenderFyber() {
		return renderFyber;
	}

	public void setRenderFyber(boolean renderFyber) {
		this.renderFyber = renderFyber;
	}

	public boolean isRenderHasoffers() {
		return renderHasoffers;
	}

	public void setRenderHasoffers(boolean renderHasoffers) {
		this.renderHasoffers = renderHasoffers;
	}

	public List<AdProviderEntity> getListAdProviders() {
		return listAdProviders;
	}

	public void setListAdProviders(List<AdProviderEntity> listAdProviders) {
		this.listAdProviders = listAdProviders;
	}

	public boolean isOfferWallGenerationEnabled() {
		return offerWallGenerationEnabled;
	}

	public void setOfferWallGenerationEnabled(boolean offerWallGenerationEnabled) {
		this.offerWallGenerationEnabled = offerWallGenerationEnabled;
	}

	public int getOfferWallGenerationIntervals() {
		return offerWallGenerationIntervals;
	}

	public void setOfferWallGenerationIntervals(int offerWallGenerationIntervals) {
		this.offerWallGenerationIntervals = offerWallGenerationIntervals;
	}

	public SingleOfferWallConfigurationDataModelBean getOfferWallConfigurationsDataModel() {
		return offerWallConfigurationsDataModel;
	}

	public void setOfferWallConfigurationsDataModel(
			SingleOfferWallConfigurationDataModelBean offerWallConfigurationsDataModel) {
		this.offerWallConfigurationsDataModel = offerWallConfigurationsDataModel;
	}

	public SingleOfferWallConfiguration getEditedSingleOfferWall() {
		return editedSingleOfferWall;
	}

	public void setEditedSingleOfferWall(
			SingleOfferWallConfiguration editedSingleOfferWall) {
		this.editedSingleOfferWall = editedSingleOfferWall;
	}

	public SingleOfferWallConfiguration getCreatedSingleOfferWall() {
		return createdSingleOfferWall;
	}

	public void setCreatedSingleOfferWall(
			SingleOfferWallConfiguration createdSingleOfferWall) {
		this.createdSingleOfferWall = createdSingleOfferWall;
	}

	public String getGeneratedRawOfferContent() {
		return generatedRawOfferContent;
	}

	public void setGeneratedRawOfferContent(String generatedRawOfferContent) {
		this.generatedRawOfferContent = generatedRawOfferContent;
	}

	public List<RewardTypeEntity> getListRewardTypes() {
		return listRewardTypes;
	}

	public void setListRewardTypes(List<RewardTypeEntity> listRewardTypes) {
		this.listRewardTypes = listRewardTypes;
	}

	public SingleOfferConfigurationDataModelBean getOfferConfigurationDataModel() {
		return offerConfigurationDataModel;
	}

	public void setOfferConfigurationDataModel(
			SingleOfferConfigurationDataModelBean offerConfigurationDataModel) {
		this.offerConfigurationDataModel = offerConfigurationDataModel;
	}

	
}

