package ejb.bl.offerProviders.supersonic.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class ImagesEntry {
	@JsonProperty("height") private Integer height;
	@JsonProperty("width") private Integer width;
	@JsonProperty("url") private String url;
}
