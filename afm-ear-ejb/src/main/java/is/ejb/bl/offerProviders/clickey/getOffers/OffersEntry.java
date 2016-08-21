package is.ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffersEntry {
	@JsonProperty("caps_total") private String caps_total;
	@JsonProperty("icon") private String icon;
	@JsonProperty("offer_model") private String offer_model;
	@JsonProperty("targeting") private Targeting targeting;
	@JsonProperty("caps_daily_remaining") private String caps_daily_remaining;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("creative") private List<CreativeEntry> creative;
	@JsonProperty("caps_total_remaining") private String caps_total_remaining;
	@JsonProperty("instructions") private String instructions;
	@JsonProperty("link") private String link;
	@JsonProperty("type") private String type;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("offer_type") private String offer_type;
	@JsonProperty("free") private Boolean free;
	@JsonProperty("category") private List<String> category;
	@JsonProperty("caps_daily") private String caps_daily;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("expiration_date") private Object expiration_date;
	@JsonProperty("traffic_type") private String traffic_type;
	@JsonProperty("app_id") private String app_id;
	@JsonProperty("description_lang") private Object description_lang;
	@JsonProperty("r") private boolean r;
	//@JsonProperty("lead_type") private String lead_type;
	
	
	public String getCaps_total() {
		return caps_total;
	}
	public void setCaps_total(String caps_total) {
		this.caps_total = caps_total;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getOffer_model() {
		return offer_model;
	}
	public void setOffer_model(String offer_model) {
		this.offer_model = offer_model;
	}
	public Targeting getTargeting() {
		return targeting;
	}
	public void setTargeting(Targeting targeting) {
		this.targeting = targeting;
	}
	public String getCaps_daily_remaining() {
		return caps_daily_remaining;
	}
	public void setCaps_daily_remaining(String caps_daily_remaining) {
		this.caps_daily_remaining = caps_daily_remaining;
	}
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public List<CreativeEntry> getCreative() {
		return creative;
	}
	public void setCreative(List<CreativeEntry> creative) {
		this.creative = creative;
	}
	public String getCaps_total_remaining() {
		return caps_total_remaining;
	}
	public void setCaps_total_remaining(String caps_total_remaining) {
		this.caps_total_remaining = caps_total_remaining;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Double getPayout() {
		return payout;
	}
	public void setPayout(Double payout) {
		this.payout = payout;
	}
	public String getOffer_type() {
		return offer_type;
	}
	public void setOffer_type(String offer_type) {
		this.offer_type = offer_type;
	}
	public Boolean getFree() {
		return free;
	}
	public void setFree(Boolean free) {
		this.free = free;
	}
	public List<String> getCategory() {
		return category;
	}
	public void setCategory(List<String> category) {
		this.category = category;
	}
	public String getCaps_daily() {
		return caps_daily;
	}
	public void setCaps_daily(String caps_daily) {
		this.caps_daily = caps_daily;
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
	public Object getExpiration_date() {
		return expiration_date;
	}
	public void setExpiration_date(Object expiration_date) {
		this.expiration_date = expiration_date;
	}
	public String getTraffic_type() {
		return traffic_type;
	}
	public void setTraffic_type(String traffic_type) {
		this.traffic_type = traffic_type;
	}
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public Object getDescription_lang() {
		return description_lang;
	}
	public void setDescription_lang(Object description_lang) {
		this.description_lang = description_lang;
	}
	public boolean isR() {
		return r;
	}
	public void setR(boolean r) {
		this.r = r;
	}
//	public String getLead_type() {
//		return lead_type;
//	}
//	public void setLead_type(String lead_type) {
//		this.lead_type = lead_type;
//	}
	
}
