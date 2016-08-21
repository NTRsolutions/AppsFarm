package is.ejb.bl.system.support.donky;

import is.ejb.bl.currencyCodes.CurrencyCodeConverter;
import is.ejb.dl.entities.UserEventEntity;

import java.text.DecimalFormat;

public class DonkyTemplate {

	private final static String basePath = "http:\\/\\/mode-rewardz.com\\/ab/donky\\/";
	private final static String TRIPPA_ANDROID_STORE_ADDRESS = "https://play.google.com/store/apps/details?id=com.tripparewardz&hl=en";
	private final static String TRIPPA_IOS_STORE_ADDRESS = "https://itunes.apple.com/gb/app/trippa-reward/id1071830981?mt=8";
	private final static String CINETREATS_ANDROID_STORE_ADDRESS = "https://play.google.com/store/apps/details?id=com.cinetreats";
	private final static String CINETREATS_IOS_STORE_ADDRESS = "https://itunes.apple.com/gb/app/cinetreats/id1078231010?mt=8";
	private final static String AIRREWARDZ_ANDROID_STORE_ADDRESS = "https://play.google.com/store/apps/details?id=com.airrewardz";
	private final static String AIRREWARDZ_IOS_STORE_ADDRESS = "https://itunes.apple.com/pl/app/air-rewardz/id1003780692?mt=8";
	private final static DecimalFormat rewardValueFormat = new DecimalFormat("#.00");

	public static String getDonkyTemplate(DonkyTemplateType templateType, UserEventEntity event) {
		String template = "";
		double rewardValue = event.getRewardValue();
		String rewardCurrencyCode = CurrencyCodeConverter
				.getUserFriendlyCurrencyCodeByGeo(event.getRewardIsoCurrencyCode());
		String rewardTypeName = event.getRewardTypeName();
		if (event.getRewardTypeName().toLowerCase().contains("cine")
				|| event.getRewardTypeName().toLowerCase().contains("trippa")) {
			rewardValue = event.getCustomRewardValue();
			rewardCurrencyCode = "";
		}
		String friendPhone = event.getFriendPhoneNumber();
		if (templateType.equals(DonkyTemplateType.INSTANT_REWARD)) {
			template = getDonkyTemplateForInstantReward(rewardValue, rewardCurrencyCode, rewardTypeName);
		} else if (templateType.equals(DonkyTemplateType.REFERRAL)) {
			template = getDonkyTemplateForReferMessage(rewardValue, rewardCurrencyCode, rewardTypeName);
		} else if (templateType.equals(DonkyTemplateType.WALLET_PAY_IN)) {
			template = getDonkyTemplateForWalletPayIn(rewardValue, rewardCurrencyCode, rewardTypeName);
		} else if (templateType.equals(DonkyTemplateType.WALLET_PAY_OUT)) {
			template = getDonkyTemplateForWalletPayOut(rewardValue, rewardCurrencyCode, rewardTypeName, friendPhone);
		}
		template = addDonkyTemplateFooter(template, event);

		return template;

	}

	private static String addDonkyTemplateFooter(String htmlTemplate, UserEventEntity event) {
		String rewardTypeName = event.getRewardTypeName();
		String deviceType = event.getDeviceType();
		String storeLink = getStoreLink(rewardTypeName, deviceType);

		htmlTemplate += "</div>";
		htmlTemplate += "<a href='" + storeLink + "'><img src='" + basePath + "rate.png' style='width:100%'></a>";
		htmlTemplate += "</body>";
		htmlTemplate += "</html>";

		return htmlTemplate;
	}

	private static String getStoreLink(String rewardType, String deviceType) {
		String storeAddress = "";
		if (deviceType != null && rewardType != null) {
			rewardType = rewardType.toLowerCase();
			deviceType = deviceType.toLowerCase();
			if (deviceType.contains("android")) {
				if (rewardType.contains("airrewardz")) {
					storeAddress = AIRREWARDZ_ANDROID_STORE_ADDRESS;
				} else if (rewardType.contains("trippa")) {
					storeAddress = TRIPPA_ANDROID_STORE_ADDRESS;
				} else if (rewardType.contains("cine")) {
					storeAddress = CINETREATS_ANDROID_STORE_ADDRESS;
				}
			}
			if (deviceType.contains("ios")) {
				if (deviceType.contains("android")) {
					if (rewardType.contains("airrewardz")) {
						storeAddress = AIRREWARDZ_IOS_STORE_ADDRESS;
					} else if (rewardType.contains("trippa")) {
						storeAddress = TRIPPA_IOS_STORE_ADDRESS;
					} else if (rewardType.contains("cine")) {
						storeAddress = CINETREATS_IOS_STORE_ADDRESS;
					}
				}
			}
		}

		return storeAddress;
	}

	private static String getWalletPayoutImageForRewardType(String rewardType) {
		String walletPayoutImage = "walletmessagemail-cashedout.png";
		if (rewardType != null) {
			if (rewardType.toLowerCase().contains("cine"))
				walletPayoutImage = "walletmessagemail-cashedout.png";
			if (rewardType.toLowerCase().contains("trippa"))
				walletPayoutImage = "walletmessagemail-cashedout.png";
		}
		return walletPayoutImage;
	}

	private static String getWalletPayinImageForRewardType(String rewardType) {
		String walletPayoutImage = "walletmessagemail.png";
		if (rewardType != null) {
			if (rewardType.toLowerCase().contains("cine"))
				walletPayoutImage = "walletmessagemailcine.png";
			if (rewardType.toLowerCase().contains("trippa"))
				walletPayoutImage = "walletmessagetrippa.jpg";
		}
		return walletPayoutImage;
	}

	private static String getDonkyTemplateForReferMessage(double value, String currency, String rewardType) {
		String htmlTemplate = "";
		htmlTemplate += "<html style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<body style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<img src='" + basePath + "header.png' style='width:100%'>";
		htmlTemplate += "<div style='width:100%;position:relative'>";
		htmlTemplate += "<img style='width:100%;' src='" + basePath + "refermessagemail.png' >";
		htmlTemplate += "<h2 style='position:absolute;top:25%;left:37%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;font-size:200%;color:#0A749C'>"
				+ rewardValueFormat.format(value) + currency + "</h2>";

		return htmlTemplate;
	}

	private static String getDonkyTemplateForWalletPayIn(double value, String currency, String rewardType) {
		String htmlTemplate = "";
		htmlTemplate += "<html style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<body style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<img src='" + basePath + "header.png' style='width:100%'>";
		htmlTemplate += "<div style='width:100%;position:relative'>";
		htmlTemplate += "<img style='width:100%;' src='" + basePath + getWalletPayinImageForRewardType(rewardType)
				+ "' >";
		htmlTemplate += "<h4 style='position:absolute;top:25%;left:37%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;font-size:200%;color:#0A749C'>"
				+ rewardValueFormat.format(value) + currency + "</h4>";

		return htmlTemplate;
	}

	private static String getDonkyTemplateForWalletPayOut(double value, String currency, String rewardType,
			String friendPhone) {
		String htmlTemplate = "";
		htmlTemplate += "<html style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<body style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<img src='" + basePath + "header.png' style='width:100%'>";
		htmlTemplate += "<div style='width:100%;position:relative'>";
		htmlTemplate += "<img style='width:100%;' src='" + basePath + getWalletPayoutImageForRewardType(rewardType)
				+ "' >";
		if (friendPhone != null) {
			htmlTemplate += "<h4 style='position:absolute;top:28%;left:15%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;color:#0A749C'>"
					+ "(Friend phone number: " + friendPhone + ")</h4>";
			htmlTemplate += "<h2 style='position:absolute;top:35%;left:37%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;font-size:140%;color:#0A749C'>"
					+ rewardValueFormat.format(value) + currency + "</h2>";
		}else{
			htmlTemplate += "<h2 style='position:absolute;top:25%;left:37%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;font-size:200%;color:#0A749C'>"
					+ rewardValueFormat.format(value) + currency + "</h2>";
		}

		return htmlTemplate;
	}

	private static String getDonkyTemplateForInstantReward(double value, String currency, String rewardType) {
		String htmlTemplate = "";
		htmlTemplate += "<html style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<body style='overflow-x:hidden;width:100%;padding:0;margin:0;'>";
		htmlTemplate += "<img src='" + basePath + "header.png' style='width:100%'>";
		htmlTemplate += "<div style='width:100%;position:relative'>";
		htmlTemplate += "<img style='width:100%;' src='" + basePath + "instantcreditmail.png' >";
		htmlTemplate += "<h2 style='position:absolute;top:25%;left:37%;width:100%;font-family:Trebuchet MS, Helvetica, sans-serif;font-size:200%;color:#0A749C'>"
				+ rewardValueFormat.format(value) + currency + "</h2>";

		return htmlTemplate;
	}

}
