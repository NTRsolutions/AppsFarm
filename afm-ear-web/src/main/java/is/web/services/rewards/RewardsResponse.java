package is.web.services.rewards;

import java.util.List;

import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.RewardCategoryEntity;
import is.web.services.APIResponse;

public class RewardsResponse extends APIResponse{
	private List<ApplicationRewardEntity> rewards;
	private List<RewardCategoryEntity> categories;
	
	public List<ApplicationRewardEntity> getRewards() {
		return rewards;
	}
	public void setRewards(List<ApplicationRewardEntity> rewards) {
		this.rewards = rewards;
	}
	public List<RewardCategoryEntity> getCategories() {
		return categories;
	}
	public void setCategories(List<RewardCategoryEntity> categories) {
		this.categories = categories;
	}
	
	
}
