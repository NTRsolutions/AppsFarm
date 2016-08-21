package ejb.bl.offerProviders.hasoffersExt.findOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Filters {
	@JsonProperty("name") private List<String> name;
}
