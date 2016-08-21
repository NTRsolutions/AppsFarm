package ejb.bl.offerProviders.snapdeal.getCategories;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetCategories {
	@JsonProperty("apiGroups") private ApiGroups apiGroups;
	@JsonProperty("title") private String title;
	@JsonProperty("description") private String description;
}
