package is.web.beans.offers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.denominationModels.DenominationModelTable;
import is.ejb.bl.denominationModels.SerDeDenominationModelTable;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.beans.denomination.DenominationDataModelBean;
import is.web.beans.denomination.DenominationTableDataModelBean;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.SortOrder;

import java.util.Map;

@ManagedBean(name="generatedOffersBean")
@SessionScoped
public class GeneratedOffersBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;
	
	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAODenominationModel daoDenominationModel;
	private List<DenominationModelEntity> listDenominationModels = new ArrayList<DenominationModelEntity>();
	
	private DenominationModelEntity selectedModelForCustomOfferAssignment = new DenominationModelEntity();

	//--------------- for custom offer-denomination model assignements (2nd tab)
	@Inject
	private DAOOfferWall daoOfferWall;
	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;
	
	private LazyDataModel<Offer> lazyOffersTableModel;

	private Offer selectedOfferForCustomDenominationModel = new Offer();

	private int idSelectedDenominationModel = -1;

	private boolean renderDialogDenominationModelAssignment = false;
	
	public GeneratedOffersBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

	   try {
		   listDenominationModels = (List<DenominationModelEntity>)daoDenominationModel.findAllByRealmId(loginBean.getUser().getRealm().getId());
		   lazyOffersTableModel = new LazyDataModel<Offer>() {
    		   @Override  
  			   public List<Offer> load(int first, int pageSize, String sortField,
  				         SortOrder sortOrder, Map<String, String> filters) {

    			ArrayList<Offer> listFilteredActiveOffers = null;
			    ArrayList<Offer> listActiveOffers = getAllActiveOffers();
	   			int totalCount = listActiveOffers.size();
	   			lazyOffersTableModel.setRowCount(totalCount);

	   			logger.info("sort field: "+sortField+" filters: "+filters);
	       		logger.info("lazy loading devices list from between: "+first+" and "+(first+pageSize)+" total devices count: "+totalCount);
    		       		
  	       		try {
  	       			if(sortField == null) { //by default sort by click date 
  	       				//sortField = "clickDate";
  	       				//sortOrder = SortOrder.DESCENDING;
  	       			}

  	       			String sortingOrder = "descending";
  	       			if (sortOrder == SortOrder.ASCENDING) {
  	 				  sortingOrder = "ascending";
  	       			} else if (sortOrder == SortOrder.DESCENDING) {
  	 				  sortingOrder = "descending";
  	       			}

  	       			//at the moment we provide no filtering
  	       			listFilteredActiveOffers = getFilteredOffers(first, pageSize, 
  		 			   			sortField, sortingOrder, 
  		 			   			filters, loginBean.getUser().getRealm().getId(), 
  		 			   			listActiveOffers);

  				} catch (Exception e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  					logger.severe(e.toString());
  				}
      			logger.info("lazy loading completed, current results returned: " + listFilteredActiveOffers.size());

      			return (List)listFilteredActiveOffers;
    	        }
    	    };  
	   } catch(Exception exc) {
		   exc.printStackTrace();
	   }
   }


	public void refresh() {
		try {
			logger.info("refreshing bean...");
			idSelectedDenominationModel = -1;
			listDenominationModels = (List<DenominationModelEntity>)daoDenominationModel.findAllByRealmId(loginBean.getUser().getRealm().getId());
			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idOffersTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
	}

	//-------------------------------------- handling customer assigned denomination models to offers (2nd tab)
	private ArrayList<Offer> getAllActiveOffers() {
		//find all active offer walls
		ArrayList<Offer> listAllAvctiveIndividualOffers = new ArrayList<Offer>();
		try {
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
						//add offer (but reject the same offers for different countries/etc
						//if(!isOfferAlreadyAdded(listAllAvctiveIndividualOffers, offer)) 
						{
							listAllAvctiveIndividualOffers.add(offer);
						}
					}
				}
			}
		} catch(Exception exc) {
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve generated offers : "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idGeneratedOffersGrowl");
		}
		
		return listAllAvctiveIndividualOffers;
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

    private ArrayList<Offer> getFilteredOffers(int first, int pageSize, 
 		   String sortField, String sortOrder, Map filters,
 		   int realmId, ArrayList<Offer> fullAvailableOffersList) {
    	
 	   ArrayList<Offer> filteredList = new ArrayList<Offer>();
 	   int startIndex = first;
 	   int endIndex = startIndex+pageSize;
 	   logger.info(" si: "+startIndex+" ei: "+endIndex+" page size: "+pageSize+" page number: " +first);
 	   for (int i=0;i<fullAvailableOffersList.size();i++) {
 		   if(i>= startIndex && i<= endIndex) {
 			   filteredList.add(fullAvailableOffersList.get(i));
 		   }
 		   if(i>endIndex) {
 			   break;
 		   }
 	   }
 	   
 	   return filteredList;
    }

    public void viewDetails(Offer offer){
		selectedOfferForCustomDenominationModel = offer;
		renderDialogDenominationModelAssignment = true;
		RequestContext.getCurrentInstance().update("tabView:idDialogOfferSetup");
		RequestContext.getCurrentInstance().update("tabView:idDialogCustomDenominationModelAssignment");
		logger.info("selected offer for customer denomination model assignment: "+offer.getTitle()+" rendering dialog: "+renderDialogDenominationModelAssignment);
    }

    public void assignCustomDenominationModel(){
    	try {
    		logger.info("assigning custom denomination model with id: "+idSelectedDenominationModel+" to offer: "+selectedOfferForCustomDenominationModel.getTitle());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination model successfully assigned to offer"));
			RequestContext.getCurrentInstance().update("tabView:idGeneratedOffersGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOffersTable");

			//reset denomination model selection in dialog box - otherwise if we remove dm it will break the ui
			idSelectedDenominationModel = -1;
			RequestContext.getCurrentInstance().update("tabView:idGeneratedOffersGrowl");
			RequestContext.getCurrentInstance().update("tabView:idOffersTable");

    	} catch(Exception exc) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to assign denomination model to offer. Error: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idGeneratedOffersGrowl");
    	}
    }

	public LazyDataModel<Offer> getLazyOffersTableModel() {
		return lazyOffersTableModel;
	}

	public void setLazyOffersTableModel(LazyDataModel<Offer> lazyOffersTableModel) {
		this.lazyOffersTableModel = lazyOffersTableModel;
	}

	public Offer getSelectedOfferForCustomDenominationModel() {
		return selectedOfferForCustomDenominationModel;
	}

	public void setSelectedOfferForCustomDenominationModel(
			Offer selectedOfferForCustomDenominationModel) {
		this.selectedOfferForCustomDenominationModel = selectedOfferForCustomDenominationModel;
	}

	public List<DenominationModelEntity> getListDenominationModels() {
		return listDenominationModels;
	}

	public void setListDenominationModels(
			List<DenominationModelEntity> listDenominationModels) {
		this.listDenominationModels = listDenominationModels;
	}

	public DenominationModelEntity getSelectedModelForCustomOfferAssignment() {
		return selectedModelForCustomOfferAssignment;
	}

	public void setSelectedModelForCustomOfferAssignment(
			DenominationModelEntity selectedModelForCustomOfferAssignment) {
		this.selectedModelForCustomOfferAssignment = selectedModelForCustomOfferAssignment;
	}

	public boolean isRenderDialogDenominationModelAssignment() {
		return renderDialogDenominationModelAssignment;
	}

	public void setRenderDialogDenominationModelAssignment(
			boolean renderDialogDenominationModelAssignment) {
		this.renderDialogDenominationModelAssignment = renderDialogDenominationModelAssignment;
	}

	public int getIdSelectedDenominationModel() {
		return idSelectedDenominationModel;
	}

	public void setIdSelectedDenominationModel(int idSelectedDenominationModel) {
		this.idSelectedDenominationModel = idSelectedDenominationModel;
	}
	
}

