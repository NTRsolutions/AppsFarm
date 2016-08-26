package is.ejb.bl.acra;

import java.util.List;

public class AcraReport {

	private double ANDROID_VERSION;
	private int APP_VERSION_CODE;
	private double APP_VERSION_NAME;
	private long AVAILABLE_MEM_SIZE;
	private String BRAND;
	private AcraBuild BUILD;
	private AcraCrashConfiguration CRASH_CONFIGURATION;
	private AcraCustomData CUSTOM_DATA;
	private List<String> DEVICE_FEATURES;
	private List<AcraDisplay> DISPLAY;
	private String DUMPSYS_MEMINFO;
	private AcraEnvironment ENVIRONMENT;
	private String FILE_PATH;
	private AcraCrashConfiguration INITIAL_CONFIGURATION;
	private String INSTALLATION_ID;
	private boolean IS_SILENT;
	private String LOGCAT;
	private String PACKAGE_NAME;
	private String PHONE_MODEL;
	private String PRODUCT;
	private String REPORT_ID;
	private List<String> SETTINGS_GLOBAL;
	private AcraSettingsSecure SETTINGS_SECURE;
	private AcraSettingsSystem SETTINGS_SYSTEM;
	private List<String> SHARED_PREFERENCES;
	private String STACK_TRACE;
	private long TOTAL_MEM_SIZE;
	private String USER_APP_START_DATE;
	private String USER_CRASH_DATE;
	private String USER_EMAIL;
	public double getANDROID_VERSION() {
		return ANDROID_VERSION;
	}
	public void setANDROID_VERSION(double aNDROID_VERSION) {
		ANDROID_VERSION = aNDROID_VERSION;
	}
	public int getAPP_VERSION_CODE() {
		return APP_VERSION_CODE;
	}
	public void setAPP_VERSION_CODE(int aPP_VERSION_CODE) {
		APP_VERSION_CODE = aPP_VERSION_CODE;
	}
	public double getAPP_VERSION_NAME() {
		return APP_VERSION_NAME;
	}
	public void setAPP_VERSION_NAME(double aPP_VERSION_NAME) {
		APP_VERSION_NAME = aPP_VERSION_NAME;
	}
	public long getAVAILABLE_MEM_SIZE() {
		return AVAILABLE_MEM_SIZE;
	}
	public void setAVAILABLE_MEM_SIZE(long aVAILABLE_MEM_SIZE) {
		AVAILABLE_MEM_SIZE = aVAILABLE_MEM_SIZE;
	}
	public String getBRAND() {
		return BRAND;
	}
	public void setBRAND(String bRAND) {
		BRAND = bRAND;
	}
	public AcraBuild getBUILD() {
		return BUILD;
	}
	public void setBUILD(AcraBuild bUILD) {
		BUILD = bUILD;
	}
	public AcraCrashConfiguration getCRASH_CONFIGURATION() {
		return CRASH_CONFIGURATION;
	}
	public void setCRASH_CONFIGURATION(AcraCrashConfiguration cRASH_CONFIGURATION) {
		CRASH_CONFIGURATION = cRASH_CONFIGURATION;
	}
	public AcraCustomData getCUSTOM_DATA() {
		return CUSTOM_DATA;
	}
	public void setCUSTOM_DATA(AcraCustomData cUSTOM_DATA) {
		CUSTOM_DATA = cUSTOM_DATA;
	}
	public List<String> getDEVICE_FEATURES() {
		return DEVICE_FEATURES;
	}
	public void setDEVICE_FEATURES(List<String> dEVICE_FEATURES) {
		DEVICE_FEATURES = dEVICE_FEATURES;
	}
	public List<AcraDisplay> getDISPLAY() {
		return DISPLAY;
	}
	public void setDISPLAY(List<AcraDisplay> dISPLAY) {
		DISPLAY = dISPLAY;
	}
	public String getDUMPSYS_MEMINFO() {
		return DUMPSYS_MEMINFO;
	}
	public void setDUMPSYS_MEMINFO(String dUMPSYS_MEMINFO) {
		DUMPSYS_MEMINFO = dUMPSYS_MEMINFO;
	}
	public AcraEnvironment getENVIRONMENT() {
		return ENVIRONMENT;
	}
	public void setENVIRONMENT(AcraEnvironment eNVIRONMENT) {
		ENVIRONMENT = eNVIRONMENT;
	}
	public String getFILE_PATH() {
		return FILE_PATH;
	}
	public void setFILE_PATH(String fILE_PATH) {
		FILE_PATH = fILE_PATH;
	}
	public AcraCrashConfiguration getINITIAL_CONFIGURATION() {
		return INITIAL_CONFIGURATION;
	}
	public void setINITIAL_CONFIGURATION(AcraCrashConfiguration iNITIAL_CONFIGURATION) {
		INITIAL_CONFIGURATION = iNITIAL_CONFIGURATION;
	}
	public String getINSTALLATION_ID() {
		return INSTALLATION_ID;
	}
	public void setINSTALLATION_ID(String iNSTALLATION_ID) {
		INSTALLATION_ID = iNSTALLATION_ID;
	}
	public boolean isIS_SILENT() {
		return IS_SILENT;
	}
	public void setIS_SILENT(boolean iS_SILENT) {
		IS_SILENT = iS_SILENT;
	}
	public String getLOGCAT() {
		return LOGCAT;
	}
	public void setLOGCAT(String lOGCAT) {
		LOGCAT = lOGCAT;
	}
	public String getPACKAGE_NAME() {
		return PACKAGE_NAME;
	}
	public void setPACKAGE_NAME(String pACKAGE_NAME) {
		PACKAGE_NAME = pACKAGE_NAME;
	}
	public String getPHONE_MODEL() {
		return PHONE_MODEL;
	}
	public void setPHONE_MODEL(String pHONE_MODEL) {
		PHONE_MODEL = pHONE_MODEL;
	}
	public String getPRODUCT() {
		return PRODUCT;
	}
	public void setPRODUCT(String pRODUCT) {
		PRODUCT = pRODUCT;
	}
	public String getREPORT_ID() {
		return REPORT_ID;
	}
	public void setREPORT_ID(String rEPORT_ID) {
		REPORT_ID = rEPORT_ID;
	}
	public List<String> getSETTINGS_GLOBAL() {
		return SETTINGS_GLOBAL;
	}
	public void setSETTINGS_GLOBAL(List<String> sETTINGS_GLOBAL) {
		SETTINGS_GLOBAL = sETTINGS_GLOBAL;
	}
	public AcraSettingsSecure getSETTINGS_SECURE() {
		return SETTINGS_SECURE;
	}
	public void setSETTINGS_SECURE(AcraSettingsSecure sETTINGS_SECURE) {
		SETTINGS_SECURE = sETTINGS_SECURE;
	}
	public AcraSettingsSystem getSETTINGS_SYSTEM() {
		return SETTINGS_SYSTEM;
	}
	public void setSETTINGS_SYSTEM(AcraSettingsSystem sETTINGS_SYSTEM) {
		SETTINGS_SYSTEM = sETTINGS_SYSTEM;
	}
	public List<String> getSHARED_PREFERENCES() {
		return SHARED_PREFERENCES;
	}
	public void setSHARED_PREFERENCES(List<String> sHARED_PREFERENCES) {
		SHARED_PREFERENCES = sHARED_PREFERENCES;
	}
	public String getSTACK_TRACE() {
		return STACK_TRACE;
	}
	public void setSTACK_TRACE(String sTACK_TRACE) {
		STACK_TRACE = sTACK_TRACE;
	}
	public long getTOTAL_MEM_SIZE() {
		return TOTAL_MEM_SIZE;
	}
	public void setTOTAL_MEM_SIZE(long tOTAL_MEM_SIZE) {
		TOTAL_MEM_SIZE = tOTAL_MEM_SIZE;
	}
	public String getUSER_APP_START_DATE() {
		return USER_APP_START_DATE;
	}
	public void setUSER_APP_START_DATE(String uSER_APP_START_DATE) {
		USER_APP_START_DATE = uSER_APP_START_DATE;
	}
	public String getUSER_CRASH_DATE() {
		return USER_CRASH_DATE;
	}
	public void setUSER_CRASH_DATE(String uSER_CRASH_DATE) {
		USER_CRASH_DATE = uSER_CRASH_DATE;
	}
	public String getUSER_EMAIL() {
		return USER_EMAIL;
	}
	public void setUSER_EMAIL(String uSER_EMAIL) {
		USER_EMAIL = uSER_EMAIL;
	}
	@Override
	public String toString() {
		return "AcraReport [ANDROID_VERSION=" + ANDROID_VERSION + ", APP_VERSION_CODE=" + APP_VERSION_CODE
				+ ", APP_VERSION_NAME=" + APP_VERSION_NAME + ", AVAILABLE_MEM_SIZE=" + AVAILABLE_MEM_SIZE + ", BRAND="
				+ BRAND + ", BUILD=" + BUILD + ", CRASH_CONFIGURATION=" + CRASH_CONFIGURATION + ", CUSTOM_DATA="
				+ CUSTOM_DATA + ", DEVICE_FEATURES=" + DEVICE_FEATURES + ", DISPLAY=" + DISPLAY + ", DUMPSYS_MEMINFO="
				+ DUMPSYS_MEMINFO + ", ENVIRONMENT=" + ENVIRONMENT + ", FILE_PATH=" + FILE_PATH
				+ ", INITIAL_CONFIGURATION=" + INITIAL_CONFIGURATION + ", INSTALLATION_ID=" + INSTALLATION_ID
				+ ", IS_SILENT=" + IS_SILENT + ", LOGCAT=" + LOGCAT + ", PACKAGE_NAME=" + PACKAGE_NAME
				+ ", PHONE_MODEL=" + PHONE_MODEL + ", PRODUCT=" + PRODUCT + ", REPORT_ID=" + REPORT_ID
				+ ", SETTINGS_GLOBAL=" + SETTINGS_GLOBAL + ", SETTINGS_SECURE=" + SETTINGS_SECURE + ", SETTINGS_SYSTEM="
				+ SETTINGS_SYSTEM + ", SHARED_PREFERENCES=" + SHARED_PREFERENCES + ", STACK_TRACE=" + STACK_TRACE
				+ ", TOTAL_MEM_SIZE=" + TOTAL_MEM_SIZE + ", USER_APP_START_DATE=" + USER_APP_START_DATE
				+ ", USER_CRASH_DATE=" + USER_CRASH_DATE + ", USER_EMAIL=" + USER_EMAIL + "]";
	}
	
	
	
}
