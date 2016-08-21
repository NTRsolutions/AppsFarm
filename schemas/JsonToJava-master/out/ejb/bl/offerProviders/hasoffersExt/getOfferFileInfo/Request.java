package ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Request {
	@JsonProperty("Format") private String Format;
	@JsonProperty("Service") private String Service;
	@JsonProperty("_biz_uid") private String _biz_uid;
	@JsonProperty("_biz_nA") private Integer _biz_nA;
	@JsonProperty("Method") private String Method;
	@JsonProperty("__utmz") private String __utmz;
	@JsonProperty("_biz_pendingA") private String _biz_pendingA;
	@JsonProperty("Target") private String Target;
	@JsonProperty("NetworkId") private String NetworkId;
	@JsonProperty("api_key") private String api_key;
	@JsonProperty("callback") private String callback;
	@JsonProperty("__gaTune") private String __gaTune;
	@JsonProperty("_biz_dfsA") private String _biz_dfsA;
	@JsonProperty("__utma") private String __utma;
	@JsonProperty("_ga") private String _ga;
	@JsonProperty("Version") private Integer Version;
	@JsonProperty("_hp2_id_1318563364") private String _hp2_id_1318563364;
	@JsonProperty("__lc_visitor_id_1040387") private String __lc_visitor_id_1040387;
}
