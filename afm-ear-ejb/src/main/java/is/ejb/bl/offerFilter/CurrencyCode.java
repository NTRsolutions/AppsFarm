package is.ejb.bl.offerFilter;

public class CurrencyCode {
	private String code;
	private double payoutTreshold; //we reject all offers below this value
	private double instantRewardTreshold;  //we disable instant rewards below this value
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public double getPayoutTreshold() {
		return payoutTreshold;
	}
	public void setPayoutTreshold(double payoutTreshold) {
		this.payoutTreshold = payoutTreshold;
	}
	public double getInstantRewardTreshold() {
		return instantRewardTreshold;
	}
	public void setInstantRewardTreshold(double instantRewardTreshold) {
		this.instantRewardTreshold = instantRewardTreshold;
	}
	
}
