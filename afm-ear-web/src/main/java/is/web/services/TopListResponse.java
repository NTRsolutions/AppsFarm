package is.web.services;

import java.util.List;

public class TopListResponse extends Response {

	private List<ToplistRow> topListData;

	public List<ToplistRow> getTopListData() {
		return topListData;
	}

	public void setTopListData(List<ToplistRow> topListData) {
		this.topListData = topListData;
	}
	
	
}
