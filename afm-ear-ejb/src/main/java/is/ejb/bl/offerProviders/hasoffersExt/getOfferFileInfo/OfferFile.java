package is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class OfferFile {
	@JsonProperty("offer_id") private Integer offer_id;
	@JsonProperty("account_id") private Integer account_id;
	@JsonProperty("status") private String status;
	@JsonProperty("flash_vars") private Object flash_vars;
	@JsonProperty("width") private Integer width;
	@JsonProperty("display") private String display;
	@JsonProperty("code") private Object code;
	@JsonProperty("type") private String type;
	@JsonProperty("interface") private String interfaceStr;
	@JsonProperty("url") private String url;
	@JsonProperty("modified") private String modified;
	@JsonProperty("size") private Integer size;
	@JsonProperty("id") private Integer id;
	@JsonProperty("thumbnail") private String thumbnail;
	@JsonProperty("height") private Integer height;
	@JsonProperty("created") private String created;
	@JsonProperty("is_private") private Integer is_private;
	@JsonProperty("filename") private String filename;
	public Integer getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(Integer offer_id) {
		this.offer_id = offer_id;
	}
	public Integer getAccount_id() {
		return account_id;
	}
	public void setAccount_id(Integer account_id) {
		this.account_id = account_id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Object getFlash_vars() {
		return flash_vars;
	}
	public void setFlash_vars(Object flash_vars) {
		this.flash_vars = flash_vars;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public Object getCode() {
		return code;
	}
	public void setCode(Object code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInterfaceStr() {
		return interfaceStr;
	}
	public void setInterfaceStr(String interfaceStr) {
		this.interfaceStr = interfaceStr;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public Integer getIs_private() {
		return is_private;
	}
	public void setIs_private(Integer is_private) {
		this.is_private = is_private;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	
}
