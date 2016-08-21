package is.web.services;

import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;

public class ResponseTokenValidation {
	private String status;
	private String code;
	private String validatedToken;
	private boolean tokenSuccessfullyValidated;
	
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
	public boolean isTokenSuccessfullyValidated() {
		return tokenSuccessfullyValidated;
	}
	public void setTokenSuccessfullyValidated(boolean tokenSuccessfullyValidated) {
		this.tokenSuccessfullyValidated = tokenSuccessfullyValidated;
	}
	public String getValidatedToken() {
		return validatedToken;
	}
	public void setValidatedToken(String validatedToken) {
		this.validatedToken = validatedToken;
	}

	
}
