package is.ejb.bl.offerWall;

public class RealtimeFeedDataHolder {
	private String userId;
	private String ua;
	private String ip;
	private String gaid;
	private String idfa;
	private String deviceType;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getGaid() {
		return gaid;
	}
	public void setGaid(String gaid) {
		this.gaid = gaid;
	}
	public String getIdfa() {
		return idfa;
	}
	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}
	public String getUa() {
		return ua;
	}
	public void setUa(String ua) {
		this.ua = ua;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	
}
