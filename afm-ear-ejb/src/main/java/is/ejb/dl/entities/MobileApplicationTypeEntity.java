package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "MobileApplicationType")
public class MobileApplicationTypeEntity {
	@Id
	@GeneratedValue
	private Integer id;
	private String name;
	private Timestamp creationTime;
	private int realmId;
	private boolean versionCheck;
	private String minimumVersion;
	private String versionErrorMessage;
	private String gcmKey;

	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Timestamp getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}
	public int getRealmId() {
		return realmId;
	}
	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}
	public boolean isVersionCheck() {
		return versionCheck;
	}
	public void setVersionCheck(boolean versionCheck) {
		this.versionCheck = versionCheck;
	}
	public String getMinimumVersion() {
		return minimumVersion;
	}
	public void setMinimumVersion(String minimumVersion) {
		this.minimumVersion = minimumVersion;
	}
	public String getVersionErrorMessage() {
		return versionErrorMessage;
	}
	public void setVersionErrorMessage(String versionErrorMessage) {
		this.versionErrorMessage = versionErrorMessage;
	}
	public String getGcmKey() {
		return gcmKey;
	}
	public void setGcmKey(String gcmKey) {
		this.gcmKey = gcmKey;
	}
	
}
