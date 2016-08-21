package is.web.beans.denomination;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignment;
import is.ejb.bl.denominationModels.CustomDenominationModelAssignments;
import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.denominationModels.DenominationModelTable;
import is.ejb.bl.denominationModels.SerDeCustomDenominationModelAssignments;
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
import is.ejb.dl.dao.DAOCustomDenominationModel;
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
import is.ejb.dl.entities.CustomDenominationModelEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.beans.offers.AdProviderDataModelBean;
import is.web.beans.offers.SingleOfferWallConfigurationDataModelBean;
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

@ManagedBean(name="denominationModelBean")
@SessionScoped
public class DenominationModelBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;
	
	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private SerDeDenominationModelTable serDeDenominationModelTable; 
	
	@Inject
	private DAOCurrencyCode daoCurrencyCode;
	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;
	private CurrencyCodeEntity currencyConfigurationEntity;
	private CurrencyCodes currencyCodes = new CurrencyCodes(); 
	private ArrayList<CurrencyCode> listCurrencyCodes = new ArrayList<CurrencyCode>();
	
	@Inject
	private DAODenominationModel daoDenominationModel;
	private List<RewardTypeEntity> listRewardTypes = new ArrayList<RewardTypeEntity>();
	private List<DenominationModelEntity> listDenominationModels = new ArrayList<DenominationModelEntity>();
	private ArrayList<DenominationModelRow> listDenominationModelRows = new ArrayList<DenominationModelRow>();
	private DenominationModelRow editedDenominationModelRow = new DenominationModelRow();
	private DenominationModelRow createdDenominationModelRow = new DenominationModelRow();
	
	private DenominationDataModelBean denominationDataModel;
	private DenominationTableDataModelBean denominationTableDataModel;

	private DenominationModelEntity editedModel = new DenominationModelEntity();
	private DenominationModelEntity createdModel = new DenominationModelEntity();
	private DenominationModelEntity selectedModelForCustomOfferAssignment = new DenominationModelEntity();

	//for custom denomination model assignments (used when checking if such assignments exist during global dm removal)
	@Inject
	private DAOCustomDenominationModel daoCustomDenominationModel;
	@Inject
	private SerDeCustomDenominationModelAssignments serDeCustomDenominationModelAssignments;
	private CustomDenominationModelEntity customDenominationModelEntity = null;
	private CustomDenominationModelAssignments customDenominationModelAssignmentsDataHolder = null;
	private ArrayList<CustomDenominationModelAssignment> listCustomDenominationModelAssignments = null;

	private String strNewDenominationModelName = "";
	
	public DenominationModelBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   denominationTableDataModel = new DenominationTableDataModelBean(listDenominationModelRows);
	   //create fake 
	   createdModel = new DenominationModelEntity();
	   createdModel.setName(" ");
	   createdModel.setRewardTypeName(" ");
	   createdModel.setCountryCode(" ");
	   createdModel.setGenerationDate(new Timestamp(System.currentTimeMillis()));
	   createdModel.setRealm(loginBean.getUser().getRealm());
	   createdModel.setCountryCode(" ");

	   editedModel = new DenominationModelEntity();
	   editedModel.setName(" ");
	   editedModel.setRewardTypeName(" ");
	   editedModel.setCountryCode(" ");
	   editedModel.setGenerationDate(new Timestamp(System.currentTimeMillis()));
	   editedModel.setRealm(loginBean.getUser().getRealm());
	   editedModel.setCountryCode(" ");
   }

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			currencyConfigurationEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
			currencyCodes = serDeCurrencyCode.deserialize(currencyConfigurationEntity.getSupportedCurrencies());
			listCurrencyCodes = currencyCodes.getListCodes();
			
			listDenominationModels = (List<DenominationModelEntity>)daoDenominationModel.findAllByRealmId(loginBean.getUser().getRealm().getId());
			listRewardTypes = daoRewardType.findAllByRealmId(loginBean.getUser().getRealm().getId());
			logger.info("identified denomination models: "+listDenominationModels.size());
			denominationDataModel = new DenominationDataModelBean(listDenominationModels);
			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
	}

	public void create() {
		logger.info("creating denomination model: "+createdModel.getName());
		
		try {
			if(createdModel.getName().trim().length()==0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide name for configured Denomination Model"));
				RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
				return;
			}
			
			//if denomination model with given name exists for this realm - abort creation!
			DenominationModelEntity foundModel = daoDenominationModel.findByNameAndRealmId(createdModel.getName(), loginBean.getUser().getRealm().getId());
			if(foundModel != null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Denomination Model: "+createdModel.getName()+" with given name already exists. Please provide different name"));
				RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
				return;
			}
			
			//if denomination model for the same reward type and source payoff exists - abort creation as this would be duplicate model!
			List<DenominationModelEntity> foundModels = daoDenominationModel.findByRewardTypeNameAndRealmId(createdModel.getRewardTypeName(), loginBean.getUser().getRealm().getId());
			if(foundModels != null) {
				Iterator i = foundModels.iterator();
				while(i.hasNext()) {
					foundModel = (DenominationModelEntity)i.next();
					//System.out.println("????????????????????? "+foundModel.getSourcePayoutCurrencyCode()+" "+createdModel.getSourcePayoutCurrencyCode());
					if(foundModel.getSourcePayoutCurrencyCode().equals(createdModel.getSourcePayoutCurrencyCode())
							&& foundModel.isDefaultModel() && createdModel.isDefaultModel()) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", 
						"Default denomination model for reward type: "+foundModel.getRewardTypeName()+
						" and source payout currency code: "+foundModel.getSourcePayoutCurrencyCode()+
						" already exists. Please make sure that payout currency code is different for the same reward type or"+
						" new denomination model is not set as default!"));
						RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
						return;
					}
				}
			}

			//creating new 
			createdModel.setRealm(loginBean.getUser().getRealm());
			sortDenominationModelRows(listDenominationModelRows);
			DenominationModelTable modelTable = new DenominationModelTable();
			modelTable.setRows(listDenominationModelRows);
			String strModelTable = serDeDenominationModelTable.serialize(modelTable);
			createdModel.setContent(strModelTable);
		    createdModel.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			createdModel = daoDenominationModel.createOrUpdate(createdModel);

			RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model: "+createdModel.getName()+" created."));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			
			//reset values for next domain creation
			createdModel = new DenominationModelEntity();
			createdModel.setName(" ");
			//reset 
			//listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
			//offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);

			//todo growl message
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create Denomination Model: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public void update() {
		logger.info("updating denomination model: "+editedModel.getName());
		
		try {
			//if denomination model for the same reward type and source payoff exists - abort creation as this would be duplicate model!
			List<DenominationModelEntity> foundModels = daoDenominationModel.findByRewardTypeNameAndRealmId(editedModel.getRewardTypeName(), loginBean.getUser().getRealm().getId());
			if(foundModels != null) {
				Iterator i = foundModels.iterator();
				while(i.hasNext()) {
					DenominationModelEntity foundModel = (DenominationModelEntity)i.next();
					//System.out.println("????????????????????? "+foundModel.getSourcePayoutCurrencyCode()+" "+editedModel.getSourcePayoutCurrencyCode());
					if((foundModel.isDefaultModel() && editedModel.isDefaultModel()) &&
							foundModel.getSourcePayoutCurrencyCode().equals(editedModel.getSourcePayoutCurrencyCode()) && 
							!foundModel.getName().equals(editedModel.getName())) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							"Failed", 
							"Denomination Model: "+editedModel.getName()+" for reward type: "+foundModel.getRewardTypeName()+" and source payout currency code: "+foundModel.getSourcePayoutCurrencyCode()+" already exists. Please make sure that payout currency code is different for the same reward type!"));
						RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
						return;
					}
				}
			}

			DenominationModelTable modelTable = new DenominationModelTable();
			sortDenominationModelRows(listDenominationModelRows);
			modelTable.setRows(listDenominationModelRows);
			String strModelTable = serDeDenominationModelTable.serialize(modelTable);
			editedModel.setContent(strModelTable);
			editedModel.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			editedModel = daoDenominationModel.createOrUpdate(editedModel);

			RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditTable");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model: "+editedModel.getName()+" successfully updated."));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			
			//reset table (in case we wanted to create new one) 
			//listDenominationModelRows = new ArrayList<DenominationModelRow>();
			//denominationTableDataModel = new DenominationTableDataModelBean(listDenominationModelRows);
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create Denomination Model: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public void setDenominationModelAsDefault() {
		logger.info("setting denomination model: "+editedModel.getName()+" as default one...");
		
		try {
			editedModel = daoDenominationModel.findById(editedModel.getId());
			editedModel.setDefaultModel(true);
			editedModel = daoDenominationModel.createOrUpdate(editedModel);

			/**
			 * if denomination model for the same reward type and source payoff exists
			 * check if it is set as default and if so - set as non-default 
			 */
			List<DenominationModelEntity> foundModels = daoDenominationModel.findByRewardTypeNameAndRealmId(editedModel.getRewardTypeName(), loginBean.getUser().getRealm().getId());
			if(foundModels != null) {
				Iterator i = foundModels.iterator();
				while(i.hasNext()) {
					DenominationModelEntity foundModel = (DenominationModelEntity)i.next();
					//System.out.println("????????????????????? "+foundModel.getSourcePayoutCurrencyCode()+" "+editedModel.getSourcePayoutCurrencyCode());
					if(!foundModel.getName().equals(editedModel.getName()) &&
							foundModel.getSourcePayoutCurrencyCode().equals(editedModel.getSourcePayoutCurrencyCode()) &&
							foundModel.isDefaultModel()) {
						foundModel = daoDenominationModel.findById(foundModel.getId());
						foundModel.setDefaultModel(false);
						foundModel = daoDenominationModel.createOrUpdate(foundModel);
					}
				}
			}
			
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditFields");
			//RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditTable");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model: "+editedModel.getName()+" successfully set as default."));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to set edited denomination model as default: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public void delete() {
		logger.info("deleting denomination model: "+editedDenominationModelRow.getName());
		
		try {
			//first check if this model is not being used in custom denomination model assignment for individual offers - if it is - do not allow for it removal
			if(isDenominationModelUsedForCustomOfferAssignments(editedModel)) {
				logger.info("not allowed to delete as custom dm assignments have been identified...");
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Denomination Model: "+editedModel.getName()+" assigned to custom offers - please remove the assignment before removing this model!"));
				RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
				
				return;
			}
			
			editedModel = daoDenominationModel.findById(editedModel.getId());
			editedModel.setRealm(null);
			editedModel = daoDenominationModel.createOrUpdate(editedModel);
			daoDenominationModel.delete(editedModel);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model: "+editedModel.getName()+" successfully deleted."));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to delete Denomination Model: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			refresh();
		}
	}
	
	private boolean isDenominationModelUsedForCustomOfferAssignments(DenominationModelEntity denominationModel) {
		try {
			customDenominationModelEntity = daoCustomDenominationModel.findByRealmId(loginBean.getUser().getRealm().getId());
			customDenominationModelAssignmentsDataHolder = serDeCustomDenominationModelAssignments.deserialize(customDenominationModelEntity.getContent());
			listCustomDenominationModelAssignments = customDenominationModelAssignmentsDataHolder.getListCustomDenominationModelAssignments();

			for(int i=0;i<listCustomDenominationModelAssignments.size();i++) {
				CustomDenominationModelAssignment customDMA = listCustomDenominationModelAssignments.get(i);
				if(customDMA.getDenominationModelId() == denominationModel.getId()) {
					return true;
				}
			}
			return false;
		} catch(Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to verify custom Denomination Model assignments : "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			return false;
		}
	}
	
	public void createNewDenominationBasedOnCurrentlyEditedOne() {
		try {
			logger.info("creating new denomination model with name: "+strNewDenominationModelName);
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelTable");
			DenominationModelEntity newModel = new DenominationModelEntity();
			newModel.setName(strNewDenominationModelName);
			newModel.setDefaultModel(false);
			
			
			if(newModel.getName().trim().length()==0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
						"Failed", 
						"Please provide name for configured Denomination Model"));
				RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
				return;
			}
			
			//if denomination model with given name exists for this realm - abort creation!
			DenominationModelEntity foundModel = daoDenominationModel.findByNameAndRealmId(newModel.getName(), loginBean.getUser().getRealm().getId());
			if(foundModel != null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
						"Failed", 
						"Denomination Model: "+newModel.getName()+" with given name already exists. Please provide different name"));
				RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
				return;
			}
			
			//creating new 
			newModel.setRealm(loginBean.getUser().getRealm());
			sortDenominationModelRows(listDenominationModelRows);
			DenominationModelTable modelTable = new DenominationModelTable();
			modelTable.setRows(listDenominationModelRows);
			String strModelTable = serDeDenominationModelTable.serialize(modelTable);
			newModel.setContent(strModelTable);
			newModel.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			newModel.setRewardTypeName(editedModel.getRewardTypeName());
			newModel.setSourcePayoutCurrencyCode(editedModel.getSourcePayoutCurrencyCode());
			newModel.setTargetPayoutCurrencyCode(editedModel.getTargetPayoutCurrencyCode());
			newModel = daoDenominationModel.createOrUpdate(newModel);

			RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
					"Success", "Denomination Model: "+newModel.getName()+" created."));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			
			//reset values for next domain creation
			createdModel = new DenominationModelEntity();
			createdModel.setName(" ");
			//reset 
			//listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
			//offerWallConfigurationsDataModel = new SingleOfferWallConfigurationDataModelBean(listSingleOfferWallConfigurations);

			//todo growl message
			refresh();
			strNewDenominationModelName = ""; // reset name
		} catch(Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create new denomination model based on the currently edited one. Error: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}
	
	private void sortDenominationModelRows(ArrayList<DenominationModelRow> listDenominationModelRows) {
		logger.info("sorting denomination model");
		for(int j=0;j<listDenominationModelRows.size();j++) {
			for(int i=0;i<listDenominationModelRows.size()-1;i++) {
				DenominationModelRow row1 = listDenominationModelRows.get(i);
				DenominationModelRow row2 = listDenominationModelRows.get(i+1);
				if(row1.getSourceOfferPayoffValue() > row2.getSourceOfferPayoffValue()) {
					listDenominationModelRows.set(i, row2);
					listDenominationModelRows.set(i+1, row1);
				}
			}
		}
	}
	
	public void setEditedDenominationModel(DenominationModelEntity model) {
		logger.info("setting edited denomination model: "+model.getName());
		OfferWallConfiguration offerWallConfiguration;
		try {
			DenominationModelTable modelTable = serDeDenominationModelTable.deserialize(model.getContent());
			listDenominationModelRows = modelTable.getRows();
			denominationTableDataModel = new DenominationTableDataModelBean(listDenominationModelRows);
			editedModel = model;
			logger.info("number of individual offer wall configurations: "+listDenominationModelRows.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to open edit dialog for Denomination Model: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public void editTableRow(DenominationModelRow row) {
		logger.info("setting edited denomination model row: "+row.getSourceOfferPayoffValue());
		editedDenominationModelRow = row;
		RequestContext.getCurrentInstance().update("tabView:idModelRowEditGrid");
	}

	public void clearDenominationTable() {
		logger.info("clearing denomination table rows as create button was clicked...");
		listDenominationModelRows = new ArrayList<DenominationModelRow>();
		denominationTableDataModel = new DenominationTableDataModelBean(listDenominationModelRows);
		RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
	}
	
	public void createDenominationModelRowEntry() {
		logger.info("creating denomination model row entry: "+createdDenominationModelRow.getName()+" total rows count: "+listDenominationModelRows.size());
		boolean offerAlreadyExists = false;
		try {
			for(int i=0;i<listDenominationModelRows.size();i++) {
				if(listDenominationModelRows.get(i).getName().trim().equals(createdDenominationModelRow.getName().trim())) {
					System.out.println("checking: "+listDenominationModelRows.get(i).getName()+" "+createdDenominationModelRow.getName());
					offerAlreadyExists = true;	
					break;
				}
			}

			if(!offerAlreadyExists) {
				listDenominationModelRows.add(createdDenominationModelRow);
				//create new object
				createdDenominationModelRow = new DenominationModelRow();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New Denomination Model Row successfully added"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Denomination Model Row entry already exists."));
			}
			
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditTable");
			//RequestContext.getCurrentInstance().update("tabView:idModelRowSetupDialog");
			//RequestContext.getCurrentInstance().update("tabView:idOfferWallConfigurationsEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to add new Denomination Model Row entry: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public void deleteTableRow(String rowName) {
		logger.info("deleting model row entry with name: "+rowName);
		
		try {
			for(int i=0;i<listDenominationModelRows.size();i++) {
				if(listDenominationModelRows.get(i).getName().equals(rowName)) {
					listDenominationModelRows.remove(i);
					System.out.println("removed...");
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model Row Entry successfully removed"));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to remove Denomination Model Row Entry: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idOfferWallGrowl");
		}
	}

	public void moveUpTableRow(String rowName) {
		logger.info("moving up individual row: "+rowName);
		
		try {
			for(int i=0;i<listDenominationModelRows.size();i++) {
				if(listDenominationModelRows.get(i).getName().equals(rowName)) {
					if(i>0) {
						System.out.println("moving up...");
						DenominationModelRow ofcToMove = listDenominationModelRows.get(i);
						DenominationModelRow ofc = listDenominationModelRows.get(i-1);
						listDenominationModelRows.set(i-1, ofcToMove);
						listDenominationModelRows.set(i, ofc);
					} else {
						System.out.println("already up...");						
					}
					
				}
			}

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Denomination Model Row successfully removed"));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelSettingsTable");
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelEditTable");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to move Denomination Model Row : "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
		}
	}

	public DenominationDataModelBean getDenominationDataModel() {
		return denominationDataModel;
	}

	public void setDenominationDataModel(
			DenominationDataModelBean denominationDataModel) {
		this.denominationDataModel = denominationDataModel;
	}

	public DenominationModelEntity getEditedModel() {
		return editedModel;
	}

	public void setEditedModel(DenominationModelEntity editedModel) {
		this.editedModel = editedModel;
	}

	public DenominationModelEntity getCreatedModel() {
		return createdModel;
	}

	public void setCreatedModel(DenominationModelEntity createdModel) {
		this.createdModel = createdModel;
	}

	public DenominationTableDataModelBean getDenominationTableDataModel() {
		return denominationTableDataModel;
	}

	public void setDenominationTableDataModel(
			DenominationTableDataModelBean denominationTableDataModel) {
		this.denominationTableDataModel = denominationTableDataModel;
	}

	public DenominationModelRow getEditedDenominationModelRow() {
		return editedDenominationModelRow;
	}

	public void setEditedDenominationModelRow(
			DenominationModelRow editedDenominationModelRow) {
		this.editedDenominationModelRow = editedDenominationModelRow;
	}

	public DenominationModelRow getCreatedDenominationModelRow() {
		return createdDenominationModelRow;
	}

	public void setCreatedDenominationModelRow(
			DenominationModelRow createdDenominationModelRow) {
		this.createdDenominationModelRow = createdDenominationModelRow;
	}

	public List<RewardTypeEntity> getListRewardTypes() {
		return listRewardTypes;
	}

	public void setListRewardTypes(List<RewardTypeEntity> listRewardTypes) {
		this.listRewardTypes = listRewardTypes;
	}

	public ArrayList<CurrencyCode> getListCurrencyCodes() {
		return listCurrencyCodes;
	}

	public void setListCurrencyCodes(ArrayList<CurrencyCode> listCurrencyCodes) {
		this.listCurrencyCodes = listCurrencyCodes;
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

	public String getStrNewDenominationModelName() {
		return strNewDenominationModelName;
	}

	public void setStrNewDenominationModelName(String strNewDenominationModelName) {
		this.strNewDenominationModelName = strNewDenominationModelName;
	}
	
}

