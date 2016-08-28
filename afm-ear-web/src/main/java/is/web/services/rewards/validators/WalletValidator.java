package is.web.services.rewards.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.web.services.APIValidator;

@ManagedBean
public class WalletValidator implements APIValidator {

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAOApplicationReward daoApplicationReward;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			String username = (String) parameters.get("username");
			int rewardId = (Integer) parameters.get("rewardId");
			AppUserEntity appUser = daoAppUser.findByUsername(username);
			WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
			ApplicationRewardEntity reward = daoApplicationReward.findById(rewardId);
			if (walletData.getBalance() >= reward.getRewardValue()) {
				return true;
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
		return RespCodesEnum.ERROR_INVALID_CREDIT_POINTS;
	}

}
