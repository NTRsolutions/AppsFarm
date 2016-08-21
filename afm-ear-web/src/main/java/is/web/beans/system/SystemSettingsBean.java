package is.web.beans.system;


import is.ejb.bl.business.Application;
import is.ejb.bl.system.MaintenanceConfigurationHolder;
import is.ejb.bl.system.MaintenanceConfigurationSerDe;
import is.ejb.bl.system.MaintenanceManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAODeviceAlertsConfiguration;
import is.ejb.dl.dao.DAODeviceProfile;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.DeviceAlertsConfigurationEntity;
import is.ejb.dl.entities.DeviceProfileEntity;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.users.LoginBean;

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
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;

@ManagedBean(name="systemSettingsBean")
@SessionScoped
public class SystemSettingsBean implements Serializable {

	@Inject
	private Logger logger;

	@Inject
	private MaintenanceManager maintenanceManager;
	
	@Inject
	private DAOMaintenanceConfiguration daoMaintenanceConfiguration;

	@Inject
	private MaintenanceConfigurationSerDe maintenanceSerDe;

	private LoginBean loginBean = null;

	private String infoMaintenaceStatus = "System is in production mode";
	private String infoMaintenaceActive = "Not set";
	private String infoMaintenaceDuration = "Not set";
	private String infoMaintenaceStartDate = "Not set";
	private String infoMaintenaceEndDate = "Not set";
	private String infoMaintenaceAdvancedFunctionalityStartDate = "Not set";
	private String infoMaintenaceSetupAdjustmentDate = "Not set";
	
	//this should be set based on entity value
	private String rbCRFEnabled = "disabled"; //conversion filter
	private String rbOfferWallGeneration = "disabled";
	private String rbLoggingMode = "default";
	private String rbAutoconfMode = "disabled";
	private String rbMonitoring = "disabled";
	private String rbServerMonitoring = "disabled";
	private String rbSQLStorageSizeMonitor = "disabled";
	private String rbReporting = "disabled";
	private String rbServerStatusCollection = "disabled";
	private String rbMaintenance = "disabled";
	private String rbCpeCreationMode = "auto";
	
	private int crfTriggerIntervals = 15; //minutes
	private int offerWallGenerationIntervals = 24; //hours
	private int reportingIntervals = 1;//minutes
	private int maintenanceDuration = 60; //minutes
	private int sqlStorageSizeMonitorIntervals = 15; //minutes
	private int sqlStorageHistoryLength = 14; //days
	private String storageServerIp = "";
	private String storageServerBackupIp = "";
	private int displayedCpeLogEvents = 5;
	private int displayedAlertEvents = 5;
	private int monitoringIntervals = 1;//minutes
	private int serverStatusCollectionIntervals = 30; //seconds
	private String stunPort = "3478";

	private String rbForecasting = "disabled";
	private String forecastingServerIp = "";
	private String masterServerIp = "";
	private String imdgIp = "";
	private int forecastingIntervals = 1;//minutes

   @PostConstruct
   public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
   }

	public void refresh() {
		try {
			logger.info("refresh called on system settings bean...");
			//read maintenance status
			readMaintenanceStatus();
			
			//storage server ip
            storageServerIp = Application.getLogServerAddress();

			//storage server ip
            storageServerBackupIp = Application.getLogServerBackupAddress();

			//forecasting server ip
            forecastingServerIp = Application.getForecastingServerAddress();

			//in-memory data grid server ip
            imdgIp = Application.getIMDGServerAddress();

			//master server ip
            masterServerIp = Application.getMasterServerIp();
            System.out.println("master server ip is: "+masterServerIp);
            //stun port
            stunPort = Application.getSTUNport()+"";

            //this represents subset of conf knobs for advanced server functionality (that is disabled during maintenance)
            readAdvancedServerFunctionalityStatus();
            
            //server status collection enabled
            if(Application.isServerStatusCollectionEnabled()) {
            	rbServerStatusCollection = "enabled";
            } else {
            	rbServerStatusCollection = "disabled";
            }

            //maintenance
            if(Application.isMaintenanceEnabled()) {
            	rbMaintenance = "enabled";
            } else {
            	rbMaintenance = "disabled";
            }

            //autoregister cpe
            if(Application.getAutoCreateCpe()) {
            	rbCpeCreationMode = "auto";
            } else {
            	rbCpeCreationMode = "manual";
            }

            //crf setup
            if(Application.getCRFEnabled()) {
            	rbCRFEnabled = "enabled";
            } else {
            	rbCRFEnabled = "disabled";
            }

            //offer wall generation setup
            if(Application.getGenerateOffers()) {
            	rbOfferWallGeneration = "enabled";
            } else {
            	rbOfferWallGeneration = "disabled";
            }
 
            //sql data size monitor
            if(Application.isSQLDataSizeMonitorEnabled()) {
            	rbSQLStorageSizeMonitor = "enabled";
            } else {
            	rbSQLStorageSizeMonitor = "disabled";
            }
            
            //logging mode
            if(Application.isVerboseLogging()) {
            	rbLoggingMode = "detailed";
            } else {
            	rbLoggingMode = "default";
            }

            //log and alert events config
            displayedCpeLogEvents = Application.getDisplayedCPELogEvents();
            displayedAlertEvents = Application.getDisplayedAlertEvents();
           
            //monit intervals
            monitoringIntervals = Application.getMonitoringIntervals();
            
            //reporting intervals
            reportingIntervals = Application.getReportingIntervals();
            
            //offer generation intervals
            offerWallGenerationIntervals = Application.getOfferWallIntervals();

            //crf intervals
            crfTriggerIntervals = Application.getCRFIntervals();

            //sql storage size monitoring intervals 
            sqlStorageSizeMonitorIntervals = Application.getSQLStorageSizeMonitoringIntervals();

            //sql storage history length beyond which data is deleted 
            sqlStorageHistoryLength  = Application.getSQLStorageHistoryLenght();
            //forecasting intervals
            forecastingIntervals = Application.getForecastingIntervals();

            //maintenance duration
            maintenanceDuration = Application.getMaintenanceDuration();

            serverStatusCollectionIntervals = Application.getServerStatusCollectionIntervals();

		    FacesMessage msg = new FacesMessage("Success", "System settings sucessfully refreshed.");  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettings");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error refreshing system settings: "+e.toString());
		    FacesMessage msg = new FacesMessage("Success", "Error refreshing system settings: "+e.toString());  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		}
	}

	public void saveSettings() {
		logger.info("======================================");
		logger.info("rbCpeCreationMode: "+storageServerIp+" "+rbCpeCreationMode+" logging: "+rbLoggingMode);
		try {
            Application.setLogServerAddress(storageServerIp);
            Application.setLogServerBackupAddress(storageServerBackupIp);
            Application.setForecastingServerAddress(forecastingServerIp);
            Application.setIMDGServerAddress(imdgIp);
            Application.setMasterServerIp(masterServerIp);
            
            int nStunPort = Integer.parseInt(stunPort);
            Application.setSTUNport(nStunPort);
            
            if(rbAutoconfMode.equals("enabled")) {
            	Application.setAcEnabled(true);	
            } else {
            	Application.setAcEnabled(false);
            }

            if(rbMonitoring.equals("enabled")) {
            	Application.setMonitoringEnabled(true);	
            } else {
            	Application.setMonitoringEnabled(false);
            }

            if(rbServerMonitoring.equals("enabled")) {
            	Application.setServerMonitoringEnabled(true);	
            } else {
            	Application.setServerMonitoringEnabled(false);
            }

            if(rbSQLStorageSizeMonitor.equals("enabled")) {
            	Application.setSQLDataSizeMonitorEnabled(true);	
            } else {
            	Application.setSQLDataSizeMonitorEnabled(false);
            }

            if(rbCRFEnabled.equals("enabled")) {
            	Application.setCRFEnabled(true);	
            } else {
            	Application.setCRFEnabled(false);
            }

            if(rbOfferWallGeneration.equals("enabled")) {
            	Application.setGenerateOffers(true);	
            } else {
            	Application.setGenerateOffers(false);
            }

            if(rbReporting.equals("enabled")) {
            	Application.setReportingEnabled(true);	
            } else {
            	Application.setReportingEnabled(false);
            }

            if(rbForecasting.equals("enabled")) {
            	Application.setFcEnabled(true);	
            } else {
            	Application.setFcEnabled(false);
            }

            if(rbServerStatusCollection.equals("enabled")) {
            	Application.setServerStatusCollectionEnabled(true);	
            } else {
            	Application.setServerStatusCollectionEnabled(false);
            }

            //configure maintenance status based on existing setup
            configureMaintenanceStatus();
            
            if(rbMaintenance.equals("enabled")) {
            	Application.setMaintenanceEnabled(true);	
            } else {
            	Application.setMaintenanceEnabled(false);
            }
            
            if(rbCpeCreationMode.equals("auto")) {
            	Application.setAutoCreateCpe(true);
            } else {
            	Application.setAutoCreateCpe(false);
            }
            
            if(rbLoggingMode.equals("detailed")) {
            	Application.setVerboseLogging(true);	
            } else {
            	Application.setVerboseLogging(false);
            }

            //log and alert events config
            if(displayedCpeLogEvents < 5) {
            	displayedCpeLogEvents = 5;
            }
            Application.setDisplayedCPELogEvents(displayedCpeLogEvents);
            
            if(displayedAlertEvents < 5) {
            	displayedAlertEvents = 5;
            }
            Application.setDisplayedAlertEvents(displayedAlertEvents);

            //monitoring intervals
            if(monitoringIntervals <= 0) {
            	monitoringIntervals = 1;
            }
            Application.setMonitoringIntervas(monitoringIntervals);

            if(sqlStorageSizeMonitorIntervals < 1) {
            	sqlStorageSizeMonitorIntervals = 1;
            }
            Application.setSQLStorageSizeMonitoringIntervals(sqlStorageSizeMonitorIntervals);

            if(sqlStorageHistoryLength < 1) {
            	sqlStorageHistoryLength = 1;
            }
            Application.setSQLStorageHistoryLenght(sqlStorageHistoryLength);

            //crf generation intervals
            if(crfTriggerIntervals <= 0) {
            	crfTriggerIntervals = 1;
            }
            Application.setCRFIntervals(crfTriggerIntervals);

            //offer wall generation intervals
            if(offerWallGenerationIntervals <= 0) {
            	offerWallGenerationIntervals = 1;
            }
            Application.setOfferWallIntervals(offerWallGenerationIntervals);

            //reporting intervals
            if(reportingIntervals <= 0) {
            	reportingIntervals = 1;
            }
            Application.setReportingIntervas(reportingIntervals);

            //forecasting intervals
            if(forecastingIntervals <= 0) {
            	forecastingIntervals = 1;
            }
            Application.setForecastingIntervas(forecastingIntervals);

            //ssc intervals
            if(serverStatusCollectionIntervals < 10) {
            	serverStatusCollectionIntervals = 10;
            }
            if(serverStatusCollectionIntervals > 60) {
            	serverStatusCollectionIntervals = 60;
            }
            Application.setServerStatusCollectionIntervas(serverStatusCollectionIntervals);

            //maintenance duration
            if(maintenanceDuration < 0) {
            	maintenanceDuration = 0;
            }
            Application.setMaintenanceDuration(maintenanceDuration);

		    FacesMessage msg = new FacesMessage("Success", "System settings sucessfully saved.");  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettings");
		    
		} catch(Exception exc) {
			exc.printStackTrace();
		    FacesMessage msg = new FacesMessage("Error", "Unable to save system settings: "+exc.toString());  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		}
		
	}

	public void readAdvancedServerFunctionalityStatus() {
        //autoconf
        if(Application.isAcEnabled()) {
        	rbAutoconfMode = "enabled";
        } else {
        	rbAutoconfMode = "disabled";
        }
        
        //monitoring
        if(Application.isMonitoringEnabled()) {
        	rbMonitoring = "enabled";
        } else {
        	rbMonitoring = "disabled";
        }

        //server monitoring
        if(Application.isServerMonitoringEnabled()) {
        	rbServerMonitoring = "enabled";
        } else {
        	rbServerMonitoring = "disabled";
        }

        //reporting
        if(Application.isReportingEnabled()) {
        	rbReporting = "enabled";
        } else {
        	rbReporting = "disabled";
        }

        //forecasting
        if(Application.isFcEnabled()) {
        	rbForecasting = "enabled";
        } else {
        	rbForecasting = "disabled";
        }
	}
	
	public void configureMaintenanceStatus() {
		try {
			maintenanceManager.updateMaintenanceStatus(rbMaintenance, 
					maintenanceDuration,
					rbMonitoring,
					rbAutoconfMode,
					rbForecasting,
					rbReporting);

			//execute refresh in order to read current values of all configuration knobs (since we might have modified them via maintenance changes)
			readAdvancedServerFunctionalityStatus();
			
			RequestContext.getCurrentInstance().update("tabView:idMaintenanceStatusMessage");
		    RequestContext.getCurrentInstance().update("tabView:tabSystemSettings:idMonitoringMode");
		    RequestContext.getCurrentInstance().update("tabView:tabSystemSettings:idReportingMode");
		    RequestContext.getCurrentInstance().update("tabView:tabSystemSettings:idForecastingMode");
		    RequestContext.getCurrentInstance().update("tabView:tabSystemSettings:idAcMode");

	        //read maintenance config again and display its settings on UI
	        readMaintenanceStatus();
		} catch(Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.ERROR, "Error: "+exc.toString());
		    FacesMessage msg = new FacesMessage("Error", "Unable to update system maintenance status: "+exc.toString());  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		}
	}

	public void readMaintenanceStatus() {
		try {
			MaintenanceConfigurationEntity maintenanceConfigurationEntity = maintenanceManager.getMaintenanceConfiguration();

			if(maintenanceConfigurationEntity != null){
				if(maintenanceConfigurationEntity.isActive()) {
					MaintenanceConfigurationEntity mce = maintenanceManager.getMaintenanceConfiguration();
					long timeDistance = (mce.getConfigurationHolder().getPostActivationDate().getTime() - System.currentTimeMillis())/1000/60; //in minutes
					infoMaintenaceStatus = ""+"System is in maintenance mode and will be brought back into full production mode at: "+maintenanceConfigurationEntity.getConfigurationHolder().getPostActivationDate().toString()+" server time (in: "+timeDistance+" minutes)";
				} else {
					infoMaintenaceStatus = "";
				}
				infoMaintenaceActive = ""+maintenanceConfigurationEntity.isActive();
				infoMaintenaceStartDate = maintenanceConfigurationEntity.getConfigurationHolder().getActivationDate().toString();
				infoMaintenaceEndDate = maintenanceConfigurationEntity.getConfigurationHolder().getDeactivationDate().toString();
				infoMaintenaceDuration = maintenanceConfigurationEntity.getConfigurationHolder().getMaintenanceDuration()+" minute(s)";
				infoMaintenaceAdvancedFunctionalityStartDate = maintenanceConfigurationEntity.getConfigurationHolder().getPostActivationDate().toString();
				infoMaintenaceSetupAdjustmentDate = maintenanceConfigurationEntity.getTimestamp().toString();
			}
			RequestContext.getCurrentInstance().update("tabView:tabSystemSettings:idInfoMaintenanceSetup");
		} catch(Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.ERROR, "Error: "+exc.toString());
		    FacesMessage msg = new FacesMessage("Error", "Unable to read system maintenance status: "+exc.toString());  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    RequestContext.getCurrentInstance().update("tabView:idSystemSettingsGrowl");
		}
	}

	public String getRbCpeCreationMode() {
		return rbCpeCreationMode;
	}

	public void setRbCpeCreationMode(String rbCpeCreationMode) {
		this.rbCpeCreationMode = rbCpeCreationMode;
	}

	public String getRbLoggingMode() {
		return rbLoggingMode;
	}

	public void setRbLoggingMode(String rbLoggingMode) {
		this.rbLoggingMode = rbLoggingMode;
	}

	
	public String getStorageServerIp() {
		return storageServerIp;
	}

	public void setStorageServerIp(String storageServerIp) {
		this.storageServerIp = storageServerIp;
	}

	public String getRbAutoconfMode() {
		return rbAutoconfMode;
	}

	public void setRbAutoconfMode(String rbAutoconfMode) {
		this.rbAutoconfMode = rbAutoconfMode;
	}

	public int getDisplayedCpeLogEvents() {
		return displayedCpeLogEvents;
	}

	public void setDisplayedCpeLogEvents(int displayedCpeLogEvents) {
		this.displayedCpeLogEvents = displayedCpeLogEvents;
	}

	public int getDisplayedAlertEvents() {
		return displayedAlertEvents;
	}

	public void setDisplayedAlertEvents(int displayedAlertEvents) {
		this.displayedAlertEvents = displayedAlertEvents;
	}

	public String getStunPort() {
		return stunPort;
	}

	public void setStunPort(String stunPort) {
		this.stunPort = stunPort;
	}

	public String getRbMonitoring() {
		return rbMonitoring;
	}

	public void setRbMonitoring(String rbMonitoring) {
		this.rbMonitoring = rbMonitoring;
	}

	public int getMonitoringIntervals() {
		return monitoringIntervals;
	}

	public void setMonitoringIntervals(int monitoringIntervals) {
		this.monitoringIntervals = monitoringIntervals;
	}

	public String getRbReporting() {
		return rbReporting;
	}

	public void setRbReporting(String rbReporting) {
		this.rbReporting = rbReporting;
	}

	public int getReportingIntervals() {
		return reportingIntervals;
	}

	public void setReportingIntervals(int reportingIntervals) {
		this.reportingIntervals = reportingIntervals;
	}

	public String getRbForecasting() {
		return rbForecasting;
	}

	public void setRbForecasting(String rbForecasting) {
		this.rbForecasting = rbForecasting;
	}

	public String getForecastingServerIp() {
		return forecastingServerIp;
	}

	public void setForecastingServerIp(String forecastingServerIp) {
		this.forecastingServerIp = forecastingServerIp;
	}

	public String getImdgIp() {
		return imdgIp;
	}

	public void setImdgIp(String imdgIp) {
		this.imdgIp = imdgIp;
	}

	public int getForecastingIntervals() {
		return forecastingIntervals;
	}

	public void setForecastingIntervals(int forecastingIntervals) {
		this.forecastingIntervals = forecastingIntervals;
	}

	public String getRbServerStatusCollection() {
		return rbServerStatusCollection;
	}

	public void setRbServerStatusCollection(String rbServerStatusCollection) {
		this.rbServerStatusCollection = rbServerStatusCollection;
	}

	public int getServerStatusCollectionIntervals() {
		return serverStatusCollectionIntervals;
	}

	public void setServerStatusCollectionIntervals(
			int serverStatusCollectionIntervals) {
		this.serverStatusCollectionIntervals = serverStatusCollectionIntervals;
	}

	public String getRbMaintenance() {
		return rbMaintenance;
	}

	public void setRbMaintenance(String rbMaintenance) {
		this.rbMaintenance = rbMaintenance;
	}

	public int getMaintenanceDuration() {
		return maintenanceDuration;
	}

	public void setMaintenanceDuration(int maintenanceDuration) {
		this.maintenanceDuration = maintenanceDuration;
	}

	public String getInfoMaintenaceActive() {
		return infoMaintenaceActive;
	}

	public void setInfoMaintenaceActive(String infoMaintenaceActive) {
		this.infoMaintenaceActive = infoMaintenaceActive;
	}

	public String getInfoMaintenaceStartDate() {
		return infoMaintenaceStartDate;
	}

	public void setInfoMaintenaceStartDate(String infoMaintenaceStartDate) {
		this.infoMaintenaceStartDate = infoMaintenaceStartDate;
	}

	public String getInfoMaintenaceEndDate() {
		return infoMaintenaceEndDate;
	}

	public void setInfoMaintenaceEndDate(String infoMaintenaceEndDate) {
		this.infoMaintenaceEndDate = infoMaintenaceEndDate;
	}

	public String getInfoMaintenaceAdvancedFunctionalityStartDate() {
		return infoMaintenaceAdvancedFunctionalityStartDate;
	}

	public void setInfoMaintenaceAdvancedFunctionalityStartDate(
			String infoMaintenaceAdvancedFunctionalityStartDate) {
		this.infoMaintenaceAdvancedFunctionalityStartDate = infoMaintenaceAdvancedFunctionalityStartDate;
	}

	public String getInfoMaintenaceSetupAdjustmentDate() {
		return infoMaintenaceSetupAdjustmentDate;
	}

	public void setInfoMaintenaceSetupAdjustmentDate(
			String infoMaintenaceSetupAdjustmentDate) {
		this.infoMaintenaceSetupAdjustmentDate = infoMaintenaceSetupAdjustmentDate;
	}

	public String getInfoMaintenaceDuration() {
		return infoMaintenaceDuration;
	}

	public void setInfoMaintenaceDuration(String infoMaintenaceDuration) {
		this.infoMaintenaceDuration = infoMaintenaceDuration;
	}

	public String getInfoMaintenaceStatus() {
		return infoMaintenaceStatus;
	}

	public void setInfoMaintenaceStatus(String infoMaintenaceStatus) {
		this.infoMaintenaceStatus = infoMaintenaceStatus;
	}

	public String getRbOfferWallGeneration() {
		return rbOfferWallGeneration;
	}

	public void setRbOfferWallGeneration(String rbOfferWallGeneration) {
		this.rbOfferWallGeneration = rbOfferWallGeneration;
	}

	public int getOfferWallGenerationIntervals() {
		return offerWallGenerationIntervals;
	}

	public void setOfferWallGenerationIntervals(int offerWallGenerationIntervals) {
		this.offerWallGenerationIntervals = offerWallGenerationIntervals;
	}

	public String getRbSQLStorageSizeMonitor() {
		return rbSQLStorageSizeMonitor;
	}

	public void setRbSQLStorageSizeMonitor(String rbSQLStorageSizeMonitor) {
		this.rbSQLStorageSizeMonitor = rbSQLStorageSizeMonitor;
	}

	public int getSqlStorageSizeMonitorIntervals() {
		return sqlStorageSizeMonitorIntervals;
	}

	public void setSqlStorageSizeMonitorIntervals(int sqlStorageSizeMonitorIntervals) {
		this.sqlStorageSizeMonitorIntervals = sqlStorageSizeMonitorIntervals;
	}

	public int getSqlStorageHistoryLength() {
		return sqlStorageHistoryLength;
	}

	public void setSqlStorageHistoryLength(int sqlStorageHistoryLength) {
		this.sqlStorageHistoryLength = sqlStorageHistoryLength;
	}

	public String getMasterServerIp() {
		return masterServerIp;
	}

	public void setMasterServerIp(String masterServerIp) {
		this.masterServerIp = masterServerIp;
	}

	public String getRbServerMonitoring() {
		return rbServerMonitoring;
	}

	public void setRbServerMonitoring(String rbServerMonitoring) {
		this.rbServerMonitoring = rbServerMonitoring;
	}

	public String getRbCRFEnabled() {
		return rbCRFEnabled;
	}

	public void setRbCRFEnabled(String rbCRFEnabled) {
		this.rbCRFEnabled = rbCRFEnabled;
	}

	public int getCrfTriggerIntervals() {
		return crfTriggerIntervals;
	}

	public void setCrfTriggerIntervals(int crfTriggerIntervals) {
		this.crfTriggerIntervals = crfTriggerIntervals;
	}

	public String getStorageServerBackupIp() {
		return storageServerBackupIp;
	}

	public void setStorageServerBackupIp(String storageServerBackupIp) {
		this.storageServerBackupIp = storageServerBackupIp;
	}
	
}

