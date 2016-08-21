package ejb.bl.offerProviders.woobi.getIOSOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class WoobiIOSGetOffers {
	@JsonProperty("totalCreditsForWeb") private Double totalCreditsForWeb;
	@JsonProperty("appId") private Integer appId;
	@JsonProperty("offersUTC") private Long offersUTC;
	@JsonProperty("offers") private List<OffersEntry> offers;
	@JsonProperty("noSession") private Boolean noSession;
	@JsonProperty("offersCount") private Integer offersCount;
	@JsonProperty("isForAccumulation") private Boolean isForAccumulation;
	@JsonProperty("getMoreOffer") private Boolean getMoreOffer;
	@JsonProperty("creditsSingular") private String creditsSingular;
	@JsonProperty("payoutCurrency") private String payoutCurrency;
	@JsonProperty("isFacebookApp") private Boolean isFacebookApp;
	@JsonProperty("creditsPlural") private String creditsPlural;
	@JsonProperty("isUnruly") private Boolean isUnruly;
	@JsonProperty("country") private String country;
}
