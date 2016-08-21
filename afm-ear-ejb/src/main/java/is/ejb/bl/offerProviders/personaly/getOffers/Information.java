package is.ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Information {
	@JsonProperty("virtual_currency") private String virtual_currency;
	@JsonProperty("app_id") private Integer app_id;
	@JsonProperty("app_name") private String app_name;
	public String getVirtual_currency() {
		return virtual_currency;
	}
	public void setVirtual_currency(String virtual_currency) {
		this.virtual_currency = virtual_currency;
	}
	public Integer getApp_id() {
		return app_id;
	}
	public void setApp_id(Integer app_id) {
		this.app_id = app_id;
	}
	public String getApp_name() {
		return app_name;
	}
	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}
	
	
}
