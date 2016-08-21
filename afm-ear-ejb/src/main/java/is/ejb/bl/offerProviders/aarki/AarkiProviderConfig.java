package is.ejb.bl.offerProviders.aarki;

public class AarkiProviderConfig {
	private String placementId = ""; 
	private int numberOfPulledOffers = 0;
	
	public String getPlacementId() {
		return placementId;
	}
	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}
	public int getNumberOfPulledOffers() {
		return numberOfPulledOffers;
	}
	public void setNumberOfPulledOffers(int numberOfPulledOffers) {
		this.numberOfPulledOffers = numberOfPulledOffers;
	}
	
}
