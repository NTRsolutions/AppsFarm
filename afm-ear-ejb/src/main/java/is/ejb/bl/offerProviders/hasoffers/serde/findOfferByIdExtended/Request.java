package is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
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
	public String getFormat() {
		return Format;
	}
	public void setFormat(String format) {
		Format = format;
	}
	public String getService() {
		return Service;
	}
	public void setService(String service) {
		Service = service;
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
	public String getNetworkurl() {
		return networkurl;
	}
	public void setNetworkurl(String networkurl) {
		this.networkurl = networkurl;
	}
	public String getNetworkname() {
		return networkname;
	}
	public void setNetworkname(String networkname) {
		this.networkname = networkname;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTarget() {
		return Target;
	}
	public void setTarget(String target) {
		Target = target;
	}
	public String getNetworkId() {
		return NetworkId;
	}
	public void setNetworkId(String networkId) {
		NetworkId = networkId;
	}
	public List<String> getContain() {
		return contain;
	}
	public void setContain(List<String> contain) {
		this.contain = contain;
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
	public String get__lc_visitor_id_1040387() {
		return __lc_visitor_id_1040387;
	}
	public void set__lc_visitor_id_1040387(String __lc_visitor_id_1040387) {
		this.__lc_visitor_id_1040387 = __lc_visitor_id_1040387;
	}
	
	
}
