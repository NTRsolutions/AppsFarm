package is.ejb.bl.offerProviders.snapdeal.CategoryOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryOffers {
	@JsonProperty("nextUrl") private String nextUrl;
	@JsonProperty("validTill") private Long validTill;
	@JsonProperty("products") private List<ProductsEntry> products;
	public String getNextUrl() {
		return nextUrl;
	}
	public void setNextUrl(String nextUrl) {
		this.nextUrl = nextUrl;
	}
	public Long getValidTill() {
		return validTill;
	}
	public void setValidTill(Long validTill) {
		this.validTill = validTill;
	}
	public List<ProductsEntry> getProducts() {
		return products;
	}
	public void setProducts(List<ProductsEntry> products) {
		this.products = products;
	}
	
	
}
