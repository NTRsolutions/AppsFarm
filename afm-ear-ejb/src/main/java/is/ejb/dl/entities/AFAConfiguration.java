package is.ejb.dl.entities;



public class AFAConfiguration {
	private boolean AFASessionCheckingEnabled;
	private int AFASessionTimeout;
	private String AFASessionTimeoutMessage;
	private int AFASessionReminderTime;
	private String AFASessionReminderMessage;
	private int AFAVerifiedSessionTime;
	private int AFASessionTimeoutResetTime;
	private int AFASessionFlushTime;

	public boolean isAFASessionCheckingEnabled() {
		return AFASessionCheckingEnabled;
	}

	public void setAFASessionCheckingEnabled(boolean aFASessionCheckingEnabled) {
		AFASessionCheckingEnabled = aFASessionCheckingEnabled;
	}

	public int getAFASessionTimeout() {
		return AFASessionTimeout;
	}

	public void setAFASessionTimeout(int aFASessionTimeout) {
		AFASessionTimeout = aFASessionTimeout;
	}

	

	public int getAFAVerifiedSessionTime() {
		return AFAVerifiedSessionTime;
	}

	public void setAFAVerifiedSessionTime(int aFAVerifiedSessionTime) {
		AFAVerifiedSessionTime = aFAVerifiedSessionTime;
	}

	public int getAFASessionTimeoutResetTime() {
		return AFASessionTimeoutResetTime;
	}

	public void setAFASessionTimeoutResetTime(int aFASessionTimeoutResetTime) {
		AFASessionTimeoutResetTime = aFASessionTimeoutResetTime;
	}

	public int getAFASessionFlushTime() {
		return AFASessionFlushTime;
	}

	public void setAFASessionFlushTime(int aFASessionFlushTime) {
		AFASessionFlushTime = aFASessionFlushTime;
	}

	public String getAFASessionTimeoutMessage() {
		return AFASessionTimeoutMessage;
	}

	public void setAFASessionTimeoutMessage(String aFASessionTimeoutMessage) {
		AFASessionTimeoutMessage = aFASessionTimeoutMessage;
	}

	public int getAFASessionReminderTime() {
		return AFASessionReminderTime;
	}

	public void setAFASessionReminderTime(int aFASessionReminderTime) {
		AFASessionReminderTime = aFASessionReminderTime;
	}

	public String getAFASessionReminderMessage() {
		return AFASessionReminderMessage;
	}

	public void setAFASessionReminderMessage(String aFASessionReminderMessage) {
		AFASessionReminderMessage = aFASessionReminderMessage;
	}
	
	

}
