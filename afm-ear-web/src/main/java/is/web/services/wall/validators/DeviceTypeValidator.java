package is.web.services.wall.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class DeviceTypeValidator implements APIValidator {

	
	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("deviceType")){
			String deviceType = (String) parameters.get("deviceType");
			if (deviceType.equals("Android") || deviceType.equals("iOS")){
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
		return RespCodesEnum.ERROR_INVALID_DEVICE_TYPE;
	}

}
