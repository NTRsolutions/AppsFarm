package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class PlatformsEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("name") private String name;
}
