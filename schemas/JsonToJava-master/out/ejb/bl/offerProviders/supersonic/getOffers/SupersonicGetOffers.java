package ejb.bl.offerProviders.supersonic.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class SupersonicGetOffers {
	@JsonProperty("offers") private Map<String, OffersEntry> offers;
	@JsonProperty("offersPerPage") private Integer offersPerPage;
	@JsonProperty("offersCount") private Integer offersCount;
	@JsonProperty("currentPage") private Integer currentPage;
}
