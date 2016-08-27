package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class PayoutCurrencyValidator implements APIValidator{

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("payoutCurrency")){
			String payoutCurrency = (String) parameters.get("payoutCurrency");
			if (payoutCurrency != null && payoutCurrency.length() > 0){
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
		return RespCodesEnum.ERROR_MISSING_OFFER_CURRENCY_CODE;
	}

}
