package is.ejb.bl.offerProviders.trialpay.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrialPayOffer {
	@JsonProperty("id") private String id;
	@JsonProperty("app_id") private String app_id;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHelp() {
		return help;
	}
	public void setHelp(String help) {
		this.help = help;
	}
	public List<String> getCategory() {
		return category;
	}
	public void setCategory(List<String> category) {
		this.category = category;
	}
	public String getImpression_url() {
		return impression_url;
	}
	public void setImpression_url(String impression_url) {
		this.impression_url = impression_url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getReward_name() {
		return reward_name;
	}
	public void setReward_name(String reward_name) {
		this.reward_name = reward_name;
	}
	public Integer getVc_amount() {
		return vc_amount;
	}
	public void setVc_amount(Integer vc_amount) {
		this.vc_amount = vc_amount;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getButton_label() {
		return button_label;
	}
	public void setButton_label(String button_label) {
		this.button_label = button_label;
	}
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	
	
}
