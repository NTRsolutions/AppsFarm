package is.ejb.bl.notificationSystems;

import is.ejb.bl.currencyCodes.CurrencyCodeConverter;
import is.ejb.bl.system.support.donky.DonkyTemplate;
import is.ejb.bl.system.support.donky.DonkyTemplateType;
import is.ejb.dl.entities.UserEventEntity;

public class NotificationMessageDictionary {

	public String getRewardNotificationMessage(UserEventEntity event, boolean success, String notificationTypeStr,
			boolean isEventFromUserThatWasInviting) {
		String notificationPayload = "";
		if (notificationTypeStr.equals(NotificationType.REWARD_INSTANT.toString())) {
			notificationPayload = getInstantRewardNotificationMessage(event, success);
		}

		if (notificationTypeStr.equals(NotificationType.REWARD_VIA_WALLET_PAYIN.toString())
				|| notificationTypeStr.equals(NotificationType.INSTALL.toString())) {
			notificationPayload = getWalletRewardNotificationMessage(event, success);
		}

		if (notificationTypeStr.equals(NotificationType.REWARD_VIA_REFERRAL_REGISTRATION.toString())) {
			notificationPayload = getReferralRewardNotificationMessage(event, success, isEventFromUserThatWasInviting);
		}

		if (notificationTypeStr.equals(NotificationType.WALLET_PAYOUT.toString())) {
			notificationPayload = getWalletPayoutNotificationMessage(event, success);
		}
		
		if (notificationTypeStr.equals(NotificationType.SNAPDEAL_EVENT_CHANGE.toString())) {
			notificationPayload = getSnapdealOfferStatusChangeNotificationMessage(event,success);
		}

		return notificationPayload;
	}

	private String getSnapdealOfferStatusChangeNotificationMessage(UserEventEntity event,boolean success){
		String notificationPayload = "Status for your offer with name <b>" + event.getOfferTitle() + "</b> has changed to: <b><i>"
				+ event.getRewardResponseStatus() + "</i></b>.";
		return notificationPayload;
	}
	
	
	private String getWalletPayoutNotificationMessage(UserEventEntity event, boolean success) {
		
		String notificationPayload = "Your payout has failed - please contact support for help.";
		if (event.getFriendPhoneNumber() != null){
			notificationPayload = "Your payout for friend with phone number <b>"+event.getFriendPhoneNumber() +
					"</b>  has failed - please contact support for help.";
		}
		if (success) {
			notificationPayload = DonkyTemplate.getDonkyTemplate(DonkyTemplateType.WALLET_PAY_OUT, event);
		}
		return notificationPayload;
	}

	private String getReferralRewardNotificationMessage(UserEventEntity event, boolean success, boolean isEventFromUserThatWasInviting) {
		String notificationPayload = "Refferal registration reward failed - please contact support for help.";

		if (success) {
			if (isEventFromUserThatWasInviting) {
				notificationPayload = DonkyTemplate.getDonkyTemplate(DonkyTemplateType.REFERRAL, event);
			}else{
				notificationPayload = "";
			}
		}

		return notificationPayload;
	}

	private String getWalletRewardNotificationMessage(UserEventEntity event, boolean success) {
		String notificationPayload = "Your reward has failed - please contact support for help.";
		if (success) {
			notificationPayload = DonkyTemplate.getDonkyTemplate(DonkyTemplateType.WALLET_PAY_IN, event);
		}

		return notificationPayload;
	}

	private String getInstantRewardNotificationMessage(UserEventEntity event, boolean success) {
		String notificationPayload = "Your reward has failed - please contact support for help.";

		if (success) {
			notificationPayload = DonkyTemplate.getDonkyTemplate(DonkyTemplateType.INSTANT_REWARD, event);
		}
		return notificationPayload;
	}

}
