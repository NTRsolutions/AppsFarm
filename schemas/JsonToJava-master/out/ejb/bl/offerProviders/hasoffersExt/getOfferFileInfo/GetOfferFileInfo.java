package ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOfferFileInfo {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
