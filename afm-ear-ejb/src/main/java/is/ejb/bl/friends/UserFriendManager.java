package is.ejb.bl.friends;

import is.ejb.dl.dao.DAOUserFriend;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.UserFriendEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class UserFriendManager {

	@Inject
	private DAOUserFriend daoUserFriend;

	@Inject
	private Logger logger;

	public UserFriendEntity createUserFriend(int userId, String name, String phoneNumber, int payoutCarrierId) {
		UserFriendEntity userFriendEntity = new UserFriendEntity();
		userFriendEntity.setUserId(userId);
		userFriendEntity.setName(name);
		userFriendEntity.setPhoneNumber(phoneNumber);
		userFriendEntity.setPayoutCarrierId(payoutCarrierId);
		return daoUserFriend.createOrUpdate(userFriendEntity);
	}

	public UserFriendEntity getUserFriendWithPhoneNumber(int userId, String phoneNumber) {
		try {
			logger.info("getUserFriendWithPhoneNumbe called with params userId: " + userId + " phoneNumber: "
					+ phoneNumber);
			System.out.println("getUserFriendWithPhoneNumbe called with params userId: " + userId + " phoneNumber: "
					+ phoneNumber);
			List<UserFriendEntity> friendList = daoUserFriend.findByUserId(userId);
			logger.info("Received list: " + friendList);
			System.out.println("Received list: " + friendList);
			if (friendList != null) {
				for (UserFriendEntity friend : friendList) {
					if (friend.getPhoneNumber().equals(phoneNumber)) {
						return friend;
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return null;
	}

	public void removeUserFriend(int userId, String phoneNumber) {
		UserFriendEntity userFriend = getUserFriendWithPhoneNumber(userId, phoneNumber);
		if (userFriend != null) {
			daoUserFriend.delete(userFriend);
		}

	}

	public boolean isFriendInList(int userId, String phoneNumber) {
		try {
			logger.info("isFriendInList called with params userId: " + userId + " phoneNumber: " + phoneNumber);
			List<UserFriendEntity> friendList = daoUserFriend.findByUserId(userId);
			logger.info("Received list: " + friendList);
			if (friendList != null) {
				for (UserFriendEntity friend : friendList) {
					if (friend.getPhoneNumber().equals(phoneNumber)) {
						logger.info("Friend with phoneNumber: "+phoneNumber + " and for userid: "+userId + " is in list");
						return true;
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}

	public List<UserFriendEntity> getUserFriendList(int userId) {
		try {
			return daoUserFriend.findByUserId(userId);
		} catch (Exception exception) {
			exception.printStackTrace();
			return new ArrayList<UserFriendEntity>();
		}
	}

}
