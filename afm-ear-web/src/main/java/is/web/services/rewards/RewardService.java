package is.web.services.rewards;

import java.sql.Timestamp;
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
import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.bl.reward.RewardTicketManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAORewardCategory;
import is.ejb.dl.dao.DAORewardTickets;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.RewardCategoryEntity;
import is.ejb.dl.entities.RewardTicketEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIResponse;
import is.web.services.APIValidator;
import is.web.services.rewards.validators.RewardIdValidator;
import is.web.services.rewards.validators.WalletValidator;
import is.web.services.user.validators.PasswordValidator;
import is.web.services.user.validators.UsernameValidator;
import is.web.services.wall.validators.RequestValidator;
import is.web.services.wall.validators.RewardTypeValidator;
import is.web.services.wallet.validators.UsernamePasswordCombinationValidator;

@Path("/")
public class RewardService {

	@Inject
	private DAOApplicationReward daoApplicationReward;

	@Inject
	private DAORewardCategory daoRewardCategory;
	@Inject
	private RewardTicketManager rewardTicketManager;
	@Inject
	private RewardTypeValidator rewardTypeValidator;
	@Inject
	private RequestValidator requestValidator;
	@Inject
	private UsernameValidator usernameValidator;
	@Inject
	private PasswordValidator passwordValidator;
	@Inject
	private UsernamePasswordCombinationValidator usernamePasswordCombinationValidator;
	@Inject
	private RewardIdValidator rewardIdValidator;
	@Inject
	private WalletValidator walletValidator;

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
			for (APIValidator validator : getRewardDataValidators()) {
				if (!validator.validate(parameters)) {
					logger.info("Failed validator: " + validator.getClass() + " error: "
							+ rewardTypeValidator.getInvalidValueErrorCode());
					apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INVALID_REWARD_TYPE);
					Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
							Application.REWARD_ACTIVITY + "Failed validator: " + validator.getClass() + " error: "
									+ rewardTypeValidator.getInvalidValueErrorCode() + " Details: " + details);
					return apiHelper.getGson().toJson(response);
				} else {
					logger.info("OK validator: " + validator.getClass());
				}
			}

			logger.info("Validation OK");
			String rewardType = (String) parameters.get("rewardType");
			List<ApplicationRewardEntity> applicationRewards = daoApplicationReward.findByRewardType(rewardType);
			List<RewardCategoryEntity> allRewardCategories = daoRewardCategory.getAll();
			List<RewardCategoryEntity> rewardCategoriesFiltered = filterCategories(applicationRewards,
					allRewardCategories);
			apiHelper.setupSuccessResponse(response);
			response.setCategories(rewardCategoriesFiltered);
			response.setRewards(applicationRewards);

		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}
		Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.OK,
				Application.REWARD_ACTIVITY + "Returning: " + response + " for request: " + details);
		return apiHelper.getGson().toJson(response);
	}

	private List<APIValidator> getRewardDataValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(rewardTypeValidator);
		validators.add(requestValidator);
		return validators;
	}

	private List<RewardCategoryEntity> filterCategories(List<ApplicationRewardEntity> applicationRewards,
			List<RewardCategoryEntity> allRewardCategories) {
		List<RewardCategoryEntity> categories = new ArrayList<RewardCategoryEntity>();
		for (ApplicationRewardEntity applicationReward : applicationRewards) {
			for (RewardCategoryEntity category : allRewardCategories) {
				if (category.getName().equals(applicationReward.getRewardCategory())) {
					if (!categories.contains(category.getName())) {
						categories.add(category);
					}
				}
			}
		}
		return categories;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reward/ticket/create")
	public String createRewardTicket(final APIRequestDetails details) {
		APIResponse response = new APIResponse();
		try {
			HashMap<String, Object> parameters = details.getParameters();
			logger.info("Received reward create ticket request: " + details);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Received get rewards request ipAddress: "
							+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + details);
			for (APIValidator validator : getCreateRewardTickerValidators()) {
				if (!validator.validate(parameters)) {
					logger.info("Failed validator: " + validator.getClass() + " error: "
							+ validator.getInvalidValueErrorCode());
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1,
							LogStatus.ERROR,
							Application.REWARD_TICKET_CREATE_ACTIVITY + "Failed validator: " + validator.getClass()
									+ " error: " + rewardTypeValidator.getInvalidValueErrorCode() + " Details: "
									+ details);
					return apiHelper.getGson().toJson(response);
				} else {
					logger.info("OK validator: " + validator.getClass());
				}
			}

			RewardTicketEntity rewardTicket = rewardTicketManager.createRewardTicket(parameters);
			if (rewardTicket != null) {
				apiHelper.setupSuccessResponse(response);
			} else {
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_CREATING_REWARD_TICKET);
			}
			return apiHelper.getGson().toJson(response);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1,
					LogStatus.ERROR,
					Application.REWARD_TICKET_CREATE_ACTIVITY + " Error: " + exc.toString() + " Details: " + details);
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return apiHelper.getGson().toJson(response);
		}
	}

	private List<APIValidator> getCreateRewardTickerValidators() {
		List<APIValidator> validators = new ArrayList<APIValidator>();
		validators.add(usernameValidator);
		validators.add(passwordValidator);
		validators.add(usernamePasswordCombinationValidator);
		validators.add(requestValidator);
		validators.add(rewardIdValidator);
		validators.add(walletValidator);
		return validators;
	}

}
