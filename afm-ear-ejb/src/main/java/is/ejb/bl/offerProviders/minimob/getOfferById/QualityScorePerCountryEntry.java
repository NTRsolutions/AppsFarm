package is.ejb.bl.offerProviders.minimob.getOfferById;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class QualityScorePerCountryEntry {
	@JsonProperty("countryCode") private String countryCode;
	@JsonProperty("qualityScore") private Double qualityScore;
}
