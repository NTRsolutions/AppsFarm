package is.ejb.bl.reporting;

public class RegistrationLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumberExtension;
	private String phoneNumber;
	private String email;
	private String deviceType;
	private boolean isMale;
	private String ageRange;
	private String locale;
	private String systemInfo;
	private String gaid;
	private String idfa;
	private String applicationName;

	
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

	public boolean isMale() {
		return isMale;
	}

	public void setMale(boolean isMale) {
		this.isMale = isMale;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = validate(ageRange);
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = validate(locale);
	}

	public String getSystemInfo() {
		return systemInfo;
	}

	public void setSystemInfo(String systemInfo) {
		this.systemInfo = validate(systemInfo);
	}

	public String getGaid() {
		return gaid;
	}

	public void setGaid(String gaid) {
		this.gaid = validate(gaid);
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = validate(idfa);
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = validate(applicationName);
	}

	@Override
	public String toCSV() {
		return timestamp + "," + phoneNumberExtension + "," + phoneNumber 
				+ "," + email + "," + deviceType + "," + isMale + "," + ageRange
				+ "," + locale + "," + removeCommas(systemInfo) + "," + removeCommas(gaid)
				+ "," + removeCommas(idfa) + "," + removeCommas(applicationName);
	}

}
