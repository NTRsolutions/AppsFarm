package is.ejb.bl.offerProviders.hasoffersExt.getPayoutDetails;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
	@JsonProperty("offer_payout") private Offer_payout offer_payout;

	public Offer_payout getOffer_payout() {
		return offer_payout;
	}

	public void setOffer_payout(Offer_payout offer_payout) {
		this.offer_payout = offer_payout;
	}
	
	
}
