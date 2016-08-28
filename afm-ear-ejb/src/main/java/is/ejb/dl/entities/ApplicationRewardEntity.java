package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "ApplicationReward")
public class ApplicationRewardEntity {

	@Id
	@GeneratedValue
	private int id;
	private String rewardName;
	private double rewardValue;
	private int applicationId;
	private String applicationName;
	private int realmId;
	private String rewardCategory;
	private String rewardType;
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRewardName() {
		return rewardName;
	}

	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
	}

	public double getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	

	public String getRewardCategory() {
		return rewardCategory;
	}

	public void setRewardCategory(String rewardCategory) {
		this.rewardCategory = rewardCategory;
	}

	public String getRewardType() {
		return rewardType;
	}

	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}

	@Override
	public String toString() {
		return "ApplicationRewardEntity [id=" + id + ", rewardName=" + rewardName + ", rewardValue=" + rewardValue
				+ ", applicationId=" + applicationId + ", applicationName=" + applicationName + ", realmId=" + realmId
				+ ", rewardCategory=" + rewardCategory + ", rewardType=" + rewardType + "]";
	}

	
	
	
}
