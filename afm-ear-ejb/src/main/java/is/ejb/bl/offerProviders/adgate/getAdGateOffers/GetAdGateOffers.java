package is.ejb.bl.offerProviders.adgate.getAdGateOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAdGateOffers {
	@JsonProperty("offers") private List<OffersEntry> offers;

	public List<OffersEntry> getOffers() {
		return offers;
	}

	public void setOffers(List<OffersEntry> offers) {
		this.offers = offers;
	}
}
