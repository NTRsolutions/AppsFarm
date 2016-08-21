package is.ejb.bl.conversionHistory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class ConversionHistoryEntry  implements java.io.Serializable {

	private String offerId; //id of an offer (we first search if the user hasn't clicked on the same offer before)
	private String sourceOfferId; //id of an offer (we first search if the user hasn't clicked on the same offer before)

	private String internalTransactionId;

    private String adProviderCodeName;
	private String offerTitle; 
    private double rewardValue;
    private String rewardCurrency;
    private boolean approved;
    private boolean successful;
    private String rewardStatus;
    private String rewardStatusMessage; //can tontain either reward request or reward response status message (former is used if it fails with message we can supply here)
    private Timestamp clickTimestamp = null; 
    private Timestamp conversionTimestamp = null;
    private Timestamp rewardTimestamp = null;
    
    private String applicationTypeName;
    private String rewardTypeName; //this links the event with one of denomination models assigned to this particular reward (there may be multiple denomination models for the same reward type only if they have different source payout currency code)

    //valide defined by UserEventCategory enum and can be of type: [install, invite]
    private String userEventCategory;  
    
   	public String getInternalTransactionId() {
		return internalTransactionId;
	}
	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getOfferTitle() {
		return offerTitle;
	}
	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}
	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}
	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}
	public double getRewardValue() {
		return rewardValue;
	}
	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}
	public String getRewardCurrency() {
		return rewardCurrency;
	}
	public void setRewardCurrency(String rewardCurrency) {
		this.rewardCurrency = rewardCurrency;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public boolean isSuccessful() {
		return successful;
	}
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
	public String getRewardStatusMessage() {
		return rewardStatusMessage;
	}
	public void setRewardStatusMessage(String rewardStatusMessage) {
		this.rewardStatusMessage = rewardStatusMessage;
	}
	public Timestamp getClickTimestamp() {
		return clickTimestamp;
	}
	public void setClickTimestamp(Timestamp clickTimestamp) {
		this.clickTimestamp = clickTimestamp;
	}
	public Timestamp getConversionTimestamp() {
		return conversionTimestamp;
	}
	public void setConversionTimestamp(Timestamp conversionTimestamp) {
		this.conversionTimestamp = conversionTimestamp;
	}
	public Timestamp getRewardTimestamp() {
		return rewardTimestamp;
	}
	public void setRewardTimestamp(Timestamp rewardTimestamp) {
		this.rewardTimestamp = rewardTimestamp;
	}
	public String getRewardTypeName() {
		return rewardTypeName;
	}
	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}
	public String getSourceOfferId() {
		return sourceOfferId;
	}
	public void setSourceOfferId(String sourceOfferId) {
		this.sourceOfferId = sourceOfferId;
	}
	public String getApplicationTypeName() {
		return applicationTypeName;
	}
	public void setApplicationTypeName(String applicationTypeName) {
		this.applicationTypeName = applicationTypeName;
	}
	public String getRewardStatus() {
		return rewardStatus;
	}
	public void setRewardStatus(String rewardStatus) {
		this.rewardStatus = rewardStatus;
	}
	public String getUserEventCategory() {
		return userEventCategory;
	}
	public void setUserEventCategory(String userEventCategory) {
		this.userEventCategory = userEventCategory;
	}
	@Override
	public String toString() {
		return "ConversionHistoryEntry [offerId=" + offerId + ", sourceOfferId=" + sourceOfferId + ", internalTransactionId=" + internalTransactionId
				+ ", adProviderCodeName=" + adProviderCodeName + ", offerTitle=" + offerTitle + ", rewardValue=" + rewardValue + ", rewardCurrency="
				+ rewardCurrency + ", approved=" + approved + ", successful=" + successful + ", rewardStatus=" + rewardStatus
				+ ", rewardStatusMessage=" + rewardStatusMessage + ", clickTimestamp=" + clickTimestamp + ", conversionTimestamp="
				+ conversionTimestamp + ", rewardTimestamp=" + rewardTimestamp + ", applicationTypeName=" + applicationTypeName + ", rewardTypeName="
				+ rewardTypeName + ", userEventCategory=" + userEventCategory + "]";
	}

	
}
