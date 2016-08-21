package is.iWeb.sentinel.data.dao.serde.hasoffers.findOfferById;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class FindOfferById {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
