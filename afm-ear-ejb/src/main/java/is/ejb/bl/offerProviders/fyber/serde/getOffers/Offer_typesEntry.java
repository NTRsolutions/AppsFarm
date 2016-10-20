package is.ejb.bl.offerProviders.fyber.serde.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer_typesEntry {
	@JsonProperty("readable") private String readable;
	@JsonProperty("offer_type_id") private Integer offer_type_id;
	
	public String getReadable() {
		return readable;
	}
	public void setReadable(String readable) {
		this.readable = readable;
	}
	public Integer getOffer_type_id() {
		return offer_type_id;
	}
	public void setOffer_type_id(Integer offer_type_id) {
		this.offer_type_id = offer_type_id;
	}
	
}
