package is.web.services.conversion.validators;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.UserEventEntity;
import is.web.services.conversion.ConversionData;

@ManagedBean
public class EventValidator implements ConversionValidator {

	@Inject
	private DAOUserEvent daoUserEvent;

	@Override
	public boolean validate(ConversionData data) {
		try {
			UserEventEntity event = daoUserEvent.findByInternalTransactionId(data.getInternalTransactionId());
			if (event == null) {
				logEvent(data);
				return false;

			} else {
				data.setUserEvent(event);
				return true;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			logEvent(data);
			return false;
		}
	}

	private void logEvent(ConversionData data){
		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, -1, LogStatus.ERROR,
				Application.CONVERSION_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
						+ RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_TRANSACTION + " data: " + data);
	}
	
	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_UNABLE_TO_IDENTIFY_TRANSACTION;
	}

}
