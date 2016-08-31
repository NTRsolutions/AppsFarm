package is.ejb.bl.firebase;

public class FirebaseRequest {

	private String to;
	private FirebaseNotification notification;
	private int time_to_live;
	private boolean delay_while_idle;
	private int priority;
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public FirebaseNotification getNotification() {
		return notification;
	}
	public void setNotification(FirebaseNotification notification) {
		this.notification = notification;
	}
	public int getTime_to_live() {
		return time_to_live;
	}
	public void setTime_to_live(int time_to_live) {
		this.time_to_live = time_to_live;
	}
	public boolean isDelay_while_idle() {
		return delay_while_idle;
	}
	public void setDelay_while_idle(boolean delay_while_idle) {
		this.delay_while_idle = delay_while_idle;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	@Override
	public String toString() {
		return "FirebaseRequest [to=" + to + ", notification=" + notification + ", time_to_live=" + time_to_live
				+ ", delay_while_idle=" + delay_while_idle + ", priority=" + priority + "]";
	}
	
	
	
}
