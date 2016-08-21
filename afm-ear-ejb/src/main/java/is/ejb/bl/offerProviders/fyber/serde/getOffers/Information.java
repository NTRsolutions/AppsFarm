package is.ejb.bl.offerProviders.fyber.serde.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Information {
	@JsonProperty("support_url") private String support_url;
	@JsonProperty("appid") private Integer appid;
	@JsonProperty("virtual_currency") private String virtual_currency;
	@JsonProperty("language") private String language;
	@JsonProperty("app_name") private String app_name;
	@JsonProperty("country") private String country;
	
	public String getSupport_url() {
		return support_url;
	}
	public void setSupport_url(String support_url) {
		this.support_url = support_url;
	}
	public Integer getAppid() {
		return appid;
	}
	public void setAppid(Integer appid) {
		this.appid = appid;
	}
	public String getVirtual_currency() {
		return virtual_currency;
	}
	public void setVirtual_currency(String virtual_currency) {
		this.virtual_currency = virtual_currency;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getApp_name() {
		return app_name;
	}
	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
}
