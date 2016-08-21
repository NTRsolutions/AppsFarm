package is.iWeb.sentinel.data.dao.serde.hasoffers.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class FindAllOfferGroups {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
