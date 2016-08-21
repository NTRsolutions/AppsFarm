package is.ejb.bl.reporting;

import javax.ejb.Stateless;

@Stateless
public abstract class AbstractEventLog implements IEventLog {
	
	protected static final String DEFAULT_STRING_VALUE = "NO_VALUE";
	
	protected String removeCommas(String field){
		if(field != null && !field.isEmpty()) {
			return field.replaceAll(",", " ");
		}
		return DEFAULT_STRING_VALUE;
	}
	
	protected String formatIpAddress(String ipAddress){
		if(ipAddress != null && !ipAddress.isEmpty()) {
			return ipAddress.replaceAll(" ", "").replaceAll(",", "-");
		}
		return DEFAULT_STRING_VALUE;
	}
	
	protected String formatMiscData(String miscData){
		if(miscData != null && !miscData.isEmpty()) {
			return miscData.replaceAll(",", "#");
		}
		return DEFAULT_STRING_VALUE;
	}
	
	protected String formatSupportQuestion(String supportQuestion){
		if(supportQuestion != null && !supportQuestion.isEmpty()) {
			return supportQuestion.replaceAll(",", "#");
		}
		return DEFAULT_STRING_VALUE;
	}
	
	protected String validate(String field){
		if(field != null && !field.isEmpty()){
			return field;
		} else {
			return DEFAULT_STRING_VALUE;
		}
	}

}
