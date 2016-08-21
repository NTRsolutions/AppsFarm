package is.ejb.bl.system.support.donky;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
@XmlRootElement
public class Notification {

	@XmlElement private String NotificationId;
	@XmlElement private  String Type;
	@XmlElement private  String SequenceGroup;
	@XmlElement private  String SequenceEntityId;
	@XmlElement private  String Data;
	@XmlElement private  String CreatedOn;
	
	
	
	public String getNotificationId() {
		return NotificationId;
	}



	public void setNotificationId(String notificationId) {
		NotificationId = notificationId;
	}



	public String getType() {
		return Type;
	}



	public void setType(String type) {
		Type = type;
	}



	public String getSequenceGroup() {
		return SequenceGroup;
	}



	public void setSequenceGroup(String sequenceGroup) {
		SequenceGroup = sequenceGroup;
	}



	public String getSequenceEntityId() {
		return SequenceEntityId;
	}



	public void setSequenceEntityId(String sequenceEntityId) {
		SequenceEntityId = sequenceEntityId;
	}



	public String getData() {
		return Data;
	}



	public void setData(String data) {
		Data = data;
	}



	public String getCreatedOn() {
		return CreatedOn;
	}



	public void setCreatedOn(String createdOn) {
		CreatedOn = createdOn;
	}



	@Override
	public String toString() {
		return "Notification [NotificationId=" + NotificationId + ", Type="
				+ Type + ", SequenceGroup=" + SequenceGroup
				+ ", SequenceEntityId=" + SequenceEntityId + ", Data=" + Data
				+ ", CreatedOn=" + CreatedOn + "]";
	}
	
	
}
