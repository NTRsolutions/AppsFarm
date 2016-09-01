package is.web.services.conversion.validators;

import java.util.List;

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
public class FraudDetectionValidator implements ConversionValidator {

	@Inject
	private DAOUserEvent daoUserEvent;

	@Override
	public boolean validate(ConversionData data) {
		UserEventEntity event = data.getUserEvent();
		try {
			List<UserEventEntity> listFraudConversions = daoUserEvent
					.findAllByUserIdAndProviderOfferId(event.getUserId(), event.getOfferSourceId());
			if (listFraudConversions != null && (listFraudConversions.size() > 1)) {
				boolean historyConversionForSameOfferIdentified = false;
				for (int i = 0; i < listFraudConversions.size(); i++) {
					if (listFraudConversions.get(i) != null
							&& listFraudConversions.get(i).getConversionDate() != null) {
						historyConversionForSameOfferIdentified = true;
					}
				}

				if (historyConversionForSameOfferIdentified) {
					logDupliacteConversion(data.getIpAddress(), event, listFraudConversions);
					return false;

				}
			}

			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return true;
		}
	}

	private void logDupliacteConversion(String ipAddress, UserEventEntity event,
			List<UserEventEntity> listFraudConversions) {
		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, -1, LogStatus.ERROR,
				Application.CONVERSION_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
						+ RespCodesEnum.ERROR_USER_FRAUD_DUPLICATE_CONVERSION_DETECTED
						+ " number of identified unique clicks: " + listFraudConversions.size() + " ip: " + ipAddress
						+ " for user with phone: " + event.getPhoneNumber() + " user id: " + event.getUserId()
						+ " offer title: " + event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
						+ " offer provider id: " + event.getOfferSourceId() + " offer id: " + event.getOfferId());
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_DUPLICATE_CONVERSION_IDENTIFIED;
	}

}
