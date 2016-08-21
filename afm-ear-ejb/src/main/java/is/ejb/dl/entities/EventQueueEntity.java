package is.ejb.dl.entities;

import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
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
@Table(name = "EventQueueEntity") 
public class EventQueueEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;
   
   @NotNull
   private int userId; 

   @NotNull
   @Column(unique=true)
   private int eventId; 

   @NotNull
   private String phoneNumberExtension; 

   @NotNull
   private String phoneNumber; 

   private Timestamp generationDate = null; //when added to queue
   private Timestamp rewardingSystemIssueDate = null; //when send to mode
   private boolean pushedToRewardingSystem = false;

   private String rewardingSystemSendStatus = "";
   private String rewardingSystemSendStatusMessage = "";
   
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
	
	public int getEventId() {
		return eventId;
	}
	
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	public String getPhoneNumberExtension() {
		return phoneNumberExtension;
	}
	
	public void setPhoneNumberExtension(String phoneNumberExtension) {
		this.phoneNumberExtension = phoneNumberExtension;
	}
	
	public Timestamp getGenerationDate() {
		return generationDate;
	}
	
	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Timestamp getRewardingSystemIssueDate() {
		return rewardingSystemIssueDate;
	}

	public void setRewardingSystemIssueDate(Timestamp rewardingSystemIssueDate) {
		this.rewardingSystemIssueDate = rewardingSystemIssueDate;
	}

	public boolean isPushedToRewardingSystem() {
		return pushedToRewardingSystem;
	}

	public void setPushedToRewardingSystem(boolean pushedToRewardingSystem) {
		this.pushedToRewardingSystem = pushedToRewardingSystem;
	}

	public String getRewardingSystemSendStatus() {
		return rewardingSystemSendStatus;
	}

	public void setRewardingSystemSendStatus(String rewardingSystemSendStatus) {
		this.rewardingSystemSendStatus = rewardingSystemSendStatus;
	}

	public String getRewardingSystemSendStatusMessage() {
		return rewardingSystemSendStatusMessage;
	}

	public void setRewardingSystemSendStatusMessage(
			String rewardingSystemSendStatusMessage) {
		this.rewardingSystemSendStatusMessage = rewardingSystemSendStatusMessage;
	}

	
}



