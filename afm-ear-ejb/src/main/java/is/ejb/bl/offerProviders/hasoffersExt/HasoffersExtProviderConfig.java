package is.ejb.bl.offerProviders.hasoffersExt;

public class HasoffersExtProviderConfig {
	private String networkId = ""; 
	private String categoryName = "";
	private String networkToken = "";
	private String affiliateId;
	private long serviceQueryInterval; //in ms
	
	public String getNetworkId() {
		return networkId;
	}
	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}
	public String getNetworkToken() {
		return networkToken;
	}
	public void setNetworkToken(String networkToken) {
		this.networkToken = networkToken;
	}
	public long getServiceQueryInterval() {
		return serviceQueryInterval;
	}
	public void setServiceQueryInterval(long serviceQueryInterval) {
		this.serviceQueryInterval = serviceQueryInterval;
	}
	public String getAffiliateId() {
		return affiliateId;
	}
	public void setAffiliateId(String affiliateId) {
		this.affiliateId = affiliateId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
}
