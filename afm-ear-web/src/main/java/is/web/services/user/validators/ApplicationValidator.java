package is.web.services.user.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class ApplicationValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("applicationName")) {
			String applicationName = (String) parameters.get("applicationName");
			if (applicationName != null && applicationName.length() > 0 && validateApplication(applicationName)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean validateApplication(String applicationName) {
		if (applicationName.equals("AppsFarm")) {
			return true;
		}
		return false;
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_APPLICATION_NAME;
	}

}
