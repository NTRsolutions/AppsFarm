package is.ejb.bl.reporting;

public class CrashReportLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumberExtension;
	private String phoneNumber;
	private String ipAddress;
	private String applicationName;
	private String deviceInfo;
	private String deviceVersion;
	private String breadcrumb;

	
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = validate(ipAddress);
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = validate(applicationName);
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = validate(deviceInfo);
	}

	public String getDeviceVersion() {
		return deviceVersion;
	}

	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = validate(deviceVersion);
	}

	public String getBreadcrumb() {
		return breadcrumb;
	}

	public void setBreadcrumb(String breadcrumb) {
		this.breadcrumb = validate(breadcrumb);
	}

	@Override
	public String toCSV() {
		return timestamp + "," + phoneNumberExtension + "," + phoneNumber + ","
				+ formatIpAddress(ipAddress) + "," + applicationName + "," + deviceInfo + ","
				+ deviceVersion + "," + breadcrumb;
	}

}
