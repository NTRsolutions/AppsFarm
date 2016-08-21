package is.ejb.dl.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "SpinnerRewardEntity")
public class SpinnerRewardEntity {

	@Id
	@GeneratedValue
	private int id;
	@NotNull
	private int rewardTypeId;
	private Date updateTime;
	private BigDecimal rewardProbability;
	private String rewardName;
	private String rewardType;
	private BigDecimal rewardValue;
	private int ratioX;
	private int ratioY;
	private int monthLimit;
	private int monthLimitCount;
	private Timestamp monthLimitLastRewardTimestamp;
	
	@Transient
	private double generated;
	
	private String notificationMessage;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRewardTypeId() {
		return rewardTypeId;
	}
	public void setRewardTypeId(int rewardTypeId) {
		this.rewardTypeId = rewardTypeId;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public BigDecimal getRewardProbability() {
		return rewardProbability;
	}
	public void setRewardProbability(BigDecimal rewardProbability) {
		this.rewardProbability = rewardProbability;
	}
	public String getRewardName() {
		return rewardName;
	}
	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public BigDecimal getRewardValue() {
		return rewardValue;
	}
	public void setRewardValue(BigDecimal rewardValue) {
		this.rewardValue = rewardValue;
	}
	
	public double getGenerated() {
		return generated;
	}
	public void setGenerated(double generated) {
		this.generated = generated;
	}
	public SpinnerRewardEntity(){
		
	}
	
	public int getRatioX() {
		return ratioX;
	}
	public void setRatioX(int ratioX) {
		this.ratioX = ratioX;
	}
	public int getRatioY() {
		return ratioY;
	}
	public void setRatioY(int ratioY) {
		this.ratioY = ratioY;
	}
	
	public String getNotificationMessage() {
		return notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
	
	public int getMonthLimit() {
		return monthLimit;
	}
	public void setMonthLimit(int monthLimit) {
		this.monthLimit = monthLimit;
	}
	public int getMonthLimitCount() {
		return monthLimitCount;
	}
	public void setMonthLimitCount(int monthLimitCount) {
		this.monthLimitCount = monthLimitCount;
	}
	
	
	public Timestamp getMonthLimitLastRewardTimestamp() {
		return monthLimitLastRewardTimestamp;
	}
	public void setMonthLimitLastRewardTimestamp(Timestamp monthLimitLastRewardTimestamp) {
		this.monthLimitLastRewardTimestamp = monthLimitLastRewardTimestamp;
	}
	public SpinnerRewardEntity(int id, int rewardTypeId, Date updateTime, BigDecimal rewardProbability,
			String rewardName, String rewardType, BigDecimal rewardValue,String notificationMessage) {
		super();
		this.id = id;
		this.rewardTypeId = rewardTypeId;
		this.updateTime = updateTime;
		this.rewardProbability = rewardProbability;
		this.rewardName = rewardName;
		this.rewardType = rewardType;
		this.rewardValue = rewardValue;
		this.notificationMessage = notificationMessage;
	}
	


	@Override
	public String toString() {
		return "SpinnerRewardEntity [id=" + id + ", rewardTypeId=" + rewardTypeId + ", updateTime=" + updateTime
				+ ", rewardProbability=" + rewardProbability + ", rewardName=" + rewardName + ", rewardType="
				+ rewardType + ", rewardValue=" + rewardValue + ", ratioX=" + ratioX + ", ratioY=" + ratioY
				+ ", monthLimit=" + monthLimit + ", monthLimitCount=" + monthLimitCount
				+ ", monthLimitLastRewardTimestamp=" + monthLimitLastRewardTimestamp + ", generated=" + generated
				+ ", notificationMessage=" + notificationMessage + "]";
	}
	@Override
	public SpinnerRewardEntity clone() {
		SpinnerRewardEntity cloneEntity = new SpinnerRewardEntity();
		cloneEntity.setId(id);
		cloneEntity.setRewardName(rewardName);
		cloneEntity.setRewardProbability(rewardProbability);
		cloneEntity.setRewardType(rewardType);
		cloneEntity.setRewardTypeId(rewardTypeId);
		cloneEntity.setRewardValue(rewardValue);
		cloneEntity.setUpdateTime(updateTime);
		cloneEntity.setRatioX(ratioX);
		cloneEntity.setRatioY(ratioY);
		cloneEntity.setNotificationMessage(notificationMessage);
		cloneEntity.setMonthLimit(monthLimit);
		cloneEntity.setMonthLimitCount(monthLimitCount);
		cloneEntity.setMonthLimitLastRewardTimestamp(monthLimitLastRewardTimestamp);
		return cloneEntity;
	}



	
	
}
