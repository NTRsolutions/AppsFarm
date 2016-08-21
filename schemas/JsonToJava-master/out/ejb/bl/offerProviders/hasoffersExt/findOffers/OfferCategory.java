package ejb.bl.offerProviders.hasoffersExt.findOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OfferCategory {
	@JsonProperty("id") private Integer id;
	@JsonProperty("name") private String name;
}
