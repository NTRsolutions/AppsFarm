package is.ejb.bl.offerProviders.trialpay;

public class TrialPayProviderConfig {
	private String vic = ""; 
	private int numberOfPulledOffers = 0;
	
	public String getVic() {
		return vic;
	}
	public void setVic(String vic) {
		this.vic = vic;
	}
	public int getNumberOfPulledOffers() {
		return numberOfPulledOffers;
	}
	public void setNumberOfPulledOffers(int numberOfPulledOffers) {
		this.numberOfPulledOffers = numberOfPulledOffers;
	}
	
}
