package is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class DataEntry {
	@JsonProperty("OfferFile") private OfferFile OfferFile;

	public OfferFile getOfferFile() {
		return OfferFile;
	}

	public void setOfferFile(OfferFile offerFile) {
		OfferFile = offerFile;
	}
	
	
}
