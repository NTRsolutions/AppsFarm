package is.iWeb.sentinel.data.dao.serde.fyber.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("title") private String title;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("thumbnail") private Map<String, String> thumbnail;
	@JsonProperty("time_to_payout") private Time_to_payout time_to_payout;
	@JsonProperty("teaser") private String teaser;
	@JsonProperty("required_actions") private String required_actions;
	@JsonProperty("link") private String link;
	@JsonProperty("offer_types") private List<Offer_typesEntry> offer_types;
	@JsonProperty("payout") private Integer payout;
}
