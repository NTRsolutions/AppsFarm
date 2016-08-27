package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

@ManagedBean
public class AdProviderValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("adProviderCodeName")){
			String adProviderCodeName = (String) parameters.get("adProviderCodeName");
			if (adProviderCodeName != null && adProviderCodeName.length() > 0){
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
		return RespCodesEnum.ERROR_INVALID_AD_PROVIDER;
	}

}
