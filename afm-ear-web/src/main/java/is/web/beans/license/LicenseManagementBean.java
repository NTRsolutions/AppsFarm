package is.web.beans.license;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.dl.dao.DAOLicense;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.LicenseEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.tab.SentinelTabBean;
import is.web.beans.tab.SentinelTabs;
import is.web.beans.tab.SentinelTabBean.SingleTabBean;
import is.web.beans.users.LoginBean;
import is.web.geo.GeoLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name="licenseManagementBean")
@SessionScoped
public class LicenseManagementBean implements Serializable  {
	
	@Inject
	Logger logger;

	private LoginBean loginBean = null;
	private String activeLicenseContent = "No license activated yet.";

	@Inject
	LicenseManager lm;
	
	@Inject
	DAOLicense daoLicense;

	public LicenseManagementBean() {
	}

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	}

	public void refresh() {
       try {
    	   LicenseEntity existingLE = daoLicense.findByTypeAndRealmId("AppLicense", loginBean.getUser().getRealm().getId());
    	   if(existingLE != null) {
        	   activeLicenseContent = new String(existingLE.getContent());
    		   RequestContext.getCurrentInstance().update("tabView:idActiveLicenseContent");

    		   //decode license
    		   License decodedLicense = lm.decodeLicense(activeLicenseContent);
    		   System.out.println("Decoded license content: "+decodedLicense.getCustomerName()+" "+decodedLicense.getNumberOfManagedDevices()+" "+decodedLicense.getModulesSet().size());
    		   
        	   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Refreshed license information"));
        	   RequestContext.getCurrentInstance().update("tabView:idLicenseManagementGrowl");
    	   } else {
        	   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Warning", "No license activated - please activate the license in order to use the product."));
        	   RequestContext.getCurrentInstance().update("tabView:idLicenseManagementGrowl");
    	   }
       } catch (Exception e) {
    	   e.printStackTrace();
    	   logger.severe(e.toString());
    	   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Unable to retrieve license: "+e.toString()));
    	   RequestContext.getCurrentInstance().update("tabView:idLicenseManagementGrowl");
       }
	}

	public void handleFileUpload(FileUploadEvent event) {
		   UploadedFile file = event.getFile();
		   FacesContext ctx = FacesContext.getCurrentInstance();
		   String paramHostName = ctx.getExternalContext().getInitParameter("hostName");
		   
		   file = event.getFile();
		   ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	       String webAppContextPath = servletContext.getRealPath("");// + File.separator + "upload" + File.separator + file.getFileName();
	       
		   logger.info("************ uploaded license file: "+file.getFileName()+" size: "+file.getSize()+" content: "+file.getContents());
	       
		   StringBuilder result = new StringBuilder();
		   BufferedReader reader = null;
		   try {
		        reader = new BufferedReader(new InputStreamReader(file.getInputstream()));
		        char[] buf = new char[1024];
		        int r = 0;
		        while ((r = reader.read(buf)) != -1) {
		            result.append(buf, 0, r);
		        }
		    } catch(Exception exc) {
		    	   FacesMessage msg = new FacesMessage("Error", "The system experienced problems when uploading license file: "+
   			   			event.getFile().getFileName()+". Error: "+exc.toString());  
		    	   FacesContext.getCurrentInstance().addMessage(null, msg);  
            } finally {
		        try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }

		   String licenseContent = result.toString();
		   logger.info("license content: "+licenseContent);
		   
	       try {
	    	   LicenseEntity existingLE = daoLicense.findByTypeAndRealmId("AppLicense", loginBean.getUser().getRealm().getId());
	    	   if(existingLE == null) {
	    		   logger.info("no previous license found - creating new license entry...");
		    	   LicenseEntity newLE = new LicenseEntity();
		    	   newLE.setRealmId(loginBean.getUser().getRealm().getId());
		    	   newLE.setContent(licenseContent.getBytes());
		    	   newLE.setTimestamp(new Timestamp(System.currentTimeMillis()));
	    		   daoLicense.createOrUpdate(newLE);
	    		   activeLicenseContent = new String(newLE.getContent());
	    		   
		    	   FacesMessage msg = new FacesMessage("Succesful", "New license successfully activated! Please logout and login again for changes to take effect.");  
		           FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idLicenseManagementGrowl");
	    	   } else {
		    	   existingLE.setContent(licenseContent.getBytes());
		    	   existingLE.setTimestamp(new Timestamp(System.currentTimeMillis()));
		    	   existingLE.setRealmId(loginBean.getUser().getRealm().getId());
		    	   daoLicense.createOrUpdate(existingLE);
	    		   activeLicenseContent = new String(existingLE.getContent());
		    	   
		    	   FacesMessage msg = new FacesMessage("Succesful", "License upgraded. Please logout and login again for changes to take effect.");  
		           FacesContext.getCurrentInstance().addMessage(null, msg);
		           RequestContext.getCurrentInstance().update("tabView:idLicenseManagementGrowl");
		           
		           //reload license stored in login bean and refresh top right UI corner with license display info
		           //loginBean.refreshLicenseText();
	    	   }
	    	   
    		   RequestContext.getCurrentInstance().update("tabView:idActiveLicenseContent");
	       } catch (Exception e) {
	    	   e.printStackTrace();
	    	   logger.severe(e.toString());
	    	   FacesMessage msg = new FacesMessage("Error", "The system experienced problems when uploading license file: "+
	    			   			event.getFile().getFileName()+". Error: "+e.toString());  
	           FacesContext.getCurrentInstance().addMessage(null, msg);  
	       }
	   }

	public String getActiveLicenseContent() {
		return activeLicenseContent;
	}

	public void setActiveLicenseContent(String activeLicenseContent) {
		this.activeLicenseContent = activeLicenseContent;
	}


	
}
                    