package ejb.bl.offerProviders.adgate.getAdGateOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("icon") private String icon;
	@JsonProperty("category") private String category;
	@JsonProperty("epc") private Float epc;
	@JsonProperty("tracking_url") private String tracking_url;
	@JsonProperty("preview_url") private String preview_url;
	@JsonProperty("name") private String name;
	@JsonProperty("ua") private String ua;
	@JsonProperty("type") private String type;
	@JsonProperty("requirements") private String requirements;
	@JsonProperty("anchor") private String anchor;
	@JsonProperty("country") private String country;
	@JsonProperty("payout") private Float payout;
}
