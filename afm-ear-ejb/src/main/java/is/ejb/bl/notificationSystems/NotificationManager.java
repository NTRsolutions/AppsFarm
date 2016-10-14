package is.ejb.bl.notificationSystems;

import is.ejb.bl.business.Application;
import is.ejb.bl.firebase.FirebaseManager;
import is.ejb.bl.firebase.FirebaseMessage;
import is.ejb.bl.firebase.FirebaseResponse;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;

import is.ejb.bl.system.logging.LogStatus;

import is.ejb.dl.dao.DAOMobileApplicationType;

import is.ejb.dl.entities.AppUserEntity;

import is.ejb.dl.entities.MobileApplicationTypeEntity;


import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;


@Stateless
public class NotificationManager {

	@Inject
	private Logger logger;


	@Inject
	private DAOMobileApplicationType daoMobileApplicationType;

	@Inject
	private NotificationMessageDictionary notificationMessageDictionary;

	@Inject
	private FirebaseManager firebaseManager;


	
	
	public FirebaseResponse sendNotification(AppUserEntity appUser, String message) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					Application.NOTIFICATION_ACTIVITY + " sending notification message: " + message + " to appUser:"
							+ appUser);
			logger.info(" sending notification message: " + message + " to appUser:" + appUser);

			String apiKey = getFirebaseKey(appUser.getApplicationName());
			String deviceToken = appUser.getAndroidDeviceToken();
			if (apiKey == null || apiKey.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
						Application.NOTIFICATION_ACTIVITY + " error sending notification message: " + message
								+ " to appUser:" + appUser + " error: invalid api key for applicationName:"
								+ appUser.getApplicationName());
				return new FirebaseResponse(500,"Invalid api key");
			}

			if (deviceToken == null || deviceToken.length() == 0) {
				Application.getElasticSearchLogger().indexLog(Application.NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
						Application.NOTIFICATION_ACTIVITY + " error sending notification message: " + message
								+ " to appUser:" + appUser + " error: invalid device token");
				return new FirebaseResponse(500,"Invalid device token");
			}

			FirebaseMessage firebaseMessage = firebaseManager.prepareFirebaseMessage(apiKey, "AppsFarm", message, deviceToken);
			FirebaseResponse result = firebaseManager.sendMessage(firebaseMessage);
			
			Application.getElasticSearchLogger().indexLog(Application.NOTIFICATION_ACTIVITY, -1, LogStatus.OK,
					Application.NOTIFICATION_ACTIVITY + " sending notification message: " + message + " to appUser:"
							+ appUser + " result: " + result);
			return result;

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.NOTIFICATION_ACTIVITY, -1, LogStatus.ERROR,
					Application.NOTIFICATION_ACTIVITY + " error sending notification message: " + message
							+ " to appUser:" + appUser + " error: " + exc.toString());
			return new FirebaseResponse(500,"Exc: " + exc.getMessage());
		}
	}

	private String getFirebaseKey(String applicationName) {
		try {
			MobileApplicationTypeEntity application = daoMobileApplicationType.findByName(applicationName);
			return application.getFirebaseKey();
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

}
