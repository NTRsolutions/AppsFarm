package is.web.services.acra;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import is.ejb.bl.acra.AcraReport;
import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;


@Path("/")
public class AcraService {

	@Inject
	private Logger logger;
	
	@Path("/report/acra")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(final AcraReport acraReport) {
		logger.info("Received acra report:" + acraReport);
		Application.getElasticSearchLogger().indexLog(Application.ACRA_ERROR_REPORT, -1, LogStatus.OK,
				Application.ACRA_ERROR_REPORT + " Received acra report: " +acraReport.toString());
		return Response.accepted().build();
	}
}
