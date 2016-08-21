package is.ejb.bl.offerWall.content;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class OfferWallContent implements java.io.Serializable {

	private String id;
	private String compositeOfferWallName;
	private long generationTimestamp = -1;
	private String generationTime;
	private ArrayList<IndividualOfferWall> offerWalls = new ArrayList<IndividualOfferWall>();
	
	public ArrayList<IndividualOfferWall> getOfferWalls() {
		return offerWalls;
	}

	public void setOfferWalls(ArrayList<IndividualOfferWall> offerWalls) {
		this.offerWalls = offerWalls;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompositeOfferWallName() {
		return compositeOfferWallName;
	}

	public void setCompositeOfferWallName(String compositeOfferWallName) {
		this.compositeOfferWallName = compositeOfferWallName;
	}

	public long getGenerationTimestamp() {
		return generationTimestamp;
	}

	public void setGenerationTimestamp(long generationTimestamp) {
		this.generationTimestamp = generationTimestamp;
	}

	public String getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(String generationTime) {
		this.generationTime = generationTime;
	}
	
}

