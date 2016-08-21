package is.ejb.bl.offerProviders.woobi.getIOSOffers;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffersEntry {
	
	
	@JsonProperty("conversionType") private Integer conversionType;
	
	
	@JsonProperty("appSize") private Double appSize;
	
	
	@JsonProperty("artworkLong") private String artworkLong;
	
	
	@JsonProperty("appId") private String appId;
	
	
	@JsonProperty("appPublisher") private String appPublisher;
	
	
	@JsonProperty("credits") private Double credits;
	
	
	@JsonProperty("artworkWide") private String artworkWide;
	
	
	@JsonProperty("cpnId") private Integer cpnId;
	
	
	@JsonProperty("payout") private Double payout;
	
	
	@JsonProperty("appRanking") private String appRanking;
	
	
	@JsonProperty("title") private String title;
	
	
	@JsonProperty("adId") private Integer adId;
	
	
	@JsonProperty("description") private String description;
	
	
	@JsonProperty("deviceType") private String deviceType;
	
	
	@JsonProperty("incent") private String incent;
	
	
	@JsonProperty("adType") private Integer adType;
	
	
	@JsonProperty("artworkSqr") private String artworkSqr;
	
	
	@JsonProperty("isForAccumulation") private Boolean isForAccumulation;
	
	
	@JsonProperty("appType") private Integer appType;
	
	
	@JsonProperty("clickURL") private String clickURL;
	
	
	@JsonProperty("priceTerm") private String priceTerm;
	
	
	@JsonProperty("priceCurrency") private String priceCurrency;
	
	
	@JsonProperty("appDomain") private String appDomain;
	
	
	@JsonProperty("geoCode") private List<String> geoCode;
	
	
	@JsonProperty("thumbnail") private String thumbnail;
	
	
	@JsonProperty("subtitle") private String subtitle;
	
	
	@JsonProperty("artworkIcon") private String artworkIcon;
	
	
	@JsonProperty("language") private String language;
		
	
	@JsonProperty("accumPoints") private Double accumPoints;
	
	
	@JsonProperty("cpnName") private String cpnName;
	
	
	@JsonProperty("payoutType") private String payoutType;
	
	@JsonProperty("appNumOfInstalls") private String appNumOfInstalls;
	
	@JsonProperty("apk") private boolean apk;

	@JsonProperty("cpe") private boolean cpe;

	@JsonProperty("paid") private boolean paid;

	public Integer getConversionType() {
		return conversionType;
	}
	
	
	public void setConversionType(Integer conversionType) {
		this.conversionType = conversionType;
	}
	
	
	public Double getAppSize() {
		return appSize;
	}
	
	
	public void setAppSize(Double appSize) {
		this.appSize = appSize;
	}
	
	
	public String getArtworkLong() {
		return artworkLong;
	}
	
	
	public void setArtworkLong(String artworkLong) {
		this.artworkLong = artworkLong;
	}
	
	
	public String getAppId() {
		return appId;
	}
	
	
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	
	public String getAppPublisher() {
		return appPublisher;
	}
	
	
	public void setAppPublisher(String appPublisher) {
		this.appPublisher = appPublisher;
	}
	
	
	public Double getCredits() {
		return credits;
	}
	
	
	public void setCredits(Double credits) {
		this.credits = credits;
	}
	
	
	public String getArtworkWide() {
		return artworkWide;
	}
	
	
	public void setArtworkWide(String artworkWide) {
		this.artworkWide = artworkWide;
	}
	
	
	public Integer getCpnId() {
		return cpnId;
	}
	
	
	public void setCpnId(Integer cpnId) {
		this.cpnId = cpnId;
	}
	
	
	public Double getPayout() {
		return payout;
	}
	
	
	public void setPayout(Double payout) {
		this.payout = payout;
	}
	
	
	public String getAppRanking() {
		return appRanking;
	}
	
	
	public void setAppRanking(String appRanking) {
		this.appRanking = appRanking;
	}
	
	
	public String getTitle() {
		return title;
	}
	
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	public Integer getAdId() {
		return adId;
	}
	
	
	public void setAdId(Integer adId) {
		this.adId = adId;
	}
	
	
	public String getDescription() {
		return description;
	}
	
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public String getDeviceType() {
		return deviceType;
	}
	
	
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	
	public String getIncent() {
		return incent;
	}
	
	
	public void setIncent(String incent) {
		this.incent = incent;
	}
	
	
	public Integer getAdType() {
		return adType;
	}
	
	
	public void setAdType(Integer adType) {
		this.adType = adType;
	}
	
	
	public String getArtworkSqr() {
		return artworkSqr;
	}
	
	
	public void setArtworkSqr(String artworkSqr) {
		this.artworkSqr = artworkSqr;
	}
	
	
	public Boolean getIsForAccumulation() {
		return isForAccumulation;
	}
	
	
	public void setIsForAccumulation(Boolean isForAccumulation) {
		this.isForAccumulation = isForAccumulation;
	}
	
	
	public Integer getAppType() {
		return appType;
	}
	
	
	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	
	
	public String getClickURL() {
		return clickURL;
	}
	
	
	public void setClickURL(String clickURL) {
		this.clickURL = clickURL;
	}
	
	
	public String getPriceTerm() {
		return priceTerm;
	}
	
	
	public void setPriceTerm(String priceTerm) {
		this.priceTerm = priceTerm;
	}
	
	
	public String getPriceCurrency() {
		return priceCurrency;
	}
	
	
	public void setPriceCurrency(String priceCurrency) {
		this.priceCurrency = priceCurrency;
	}
	
	
	public String getAppDomain() {
		return appDomain;
	}
	
	
	public void setAppDomain(String appDomain) {
		this.appDomain = appDomain;
	}
	
	
	public List<String> getGeoCode() {
		return geoCode;
	}
	
	
	public void setGeoCode(List<String> geoCode) {
		this.geoCode = geoCode;
	}
	
	
	public String getThumbnail() {
		return thumbnail;
	}
	
	
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	
	public String getSubtitle() {
		return subtitle;
	}
	
	
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	
	
	public String getArtworkIcon() {
		return artworkIcon;
	}
	
	
	public void setArtworkIcon(String artworkIcon) {
		this.artworkIcon = artworkIcon;
	}
	
	
	public String getLanguage() {
		return language;
	}
	
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	
	public Double getAccumPoints() {
		return accumPoints;
	}
	
	
	public void setAccumPoints(Double accumPoints) {
		this.accumPoints = accumPoints;
	}
	
	
	public String getCpnName() {
		return cpnName;
	}
	
	
	public void setCpnName(String cpnName) {
		this.cpnName = cpnName;
	}
	
	
	public String getPayoutType() {
		return payoutType;
	}
	
	
	public void setPayoutType(String payoutType) {
		this.payoutType = payoutType;
	}


	public String getAppNumOfInstalls() {
		return appNumOfInstalls;
	}


	public void setAppNumOfInstalls(String appNumOfInstalls) {
		this.appNumOfInstalls = appNumOfInstalls;
	}


	public boolean isApk() {
		return apk;
	}

	public void setApk(boolean apk) {
		this.apk = apk;
	}


	public boolean isCpe() {
		return cpe;
	}


	public void setCpe(boolean cpe) {
		this.cpe = cpe;
	}


	public boolean isPaid() {
		return paid;
	}


	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	
	
}
