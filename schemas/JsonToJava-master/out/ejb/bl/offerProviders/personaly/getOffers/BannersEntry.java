package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class BannersEntry {
	@JsonProperty("type") private String type;
	@JsonProperty("url") private String url;
}
