package is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
	@JsonProperty("Thumbnail") private Thumbnail Thumbnail;
	@JsonProperty("Offer") private Offer Offer;
	public Thumbnail getThumbnail() {
		return Thumbnail;
	}
	public void setThumbnail(Thumbnail thumbnail) {
		Thumbnail = thumbnail;
	}
	public Offer getOffer() {
		return Offer;
	}
	public void setOffer(Offer offer) {
		Offer = offer;
	}
	
}
