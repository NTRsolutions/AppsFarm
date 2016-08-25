package is.web.services.user;

import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIResponse;

public class RegisterUserResponse extends APIResponse {
	private AppUserEntity registeredAppUserEntity;

	public AppUserEntity getRegisteredAppUserEntity() {
		return registeredAppUserEntity;
	}

	public void setRegisteredAppUserEntity(AppUserEntity registeredAppUserEntity) {
		this.registeredAppUserEntity = registeredAppUserEntity;
	}

	
	
}
