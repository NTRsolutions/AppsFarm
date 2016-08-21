package is.ejb.bl.system;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOMaintenanceConfiguration;
import is.ejb.dl.entities.MaintenanceConfigurationEntity;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

public class MaintenanceManager {

	@Inject
	private DAOMaintenanceConfiguration daoMaintenanceConfiguration;

	@Inject
	private MaintenanceConfigurationSerDe maintenanceSerDe;

	
	public void updateMaintenanceStatus(String rbMaintenance, 
										int maintenanceDuration,
										String rbMonitoring,
										String rbAutoConfiguration,
										String rbForecasting,
										String rbReporting) throws Exception {
		//check if we have maintenance configuration entry created
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Checking system maintenance options...");        		

		MaintenanceConfigurationEntity maintenanceConfigurationEntity = null;
		
		List<MaintenanceConfigurationEntity> listMaintenanceConfiguration = daoMaintenanceConfiguration.findAll();
		if(listMaintenanceConfiguration.size() == 0){ 
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "No maintenance configuration identified...");        		
		} else { //there is configuration created
			maintenanceConfigurationEntity = listMaintenanceConfiguration.get(0);
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Maintenance configuration identified, timestamp: "+maintenanceConfigurationEntity.getTimestamp().toString()+" active: "+maintenanceConfigurationEntity.isActive());        		
		}

        if(!Application.isMaintenanceEnabled() && rbMaintenance.equals("enabled")) { //activate maintenance
			if(maintenanceConfigurationEntity == null) { //create new config entry in db
				maintenanceConfigurationEntity = new MaintenanceConfigurationEntity();
			}
        	maintenanceConfigurationEntity.setActive(true);
        	maintenanceConfigurationEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
        	MaintenanceConfigurationHolder mch = new MaintenanceConfigurationHolder();
        	mch.setActivationDate(new Timestamp(System.currentTimeMillis()));
        	mch.setDeactivationDate(new Timestamp(System.currentTimeMillis()+maintenanceDuration * 60 *1000));
        	mch.setMaintenanceDuration(maintenanceDuration);
        	
        	//store existing configuration
        	if(rbAutoConfiguration.equals("enabled")) {
        		mch.setAutoconfigurationEnabled(true);
        	} else {
        		mch.setAutoconfigurationEnabled(false);
        	}
        	
        	if(rbMonitoring.equals("enabled")) {
        		mch.setMonitoringEnabled(true);
        	}
        	 else {
         		mch.setMonitoringEnabled(false);
         	}
        	
        	if(rbForecasting.equals("enabled")) {
        		mch.setForecastingEnabled(true);
        	} else {
        		mch.setForecastingEnabled(false);
        	}

        	if(rbReporting.equals("enabled")) {
        		mch.setReportingEnabled(true);
        	} else {
        		mch.setReportingEnabled(false);
        	}
        	
        	//disable advanced functionality for the time of maintenance
       		Application.setAcEnabled(false);
    		Application.setMonitoringEnabled(false);
    		Application.setFcEnabled(false);
    		Application.setReportingEnabled(false);
        	
        	//set post activation to be deactivationDate+2*monitoring interval
        	long postActivationTime = mch.getDeactivationDate().getTime()+Application.getMonitoringIntervals()*60*1000 * 2;
        	mch.setPostActivationDate(new Timestamp(postActivationTime));
        	String mchString = maintenanceSerDe.serialize(mch);
        	maintenanceConfigurationEntity.setContent(mchString);
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Activating system maintenance");
        	maintenanceConfigurationEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
			daoMaintenanceConfiguration.createOrUpdate(maintenanceConfigurationEntity);
        } else if(Application.isMaintenanceEnabled() && rbMaintenance.equals("disabled")) { //deactivate maintenance
			if(maintenanceConfigurationEntity == null) { //create new config entry in db
				maintenanceConfigurationEntity = new MaintenanceConfigurationEntity();
			}
        	maintenanceConfigurationEntity.setActive(false);
        	maintenanceConfigurationEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
        	String configContent = maintenanceConfigurationEntity.getContent();
    		//deserialise the saved configuration content
    		MaintenanceConfigurationHolder mch = maintenanceSerDe.deserialize(maintenanceConfigurationEntity.getContent()); 
    		mch.setDeactivationDate(new Timestamp(System.currentTimeMillis()));
    		
    		//disable maintenance flag
    		Application.setMaintenanceEnabled(false);
    		
			//bring back the pre-maintenance global system configuration
			//autoconf
        	if(mch.isAutoconfigurationEnabled()) {
        		Application.setAcEnabled(true);
        	} else {
        		Application.setAcEnabled(false);
        	}
        	
        	//monitoring
        	if(mch.isMonitoringEnabled()) {
        		Application.setMonitoringEnabled(true);
        	} else {
        		Application.setMonitoringEnabled(false);
        	}

        	//forecasting
        	if(mch.isForecastingEnabled()) {
        		Application.setFcEnabled(true);
        	} else {
        		Application.setFcEnabled(false);
        	}

        	//reporting
        	if(mch.isReportingEnabled()) {
        		Application.setReportingEnabled(true);
        	} else {
        		Application.setReportingEnabled(false);
        	}

        	//set post activation to be deactivationDate+2*monitoring interval
        	long postActivationTime = mch.getDeactivationDate().getTime()+Application.getMonitoringIntervals()*60*1000 * 2;
        	mch.setPostActivationDate(new Timestamp(postActivationTime));
        	String mchString = maintenanceSerDe.serialize(mch);
        	maintenanceConfigurationEntity.setContent(mchString);
        	//apply the saved configuration to the system (inform intervals from device schemas) 
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Deactivating system maintenance");
			maintenanceConfigurationEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
			daoMaintenanceConfiguration.createOrUpdate(maintenanceConfigurationEntity);
        } else { //no config was altered 
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Not altering maintenance configuration - nothing to change");        		
        }
	}
	
	public MaintenanceConfigurationEntity getMaintenanceConfiguration() throws Exception {
		//check if we have maintenance configuration entry created
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Retrieving maintenance configuration");        		

		MaintenanceConfigurationEntity maintenanceConfigurationEntity = null;
		List<MaintenanceConfigurationEntity> listMaintenanceConfiguration = daoMaintenanceConfiguration.findAll();
		if(listMaintenanceConfiguration.size() == 0){ 
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "No maintenance configuration identified");
		} else { //there is configuration created
			maintenanceConfigurationEntity = listMaintenanceConfiguration.get(0);
			Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Maintenance configuration identified, creation date: "+maintenanceConfigurationEntity.getTimestamp().toString()+" active: "+maintenanceConfigurationEntity.isActive());
			MaintenanceConfigurationHolder mch = maintenanceSerDe.deserialize(maintenanceConfigurationEntity.getContent());
			maintenanceConfigurationEntity.setConfigurationHolder(mch);
		}
		
		return maintenanceConfigurationEntity;
	}
	
	public void updateMaintenanceConfiguration(MaintenanceConfigurationEntity mce) throws Exception {
		//check if we have maintenance configuration entry created
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_MAINTENANCE_ACTIVITY_LOG, -1, LogStatus.OK, "Updating maintenance configuration");        		
		daoMaintenanceConfiguration.createOrUpdate(mce);
	}

}
