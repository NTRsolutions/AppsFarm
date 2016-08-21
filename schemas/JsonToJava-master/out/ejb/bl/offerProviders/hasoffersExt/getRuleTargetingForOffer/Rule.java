package ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Rule {
	@JsonProperty("req_device_brand") private Object req_device_brand;
	@JsonProperty("req_device_marketing_name") private Object req_device_marketing_name;
	@JsonProperty("req_language") private Object req_language;
	@JsonProperty("req_device_model") private Object req_device_model;
	@JsonProperty("req_affiliate_info5") private Object req_affiliate_info5;
	@JsonProperty("id") private Integer id;
	@JsonProperty("category") private String category;
	@JsonProperty("req_device_os") private String req_device_os;
	@JsonProperty("req_affiliate_info1") private Object req_affiliate_info1;
	@JsonProperty("source") private String source;
	@JsonProperty("req_affiliate_info2") private Object req_affiliate_info2;
	@JsonProperty("req_user_agent") private Object req_user_agent;
	@JsonProperty("req_affiliate_info3") private Object req_affiliate_info3;
	@JsonProperty("description") private String description;
	@JsonProperty("req_affiliate_info4") private Object req_affiliate_info4;
	@JsonProperty("name") private String name;
	@JsonProperty("req_mobile_carrier") private Object req_mobile_carrier;
	@JsonProperty("req_browser_version") private Object req_browser_version;
	@JsonProperty("req_device_os_version") private Object req_device_os_version;
	@JsonProperty("req_browser_name") private Object req_browser_name;
	@JsonProperty("req_connection_speed") private Object req_connection_speed;
}
