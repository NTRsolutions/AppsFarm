package is.web.services;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;

public class APIResponse {
	
	private RespStatusEnum status;
	private RespCodesEnum code;
	public RespStatusEnum getStatus() {
		return status;
	}
	public void setStatus(RespStatusEnum status) {
		this.status = status;
	}
	public RespCodesEnum getCode() {
		return code;
	}
	public void setCode(RespCodesEnum code) {
		this.code = code;
	}
	
	
	
}
