package is.ejb.bl.acra;

public class AcraCrashConfiguration {
	private String fontScale;
	private String hardKeyboardHidden;
	private String keyboard;
	private String keyboardHidden;
	private String locale;
	private int mcc;
	private int mnc;
	private String navigation;
	private String orientation;
	private String touchscreen;
	private boolean userSetLocale;
	public String getFontScale() {
		return fontScale;
	}
	public void setFontScale(String fontScale) {
		this.fontScale = fontScale;
	}
	public String getHardKeyboardHidden() {
		return hardKeyboardHidden;
	}
	public void setHardKeyboardHidden(String hardKeyboardHidden) {
		this.hardKeyboardHidden = hardKeyboardHidden;
	}
	public String getKeyboard() {
		return keyboard;
	}
	public void setKeyboard(String keyboard) {
		this.keyboard = keyboard;
	}
	public String getKeyboardHidden() {
		return keyboardHidden;
	}
	public void setKeyboardHidden(String keyboardHidden) {
		this.keyboardHidden = keyboardHidden;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public int getMcc() {
		return mcc;
	}
	public void setMcc(int mcc) {
		this.mcc = mcc;
	}
	public int getMnc() {
		return mnc;
	}
	public void setMnc(int mnc) {
		this.mnc = mnc;
	}
	public String getNavigation() {
		return navigation;
	}
	public void setNavigation(String navigation) {
		this.navigation = navigation;
	}
	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
	public String getTouchscreen() {
		return touchscreen;
	}
	public void setTouchscreen(String touchscreen) {
		this.touchscreen = touchscreen;
	}
	public boolean isUserSetLocale() {
		return userSetLocale;
	}
	public void setUserSetLocale(boolean userSetLocale) {
		this.userSetLocale = userSetLocale;
	}
	@Override
	public String toString() {
		return "AcraCrashConfiguration [fontScale=" + fontScale + ", hardKeyboardHidden=" + hardKeyboardHidden
				+ ", keyboard=" + keyboard + ", keyboardHidden=" + keyboardHidden + ", locale=" + locale + ", mcc="
				+ mcc + ", mnc=" + mnc + ", navigation=" + navigation + ", orientation=" + orientation
				+ ", touchscreen=" + touchscreen + ", userSetLocale=" + userSetLocale + "]";
	}
	
	
}
