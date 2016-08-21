package is.ejb.bl.system;

import java.io.Serializable;
import java.sql.Timestamp;

public class MaintenanceConfigurationHolder implements Serializable {
	private Timestamp activationDate; //date when maintanance was enabled
	private Timestamp deactivationDate; //date when maintenance was disabled (either through time expiry or via manual deactivation)
	private Timestamp postActivationDate; //date when advanced server functionality is brought back to life after maintenance is deactivated
	
	private boolean autoconfigurationEnabled;
	private boolean reportingEnabled;
	private boolean monitoringEnabled;
	private boolean forecastingEnabled;
	
	private int maintenanceDuration = 0; //in minutes

	public Timestamp getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Timestamp activationDate) {
		this.activationDate = activationDate;
	}

	public Timestamp getDeactivationDate() {
		return deactivationDate;
	}

	public void setDeactivationDate(Timestamp deactivationDate) {
		this.deactivationDate = deactivationDate;
	}

	public Timestamp getPostActivationDate() {
		return postActivationDate;
	}

	public void setPostActivationDate(Timestamp postActivationDate) {
		this.postActivationDate = postActivationDate;
	}

	public int getMaintenanceDuration() {
		return maintenanceDuration;
	}

	public void setMaintenanceDuration(int maintenanceDuration) {
		this.maintenanceDuration = maintenanceDuration;
	}

	public boolean isAutoconfigurationEnabled() {
		return autoconfigurationEnabled;
	}

	public void setAutoconfigurationEnabled(boolean autoconfigurationEnabled) {
		this.autoconfigurationEnabled = autoconfigurationEnabled;
	}

	public boolean isMonitoringEnabled() {
		return monitoringEnabled;
	}

	public void setMonitoringEnabled(boolean monitoringEnabled) {
		this.monitoringEnabled = monitoringEnabled;
	}

	public boolean isForecastingEnabled() {
		return forecastingEnabled;
	}

	public void setForecastingEnabled(boolean forecastingEnabled) {
		this.forecastingEnabled = forecastingEnabled;
	}

	public boolean isReportingEnabled() {
		return reportingEnabled;
	}

	public void setReportingEnabled(boolean reportingEnabled) {
		this.reportingEnabled = reportingEnabled;
	}
	
}
