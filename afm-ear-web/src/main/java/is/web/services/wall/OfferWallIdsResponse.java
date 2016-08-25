package is.web.services.wall;

import java.util.List;

import is.web.services.APIResponse;

public class OfferWallIdsResponse extends APIResponse {
	private List<Integer> ids;

	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}
	
}
