package is.ejb.bl.offerProviders.aarki.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AarkiOffer {
	@JsonProperty("per_user_limit") private Integer per_user_limit;
	@JsonProperty("payout_currency") private String payout_currency;
	@JsonProperty("payout_event") private String payout_event;
	@JsonProperty("requires_purchase") private Boolean requires_purchase;
	@JsonProperty("store_id") private String store_id;
	@JsonProperty("incentive_action") private String incentive_action;
	@JsonProperty("countries") private List<String> countries;
	@JsonProperty("url") private String url;
	@JsonProperty("id") private String id;
	@JsonProperty("frequency_cap") private Object frequency_cap;
	@JsonProperty("network_targeting") private List<Object> network_targeting;
	@JsonProperty("image_url") private String image_url;
	@JsonProperty("matching_requirements") private List<String> matching_requirements;
	@JsonProperty("name") private String name;
	@JsonProperty("ad_copy") private String ad_copy;
	@JsonProperty("device_targeting") private List<String> device_targeting;
	@JsonProperty("payout_amount") private Float payout_amount;
	public Integer getPer_user_limit() {
		return per_user_limit;
	}
	public void setPer_user_limit(Integer per_user_limit) {
		this.per_user_limit = per_user_limit;
	}
	public String getPayout_currency() {
		return payout_currency;
	}
	public void setPayout_currency(String payout_currency) {
		this.payout_currency = payout_currency;
	}
	public String getPayout_event() {
		return payout_event;
	}
	public void setPayout_event(String payout_event) {
		this.payout_event = payout_event;
	}
	public Boolean getRequires_purchase() {
		return requires_purchase;
	}
	public void setRequires_purchase(Boolean requires_purchase) {
		this.requires_purchase = requires_purchase;
	}
	public String getStore_id() {
		return store_id;
	}
	public void setStore_id(String store_id) {
		this.store_id = store_id;
	}
	public String getIncentive_action() {
		return incentive_action;
	}
	public void setIncentive_action(String incentive_action) {
		this.incentive_action = incentive_action;
	}
	public List<String> getCountries() {
		return countries;
	}
	public void setCountries(List<String> countries) {
		this.countries = countries;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getFrequency_cap() {
		return frequency_cap;
	}
	public void setFrequency_cap(Object frequency_cap) {
		this.frequency_cap = frequency_cap;
	}
	public List<Object> getNetwork_targeting() {
		return network_targeting;
	}
	public void setNetwork_targeting(List<Object> network_targeting) {
		this.network_targeting = network_targeting;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public List<String> getMatching_requirements() {
		return matching_requirements;
	}
	public void setMatching_requirements(List<String> matching_requirements) {
		this.matching_requirements = matching_requirements;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAd_copy() {
		return ad_copy;
	}
	public void setAd_copy(String ad_copy) {
		this.ad_copy = ad_copy;
	}
	public List<String> getDevice_targeting() {
		return device_targeting;
	}
	public void setDevice_targeting(List<String> device_targeting) {
		this.device_targeting = device_targeting;
	}
	public Float getPayout_amount() {
		return payout_amount;
	}
	public void setPayout_amount(Float payout_amount) {
		this.payout_amount = payout_amount;
	}
	
}
