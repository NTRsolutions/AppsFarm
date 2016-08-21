package is.ejb.bl.offerProviders.supersonic.getOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SupersonicGetOffers {
	@JsonProperty("offers") private Map<String, OffersEntry> offers;
	@JsonProperty("offersPerPage") private Integer offersPerPage;
	@JsonProperty("offersCount") private Integer offersCount;
	@JsonProperty("currentPage") private Integer currentPage;

	public Map<String, OffersEntry> getOffers() {
		return offers;
	}
	public void setOffers(Map<String, OffersEntry> offers) {
		this.offers = offers;
	}
	public Integer getOffersPerPage() {
		return offersPerPage;
	}
	public void setOffersPerPage(Integer offersPerPage) {
		this.offersPerPage = offersPerPage;
	}
	public Integer getOffersCount() {
		return offersCount;
	}
	public void setOffersCount(Integer offersCount) {
		this.offersCount = offersCount;
	}
	public Integer getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
}
