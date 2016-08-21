package is.ejb.bl.reporting;

public class WallRequestLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumberExtension;
	private String phoneNumber;
	private String email;
	private String locale;
	private String ipAddress;
	private String wallRewardType;
	private String wallGeo;
	private String wallDeviceType;
	private int wallId;
	private String ua;
	private String systemInfo;

	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = validate(timestamp);
	}

	public String getPhoneNumberExtension() {
		return phoneNumberExtension;
	}

	public void setPhoneNumberExtension(String phoneNumberExtension) {
		this.phoneNumberExtension = validate(phoneNumberExtension);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = validate(phoneNumber);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = validate(email);
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = validate(locale);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = validate(ipAddress);
	}

	public String getWallRewardType() {
		return wallRewardType;
	}

	public void setWallRewardType(String wallRewardType) {
		this.wallRewardType = validate(wallRewardType);
	}

	public String getWallGeo() {
		return wallGeo;
	}

	public void setWallGeo(String wallGeo) {
		this.wallGeo = validate(wallGeo);
	}

	public String getWallDeviceType() {
		return wallDeviceType;
	}

	public void setWallDeviceType(String wallDeviceType) {
		this.wallDeviceType = validate(wallDeviceType);
	}

	public int getWallId() {
		return wallId;
	}

	public void setWallId(int wallId) {
		this.wallId = wallId;
	}

	public String getUa() {
		return ua;
	}

	public void setUa(String ua) {
		this.ua = validate(ua);
	}

	public String getSystemInfo() {
		return systemInfo;
	}

	public void setSystemInfo(String systemInfo) {
		this.systemInfo = validate(systemInfo);
	}

	@Override
	public String toCSV() {
		return timestamp + "," + phoneNumberExtension + "," + phoneNumber 
				+ "," + email + "," + locale + "," + formatIpAddress(ipAddress) + "," + wallRewardType
				+ "," + removeCommas(wallGeo) + "," + removeCommas(wallDeviceType) + "," + wallId 
				+ "," + removeCommas(ua) + "," + removeCommas(systemInfo);
	}

}
