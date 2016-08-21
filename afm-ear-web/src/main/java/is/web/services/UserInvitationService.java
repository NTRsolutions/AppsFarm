package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/")
public class UserInvitationService {

	@Inject
	private Logger logger;

	@Inject
	private DAOAppUser daoAppUser;

	
	@GET
	@Produces("application/json")
	@Path("/v1/GetNumberOfUserInvitations/")
	public String getNumberOfUserInvitations(@QueryParam("phoneNumber") String phoneNumber,
		@QueryParam("systemInfo") String systemInfo,
		@QueryParam("miscData") String miscData) {

		if (phoneNumber == null) {
			logger.info("Phone number not found");
			logToES(LogStatus.ERROR, "Phone number not found", phoneNumber, miscData, systemInfo);
			return getFailedResponse(RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER);
		}

		try {
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			int numberOfAllInvitations = appUser.getPendingReferralsCounter();
			int numberOfSuccessfulInvitations = appUser.getSuccessfulReferralsCounter();
			logToES(LogStatus.OK, "Number of all invitations: " + numberOfAllInvitations +
					" number of successful invitations: " + numberOfSuccessfulInvitations,
					phoneNumber, miscData, systemInfo);
			return getSuccessResponse(RespCodesEnum.OK, numberOfAllInvitations, numberOfSuccessfulInvitations);
		} catch (Exception e) {
			logger.info("User " + phoneNumber + " does not exist");
			logToES(LogStatus.ERROR, "User does not exist", phoneNumber, miscData, systemInfo);
			return getFailedResponse(RespCodesEnum.ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND);
		}
	}

	private String getSuccessResponse(RespCodesEnum code,
		int numberOfAllInvitations, int numberOfSuccessfulInvitations) {

		return "{\n" +
				"\t\"status\": \"" + RespStatusEnum.SUCCESS + "\",\n" +
				"\t\"code\": \"" + code + "\",\n" +
				"\t\"allInvitations\": \"" + numberOfAllInvitations + "\",\n" +
				"\t\"successfulInvitations\": \"" + numberOfSuccessfulInvitations + "\"\n" +
				"}";
	}

	private String getFailedResponse(RespCodesEnum code) {
		return "{\n" +
				"\t\"status\": \"" + RespStatusEnum.FAILED + "\",\n" +
				"\t\"code\": \"" + code + "\"\n" +
				"}";
	}

	private void logToES(LogStatus logStatus, String message, String phoneNumber, String miscData, String systemInfo) {
		int realmId = -1;
		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, realmId,
				logStatus, message + " phoneNumber: " + phoneNumber + " miscData: " + miscData + " systemInfo: " + systemInfo);
	}

}
