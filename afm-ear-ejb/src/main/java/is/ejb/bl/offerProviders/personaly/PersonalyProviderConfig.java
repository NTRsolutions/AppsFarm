package is.ejb.bl.offerProviders.personaly;

public class PersonalyProviderConfig {
	private String appHash = "";
	private int recordsPerPage = 1000; //-1 - pull all

	public int getRecordsPerPage() {
		return recordsPerPage;
	}
	public void setRecordsPerPage(int recordsPerPage) {
		this.recordsPerPage = recordsPerPage;
	}
	public String getAppHash() {
		return appHash;
	}
	public void setAppHash(String appHash) {
		this.appHash = appHash;
	}
	
}
