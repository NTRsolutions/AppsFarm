package is.ejb.bl.acra;

public class AcraBuildVersion {
	private long INCREMENTAL;
	private double RELEASE;
	private double SDK;
	public long getINCREMENTAL() {
		return INCREMENTAL;
	}
	public void setINCREMENTAL(long iNCREMENTAL) {
		INCREMENTAL = iNCREMENTAL;
	}
	public double getRELEASE() {
		return RELEASE;
	}
	public void setRELEASE(double rELEASE) {
		RELEASE = rELEASE;
	}
	public double getSDK() {
		return SDK;
	}
	public void setSDK(double sDK) {
		SDK = sDK;
	}
	@Override
	public String toString() {
		return "AcraBuildVersion [INCREMENTAL=" + INCREMENTAL + ", RELEASE=" + RELEASE + ", SDK=" + SDK + "]";
	}
	
	
}
