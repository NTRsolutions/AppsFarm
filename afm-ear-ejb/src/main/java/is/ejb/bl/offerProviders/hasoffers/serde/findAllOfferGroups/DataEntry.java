package is.ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataEntry {
	@JsonProperty("OfferGroup") private OfferGroup OfferGroup;

	public OfferGroup getOfferGroup() {
		return OfferGroup;
	}

	public void setOfferGroup(OfferGroup offerGroup) {
		OfferGroup = offerGroup;
	}
	
	
}
