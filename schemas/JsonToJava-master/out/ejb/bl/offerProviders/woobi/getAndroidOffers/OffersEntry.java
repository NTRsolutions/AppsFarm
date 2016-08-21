package ejb.bl.offerProviders.woobi.getAndroidOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OffersEntry {
	@JsonProperty("conversionType") private Integer conversionType;
	@JsonProperty("appSize") private Double appSize;
	@JsonProperty("artworkLong") private String artworkLong;
	@JsonProperty("appId") private String appId;
	@JsonProperty("appPublisher") private String appPublisher;
	@JsonProperty("credits") private Double credits;
	@JsonProperty("artworkWide") private String artworkWide;
	@JsonProperty("cpnId") private Integer cpnId;
	@JsonProperty("payout") private Double payout;
	@JsonProperty("appRanking") private Float appRanking;
	@JsonProperty("title") private String title;
	@JsonProperty("adId") private Integer adId;
	@JsonProperty("deviceType") private String deviceType;
	@JsonProperty("appNumOfInstalls") private String appNumOfInstalls;
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
	@JsonProperty("cpnName") private String cpnName;
	@JsonProperty("payoutType") private String payoutType;
}
