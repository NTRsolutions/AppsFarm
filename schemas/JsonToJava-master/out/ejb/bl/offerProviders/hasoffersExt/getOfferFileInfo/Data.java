package ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Data {
	@JsonProperty("pageCount") private Integer pageCount;
	@JsonProperty("count") private Integer count;
	@JsonProperty("page") private Integer page;
	@JsonProperty("data") private List<DataEntry> data;
	@JsonProperty("current") private Integer current;
}
