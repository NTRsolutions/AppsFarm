package is.ejb.bl.offerWall.content;

import java.sql.Date;
import java.util.ArrayList;

public class IndividualOfferWall implements java.io.Serializable {

	private String id;
	private String offerWallName;
	private String adProviderCodeName;
	//private long generationTimestamp = -1;
	private String generationTime;

	private ArrayList<String> listOfferStats = new ArrayList<String>();
	
	private ArrayList<Offer> offers = new ArrayList<Offer>();
	
	public ArrayList<Offer> getOffers() {
		return offers;
	}

	public void setOffers(ArrayList<Offer> offers) {
		this.offers = offers;
	}

	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}

	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}

	public String getOfferWallName() {
		return offerWallName;
	}

	public void setOfferWallName(String offerWallName) {
		this.offerWallName = offerWallName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(String generationTime) {
		this.generationTime = generationTime;
	}

	public ArrayList<String> getListOfferStats() {
		return listOfferStats;
	}

	public void setListOfferStats(ArrayList<String> listOfferStats) {
		this.listOfferStats = listOfferStats;
	}

	
}

