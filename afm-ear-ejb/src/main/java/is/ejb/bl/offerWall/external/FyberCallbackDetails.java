package is.ejb.bl.offerWall.external;

public class FyberCallbackDetails {
	private String uid;
	private String sid;
	private String amount;
	private String currencyName;
	private String currencyId;
	private String transId;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getCurrencyId() {
		return currencyId;
	}
	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}
	public String getTransId() {
		return transId;
	}
	public void setTransId(String transId) {
		this.transId = transId;
	}
	@Override
	public String toString() {
		return "FyberCallbackDetails [uid=" + uid + ", sid=" + sid + ", amount=" + amount + ", currencyName="
				+ currencyName + ", currencyId=" + currencyId + ", transId=" + transId + "]";
	}
	
}