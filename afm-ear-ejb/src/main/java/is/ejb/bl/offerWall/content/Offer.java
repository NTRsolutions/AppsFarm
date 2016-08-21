package is.ejb.bl.offerWall.content;

import is.ejb.bl.business.OfferType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class Offer  implements java.io.Serializable {
	
	private String id;
	private String affiliateId;
	private int internalNetworkId;
	private String sourceId; //id obtained from ad provider
	private String title;
	private String description;
	private String adProviderCodeName;
	private String url;
	private String previewUrl;
	private String callToAction=""; // Install and Complete an in-app purchase
	private List<String> trackingRequirements = new ArrayList<String>(); //"matching_requirements": ["advertising_id", "device_id"],
	
	private Map<String, String> image;
	private double rating;

	private String currency;
	private double payout;
	private double payoutInTargetCurrency;
	
	private String rewardCurrency;
	private double rewardValue;

	private double revenueSplitValue;
	
	private double profitValue;
	
	private String rewardType;
	
	private boolean incentivised = true; //by default all offers are incentivised
	
	private ArrayList<String> supportedCountryCodes = new ArrayList<String>();
	private ArrayList<String> supportedTargetDevices = new ArrayList<String>();

	private boolean rewardPaymentInstant;
	private boolean rewardPaymentTopUp;
	
	private String type = OfferType.STANDARD.toString();
	private boolean positioned = false;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
		//this.description = "";
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}
	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public Map<String, String> getImage() {
		return image;
	}
	public void setImage(Map<String, String> image) {
		this.image = image;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getPreviewUrl() {
		return previewUrl;
	}
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}
	public double getPayout() {
		return payout;
	}
	public void setPayout(double payout) {
		this.payout = payout;
	}
	public int getInternalNetworkId() {
		return internalNetworkId;
	}
	public void setInternalNetworkId(int internalNetworkId) {
		this.internalNetworkId = internalNetworkId;
	}
	public String getRewardCurrency() {
		return rewardCurrency;
	}
	public void setRewardCurrency(String rewardCurrency) {
		this.rewardCurrency = rewardCurrency;
	}
	public double getRewardValue() {
		return rewardValue;
	}
	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}
	public double getRevenueSplitValue() {
		return revenueSplitValue;
	}
	public void setRevenueSplitValue(double revenueSplitValue) {
		this.revenueSplitValue = revenueSplitValue;
	}
	public double getPayoutInTargetCurrency() {
		return payoutInTargetCurrency;
	}
	public void setPayoutInTargetCurrency(double payoutInTargetCurrency) {
		this.payoutInTargetCurrency = payoutInTargetCurrency;
	}
	public double getProfitValue() {
		return profitValue;
	}
	public void setProfitValue(double profitValue) {
		this.profitValue = profitValue;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public ArrayList<String> getSupportedCountryCodes() {
		return supportedCountryCodes;
	}
	public void setSupportedCountryCodes(ArrayList<String> supportedCountryCodes) {
		this.supportedCountryCodes = supportedCountryCodes;
	}
	public ArrayList<String> getSupportedTargetDevices() {
		return supportedTargetDevices;
	}
	public void setSupportedTargetDevices(ArrayList<String> supportedTargetDevices) {
		this.supportedTargetDevices = supportedTargetDevices;
	}
	public String getCallToAction() {
		return callToAction;
	}
	public void setCallToAction(String callToAction) {
		this.callToAction = callToAction;
	}
	public List<String> getTrackingRequirements() {
		return trackingRequirements;
	}
	public void setTrackingRequirements(List<String> trackingRequirements) {
		this.trackingRequirements = trackingRequirements;
	}
	public boolean isIncentivised() {
		return incentivised;
	}
	public void setIncentivised(boolean incentivised) {
		this.incentivised = incentivised;
	}
	public String getAffiliateId() {
		return affiliateId;
	}
	public void setAffiliateId(String affiliateId) {
		this.affiliateId = affiliateId;
	}
	public boolean isRewardPaymentInstant() {
		return rewardPaymentInstant;
	}
	public void setRewardPaymentInstant(boolean rewardPaymentInstant) {
		this.rewardPaymentInstant = rewardPaymentInstant;
	}
	public boolean isRewardPaymentTopUp() {
		return rewardPaymentTopUp;
	}
	public void setRewardPaymentTopUp(boolean rewardPaymentTopUp) {
		this.rewardPaymentTopUp = rewardPaymentTopUp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isPositioned() {
		return positioned;
	}
	public void setPositioned(boolean positioned) {
		this.positioned = positioned;
	}

	
}
