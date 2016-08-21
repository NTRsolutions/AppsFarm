package is.ejb.dl.entities;

import is.ejb.bl.cloudtraxConfiguration.AccessPoints;
import is.ejb.bl.cloudtraxConfiguration.AllowedDomains;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "CloudtraxConfiguration")
public class CloudtraxConfigurationEntity {

	@Id
	@GeneratedValue
	private int id;
	private String login;
	private String password;
	private String networkName;
	private String radiusServer1;
	private String radiusServer2;
	private String radiusSecret;
	private String captivePortalServer;
	private String captivePortalURL;
	private String captivePortalSecret;
	private int radiusServer1Id;
	private int radiusServer2Id;
	
	
	
	@Lob
	@Column
	private String allowedDomains;

	@Lob
	@Column
	private String accessPoints;
	
	@Transient
	private List<String> allowedDomainsList;
	
	@Transient
	private List<String> accessPointsList;
	
	@Transient
	private RadiusConfigurationEntity radiusServer1Config;
	
	@Transient
	private RadiusConfigurationEntity radiusServer2Config;

	@Transient
	private List <AccessPointEntity> accessPointEntityList;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getRadiusServer1() {
		return radiusServer1;
	}

	public void setRadiusServer1(String radiusServer1) {
		this.radiusServer1 = radiusServer1;
	}

	public String getRadiusServer2() {
		return radiusServer2;
	}

	public void setRadiusServer2(String radiusServer2) {
		this.radiusServer2 = radiusServer2;
	}

	public String getRadiusSecret() {
		return radiusSecret;
	}

	public void setRadiusSecret(String radiusSecret) {
		this.radiusSecret = radiusSecret;
	}

	public String getCaptivePortalServer() {
		return captivePortalServer;
	}

	public void setCaptivePortalServer(String captivePortalServer) {
		this.captivePortalServer = captivePortalServer;
	}

	public String getCaptivePortalURL() {
		return captivePortalURL;
	}

	public void setCaptivePortalURL(String captivePortalURL) {
		this.captivePortalURL = captivePortalURL;
	}

	public String getCaptivePortalSecret() {
		return captivePortalSecret;
	}

	public void setCaptivePortalSecret(String captivePortalSecret) {
		this.captivePortalSecret = captivePortalSecret;
	}

	public String getAllowedDomains() {
		return allowedDomains;
	}

	public void setAllowedDomains(String allowedDomains) {
		this.allowedDomains = allowedDomains;
	}

	public String getAccessPoints() {
		return accessPoints;
	}

	public void setAccessPoints(String acessPoints) {
		this.accessPoints = acessPoints;
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


	public int getRadiusServer1Id() {
		return radiusServer1Id;
	}

	public void setRadiusServer1Id(int radiusServer1Id) {
		this.radiusServer1Id = radiusServer1Id;
	}

	public int getRadiusServer2Id() {
		return radiusServer2Id;
	}

	public void setRadiusServer2Id(int radiusServer2Id) {
		this.radiusServer2Id = radiusServer2Id;
	}

	public List<String> getAllowedDomainsList() {
		return allowedDomainsList;
	}

	public void setAllowedDomainsList(List<String> allowedDomainsList) {
		this.allowedDomainsList = allowedDomainsList;
	}

	public List<String> getAccessPointsList() {
		return accessPointsList;
	}

	public void setAccessPointsList(List<String> accessPointsList) {
		this.accessPointsList = accessPointsList;
	}

	public RadiusConfigurationEntity getRadiusServer1Config() {
		return radiusServer1Config;
	}

	public void setRadiusServer1Config(RadiusConfigurationEntity radiusServer1Config) {
		this.radiusServer1Config = radiusServer1Config;
	}

	public RadiusConfigurationEntity getRadiusServer2Config() {
		return radiusServer2Config;
	}

	public void setRadiusServer2Config(RadiusConfigurationEntity radiusServer2Config) {
		this.radiusServer2Config = radiusServer2Config;
	}

	public List<AccessPointEntity> getAccessPointEntityList() {
		return accessPointEntityList;
	}

	public void setAccessPointEntityList(
			List<AccessPointEntity> accessPointEntityList) {
		this.accessPointEntityList = accessPointEntityList;
	}

	@Override
	public String toString() {
		return "CloudtraxConfigurationEntity [id=" + id + ", login=" + login
				+ ", password=" + password + ", networkName=" + networkName
				+ ", radiusServer1=" + radiusServer1 + ", radiusServer2="
				+ radiusServer2 + ", radiusSecret=" + radiusSecret
				+ ", captivePortalServer=" + captivePortalServer
				+ ", captivePortalURL=" + captivePortalURL
				+ ", captivePortalSecret=" + captivePortalSecret
				+ ", radiusServer1Id=" + radiusServer1Id + ", radiusServer2Id="
				+ radiusServer2Id + ", allowedDomains=" + allowedDomains
				+ ", accessPoints=" + accessPoints + ", allowedDomainsList="
				+ allowedDomainsList + ", accessPointsList=" + accessPointsList
				+ ", radiusServer1Config=" + radiusServer1Config
				+ ", radiusServer2Config=" + radiusServer2Config
				+ ", accessPointEntityList=" + accessPointEntityList + "]";
	}

	
	
	
	

	
}
