package is.ejb.bl.reporting;

public class SupportRequestLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumberExtension;
	private String phoneNumber;
	private String email;
	private String deviceType;
	private String locale;
	private String ipAddress;
	private String errorCategory;
	private String supportQuestion;
	private String miscData;
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

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = validate(deviceType);
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

	public String getErrorCategory() {
		return errorCategory;
	}

	public void setErrorCategory(String errorCategory) {
		this.errorCategory = validate(errorCategory);
	}

	public String getSupportQuestion() {
		return supportQuestion;
	}

	public void setSupportQuestion(String supportQuestion) {
		this.supportQuestion = validate(supportQuestion);
	}

	public String getMiscData() {
		return miscData;
	}

	public void setMiscData(String miscData) {
		this.miscData = validate(miscData);
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
				+ "," + email + "," + deviceType + "," + locale + "," + formatIpAddress(ipAddress)
				+ "," + removeCommas(errorCategory) + "," + formatSupportQuestion(supportQuestion) 
				+ "," + formatMiscData(miscData) + "," + removeCommas(systemInfo);
	}

}
