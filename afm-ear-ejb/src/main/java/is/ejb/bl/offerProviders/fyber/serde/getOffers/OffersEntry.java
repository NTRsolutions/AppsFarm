package is.ejb.bl.offerProviders.fyber.serde.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffersEntry {
	@JsonProperty("title") private String title;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("thumbnail") private Map<String, String> thumbnail;
	@JsonProperty("time_to_payout") private Time_to_payout time_to_payout;
	@JsonProperty("teaser") private String teaser;
	@JsonProperty("required_actions") private String required_actions;
	@JsonProperty("link") private String link;
	@JsonProperty("offer_types") private List<Offer_typesEntry> offer_types;
	@JsonProperty("payout") private Integer payout;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public Map<String, String> getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Map<String, String> thumbnail) {
		this.thumbnail = thumbnail;
	}
	public Time_to_payout getTime_to_payout() {
		return time_to_payout;
	}
	public void setTime_to_payout(Time_to_payout time_to_payout) {
		this.time_to_payout = time_to_payout;
	}
	public String getTeaser() {
		return teaser;
	}
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
	public String getRequired_actions() {
		return required_actions;
	}
	public void setRequired_actions(String required_actions) {
		this.required_actions = required_actions;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public List<Offer_typesEntry> getOffer_types() {
		return offer_types;
	}
	public void setOffer_types(List<Offer_typesEntry> offer_types) {
		this.offer_types = offer_types;
	}
	public Integer getPayout() {
		return payout;
	}
	public void setPayout(Integer payout) {
		this.payout = payout;
	}
	
}
