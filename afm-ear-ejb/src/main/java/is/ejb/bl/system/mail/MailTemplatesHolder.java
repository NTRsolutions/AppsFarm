package is.ejb.bl.system.mail;

import is.ejb.bl.business.ReferralType;
import is.ejb.bl.reporting.ReportDH;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.util.ArrayList;

public class MailTemplatesHolder {

	// http://stackoverflow.com/questions/5068827/how-do-i-send-html-email-via-java
	public MailDataHolder getRegistrationEmailDHCinetreats(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi " + mailParamsHolder.getEmailRecipientFullName() + "!</div>" + "\n <br/>"
				+ "\n <div>Welcome to Cinetreats - the app that earns you stubs to redeem against free cinema tickets, digital downloads and more!</div>"
				+ "\n <div></div>" + "\n <br/>"
				+ "\n <div>In Cinetreats you will find loads of offers to complete and apps to install and open that can earn you valuable stubs which are redeemed for cinema tickets, soft drinks, popcorn and even digital downloads.</div>"
				+ "\n <br/>"
				+ "\n <div>Once you have completed the tasks you will get a message saying you have stubs in your wallet. Go to your in app wallet to see how many stubs you have earned and what you can cash them in for.</div>"
				+ "\n <br/>"
				+ "\n <div>You will receive a voucher to this email address as soon as you decide what you would like to use your stubs for.  </div>"
				+ "\n <div>Good luck and have a great time at the movies!</div>" + "\n <br/>"
				+ "\n <div>Contact us at support@cinetreats.co.uk if you need any help or find us on social media</div>"
				+ "\n <div>The Cinetreats Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Cinetreats Account");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;

	}

	public MailDataHolder getRegistrationEmailDHGoAhead(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi " + mailParamsHolder.getEmailRecipientFullName() + "!</div>" + "\n <br/>"
				+ "\n <div>Welcome to Trippa Reward the app that earns you money off your next journey!</div>"
				+ "\n <div></div>" + "\n <br/>"
				+ "\n <div>Just click on one of the offers or apps on our list and follow the instructions to either install and open the app or complete the offer.  You will then start to get points into your wallet in the app, and can see the offers you have completed in reward history.</div>"
				+ "\n <br/>"
				+ "\n <div>Once you have enough points, you can then redeem the offers in the in app wallet to get your next daily or weekly pass completely free via the mtickets app.</div>"
				+ "\n <br/>"
				+ "\n <div>Please contact support@trippareward.com if you have any questions or find us on social media.</div>"
				+ "\n <div>Also, please ensure you have installed and registered for the the <a href=\"http://bluepodmedia.go2cloud.org/aff_c?offer_id=1337&aff_id=2\">mtickets</a> app in order to be able to use your Trippa Reward travel discounts.</div>"
				+ "\n <br/>" + "\n <a href=\"http://www.trippareward.com\">http://www.trippareward.com</a><br/>"
				+ "\n <a href=\"https://www.facebook.com/Trippa-Reward-455838121267498\">https://www.facebook.com/Trippa-Reward-455838121267498</a><br/>"
				+ "\n <a href=\"https://twitter.com/trippareward\">https://twitter.com/trippareward</a><br/>"
				+ "\n <br/>" + "\n <div>The Trippa Reward Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Trippa Account");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getRegistrationEmailDHRewardz(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		/*
		 * String emailContent= "<div>Hi "
		 * +mailParamsHolder.getEmailReceipientFullName()+"!</div>"+ "\n <br/>"+
		 * "\n <div>Congratulations on signing up to Air Rewardz!</div>"+
		 * "\n <div>You're now ready to start earning FREE mobile airtime credits!</div>"
		 * + "\n <br/>"+
		 * "\n <div>Simply open Air Rewardz, download and run your chosen apps and your FREE airtime will be automatically credited to your mobile account.</div>"
		 * + "\n <br/>"+
		 * "\n <div>...and don't forget about our built-in free messaging service, AirChatz!</div>"
		 * + "\n <br/>"+
		 * "\n <div>For more information on Air Rewardz and Air Chatz, you can visit our website or find us on social media.</div>"
		 * + "\n <br/>"+
		 * "\n <a href=\"www.airrewardz.net\">www.airrewardz.net</a><br/>"+
		 * "\n <a href=\"www.facebook.com/airrewardz\">www.facebook.com/airrewardz</a><br/>"
		 * +
		 * "\n <a href=\"www.twitter.com/airrewardz\">www.twitter.com/airrewardz</a><br/>"
		 * + "\n <br/>"+ "\n <div>Welcome, and enjoy!</div>"+ "\n <br/>"+
		 * "\n <div>The Air Rewardz Team</div>";
		 */

		String emailContent = "<div>Hi " + mailParamsHolder.getEmailRecipientFullName() + "!</div>" + "\n <br/>"
				+ "\n <div>Congratulations on signing up to Air Rewardz!</div>"
				+ "\n <div>You're now ready to start earning FREE mobile airtime credits!</div>" + "\n <br/>"
				+ "\n <div>Simply open Air Rewardz, download and run your chosen apps and your FREE airtime will be automatically credited to your mobile account.</div>"
				+ "\n <br/>" + "\n <div>Click on this link to activate your account: "
				+ mailParamsHolder.getActivationLink() + "</div>" + "\n <br/>"
				+ "\n <div>...and don't forget about our built-in free messaging service, AirChatz!</div>" + "\n <br/>"
				+ "\n <div> There are some exciting and brand new features in this new release of Air Rewardz you have just installed. Video rewardz, which you can access from the main menu in the app, gives you airtime credit into your air rewardz wallet every time you just watch a video!</div>"
				+ "\n <br/>"
				+ "\n <div>Also, you can use the wallet to store your airtime credit until you have enough in there to get an airtime credit rewarded to your mobile account - or opt for instant airtime reward on our higher payout offers. Simply go to the wallet page from the main menu, where you can check your up to date balance, input as much as you want to get credited, which will be covered by your current balance, and press the REDEEM button - the airtime credit will then be available for you on your phone.</div>"
				+ "\n <br/>"
				+ "\n <div>Finally, you can earn airtime credit by recommending a friend to use Air Rewardz. Just chose 'invite a friend' from the main menu. Put their email address in and press submit - your friend will then receive an email with the link to install Air Rewardz. plus a special code they need to use in their registration - they just enter this in the 'referral code' field on the registration page. Once this is done - both you and they get an airtime reward! (please note the amount of times you can refer a friend may be limited to only four emails at any one time)</div>"
				+ "\n <br/>"
				+ "\n <div>For more information on Air Rewardz and Air Chatz, you can visit our website or find us on social media.</div>"
				+ "\n <br/>" + "\n <div>Any questions, give us a shout - support@airrewardz.net</div>" + "\n <br/>"
				+ "\n <a href=\"www.airrewardz.net\">www.airrewardz.net</a><br/>"
				+ "\n <a href=\"www.facebook.com/airrewardz\">www.facebook.com/airrewardz</a><br/>"
				+ "\n <a href=\"www.twitter.com/airrewardz\">www.twitter.com/airrewardz</a><br/>" + "\n <br/>"
				+ "\n <div>Welcome, and enjoy!</div>" + "\n <br/>" + "\n <div>The Air Rewardz Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Air Rewardz Account");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getPasswordChangeEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi " + mailParamsHolder.getEmailRecipientFullName() + "!</div>" + "\n <br/>"
				+ "\n <div>Your password has been successfully changed.</div>" + "\n <br/>"
				+ "\n <div><div>Your new password is: " + mailParamsHolder.getEmailRecipientNewPassword() + "</div>"
				+ "\n <br/>"
				+ "\n <div>If you did not request a password change, please notify us using support@airrewardz.net</div>"
				+ "\n <br/>"
				+ "\n <div>You can change your password by logging into your account at <a href=\"www.airrewardz.net\">www.airrewardz.net</a></div>"
				+ "\n <br/>" + "\n <div>The Air Rewardz Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Air Rewardz Account Password Change");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getPasswordRecoveryEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi " + mailParamsHolder.getEmailRecipientFullName() + "!</div>" + "\n <br/>"
				+ "\n <div><div>We have successfully created you a new password: "
				+ mailParamsHolder.getEmailRecipientNewPassword() + " You can now login using this. </div>" + "\n <br/>"
				+ "\n <div>If you did not request a password recovery, please notify us using support@airrewardz.net</div>"
				+ "\n <br/>"
				+ "\n <div>You can change your password by logging into your account at <a href=\"www.airrewardz.net\">www.airrewardz.net</a></div>"
				+ "\n <br/>" + "\n <div>The Air Rewardz Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Air Rewardz Account Password Recovery");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getReportingEmailDH(MailParamsHolder mailParamsHolder, MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();
		ArrayList<ReportDH> listReports = mailParamsHolder.getReports();

		String emailContent = "<h1>Report</h1><br>";
		for (int i = 0; i < listReports.size(); i++) {
			ReportDH report = listReports.get(i);
			// only last report (monthly) contains user retention matrix
			double[][] userRetentionMatrix = report.getUserRetentionMatrix();

			/*emailContent = emailContent + "<div>--------------- Reward type: " + report.getRewardTypeName() + " ("
					+ report.getReportPeriodNameString() + ") ----------------</div>" + "\n <div>Date range: "
					+ report.getDateStart().toString() + " - " + report.getDateEnd().toString() + "</div>" +
					// "\n <br/>"+
					"\n <div>Registrations: " + report.getRegistrationsSum() + "\n <div>Clicks: "
					+ report.getClicksSum() + "\n <div>Conversions: " + report.getConversionsSum()
					+ "\n <div>Conversion rate (CR): " + report.getConversionRate() + "</div>"
					+ "\n <div>Gained profit value: " + report.getProfitSumInTargetCurrency() + "</div>"
					+ "\n <div>Spent reward value: " + report.getRewardSumInTargetCurrency() + "</div>"
					+ "\n <div>Snapdeal clicks:" + report.getSnapdealClicksSum() + "</div>"
					+ "\n <div>Snapdeal conversions:" + report.getSnapdealConversionSum() + "</div>"
					+ "\n <div>Snapdeal conversions approved:" + report.getSnapdealConversionApprovedSum() + "</div>"
					+ "\n <div>Quidco clicks:" + report.getQuidcoClicksSum() + "</div>" + "\n <div>Quidco conversions:"
					+ report.getQuidcoConversionSum() + "</div>" + "\n <div>Quidco conversions approved:"
					+ report.getQuidcoConversionApprovedSum() + "</div>" + "\n <div>Spins:" + report.getSpinSum()
					+ "</div>" + "\n <div>Spin rewards:<br>" + report.getSpinRewards() + "</div>";*/
			emailContent += "<hr>";
			emailContent += "<h3>Report for "+report.getRewardTypeName()+" for "+ report.getReportPeriodNameString()+"</h3>";
			emailContent += "<h4>Date range: "
					+ report.getDateStart().toString() + " - " + report.getDateEnd().toString()+"</h4>";
			emailContent += "<h4>General stats</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total offer payout</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalPayout()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total expenses</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalExpenses()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total profit</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalProfit()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Registrations</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getRegistrationsSum()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Wall selections</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getWallSelectionsSum()+"</td></tr>";
			emailContent += "</table>";
			
			emailContent += "<br><h4>Installs report:</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";

			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Conversions</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getConversionsSum()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Conversions Rate (CR)</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getConversionRate()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total installs payout</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalInstallPayout()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total installs expenses</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalInstallExpenses()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total installs profit</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalInstallProfit()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total unique clicks</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getTotalUniqueClicksDao()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Unique conversion rate</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getConversionRateDao()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Total clicks</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getClicksSum()+"</td></tr>";
			
			emailContent += "</table>";
			
			
			
			
			
			
			
			emailContent += "<h4>Quidco report:</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Quidco clicks</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getQuidcoClicksSum()+"</td></tr>";

			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Quidco conversions</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getQuidcoConversionSum()+"</td></tr>";

			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Quidco conversions approved</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getQuidcoConversionSum()+"</td></tr>";
			
			emailContent += "<tr style='border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'>Quidco profit</td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+report.getProfitFromQuidco()+"</td>"
					+ "</tr>";
			emailContent += "</table>";
			
			emailContent += "<h4>Snapdeal report:</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Snapdeal clicks</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getSnapdealClicksSum()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Snapdeal conversions</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getSnapdealConversionSum()+"</td></tr>";

			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Snapdeal conversions approved</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getSnapdealConversionSum()+"</td></tr>";
			
			emailContent += "<tr style='border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'>Snapdeal profit</td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+report.getProfitFromSnapdeal()+"</td>"
					+ "</tr>";
	
			emailContent += "</table>";
			
			
			emailContent += "<h4>Videos report:</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Videos clicks</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getVideosCount()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Videos profit</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getProfitFromVideos()+"</td></tr>";

			emailContent += "</table>";
			
			
			emailContent += "<h4>Referral report:</h4>";
			emailContent += "<table style='width:50%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Report type</b></td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'><b>Value</b></td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Referrals count</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getReferralClicksSum()+"</td></tr>";
			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Success referrals count </td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getReferralSuccessSum()+"</td></tr>";

			emailContent += "<tr style='width:100%;border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>Loss from referrals</td>"
					+ "<td style='width:100%;border: 1px solid black;border-collapse: collapse;'>"+report.getReferralLoseSum()+"</td></tr>";	

			emailContent += "</table>";
			
			emailContent += "<h4>Spinner report:</h4>";
			emailContent += "Generated spin rewards: " + report.getSpinRewards().getTotalSpins() + " <br>";
			emailContent += "Selected spin rewards from " + report.getSpinRewards().getStartDate() + " to " + report.getSpinRewards().getEndDate() + "<br>";
			emailContent += "Profit from spinner:" + report.getSpinRewards().getProfit()+ "<br>";
			emailContent += "Loss from handed out rewards: " + report.getSpinRewards().getLoss() + " <br>";
			emailContent += "Income from bought spins: " + report.getTotalSpinProfit() + "<br>";
			emailContent += "Unique users: " + report.getSpinRewards().getUserCount()+ "<br><br>";
			emailContent += "<table style='width:70%;border: 1px solid black;border-collapse: collapse;'>";
			emailContent += "<tr style='border: 1px solid black;border-collapse: collapse;'>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Reward name</b></td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Reward value</b></td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Reward type</b></td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Reward count</b></td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Reward percentage</b></td>"
					+ "<td style='border: 1px solid black;border-collapse: collapse;'><b>Unique users</b></td>"
					+ "</tr>";
			for (SpinnerRewardEntity spinnerReward : report.getSpinRewards().getSpinRewardsMap().keySet()) {
				int count = report.getSpinRewards().getSpinRewardsMap().get(spinnerReward);
				float percentage = (count * 100f) / report.getSpinRewards().getTotalSpins();
				emailContent += "<tr style='border: 1px solid black;border-collapse: collapse;'>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+spinnerReward.getRewardName()+"</td>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+spinnerReward.getRewardValue()+"</td>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+spinnerReward.getRewardType()+"</td>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+count+"</td>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+percentage+"%</td>"
						+ "<td style='border: 1px solid black;border-collapse: collapse;'>"+report.getSpinRewards().getSpinRewardsUserMap().get(spinnerReward).size()+"</td>"
						+ "</tr>";
			}

			emailContent += "</table>";
			
			
			emailContent += "<h4>Retention:</h4>";
			if (userRetentionMatrix != null) {
				double totalUniqueUsers = 0;
				emailContent = emailContent + "\n <br/>";
				emailContent = emailContent + "\n <div><div>User retention stats </div>";
				for (int j = 1; j < userRetentionMatrix[0].length; j++) {
					totalUniqueUsers = totalUniqueUsers + userRetentionMatrix[0][j];
					emailContent = emailContent + "\n <div>" + "" + j + " unique daily visits* performed by: "
							+ userRetentionMatrix[0][j] + " users" + " (" + userRetentionMatrix[1][j] * 100 + "%)"
							+ "</div>";
				}
				emailContent = emailContent + "\n <div>Total unique users: " + totalUniqueUsers + "</div>";
				emailContent = emailContent
						+ "\n <div>*5 unique daily visits performed by 3 users indicate that 3 users performed offer clicks in 5 different days</div>";
			}

		}

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("AdBroker activity report");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getProblemReportEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Problem Type: " + mailParamsHolder.getProblemType() + "</div>" + "\n <br/>"
				+ "\n <div>Message: " + mailParamsHolder.getProblemMessage() + "</div>" + "\n <br/>"
				+ "\n <div>Link in admin panel: " + mailParamsHolder.getAdminPanelLink() + "</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Air Rewardz Problem Report");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getInvitationEmailDH(MailParamsHolder mailParamsHolder, MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi,</div>" + "\n <br/>" + "\n <div>You have been invited by "
				+ mailParamsHolder.getEmailInviting() + " to join Air Rewardz to earn free airtime today!</div>"
				+ "\n <br/>" + "\n <div>Download our app using link below.</div>" + "\n <br/>"
				+ "\n <div><a href=\"http://mode-rewardz.com:8080/ab/Invitation.jsf?code=" + mailParamsHolder.getCode()
				+ "&type=" + ReferralType.EMAIL + "\">Download Air Rewardz</a></div>" + "\n <br/>"
				+ "\n <div>The Air Rewardz Team</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Air Rewardz Invitation");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getTrippaQuidcoRewardCashbackSuccessEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>"
				+ "Great news,  your Trippa Reward Cashback Points have been recorded. "
				+ "The cash back from any purchase(s) will now show in your Trippa wallet "
				+ "as pending Trippa points, which will be available to cash out once the transaction has been approved by our systems."
				+ "<br/>" + "<br/>"
				+ "Thank you for using Trippa Reward, please contact us on support@trippareward.com if you have any questions.";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Your Trippa Reward Cashback Success!");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getTrippaQuidcoRewardCashbackTrackedEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>"
				+ "Great news, your recent purchase via Trippa Reward has successfully tracked. " + "<br/>" + "<div>"
				+ "Receipt to: " + mailParamsHolder.getEmailRecipientAddress() + "<br/>" + "Retailer: "
				+ mailParamsHolder.getRetailer() + "<br/>" + "Purchase amount: " + mailParamsHolder.getPurchaseAmount()
				+ "<br/>" + "Cashback amount: " + mailParamsHolder.getCashbackAmount() + " Trippa points" + "</div>"
				+ "<br/>"
				+ "Your Trippa points will show as pending in your Trippa wallet and will be available to cash out once this transaction has been approved by the retailer. "
				+ "<br/>"
				+ "Thank you for using Trippa Reward, please contact us on support@trippareward.com if you have any questions.";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Your Trippa Reward cashback has tracked successfully!");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getTrippaQuidcoRewardAvailableToSpendEmailDH(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>"
				+ "Great news, your recent purchase via Trippa Reward has been approved and your points are now in your wallet as available to spend. "
				+ "<br/>" + "<div>" + "Receipt to: " + mailParamsHolder.getEmailRecipientAddress() + "<br/>"
				+ "Retailer: " + mailParamsHolder.getRetailer() + "<br/>" + "Purchase amount: "
				+ mailParamsHolder.getPurchaseAmount() + "<br/>" + "Cashback amount: "
				+ mailParamsHolder.getCashbackAmount() + " Trippa points" + "</div>" + "<br/>"
				+ "Thank you for using Trippa Reward, please contact us on support@trippareward.com if you have any questions.";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Your Trippa Reward points are now available to spend!");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getTrippaQuidcoCreditCardRegistrationEmail(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>" + "Welcome to In-store Cashback Points with Trippa" + "</div>" + "<br/>" + "<div>"
				+ "We can see you have attempted to register your credit card for "
				+ "Cashback points on Trippa Reward which brings you a big step "
				+ "closer to earning cashback Trippa points via in store purchases " + "on the High Street!" + "</div>"
				+ "<br/>" + "<div>" + "To ensure your purchase tracks properly we will simply ask you to wait 24 hours "
				+ "before using your card in-store, having then activated the offers for the retailer (s) you want to use."
				+ "</div>" + "<br/>" + "<div>"
				+ "Don’t forget to browse and activate cash back offers as in-store offers sometimes "
				+ "need activation with just one click! Activation can be done before you shop via the Trippa Reward app offers page."
				+ "</div>" + "<br/>" + "<div>"
				+ "If you have any questions, please get in touch via support@trippareward.com" + "</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Welcome to In-store Cashback Points with Trippa");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getSnapdealOfferTrackedEmail(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>" + " <div>thank you for buying "
				+ mailParamsHolder.getRetailer()
				+ " via Air Rewardz. Great news -  the airtime reward you have earned of up to"
				+ mailParamsHolder.getCashbackAmount() + " airtime "
				+ " is now showing in your Air Rewardz wallet.  You will be able to cash this out as soon as we get"
				+ " confirmation from the retailer about the purchase and don’t see it cancelled for any reason.</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Hot offer tracked");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getSnapdealOfferCancelledEmail(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>"
				+ "<div>thank you for making a purchase via Air Rewardz. This is to confirm that " + "your purchase of "
				+ mailParamsHolder.getRetailer() + " has been cancelled and the airtime reward of up to "
				+ mailParamsHolder.getCashbackAmount() + " airtime will be taken out of your wallet.</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Hot offer cancelled");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

	public MailDataHolder getSnapdealOfferApprovedEmail(MailParamsHolder mailParamsHolder,
			MonitoringSetupEntity mailBoxSetup) {
		MailDataHolder mdh = new MailDataHolder();

		String emailContent = "<div>Hi there,</div>" + "<br/>" + "<div> great news! Your purchase of "
				+ mailParamsHolder.getRetailer() + " via Air Rewardz has now been confirmed and you have earned"
				+ mailParamsHolder.getCashbackAmount() + " airtime reward which is now available in your wallet.</div>";

		// fill email data
		mdh.setEmailAddress(mailParamsHolder.getEmailRecipientAddress());
		// fill subject
		mdh.setEmailSubject("Hot offer approved");
		// fill content
		mdh.setEmailContent(emailContent);
		mdh.setMailboxSetup(mailBoxSetup);

		return mdh;
	}

}
