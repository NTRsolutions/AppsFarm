package is.web.services.user;

import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIResponse;

public class RegisterUserResponse extends APIResponse {
	private AppUserEntity appUserEntity;

	public AppUserEntity getAppUserEntity() {
		return appUserEntity;
	}

	public void setAppUserEntity(AppUserEntity appUserEntity) {
		this.appUserEntity = appUserEntity;
	}
	
}
