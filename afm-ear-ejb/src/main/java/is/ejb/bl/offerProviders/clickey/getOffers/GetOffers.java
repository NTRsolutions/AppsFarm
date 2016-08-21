package is.ejb.bl.offerProviders.clickey.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOffers {
	@JsonProperty("count") private Integer count;
	@JsonProperty("status") private String status;
	@JsonProperty("pagecount") private Integer pagecount;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("pageindex") private Integer pageindex;
	@JsonProperty("available") private Integer available;
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getPagecount() {
		return pagecount;
	}
	public void setPagecount(Integer pagecount) {
		this.pagecount = pagecount;
	}
	public List<OffersEntry> getOffers() {
		return offers;
	}
	public void setOffers(List<OffersEntry> offers) {
		this.offers = offers;
	}
	public Integer getPageindex() {
		return pageindex;
	}
	public void setPageindex(Integer pageindex) {
		this.pageindex = pageindex;
	}
	public Integer getAvailable() {
		return available;
	}
	public void setAvailable(Integer available) {
		this.available = available;
	}
	
	
}
