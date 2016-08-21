package is.ejb.bl.offerWall.config;

public class SingleOfferWallConfiguration implements java.io.Serializable {

	private String name = "";
	private String adProviderConfigurationName = "";
	private String adProviderCodeName = "";
	private int numberOfOffers = 0;
	
	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}
	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}
	public int getNumberOfOffers() {
		return numberOfOffers;
	}
	public void setNumberOfOffers(int numberOfOffers) {
		this.numberOfOffers = numberOfOffers;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAdProviderConfigurationName() {
		return adProviderConfigurationName;
	}
	public void setAdProviderConfigurationName(String adProviderConfigurationName) {
		this.adProviderConfigurationName = adProviderConfigurationName;
	}
}
