package is.ejb.dl.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;


@Entity
@XmlRootElement
@Table(name = "DeviceProfile")
public class DeviceProfileEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
   @Id
   @GeneratedValue
   private Integer id;

   private String name;
   private int realmId; 
   private Integer informinterval;
   private Integer dayskeepstats;
   private Boolean savestats;
   private Boolean saveLog;
   private Boolean saveParamValues;
   private Integer saveParamValuesInterval;
   private Boolean saveParamValuesOnChange;
   private Boolean saveParamValuesOnBoot;
   private Boolean enableMonitoring;
   private String scriptname;
   
   @OneToMany//(fetch = FetchType.EAGER)
   @LazyCollection(LazyCollectionOption.FALSE)
   private Collection<ProfilePropertyEntity> profileProperties;

   @ManyToOne//(fetch = FetchType.LAZY)
   @LazyCollection(LazyCollectionOption.FALSE)
   private DeviceAlertsConfigurationEntity monitoringProfile;

   private String baseprofile;
   
   public DeviceProfileEntity() {}

	public DeviceProfileEntity(java.lang.String name)
	{
        setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getInforminterval() {
		return informinterval;
	}

	public void setInforminterval(Integer informinterval) {
		this.informinterval = informinterval;
	}

	public Integer getDayskeepstats() {
		return dayskeepstats;
	}

	public void setDayskeepstats(Integer dayskeepstats) {
		this.dayskeepstats = dayskeepstats;
	}

	public Boolean getSavestats() {
		return savestats;
	}

	public void setSavestats(Boolean savestats) {
		this.savestats = savestats;
	}

	public Boolean getSaveLog() {
		return saveLog;
	}

	public void setSaveLog(Boolean saveLog) {
		this.saveLog = saveLog;
	}

	public Boolean getSaveParamValues() {
		return saveParamValues;
	}

	public void setSaveParamValues(Boolean saveParamValues) {
		this.saveParamValues = saveParamValues;
	}

	public Integer getSaveParamValuesInterval() {
		return saveParamValuesInterval;
	}

	public void setSaveParamValuesInterval(Integer saveParamValuesInterval) {
		this.saveParamValuesInterval = saveParamValuesInterval;
	}

	public Boolean getSaveParamValuesOnChange() {
		return saveParamValuesOnChange;
	}

	public void setSaveParamValuesOnChange(Boolean saveParamValuesOnChange) {
		this.saveParamValuesOnChange = saveParamValuesOnChange;
	}

	public Boolean getSaveParamValuesOnBoot() {
		return saveParamValuesOnBoot;
	}

	public void setSaveParamValuesOnBoot(Boolean saveParamValuesOnBoot) {
		this.saveParamValuesOnBoot = saveParamValuesOnBoot;
	}

	public String getScriptname() {
		return scriptname;
	}

	public void setScriptname(String scriptname) {
		this.scriptname = scriptname;
	}

	public String getBaseprofile() {
		return baseprofile;
	}

	public void setBaseprofile(String baseprofile) {
		this.baseprofile = baseprofile;
	}

	public Collection<ProfilePropertyEntity> getProfileProperties() {
		return profileProperties;
	}

	public void setProfileProperties(Collection<ProfilePropertyEntity> profileProperties) {
		this.profileProperties = profileProperties;
	}

	public DeviceAlertsConfigurationEntity getMonitoringProfile() {
		return monitoringProfile;
	}

	public void setMonitoringProfile(
			DeviceAlertsConfigurationEntity monitoringProfile) {
		this.monitoringProfile = monitoringProfile;
	}

	public Boolean getEnableMonitoring() {
		return enableMonitoring;
	}

	public void setEnableMonitoring(Boolean enableMonitoring) {
		this.enableMonitoring = enableMonitoring;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
}
