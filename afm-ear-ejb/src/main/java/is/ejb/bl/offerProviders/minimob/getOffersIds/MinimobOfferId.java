package is.ejb.bl.offerProviders.minimob.getOffersIds;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class MinimobOfferId {
	@JsonProperty("storeName") private String storeName;
	@JsonProperty("monthlyConversionCap") private Integer monthlyConversionCap;
	@JsonProperty("qualityScore") private Double qualityScore;
	@JsonProperty("payoutModel") private String payoutModel;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("id") private String id;
	@JsonProperty("incentivized") private String incentivized;
	@JsonProperty("targetPlatform") private String targetPlatform;
	@JsonProperty("dailyConversionCap") private Integer dailyConversionCap;
	@JsonProperty("targetedCountries") private List<String> targetedCountries;
	@JsonProperty("name") private String name;
	@JsonProperty("weeklyConversionCap") private Integer weeklyConversionCap;
	@JsonProperty("payoutCurrency") private String payoutCurrency;
	@JsonProperty("overallConversionCap") private Integer overallConversionCap;
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public Integer getMonthlyConversionCap() {
		return monthlyConversionCap;
	}
	public void setMonthlyConversionCap(Integer monthlyConversionCap) {
		this.monthlyConversionCap = monthlyConversionCap;
	}
	public Double getQualityScore() {
		return qualityScore;
	}
	public void setQualityScore(Double qualityScore) {
		this.qualityScore = qualityScore;
	}
	public String getPayoutModel() {
		return payoutModel;
	}
	public void setPayoutModel(String payoutModel) {
		this.payoutModel = payoutModel;
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
	public List<String> getTargetedCountries() {
		return targetedCountries;
	}
	public void setTargetedCountries(List<String> targetedCountries) {
		this.targetedCountries = targetedCountries;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	
}
