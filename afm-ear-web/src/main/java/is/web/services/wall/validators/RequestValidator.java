package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.security.HashValidationManager;
import is.web.services.APIValidator;

@ManagedBean
public class RequestValidator implements APIValidator{

	@Inject
	private HashValidationManager hashValidationManager;
	
	
	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		boolean isRequestValid = hashValidationManager.isAPIRequestValid(parameters);
		if (isRequestValid) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED;
	}

}
