package is.web.services.rewards;

import java.util.List;

import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.RewardCategoryEntity;
import is.web.services.APIResponse;

public class RewardsResponse extends APIResponse{
	private List<ApplicationRewardEntity> rewards;
	public List<ApplicationRewardEntity> getRewards() {
		return rewards;
	}
	public void setRewards(List<ApplicationRewardEntity> rewards) {
		this.rewards = rewards;
	}
	
	
}
