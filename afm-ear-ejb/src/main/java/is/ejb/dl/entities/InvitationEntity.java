package is.ejb.dl.entities;

import is.ejb.bl.business.ReferralType;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "Invitation")
public class InvitationEntity implements Serializable {
	
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;
	
	private String emailInviting;
	private String phoneNumberInviting;
	private String phoneNumberExtInviting;
	
	private String emailInvited;
	private String phoneNumberInvited;
	private String phoneNumberExtInvited;
	
	private String code;
	private String rewardType;
	private String rewardTypeName;
	private double rewardValueInvited;
	private String rewardValueCurrencyCodeInvited;
	
	private double rewardValueInviting;
	private String rewardValueCurrencyCodeInviting;
	
	private Timestamp dateOfInvitation;
	private Timestamp dateOfRegistration;
	
	private boolean isValid = false;
	private boolean isRealized;

	private String processingStatusMessage;
	private String processingStatus;

	private String invitedInternalTransactionId;
	private String invitedRewardRequestStatusMessage;
	private String invitedRewardRequestStatus;
	private String invitedRewardResponseStatusMessage;
	private String invitedRewardResponseStatus;
	private Timestamp invitedRewardRequestDate;
	private Timestamp invitedRewardResponseDate;

	private String invitingInternalTransactionId;
	private String invitingRewardRequestStatusMessage;
	private String invitingRewardRequestStatus;
	private String invitingRewardResponseStatusMessage;
	private String invitingRewardResponseStatus;
	private Timestamp invitingRewardRequestDate;
	private Timestamp invitingRewardResponseDate;
	
	private String invitingFBInviteCode;
	
	@Enumerated(EnumType.STRING)
	private ReferralType referralSource;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmailInviting() {
		return emailInviting;
	}

	public void setEmailInviting(String emailInviting) {
		this.emailInviting = emailInviting;
	}

	public String getPhoneNumberInviting() {
		return phoneNumberInviting;
	}

	public void setPhoneNumberInviting(String phoneNumberInviting) {
		this.phoneNumberInviting = phoneNumberInviting;
	}

	public String getEmailInvited() {
		return emailInvited;
	}

	public void setEmailInvited(String emailInvited) {
		this.emailInvited = emailInvited;
	}

	public String getPhoneNumberInvited() {
		return phoneNumberInvited;
	}

	public void setPhoneNumberInvited(String phoneNumberInvited) {
		this.phoneNumberInvited = phoneNumberInvited;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRewardType() {
		return rewardType;
	}

	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}

	public Timestamp getDateOfInvitation() {
		return dateOfInvitation;
	}

	public void setDateOfInvitation(Timestamp dateOfInvitation) {
		this.dateOfInvitation = dateOfInvitation;
	}

	public Timestamp getDateOfRegistration() {
		return dateOfRegistration;
	}

	public void setDateOfRegistration(Timestamp dateOfRegistration) {
		this.dateOfRegistration = dateOfRegistration;
	}

	public boolean isRealized() {
		return isRealized;
	}

	public void setRealized(boolean isRealized) {
		this.isRealized = isRealized;
	}

	public String getPhoneNumberExtInviting() {
		return phoneNumberExtInviting;
	}

	public void setPhoneNumberExtInviting(String phoneNumberExtInviting) {
		this.phoneNumberExtInviting = phoneNumberExtInviting;
	}

	public String getPhoneNumberExtInvited() {
		return phoneNumberExtInvited;
	}

	public void setPhoneNumberExtInvited(String phoneNumberExtInvited) {
		this.phoneNumberExtInvited = phoneNumberExtInvited;
	}

	public String getInvitedInternalTransactionId() {
		return invitedInternalTransactionId;
	}

	public void setInvitedInternalTransactionId(String invitedInternalTransactionId) {
		this.invitedInternalTransactionId = invitedInternalTransactionId;
	}

	public String getInvitedRewardRequestStatusMessage() {
		return invitedRewardRequestStatusMessage;
	}

	public void setInvitedRewardRequestStatusMessage(
			String invitedRewardRequestStatusMessage) {
		this.invitedRewardRequestStatusMessage = invitedRewardRequestStatusMessage;
	}

	public String getInvitedRewardRequestStatus() {
		return invitedRewardRequestStatus;
	}

	public void setInvitedRewardRequestStatus(String invitedRewardRequestStatus) {
		this.invitedRewardRequestStatus = invitedRewardRequestStatus;
	}

	public String getInvitedRewardResponseStatusMessage() {
		return invitedRewardResponseStatusMessage;
	}

	public void setInvitedRewardResponseStatusMessage(
			String invitedRewardResponseStatusMessage) {
		this.invitedRewardResponseStatusMessage = invitedRewardResponseStatusMessage;
	}

	public String getInvitedRewardResponseStatus() {
		return invitedRewardResponseStatus;
	}

	public void setInvitedRewardResponseStatus(String invitedRewardResponseStatus) {
		this.invitedRewardResponseStatus = invitedRewardResponseStatus;
	}

	public String getInvitingInternalTransactionId() {
		return invitingInternalTransactionId;
	}

	public void setInvitingInternalTransactionId(
			String invitingInternalTransactionId) {
		this.invitingInternalTransactionId = invitingInternalTransactionId;
	}

	public String getInvitingRewardRequestStatusMessage() {
		return invitingRewardRequestStatusMessage;
	}

	public void setInvitingRewardRequestStatusMessage(
			String invitingRewardRequestStatusMessage) {
		this.invitingRewardRequestStatusMessage = invitingRewardRequestStatusMessage;
	}

	public String getInvitingRewardRequestStatus() {
		return invitingRewardRequestStatus;
	}

	public void setInvitingRewardRequestStatus(String invitingRewardRequestStatus) {
		this.invitingRewardRequestStatus = invitingRewardRequestStatus;
	}

	public String getInvitingRewardResponseStatusMessage() {
		return invitingRewardResponseStatusMessage;
	}

	public void setInvitingRewardResponseStatusMessage(
			String invitingRewardResponseStatusMessage) {
		this.invitingRewardResponseStatusMessage = invitingRewardResponseStatusMessage;
	}

	public String getInvitingRewardResponseStatus() {
		return invitingRewardResponseStatus;
	}

	public void setInvitingRewardResponseStatus(String invitingRewardResponseStatus) {
		this.invitingRewardResponseStatus = invitingRewardResponseStatus;
	}

	public String getProcessingStatusMessage() {
		return processingStatusMessage;
	}

	public void setProcessingStatusMessage(String processingStatusMessage) {
		this.processingStatusMessage = processingStatusMessage;
	}

	public String getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}

	public Timestamp getInvitedRewardRequestDate() {
		return invitedRewardRequestDate;
	}

	public void setInvitedRewardRequestDate(Timestamp invitedRewardRequestDate) {
		this.invitedRewardRequestDate = invitedRewardRequestDate;
	}

	public Timestamp getInvitedRewardResponseDate() {
		return invitedRewardResponseDate;
	}

	public void setInvitedRewardResponseDate(Timestamp invitedRewardResponseDate) {
		this.invitedRewardResponseDate = invitedRewardResponseDate;
	}

	public Timestamp getInvitingRewardRequestDate() {
		return invitingRewardRequestDate;
	}

	public void setInvitingRewardRequestDate(Timestamp invitingRewardRequestDate) {
		this.invitingRewardRequestDate = invitingRewardRequestDate;
	}

	public Timestamp getInvitingRewardResponseDate() {
		return invitingRewardResponseDate;
	}

	public void setInvitingRewardResponseDate(Timestamp invitingRewardResponseDate) {
		this.invitingRewardResponseDate = invitingRewardResponseDate;
	}

	public double getRewardValueInvited() {
		return rewardValueInvited;
	}

	public void setRewardValueInvited(double rewardValueInvited) {
		this.rewardValueInvited = rewardValueInvited;
	}

	public String getRewardValueCurrencyCodeInvited() {
		return rewardValueCurrencyCodeInvited;
	}

	public void setRewardValueCurrencyCodeInvited(
			String rewardValueCurrencyCodeInvited) {
		this.rewardValueCurrencyCodeInvited = rewardValueCurrencyCodeInvited;
	}

	public double getRewardValueInviting() {
		return rewardValueInviting;
	}

	public void setRewardValueInviting(double rewardValueInviting) {
		this.rewardValueInviting = rewardValueInviting;
	}

	public String getRewardValueCurrencyCodeInviting() {
		return rewardValueCurrencyCodeInviting;
	}

	public void setRewardValueCurrencyCodeInviting(
			String rewardValueCurrencyCodeInviting) {
		this.rewardValueCurrencyCodeInviting = rewardValueCurrencyCodeInviting;
	}

	public String getInvitingFBInviteCode() {
		return invitingFBInviteCode;
	}

	public void setInvitingFBInviteCode(String invitingFBInviteCode) {
		this.invitingFBInviteCode = invitingFBInviteCode;
	}
	
	public ReferralType getReferralSource() {
		return referralSource;
	}

	public void setReferralSource(ReferralType referralSource) {
		this.referralSource = referralSource;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	@Override
	public String toString() {
		return "InvitationEntity [id=" + id + ", emailInviting=" + emailInviting + ", phoneNumberInviting="
				+ phoneNumberInviting + ", phoneNumberExtInviting=" + phoneNumberExtInviting + ", emailInvited="
				+ emailInvited + ", phoneNumberInvited=" + phoneNumberInvited + ", phoneNumberExtInvited="
				+ phoneNumberExtInvited + ", code=" + code + ", rewardType=" + rewardType + ", rewardTypeName="
				+ rewardTypeName + ", rewardValueInvited=" + rewardValueInvited + ", rewardValueCurrencyCodeInvited="
				+ rewardValueCurrencyCodeInvited + ", rewardValueInviting=" + rewardValueInviting
				+ ", rewardValueCurrencyCodeInviting=" + rewardValueCurrencyCodeInviting + ", dateOfInvitation="
				+ dateOfInvitation + ", dateOfRegistration=" + dateOfRegistration + ", isValid=" + isValid
				+ ", isRealized=" + isRealized + ", processingStatusMessage=" + processingStatusMessage
				+ ", processingStatus=" + processingStatus + ", invitedInternalTransactionId="
				+ invitedInternalTransactionId + ", invitedRewardRequestStatusMessage="
				+ invitedRewardRequestStatusMessage + ", invitedRewardRequestStatus=" + invitedRewardRequestStatus
				+ ", invitedRewardResponseStatusMessage=" + invitedRewardResponseStatusMessage
				+ ", invitedRewardResponseStatus=" + invitedRewardResponseStatus + ", invitedRewardRequestDate="
				+ invitedRewardRequestDate + ", invitedRewardResponseDate=" + invitedRewardResponseDate
				+ ", invitingInternalTransactionId=" + invitingInternalTransactionId
				+ ", invitingRewardRequestStatusMessage=" + invitingRewardRequestStatusMessage
				+ ", invitingRewardRequestStatus=" + invitingRewardRequestStatus
				+ ", invitingRewardResponseStatusMessage=" + invitingRewardResponseStatusMessage
				+ ", invitingRewardResponseStatus=" + invitingRewardResponseStatus + ", invitingRewardRequestDate="
				+ invitingRewardRequestDate + ", invitingRewardResponseDate=" + invitingRewardResponseDate
				+ ", invitingFBInviteCode=" + invitingFBInviteCode + ", referralSource=" + referralSource + "]";
	}

	
	
	
	
}
