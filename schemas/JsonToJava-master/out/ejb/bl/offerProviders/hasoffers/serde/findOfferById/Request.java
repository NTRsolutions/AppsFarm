package ejb.bl.offerProviders.hasoffers.serde.findOfferById;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Request {
	@JsonProperty("NetworkToken") private String NetworkToken;
	@JsonProperty("edition") private String edition;
	@JsonProperty("Format") private String Format;
	@JsonProperty("Service") private String Service;
	@JsonProperty("Method") private String Method;
	@JsonProperty("_gat") private Integer _gat;
	@JsonProperty("networkurl") private String networkurl;
	@JsonProperty("networkname") private String networkname;
	@JsonProperty("id") private Integer id;
	@JsonProperty("Target") private String Target;
	@JsonProperty("NetworkId") private String NetworkId;
	@JsonProperty("contain") private List<String> contain;
	@JsonProperty("callback") private String callback;
	@JsonProperty("_ga") private String _ga;
	@JsonProperty("networkid") private String networkid;
	@JsonProperty("Version") private Integer Version;
	@JsonProperty("__lc_visitor_id_1040387") private String __lc_visitor_id_1040387;
}
