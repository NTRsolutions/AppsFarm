package ejb.bl.offerProviders.hasoffersExt.getOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OfferCategoryEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("name") private String name;
}
