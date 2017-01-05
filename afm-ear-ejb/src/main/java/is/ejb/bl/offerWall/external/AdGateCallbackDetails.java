package is.ejb.bl.offerWall.external;

public class AdGateCallbackDetails {
	private String offerId;
	private String offerName;
	private String affiliateId;
	private String source;
	private String s1;
	private String transactionId;
	private String sessionIp;
	private String date;
	private String time;
	private String dateTime;
	private String ran;
	private String payout;
	private String status;
	private String points;
	private String vcTitle;
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getOfferName() {
		return offerName;
	}
	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}
	public String getAffiliateId() {
		return affiliateId;
	}
	public void setAffiliateId(String affiliateId) {
		this.affiliateId = affiliateId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getS1() {
		return s1;
	}
	public void setS1(String s1) {
		this.s1 = s1;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getSessionIp() {
		return sessionIp;
	}
	public void setSessionIp(String sessionIp) {
		this.sessionIp = sessionIp;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getRan() {
		return ran;
	}
	public void setRan(String ran) {
		this.ran = ran;
	}
	public String getPayout() {
		return payout;
	}
	public void setPayout(String payout) {
		this.payout = payout;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPoints() {
		return points;
	}
	public void setPoints(String points) {
		this.points = points;
	}
	public String getVcTitle() {
		return vcTitle;
	}
	public void setVcTitle(String vcTitle) {
		this.vcTitle = vcTitle;
	}
	@Override
	public String toString() {
		return "AdGateCallbackDetails [offerId=" + offerId + ", offerName=" + offerName + ", affiliateId=" + affiliateId
				+ ", source=" + source + ", s1=" + s1 + ", transactionId=" + transactionId + ", sessionIp=" + sessionIp
				+ ", date=" + date + ", time=" + time + ", dateTime=" + dateTime + ", ran=" + ran + ", payout=" + payout
				+ ", status=" + status + ", points=" + points + ", vcTitle=" + vcTitle + "]";
	}
	
	
}
