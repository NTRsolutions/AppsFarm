package is.ejb.bl.offerProviders.hasoffersExt.getOfferFileInfo;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Data {
	@JsonProperty("pageCount") private Integer pageCount;
	@JsonProperty("count") private Integer count;
	@JsonProperty("page") private Integer page;
	@JsonProperty("dataValue") private List<DataEntry> data;
	@JsonProperty("current") private Integer current;
	public Integer getPageCount() {
		return pageCount;
	}
	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public List<DataEntry> getData() {
		return data;
	}
	public void setData(List<DataEntry> data) {
		this.data = data;
	}
	public Integer getCurrent() {
		return current;
	}
	public void setCurrent(Integer current) {
		this.current = current;
	}
	
}
