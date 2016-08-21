package ejb.bl.offerProviders.snapdeal.getCategories;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class ListingVersions {
	@JsonProperty("v1") private Map<String, String> v1;
}
