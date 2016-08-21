package is.web.beans.security;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserRoles;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOLicense;
import is.ejb.dl.entities.LicenseEntity;
import is.ejb.dl.entities.RoleEntity;
import is.web.beans.license.License;
import is.web.beans.license.LicenseManager;
import is.web.beans.license.LicenseModules;
import is.web.beans.users.LoginBean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

@ManagedBean(name="acccessFilterBean")
@SessionScoped
public class AccessFilter {
	
	private LoginBean loginBean = null;
 
	@Inject
	private LicenseManager lm;
	@Inject
	private DAOLicense daoLicense;
	private License decodedLicense = null;
	private String licenseText = "";
	private boolean licenseExpired = false;
	private boolean noLicenseFound = false;
	private String loggedUserRoleName = "";
	
	private boolean isSU = false;
	private boolean isAdmin = false;
	private boolean isUser = false;
	
	private boolean renderUsers = false;
	private boolean renderServer = false;
	private boolean renderLicese = false;
	private boolean renderTracking = true;
	private boolean renderConfiguration = true;
	
	//licensed modules
	private boolean renderModuleBackups = false;
	private boolean renderModuleGeoLocation = false;
	private boolean renderModuleTracking = false;
	private boolean renderModuleSimpleReporting = false;
	private boolean renderModuleConfiguration = false;
	private boolean renderModuleUpgrades = false;
	private boolean renderModuleMonitoring = false;
	private boolean renderModuleAdvancedReporting = false;
	
	@PostConstruct
	public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   //extract logged user role name
	   List<RoleEntity> loggedUserRoles = (List<RoleEntity>) loginBean.getUser().getRoles();
	   loggedUserRoleName = loggedUserRoles.get(0).getName(); //at the moment support only single role assignment

	   //applyLicenseFilter();
	   applyUserFilter();
	}

	//filter based on functionality provided by license
	private void applyLicenseFilter() {
		try {
			System.out.println("applying license access filter...");
	 	   LicenseEntity existingLE = daoLicense.findByTypeAndRealmId("AppLicense", loginBean.getUser().getRealm().getId());
	 	   if(existingLE == null) {
	 		   noLicenseFound = true; 
	 	   } else {
			   //decode license
			   decodedLicense = lm.decodeLicense(new String(existingLE.getContent()));
			   if(lm.getDaysToExpire(decodedLicense) <= 0) {
				   licenseExpired = true;
			   } else {
				   //process license based on functionality included within license
				   ArrayList<String> licensedModules = decodedLicense.getModulesSet();
				   if(decodedLicense.getModulesSet().contains(LicenseModules.backups.toString())) {
					   renderModuleBackups = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.geolocation.toString())) {
					   renderModuleGeoLocation = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.tracking.toString())) {
					   renderModuleTracking = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.simpleReporting.toString())) {
					   renderModuleSimpleReporting = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.configuration.toString())) {
					   renderModuleConfiguration = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.upgrades.toString())) {
					   renderModuleUpgrades = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.monitoring.toString())) {
					   renderModuleMonitoring = true;
				   }
				   if(decodedLicense.getModulesSet().contains(LicenseModules.advancedReporting.toString())) {
					   renderModuleAdvancedReporting = true;
				   }

					/*
				   for(int i=0;i<licensedModules.size();i++) {
					   String licensedModuleName = licensedModules.get(i);
					   System.out.println("dupa: "+licensedModuleName);
				   }
				   */
			   }
			   
	 	   }
	 	   
	 	   if(noLicenseFound || licenseExpired) {
				renderTracking = false;
				renderConfiguration = false;
				renderUsers = false;
	 		   //if admin - disable all functionality except license upload
	 		  if(loggedUserRoleName.equals(UserRoles.ADMIN.toString()) || loggedUserRoleName.equals(UserRoles.SUPER_USER.toString())){
	 				//renderLicese = true;
	 		   //if simple user - disable all functionality
	 		  } else if(loggedUserRoleName.equals(UserRoles.USER.toString())){
	 		  }
	 	   }
	 	   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.OK, "LOGIN_ACTIVITY successfully applied license filter for user: "+loginBean.getUser().getName()+" realm: "+loginBean.getUser().getRealm().getName()+" realm id: "+loginBean.getUser().getRealm().getId());
		} catch(Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.ERROR, "LOGIN_ACTIVITY error applying license filter for user: "+loginBean.getUser().getName()+" realm: "+loginBean.getUser().getRealm().getName()+" realm id: "+loginBean.getUser().getRealm().getId()+" Error: "+exc.toString());
		}
	}
	
	private void applyUserFilter() {
		System.out.println("applying user access filter...logged user role: "+loggedUserRoleName+" ["+UserRoles.SUPER_USER.toString()+"]");
		if(loggedUserRoleName.equals(UserRoles.SUPER_USER.toString())){
			isSU = true;
			renderUsers = true;
			renderServer = true;
			renderTracking = false;
			renderModuleTracking = false;
			renderConfiguration = false;
			renderModuleAdvancedReporting = false;
			renderModuleConfiguration = false;
			renderModuleGeoLocation = false;
			renderModuleMonitoring = false;
			renderModuleSimpleReporting = false;
			renderModuleUpgrades = false;
		} else if(loggedUserRoleName.equals(UserRoles.ADMIN.toString())){
			isAdmin = true;
			renderUsers = true;
			renderServer = false;
			//renderLicese = true;
		} else if(loggedUserRoleName.equals(UserRoles.USER.toString())){
			isUser = true;
			renderUsers = false;
			renderServer = false;
			//renderLicese = false;
			renderConfiguration = false;
			renderModuleAdvancedReporting = false;
			renderModuleConfiguration = false;
			renderModuleGeoLocation = false;
			renderModuleMonitoring = false;
			renderModuleSimpleReporting = false;
			renderModuleTracking = false;
			renderModuleUpgrades = false;
		}
	}

	public boolean isRenderServer() {
		return renderServer;
	}

	public void setRenderServer(boolean renderServer) {
		this.renderServer = renderServer;
	}

	public boolean isRenderUsers() {
		return renderUsers;
	}

	public void setRenderUsers(boolean renderUsers) {
		this.renderUsers = renderUsers;
	}

	public boolean isRenderLicese() {
		return renderLicese;
	}

	public void setRenderLicese(boolean renderLicese) {
		this.renderLicese = renderLicese;
	}

	public boolean isRenderModuleBackups() {
		return renderModuleBackups;
	}

	public void setRenderModuleBackups(boolean renderModuleBackups) {
		this.renderModuleBackups = renderModuleBackups;
	}

	public boolean isRenderModuleGeoLocation() {
		return renderModuleGeoLocation;
	}

	public void setRenderModuleGeoLocation(boolean renderModuleGeoLocation) {
		this.renderModuleGeoLocation = renderModuleGeoLocation;
	}

	public boolean isRenderModuleTracking() {
		return renderModuleTracking;
	}

	public void setRenderModuleTracking(boolean renderModuleTracking) {
		this.renderModuleTracking = renderModuleTracking;
	}

	public boolean isRenderModuleSimpleReporting() {
		return renderModuleSimpleReporting;
	}

	public void setRenderModuleSimpleReporting(boolean renderModuleSimpleReporting) {
		this.renderModuleSimpleReporting = renderModuleSimpleReporting;
	}

	public boolean isRenderModuleConfiguration() {
		return renderModuleConfiguration;
	}

	public void setRenderModuleConfiguration(boolean renderModuleConfiguration) {
		this.renderModuleConfiguration = renderModuleConfiguration;
	}

	public boolean isRenderModuleUpgrades() {
		return renderModuleUpgrades;
	}

	public void setRenderModuleUpgrades(boolean renderModuleUpgrades) {
		this.renderModuleUpgrades = renderModuleUpgrades;
	}

	public boolean isRenderModuleMonitoring() {
		return renderModuleMonitoring;
	}

	public void setRenderModuleMonitoring(boolean renderModuleMonitoring) {
		this.renderModuleMonitoring = renderModuleMonitoring;
	}

	public boolean isRenderModuleAdvancedReporting() {
		return renderModuleAdvancedReporting;
	}

	public void setRenderModuleAdvancedReporting(
			boolean renderModuleAdvancedReporting) {
		this.renderModuleAdvancedReporting = renderModuleAdvancedReporting;
	}

	public boolean isRenderTracking() {
		return renderTracking;
	}

	public void setRenderTracking(boolean renderTracking) {
		this.renderTracking = renderTracking;
	}

	public boolean isRenderConfiguration() {
		return renderConfiguration;
	}

	public void setRenderConfiguration(boolean renderConfiguration) {
		this.renderConfiguration = renderConfiguration;
	}

	
	
}
