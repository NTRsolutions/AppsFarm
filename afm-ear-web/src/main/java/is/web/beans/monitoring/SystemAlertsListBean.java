package is.web.beans.monitoring;

import is.ejb.bl.business.Constants;
import is.ejb.dl.dao.DAOSystemAlert;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.SystemAlertEntity;
import is.ejb.dl.entities.UserEntity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@ManagedBean(name="systemAlertsListBean")
@RequestScoped
public class SystemAlertsListBean implements Serializable {

	@Inject
	private Logger logger;

	private Collection<SystemAlertEntity> listAlerts;
	
//	private DomainTreeBean treeBean;
//	private TabBean tabBean;

	@Inject
	private DAOUser daoCustomer;
	@Inject
	private DAOSystemAlert daoSystemAlert;
	
	private UserEntity customer;

	public SystemAlertsListBean() {
	}
	
   @PostConstruct
   public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
//	   treeBean = (DomainTreeBean)fc.getApplication().evaluateExpressionGet(fc, "#{domainTreeBean}", DomainTreeBean.class);
//	   tabBean = (TabBean)  fc.getApplication().evaluateExpressionGet(fc, "#{tabBean}", TabBean.class);
	   
	   setDefaultDomain();
	   
	   //regenerate list
	   refresh();
   }

   //return default domain for which we will display list of nodes at the startup of this tab (first domain with >0 nodes)
   private void setDefaultDomain() {
		try {
			customer = daoCustomer.findByNameAndCredentials("BT", "admin", "admin");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("error when getting default domain: "+e.toString());
		}
   }
   
	public void refresh() {
		//retrieve reference of an objection from session
		logger.info("**************************** updating system alerts list ");
		try {
			listAlerts = daoSystemAlert.findAll();
			//refresh UI
			RequestContext.getCurrentInstance().update("tabView:alertsListTable");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe("error listing domain nodes: "+e.toString());
		}
	}

	public void deleteAlert(SystemAlertEntity alert) {
		   logger.info("=========> deleting alert: "+alert.getContent());
		   try {
			   daoSystemAlert.delete(alert);
				//refresh UI
				refresh();
		   } catch(Exception e) {
			   e.printStackTrace();
			   logger.severe(e.toString());
		   }
	   }
	   

	public Collection<SystemAlertEntity> getListAlerts() {
		return listAlerts;
	}

	public void setListAlerts(Collection<SystemAlertEntity> listAlerts) {
		this.listAlerts = listAlerts;
	}
		
}
				