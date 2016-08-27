package is.web.services.wall.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class AdvertisingValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("advertisingId")) {
			String advertisingId = (String) parameters.get("advertisingId");
			if (advertisingId!= null && advertisingId.length() > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_ADVERTISING_ID;
	}

}
