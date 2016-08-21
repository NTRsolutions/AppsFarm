package is.ejb.bl.monitoring.alert;

import java.util.Date;

public class Alert {
	
	private String id;
	private String timestamp;
	private Date date;
	private Date eventDateStart;
	private Date eventDateEnd;
	private String sn;
	private String alertType;
	private String alertMessage;
	long eventId;
	private String styleCSS;
	private String alertStatus;
	private String monitoredParameter;
	private int deviceProfileId;
	
	public Alert(String id, long eventId, String sn, Date date, String timestamp, String alertType, String alertStatus, String alertMessage,
			Date eventDateStartRef, Date eventDateEndRef,
			String monitoredParameter,
			int deviceProfileIdRef) {
		this.id = id;
		this.eventId = eventId;
		this.sn = sn;
		this.date = date;
		this.timestamp = timestamp;
		this.alertType = alertType;
		this.alertMessage = alertMessage;
		this.alertStatus = alertStatus;
		this.eventDateStart = eventDateStartRef;
		this.eventDateEnd = eventDateEndRef;
		this.monitoredParameter = monitoredParameter;
		this.deviceProfileId = deviceProfileIdRef;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	public String getAlertMessage() {
		return alertMessage;
	}
	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String getStyleCSS() {
		return styleCSS;
	}

	public void setStyleCSS(String styleCSS) {
		this.styleCSS = styleCSS;
	}

	public String getAlertStatus() {
		return alertStatus;
	}

	public void setAlertStatus(String alertStatus) {
		this.alertStatus = alertStatus;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getEventDateStart() {
		return eventDateStart;
	}

	public void setEventDateStart(Date eventDateStart) {
		this.eventDateStart = eventDateStart;
	}

	public Date getEventDateEnd() {
		return eventDateEnd;
	}

	public void setEventDateEnd(Date eventDateEnd) {
		this.eventDateEnd = eventDateEnd;
	}

	public String getMonitoredParameter() {
		return monitoredParameter;
	}

	public void setMonitoredParameter(String monitoredParameter) {
		this.monitoredParameter = monitoredParameter;
	}

	public int getDeviceProfileId() {
		return deviceProfileId;
	}

	public void setDeviceProfileId(int deviceProfileId) {
		this.deviceProfileId = deviceProfileId;
	}
	
}
