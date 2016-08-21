package ejb.bl.offerProviders.trialpay.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetOffers {
	@JsonProperty("id") private String id;
	@JsonProperty("help") private String help;
	@JsonProperty("category") private List<String> category;
	@JsonProperty("impression_url") private String impression_url;
	@JsonProperty("title") private String title;
	@JsonProperty("instructions") private String instructions;
	@JsonProperty("image_url") private String image_url;
	@JsonProperty("description") private String description;
	@JsonProperty("reward_name") private String reward_name;
	@JsonProperty("vc_amount") private Integer vc_amount;
	@JsonProperty("link") private String link;
	@JsonProperty("button_label") private String button_label;
}
