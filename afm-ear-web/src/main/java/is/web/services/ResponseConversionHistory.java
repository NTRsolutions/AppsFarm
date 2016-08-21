package is.web.services;

import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;

public class ResponseConversionHistory {
	private String status;
	private String code;
	private ConversionHistoryHolder conversionHistoryHolder;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ConversionHistoryHolder getConversionHistoryHolder() {
		return conversionHistoryHolder;
	}
	public void setConversionHistoryHolder(
			ConversionHistoryHolder conversionHistoryHolder) {
		this.conversionHistoryHolder = conversionHistoryHolder;
	}
	
}
