package ejb.bl.offerProviders.woobi.getIOSOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("conversionType") private Integer conversionType;
	@JsonProperty("appSize") private Double appSize;
	@JsonProperty("artworkLong") private String artworkLong;
	@JsonProperty("appId") private Integer appId;
	@JsonProperty("appPublisher") private String appPublisher;
	@JsonProperty("credits") private Double credits;
	@JsonProperty("artworkWide") private String artworkWide;
	@JsonProperty("cpnId") private Integer cpnId;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("appRanking") private Integer appRanking;
	@JsonProperty("title") private String title;
	@JsonProperty("adId") private Integer adId;
	@JsonProperty("description") private String description;
	@JsonProperty("deviceType") private String deviceType;
	@JsonProperty("incent") private String incent;
	@JsonProperty("adType") private Integer adType;
	@JsonProperty("artworkSqr") private String artworkSqr;
	@JsonProperty("isForAccumulation") private Boolean isForAccumulation;
	@JsonProperty("appType") private Integer appType;
	@JsonProperty("clickURL") private String clickURL;
	@JsonProperty("priceTerm") private String priceTerm;
	@JsonProperty("priceCurrency") private String priceCurrency;
	@JsonProperty("appDomain") private String appDomain;
	@JsonProperty("geoCode") private List<String> geoCode;
	@JsonProperty("thumbnail") private String thumbnail;
	@JsonProperty("subtitle") private String subtitle;
	@JsonProperty("artworkIcon") private String artworkIcon;
	@JsonProperty("language") private String language;
	@JsonProperty("accumPoints") private Double accumPoints;
	@JsonProperty("cpnName") private String cpnName;
	@JsonProperty("payoutType") private String payoutType;
}
