package ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Data {
	@JsonProperty("Thumbnail") private Thumbnail Thumbnail;
	@JsonProperty("Offer") private Offer Offer;
}
