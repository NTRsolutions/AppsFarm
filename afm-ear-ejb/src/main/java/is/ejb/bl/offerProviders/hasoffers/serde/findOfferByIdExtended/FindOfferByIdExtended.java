package is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FindOfferByIdExtended {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
	public Request getRequest() {
		return request;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	
	
}
