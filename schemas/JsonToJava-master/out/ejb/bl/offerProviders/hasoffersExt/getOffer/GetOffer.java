package ejb.bl.offerProviders.hasoffersExt.getOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOffer {
	@JsonProperty("OfferCategory") private Map<String, OfferCategoryEntry> OfferCategory;
	@JsonProperty("Offer") private Offer Offer;
}
