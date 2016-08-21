package is.ejb.bl.offerWall.positioning;

import is.ejb.bl.business.OfferType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class OfferPositioningEntry  implements java.io.Serializable {
	
	private String offerTitle;
	private String offerType;
	private boolean offerPosioned = false;
	
	public OfferPositioningEntry(String offerTitle, String offerType,
			boolean offerPosioned) {
		super();
		this.offerTitle = offerTitle;
		this.offerType = offerType;
		this.offerPosioned = offerPosioned;
	}
	public String getOfferTitle() {
		return offerTitle;
	}
	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}
	public String getOfferType() {
		return offerType;
	}
	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}
	public boolean isOfferPosioned() {
		return offerPosioned;
	}
	public void setOfferPosioned(boolean offerPosioned) {
		this.offerPosioned = offerPosioned;
	}
	
	
	
}
