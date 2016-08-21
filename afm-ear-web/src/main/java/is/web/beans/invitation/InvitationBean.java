package is.web.beans.invitation;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ReferralType;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.invitation.GeneratingInvitationCodeException;
import is.ejb.bl.invitation.InvitationManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.util.WebResources;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;

@ManagedBean(name = "InvitationBean")
@SessionScoped
public class InvitationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private final static String INVITATION_CODE_PARAMETER_NAME = "code";
	private final static String INVITATION_TYPE_PARAMETER_NAME = "type";
	private final static String GPLAY_URL = "https://play.google.com/store/apps/details?id=com.airrewardz";
	private final static String REFERRER_PREFIX = "ADBROKER_";

	@Inject
	private Logger logger;

	@Inject
	private WebResources webResources;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private DAORealm daoRealm;

	private Map<String, String> parameters;

	@PostConstruct
	public void init() {

	}

	public void execute() {
		try {
			String code = parameters.get(INVITATION_CODE_PARAMETER_NAME);
			ReferralType type = null;
			try {
				type = ReferralType.valueOf(parameters.get(INVITATION_TYPE_PARAMETER_NAME));
			} catch (Exception e) {
				type = null;
			}
			if (code == null || code.isEmpty() || type == null) {
				logger.info("Wrong parameters: code: " + code + " type: " + type);
				logErrorToES("Wrong parameters: code: " + code + " type: " + type);
				redirectToError();
				return;
			}
			AppUserEntity appUser = null;
			try {
				appUser = daoAppUser.findByFBInvitationCode(code);
				if (appUser == null) {
					logger.info("Code " + code + " not found");
					logErrorToES("code not found error when creating invitation object for invitaton code: " + code
							+ " type: " + type.toString());
					redirectToError();
					return;
				}
				String redirectionCode = null;
				InvitationManager invitationManager = new InvitationManager();
				redirectionCode = invitationManager.generateRedirectingInvitationCode(daoInvitation, appUser);
				InvitationEntity invitation = addInvitation(redirectionCode, type, appUser);
				indexInvitationInES(invitation, getReferrerURL(redirectionCode));
				increaseUserPendingCounter(appUser);
				redirectToGP(redirectionCode);

				logger.info("Redirecting...");
				logSuccessToES("Redirected to Google Play Store, user: " + appUser.getEmail() + " phone:"
						+ appUser.getPhoneNumber() + " referral code: " + code + " type: " + type);
			} catch (GeneratingInvitationCodeException e) {
				logger.info("Collision error");
				logErrorToES("collision error when creating invitation object for user: " + appUser.getEmail()
						+ " phone: " + appUser.getPhoneNumber() + " referral code: " + code + " type: " + type);
				redirectToError();
				return;
			} catch (IOException e) {
				logger.info("Redirecting error");
				logErrorToES("Redirecting error - can't redirect to Google Play Store, user: " + appUser.getEmail()
						+ " phone:" + appUser.getPhoneNumber() + " referral code: " + code + " type: " + type);
				redirectToError();
			} catch (Exception e) {
				logger.info("Excpetion: " + e.getMessage());
				logErrorToES("Excpetion: " + e.getMessage());
				redirectToError();
			}
		} catch (IOException e) {
			logger.info("Problem with redirecting to error page  IOExcpetion: " + e.getMessage());
			logErrorToES("Problem with redirecting to error page  IOExcpetion: " + e.getMessage());
		} catch (Exception e) {
			logger.info("The last exception " + e.getMessage());
			logErrorToES("The last exception: " + e.getMessage());
		}

	}

	private void indexInvitationInES(InvitationEntity invitation, String link) {
		logger.info("Indexing invitation in es");
		try {
			AppUserEntity appUser = daoAppUser.findByEmail(invitation.getEmailInviting());

			RealmEntity realm = daoRealm.findById(appUser.getRealmId());

			Application.getElasticSearchLogger().indexUserClick(realm.getId(), invitation.getEmailInvited(),
					invitation.getEmailInviting(), appUser.getDeviceType(), invitation.getReferralSource().toString(), null, null, null,
					appUser.getRewardTypeName(), 0, 0, null, 0, realm.getName(), link, "REFERRAL_CLICK",
					invitation.getInvitedInternalTransactionId(), null, UserEventCategory.INVITE.toString(), null, null,
					null, appUser.getCountryCode(), false, appUser.getApplicationName(), appUser.getAdvertisingId(),
					appUser.getIdfa(), realm.isTestMode(), 0, null);
			logger.info("Indexed invitation in es");

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private InvitationEntity addInvitation(String code, ReferralType type, AppUserEntity appUser) {
		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.OK,
				Application.INVITATION_ACTIVITY + " " + " creating invitation object for user: " + appUser.getEmail()
						+ " phone: " + appUser.getPhoneNumber() + " referral code: " + code + " type: " + type);

		InvitationEntity invitation = new InvitationEntity();
		invitation.setCode(code);
		invitation.setReferralSource(type);
		invitation.setInvitingFBInviteCode(appUser.getFbInvitationCode());
		invitation.setPhoneNumberInviting(appUser.getPhoneNumber());
		invitation.setEmailInviting(appUser.getEmail());
		invitation.setPhoneNumberExtInviting(appUser.getPhoneNumberExtension());
		invitation.setDateOfInvitation(new Timestamp(System.currentTimeMillis()));
		invitation.setRewardValueInvited(0);
		invitation.setRewardValueInviting(0);
		invitation.setRewardType(UserEventCategory.INVITE.toString());
		invitation.setRewardTypeName(appUser.getRewardTypeName());
		daoInvitation.createOrUpdate(invitation);

		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.OK,
				Application.INVITATION_ACTIVITY + " " + " successfully created invitation object for user: "
						+ appUser.getEmail() + " phone: " + appUser.getPhoneNumber() + " referral code: "
						+ invitation.getCode() + " type: " + invitation.getReferralSource() + " invitation date: "
						+ invitation.getDateOfInvitation().toString());

		return invitation;
	}

	private void redirectToGP(String redirectionCode) throws IOException {
		String redirectionURL = getReferrerURL(redirectionCode);
		ExternalContext external = webResources.produceFacesContext().getExternalContext();
		external.redirect(redirectionURL);
	}

	private void redirectToError() throws IOException {
		String url = "/ab/errori.jsf";
		ExternalContext external = webResources.produceFacesContext().getExternalContext();
		external.redirect(url);
	}

	public void loadParameters() {
		parameters = webResources.produceFacesContext().getExternalContext().getRequestParameterMap();
	}

	private String getReferrerURL(String code) {
		return GPLAY_URL + "&referrer=" + REFERRER_PREFIX + code;
	}

	private void increaseUserPendingCounter(AppUserEntity appUser) {
		int pendingCounter = appUser.getPendingReferralsCounter();
		pendingCounter++;
		appUser.setPendingReferralsCounter(pendingCounter);
		daoAppUser.createOrUpdate(appUser);
	}

	private void logSuccessToES(String message) {
		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.OK,
				Application.INVITATION_ACTIVITY + " " + message);
	}

	private void logErrorToES(String message) {
		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.ERROR,
				Application.INVITATION_ACTIVITY + " " + message);
	}

}
