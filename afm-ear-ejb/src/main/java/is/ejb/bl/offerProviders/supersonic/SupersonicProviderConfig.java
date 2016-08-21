package is.ejb.bl.offerProviders.supersonic;

public class SupersonicProviderConfig {
	private String accessKey = ""; 
	private String secretKey = "";
	private String applicationKey = "";
	private String platform="mobile";
	private int numberOfPulledOffers = -1; //-1 - pull all
	
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getApplicationKey() {
		return applicationKey;
	}
	public void setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getNumberOfPulledOffers() {
		return numberOfPulledOffers;
	}
	public void setNumberOfPulledOffers(int numberOfPulledOffers) {
		this.numberOfPulledOffers = numberOfPulledOffers;
	}
	
}
