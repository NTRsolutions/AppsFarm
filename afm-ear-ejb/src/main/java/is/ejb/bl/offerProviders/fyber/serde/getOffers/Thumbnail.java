package is.ejb.bl.offerProviders.fyber.serde.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Thumbnail {
	@JsonProperty("lowres") private String lowres;
	@JsonProperty("hires") private String hires;
	
	public String getLowres() {
		return lowres;
	}
	public void setLowres(String lowres) {
		this.lowres = lowres;
	}
	public String getHires() {
		return hires;
	}
	public void setHires(String hires) {
		this.hires = hires;
	}
	
}
