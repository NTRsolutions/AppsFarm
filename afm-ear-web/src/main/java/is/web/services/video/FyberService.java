package is.web.services.video;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class FyberService {

	/*@Path("/video/fyber/reward")
	@GET
	public Response rewardCallback(@QueryParam("uid") String uid, @QueryParam("amount") int amount,
			@QueryParam("currency_id") String currencyId, @QueryParam("currency_name") String currencyName,
			@QueryParam("userId") String userId) {
		System.out.println("UID: " + uid);
		System.out.println("Amount: " + amount);
		System.out.println("Currency Id: " + currencyId);
		System.out.println("Currency name: " + currencyName);
		System.out.println("UserId: " + userId);
		return Response.accepted().build();
	}*/
	
	@Path("/video/fyber/reward")
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
	}
	
	
}
