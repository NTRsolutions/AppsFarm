package ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class CreativeEntry {
	@JsonProperty("height") private Integer height;
	@JsonProperty("width") private Integer width;
	@JsonProperty("url") private String url;
}
