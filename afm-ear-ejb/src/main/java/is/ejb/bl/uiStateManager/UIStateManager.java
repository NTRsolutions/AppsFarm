package is.ejb.bl.uiStateManager;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class UIStateManager {
	
	@Inject
	private Logger logger;

	public boolean displayReferrralInvite(AppUserEntity appUser, RealmEntity realm) {
		try {
			if(realm.isInvitationEnabled()) {
				if (appUser.getNumberOfSuccessfulInvitations() > realm.getMaxInvitationsLimit()) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.UI_STATE_MANAGER, realm.getId(), 
					LogStatus.ERROR, 
					Application.UI_STATE_MANAGER+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());

			return false;
		}
	}
	
	public UIStateHolder getUIStateHolder(AppUserEntity appUser, RealmEntity realm) {
		try {
			UIStateHolder stateHolder = new UIStateHolder();
			stateHolder.setDisplayReferrralInvite(displayReferrralInvite(appUser, realm)); //set flag for displaying referral invite window

			return stateHolder;
		} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.UI_STATE_MANAGER, realm.getId(), 
					LogStatus.ERROR, 
					Application.UI_STATE_MANAGER+" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString());

			return null;
		}
		
	}
}
