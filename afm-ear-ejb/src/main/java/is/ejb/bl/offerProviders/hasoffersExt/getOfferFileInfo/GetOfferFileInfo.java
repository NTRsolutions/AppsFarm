package is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOfferFileInfo {
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
