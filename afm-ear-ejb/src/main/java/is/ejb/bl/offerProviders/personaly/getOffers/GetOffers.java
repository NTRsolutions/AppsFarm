package is.ejb.bl.offerProviders.personaly.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOffers {
	@JsonProperty("information") private Information information;
	@JsonProperty("pages") private Integer pages;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("code_message") private String code_message;
	@JsonProperty("offer_count") private Integer offer_count;
	@JsonProperty("code") private Integer code;
	public Information getInformation() {
		return information;
	}
	public void setInformation(Information information) {
		this.information = information;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public List<OffersEntry> getOffers() {
		return offers;
	}
	public void setOffers(List<OffersEntry> offers) {
		this.offers = offers;
	}
	public String getCode_message() {
		return code_message;
	}
	public void setCode_message(String code_message) {
		this.code_message = code_message;
	}
	public Integer getOffer_count() {
		return offer_count;
	}
	public void setOffer_count(Integer offer_count) {
		this.offer_count = offer_count;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	
	
}
