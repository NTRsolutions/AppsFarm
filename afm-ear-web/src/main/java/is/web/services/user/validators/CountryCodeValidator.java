package is.web.services.user.validators;

import java.util.HashMap;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

public class CountryCodeValidator implements APIValidator {

	

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("countryCode")) {
			String parameter = (String) parameters.get("countryCode");
			if (parameter.equals("GB") || parameter.equals("US")){
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
		return RespCodesEnum.ERROR_USER_INVALID_COUNTRY;
	}


}
