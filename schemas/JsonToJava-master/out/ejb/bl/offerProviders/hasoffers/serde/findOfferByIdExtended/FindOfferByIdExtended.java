package ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class FindOfferByIdExtended {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
