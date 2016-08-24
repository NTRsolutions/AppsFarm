package is.web.services;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;

public class Response {

	private String status;
	private String code;

	public Response() {
	}

	public Response(RespStatusEnum status, RespCodesEnum code) {
		this.status = status.toString();
		this.code = code.toString();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStatus(RespStatusEnum status) {
		this.status = status.toString();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCode(RespCodesEnum code) {
		this.code = code.toString();
	}

	public Response getSuccessResponse() {
		this.status = "SUCCESS";
		this.code = "OK_NO_CONTENT";
		return this;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", code=" + code + "]";
	}

}
