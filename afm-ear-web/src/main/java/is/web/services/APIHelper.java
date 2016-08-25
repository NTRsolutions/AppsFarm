package is.web.services;

import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;

@ManagedBean
public class APIHelper {

	@Inject
	private Logger logger;
	
	private Gson gson = new Gson();
	
	public String getIpAddressFromHttpRequest(HttpServletRequest httpRequest){
		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}
		return ipAddress;
	}
	
	public void setupFailedResponseForError(APIResponse response, RespCodesEnum code) {
		logger.info("Setup failed response for error: " + code);
		response.setStatus(RespStatusEnum.FAILED);
		response.setCode(code);
	}

	public void setupSuccessResponse(APIResponse response) {
		logger.info("Setup success response");
		response.setStatus(RespStatusEnum.SUCCESS);
		response.setCode(RespCodesEnum.OK);
	}
	
	public Gson getGson(){
		return gson;
	}
	
}
