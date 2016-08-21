package is.ejb.bl.offerProviders.snapdeal;

import java.util.List;

public class SnapdealReportResponse {

	private List<SnapdealProductDetail> productDetails;
	private String nextURL;
	@Override
	public String toString() {
		return "SnapdealReportResponse [productDetails=" + productDetails + ", nextURL=" + nextURL + "]";
	}
	public List<SnapdealProductDetail> getProductDetails() {
		return productDetails;
	}
	public void setProductDetails(List<SnapdealProductDetail> productDetails) {
		this.productDetails = productDetails;
	}
	public String getNextURL() {
		return nextURL;
	}
	public void setNextURL(String nextURL) {
		this.nextURL = nextURL;
	}
	
	
}
