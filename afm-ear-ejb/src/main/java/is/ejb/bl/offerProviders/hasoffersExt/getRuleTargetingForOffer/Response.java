package is.ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class Response {
	@JsonProperty("errorMessage") private Object errorMessage;
	@JsonProperty("errors") private List<Object> errors;
	@JsonProperty("httpStatus") private Integer httpStatus;
	@JsonProperty("status") private Integer status;
	@JsonProperty("data") private List<DataEntry> data;
	public Object getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(Object errorMessage) {
		this.errorMessage = errorMessage;
	}
	public List<Object> getErrors() {
		return errors;
	}
	public void setErrors(List<Object> errors) {
		this.errors = errors;
	}
	public Integer getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public List<DataEntry> getData() {
		return data;
	}
	public void setData(List<DataEntry> data) {
		this.data = data;
	}
	
}
