package is.ejb.bl.offerWall.external;

public class PersonalyCallbackDetails {
	private String userId;
	private String amount;
	private String offerId;
	private String appId;
	private String signature;
	private String offerName;
	private String packageId;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getOfferName() {
		return offerName;
	}
	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}
	public String getPackageId() {
		return packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	@Override
	public String toString() {
		return "PersonalyCallbackDetails [userId=" + userId + ", amount=" + amount + ", offerId=" + offerId + ", appId="
				+ appId + ", signature=" + signature + ", offerName=" + offerName + ", packageId=" + packageId + "]";
	}
	
	
}
