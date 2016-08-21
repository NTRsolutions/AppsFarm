package is.ejb.bl.system.support.donky;

import com.google.gson.annotations.Expose;

public class Asset {
	@Expose
	private String AssetId;
	
	@Expose
	private String MimeType;
	
	@Expose
	private String Name;
	
	@Expose
	private String AssetUrl;

	public String getAssetId() {
		return AssetId;
	}

	public void setAssetId(String assetId) {
		AssetId = assetId;
	}

	public String getMimeType() {
		return MimeType;
	}

	public void setMimeType(String mimeType) {
		MimeType = mimeType;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getAssetUrl() {
		return AssetUrl;
	}

	public void setAssetUrl(String assetUrl) {
		AssetUrl = assetUrl;
	}
	
	
}
