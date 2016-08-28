package is.web.services.rewards.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOApplicationReward;
import is.web.services.APIValidator;

@ManagedBean
public class RewardIdValidator implements APIValidator {

	@Inject
	private DAOApplicationReward daoApplicationReward;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			if (parameters.containsKey("rewardId")) {
				Integer rewardId = (Integer) parameters.get("rewardId");
				if (rewardId != 0 && daoApplicationReward.findById(rewardId) != null) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_REWARD_ID;
	}

}
