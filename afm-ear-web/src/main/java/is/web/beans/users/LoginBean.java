package is.web.beans.users;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserRoles;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.dl.dao.DAOLicense;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.LicenseEntity;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.license.License;
import is.web.beans.license.LicenseManager;
import is.web.beans.tab.SentinelTabBean;
import is.web.beans.tab.SentinelTabs;
import is.web.beans.tab.SentinelTabBean.SingleTabBean;
import is.web.geo.GeoLocation;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;

@ManagedBean(name="loginBean")
@SessionScoped
public class LoginBean implements Serializable  {
	
	@Inject
	Logger logger;

	@Inject
	private DAOUser daoUser;
	@Inject
	private SecurityManager secManager;
	private UserEntity user;

	@Inject
	private LicenseManager lm;

	@Inject
	private MaintenanceManager maintenanceManager;

	@Inject
	private DAOLicense daoLicense;
	
	private String licenseDisplayText = "";
	private License decodedLicense = null;
	private boolean licenseLoaded = false;
	private boolean licenseExpired = false;
	private String licenseText = "";
	
	private String login = "";
	private String password = "";
	
	private boolean authenticated = false;
	//store realm id here
	
	private String systemStatusMessage = "";
	
	public LoginBean() {
	}

	@PostConstruct
	public void init() {
	}
	
	public void authenticate() {
		FacesMessage msg = null;  
		logger.info("authenticating user: "+login);
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.OK, "LOGIN_ACTIVITY authenticating user: "+login);		
	   if(login==null || login.length()==0 || password==null || password.length()==0) {
		   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Please make sure that login and password fields are not empty!"));
		   RequestContext.getCurrentInstance().update("idLogonGrowl");
	   } else {
		   //find user with provided credentials in db
		   try {
			   user = daoUser.findByCredentials(login);
			   if(user != null) {
				   authenticated = secManager.validatePassword(password, user.getPassword());
				   if(authenticated) {
					   msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Logging in as", login);
					   try {
						   licenseLoaded = true;
						   licenseExpired = false;
						   /*
				    	   LicenseEntity existingLE = daoLicense.findByTypeAndRealmId("AppLicense", user.getRealm().getId());
				    	   if(existingLE == null) {
				    		   licenseText = "No license found. Activate the license to use the product!";
				    		   licenseLoaded = false;
				    		   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.OK, "LOGIN_ACTIVITY no license found for user: "+login+" realm: "+user.getRealm().getName()+" realm id: "+user.getRealm().getId());
				    	   } else {
				    		   licenseText = new String(existingLE.getContent());
				    		   //decode license
				    		   decodedLicense = lm.decodeLicense(licenseText);
				    		   licenseLoaded = true;
				    		   if(lm.getDaysToExpire(decodedLicense) <= 0) {
				    			   licenseExpired = true;
				    		   }
				    		   if(licenseExpired) {
				    			   licenseText = "License expired! Please renew your license (via admin account) in order to use the product.";   
				    		   } else {
				    			   licenseText = "License expires in "+lm.getDaysToExpire(decodedLicense)+" days";   
				    		   }
				    	   }
						   */
			    		   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.OK, "LOGIN_ACTIVITY successfully decoded license for user: "+login+" realm: "+user.getRealm().getName()+" realm id: "+user.getRealm().getId());
					   } catch(Exception exc) {
						   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.ERROR, "LOGIN_ACTIVITY error retrieving license for user: "+login+" realm: "+user.getRealm().getName()+" realm id: "+user.getRealm().getId()+" error: "+exc.toString());
					   }
				   }
			   } else {
				   authenticated = false;
				   msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error", "Invalid credentials");  
			   }
		   } catch(Exception exc) {
			   authenticated = false;
			   msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error", "Unable to retrieve user data from DB: "+exc.toString());  
			   exc.printStackTrace();
		   }

		   /*
		   if(name.equals(nameAuth) && password.equals(passwordAuth)) {
			   authenticated = true;
			   msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", name);
		   } else {
			   authenticated = false;
			   msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error", "Invalid credentials");  
		   }
		   */

		   if(authenticated) {
			   generateSystemAlertMessages();
			   try {
				   if(!licenseExpired || user.getRolesString().equals(UserRoles.SUPER_USER.toString()) || user.getRolesString().equals(UserRoles.ADMIN.toString())) { //redirect always if license not expired or SU type of user
		        	   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, user.getRealm().getId(), LogStatus.OK, "LOGIN_ACTIVITY user with login: "+login+" successfully authenticated, redirecting to main page...");        		
					   logger.info("user authenticated, redirecting to main page...");
					   //roles are created only by me (no gui for adding new roles)
					   RequestContext context = RequestContext.getCurrentInstance();
					   FacesContext.getCurrentInstance().addMessage(null, msg);   
				       context.addCallbackParam("loggedIn", authenticated);  

					   FacesContext.getCurrentInstance().getExternalContext().redirect("main.jsf");
				   } else if(!user.getRolesString().equals(UserRoles.SUPER_USER.toString()) && licenseExpired) { //do not redirect if non-SU user and license expired
					   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "License expired!", "Please renew your license (via admin account) in order to use the product."));
					   RequestContext.getCurrentInstance().update("idLogonGrowl");
					   
					   //roles are created only by me (no gui for adding new roles)
					   RequestContext context = RequestContext.getCurrentInstance();
					   FacesContext.getCurrentInstance().addMessage(null, msg);   
				       context.addCallbackParam("loggedIn", !authenticated);  
				   } 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    
		   } else {
			   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", "Please make sure that login and password fields are correct and not empty!"));
			   RequestContext.getCurrentInstance().update("idLogonGrowl");

			   //show growl message
			   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.WARNING, "LOGIN_ACTIVITY user authenticated, Username: "+login+" or password do not match");        		
		   }
	   }
	}

	public void generateSystemAlertMessages() {
		try {
			if(Application.isMaintenanceEnabled()) {
				//read maintenance config data
				MaintenanceConfigurationEntity mce = maintenanceManager.getMaintenanceConfiguration();
				long timeDistance = (mce.getConfigurationHolder().getPostActivationDate().getTime() - System.currentTimeMillis())/1000/60; //in minutes
				systemStatusMessage = "System is in maintenance mode and will be brought back into full production mode at: "+mce.getConfigurationHolder().getPostActivationDate().toString()+" server time (in: "+timeDistance+" minutes)";
			} else {
				systemStatusMessage = "";
			}
		} catch(Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, user.getRealm().getId(), LogStatus.OK, "LOGIN_ACTIVITY user with login: "+login+" error reading server maintenance configuration: "+exc.toString());
			exc.printStackTrace();
		}
	}
	
	public void logout() {
 	   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, user.getRealm().getId(), LogStatus.OK, "LOGIN_ACTIVITY user with login: "+login+" signed out...redirecting to logon page...");        		
 	   FacesContext context = FacesContext.getCurrentInstance();
 	   HttpSession session = (HttpSession)context.getExternalContext().getSession(false);
 	   session.invalidate();
 	   try {
 		   FacesContext.getCurrentInstance().getExternalContext().redirect("index.jsf");
		
 	   } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 	   }
	}

	//called when su updates the license and refreshes license and its info on top right UI corner
	public void refreshLicenseText(){
		   try {
	    	   LicenseEntity existingLE = daoLicense.findByType("AppLicense");
	    	   if(existingLE == null) {
	    		   licenseText = "No license found. Activate the license to use the product!";
	    		   licenseLoaded = false;
	    		   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.OK, "LOGIN_ACTIVITY no license found for user: "+login+" realm: "+user.getRealm().getName()+" realm id: "+user.getRealm().getId());
	    	   } else {
	    		   licenseText = new String(existingLE.getContent());
	    	   }

    		   //decode license
    		   decodedLicense = lm.decodeLicense(licenseText);
    		   licenseLoaded = true;
    		   if(lm.getDaysToExpire(decodedLicense) <= 0) {
    			   licenseExpired = true;
    		   }
    		   licenseText = "License expires in "+lm.getDaysToExpire(decodedLicense)+" days";
			   RequestContext.getCurrentInstance().update("idTopPanelGrid");
		   } catch(Exception exc) {
			   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.ERROR, "LOGIN_ACTIVITY error retrieving license for user: "+login+" realm: "+user.getRealm().getName()+" realm id: "+user.getRealm().getId()+" error: "+exc.toString());
		   }
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserEntity getUser() {
		return user;
	}

	public String getLicenseText() {
		return licenseText;
	}

	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}

	public String getSystemStatusMessage() {
		return systemStatusMessage;
	}

	public void setSystemStatusMessage(String systemStatusMessage) {
		this.systemStatusMessage = systemStatusMessage;
	}
	
}
                    