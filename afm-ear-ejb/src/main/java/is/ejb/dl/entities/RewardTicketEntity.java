package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.bl.system.security.KeyGenerator;

@Entity
@XmlRootElement
@Table(name = "RewardTicket")
public class RewardTicketEntity {

	@Id
	@GeneratedValue
	private int id;

	private int userId;
	private String email;
	private String rewardName;
	private double creditPoints;
	private Timestamp requestDate;
	private Timestamp processingDate;
	private Timestamp closeDate;

	@Enumerated(EnumType.STRING)
	private RewardTicketStatus status;

	private String comment;
	private String ticketOwner;
	private String hash;
	
	
	/***
	 * Generate and set hash
	 * 
	 * @return the hash
	 */
	public String generateHash() {
		return hash = KeyGenerator.genetareSha1Hex(userId + email + rewardName + requestDate.toString()) + "r6t6";
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRewardName() {
		return rewardName;
	}

	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
	}

	public double getCreditPoints() {
		return creditPoints;
	}

	public void setCreditPoints(double creditPoints) {
		this.creditPoints = creditPoints;
	}

	public Timestamp getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Timestamp requestDate) {
		this.requestDate = requestDate;
	}

	public Timestamp getProcessingDate() {
		return processingDate;
	}

	public void setProcessingDate(Timestamp processingDate) {
		this.processingDate = processingDate;
	}

	public Timestamp getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Timestamp closeDate) {
		this.closeDate = closeDate;
	}

	public RewardTicketStatus getStatus() {
		return status;
	}

	public void setStatus(RewardTicketStatus status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTicketOwner() {
		return ticketOwner;
	}

	public void setTicketOwner(String ticketOwner) {
		this.ticketOwner = ticketOwner;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getContent() {
		return "[userId=" + userId + ", email=" + email + ", rewardName=" + rewardName
				+ ", creditPoints=" + creditPoints + ", requestDate=" + requestDate + ", processingDate="
				+ processingDate + ", closeDate=" + closeDate + ", status=" + status + ", comment=" + comment
				+ ", ticketOwner=" + ticketOwner + ", hash=" + hash + "]";
	}

	@Override
	public String toString() {
		return "RewardTicketEntity [id=" + id + ", userId=" + userId + ", email=" + email + ", rewardName=" + rewardName
				+ ", creditPoints=" + creditPoints + ", requestDate=" + requestDate + ", processingDate="
				+ processingDate + ", closeDate=" + closeDate + ", status=" + status + ", comment=" + comment
				+ ", ticketOwner=" + ticketOwner + ", hash=" + hash + "]";
	}

}
