package is.web.services.wall;

import is.web.services.APIResponse;

public class ClickResponse extends APIResponse {
	private String internalTransactionId;
	private String url;
	public String getInternalTransactionId() {
		return internalTransactionId;
	}
	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
