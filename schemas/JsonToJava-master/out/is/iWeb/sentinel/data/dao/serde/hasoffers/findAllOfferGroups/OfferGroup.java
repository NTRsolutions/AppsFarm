package is.iWeb.sentinel.data.dao.serde.hasoffers.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OfferGroup {
	@JsonProperty("id") private Integer id;
	@JsonProperty("status") private String status;
	@JsonProperty("date_updated") private String date_updated;
	@JsonProperty("name") private String name;
	@JsonProperty("date_created") private String date_created;
	@JsonProperty("offer_count") private Integer offer_count;
}
