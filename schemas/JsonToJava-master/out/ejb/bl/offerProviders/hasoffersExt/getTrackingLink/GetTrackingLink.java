package ejb.bl.offerProviders.hasoffersExt.getTrackingLink;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetTrackingLink {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
