package is.ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer {
	@JsonProperty("show_custom_variables") private Integer show_custom_variables;
	@JsonProperty("customer_list_id") private Integer customer_list_id;
	@JsonProperty("max_percent_payout") private Object max_percent_payout;
	@JsonProperty("offer_url") private String offer_url;
	@JsonProperty("featured") private Object featured;
	@JsonProperty("disable_click_macro") private Integer disable_click_macro;
	@JsonProperty("session_impression_hours") private Integer session_impression_hours;
	@JsonProperty("enable_offer_whitelist") private Integer enable_offer_whitelist;
	@JsonProperty("description") private String description;
	@JsonProperty("dne_unsubscribe_url") private Object dne_unsubscribe_url;
	@JsonProperty("dne_download_url") private Object dne_download_url;
	@JsonProperty("show_mail_list") private Integer show_mail_list;
	@JsonProperty("allow_multiple_conversions") private Integer allow_multiple_conversions;
	@JsonProperty("email_instructions_from") private String email_instructions_from;
	@JsonProperty("status") private String status;
	@JsonProperty("dne_list_id") private Integer dne_list_id;
	@JsonProperty("terms_and_conditions") private Object terms_and_conditions;
	@JsonProperty("is_expired") private Integer is_expired;
	@JsonProperty("advertiser_id") private Integer advertiser_id;
	@JsonProperty("set_session_on_impression") private Integer set_session_on_impression;
	@JsonProperty("modified") private Integer modified;
	@JsonProperty("require_terms_and_conditions") private Integer require_terms_and_conditions;
	@JsonProperty("revenue_cap") private Float revenue_cap;
	@JsonProperty("click_macro_url") private Object click_macro_url;
	@JsonProperty("monthly_revenue_cap") private Float monthly_revenue_cap;
	@JsonProperty("has_goals_enabled") private Integer has_goals_enabled;
	@JsonProperty("subscription_duration") private Object subscription_duration;
	@JsonProperty("conversion_cap") private Integer conversion_cap;
	@JsonProperty("percent_payout") private Object percent_payout;
	@JsonProperty("default_goal_name") private String default_goal_name;
	@JsonProperty("expiration_date") private String expiration_date;
	@JsonProperty("monthly_payout_cap") private Float monthly_payout_cap;
	@JsonProperty("is_private") private Integer is_private;
	@JsonProperty("redirect_offer_id") private Integer redirect_offer_id;
	@JsonProperty("allow_direct_links") private Integer allow_direct_links;
	@JsonProperty("tiered_payout") private Integer tiered_payout;
	@JsonProperty("display_advertiser") private Integer display_advertiser;
	@JsonProperty("is_subscription") private Integer is_subscription;
	@JsonProperty("default_payout") private Float default_payout;
	@JsonProperty("monthly_conversion_cap") private Integer monthly_conversion_cap;
	@JsonProperty("preview_url") private String preview_url;
	@JsonProperty("target_browsers") private Integer target_browsers;
	@JsonProperty("enforce_geo_targeting") private Integer enforce_geo_targeting;
	@JsonProperty("enforce_encrypt_tracking_pixels") private Integer enforce_encrypt_tracking_pixels;
	@JsonProperty("currency") private String currency;
	@JsonProperty("session_hours") private Integer session_hours;
	@JsonProperty("id") private Integer id;
	@JsonProperty("tiered_revenue") private Integer tiered_revenue;
	@JsonProperty("converted_offer_url") private Object converted_offer_url;
	@JsonProperty("name") private String name;
	@JsonProperty("use_revenue_groups") private Integer use_revenue_groups;
	@JsonProperty("email_instructions_subject") private String email_instructions_subject;
	@JsonProperty("revenue_type") private String revenue_type;
	@JsonProperty("is_seo_friendly_301") private Integer is_seo_friendly_301;
	@JsonProperty("payout_cap") private Float payout_cap;
	@JsonProperty("note") private String note;
	@JsonProperty("converted_offer_type") private Object converted_offer_type;
	@JsonProperty("protocol") private String protocol;
	@JsonProperty("hostname_id") private Object hostname_id;
	@JsonProperty("require_approval") private Integer require_approval;
	@JsonProperty("subscription_frequency") private Object subscription_frequency;
	@JsonProperty("email_instructions") private Integer email_instructions;
	@JsonProperty("use_target_rules") private Integer use_target_rules;
	@JsonProperty("max_payout") private Float max_payout;
	@JsonProperty("converted_offer_id") private Object converted_offer_id;
	@JsonProperty("ref_id") private Integer ref_id;
	@JsonProperty("allow_website_links") private Integer allow_website_links;
	@JsonProperty("rating") private Integer rating;
	@JsonProperty("conversion_ratio_threshold") private Object conversion_ratio_threshold;
	@JsonProperty("approve_conversions") private Integer approve_conversions;
	@JsonProperty("use_payout_groups") private Integer use_payout_groups;
	@JsonProperty("payout_type") private String payout_type;
	public Integer getShow_custom_variables() {
		return show_custom_variables;
	}
	public void setShow_custom_variables(Integer show_custom_variables) {
		this.show_custom_variables = show_custom_variables;
	}
	public Integer getCustomer_list_id() {
		return customer_list_id;
	}
	public void setCustomer_list_id(Integer customer_list_id) {
		this.customer_list_id = customer_list_id;
	}
	public Object getMax_percent_payout() {
		return max_percent_payout;
	}
	public void setMax_percent_payout(Object max_percent_payout) {
		this.max_percent_payout = max_percent_payout;
	}
	public String getOffer_url() {
		return offer_url;
	}
	public void setOffer_url(String offer_url) {
		this.offer_url = offer_url;
	}
	public Object getFeatured() {
		return featured;
	}
	public void setFeatured(Object featured) {
		this.featured = featured;
	}
	public Integer getDisable_click_macro() {
		return disable_click_macro;
	}
	public void setDisable_click_macro(Integer disable_click_macro) {
		this.disable_click_macro = disable_click_macro;
	}
	public Integer getSession_impression_hours() {
		return session_impression_hours;
	}
	public void setSession_impression_hours(Integer session_impression_hours) {
		this.session_impression_hours = session_impression_hours;
	}
	public Integer getEnable_offer_whitelist() {
		return enable_offer_whitelist;
	}
	public void setEnable_offer_whitelist(Integer enable_offer_whitelist) {
		this.enable_offer_whitelist = enable_offer_whitelist;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Object getDne_unsubscribe_url() {
		return dne_unsubscribe_url;
	}
	public void setDne_unsubscribe_url(Object dne_unsubscribe_url) {
		this.dne_unsubscribe_url = dne_unsubscribe_url;
	}
	public Object getDne_download_url() {
		return dne_download_url;
	}
	public void setDne_download_url(Object dne_download_url) {
		this.dne_download_url = dne_download_url;
	}
	public Integer getShow_mail_list() {
		return show_mail_list;
	}
	public void setShow_mail_list(Integer show_mail_list) {
		this.show_mail_list = show_mail_list;
	}
	public Integer getAllow_multiple_conversions() {
		return allow_multiple_conversions;
	}
	public void setAllow_multiple_conversions(Integer allow_multiple_conversions) {
		this.allow_multiple_conversions = allow_multiple_conversions;
	}
	public String getEmail_instructions_from() {
		return email_instructions_from;
	}
	public void setEmail_instructions_from(String email_instructions_from) {
		this.email_instructions_from = email_instructions_from;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getDne_list_id() {
		return dne_list_id;
	}
	public void setDne_list_id(Integer dne_list_id) {
		this.dne_list_id = dne_list_id;
	}
	public Object getTerms_and_conditions() {
		return terms_and_conditions;
	}
	public void setTerms_and_conditions(Object terms_and_conditions) {
		this.terms_and_conditions = terms_and_conditions;
	}
	public Integer getIs_expired() {
		return is_expired;
	}
	public void setIs_expired(Integer is_expired) {
		this.is_expired = is_expired;
	}
	public Integer getAdvertiser_id() {
		return advertiser_id;
	}
	public void setAdvertiser_id(Integer advertiser_id) {
		this.advertiser_id = advertiser_id;
	}
	public Integer getSet_session_on_impression() {
		return set_session_on_impression;
	}
	public void setSet_session_on_impression(Integer set_session_on_impression) {
		this.set_session_on_impression = set_session_on_impression;
	}
	public Integer getModified() {
		return modified;
	}
	public void setModified(Integer modified) {
		this.modified = modified;
	}
	public Integer getRequire_terms_and_conditions() {
		return require_terms_and_conditions;
	}
	public void setRequire_terms_and_conditions(Integer require_terms_and_conditions) {
		this.require_terms_and_conditions = require_terms_and_conditions;
	}
	public Float getRevenue_cap() {
		return revenue_cap;
	}
	public void setRevenue_cap(Float revenue_cap) {
		this.revenue_cap = revenue_cap;
	}
	public Object getClick_macro_url() {
		return click_macro_url;
	}
	public void setClick_macro_url(Object click_macro_url) {
		this.click_macro_url = click_macro_url;
	}
	public Float getMonthly_revenue_cap() {
		return monthly_revenue_cap;
	}
	public void setMonthly_revenue_cap(Float monthly_revenue_cap) {
		this.monthly_revenue_cap = monthly_revenue_cap;
	}
	public Integer getHas_goals_enabled() {
		return has_goals_enabled;
	}
	public void setHas_goals_enabled(Integer has_goals_enabled) {
		this.has_goals_enabled = has_goals_enabled;
	}
	public Object getSubscription_duration() {
		return subscription_duration;
	}
	public void setSubscription_duration(Object subscription_duration) {
		this.subscription_duration = subscription_duration;
	}
	public Integer getConversion_cap() {
		return conversion_cap;
	}
	public void setConversion_cap(Integer conversion_cap) {
		this.conversion_cap = conversion_cap;
	}
	public Object getPercent_payout() {
		return percent_payout;
	}
	public void setPercent_payout(Object percent_payout) {
		this.percent_payout = percent_payout;
	}
	public String getDefault_goal_name() {
		return default_goal_name;
	}
	public void setDefault_goal_name(String default_goal_name) {
		this.default_goal_name = default_goal_name;
	}
	public String getExpiration_date() {
		return expiration_date;
	}
	public void setExpiration_date(String expiration_date) {
		this.expiration_date = expiration_date;
	}
	public Float getMonthly_payout_cap() {
		return monthly_payout_cap;
	}
	public void setMonthly_payout_cap(Float monthly_payout_cap) {
		this.monthly_payout_cap = monthly_payout_cap;
	}
	public Integer getIs_private() {
		return is_private;
	}
	public void setIs_private(Integer is_private) {
		this.is_private = is_private;
	}
	public Integer getRedirect_offer_id() {
		return redirect_offer_id;
	}
	public void setRedirect_offer_id(Integer redirect_offer_id) {
		this.redirect_offer_id = redirect_offer_id;
	}
	public Integer getAllow_direct_links() {
		return allow_direct_links;
	}
	public void setAllow_direct_links(Integer allow_direct_links) {
		this.allow_direct_links = allow_direct_links;
	}
	public Integer getTiered_payout() {
		return tiered_payout;
	}
	public void setTiered_payout(Integer tiered_payout) {
		this.tiered_payout = tiered_payout;
	}
	public Integer getDisplay_advertiser() {
		return display_advertiser;
	}
	public void setDisplay_advertiser(Integer display_advertiser) {
		this.display_advertiser = display_advertiser;
	}
	public Integer getIs_subscription() {
		return is_subscription;
	}
	public void setIs_subscription(Integer is_subscription) {
		this.is_subscription = is_subscription;
	}
	public Float getDefault_payout() {
		return default_payout;
	}
	public void setDefault_payout(Float default_payout) {
		this.default_payout = default_payout;
	}
	public Integer getMonthly_conversion_cap() {
		return monthly_conversion_cap;
	}
	public void setMonthly_conversion_cap(Integer monthly_conversion_cap) {
		this.monthly_conversion_cap = monthly_conversion_cap;
	}
	public String getPreview_url() {
		return preview_url;
	}
	public void setPreview_url(String preview_url) {
		this.preview_url = preview_url;
	}
	public Integer getTarget_browsers() {
		return target_browsers;
	}
	public void setTarget_browsers(Integer target_browsers) {
		this.target_browsers = target_browsers;
	}
	public Integer getEnforce_geo_targeting() {
		return enforce_geo_targeting;
	}
	public void setEnforce_geo_targeting(Integer enforce_geo_targeting) {
		this.enforce_geo_targeting = enforce_geo_targeting;
	}
	public Integer getEnforce_encrypt_tracking_pixels() {
		return enforce_encrypt_tracking_pixels;
	}
	public void setEnforce_encrypt_tracking_pixels(
			Integer enforce_encrypt_tracking_pixels) {
		this.enforce_encrypt_tracking_pixels = enforce_encrypt_tracking_pixels;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Integer getSession_hours() {
		return session_hours;
	}
	public void setSession_hours(Integer session_hours) {
		this.session_hours = session_hours;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTiered_revenue() {
		return tiered_revenue;
	}
	public void setTiered_revenue(Integer tiered_revenue) {
		this.tiered_revenue = tiered_revenue;
	}
	public Object getConverted_offer_url() {
		return converted_offer_url;
	}
	public void setConverted_offer_url(Object converted_offer_url) {
		this.converted_offer_url = converted_offer_url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getUse_revenue_groups() {
		return use_revenue_groups;
	}
	public void setUse_revenue_groups(Integer use_revenue_groups) {
		this.use_revenue_groups = use_revenue_groups;
	}
	public String getEmail_instructions_subject() {
		return email_instructions_subject;
	}
	public void setEmail_instructions_subject(String email_instructions_subject) {
		this.email_instructions_subject = email_instructions_subject;
	}
	public String getRevenue_type() {
		return revenue_type;
	}
	public void setRevenue_type(String revenue_type) {
		this.revenue_type = revenue_type;
	}
	public Integer getIs_seo_friendly_301() {
		return is_seo_friendly_301;
	}
	public void setIs_seo_friendly_301(Integer is_seo_friendly_301) {
		this.is_seo_friendly_301 = is_seo_friendly_301;
	}
	public Float getPayout_cap() {
		return payout_cap;
	}
	public void setPayout_cap(Float payout_cap) {
		this.payout_cap = payout_cap;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Object getConverted_offer_type() {
		return converted_offer_type;
	}
	public void setConverted_offer_type(Object converted_offer_type) {
		this.converted_offer_type = converted_offer_type;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public Object getHostname_id() {
		return hostname_id;
	}
	public void setHostname_id(Object hostname_id) {
		this.hostname_id = hostname_id;
	}
	public Integer getRequire_approval() {
		return require_approval;
	}
	public void setRequire_approval(Integer require_approval) {
		this.require_approval = require_approval;
	}
	public Object getSubscription_frequency() {
		return subscription_frequency;
	}
	public void setSubscription_frequency(Object subscription_frequency) {
		this.subscription_frequency = subscription_frequency;
	}
	public Integer getEmail_instructions() {
		return email_instructions;
	}
	public void setEmail_instructions(Integer email_instructions) {
		this.email_instructions = email_instructions;
	}
	public Integer getUse_target_rules() {
		return use_target_rules;
	}
	public void setUse_target_rules(Integer use_target_rules) {
		this.use_target_rules = use_target_rules;
	}
	public Float getMax_payout() {
		return max_payout;
	}
	public void setMax_payout(Float max_payout) {
		this.max_payout = max_payout;
	}
	public Object getConverted_offer_id() {
		return converted_offer_id;
	}
	public void setConverted_offer_id(Object converted_offer_id) {
		this.converted_offer_id = converted_offer_id;
	}
	public Integer getRef_id() {
		return ref_id;
	}
	public void setRef_id(Integer ref_id) {
		this.ref_id = ref_id;
	}
	public Integer getAllow_website_links() {
		return allow_website_links;
	}
	public void setAllow_website_links(Integer allow_website_links) {
		this.allow_website_links = allow_website_links;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Object getConversion_ratio_threshold() {
		return conversion_ratio_threshold;
	}
	public void setConversion_ratio_threshold(Object conversion_ratio_threshold) {
		this.conversion_ratio_threshold = conversion_ratio_threshold;
	}
	public Integer getApprove_conversions() {
		return approve_conversions;
	}
	public void setApprove_conversions(Integer approve_conversions) {
		this.approve_conversions = approve_conversions;
	}
	public Integer getUse_payout_groups() {
		return use_payout_groups;
	}
	public void setUse_payout_groups(Integer use_payout_groups) {
		this.use_payout_groups = use_payout_groups;
	}
	public String getPayout_type() {
		return payout_type;
	}
	public void setPayout_type(String payout_type) {
		this.payout_type = payout_type;
	}
	
	
}
