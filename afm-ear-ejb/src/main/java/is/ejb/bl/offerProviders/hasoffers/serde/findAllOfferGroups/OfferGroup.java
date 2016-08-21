package is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferGroup {
	@JsonProperty("id") private Integer id;
	@JsonProperty("status") private String status;
	@JsonProperty("date_updated") private String date_updated;
	@JsonProperty("name") private String name;
	@JsonProperty("date_created") private String date_created;
	@JsonProperty("offer_count") private Integer offer_count;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDate_updated() {
		return date_updated;
	}
	public void setDate_updated(String date_updated) {
		this.date_updated = date_updated;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate_created() {
		return date_created;
	}
	public void setDate_created(String date_created) {
		this.date_created = date_created;
	}
	public Integer getOffer_count() {
		return offer_count;
	}
	public void setOffer_count(Integer offer_count) {
		this.offer_count = offer_count;
	}
	
	
}
