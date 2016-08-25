package is.web.services;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;

public interface APIValidator {
	public boolean validate(HashMap<String,Object> parameters);
	public RespCodesEnum getInvalidValueErrorCode();
}
