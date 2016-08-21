package is.web.services;

import is.ejb.bl.uiStateManager.UIStateHolder;
import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;

public class ResponseLogin {
	private String status;
	private String code;
	private AppUserEntity userData;
	private UIStateHolder uiStateHolder;
	
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
	public AppUserEntity getUserData() {
		return userData;
	}
	public void setUserData(AppUserEntity userData) {
		this.userData = userData;
	}
	public UIStateHolder getUiStateHolder() {
		return uiStateHolder;
	}
	public void setUiStateHolder(UIStateHolder uiStateHolder) {
		this.uiStateHolder = uiStateHolder;
	}
	
}
