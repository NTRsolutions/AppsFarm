package is.ejb.bl.offerProviders.minimob;

public class MinimobProviderConfig {
	//private String authKey; //key used for authentication when requesting data from external system - this should be realm and offer wall related and unique!
	private String apiKey = ""; //url provided from Mocean that generates Ad output
	private long serviceQueryInterval; //in ms

	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public long getServiceQueryInterval() {
		return serviceQueryInterval;
	}
	public void setServiceQueryInterval(long serviceQueryInterval) {
		this.serviceQueryInterval = serviceQueryInterval;
	}
	
}
