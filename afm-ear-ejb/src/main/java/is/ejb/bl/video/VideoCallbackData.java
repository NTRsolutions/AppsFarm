package is.ejb.bl.video;

public class VideoCallbackData {
	private String userId;
	private String username;
	private String uid;
	private int amount;
	private String currencyId;
	private String currencyName;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getCurrencyId() {
		return currencyId;
	}
	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	@Override
	public String toString() {
		return "VideoCallbackData [userId=" + userId + ", username=" + username + ", uid=" + uid + ", amount=" + amount
				+ ", currencyId=" + currencyId + ", currencyName=" + currencyName + "]";
	}
	
	
	
}
