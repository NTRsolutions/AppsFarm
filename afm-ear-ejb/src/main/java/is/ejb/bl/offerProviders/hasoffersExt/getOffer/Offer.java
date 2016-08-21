package is.ejb.bl.offerProviders.hasoffersExt.getOffer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import is.ejb.bl.offerProviders.hasoffersExt.getThumbnail.Thumbnail;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer {
	@JsonProperty("id") private Integer id;
	@JsonProperty("currency") private String currency;
	@JsonProperty("preview_url") private String previewUrl;
	@JsonProperty("default_payout") private double defaultPayout;
	@JsonProperty("use_target_rules") private Integer use_target_rules;
	@JsonProperty("conversion_cap") private Integer conversion_cap;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("payout_cap") private Float payout_cap;
	
	private Thumbnail thumbnail; 
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUse_target_rules() {
		return use_target_rules;
	}
	public void setUse_target_rules(Integer use_target_rules) {
		this.use_target_rules = use_target_rules;
	}
	public Integer getConversion_cap() {
		return conversion_cap;
	}
	public void setConversion_cap(Integer conversion_cap) {
		this.conversion_cap = conversion_cap;
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
	public Float getPayout_cap() {
		return payout_cap;
	}
	public void setPayout_cap(Float payout_cap) {
		this.payout_cap = payout_cap;
	}
	public Thumbnail getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getPreviewUrl() {
		return previewUrl;
	}
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}
	public double getDefaultPayout() {
		return defaultPayout;
	}
	public void setDefaultPayout(double defaultPayout) {
		this.defaultPayout = defaultPayout;
	}
	
	
}
