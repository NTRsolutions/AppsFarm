package is.web.services.acra;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIHelper;


@Path("/")
public class AcraService {

	@Inject
	private Logger logger;
	
    @Context
    private UriInfo context;
	
    @Inject
    private APIHelper apiHelper;
    
    @Inject
    private DAOAppUser daoAppUser;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    
	@Path("/report/acra")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveReport(final String json) {

		AcraReport report = apiHelper.getGson().fromJson(json, AcraReport.class);
		logger.info("Report: " + report);
		String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpServletRequest);
		String application = report.getCustomData().getApplication() != null ? report.getCustomData().getApplication()
				: "";
		String type = report.getCustomData().getType() != null ? report.getCustomData().getType() : "FATAL";

		AppUserEntity appUser = null;
		if (report.getCustomData() != null && report.getCustomData().getUserId() != 0) {
			logger.info("Selecting appUser with id: " + report.getCustomData().getUserId());
			appUser = selectAppUser(report.getCustomData().getUserId());
		}
		if (appUser != null) {
			Application.getElasticSearchLogger().indexCrashReport(appUser.getPhoneNumberExtension(),
					appUser.getPhoneNumber(), report.getPhoneModel(), report.getAndroidVersion(), application,
					"" + report.getAppVersionCode(), type, report.getStackTrace(), ipAddress, 
					appUser.getEmail(), appUser.getRewardTypeName(), appUser.getCountryCode(), appUser.getDeviceType(), appUser.getAdvertisingId(), appUser.getIdfa(), appUser.getAgeRange(), appUser.getGender());
			logger.info("Error indexed.");
		} else {
			logger.info("User is null and indexed without user data.");
			Application.getElasticSearchLogger().indexCrashReport("", "", report.getPhoneModel(),
					report.getAndroidVersion(), application, "" + report.getAppVersionCode(), type,
					report.getStackTrace(), ipAddress,
					"", "", "", "", "", "", "", "");
		}

		return Response.accepted().build();
	}
	
	private AppUserEntity selectAppUser(int userId) {
		try {
			return daoAppUser.findById(userId);
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}
	
}
