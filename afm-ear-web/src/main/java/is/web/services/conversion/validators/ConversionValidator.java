package is.web.services.conversion.validators;

import is.ejb.bl.business.RespCodesEnum;
import is.web.services.conversion.ConversionData;

public interface ConversionValidator {
	public boolean validate(ConversionData data);
	public RespCodesEnum getInvalidValueErrorCode();
}
