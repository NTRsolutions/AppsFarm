package is.ejb.bl.reporting;

public class ClickLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumber;
	private String countryCode;
	private String deviceType;
	private String userEventCategory;
	private String eventType;
	private String rewardType;
	private String offerProviderName;
	private String offerId;
	private String offerName;
	private String offerCurrency;
	private double offerPayout;
	private double offerReward;
	private double profit;
	private String internalTransactionId;
	private String gaid;
	private String idfa;

	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = validate(timestamp);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = validate(phoneNumber);
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = validate(countryCode);
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = validate(deviceType);
	}

	public String getUserEventCategory() {
		return userEventCategory;
	}

	public void setUserEventCategory(String userEventCategory) {
		this.userEventCategory = validate(userEventCategory);
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = validate(eventType);
	}

	public String getRewardType() {
		return rewardType;
	}

	public void setRewardType(String rewardType) {
		this.rewardType = validate(rewardType);
	}

	public String getOfferProviderName() {
		return offerProviderName;
	}

	public void setOfferProviderName(String offerProviderName) {
		this.offerProviderName = validate(offerProviderName);
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = validate(offerId);
	}

	public String getOfferName() {
		return offerName;
	}

	public void setOfferName(String offerName) {
		this.offerName = validate(offerName);
	}

	public String getOfferCurrency() {
		return offerCurrency;
	}

	public void setOfferCurrency(String offerCurrency) {
		this.offerCurrency = validate(offerCurrency);
	}

	public double getOfferPayout() {
		return offerPayout;
	}

	public void setOfferPayout(double offerPayout) {
		this.offerPayout = offerPayout;
	}

	public double getOfferReward() {
		return offerReward;
	}

	public void setOfferReward(double offerReward) {
		this.offerReward = offerReward;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String getInternalTransactionId() {
		return internalTransactionId;
	}

	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = validate(internalTransactionId);
	}

	public String getGaid() {
		return gaid;
	}

	public void setGaid(String gaid) {
		this.gaid = validate(gaid);
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = validate(idfa);
	}

	@Override
	public String toCSV() {
		return timestamp + "," + phoneNumber + "," + countryCode + "," + removeCommas(deviceType) 
				+ "," + userEventCategory + "," + eventType + "," + rewardType 
				+ "," + removeCommas(offerProviderName) + "," + offerId + "," + removeCommas(offerName) 
				+ "," + offerCurrency + "," + offerPayout + "," + offerReward 
				+ "," + profit + "," + internalTransactionId + "," + removeCommas(gaid) + "," + removeCommas(idfa);
	}

}
