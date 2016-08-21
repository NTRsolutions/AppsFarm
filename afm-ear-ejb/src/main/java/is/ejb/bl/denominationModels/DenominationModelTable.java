package is.ejb.bl.denominationModels;

import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.Offer;

import java.sql.Date;
import java.util.ArrayList;

public class DenominationModelTable implements java.io.Serializable {

	private ArrayList<DenominationModelRow> rows = new ArrayList<DenominationModelRow>(); //each element is the row in the table 

	public ArrayList<DenominationModelRow> getRows() {
		return rows;
	}

	public void setRows(ArrayList<DenominationModelRow> rows) {
		this.rows = rows;
	}
	
}

