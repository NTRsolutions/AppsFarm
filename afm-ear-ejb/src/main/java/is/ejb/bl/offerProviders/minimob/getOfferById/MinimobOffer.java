package is.ejb.bl.offerProviders.minimob.getOfferById;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class MinimobOffer {
	@JsonProperty("acquisitionModel") private String acquisitionModel;
	@JsonProperty("storeName") private String storeName;
	@JsonProperty("expirationDate") private String expirationDate;
	@JsonProperty("appId") private String appId;
	@JsonProperty("monthlyConversionCap") private Integer monthlyConversionCap;
	@JsonProperty("qualityScorePerCountry") private List<QualityScorePerCountryEntry> qualityScorePerCountry;
	@JsonProperty("appPreviewLink") private String appPreviewLink;
	@JsonProperty("qualityScore") private Double qualityScore;
	@JsonProperty("appTitle") private String appTitle;
	@JsonProperty("objectiveUrl") private String objectiveUrl;
	@JsonProperty("payoutModel") private String payoutModel;
	@JsonProperty("creatives") private List<CreativesEntry> creatives;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("id") private String id;
	@JsonProperty("incentivized") private String incentivized;
	@JsonProperty("targetPlatform") private String targetPlatform;
	@JsonProperty("dailyConversionCap") private Integer dailyConversionCap;
	@JsonProperty("acquisitionModelDescription") private String acquisitionModelDescription;
	@JsonProperty("targetedCountries") private List<String> targetedCountries;
	@JsonProperty("appIconLink") private String appIconLink;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("postbackUrl") private String postbackUrl;
	@JsonProperty("weeklyConversionCap") private Integer weeklyConversionCap;
	@JsonProperty("payoutCurrency") private String payoutCurrency;
	@JsonProperty("overallConversionCap") private Integer overallConversionCap;
	@JsonProperty("appDescription") private String appDescription;
	public String getAcquisitionModel() {
		return acquisitionModel;
	}
	public void setAcquisitionModel(String acquisitionModel) {
		this.acquisitionModel = acquisitionModel;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public Integer getMonthlyConversionCap() {
		return monthlyConversionCap;
	}
	public void setMonthlyConversionCap(Integer monthlyConversionCap) {
		this.monthlyConversionCap = monthlyConversionCap;
	}
	public List<QualityScorePerCountryEntry> getQualityScorePerCountry() {
		return qualityScorePerCountry;
	}
	public void setQualityScorePerCountry(
			List<QualityScorePerCountryEntry> qualityScorePerCountry) {
		this.qualityScorePerCountry = qualityScorePerCountry;
	}
	public String getAppPreviewLink() {
		return appPreviewLink;
	}
	public void setAppPreviewLink(String appPreviewLink) {
		this.appPreviewLink = appPreviewLink;
	}
	public Double getQualityScore() {
		return qualityScore;
	}
	public void setQualityScore(Double qualityScore) {
		this.qualityScore = qualityScore;
	}
	public String getAppTitle() {
		return appTitle;
	}
	public void setAppTitle(String appTitle) {
		this.appTitle = appTitle;
	}
	public String getObjectiveUrl() {
		return objectiveUrl;
	}
	public void setObjectiveUrl(String objectiveUrl) {
		this.objectiveUrl = objectiveUrl;
	}
	public String getPayoutModel() {
		return payoutModel;
	}
	public void setPayoutModel(String payoutModel) {
		this.payoutModel = payoutModel;
	}
	public List<CreativesEntry> getCreatives() {
		return creatives;
	}
	public void setCreatives(List<CreativesEntry> creatives) {
		this.creatives = creatives;
	}
	public Double getPayout() {
		return payout;
	}
	public void setPayout(Double payout) {
		this.payout = payout;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIncentivized() {
		return incentivized;
	}
	public void setIncentivized(String incentivized) {
		this.incentivized = incentivized;
	}
	public String getTargetPlatform() {
		return targetPlatform;
	}
	public void setTargetPlatform(String targetPlatform) {
		this.targetPlatform = targetPlatform;
	}
	public Integer getDailyConversionCap() {
		return dailyConversionCap;
	}
	public void setDailyConversionCap(Integer dailyConversionCap) {
		this.dailyConversionCap = dailyConversionCap;
	}
	public String getAcquisitionModelDescription() {
		return acquisitionModelDescription;
	}
	public void setAcquisitionModelDescription(String acquisitionModelDescription) {
		this.acquisitionModelDescription = acquisitionModelDescription;
	}
	public List<String> getTargetedCountries() {
		return targetedCountries;
	}
	public void setTargetedCountries(List<String> targetedCountries) {
		this.targetedCountries = targetedCountries;
	}
	public String getAppIconLink() {
		return appIconLink;
	}
	public void setAppIconLink(String appIconLink) {
		this.appIconLink = appIconLink;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPostbackUrl() {
		return postbackUrl;
	}
	public void setPostbackUrl(String postbackUrl) {
		this.postbackUrl = postbackUrl;
	}
	public Integer getWeeklyConversionCap() {
		return weeklyConversionCap;
	}
	public void setWeeklyConversionCap(Integer weeklyConversionCap) {
		this.weeklyConversionCap = weeklyConversionCap;
	}
	public String getPayoutCurrency() {
		return payoutCurrency;
	}
	public void setPayoutCurrency(String payoutCurrency) {
		this.payoutCurrency = payoutCurrency;
	}
	public Integer getOverallConversionCap() {
		return overallConversionCap;
	}
	public void setOverallConversionCap(Integer overallConversionCap) {
		this.overallConversionCap = overallConversionCap;
	}
	public String getAppDescription() {
		return appDescription;
	}
	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}
	
	
}
