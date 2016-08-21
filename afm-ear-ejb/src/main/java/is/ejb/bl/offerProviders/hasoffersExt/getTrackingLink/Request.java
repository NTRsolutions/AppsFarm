package is.ejb.bl.offerProviders.hasoffersExt.getTrackingLink;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
	@JsonProperty("Service") private String Service;
	@JsonProperty("Format") private String Format;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("NetworkId") private String NetworkId;
	@JsonProperty("Target") private String Target;
	@JsonProperty("Method") private String Method;
	@JsonProperty("_gat") private Integer _gat;
	@JsonProperty("api_key") private String api_key;
	@JsonProperty("callback") private String callback;
	@JsonProperty("_ga") private String _ga;
	@JsonProperty("Version") private Integer Version;
	@JsonProperty("__lc_visitor_id_1040387") private String __lc_visitor_id_1040387;
	public String getService() {
		return Service;
	}
	public void setService(String service) {
		Service = service;
	}
	public String getFormat() {
		return Format;
	}
	public void setFormat(String format) {
		Format = format;
	}
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public String getNetworkId() {
		return NetworkId;
	}
	public void setNetworkId(String networkId) {
		NetworkId = networkId;
	}
	public String getTarget() {
		return Target;
	}
	public void setTarget(String target) {
		Target = target;
	}
	public String getMethod() {
		return Method;
	}
	public void setMethod(String method) {
		Method = method;
	}
	public Integer get_gat() {
		return _gat;
	}
	public void set_gat(Integer _gat) {
		this._gat = _gat;
	}
	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String get_ga() {
		return _ga;
	}
	public void set_ga(String _ga) {
		this._ga = _ga;
	}
	public Integer getVersion() {
		return Version;
	}
	public void setVersion(Integer version) {
		Version = version;
	}
	public String get__lc_visitor_id_1040387() {
		return __lc_visitor_id_1040387;
	}
	public void set__lc_visitor_id_1040387(String __lc_visitor_id_1040387) {
		this.__lc_visitor_id_1040387 = __lc_visitor_id_1040387;
	}
	
	
}
