package is.ejb.bl.invitation;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.security.KeyGenerator;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.entities.AppUserEntity;

public class InvitationManager {
	
	private final static int CODE_LENGTH = 30;
	private final static int MAX_COLLISIONS_COUNT = 50;
	
	public String getUserInvitationCode(DAOAppUser daoAppUser, AppUserEntity appUser) throws GeneratingInvitationCodeException{
		if (hasUserInvitationCode(appUser)) {
			return appUser.getFbInvitationCode();
		} else {
			return generateNewInvitationCode(daoAppUser, appUser);
		}
	}
	
	public String generateRedirectingInvitationCode(DAOInvitation daoInvitation, AppUserEntity appUser) throws GeneratingInvitationCodeException{
		int count = 0;
		String redirectionCode = null;
		do{
			redirectionCode = generateInvitationCode();
			count++;
		}while((daoInvitation.findByCode(redirectionCode) != null) && (count < MAX_COLLISIONS_COUNT));
		
		if(count < MAX_COLLISIONS_COUNT){
			return redirectionCode;
		} else {
			throw new GeneratingInvitationCodeException("Exceed limit of generating new code attempts");
		}
	}

	/*
	 * This method generate new invitation code for given user
	 * and save it to db
	 */
	private String generateNewInvitationCode(DAOAppUser daoAppUser, AppUserEntity appUser) throws GeneratingInvitationCodeException{
		String newInvitationCode = null;
		int count = 0;
		try {
			do {
				newInvitationCode = generateInvitationCode();
				count++;
			} while ((daoAppUser.findByFBInvitationCode(newInvitationCode) != null) && (count < MAX_COLLISIONS_COUNT));
		} catch (Exception e) {
			throw new NullPointerException(e.getMessage());
		}
	
		if (count < MAX_COLLISIONS_COUNT) {
			appUser.setFbInvitationCode(newInvitationCode);
			daoAppUser.createOrUpdate(appUser);
		} else {
			throw new GeneratingInvitationCodeException("Exceed limit of generating new code attempts");
		}
		
		return newInvitationCode;
	}

	private boolean hasUserInvitationCode(AppUserEntity appUser) {
		String invitationCode = appUser.getFbInvitationCode();

		if (invitationCode == null) {
			return false;
		} else if (invitationCode.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	private String generateInvitationCode() {
		return KeyGenerator.generateKey(CODE_LENGTH);
	}

}
