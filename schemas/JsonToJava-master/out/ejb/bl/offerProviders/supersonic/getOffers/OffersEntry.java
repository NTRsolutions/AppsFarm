package ejb.bl.offerProviders.supersonic.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("reviewOnlyUrl") private String reviewOnlyUrl;
	@JsonProperty("applicationSize") private Float applicationSize;
	@JsonProperty("expirationDate") private Object expirationDate;
	@JsonProperty("userFlow") private String userFlow;
	@JsonProperty("supportedPlatforms") private List<String> supportedPlatforms;
	@JsonProperty("disclaimer") private String disclaimer;
	@JsonProperty("applicationCategories") private String applicationCategories;
	@JsonProperty("rewards") private Integer rewards;
	@JsonProperty("purchase") private Boolean purchase;
	@JsonProperty("game") private Boolean game;
	@JsonProperty("countries") private List<String> countries;
	@JsonProperty("url") private String url;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("applicationDeveloper") private String applicationDeveloper;
	@JsonProperty("callToAction") private String callToAction;
	@JsonProperty("title") private String title;
	@JsonProperty("incentAllowed") private Boolean incentAllowed;
	@JsonProperty("applicationBundleId") private Integer applicationBundleId;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("deviceIds") private String deviceIds;
	@JsonProperty("images") private List<ImagesEntry> images;
	@JsonProperty("conciseType") private String conciseType;
	@JsonProperty("offerId") private Integer offerId;
}
