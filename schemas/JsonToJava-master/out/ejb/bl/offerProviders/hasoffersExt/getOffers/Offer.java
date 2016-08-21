package ejb.bl.offerProviders.hasoffersExt.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Offer {
	@JsonProperty("id") private Integer id;
	@JsonProperty("use_target_rules") private Integer use_target_rules;
	@JsonProperty("conversion_cap") private Integer conversion_cap;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("payout_cap") private Float payout_cap;
}
