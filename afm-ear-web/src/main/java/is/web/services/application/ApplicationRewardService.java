package is.web.services.application;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;

@Path("/")
public class ApplicationRewardService {

	@Inject
	DAOApplicationReward daoApplicationReward;

	@Inject
	DAOMobileApplicationType daoMobileApplicationType;

	@GET
	@Produces("application/json")
	@Path("/v1/applicationRewards/")
	public String getApplicationRewards(@QueryParam("applicationName") String applicationName, @QueryParam("rewardType") String rewardType) {
		try {

			if (applicationName == null || applicationName.length() == 0) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INVALID_APPLICATION_NAME + "\"}";

			}

			List<ApplicationRewardEntity> applicationRewardEntityList = daoApplicationReward.findByApplicationName(applicationName);
			if (rewardType != null) {
				List<ApplicationRewardEntity> applicationRewardEntityListWithRewardType = new ArrayList<ApplicationRewardEntity>();
				for (ApplicationRewardEntity rewardEntity : applicationRewardEntityList) {
					if (rewardEntity.getRewardType() != null && rewardType.equals(rewardEntity.getRewardType())) {
						applicationRewardEntityListWithRewardType.add(rewardEntity);
					}
				}
				
				if (applicationRewardEntityListWithRewardType.size() > 0)
					applicationRewardEntityList = applicationRewardEntityListWithRewardType;
			}
			String json = new Gson().toJson(applicationRewardEntityList);

			return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK + "\", " + "\"offers\":" + json + "}";

		} catch (Exception exc) {
			exc.printStackTrace();
			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", " + "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + "\"}";

		}

	}
}
