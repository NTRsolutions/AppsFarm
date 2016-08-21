package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.invitation.InvitationManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/")
public class InvitationService {

	@Inject
	private Logger logger;
	
	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private MailManager mailManager;

	@Inject
	private DAORealm daoRealm;

	@Context
	private HttpServletRequest httpRequest;

    @Inject
	private HashValidationManager hashValidationManager;

	@GET
	@Produces("application/json")
	@Path("/v1/invite/")
	public String inviteEventWithQueryRouting(
			@QueryParam("emailInvited") String emailInvited,
			@QueryParam("emailInviting") String emailInviting,
			@QueryParam("phoneNumberInviting") String phoneNumberInviting,
			@QueryParam("phoneNumberExtInviting") String phoneNumberExtInviting,
			@QueryParam("countryCodeInviting") String countryCodeInviting,
			@QueryParam("networkName") String networkNameInviting,
			@QueryParam("hashkey") String hashkey,
			@QueryParam("systemInfo") String systemInfo,
			@QueryParam("miscData") String miscData) {

		String responseMessage = "";
		int realmId = -1;
		String dataContent = "";

		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			dataContent = " emailInvited: " + emailInvited
					+ " emailInviting: " + emailInviting
					+ " phoneNumberInviting: " + phoneNumberInviting
					+ " phoneNumberExtInviting: " + phoneNumberExtInviting
					+ " countryCodeInviting: " + countryCodeInviting
					+ " networkNameInviting: " + networkNameInviting
					+ " ipAddress: " + systemInfo
					+ " systemInfo: " + systemInfo
					+ " miscData: " + miscData;
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					realmId,
					LogStatus.OK,
					Application.INVITATION_ACTIVITY + " received request: "
							+ dataContent);

			if (emailInvited == null || emailInviting == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
						+ "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_EMAIL + "\"}";
			}

			if (phoneNumberInviting == null) {
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
						+ "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER + "\"}";
			}

			RealmEntity realm = daoRealm.findByName(networkNameInviting);

			//validate request hash
			boolean isRequestValid = hashValidationManager.isRequestValid(realm.getApiKey(), hashkey, 
					hashValidationManager.getFullURL(httpRequest), 
					phoneNumberInviting, phoneNumberExtInviting, systemInfo, miscData, ipAddress);
			if(!isRequestValid) {
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY
								+ " error " + RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED);

				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_REQUEST_VALIDATION_FAILED+"\", "+ "\"userId\":\"-1\"}";
			} 

			//check if user did not exceed invitation requests
			AppUserEntity appUserInviting = daoAppUser.findByEmail(emailInviting);
			if (appUserInviting.getNumberOfSuccessfulInvitations() -1 > realm.getMaxInvitationsLimit()) {
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY
								+ " error " + RespCodesEnum.ERROR_EXCEEDED_TOTAL_NUMBER_OF_ALLOWED_REFERRAL_INVITATIONS);

				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_EXCEEDED_TOTAL_NUMBER_OF_ALLOWED_REFERRAL_INVITATIONS+"\", "+ "\"userId\":\"-1\"}";
			}

			//check if invited user is not already registered on the system
			AppUserEntity invitedUser = daoAppUser.findByEmail(emailInvited);
			if(invitedUser != null) {
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY
								+ " error " + RespCodesEnum.ERROR_INVITED_USER_ALREADY_REGISTERED);

				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_INVITED_USER_ALREADY_REGISTERED+"\", "+ "\"userId\":\"-1\"}";
			}

			//check if invitation for the same user (email) was not generated already by this person
			InvitationEntity invitation = daoInvitation.findByEmailInvited(emailInvited);
			if(invitation != null) {
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY
								+ " error " + RespCodesEnum.ERROR_INVITATION_ALREADY_SENT_TO_THIS_USER);

				return "{\"status\":\""+RespStatusEnum.FAILED+"\", "+ "\"code\":\""+RespCodesEnum.ERROR_INVITATION_ALREADY_SENT_TO_THIS_USER+"\", "+ "\"userId\":\"-1\"}";
			}
			
			//TODO create es invitation entry log
			
			InvitationManager invitationManager = new InvitationManager();
			
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setEmailRecipientAddress(emailInvited);
			mailParamsHolder.setCode(invitationManager.getUserInvitationCode(daoAppUser, appUserInviting));
			mailParamsHolder.setEmailInviting(emailInviting);
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.INVITATION);

			//increment successful invitations counter for user the sent invitation
			appUserInviting = daoAppUser.findByEmail(emailInviting);
			int numberOfSuccessfulInvitations = appUserInviting.getNumberOfSuccessfulInvitations();
			numberOfSuccessfulInvitations++;
			appUserInviting.setNumberOfSuccessfulInvitations(numberOfSuccessfulInvitations);
			daoAppUser.createOrUpdate(appUserInviting);
			
			responseMessage = "{\"status\":\"" + RespStatusEnum.SUCCESS
					+ "\", " + "\"code\":\"" + RespCodesEnum.OK + "\"}";

			return responseMessage;

		} catch (Exception e) {
			e.printStackTrace();

			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.INVITATION_ACTIVITY
							+ " error inviting new appUser: " + dataContent
							+ " " + e.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
					+ "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
					+ "\"}";
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/inviteLimit/")
	public String canUserIvite (
			@QueryParam("email") String email,
			@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("networkName") String networkName) { 
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			
			if(email == null){
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
						+ "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_EMAIL + "\"}";
			}
			
			if(phoneNumber == null){
				return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
						+ "\"code\":\""
						+ RespCodesEnum.ERROR_USER_INVALID_PHONE_NUMBER + "\"}";
			}
			
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			RealmEntity realm = daoRealm.findByName(networkName);
			
			if(realm.isInvitationEnabled() && (appUser.getNumberOfSuccessfulInvitations() < realm.getMaxInvitationsLimit())){
				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", "
						+ "\"code\":\"" + RespCodesEnum.OK + "\", "
						+ "\"response\":\"" + "YES"
						+ "\"}";
			} else {
				return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", "
						+ "\"code\":\"" + RespCodesEnum.OK + "\", "
						+ "\"response\":\"" + "NO"
						+ "\"}";
			}
		} catch (Exception e){
			e.printStackTrace();

			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.INVITATION_ACTIVITY
							+ " error checking invitation limit for " + phoneNumber 
							+ " " + e.toString());

			return "{\"status\":\"" + RespStatusEnum.FAILED + "\", "
					+ "\"code\":\"" + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
					+ "\"}";
		}
	}

}
