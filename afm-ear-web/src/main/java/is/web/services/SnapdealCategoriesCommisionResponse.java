package is.web.services;

import is.ejb.bl.offerProviders.snapdeal.SnapdealCategoryCommision;

import java.util.List;

public class SnapdealCategoriesCommisionResponse extends Response {

	private List<SnapdealCategoryCommision> commisionList;

	public List<SnapdealCategoryCommision> getCommisionList() {
		return commisionList;
	}

	public void setCommisionList(List<SnapdealCategoryCommision> commisionList) {
		this.commisionList = commisionList;
	}
	
	
}
