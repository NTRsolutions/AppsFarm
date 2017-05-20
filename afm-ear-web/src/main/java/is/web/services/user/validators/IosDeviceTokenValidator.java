package is.web.services.user.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class IosDeviceTokenValidator implements APIValidator{
	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("iosDeviceToken")){
			String iosDeviceToken = (String) parameters.get("iosDeviceToken");
			if (iosDeviceToken != null && iosDeviceToken.length() > 0){
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
		return RespCodesEnum.ERROR_INVALID_IOS_DEVICE_TOKEN;
	}
}
