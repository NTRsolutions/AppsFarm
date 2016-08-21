package is.ejb.bl.wallet;

public class WalletTransactionZendeskEntity {

	private String status;
	private long ticketId;
	private String updateTime;
	private String fromAccount;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getTicketId() {
		return ticketId;
	}
	public void setTicketId(long ticketId) {
		this.ticketId = ticketId;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getFromAccount() {
		return fromAccount;
	}
	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}
	@Override
	public String toString() {
		return "WalletTransactionZendeskEntity [status=" + status
				+ ", ticketId=" + ticketId + ", updateTime=" + updateTime
				+ ", fromAccount=" + fromAccount + "]";
	}
	
	
}
