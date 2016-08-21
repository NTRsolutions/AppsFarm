package is.ejb.bl.offerProviders.snapdeal.CategoryOffers;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductsEntry implements Cloneable {
	@JsonProperty("sizes")
	private String sizes;
	@JsonProperty("categoryId")
	private Integer categoryId;
	@JsonProperty("link")
	private String link;
	@JsonProperty("mrp")
	private Integer mrp;
	@JsonProperty("subCategoryId")
	private Integer subCategoryId;
	@JsonProperty("id")
	private Long id;
	@JsonProperty("effectivePrice")
	private Integer effectivePrice;
	@JsonProperty("categoryName")
	private String categoryName;
	@JsonProperty("title")
	private String title;
	@JsonProperty("description")
	private String description;
	@JsonProperty("offerPrice")
	private Integer offerPrice;
	@JsonProperty("imageLink")
	private String imageLink;
	@JsonProperty("brand")
	private String brand;
	@JsonProperty("subCategoryName")
	private String subCategoryName;
	@JsonProperty("availability")
	private String availability;

	public String getSizes() {
		return sizes;
	}

	public void setSizes(String sizes) {
		this.sizes = sizes;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Integer getMrp() {
		return mrp;
	}

	public void setMrp(Integer mrp) {
		this.mrp = mrp;
	}

	public Integer getSubCategoryId() {
		return subCategoryId;
	}

	public void setSubCategoryId(Integer subCategoryId) {
		this.subCategoryId = subCategoryId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEffectivePrice() {
		return effectivePrice;
	}

	public void setEffectivePrice(Integer effectivePrice) {
		this.effectivePrice = effectivePrice;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getOfferPrice() {
		return offerPrice;
	}

	public void setOfferPrice(Integer offerPrice) {
		this.offerPrice = offerPrice;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	@Override
	public String toString() {
		return "ProductsEntry [sizes=" + sizes + ", categoryId=" + categoryId + ", link=" + link + ", mrp=" + mrp
				+ ", subCategoryId=" + subCategoryId + ", id=" + id + ", effectivePrice=" + effectivePrice
				+ ", categoryName=" + categoryName + ", title=" + title + ", description=" + description
				+ ", offerPrice=" + offerPrice + ", imageLink=" + imageLink + ", brand=" + brand + ", subCategoryName="
				+ subCategoryName + ", availability=" + availability + "]";
	}

	@Override
	public ProductsEntry clone() {
		ProductsEntry entry = new ProductsEntry();
		if (this.availability != null)
			entry.setAvailability(new String(this.availability));
		if (this.brand != null)
			entry.setBrand(new String(this.brand));
		if (this.categoryId != null)
			entry.setCategoryId(new Integer(this.categoryId));
		if (this.categoryName != null)
			entry.setCategoryName(new String(this.categoryName));
		if (this.description != null)
			entry.setDescription(new String(this.description));
		if (this.effectivePrice != null)
			entry.setEffectivePrice(new Integer(this.effectivePrice));
		if (this.id != null)
			entry.setId(new Long(this.id));
		if (this.imageLink != null)
			entry.setImageLink(new String(this.imageLink));
		if (this.link != null)
			entry.setLink(new String(this.link));
		if (this.mrp != null)
			entry.setMrp(new Integer(this.mrp));
		if (this.offerPrice != null)
			entry.setOfferPrice(new Integer(this.offerPrice));
		if (this.sizes != null)
			entry.setSizes(new String(this.sizes));
		if (this.subCategoryId != null)
			entry.setSubCategoryId(new Integer(this.subCategoryId));
		if (this.subCategoryName != null)
			entry.setSubCategoryName(new String(this.subCategoryName));
		if (this.title != null)
			entry.setTitle(new String(this.title));

		return entry;
	}

}
