package is.ejb.bl.system.support.donky;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DonkyForwardRequest {

	@XmlElement
	private String DonkyAccountId;
	@XmlElement
	private String ApplicationSpaceId;
	@XmlElement
	private Notification []  Notifications;
	@Override
	
	
	
	public String toString() {
		return "DonkyRequest [DonkyAccountId=" + DonkyAccountId
				+ ", ApplicationSpaceId=" + ApplicationSpaceId
				+ ", Notifications=" + Arrays.toString(Notifications) + "]";
	}
	public String getDonkyAccountId() {
		return DonkyAccountId;
	}
	public void setDonkyAccountId(String donkyAccountId) {
		DonkyAccountId = donkyAccountId;
	}
	public String getApplicationSpaceId() {
		return ApplicationSpaceId;
	}
	public void setApplicationSpaceId(String applicationSpaceId) {
		ApplicationSpaceId = applicationSpaceId;
	}
	public Notification[] getNotifications() {
		return Notifications;
	}
	public void setNotifications(Notification[] notifications) {
		Notifications = notifications;
	}

}
