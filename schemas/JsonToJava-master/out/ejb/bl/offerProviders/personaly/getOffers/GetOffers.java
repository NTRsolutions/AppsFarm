package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOffers {
	@JsonProperty("information") private Information information;
	@JsonProperty("pages") private Integer pages;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("code_message") private String code_message;
	@JsonProperty("offer_count") private Integer offer_count;
	@JsonProperty("code") private Integer code;
}
