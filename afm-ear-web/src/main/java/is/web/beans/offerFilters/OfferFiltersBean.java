package is.web.beans.offerFilters;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
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
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
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

@ManagedBean(name="offerFiltersBean")
@SessionScoped
public class OfferFiltersBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;

	private RealmEntity realm = null;

	@Inject
	private DAOOfferFilter daoOfferFilter;
	private OfferFilterEntity offerFilter;
	
	public OfferFiltersBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   realm = loginBean.getUser().getRealm();

	   refresh();
   }

   
	public void refresh() {
		try {
			logger.info("refreshing bean...");
			
		    offerFilter = daoOfferFilter.findByRealmId(loginBean.getUser().getRealm().getId());
		    if(offerFilter == null) {
		    	offerFilter = new OfferFilterEntity();
		    	offerFilter.setRealm(realm);
		    }

			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idOfferWall");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			//e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}

	public void update() {
		   try {
			   logger.info("updating bean");
			   //update
			   daoOfferFilter.createOrUpdate(offerFilter);
			   
	     	   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Offer filters successfully updated"));
	     	   RequestContext.getCurrentInstance().update("tabView:idSetupOfferFiltersGrowl");
		   } catch(Exception exc) {
		    	   FacesMessage msg = new FacesMessage("Error", "Error performing update, error: "+exc.toString());  
		    	   FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
         } 
	}
	
	public OfferFilterEntity getOfferFilter() {
		return offerFilter;
	}

	public void setOfferFilter(OfferFilterEntity offerFilter) {
		this.offerFilter = offerFilter;
	}
	
	
}

