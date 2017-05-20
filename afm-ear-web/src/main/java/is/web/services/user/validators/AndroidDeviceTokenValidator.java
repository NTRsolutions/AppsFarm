package is.web.services.user.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class AndroidDeviceTokenValidator implements APIValidator{

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("androidDeviceToken")){
			String androidDeviceToken = (String) parameters.get("androidDeviceToken");
			if (androidDeviceToken != null && androidDeviceToken.length() > 0){
				return true;
			} else{
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_ANDROID_DEVICE_TOKEN;
	}

}
