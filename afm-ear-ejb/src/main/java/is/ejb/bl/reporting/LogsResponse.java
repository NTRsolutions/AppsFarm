package is.ejb.bl.reporting;

import java.util.ArrayList;
import java.util.List;

public class LogsResponse {
	private String status = "";
	private String id = "";
	private List<LogEntry> logs = new ArrayList<LogEntry>();

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public void setLogs(List<LogEntry> logs) {
		this.logs = logs;
	}
}
