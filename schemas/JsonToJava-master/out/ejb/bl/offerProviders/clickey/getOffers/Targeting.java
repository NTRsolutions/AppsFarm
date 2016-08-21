package ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Targeting {
	@JsonProperty("os") private List<String> os;
	@JsonProperty("countries") private List<String> countries;
	@JsonProperty("os_version") private List<Object> os_version;
}
