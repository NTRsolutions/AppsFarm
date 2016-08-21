package is.iWeb.sentinel.data.dao.serde.fyber.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Information {
	@JsonProperty("support_url") private String support_url;
	@JsonProperty("appid") private Integer appid;
	@JsonProperty("virtual_currency") private String virtual_currency;
	@JsonProperty("language") private String language;
	@JsonProperty("app_name") private String app_name;
	@JsonProperty("country") private String country;
}
