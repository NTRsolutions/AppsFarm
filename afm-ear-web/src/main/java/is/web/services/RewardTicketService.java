package is.web.services;

import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.dl.dao.DAORewardTickets;
import is.ejb.dl.entities.RewardTicketEntity;

@Path("/")
public class RewardTicketService {

	@Inject
	private Logger logger;

	@Inject
	private DAORewardTickets daoRewardTickets;

	@GET
	@Produces("application/json")
	@Path("/v1/requestTicket/")
	public String requestTicket(@QueryParam("userId") int userId, @QueryParam("email") String email,
			@QueryParam("rewardName") String rewardName, @QueryParam("creditPoints") double creditPoints) {

		if (userId <= 0) {
			return getJsonResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_USER);
		}
		
		if (email == null || email.isEmpty()) {
			return getJsonResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_USER_INVALID_EMAIL);
		}
		
		if (rewardName == null || rewardName.isEmpty()) {
			return getJsonResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_REWARD_NAME);
		}
		
		if (creditPoints < 0) {
			return getJsonResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_CREDIT_POINTS);
		}

		RewardTicketEntity rewardTicket = new RewardTicketEntity();
		rewardTicket.setUserId(userId);
		rewardTicket.setEmail(email);
		rewardTicket.setRewardName(rewardName);
		rewardTicket.setCreditPoints(creditPoints);
		rewardTicket.setRequestDate(new Timestamp(System.currentTimeMillis()));
		rewardTicket.setStatus(RewardTicketStatus.NEW);

		try {
			daoRewardTickets.createOrUpdate(rewardTicket);
		} catch (Exception e) {
			logger.info("requestTicket exception: " + e.getMessage());
			return getJsonResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}

		return getJsonResponse(RespStatusEnum.SUCCESS, RespCodesEnum.OK);
	}

	private String getJsonResponse(RespStatusEnum status, RespCodesEnum code) {
		logger.info("requestTicket response [" + status.toString() + ", " + code.toString() + "]");
		Response response = new Response(status, code);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(response);
	}

}
