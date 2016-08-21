package is.ejb.bl.offerProviders.supersonic.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffersEntry {
	@JsonProperty("reviewOnlyUrl") private String reviewOnlyUrl;
	@JsonProperty("applicationSize") private Float applicationSize;
	@JsonProperty("expirationDate") private Object expirationDate;
	@JsonProperty("userFlow") private String userFlow;
	@JsonProperty("supportedPlatforms") private List<String> supportedPlatforms;
	@JsonProperty("disclaimer") private String disclaimer;
	@JsonProperty("applicationCategories") private String applicationCategories;
	@JsonProperty("rewards") private Integer rewards;
	@JsonProperty("purchase") private Boolean purchase;
	@JsonProperty("game") private Boolean game;
	@JsonProperty("countries") private List<Object> countries;
	@JsonProperty("minOsVersion") private String minOsVersion;
	@JsonProperty("url") private String url;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("applicationDeveloper") private String applicationDeveloper;
	@JsonProperty("callToAction") private String callToAction;
	@JsonProperty("title") private String title;
	@JsonProperty("incentAllowed") private Boolean incentAllowed;
	@JsonProperty("applicationBundleId") private String applicationBundleId;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("deviceIds") private String deviceIds;
	@JsonProperty("images") private List<ImagesEntry> images;
	@JsonProperty("conciseType") private String conciseType;
	@JsonProperty("offerId") private Integer offerId;
	public String getReviewOnlyUrl() {
		return reviewOnlyUrl;
	}
	public void setReviewOnlyUrl(String reviewOnlyUrl) {
		this.reviewOnlyUrl = reviewOnlyUrl;
	}
	public Float getApplicationSize() {
		return applicationSize;
	}
	public void setApplicationSize(Float applicationSize) {
		this.applicationSize = applicationSize;
	}
	public Object getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Object expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getUserFlow() {
		return userFlow;
	}
	public void setUserFlow(String userFlow) {
		this.userFlow = userFlow;
	}
	public List<String> getSupportedPlatforms() {
		return supportedPlatforms;
	}
	public void setSupportedPlatforms(List<String> supportedPlatforms) {
		this.supportedPlatforms = supportedPlatforms;
	}
	public String getDisclaimer() {
		return disclaimer;
	}
	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}
	public String getApplicationCategories() {
		return applicationCategories;
	}
	public void setApplicationCategories(String applicationCategories) {
		this.applicationCategories = applicationCategories;
	}
	public Integer getRewards() {
		return rewards;
	}
	public void setRewards(Integer rewards) {
		this.rewards = rewards;
	}
	public Boolean getPurchase() {
		return purchase;
	}
	public void setPurchase(Boolean purchase) {
		this.purchase = purchase;
	}
	public Boolean getGame() {
		return game;
	}
	public void setGame(Boolean game) {
		this.game = game;
	}
	public List<Object> getCountries() {
		return countries;
	}
	public void setCountries(List<Object> countries) {
		this.countries = countries;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Double getPayout() {
		return payout;
	}
	public void setPayout(Double payout) {
		this.payout = payout;
	}
	public String getApplicationDeveloper() {
		return applicationDeveloper;
	}
	public void setApplicationDeveloper(String applicationDeveloper) {
		this.applicationDeveloper = applicationDeveloper;
	}
	public String getCallToAction() {
		return callToAction;
	}
	public void setCallToAction(String callToAction) {
		this.callToAction = callToAction;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Boolean getIncentAllowed() {
		return incentAllowed;
	}
	public void setIncentAllowed(Boolean incentAllowed) {
		this.incentAllowed = incentAllowed;
	}
	public String getApplicationBundleId() {
		return applicationBundleId;
	}
	public void setApplicationBundleId(String applicationBundleId) {
		this.applicationBundleId = applicationBundleId;
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
	public String getDeviceIds() {
		return deviceIds;
	}
	public void setDeviceIds(String deviceIds) {
		this.deviceIds = deviceIds;
	}
	public List<ImagesEntry> getImages() {
		return images;
	}
	public void setImages(List<ImagesEntry> images) {
		this.images = images;
	}
	public String getConciseType() {
		return conciseType;
	}
	public void setConciseType(String conciseType) {
		this.conciseType = conciseType;
	}
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	public String getMinOsVersion() {
		return minOsVersion;
	}
	public void setMinOsVersion(String minOsVersion) {
		this.minOsVersion = minOsVersion;
	}
	
}
