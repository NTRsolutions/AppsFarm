package is.ejb.bl.firebase;

public class FirebaseMessage {
	private FirebaseRequest request;
	private String apiKey;
	public FirebaseRequest getRequest() {
		return request;
	}
	public void setRequest(FirebaseRequest request) {
		this.request = request;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	
}
