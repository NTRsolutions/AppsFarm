package is.ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class DataEntry {
	@JsonProperty("id") private Integer id;
	@JsonProperty("rule_id") private Integer rule_id;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("rule") private Rule rule;
	@JsonProperty("action") private String action;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getRule_id() {
		return rule_id;
	}
	public void setRule_id(Integer rule_id) {
		this.rule_id = rule_id;
	}
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public Rule getRule() {
		return rule;
	}
	public void setRule(Rule rule) {
		this.rule = rule;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
