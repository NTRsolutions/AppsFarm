package is.web.services;

import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpServletRequest;

@ManagedBean
public class APIHelper {

	public String getIpAddressFromHttpRequest(HttpServletRequest httpRequest){
		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}
		return ipAddress;
	}
}
