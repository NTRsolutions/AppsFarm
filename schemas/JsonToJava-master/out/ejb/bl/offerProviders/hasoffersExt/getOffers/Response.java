package ejb.bl.offerProviders.hasoffersExt.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Response {
	@JsonProperty("errorMessage") private Object errorMessage;
	@JsonProperty("errors") private List<Object> errors;
	@JsonProperty("httpStatus") private Integer httpStatus;
	@JsonProperty("status") private Integer status;
	@JsonProperty("data") private Map<String, DataEntry> data;
}
