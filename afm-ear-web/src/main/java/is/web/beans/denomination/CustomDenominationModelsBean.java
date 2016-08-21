package is.web.beans.denomination;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignment;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.SerDeCustomDenominationModelAssignments;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOCustomDenominationModel;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.CustomDenominationModelEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.offers.BlockedOffersDataModelBean;
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

import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.primefaces.context.RequestContext;

@ManagedBean(name="customDenominationModelsBean")
@SessionScoped
public class CustomDenominationModelsBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;

	//custom dms
	@Inject
	private DAOCustomDenominationModel daoCustomDenominationModel;
	@Inject
	private SerDeCustomDenominationModelAssignments serDeCustomDenominationModelAssignments;
	private CustomDenominationModelEntity customDenominationModelEntity = null;
	private CustomDenominationModelAssignments customDenominationModelAssignmentsDataHolder = null;
	private ArrayList<CustomDenominationModelAssignment> listCustomDenominationModelAssignments = null;

	//multi-offer walls (mows)
	@Inject
	private DAOOfferWall daoOfferWall;
	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;
	private List<OfferWallEntity> listMows = new ArrayList<OfferWallEntity>();
	private ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
	
	private CustomDenominationModelsDataModelBean customDenominationModelAssignmentsDataModel = null;
	
	private ArrayList<Offer> listAllAvctiveIndividualOffers = new ArrayList<Offer>();
	private String selectedOfferToAssignToDenominationModelKey = null;
	private Offer selectedOfferToAssignToDenominationModel = null;

	@Inject
	private DAODenominationModel daoDenominationModel;
	private List<DenominationModelEntity> listDenominationModels = new ArrayList<DenominationModelEntity>();
	private int selectedDenominationModelId = 0;
	
	@Inject
	private DAORealm daoRealm;
	
	private UserEntity customer;
	
	public CustomDenominationModelsBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

	   refresh();
   }
  
   public void refresh() {
		try {
			logger.info("refreshing bean...");
			listDenominationModels = (List<DenominationModelEntity>)daoDenominationModel.findAllByRealmId(loginBean.getUser().getRealm().getId());

			customDenominationModelEntity = daoCustomDenominationModel.findByRealmId(loginBean.getUser().getRealm().getId());
			customDenominationModelAssignmentsDataHolder = serDeCustomDenominationModelAssignments.deserialize(customDenominationModelEntity.getContent());
			listCustomDenominationModelAssignments = customDenominationModelAssignmentsDataHolder.getListCustomDenominationModelAssignments();
			customDenominationModelAssignmentsDataModel = new CustomDenominationModelsDataModelBean(listCustomDenominationModelAssignments);
			
			//find all active offer walls
			listAllAvctiveIndividualOffers = new ArrayList<Offer>();
			List<OfferWallEntity> listOfferWalls = (List<OfferWallEntity>)daoOfferWall.findAllByRealmIdAndActive(loginBean.getUser().getRealm().getId(), true);
			for(int i=0;i<listOfferWalls.size();i++) {
				OfferWallEntity mow = listOfferWalls.get(i);
				OfferWallContent offerWallContent = serDeOfferWallContent.deserialize(mow.getContent());
				ArrayList<IndividualOfferWall> listIndividualOfferWals = offerWallContent.getOfferWalls();
				for(int j=0;j<listIndividualOfferWals.size();j++) {
					IndividualOfferWall individualOfferWall = listIndividualOfferWals.get(j);
					ArrayList<Offer> listOffers = individualOfferWall.getOffers();
					for(int k=0;k<listOffers.size();k++) {
						Offer offer = listOffers.get(k);
						//add offer
						if(!isOfferAlreadyAdded(listAllAvctiveIndividualOffers, offer)) {
							listAllAvctiveIndividualOffers.add(offer);
						}
					}
				}
			}
			
			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idTableCustomDenominationModels");
			RequestContext.getCurrentInstance().update("tabView:idSelectOffers");
			RequestContext.getCurrentInstance().update("tabView:idSelectDenominationModels");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			listCustomDenominationModelAssignments = new ArrayList<CustomDenominationModelAssignment>();
			customDenominationModelAssignmentsDataModel = new CustomDenominationModelsDataModelBean(listCustomDenominationModelAssignments);

			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}

   public void persistConfiguration() {
		try {
			logger.info("persisting configuration");
			customDenominationModelEntity = daoCustomDenominationModel.findByRealmId(loginBean.getUser().getRealm().getId());
			CustomDenominationModelAssignments customDenominationModelAssignmentDataHolder = new CustomDenominationModelAssignments();
			customDenominationModelAssignmentDataHolder.setListCustomDenominationModelAssignments(listCustomDenominationModelAssignments);

			String strCustomDenominationModelAssignment = serDeCustomDenominationModelAssignments.serialize(customDenominationModelAssignmentDataHolder);
			customDenominationModelEntity.setContent(strCustomDenominationModelAssignment);
			customDenominationModelEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			customDenominationModelEntity = daoCustomDenominationModel.createOrUpdate(customDenominationModelEntity);

		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			listCustomDenominationModelAssignments = new ArrayList<CustomDenominationModelAssignment>();
			customDenominationModelAssignmentsDataModel = new CustomDenominationModelsDataModelBean(listCustomDenominationModelAssignments);

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
					"Failed", "Unable to persist custom denomination model assignment configuration: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idCustomDenominationModelsGrowl");
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
   }
   
   private boolean isOfferAlreadyAdded(ArrayList<Offer> listAllOffers, Offer offerToAdd) {
    	for(int i=0;i<listAllOffers.size();i++) {
    		Offer alreadyAddedOffer = listAllOffers.get(i);
    		if(alreadyAddedOffer.getTitle().equals(offerToAdd.getTitle()) &&
    				alreadyAddedOffer.getAdProviderCodeName().equals(offerToAdd.getAdProviderCodeName())) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
	public void blockOffer() {
		System.out.println("selected offer: "+selectedOfferToAssignToDenominationModelKey+" selected denomination model id: "+selectedDenominationModelId);
		
		try {
			DenominationModelEntity selectedDenominationModel = daoDenominationModel.findById(selectedDenominationModelId);
			
			String offerProvider = selectedOfferToAssignToDenominationModelKey.substring(0, selectedOfferToAssignToDenominationModelKey.indexOf("-")).trim();
			String offerSourceId = selectedOfferToAssignToDenominationModelKey.substring(selectedOfferToAssignToDenominationModelKey.indexOf("-")+1, selectedOfferToAssignToDenominationModelKey.length()).trim();
			logger.info("offer provider: "+offerProvider+" offer source Id: "+offerSourceId);
			for(int i=0;i<listAllAvctiveIndividualOffers.size();i++) {
				Offer offer = listAllAvctiveIndividualOffers.get(i);
				//logger.info(offer.getAdProviderCodeName()+"-"+offerProvider+" "+offer.getSourceId()+"-"+offerSourceId);
				if(offer.getAdProviderCodeName().equals(offerProvider) && offer.getSourceId().equals(offerSourceId)) {
					//logger.info("found!");
					selectedOfferToAssignToDenominationModel = offer;
					break;
				}
				
			}
			if(selectedOfferToAssignToDenominationModel != null) {
				logger.info("adding/updating custom denomination model for offer: "+selectedOfferToAssignToDenominationModel.getTitle()+" ad provider: "+selectedOfferToAssignToDenominationModel.getAdProviderCodeName());
				addOfferToBlockList(selectedOfferToAssignToDenominationModel, selectedDenominationModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"Failed", "Unable to block selected offer: "+selectedOfferToAssignToDenominationModel.getTitle()+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idCustomDenominationModelsGrowl");
			refresh();
		}
	}

	private void addOfferToBlockList(Offer offerToAssignCustomDenominationModel, DenominationModelEntity selectedDenominationModel) {
		for(int i=0;i<listCustomDenominationModelAssignments.size();i++) {
			CustomDenominationModelAssignment bo = listCustomDenominationModelAssignments.get(i);
			if(bo.getOfferSourceId().equals(offerToAssignCustomDenominationModel.getSourceId()) && 
					bo.getAdProviderCodeName().equals(offerToAssignCustomDenominationModel.getAdProviderCodeName())) {
				//only update the existing configuration with current assignment
				bo.setDenominationModelId(selectedDenominationModel.getId());
				bo.setDenominationModelName(selectedDenominationModel.getName());
				bo.setTimestamp(new Timestamp(System.currentTimeMillis()));
				persistConfiguration();

				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
						"Success", "Successfully updated denomination model setttings for offer: "+offerToAssignCustomDenominationModel.getTitle()));
				RequestContext.getCurrentInstance().update("tabView:idCustomDenominationModelsGrowl");
				return;
			}
		}
		
		logger.info("adding new custom denomination model assignment");
		CustomDenominationModelAssignment newCustomDenominationModelAssignment = new CustomDenominationModelAssignment();
		newCustomDenominationModelAssignment.setAdProviderCodeName(offerToAssignCustomDenominationModel.getAdProviderCodeName());
		newCustomDenominationModelAssignment.setOfferId(offerToAssignCustomDenominationModel.getId());
		newCustomDenominationModelAssignment.setOfferSourceId(offerToAssignCustomDenominationModel.getSourceId());
		newCustomDenominationModelAssignment.setTitle(offerToAssignCustomDenominationModel.getTitle());
		newCustomDenominationModelAssignment.setDenominationModelId(selectedDenominationModel.getId());
		newCustomDenominationModelAssignment.setDenominationModelName(selectedDenominationModel.getName());
		newCustomDenominationModelAssignment.setTimestamp(new Timestamp(System.currentTimeMillis()));
		listCustomDenominationModelAssignments.add(newCustomDenominationModelAssignment);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
				"Success", "Custom denomination model assigned for offer: "+selectedOfferToAssignToDenominationModel.getTitle()+" successfully created"));
		RequestContext.getCurrentInstance().update("tabView:idCustomDenominationModelsGrowl");
		
		//store current configuration in db
		persistConfiguration();
		
		//refresh blocked offers table 
		customDenominationModelAssignmentsDataModel = new CustomDenominationModelsDataModelBean(listCustomDenominationModelAssignments);
		RequestContext.getCurrentInstance().update("tabView:idTableCustomDenominationModels");
	}
	
	public void delete(CustomDenominationModelAssignment blockedOffer) {
		logger.info("removing custom denomination model assignment: "+blockedOffer.getTitle()+" from custom assignments list");
		
		try {
			for(int i=0;i<listCustomDenominationModelAssignments.size();i++){
				CustomDenominationModelAssignment bo = listCustomDenominationModelAssignments.get(i);
				if(bo.getOfferSourceId().equals(blockedOffer.getOfferSourceId()) &&
					bo.getAdProviderCodeName().equals(blockedOffer.getAdProviderCodeName()) &&
						bo.getTitle().equals(blockedOffer.getTitle()) ) {
					listCustomDenominationModelAssignments.remove(i);

					//store current configuration in db
					persistConfiguration();

					//refresh blocked offers table 
					customDenominationModelAssignmentsDataModel = new CustomDenominationModelsDataModelBean(listCustomDenominationModelAssignments);
					RequestContext.getCurrentInstance().update("tabView:idTableCustomDenominationModels");

					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to remove blocking: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idCustomDenominationModelsGrowl");
			refresh();
		}
	}

	public ArrayList<Offer> getListAllAvctiveIndividualOffers() {
		return listAllAvctiveIndividualOffers;
	}

	public void setListAllAvctiveIndividualOffers(
			ArrayList<Offer> listAllAvctiveIndividualOffers) {
		this.listAllAvctiveIndividualOffers = listAllAvctiveIndividualOffers;
	}

	public Offer getSelectedOfferToAssignToDenominationModel() {
		return selectedOfferToAssignToDenominationModel;
	}

	public void setSelectedOfferToAssignToDenominationModel(
			Offer selectedOfferToAssignToDenominationModel) {
		this.selectedOfferToAssignToDenominationModel = selectedOfferToAssignToDenominationModel;
	}

	public String getSelectedOfferToAssignToDenominationModelKey() {
		return selectedOfferToAssignToDenominationModelKey;
	}

	public void setSelectedOfferToAssignToDenominationModelKey(
			String selectedOfferToAssignToDenominationModelKey) {
		this.selectedOfferToAssignToDenominationModelKey = selectedOfferToAssignToDenominationModelKey;
	}

	public List<DenominationModelEntity> getListDenominationModels() {
		return listDenominationModels;
	}

	public void setListDenominationModels(
			List<DenominationModelEntity> listDenominationModels) {
		this.listDenominationModels = listDenominationModels;
	}

	public int getSelectedDenominationModelId() {
		return selectedDenominationModelId;
	}

	public void setSelectedDenominationModelId(int selectedDenominationModelId) {
		this.selectedDenominationModelId = selectedDenominationModelId;
	}

	public CustomDenominationModelsDataModelBean getCustomDenominationModelAssignmentsDataModel() {
		return customDenominationModelAssignmentsDataModel;
	}

	public void setCustomDenominationModelAssignmentsDataModel(
			CustomDenominationModelsDataModelBean customDenominationModelAssignmentsDataModel) {
		this.customDenominationModelAssignmentsDataModel = customDenominationModelAssignmentsDataModel;
	}
	
}

