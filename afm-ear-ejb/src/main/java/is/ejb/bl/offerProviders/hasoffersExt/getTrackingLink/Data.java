package is.ejb.bl.offerProviders.hasoffersExt.getTrackingLink;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
	@JsonProperty("impression_pixel") private String impression_pixel;
	@JsonProperty("click_url") private String click_url;
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("affiliate_id") private Integer affiliate_id;
	public String getImpression_pixel() {
		return impression_pixel;
	}
	public void setImpression_pixel(String impression_pixel) {
		this.impression_pixel = impression_pixel;
	}
	public String getClick_url() {
		return click_url;
	}
	public void setClick_url(String click_url) {
		this.click_url = click_url;
	}
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public Integer getAffiliate_id() {
		return affiliate_id;
	}
	public void setAffiliate_id(Integer affiliate_id) {
		this.affiliate_id = affiliate_id;
	}
	
	
}
