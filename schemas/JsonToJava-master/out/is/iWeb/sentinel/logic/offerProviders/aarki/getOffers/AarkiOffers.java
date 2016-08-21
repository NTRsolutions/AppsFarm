package is.iWeb.sentinel.logic.offerProviders.aarki.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class AarkiOffers {
	@JsonProperty("per_user_limit") private Integer per_user_limit;
	@JsonProperty("payout_currency") private String payout_currency;
	@JsonProperty("payout_event") private String payout_event;
	@JsonProperty("requires_purchase") private Boolean requires_purchase;
	@JsonProperty("store_id") private String store_id;
	@JsonProperty("incentive_action") private String incentive_action;
	@JsonProperty("countries") private List<String> countries;
	@JsonProperty("url") private String url;
	@JsonProperty("id") private String id;
	@JsonProperty("frequency_cap") private Object frequency_cap;
	@JsonProperty("network_targeting") private List<Object> network_targeting;
	@JsonProperty("image_url") private String image_url;
	@JsonProperty("matching_requirements") private List<String> matching_requirements;
	@JsonProperty("name") private String name;
	@JsonProperty("ad_copy") private String ad_copy;
	@JsonProperty("device_targeting") private List<String> device_targeting;
	@JsonProperty("payout_amount") private Float payout_amount;
}
