package is.ejb.bl.offerProviders.hasoffers.serde.findOfferById;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
	@JsonProperty("Offer") private Offer Offer;

	public Offer getOffer() {
		return Offer;
	}

	public void setOffer(Offer offer) {
		Offer = offer;
	}
	
}
