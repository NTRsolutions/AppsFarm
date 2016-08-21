package is.ejb.bl.cloudtraxConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AllowedDomains {

	List<String> allowedDomains = new ArrayList<String>();

	public List<String> getAllowedDomains() {
		return allowedDomains;
	}

	public void setAllowedDomains(List<String> allowedDomains) {
		this.allowedDomains = allowedDomains;
	}
	
}
