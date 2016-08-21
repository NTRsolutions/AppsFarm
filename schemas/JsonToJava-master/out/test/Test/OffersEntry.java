package test.Test;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("revenueSplitValue") private Double revenueSplitValue;
	@JsonProperty("rewardCurrency") private String rewardCurrency;
	@JsonProperty("rewardValue") private Double rewardValue;
	@JsonProperty("incentivised") private Boolean incentivised;
	@JsonProperty("payoutInTargetCurrency") private Double payoutInTargetCurrency;
	@JsonProperty("image") private Map<String, String> image;
	@JsonProperty("trackingRequirements") private List<Object> trackingRequirements;
	@JsonProperty("adProviderCodeName") private String adProviderCodeName;
	@JsonProperty("internalNetworkId") private Integer internalNetworkId;
	@JsonProperty("rewardType") private String rewardType;
	@JsonProperty("url") private String url;
	@JsonProperty("sourceId") private String sourceId;
	@JsonProperty("previewUrl") private String previewUrl;
	@JsonProperty("currency") private String currency;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("supportedCountryCodes") private List<String> supportedCountryCodes;
	@JsonProperty("id") private String id;
	@JsonProperty("title") private String title;
	@JsonProperty("description") private String description;
	@JsonProperty("profitValue") private Double profitValue;
	@JsonProperty("supportedTargetDevices") private List<String> supportedTargetDevices;
	@JsonProperty("rating") private Double rating;
}
