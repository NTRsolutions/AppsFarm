package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAODenominationModel;
import is.web.services.APIValidator;

@ManagedBean
public class DenominationModelValidator implements APIValidator {

	@Inject
	private DAODenominationModel daoDenominationModel;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			int realmId = (Integer) parameters.get("internalNetworkId");
			String rewardType = (String) parameters.get("rewardType");
			int numberOfRegisteredDenominationModels = daoDenominationModel
					.getRegisteredDenominationModelsNumberByRewardTypeNameAndRealmId(rewardType, realmId);
			if (numberOfRegisteredDenominationModels <= 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_NO_DENOMINATION_MODEL_REGISTERED_FOR_GIVEN_REWARD_TYPE_NAME;
	}

}
