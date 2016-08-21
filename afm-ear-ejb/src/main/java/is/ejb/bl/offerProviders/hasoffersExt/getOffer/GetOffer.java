package is.ejb.bl.offerProviders.hasoffersExt.getOffer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOffer {
	@JsonProperty("OfferCategory") private Map<String, OfferCategoryEntry> OfferCategory;
	@JsonProperty("Offer") private Offer Offer;
	public Map<String, OfferCategoryEntry> getOfferCategory() {
		return OfferCategory;
	}
	public void setOfferCategory(Map<String, OfferCategoryEntry> offerCategory) {
		OfferCategory = offerCategory;
	}
	public Offer getOffer() {
		return Offer;
	}
	public void setOffer(Offer offer) {
		Offer = offer;
	}
	
	
}
