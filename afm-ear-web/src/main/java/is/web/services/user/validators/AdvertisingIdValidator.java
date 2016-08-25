package is.web.services.user.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class AdvertisingIdValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("advertisingId")) {
			String parameter = (String) parameters.get("advertisingId");
			if (parameter == null || parameter.length() == 0) {
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
		return RespCodesEnum.ERROR_USER_INVALID_ADVERTISING_ID;
	}

}
