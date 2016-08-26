package is.ejb.bl.acra;

public class AcraEnvironment {
	private String getDataDirectory;
	private String getDownloadCacheDirectory;
	private String getExternalStorageDirectory;
	private String getExternalStorageState;
	private String getRootDirectory;
	public String getGetDataDirectory() {
		return getDataDirectory;
	}
	public void setGetDataDirectory(String getDataDirectory) {
		this.getDataDirectory = getDataDirectory;
	}
	public String getGetDownloadCacheDirectory() {
		return getDownloadCacheDirectory;
	}
	public void setGetDownloadCacheDirectory(String getDownloadCacheDirectory) {
		this.getDownloadCacheDirectory = getDownloadCacheDirectory;
	}
	public String getGetExternalStorageDirectory() {
		return getExternalStorageDirectory;
	}
	public void setGetExternalStorageDirectory(String getExternalStorageDirectory) {
		this.getExternalStorageDirectory = getExternalStorageDirectory;
	}
	public String getGetExternalStorageState() {
		return getExternalStorageState;
	}
	public void setGetExternalStorageState(String getExternalStorageState) {
		this.getExternalStorageState = getExternalStorageState;
	}
	public String getGetRootDirectory() {
		return getRootDirectory;
	}
	public void setGetRootDirectory(String getRootDirectory) {
		this.getRootDirectory = getRootDirectory;
	}
	@Override
	public String toString() {
		return "AcraEnvironment [getDataDirectory=" + getDataDirectory + ", getDownloadCacheDirectory="
				+ getDownloadCacheDirectory + ", getExternalStorageDirectory=" + getExternalStorageDirectory
				+ ", getExternalStorageState=" + getExternalStorageState + ", getRootDirectory=" + getRootDirectory
				+ "]";
	}
	
	
}
