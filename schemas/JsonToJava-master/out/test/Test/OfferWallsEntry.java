package test.Test;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OfferWallsEntry {
	@JsonProperty("id") private String id;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("generationTime") private String generationTime;
	@JsonProperty("offerWallName") private String offerWallName;
	@JsonProperty("adProviderCodeName") private String adProviderCodeName;
}
