package is.web.services.util;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.Response;

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;

@Path("/")
public class LoggerService {

	private Gson gson;

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private Logger logger;

	@GET
	@Produces("application/json")
	@Path("/v1/log/")
	public String logData(@QueryParam("applicationName") String applicationName,
			@QueryParam("systemInfo") String systemInfo, @QueryParam("miscData") String miscData,
			@QueryParam("title") String title, @QueryParam("content") String content) {

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}

		String dataContent = "ipAddress: " + ipAddress + " applicationName: " + applicationName + " systemInfo: "
				+ systemInfo + " miscData:" + miscData;

		Application.getElasticSearchLogger().indexLog(Application.LOGGER_SERVICE_LOG, -1, LogStatus.OK,
				title + content + " dataContent: " + dataContent);
		logger.info(Application.LOGGER_SERVICE_LOG + " " + LogStatus.OK + " " + title + content + " dataContent: "
				+ dataContent);
		Response response = new Response().getSuccessResponse();
		return gson.toJson(response);

	}


	@Inject
	private DAOAppUser daoAppUser;

}
