package is.ejb.bl.referral;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.InvitationEntity;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ReferralAbuseDetector {

	@Inject
	DAOAppUser daoAppUser;

	// return true when abuse is detected
	// false otherwise
	public boolean checkInvitation(InvitationEntity invitationEntity) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, 
					LogStatus.OK, 
					Application.INVITATION_ABUSE_DETECTOR_CHECK + " " +
					" invited phone: " + invitationEntity.getPhoneNumberInvited()+
					" invited code: " + invitationEntity.getCode()+
					" inviting phone: "+ invitationEntity.getPhoneNumberInviting()+
					" checking invitation:" + invitationEntity);

			// get inviting person
			//AppUserEntity invitingUser = daoAppUser.findByEmail(invitationEntity.getEmailInviting());
			AppUserEntity invitingUser = daoAppUser.findByPhoneNumber(invitationEntity.getPhoneNumberInviting());

			// get invited person
			AppUserEntity invitedUser = null;
			//try to find by phone number
			invitedUser = daoAppUser.findByPhoneNumber(invitationEntity.getPhoneNumberInvited());
			if(invitedUser == null) {
				invitedUser = daoAppUser.findByReferralCode(invitationEntity.getCode());
			} 
			if(invitedUser == null) {
				invitedUser = daoAppUser.findByEmail(invitationEntity.getEmailInvited());
			}
			
			if (invitingUser == null || invitedUser == null) {
				Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.ERROR, Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + "cant check invitation - users are not in db");
				return false;
			}
			checkDeviceType(invitingUser, invitedUser);
			

			if (invitingUser.getAndroidDeviceToken() != null && invitedUser.getAndroidDeviceToken() != null)
				if (invitingUser.getAndroidDeviceToken().equals(invitedUser.getAndroidDeviceToken())) 
					if ((invitingUser.getDeviceType().toLowerCase().contains("android") )
							&& ((invitedUser.getDeviceType().toLowerCase().contains("android")))){
	
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + 
							Application.INVITATION_ABUSE_DETECTED + " " +
								"invitation id: " + invitationEntity.getId() + " abuse detected , androidDeviceToken variable ");
					
					return true;
				}

			if (invitingUser.getDeviceId() != null && invitedUser.getDeviceId() != null)
				if (invitingUser.getDeviceId().equals(invitedUser.getDeviceId())) {
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + 
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , deviceId variable ");
					
					return true;
				}

			if (invitingUser.getPhoneId() != null && invitedUser.getPhoneId() != null)
				if (invitingUser.getPhoneId().equals(invitedUser.getPhoneId())) {
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + 
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , phoneId variable ");
					
					return true;
				}

			if (invitingUser.getMac() != null && invitedUser.getMac() != null)
				if (invitingUser.getMac().equals(invitedUser.getMac())) {
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " +
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , mac variable ");
					
					return true;
				}

			if (invitingUser.getiOSDeviceToken() != null && invitedUser.getiOSDeviceToken() != null)
				if (invitingUser.getiOSDeviceToken().equals(invitedUser.getiOSDeviceToken())) 
					if (!(invitingUser.getDeviceType().toLowerCase().contains("android") )
							&& (!(invitedUser.getDeviceType().toLowerCase().contains("android")))){
						
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + 
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , iOSDeviceToken variable ");
					
					return true;
				}

			if (invitingUser.getIdfa() != null && invitedUser.getIdfa() != null)
				if (invitingUser.getIdfa().equals(invitedUser.getIdfa())) 
						if (!(invitingUser.getDeviceType().toLowerCase().contains("android") )
								&& (!(invitedUser.getDeviceType().toLowerCase().contains("android")))){
						Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + 
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , idfa variable ");
					
					return true;
				}

			if (invitingUser.getAdvertisingId() != null && invitedUser.getAdvertisingId() != null)
				if (invitingUser.getAdvertisingId().equals(invitedUser.getAdvertisingId())) {
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.OK, 
							Application.INVITATION_ABUSE_DETECTOR_CHECK + " " +
							Application.INVITATION_ABUSE_DETECTED + " " +
									"invitation id: " + invitationEntity.getId() + " abuse detected , advertisingId variable ");
					
					return true;
				}

			Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, 
					LogStatus.OK, 
					Application.INVITATION_ABUSE_DETECTOR_CHECK + " no fraud detected between users: " +
					" inviting phone: " + invitingUser.getPhoneNumber()+
					" inviting email: "+invitingUser.getEmail()+ 
					" invited phone: " + invitedUser.getPhoneNumber()+
					" invited email: "+ invitedUser.getEmail()+
					" invitation entity: " + invitationEntity);

			return false;
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.ERROR, 
					Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + "cant check invitation - error: " + exc.toString());
			
			return false;
		}

	}

	private void checkDeviceType(AppUserEntity invitingUser, AppUserEntity invitedUser) {
		if (invitingUser.getDeviceType() == null){
			updateDeviceType(invitingUser);
		}
		if (invitedUser.getDeviceType() == null){
			updateDeviceType(invitedUser);
		}
	}
	
	private void updateDeviceType(AppUserEntity appUser){
		try{
			appUser.setDeviceType("Android");
			daoAppUser.createOrUpdate(appUser);
		}
		catch (Exception exception){
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.INVITATION_ABUSE_DETECTOR_CHECK, -1, LogStatus.ERROR, 
					Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + "cant update device type : " + exception.toString());
		
		}
	}

}
