package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Store_app_idsEntry {
	@JsonProperty("store_id") private String store_id;
	@JsonProperty("app_id") private String app_id;
}
