package is.ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
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
	public List<TagsEntry> getTags() {
		return tags;
	}
	public void setTags(List<TagsEntry> tags) {
		this.tags = tags;
	}
	public Double getPayment() {
		return payment;
	}
	public void setPayment(Double payment) {
		this.payment = payment;
	}
	public Integer getFeatured() {
		return featured;
	}
	public void setFeatured(Integer featured) {
		this.featured = featured;
	}
	public List<PlatformsEntry> getPlatforms() {
		return platforms;
	}
	public void setPlatforms(List<PlatformsEntry> platforms) {
		this.platforms = platforms;
	}
	public List<CountriesEntry> getCountries() {
		return countries;
	}
	public void setCountries(List<CountriesEntry> countries) {
		this.countries = countries;
	}
	public String getMobile_guidelines() {
		return mobile_guidelines;
	}
	public void setMobile_guidelines(String mobile_guidelines) {
		this.mobile_guidelines = mobile_guidelines;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getHas_multiple_lead() {
		return has_multiple_lead;
	}
	public void setHas_multiple_lead(Integer has_multiple_lead) {
		this.has_multiple_lead = has_multiple_lead;
	}
	public String getGuidelines() {
		return guidelines;
	}
	public void setGuidelines(String guidelines) {
		this.guidelines = guidelines;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Store_app_idsEntry> getStore_app_ids() {
		return store_app_ids;
	}
	public void setStore_app_ids(List<Store_app_idsEntry> store_app_ids) {
		this.store_app_ids = store_app_ids;
	}
	public Integer getIs_daily() {
		return is_daily;
	}
	public void setIs_daily(Integer is_daily) {
		this.is_daily = is_daily;
	}
	public List<BannersEntry> getBanners() {
		return banners;
	}
	public void setBanners(List<BannersEntry> banners) {
		this.banners = banners;
	}
	
	
}
