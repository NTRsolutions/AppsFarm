package ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Thumbnail {
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("account_id") private Object account_id;
	@JsonProperty("status") private String status;
	@JsonProperty("flash_vars") private Object flash_vars;
	@JsonProperty("width") private Integer width;
	@JsonProperty("display") private String display;
	@JsonProperty("preview_uri") private String preview_uri;
	@JsonProperty("code") private Object code;
	@JsonProperty("type") private String type;
	@JsonProperty("interface") private String interface;
	@JsonProperty("url") private String url;
	@JsonProperty("modified") private String modified;
	@JsonProperty("size") private Integer size;
	@JsonProperty("id") private Integer id;
	@JsonProperty("thumbnail") private String thumbnail;
	@JsonProperty("height") private Integer height;
	@JsonProperty("created") private String created;
	@JsonProperty("is_private") private Integer is_private;
	@JsonProperty("filename") private String filename;
}
