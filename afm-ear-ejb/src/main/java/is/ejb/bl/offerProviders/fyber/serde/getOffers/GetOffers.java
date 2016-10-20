package is.ejb.bl.offerProviders.fyber.serde.getOffers;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOffers {
	@JsonProperty("message") private String message;
	@JsonProperty("information") private Information information;
	@JsonProperty("count") private Integer count;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("pages") private Integer pages;
	@JsonProperty("code") private String code;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Information getInformation() {
		return information;
	}
	public void setInformation(Information information) {
		this.information = information;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List<OffersEntry> getOffers() {
		return offers;
	}
	public void setOffers(List<OffersEntry> offers) {
		this.offers = offers;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
}
