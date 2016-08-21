package is.ejb.bl.offerProviders.woobi.getAndroidOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WoobiAndroidGetOffers {
	@JsonProperty("totalCreditsForWeb") private Double totalCreditsForWeb;
	@JsonProperty("appId") private Integer appId;
	@JsonProperty("offersUTC") private Long offersUTC;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("noSession") private Boolean noSession;
	@JsonProperty("offersCount") private Integer offersCount;
	@JsonProperty("isForAccumulation") private Boolean isForAccumulation;
	@JsonProperty("getMoreOffer") private Boolean getMoreOffer;
	@JsonProperty("creditsSingular") private String creditsSingular;
	@JsonProperty("payoutCurrency") private String payoutCurrency;
	@JsonProperty("isFacebookApp") private Boolean isFacebookApp;
	@JsonProperty("creditsPlural") private String creditsPlural;
	@JsonProperty("isUnruly") private Boolean isUnruly;
	@JsonProperty("country") private String country;
	public Double getTotalCreditsForWeb() {
		return totalCreditsForWeb;
	}
	public void setTotalCreditsForWeb(Double totalCreditsForWeb) {
		this.totalCreditsForWeb = totalCreditsForWeb;
	}
	public Integer getAppId() {
		return appId;
	}
	public void setAppId(Integer appId) {
		this.appId = appId;
	}
	public Long getOffersUTC() {
		return offersUTC;
	}
	public void setOffersUTC(Long offersUTC) {
		this.offersUTC = offersUTC;
	}
	public List<OffersEntry> getOffers() {
		return offers;
	}
	public void setOffers(List<OffersEntry> offers) {
		this.offers = offers;
	}
	public Boolean getNoSession() {
		return noSession;
	}
	public void setNoSession(Boolean noSession) {
		this.noSession = noSession;
	}
	public Integer getOffersCount() {
		return offersCount;
	}
	public void setOffersCount(Integer offersCount) {
		this.offersCount = offersCount;
	}
	public Boolean getIsForAccumulation() {
		return isForAccumulation;
	}
	public void setIsForAccumulation(Boolean isForAccumulation) {
		this.isForAccumulation = isForAccumulation;
	}
	public Boolean getGetMoreOffer() {
		return getMoreOffer;
	}
	public void setGetMoreOffer(Boolean getMoreOffer) {
		this.getMoreOffer = getMoreOffer;
	}
	public String getCreditsSingular() {
		return creditsSingular;
	}
	public void setCreditsSingular(String creditsSingular) {
		this.creditsSingular = creditsSingular;
	}
	public String getPayoutCurrency() {
		return payoutCurrency;
	}
	public void setPayoutCurrency(String payoutCurrency) {
		this.payoutCurrency = payoutCurrency;
	}
	public Boolean getIsFacebookApp() {
		return isFacebookApp;
	}
	public void setIsFacebookApp(Boolean isFacebookApp) {
		this.isFacebookApp = isFacebookApp;
	}
	public String getCreditsPlural() {
		return creditsPlural;
	}
	public void setCreditsPlural(String creditsPlural) {
		this.creditsPlural = creditsPlural;
	}
	public Boolean getIsUnruly() {
		return isUnruly;
	}
	public void setIsUnruly(Boolean isUnruly) {
		this.isUnruly = isUnruly;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	
}
