package is.ejb.bl.offerProviders.snapdeal;

import java.util.List;

public class SnapdealCategoryCommision {

	private String superCategory;
	private List<String> categories;
	private Double commision;
	public String getSuperCategory() {
		return superCategory;
	}
	public void setSuperCategory(String superCategory) {
		this.superCategory = superCategory;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public Double getCommision() {
		return commision;
	}
	public void setCommision(Double commision) {
		this.commision = commision;
	}
	
	
	
}
