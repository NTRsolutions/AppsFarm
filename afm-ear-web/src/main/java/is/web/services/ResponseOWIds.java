package is.web.services;

import java.sql.Timestamp;

public class ResponseOWIds {
	private String networkName;
	private String status;
	private String code;
	private String errorMessage="";
	private int[] idList;

	public int[] getIdList() {
		return idList;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


	public void setIdList(int[] idList) {
		this.idList = idList;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	
}
