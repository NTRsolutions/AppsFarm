package is.web.services.conversion.validators;

import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.external.ExternalServerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.web.services.conversion.ConversionData;

@ManagedBean
public class ServerAddressValidator implements ConversionValidator {

	@Inject
	private ExternalServerManager externalServerManager;


	@Override
	public boolean validate(ConversionData data) {

		boolean isServerAddressListed = externalServerManager.isServerAddressListedWithType(data.getIpAddress(),
				data.getUserEvent().getAdProviderCodeName());
		if (isServerAddressListed) {
			return true;
		} else {
			logServer(data);
			return false;
		}
	}

	private void logServer(ConversionData data) {
		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, -1, LogStatus.OK,
				Application.CONVERSION_ACTIVITY + " " + Application.EXTERNAL_SERVER_NOT_LISTED + " ipAddress :"
						+ data.getIpAddress() + " in provider table: " + data.getUserEvent().getAdProviderCodeName()
						+ " is not listed");
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_SERVER_IS_NOT_LISTED;
	}

}
