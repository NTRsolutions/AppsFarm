package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;

@ManagedBean
public class OfferSourceValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("offerSourceId")) {
			String offerSourceId = (String) parameters.get("offerSourceId");
			if (offerSourceId != null && offerSourceId.length() > 0){
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
		return  RespCodesEnum.ERROR_INVALID_OFFER_DATA;
	}

}
