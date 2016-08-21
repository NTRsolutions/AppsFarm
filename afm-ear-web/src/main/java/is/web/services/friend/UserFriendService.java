package is.web.services.friend;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.friends.UserFriendManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.UserFriendEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.web.services.Response;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;

@Path("/")
public class UserFriendService {

	@Inject
	private UserFriendManager userFriendManager;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private Logger logger;

	@Inject
	private DAOWalletPayoutCarrier daoWalletPayoutCarrier;

	private Gson gson;

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@GET
	@Produces("application/json")
	@Path("/v1/addFriend/")
	public String addFriend(@QueryParam("userId") String userId, @QueryParam("name") String name,
			@QueryParam("phoneNumber") String phoneNumber, @QueryParam("payoutCarrierId") String payoutCarrierId) {
		String data = "userId: " + userId + " name: " + name + " phoneNumber: " + phoneNumber + " payoutCarrierId: "
				+ payoutCarrierId;
		logger.info("Received add friend request: " + data);
		if (userId == null || !(userId.length() > 0)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER_DATA.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}
		int userIdValue = Integer.valueOf(userId);

		if (!isValidUser(userIdValue)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		if (name == null || !(name.length() > 0) || phoneNumber == null || !(phoneNumber.length() > 0)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_FRIEND_DATA.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		if (!isPayoutCarrierValid(payoutCarrierId)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_WALLET_PAYOUT_CARRIER.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		WalletPayoutCarrierEntity carrier = this.getWalletPayoutCarrierWithId(Integer.parseInt(payoutCarrierId));
		

		if (userFriendManager.isFriendInList(userIdValue, phoneNumber)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_FRIEND_IS_IN_LIST.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		userFriendManager.createUserFriend(userIdValue, name, phoneNumber,carrier.getId());
		return gson.toJson(new Response().getSuccessResponse());
	}

	@GET
	@Produces("application/json")
	@Path("/v1/removeFriend/")
	public String removeFriend(@QueryParam("userId") String userId, @QueryParam("phoneNumber") String phoneNumber) {
		String data = "userId: " + userId + " phoneNumber: " + phoneNumber;
		logger.info("Remove friend request: " + data);
		if (userId == null || !(userId.length() > 0)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER_DATA.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		int userIdValue = Integer.valueOf(userId);
		if (!isValidUser(userIdValue)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		if (phoneNumber == null || !(phoneNumber.length() > 0)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_FRIEND_DATA.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		if (!userFriendManager.isFriendInList(userIdValue, phoneNumber)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_FRIEND_IS_NOT_IN_LIST.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}

		userFriendManager.removeUserFriend(userIdValue, phoneNumber);
		return gson.toJson(new Response().getSuccessResponse());

	}

	@GET
	@Produces("application/json")
	@Path("/v1/friendList/")
	public String getFriendList(@QueryParam("userId") String userId) {
		String data = "userId: " + userId;
		logger.info("Received get friendlist: " + data);
		if (userId == null || !(userId.length() > 0)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER_DATA.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}
		int userIdValue = Integer.valueOf(userId);
		if (!isValidUser(userIdValue)) {
			Response response = new Response();
			response.setCode(RespCodesEnum.ERROR_INVALID_USER.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			return gson.toJson(response);
		}
		List<UserFriendEntity> friendList = userFriendManager.getUserFriendList(userIdValue);
		FriendListResponse response = new FriendListResponse();
		response.setCode(RespCodesEnum.OK.toString());
		response.setStatus(RespStatusEnum.SUCCESS.toString());
		response.setFriendList(friendList);

		return gson.toJson(response);
	}

	private boolean isPayoutCarrierValid(String carrierId) {
		try {
			if (carrierId == null || carrierId.length() == 0) {
				return false;
			}
			int carrierIdInt = Integer.parseInt(carrierId);
			WalletPayoutCarrierEntity carrier = getWalletPayoutCarrierWithId(carrierIdInt);
			if (carrier == null) {
				return false;
			}

			return true;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}

	}

	private WalletPayoutCarrierEntity getWalletPayoutCarrierWithId(int id){
		try {
			return daoWalletPayoutCarrier.findById(id);
		}
		catch (Exception exception){
			exception.printStackTrace();
			return null;
		}
	}
	
	private boolean isValidUser(int userId) {

		try {
			AppUserEntity appUser = daoAppUser.findById(userId);
			if (appUser != null) {
				return true;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;

	}

}
