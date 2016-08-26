package is.ejb.bl.acra;

public class AcraBuild {
	private String BOARD;
	private String BRAND;
	private String DEVICE;
	private String DISPLAY;
	private String FINGERPRINT;
	private String HOST;
	private String ID;
	private String MODEL;
	private String PRODUCT;
	private String TAGS;
	private long TIME;
	private String USER;
	private AcraBuildVersion BUILD;
	public String getBOARD() {
		return BOARD;
	}
	public void setBOARD(String bOARD) {
		BOARD = bOARD;
	}
	public String getBRAND() {
		return BRAND;
	}
	public void setBRAND(String bRAND) {
		BRAND = bRAND;
	}
	public String getDEVICE() {
		return DEVICE;
	}
	public void setDEVICE(String dEVICE) {
		DEVICE = dEVICE;
	}
	public String getDISPLAY() {
		return DISPLAY;
	}
	public void setDISPLAY(String dISPLAY) {
		DISPLAY = dISPLAY;
	}
	public String getFINGERPRINT() {
		return FINGERPRINT;
	}
	public void setFINGERPRINT(String fINGERPRINT) {
		FINGERPRINT = fINGERPRINT;
	}
	public String getHOST() {
		return HOST;
	}
	public void setHOST(String hOST) {
		HOST = hOST;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getMODEL() {
		return MODEL;
	}
	public void setMODEL(String mODEL) {
		MODEL = mODEL;
	}
	public String getPRODUCT() {
		return PRODUCT;
	}
	public void setPRODUCT(String pRODUCT) {
		PRODUCT = pRODUCT;
	}
	public String getTAGS() {
		return TAGS;
	}
	public void setTAGS(String tAGS) {
		TAGS = tAGS;
	}
	
	public long getTIME() {
		return TIME;
	}
	public void setTIME(long tIME) {
		TIME = tIME;
	}
	public String getUSER() {
		return USER;
	}
	public void setUSER(String uSER) {
		USER = uSER;
	}
	public AcraBuildVersion getBUILD() {
		return BUILD;
	}
	public void setBUILD(AcraBuildVersion bUILD) {
		BUILD = bUILD;
	}
	@Override
	public String toString() {
		return "AcraBuild [BOARD=" + BOARD + ", BRAND=" + BRAND + ", DEVICE=" + DEVICE + ", DISPLAY=" + DISPLAY
				+ ", FINGERPRINT=" + FINGERPRINT + ", HOST=" + HOST + ", ID=" + ID + ", MODEL=" + MODEL + ", PRODUCT="
				+ PRODUCT + ", TAGS=" + TAGS + ", TIME=" + TIME + ", USER=" + USER + ", BUILD=" + BUILD + "]";
	}
	
}
