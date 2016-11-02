package is.web.services.acra;

import com.google.gson.annotations.SerializedName;

public class AcraReport {
	
	@SerializedName("REPORT_ID")
	private String reportId;
	
	@SerializedName("STACK_TRACE")
	private String stackTrace;
	
	@SerializedName("USER_APP_START_DATE")
	private String userAppStartDate;
	
	@SerializedName("USER_CRASH_DATE")
	private String userCrashDate;
	
	@SerializedName("CUSTOM_DATA")
	private AcraCustomData customData;
	
	@SerializedName("APP_VERSION_CODE")
	private int appVersionCode;
	
	@SerializedName("PHONE_MODEL")
	private String phoneModel;
	
	@SerializedName("ANDROID_VERSION")
	private String androidVersion;

	/**
	 * @return the reportId
	 */
	public String getReportId() {
		return reportId;
	}

	/**
	 * @param reportId the reportId to set
	 */
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	/**
	 * @return the stackTrace
	 */
	public String getStackTrace() {
		return stackTrace;
	}

	/**
	 * @param stackTrace the stackTrace to set
	 */
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	

	/**
	 * @return the userAppStartDate
	 */
	public String getUserAppStartDate() {
		return userAppStartDate;
	}

	/**
	 * @param userAppStartDate the userAppStartDate to set
	 */
	public void setUserAppStartDate(String userAppStartDate) {
		this.userAppStartDate = userAppStartDate;
	}

	/**
	 * @return the userCrashDate
	 */
	public String getUserCrashDate() {
		return userCrashDate;
	}

	/**
	 * @param userCrashDate the userCrashDate to set
	 */
	public void setUserCrashDate(String userCrashDate) {
		this.userCrashDate = userCrashDate;
	}

	/**
	 * @return the customData
	 */
	public AcraCustomData getCustomData() {
		return customData;
	}

	/**
	 * @param customData the customData to set
	 */
	public void setCustomData(AcraCustomData customData) {
		this.customData = customData;
	}

	/**
	 * @return the appVersionCode
	 */
	public int getAppVersionCode() {
		return appVersionCode;
	}

	/**
	 * @param appVersionCode the appVersionCode to set
	 */
	public void setAppVersionCode(int appVersionCode) {
		this.appVersionCode = appVersionCode;
	}

	/**
	 * @return the phoneModel
	 */
	public String getPhoneModel() {
		return phoneModel;
	}

	/**
	 * @param phoneModel the phoneModel to set
	 */
	public void setPhoneModel(String phoneModel) {
		this.phoneModel = phoneModel;
	}

	/**
	 * @return the androidVersion
	 */
	public String getAndroidVersion() {
		return androidVersion;
	}

	/**
	 * @param androidVersion the androidVersion to set
	 */
	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AcraReport [reportId=" + reportId + ", stackTrace=" + stackTrace + ", userAppStartDate="
				+ userAppStartDate + ", userCrashDate=" + userCrashDate + ", customData=" + customData
				+ ", appVersionCode=" + appVersionCode + ", phoneModel=" + phoneModel + ", androidVersion="
				+ androidVersion + "]";
	}


	
}