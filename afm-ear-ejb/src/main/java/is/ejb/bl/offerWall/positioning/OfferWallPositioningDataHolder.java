package is.ejb.bl.offerWall.positioning;

import java.sql.Date;
import java.util.ArrayList;

public class OfferWallPositioningDataHolder implements java.io.Serializable {

	private ArrayList<OfferPositioningEntry> listOfferPositioningEntries = new ArrayList<OfferPositioningEntry>();

	public OfferWallPositioningDataHolder(
			ArrayList<OfferPositioningEntry> listOfferPositioningEntries) {
		super();
		this.listOfferPositioningEntries = listOfferPositioningEntries;
	}

	public ArrayList<OfferPositioningEntry> getListOfferPositioningEntries() {
		return listOfferPositioningEntries;
	}

	public void setListOfferPositioningEntries(
			ArrayList<OfferPositioningEntry> listOfferPositioningEntries) {
		this.listOfferPositioningEntries = listOfferPositioningEntries;
	}
	
}

