package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.APIValidator;
@ManagedBean
public class RewardCurrencyValidator implements APIValidator {

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("rewardCurrency")){
			String rewardCurrency = (String) parameters.get("rewardCurrency");
			if (rewardCurrency != null && rewardCurrency.length() > 0){
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
		return RespCodesEnum.ERROR_OFFER_NOT_FOUND;
	}

}
