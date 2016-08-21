package is.ejb.bl.offerProviders.fyber;

public class FyberProviderConfig {

	private String apiId="";
	private String apiKey="";
	private String offerTypes=""; //e.g, 111,112
	
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getOfferTypes() {
		return offerTypes;
	}
	public void setOfferTypes(String offerTypes) {
		this.offerTypes = offerTypes;
	}
	
}
