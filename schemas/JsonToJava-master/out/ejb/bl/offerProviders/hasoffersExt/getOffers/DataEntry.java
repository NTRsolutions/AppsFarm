package ejb.bl.offerProviders.hasoffersExt.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class DataEntry {
	@JsonProperty("OfferCategory") private Map<String, OfferCategoryEntry> OfferCategory;
	@JsonProperty("Offer") private Offer Offer;
}
