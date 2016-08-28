package is.web.services.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAORewardCategory;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.RewardCategoryEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.wall.validators.RewardTypeValidator;

@Path("/")
public class RewardService {

	@Inject
	private DAOApplicationReward daoApplicationReward;

	@Inject
	private DAORewardCategory daoRewardCategory;

	@Inject
	private RewardTypeValidator rewardTypeValidator;

	@Inject
	private APIHelper apiHelper;

	@Inject
	private Logger logger;
	
	@Context
	private HttpServletRequest httpRequest;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/rewards")
	public String getRewards(final APIRequestDetails details) {
		RewardsResponse response = new RewardsResponse();
		try {
			logger.info("Received get rewards request: " + details);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_ACTIVITY + "Received get rewards request ipAddress: "
							+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + details);
			HashMap<String, Object> parameters = details.getParameters();
			if (!rewardTypeValidator.validate(parameters)) {
				logger.info("Failed validator: " + rewardTypeValidator.getInvalidValueErrorCode());
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INVALID_REWARD_TYPE);
			} else {
				logger.info("Validation OK");
				String rewardType = (String) parameters.get("rewardType");
				List<ApplicationRewardEntity> applicationRewards = daoApplicationReward.findByRewardType(rewardType);
				List<RewardCategoryEntity> allRewardCategories = daoRewardCategory.getAll();
				List<RewardCategoryEntity> rewardCategoriesFiltered = filterCategories(applicationRewards,
						allRewardCategories);
				apiHelper.setupSuccessResponse(response);
				response.setCategories(rewardCategoriesFiltered);
				response.setRewards(applicationRewards);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}
		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.OK,
				Application.REWARD_ACTIVITY + "Returning: " + response + " for request: " +details);
		return apiHelper.getGson().toJson(response);
	}

	private List<RewardCategoryEntity> filterCategories(List<ApplicationRewardEntity> applicationRewards,
			List<RewardCategoryEntity> allRewardCategories) {
		List<RewardCategoryEntity> categories = new ArrayList<RewardCategoryEntity>();
		for (ApplicationRewardEntity applicationReward : applicationRewards) {
			for (RewardCategoryEntity category : allRewardCategories) {
				if (category.getName().equals(applicationReward.getRewardCategory())){
					if (!categories.contains(category.getName())){
						categories.add(category);
					}
				}
			}
		}
		return categories;
	}
}
