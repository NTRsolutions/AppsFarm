package is.ejb.bl.offerProviders.adgate.getAdGateOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffersEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("icon") private String icon;
	@JsonProperty("category") private String category;
	@JsonProperty("epc") private Float epc;
	@JsonProperty("tracking_url") private String tracking_url;
	@JsonProperty("preview_url") private String preview_url;
	@JsonProperty("name") private String name;
	@JsonProperty("ua") private String ua;
	@JsonProperty("type") private String type;
	@JsonProperty("requirements") private String requirements;
	@JsonProperty("anchor") private String anchor;
	@JsonProperty("country") private String country;
	@JsonProperty("payout") private Float payout;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Float getEpc() {
		return epc;
	}
	public void setEpc(Float epc) {
		this.epc = epc;
	}
	public String getTracking_url() {
		return tracking_url;
	}
	public void setTracking_url(String tracking_url) {
		this.tracking_url = tracking_url;
	}
	public String getPreview_url() {
		return preview_url;
	}
	public void setPreview_url(String preview_url) {
		this.preview_url = preview_url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUa() {
		return ua;
	}
	public void setUa(String ua) {
		this.ua = ua;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRequirements() {
		return requirements;
	}
	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Float getPayout() {
		return payout;
	}
	public void setPayout(Float payout) {
		this.payout = payout;
	}
}
