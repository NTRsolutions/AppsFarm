package is.ejb.bl.offerProviders.hasoffersExt.getPayoutDetails;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer_payout {
	@JsonProperty("payout") private Float payout;

	public Float getPayout() {
		return payout;
	}

	public void setPayout(Float payout) {
		this.payout = payout;
	}
}
