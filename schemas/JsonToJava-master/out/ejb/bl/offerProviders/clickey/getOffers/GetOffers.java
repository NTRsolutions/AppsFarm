package ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOffers {
	@JsonProperty("count") private Integer count;
	@JsonProperty("status") private String status;
	@JsonProperty("pagecount") private Integer pagecount;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("pageindex") private Integer pageindex;
	@JsonProperty("available") private Integer available;
}
