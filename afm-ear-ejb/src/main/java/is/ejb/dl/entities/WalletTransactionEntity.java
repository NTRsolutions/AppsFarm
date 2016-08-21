package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "WalletTransaction")
public class WalletTransactionEntity {

   @Id
   @GeneratedValue
   private int id;
   private int userId;
   private long ticketId;
   private String type;
   private String status;
   private String rewardName;
   private double payoutValue;
   private String payoutCurrencyCode;
   private String payoutDescription;
   private Timestamp timestamp;
   private String internalTransactionId; //if this wallet transaction is associated with UserEvent with internal transaction id - we store it here
   private String applicationName;
   
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public double getPayoutValue() {
		return payoutValue;
	}
	public void setPayoutValue(double payoutValue) {
		this.payoutValue = payoutValue;
	}
	public String getPayoutDescription() {
		return payoutDescription;
	}
	public void setPayoutDescription(String payoutDescription) {
		this.payoutDescription = payoutDescription;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "WalletTransactionEntity [id=" + id + ", userId=" + userId
				+ ", status=" + status + ", payoutValue=" + payoutValue
				+ ", payoutDescription=" + payoutDescription + ", payoutTime="
				+ timestamp + "]";
	}
	public String getPayoutCurrencyCode() {
		return payoutCurrencyCode;
	}
	public void setPayoutCurrencyCode(String payoutCurrencyCode) {
		this.payoutCurrencyCode = payoutCurrencyCode;
	}
	public String getInternalTransactionId() {
		return internalTransactionId;
	}
	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRewardName() {
		return rewardName;
	}
	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
	}
	public long getTicketId() {
		return ticketId;
	}
	public void setTicketId(long ticketId) {
		this.ticketId = ticketId;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	
}
