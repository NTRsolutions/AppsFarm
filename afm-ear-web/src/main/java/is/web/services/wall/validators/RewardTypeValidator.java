package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.services.APIValidator;

@ManagedBean
public class RewardTypeValidator implements APIValidator {

	@Inject
	private DAORewardType daoRewardType;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		if (parameters.containsKey("rewardType")) {
			String rewardType = (String) parameters.get("rewardType");
			if (rewardType != null && rewardType.length() > 0 && getRewardTypeWithName(rewardType) != null) {
				return true;
			} else {
				return false;
			}
		}else {
			return false;
		}
	}

	private RewardTypeEntity getRewardTypeWithName(String rewardType) {
		try {
			return daoRewardType.findByName(rewardType);
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_REWARD_TYPE;
	}

}
