package is.web.services.wall;

import is.ejb.bl.offerWall.content.OfferWallContent;
import is.web.services.APIResponse;

public class OfferWallResponse extends APIResponse {

	private OfferWallContent multiOfferWall;

	public OfferWallContent getMultiOfferWall() {
		return multiOfferWall;
	}

	public void setMultiOfferWall(OfferWallContent multiOfferWall) {
		this.multiOfferWall = multiOfferWall;
	}
	
}
