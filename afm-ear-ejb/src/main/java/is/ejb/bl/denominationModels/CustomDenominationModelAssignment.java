package is.ejb.bl.denominationModels;

import java.sql.Timestamp;

public class CustomDenominationModelAssignment {
	private String offerId;
	private String offerSourceId;
	private String title;
	private String adProviderCodeName;
	
	private int denominationModelId;
	private String denominationModelName;
	
	private Timestamp timestamp; 
 
	private String rowKey;

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getOfferSourceId() {
		return offerSourceId;
	}

	public void setOfferSourceId(String offerSourceId) {
		this.offerSourceId = offerSourceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}

	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}

	public int getDenominationModelId() {
		return denominationModelId;
	}

	public void setDenominationModelId(int denominationModelId) {
		this.denominationModelId = denominationModelId;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getRowKey() {
		return rowKey;
	}

	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}

	public String getDenominationModelName() {
		return denominationModelName;
	}

	public void setDenominationModelName(String denominationModelName) {
		this.denominationModelName = denominationModelName;
	}

	
}
