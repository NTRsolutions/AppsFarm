package is.web.services;

import java.util.HashMap;

public class APIRequestDetails {
	private String systemInfo;
	private String applicationInfo;
	private HashMap<String,Object> parameters;
	public String getSystemInfo() {
		return systemInfo;
	}
	public void setSystemInfo(String systemInfo) {
		this.systemInfo = systemInfo;
	}
	public String getApplicationInfo() {
		return applicationInfo;
	}
	public void setApplicationInfo(String applicationInfo) {
		this.applicationInfo = applicationInfo;
	}
	public HashMap<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}
	@Override
	public String toString() {
		String details =  "\n** API REQUEST DETAILS: **";
		details += "\n-> systemInfo: "+systemInfo;
		details += "\n-> applicationInfo: " + applicationInfo;
		if (parameters != null){
			for (String parameterKey : parameters.keySet()){
				details += "\n-> " + parameterKey + " : " +parameters.get(parameterKey);
			}
		}
		
		return details;
				
	}
	
	
	
}
