package ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("caps_total") private String caps_total;
	@JsonProperty("icon") private String icon;
	@JsonProperty("offer_model") private String offer_model;
	@JsonProperty("targeting") private Targeting targeting;
	@JsonProperty("caps_daily_remaining") private String caps_daily_remaining;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("creative") private List<CreativeEntry> creative;
	@JsonProperty("caps_total_remaining") private String caps_total_remaining;
	@JsonProperty("instructions") private String instructions;
	@JsonProperty("link") private String link;
	@JsonProperty("type") private String type;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("offer_type") private String offer_type;
	@JsonProperty("free") private Boolean free;
	@JsonProperty("category") private List<String> category;
	@JsonProperty("caps_daily") private String caps_daily;
	@JsonProperty("description") private String description;
	@JsonProperty("name") private String name;
	@JsonProperty("expiration_date") private Object expiration_date;
	@JsonProperty("traffic_type") private String traffic_type;
	@JsonProperty("app_id") private String app_id;
	@JsonProperty("description_lang") private Object description_lang;
}
