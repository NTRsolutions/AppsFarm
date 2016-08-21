package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Information {
	@JsonProperty("virtual_currency") private String virtual_currency;
	@JsonProperty("app_id") private Integer app_id;
	@JsonProperty("app_name") private String app_name;
}
