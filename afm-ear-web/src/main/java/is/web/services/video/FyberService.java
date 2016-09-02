package is.web.services.video;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.video.VideoCallbackData;
import is.ejb.bl.video.VideoManager;
import is.web.services.APIHelper;

@Path("/")
public class FyberService {

	@Context
	private HttpServletRequest httpRequest;
	
	@Inject
	private APIHelper apiHelper;
	
	@Inject
	private VideoManager videoManager;
	
	@Inject
	private Logger logger;
	@Path("/video/fyber/reward")
	@GET
	public Response rewardCallback(@Context UriInfo ui,@QueryParam("uid") String uid,@QueryParam("sid") String sid, @QueryParam("amount") int amount,
			@QueryParam("currency_id") String currencyId, @QueryParam("currency_name") String currencyName,
			@QueryParam("pub1") String userId,@QueryParam("pub2") String username) {

		System.out.println(ui.getQueryParameters().toString());
		System.out.println("PUB1: " + userId);
		System.out.println(" PUB2:" + username);
		VideoCallbackData data = new VideoCallbackData();
		data.setAmount(amount);
		data.setCurrencyId(currencyId);
		data.setCurrencyName(currencyName);
		data.setUid(sid);
		data.setUserId(userId);
		data.setUsername(username);
		
		Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
				Application.VIDEO_REWARD_ACTIVITY + "Received video callback request from ipAddress: "
						+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + data.toString());
		logger.info("Received video callback request from ipAddress: "
						+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + data.toString());
		videoManager.issueReward(data);
		
		return Response.accepted().build();
	}
	
	/*@Path("/video/fyber/reward")
	@GET
	public Response rewardCallback(@Context UriInfo info) {
		System.out.println("FYBER CALLBACK");
		System.out.println(info.toString());
		System.out.println(info.getPath());
		System.out.println(info.getAbsolutePath());
		System.out.println(info.getQueryParameters());
		System.out.println(info.getPathParameters());
		System.out.println(info.getRequestUri());
		return Response.accepted().build();
	}*/
	
	
}
