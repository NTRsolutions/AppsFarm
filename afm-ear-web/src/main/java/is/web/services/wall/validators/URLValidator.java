package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class URLValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("url")) {
			String url = (String) parameters.get("url");
			if (url != null && url.length() > 0) {
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
		return RespCodesEnum.ERROR_INVALID_OFFER_DATA;
	}

}
