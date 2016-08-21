package is.ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
	@JsonProperty("Thumbnail") private Thumbnail Thumbnail;

	public Thumbnail getThumbnail() {
		return Thumbnail;
	}

	public void setThumbnail(Thumbnail thumbnail) {
		Thumbnail = thumbnail;
	}
	
}
