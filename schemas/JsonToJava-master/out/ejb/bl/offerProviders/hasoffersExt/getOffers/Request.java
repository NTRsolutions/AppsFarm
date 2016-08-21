package ejb.bl.offerProviders.hasoffersExt.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Request {
	@JsonProperty("Service") private String Service;
	@JsonProperty("Format") private String Format;
	@JsonProperty("NetworkId") private String NetworkId;
	@JsonProperty("Target") private String Target;
	@JsonProperty("Method") private String Method;
	@JsonProperty("api_key") private String api_key;
	@JsonProperty("contain") private Map<String, String> contain;
	@JsonProperty("_ga") private String _ga;
	@JsonProperty("Version") private Integer Version;
	@JsonProperty("__lc_visitor_id_1040387") private String __lc_visitor_id_1040387;
	@JsonProperty("fields") private List<String> fields;
}
