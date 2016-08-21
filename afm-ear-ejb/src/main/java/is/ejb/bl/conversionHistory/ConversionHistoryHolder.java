package is.ejb.bl.conversionHistory;

import is.ejb.bl.offerWall.content.IndividualOfferWall;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ConversionHistoryHolder implements java.io.Serializable {

	private ArrayList<ConversionHistoryEntry> listConversionHistoryEntries = new ArrayList<ConversionHistoryEntry>();

	public ArrayList<ConversionHistoryEntry> getListConversionHistoryEntries() {
		return listConversionHistoryEntries;
	}

	public void setListConversionHistoryEntries(
			ArrayList<ConversionHistoryEntry> listConversionHistoryEntries) {
		this.listConversionHistoryEntries = listConversionHistoryEntries;
	}
	
}

