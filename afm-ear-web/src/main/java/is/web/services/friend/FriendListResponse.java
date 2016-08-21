package is.web.services.friend;

import is.ejb.dl.entities.UserFriendEntity;
import is.web.services.Response;

import java.util.List;

public class FriendListResponse extends Response{

	private List<UserFriendEntity> friendList;

	public List<UserFriendEntity> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<UserFriendEntity> friendList) {
		this.friendList = friendList;
	}
	
	
}
