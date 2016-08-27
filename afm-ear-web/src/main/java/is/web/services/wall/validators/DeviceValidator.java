package is.web.services.wall.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class DeviceValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("deviceId")) {
			String deviceId = (String) parameters.get("deviceId");
			if (deviceId!= null && deviceId.length() > 0) {
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
		return RespCodesEnum.ERROR_INVALID_DEVICE_ID;
	}

}
