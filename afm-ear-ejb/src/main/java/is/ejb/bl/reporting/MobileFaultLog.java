package is.ejb.bl.reporting;

public class MobileFaultLog extends AbstractEventLog {

	private String timestamp;
	private String phoneNumberExtension;
	private String phoneNumber;
	private String ipAddress;
	private String action;
	private String errorMessage;
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = validate(ipAddress);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = validate(action);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = validate(errorMessage);
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
				+ "," + formatIpAddress(ipAddress) + "," + removeCommas(action) 
				+ "," + removeCommas(errorMessage)  + "," + formatMiscData(miscData) 
				+ "," + removeCommas(systemInfo);
	}

}
