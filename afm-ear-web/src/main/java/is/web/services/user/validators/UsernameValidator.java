package is.web.services.user.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

@ManagedBean
public class UsernameValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("username")) {
			String username = (String) parameters.get("username");
			if (username == null || username.length() <= 3) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_USER_INVALID_USERNAME;
	}

}
