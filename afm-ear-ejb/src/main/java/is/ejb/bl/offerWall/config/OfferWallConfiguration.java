package is.ejb.bl.offerWall.config;

import is.ejb.bl.offerWall.content.Offer;

import java.sql.Date;
import java.util.ArrayList;

public class OfferWallConfiguration implements java.io.Serializable {

	private ArrayList<SingleOfferWallConfiguration> configurations = new ArrayList<SingleOfferWallConfiguration>(); //each configuration is for single offer wall 

	public ArrayList<SingleOfferWallConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(ArrayList<SingleOfferWallConfiguration> configurations) {
		this.configurations = configurations;
	}
}

