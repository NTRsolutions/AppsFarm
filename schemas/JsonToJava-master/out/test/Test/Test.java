package test.Test;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Test {
	@JsonProperty("id") private String id;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("generationTime") private String generationTime;
	@JsonProperty("adProviderCodeName") private String adProviderCodeName;
	@JsonProperty("offerWallName") private String offerWallName;
}
