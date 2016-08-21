package ejb.bl.offerProviders.hasoffersExt.getTrackingLink;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Data {
	@JsonProperty("impression_pixel") private String impression_pixel;
	@JsonProperty("click_url") private String click_url;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("affiliate_id") private Integer affiliate_id;
}
