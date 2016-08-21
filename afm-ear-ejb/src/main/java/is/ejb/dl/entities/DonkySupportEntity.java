package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "DonkySupport")
public class DonkySupportEntity {
	
	@Id
	@GeneratedValue
	private Integer id;
	private String userId;
	private Timestamp creationTime;
	private String externalUserId;
	@Column(unique = true)
	private String conversationId;
	private String ticketId;
	private String rewardType;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public String getExternalUserId() {
		return externalUserId;
	}

	public void setExternalUserId(String externalUserId) {
		this.externalUserId = externalUserId;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRewardType() {
		return rewardType;
	}

	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}

	@Override
	public String toString() {
		return "DonkySupportEntity [id=" + id + ", userId=" + userId + ", creationTime=" + creationTime
				+ ", externalUserId=" + externalUserId + ", conversationId=" + conversationId + ", ticketId=" + ticketId
				+ ", rewardType=" + rewardType + "]";
	}



}
