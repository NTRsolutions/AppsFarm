package is.ejb.bl.reporting;

import java.sql.Timestamp;

import javax.ejb.Stateless;

@Stateless
public class LogEntry {
	
	private Timestamp time;
	private String logStatus;
	private String message;
	private String serverName;
	
	public LogEntry(){
		
	}
	
	public LogEntry(Timestamp time, String logStatus, String message,
			String serverName) {
		this.time = time;
		this.logStatus = logStatus;
		this.message = message;
		this.serverName = serverName;
	}
	
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	public String getLogStatus() {
		return logStatus;
	}
	public void setLogStatus(String logStatus) {
		this.logStatus = logStatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
}
