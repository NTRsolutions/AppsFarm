package is.web.beans.system;

public class WalletPayoutCarrierModelRow {
	private String name;
	private int id;
	private String countryCode;
	private String carrierName;
	private String carrierCurrency;
	private String minValueToPayout;
	private String payoutGap;
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getCarrierCurrency() {
		return carrierCurrency;
	}
	public void setCarrierCurrency(String carrierCurrency) {
		this.carrierCurrency = carrierCurrency;
	}
	public String getMinValueToPayout() {
		return minValueToPayout;
	}
	public void setMinValueToPayout(String minValueToPayout) {
		this.minValueToPayout = minValueToPayout;
	}
	public String getPayoutGap() {
		return payoutGap;
	}
	public void setPayoutGap(String payoutGap) {
		this.payoutGap = payoutGap;
	}

	
	

}
