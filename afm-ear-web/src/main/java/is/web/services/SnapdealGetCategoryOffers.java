package is.web.services;

import is.ejb.bl.offerProviders.personaly.getOffers.OffersEntry;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.uiStateManager.UIStateHolder;
import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;
import java.util.ArrayList;

public class SnapdealGetCategoryOffers {
	private String status;
	private String code;
	private String requestedCategoryName;
	private int requestedPage;
	private int requestedNumberOfOffers;
	
	private ArrayList<ProductsEntry> categoryOffers = new ArrayList<ProductsEntry>();
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRequestedCategoryName() {
		return requestedCategoryName;
	}
	public void setRequestedCategoryName(String requestedCategoryName) {
		this.requestedCategoryName = requestedCategoryName;
	}
	public int getRequestedPage() {
		return requestedPage;
	}
	public void setRequestedPage(int requestedPage) {
		this.requestedPage = requestedPage;
	}
	public int getRequestedNumberOfOffers() {
		return requestedNumberOfOffers;
	}
	public void setRequestedNumberOfOffers(int requestedNumberOfOffers) {
		this.requestedNumberOfOffers = requestedNumberOfOffers;
	}
	public ArrayList<ProductsEntry> getCategoryOffers() {
		return categoryOffers;
	}
	public void setCategoryOffers(ArrayList<ProductsEntry> categoryOffers) {
		this.categoryOffers = categoryOffers;
	}
	
}
