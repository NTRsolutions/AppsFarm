package ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("tags") private List<TagsEntry> tags;
	@JsonProperty("payment") private Double payment;
	@JsonProperty("featured") private Integer featured;
	@JsonProperty("platforms") private List<PlatformsEntry> platforms;
	@JsonProperty("countries") private List<CountriesEntry> countries;
	@JsonProperty("mobile_guidelines") private String mobile_guidelines;
	@JsonProperty("url") private String url;
	@JsonProperty("id") private Integer id;
	@JsonProperty("has_multiple_lead") private Integer has_multiple_lead;
	@JsonProperty("guidelines") private String guidelines;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("store_app_ids") private List<Store_app_idsEntry> store_app_ids;
	@JsonProperty("is_daily") private Integer is_daily;
	@JsonProperty("banners") private List<BannersEntry> banners;
}
