package is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
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
	public String getNetworkToken() {
		return NetworkToken;
	}
	public void setNetworkToken(String networkToken) {
		NetworkToken = networkToken;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
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
	public String getNetworkurl() {
		return networkurl;
	}
	public void setNetworkurl(String networkurl) {
		this.networkurl = networkurl;
	}
	public String get_ga() {
		return _ga;
	}
	public void set_ga(String _ga) {
		this._ga = _ga;
	}
	public String getNetworkid() {
		return networkid;
	}
	public void setNetworkid(String networkid) {
		this.networkid = networkid;
	}
	public Integer getVersion() {
		return Version;
	}
	public void setVersion(Integer version) {
		Version = version;
	}
	public String getNetworkname() {
		return networkname;
	}
	public void setNetworkname(String networkname) {
		this.networkname = networkname;
	}
	public String get__lc_visitor_id_1040387() {
		return __lc_visitor_id_1040387;
	}
	public void set__lc_visitor_id_1040387(String __lc_visitor_id_1040387) {
		this.__lc_visitor_id_1040387 = __lc_visitor_id_1040387;
	}
	
	
}
