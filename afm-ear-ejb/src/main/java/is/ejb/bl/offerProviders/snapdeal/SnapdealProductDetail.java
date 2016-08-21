package is.ejb.bl.offerProviders.snapdeal;

import java.sql.Timestamp;

public class SnapdealProductDetail {
	private String product;
	private String category;
	private long orderCode;
	private int quantity;
	private double price;
	private double sale;
	private double commisionRate;
	private double commissionEarned;
	private Timestamp dateTime;
	private String affiliateSubId1;
	private String affiliateSubId2;	
	private String userType;
	private String deviceType;
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(long orderCode) {
		this.orderCode = orderCode;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getSale() {
		return sale;
	}
	public void setSale(double sale) {
		this.sale = sale;
	}
	public double getCommisionRate() {
		return commisionRate;
	}
	public void setCommisionRate(double commisionRate) {
		this.commisionRate = commisionRate;
	}
	public double getCommissionEarned() {
		return commissionEarned;
	}
	public void setCommissionEarned(double commissionEarned) {
		this.commissionEarned = commissionEarned;
	}
	public Timestamp getDateTime() {
		return dateTime;
	}
	public void setDateTime(Timestamp dateTime) {
		this.dateTime = dateTime;
	}
	public String getAffiliateSubId1() {
		return affiliateSubId1;
	}
	public void setAffiliateSubId1(String affiliateSubId1) {
		this.affiliateSubId1 = affiliateSubId1;
	}
	public String getAffiliateSubId2() {
		return affiliateSubId2;
	}
	public void setAffiliateSubId2(String affiliateSubId2) {
		this.affiliateSubId2 = affiliateSubId2;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	@Override
	public String toString() {
		return "SnapdealProductDetail [product=" + product + ", category=" + category + ", orderCode=" + orderCode
				+ ", quantity=" + quantity + ", price=" + price + ", sale=" + sale + ", commisionRate=" + commisionRate
				+ ", commissionEarned=" + commissionEarned + ", dateTime=" + dateTime + ", affiliateSubId1="
				+ affiliateSubId1 + ", affiliateSubId2=" + affiliateSubId2 + ", userType=" + userType + ", deviceType="
				+ deviceType + "]";
	}
	
	
	
	
}
