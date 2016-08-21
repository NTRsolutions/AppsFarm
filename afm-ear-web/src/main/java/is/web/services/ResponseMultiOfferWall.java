package is.web.services;

import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;

public class ResponseMultiOfferWall {
	private String status;
	private String code;
	private OfferWallContent multiOfferWall;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public OfferWallContent getMultiOfferWall() {
		return multiOfferWall;
	}
	public void setMultiOfferWall(OfferWallContent multiOfferWall) {
		this.multiOfferWall = multiOfferWall;
	}
	
}
