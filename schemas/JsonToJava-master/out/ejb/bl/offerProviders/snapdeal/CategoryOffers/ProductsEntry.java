package ejb.bl.offerProviders.snapdeal.CategoryOffers;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class ProductsEntry {
	@JsonProperty("sizes") private String sizes;
	@JsonProperty("categoryId") private Integer categoryId;
	@JsonProperty("link") private String link;
	@JsonProperty("mrp") private Integer mrp;
	@JsonProperty("subCategoryId") private Integer subCategoryId;
	@JsonProperty("id") private Long id;
	@JsonProperty("effectivePrice") private Integer effectivePrice;
	@JsonProperty("categoryName") private String categoryName;
	@JsonProperty("title") private String title;
	@JsonProperty("description") private String description;
	@JsonProperty("offerPrice") private Integer offerPrice;
	@JsonProperty("imageLink") private String imageLink;
	@JsonProperty("brand") private String brand;
	@JsonProperty("subCategoryName") private String subCategoryName;
	@JsonProperty("availability") private String availability;
}
