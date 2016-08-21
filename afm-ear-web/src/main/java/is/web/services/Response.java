package is.web.services;

import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;

public class Response {
	private String status;
	private String code;
	
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
	
	public Response getSuccessResponse(){
		this.status = "SUCCESS";
		this.code = "OK_NO_CONTENT";
		return this;
	}
	@Override
	public String toString() {
		return "Response [status=" + status + ", code=" + code + "]";
	}
	
	
}
