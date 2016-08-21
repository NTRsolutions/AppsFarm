package is.ejb.bl.system.support.donky;

import com.google.gson.annotations.Expose;


public class ZendeskResponse {
	@Expose
	private int ticketId;
	@Expose
	private String status;
	@Expose
	private String fromAccount;
	@Expose
	private String fromUser;
	@Expose
	private String message;
	@Expose
	private String messageFormatted;
	@Expose
	private String updateTime;
	public int getTicketId() {
		return ticketId;
	}
	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFromAccount() {
		return fromAccount;
	}
	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
	
	public String getMessageFormatted() {
		return messageFormatted;
	}
	public void setMessageFormatted(String messageFormatted) {
		this.messageFormatted = messageFormatted;
	}
	@Override
	public String toString() {
		return "ZendeskResponse [ticketId=" + ticketId + ", status=" + status
				+ ", fromAccount=" + fromAccount + ", fromUser=" + fromUser
				+ ", message=" + message + ", messageFormatted="
				+ messageFormatted + ", updateTime=" + updateTime + "]";
	}
	

	
	
	
}
