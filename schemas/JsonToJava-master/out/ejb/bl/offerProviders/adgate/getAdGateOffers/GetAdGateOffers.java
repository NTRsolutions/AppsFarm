package ejb.bl.offerProviders.adgate.getAdGateOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetAdGateOffers {
	@JsonProperty("offers") private List<OffersEntry> offers;
}
