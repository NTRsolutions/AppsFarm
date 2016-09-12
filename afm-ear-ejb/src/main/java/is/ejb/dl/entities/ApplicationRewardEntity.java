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
	private int realmId;
	private String rewardType;
	private String logo;
	private String discount;
	private boolean isSendByPost;
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
	
	public int getRealmId() {
		return realmId;
	}
	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
	public boolean isSendByPost() {
		return isSendByPost;
	}
	public void setSendByPost(boolean isSendByPost) {
		this.isSendByPost = isSendByPost;
	}
	
	
	
	
	
}
