package ejb.bl.offerProviders.hasoffersExt.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class DE {
	@JsonProperty("id") private Integer id;
	@JsonProperty("name") private String name;
	@JsonProperty("code") private String code;
	@JsonProperty("regions") private List<Object> regions;
}
