package is.ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Targeting {
	@JsonProperty("os") private List<String> os;
	@JsonProperty("countries") private List<String> countries;
	@JsonProperty("os_version") private List<Object> os_version;
	public List<String> getOs() {
		return os;
	}
	public void setOs(List<String> os) {
		this.os = os;
	}
	public List<String> getCountries() {
		return countries;
	}
	public void setCountries(List<String> countries) {
		this.countries = countries;
	}
	public List<Object> getOs_version() {
		return os_version;
	}
	public void setOs_version(List<Object> os_version) {
		this.os_version = os_version;
	}
	
	
}
