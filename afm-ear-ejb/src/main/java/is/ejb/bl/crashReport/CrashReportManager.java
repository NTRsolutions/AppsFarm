package is.ejb.bl.crashReport;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationNameEnum;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.business.WalletTransactionStatus;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.rewardSystems.mode.TestModeManager;
import is.ejb.bl.rewardSystems.radius.RadiusProvider;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.bl.testing.TestManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOCloudtraxConfiguration;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.CloudtraxConfigurationEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.ejb.dl.entities.WalletPayoutOfferTransactionEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

@Stateless
public class CrashReportManager {

	@Inject
	private Logger logger;

	public CrashReportManager() {
	}

	public void persistCrashReport(Map<String, List<String>> formParams, String ipAddress) {

		/*
		 * Map<String, List<String>> errorContent = new HashMap<String,
		 * List<String>>(); errorContent.put("PHONE_MODEL",
		 * formParams.get("PHONE_MODEL")); errorContent.put("BRAND",
		 * formParams.get("BRAND")); errorContent.put("BRAND",
		 * formParams.get("BRAND")); errorContent.put("ANDROID_VERSION",
		 * formParams.get("ANDROID_VERSION")); errorContent.put("PACKAGE_NAME",
		 * formParams.get("PACKAGE_NAME")); errorContent.put("STACK_TRACE",
		 * formParams.get("STACK_TRACE")); String json = new
		 * Gson().toJson(errorContent); System.out.println(json);
		 * 
		 * Application.getElasticSearchLogger().indexLog(Application.
		 * USER_CLICK_HISTORY_REQUEST_ACTIVITY, -1, LogStatus.OK,
		 * Application.ACRA_ERROR_REPORT+" errorLog: " + json );
		 */

		System.out.println(new Gson().toJson(formParams));

		String phoneNumberExtension = "";
		String phoneNumber = "";
		String deviceInfo = "";
		String deviceVersion = "";
		String applicationVersion = "";
		String applicationName = "";
		String breadcrumb = "";
		String stackTrace = "";

		if (formParams.containsKey("CUSTOM_DATA")) {
			String customData = URLDecoder.decode(formParams.get("CUSTOM_DATA").get(0));

			String[] customDataArray = customData.split("<>");
			for (String data : customDataArray) {
				if (data.toLowerCase().contains("breadcrumb")) {
					String[] breadcrumbArray = data.split("=");
					if (breadcrumbArray.length > 1) {
						breadcrumb = breadcrumbArray[1].replaceAll("\\s+", "");
					}
				}

				if (data.toLowerCase().contains("phonenumber")) {
					String[] phoneNumberArray = data.split("=");
					if (phoneNumberArray.length > 1) {
						phoneNumber = phoneNumberArray[1].replaceAll("\\s+", "");
					}
				}

				if (data.toLowerCase().contains("extension")) {
					String[] extensionArray = data.split("=");
					if (extensionArray.length > 1) {
						phoneNumberExtension = extensionArray[1].replaceAll("\\s+", "");
					}
				}
			}

			if (formParams.containsKey("BRAND"))
				deviceInfo = formParams.get("BRAND").get(0);

			if (formParams.containsKey("PHONE_MODEL"))
				deviceInfo = deviceInfo + " " + formParams.get("PHONE_MODEL").get(0);

			if (formParams.containsKey("ANDROID_VERSION"))
				deviceVersion = formParams.get("ANDROID_VERSION").get(0);

			if (formParams.containsKey("PACKAGE_NAME"))
				applicationName = formParams.get("PACKAGE_NAME").get(0);

			if (formParams.containsKey("PACKAGE_NAME"))
				applicationVersion = formParams.get("PACKAGE_NAME").get(0);

			if (formParams.containsKey("APP_VERSION_NAME"))
				applicationVersion = formParams.get("APP_VERSION_NAME").get(0);

			if (formParams.containsKey("STACK_TRACE"))
				stackTrace = URLDecoder.decode(formParams.get("STACK_TRACE").get(0));
		}

		/*System.out.println("phoneNumberExt: "+phoneNumberExtension
				+ " phoneNumber: "+phoneNumber
				+ " deviceInfo: " +deviceInfo
				+ " deviceVersion: " +deviceVersion
				+ " applicationName: "+applicationName
				+ " applicationVersion: "+applicationVersion
				+ " breadcrumb: "+breadcrumb
				+ " stackTrace: "+stackTrace
				+ " ipAddress:" + ipAddress);*/
				
				
		
		Application.getElasticSearchLogger().indexCrashReport(phoneNumberExtension, phoneNumber, deviceInfo, deviceVersion, applicationName, applicationVersion, breadcrumb, stackTrace, ipAddress);

	}
}
