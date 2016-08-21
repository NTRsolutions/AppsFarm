package is.web.beans.offerCurrencies;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
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
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.offers.OfferWallDataModelBean;
import is.web.beans.offers.SingleOfferWallConfigurationDataModelBean;
import is.web.beans.system.CurrencyTableDataModelBean;
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

@ManagedBean(name="offerCurrenciesBean")
@SessionScoped
public class OfferCurrenciesBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;

	private RealmEntity realm = null;

	@Inject
	private DAOCurrencyCode daoCurrencyCode;
	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;

	private CurrencyCodeEntity currencyConfigurationEntity;
	private CurrencyCodes currencyCodes = new CurrencyCodes(); 
	private ArrayList<CurrencyCode> listCurrencyCode = new ArrayList<CurrencyCode>();
	private CurrencyCode editedCurrencyCode = new CurrencyCode();
	private CurrencyCode createdCurrencyCode = new CurrencyCode();
	
	private CurrencyTableDataModelBean currencyTableDataModelBean;

	public OfferCurrenciesBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		realm = loginBean.getUser().getRealm();
		
		try {
			currencyConfigurationEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
		    if(currencyConfigurationEntity == null) {
		    	currencyConfigurationEntity = new CurrencyCodeEntity();
		    	currencyConfigurationEntity.setRealm(realm);
		    	daoCurrencyCode.createOrUpdate(currencyConfigurationEntity);
		    }
		    //read currency codes and deserilize them
		    if(currencyConfigurationEntity.getSupportedCurrencies() != null && currencyConfigurationEntity.getSupportedCurrencies().length() >0){
		    	currencyCodes = serDeCurrencyCode.deserialize(currencyConfigurationEntity.getSupportedCurrencies());
		    	listCurrencyCode = currencyCodes.getListCodes();
			    currencyTableDataModelBean = new CurrencyTableDataModelBean(currencyCodes.getListCodes());
			    logger.info("successfuly deserialised list of currency codes, size: "+listCurrencyCode.size());
		    }
		} catch (Exception e) {
			logger.severe(e.toString());
			e.printStackTrace();
   	    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Unable to retrieve monitoring setup: "+e.toString()));
   	    RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
		}

	   refresh();
   }

   
	public void refresh() {
		try {
			logger.info("refreshing bean...");
		    //read currency codes and deserilize them
			currencyConfigurationEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
		    if(currencyConfigurationEntity.getSupportedCurrencies() != null && currencyConfigurationEntity.getSupportedCurrencies().length() >0){
		    	currencyCodes = serDeCurrencyCode.deserialize(currencyConfigurationEntity.getSupportedCurrencies());
			    currencyTableDataModelBean = new CurrencyTableDataModelBean(currencyCodes.getListCodes());
		    }

		    //refresh tab GUI after model update
     	    //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Currency settings successfuly loaded"));
     	    //RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
	 	    RequestContext.getCurrentInstance().update("tabView:idCurrencyTable");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			//e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}
	
	public void update() {
		try {
			   logger.info("updating bean, currency code list size: "+listCurrencyCode.size());
			   currencyConfigurationEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
			   CurrencyCodes currencyCodes = new CurrencyCodes();
			   currencyCodes.setListCodes(listCurrencyCode);
			   String strCurrencySettings = serDeCurrencyCode.serialize(currencyCodes);
			   currencyConfigurationEntity.setSupportedCurrencies(strCurrencySettings);
			   currencyConfigurationEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			   //update
			   daoCurrencyCode.createOrUpdate(currencyConfigurationEntity);
			   RequestContext.getCurrentInstance().update("tabView:idCurrencyTable");
	     	   //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Supported currencies successfully updated"));
	     	   //RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
		   } catch(Exception exc) {
		    	   FacesMessage msg = new FacesMessage("Error", "Error performing update, error: "+exc.toString());  
		    	   FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
	     } 
	}

	public void updateEditedCurrency() {
		try {
			   logger.info("updating bean, currency code list size: "+listCurrencyCode.size());
			   
			   for(int i=0;i<listCurrencyCode.size();i++) {
				   CurrencyCode currencyCode = listCurrencyCode.get(i);
				   if(currencyCode.getCode().equals(editedCurrencyCode.getCode())){
					   currencyCode.setPayoutTreshold(editedCurrencyCode.getPayoutTreshold());
					   currencyCode.setInstantRewardTreshold(editedCurrencyCode.getInstantRewardTreshold());
				   }
			   }

			   currencyConfigurationEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
			   CurrencyCodes currencyCodes = new CurrencyCodes();
			   currencyCodes.setListCodes(listCurrencyCode);
			   String strCurrencySettings = serDeCurrencyCode.serialize(currencyCodes);
			   currencyConfigurationEntity.setSupportedCurrencies(strCurrencySettings);
			   currencyConfigurationEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			   //update
			   currencyConfigurationEntity = daoCurrencyCode.createOrUpdate(currencyConfigurationEntity);
			   RequestContext.getCurrentInstance().update("tabView:idCurrencyTable");
	     	   //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Supported currencies successfully updated"));
	     	   //RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
		   } catch(Exception exc) {
		    	   FacesMessage msg = new FacesMessage("Error", "Error performing update, error: "+exc.toString());  
		    	   FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
	     } 
	}

	public void deleteCurrency(CurrencyCode currency) {
		try {
			   logger.info("deleting currency with code: "+currency.getCode());
				for(int i=0;i<listCurrencyCode.size();i++) {
					CurrencyCode existingCode = listCurrencyCode.get(i);
					if(existingCode.getCode().equals(currency.getCode())) { 
						listCurrencyCode.remove(i);
						logger.info("removed currency: "+currency.getCode());
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Currency code: "+createdCurrencyCode.getCode()+" successfully removed"));
			    	    RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
						break;
					}
				}

				currencyTableDataModelBean = new CurrencyTableDataModelBean(listCurrencyCode);
		 	    update();
		   } catch(Exception exc) {
		    	   FacesMessage msg = new FacesMessage("Error", "Error performing currency removal, error: "+exc.toString());  
		    	   FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
	     } 
	}
	
	public void setEditedCurrency(CurrencyCode currency) {
		this.editedCurrencyCode = currency;
		RequestContext.getCurrentInstance().update("tabView:idEditCurrency");
	}

	
	public void addCurrency() {
		logger.info("adding new currency: "+createdCurrencyCode.getCode());
		boolean exists = false;
		for(int i=0;i<listCurrencyCode.size();i++) {
			CurrencyCode existingCode = listCurrencyCode.get(i);
			if(existingCode.getCode().equals(createdCurrencyCode.getCode())) { //abort since code exists
				exists = true;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Warning", "Currency code: "+createdCurrencyCode.getCode()+" already defined!"));
	    	    RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
				break;
			}
		}

		if(!exists) {
			listCurrencyCode.add(createdCurrencyCode);
			currencyTableDataModelBean = new CurrencyTableDataModelBean(listCurrencyCode);
     	    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Currency successfully updated"));
     	    RequestContext.getCurrentInstance().update("tabView:idSetupCurrenciesGrowl");
	 	    createdCurrencyCode = new CurrencyCode();
	 	    
	 	    update();
		} 
	}

	public CurrencyCode getCreatedCurrencyCode() {
		return createdCurrencyCode;
	}

	public void setCreatedCurrencyCode(CurrencyCode createdCurrencyCode) {
		this.createdCurrencyCode = createdCurrencyCode;
	}

	public CurrencyTableDataModelBean getCurrencyTableDataModelBean() {
		return currencyTableDataModelBean;
	}

	public void setCurrencyTableDataModelBean(
			CurrencyTableDataModelBean currencyTableDataModelBean) {
		this.currencyTableDataModelBean = currencyTableDataModelBean;
	}

	public CurrencyCode getEditedCurrencyCode() {
		return editedCurrencyCode;
	}

	public void setEditedCurrencyCode(CurrencyCode editedCurrencyCode) {
		this.editedCurrencyCode = editedCurrencyCode;
	}

	
}

