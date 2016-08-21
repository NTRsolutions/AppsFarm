package ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class DataEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("rule_id") private Integer rule_id;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("rule") private Rule rule;
	@JsonProperty("action") private String action;
}
