package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class OfferValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("offerId")){
			String offerId = (String) parameters.get("offerId");
			if (offerId != null && offerId.length() > 0){
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
		return RespCodesEnum.ERROR_INVALID_OFFER;
	}

}
