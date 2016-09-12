package is.web.services.personalDetails.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class StreetValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("street")){
			String street = (String) parameters.get("street");
			if (street != null && street.length() > 0){
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
		return RespCodesEnum.ERROR_INVALID_PERSONAL_STREET;
	}

}
