package is.iWeb.sentinel.data.dao.serde.hasoffers.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Request {
	@JsonProperty("NetworkToken") private String NetworkToken;
	@JsonProperty("edition") private String edition;
	@JsonProperty("Service") private String Service;
	@JsonProperty("Format") private String Format;
	@JsonProperty("NetworkId") private String NetworkId;
	@JsonProperty("Target") private String Target;
	@JsonProperty("Method") private String Method;
	@JsonProperty("networkurl") private String networkurl;
	@JsonProperty("_ga") private String _ga;
	@JsonProperty("networkid") private String networkid;
	@JsonProperty("Version") private Integer Version;
	@JsonProperty("networkname") private String networkname;
	@JsonProperty("__lc_visitor_id_1040387") private String __lc_visitor_id_1040387;
}
