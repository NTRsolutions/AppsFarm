package is.web.services.personalDetails.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class PostCodeValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("postCode")){
			String postCode = (String) parameters.get("postCode");
			if (postCode != null && postCode.length() > 0){
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
		return RespCodesEnum.ERROR_INVALID_PERSONAL_POST_CODE;
	}

}
