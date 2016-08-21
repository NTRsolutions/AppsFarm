package is.iWeb.sentinel.data.dao.serde.fyber.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOffers {
	@JsonProperty("message") private String message;
	@JsonProperty("information") private Information information;
	@JsonProperty("count") private Integer count;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("pages") private Integer pages;
	@JsonProperty("code") private String code;
}
