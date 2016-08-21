package is.ejb.bl.offerProviders.minimob.getOfferById;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class CreativesEntry {
	@JsonProperty("locale") private String locale;
	@JsonProperty("dimensions") private String dimensions;
	@JsonProperty("Id") private String Id;
	@JsonProperty("mimeType") private String mimeType;
	@JsonProperty("previewUrl") private String previewUrl;
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getDimensions() {
		return dimensions;
	}
	public void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getPreviewUrl() {
		return previewUrl;
	}
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}
	
}
