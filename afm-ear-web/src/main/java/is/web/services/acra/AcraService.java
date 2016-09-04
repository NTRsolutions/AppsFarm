package is.web.services.acra;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;


@Path("/")
public class AcraService {

	@Inject
	private Logger logger;
	
    @Context
    private UriInfo context;
	
	@Path("/report/acra")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveReport(final String json) {
		Application.getElasticSearchLogger().indexLog(Application.ACRA_ERROR_REPORT, -1, LogStatus.OK,
				Application.ACRA_ERROR_REPORT + "\n\n" +json);
		return Response.accepted().build();
	}
}
