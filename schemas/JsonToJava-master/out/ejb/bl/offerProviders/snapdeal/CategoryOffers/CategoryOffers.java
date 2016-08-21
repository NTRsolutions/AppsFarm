package ejb.bl.offerProviders.snapdeal.CategoryOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class CategoryOffers {
	@JsonProperty("nextUrl") private String nextUrl;
	@JsonProperty("validTill") private Long validTill;
	@JsonProperty("products") private List<ProductsEntry> products;
}
