package is.ejb.bl.acra;

public class AcraCustomData {
	private String MULTILINE;
	private String NO_VALUE;
	public String getMULTILINE() {
		return MULTILINE;
	}
	public void setMULTILINE(String mULTILINE) {
		MULTILINE = mULTILINE;
	}
	public String getNO_VALUE() {
		return NO_VALUE;
	}
	public void setNO_VALUE(String nO_VALUE) {
		NO_VALUE = nO_VALUE;
	}
	@Override
	public String toString() {
		return "AcraCustomData [MULTILINE=" + MULTILINE + ", NO_VALUE=" + NO_VALUE + "]";
	}
	
}
