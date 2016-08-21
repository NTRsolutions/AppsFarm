package is.web.beans.tools;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.WalletDataEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

@ManagedBean(name = "rewardUserBean")
@SessionScoped
public class RewardUserBean {

	private String phoneNumber;
	private String currency;
	private String rewardValue;
	private String phoneExtension;
	private String type;

	@Inject
	DAORealm daoRealm;

	@Inject
	DAOAppUser daoAppUser;

	@Inject
	DAOWalletData daoWalletData;

	public void reward() {

		try {
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);

			if (appUser == null) {
				showAlert("Error", "Invalid user with phone number: " + phoneNumber);
				return;
			}

			double rewardValueD = Double.parseDouble(rewardValue);
			if (rewardValueD <= 0) {
				showAlert("Error", "Invalid reward value");
				return;
			}			
			if (type.equals("instant"))
				executeInstantReward();
			if (type.equals("wallet"))
				executeWalletReward(appUser, rewardValueD);
		} catch (Exception exc) {
			exc.printStackTrace();
			showAlert("Error", exc.getMessage());
		}

	}

	private void executeWalletReward(AppUserEntity appUser, double rewardValue) {
		try {

			WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
			if (walletData == null) {
				// showAlert("Error","Wallet is empty, user need to perform action in order to add him funds.");
				// return;

				walletData = new WalletDataEntity();
				walletData.setUserId(appUser.getId());
				walletData.setTransactionCounter(0);
				walletData.setBalance(0);
				walletData.setIsoCurrencyCode(getCurrencyCode(appUser.getCountryCode()));

			}

			walletData.setBalance(walletData.getBalance() + rewardValue);
			showAlert("Success", "Successfully rewarded user " + phoneNumber + " with " + rewardValue + " " + currency);

			walletData.setTransactionCounter(walletData.getTransactionCounter() + 1);
			daoWalletData.createOrUpdate(walletData);

		} catch (Exception exc) {
			exc.printStackTrace();
			showAlert("Error", exc.getMessage());
		}

	}

	private String getCurrencyCode(String countryCode) {
		if (countryCode.equals("KE")) {
			return "KSH";
		} else if (countryCode.equals("IN")) {
			return "INR";
		} else if (countryCode.equals("ZA")) {
			return "ZAR";
		} else if (countryCode.equals("GB")) {
			return "GBP";
		} else if (countryCode.equals("PL")) {
			return "ZL";
		} else
			return "KSH";

	}

	private void showAlert(String title, String message) {
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));

	}

	private void executeInstantReward() {
		long transactionId = randLong(1, Long.MAX_VALUE);

		int rewardRespond = requestRewardMode(transactionId, phoneNumber, rewardValue, currency);
		String status = "";
		
		
		//System.out.println("RESULT!!!");
		//System.out.println(rewardRespond);
		if (rewardRespond == 200) {
			status = "Rewarded successfully. Transaction id:" + transactionId;
			Application.getElasticSearchLogger().indexLog(Application.REWARD_MANUAL_USER, 1, LogStatus.OK, Application.REWARD_MANUAL_USER + " phoneNumber: " + phoneNumber + " rewardValue: " + rewardValue + " currency:" + currency + " status: " + status + " phoneExtension: " + phoneExtension

			);

		}
		if (rewardRespond == 403) {
			status = "Reward failed. Code: (" + rewardRespond + ") Transaction id:" + transactionId;
			Application.getElasticSearchLogger().indexLog(Application.REWARD_MANUAL_USER, -1, LogStatus.ERROR, Application.REWARD_MANUAL_USER + " phoneNumber: " + phoneNumber + " rewardValue: " + rewardValue + " currency:" + currency + " status: " + status + " phoneExtension: " + phoneExtension

			);
		}
		if (rewardRespond == 0) {
			status = "Reward failed. Code: (" + rewardRespond + ") Transaction id:" + transactionId;
			Application.getElasticSearchLogger().indexLog(Application.REWARD_MANUAL_USER, -1, LogStatus.ERROR, Application.REWARD_MANUAL_USER + " phoneNumber: " + phoneNumber + " rewardValue: " + rewardValue + " currency:" + currency + " status: " + status + " phoneExtension: " + phoneExtension

			);
		}
		if (rewardRespond == 502){
			status = "Reward failed. Code: (" + rewardRespond + ") Transaction id:" + transactionId;
		    Application.getElasticSearchLogger().indexLog(Application.REWARD_MANUAL_USER, -1, LogStatus.ERROR, Application.REWARD_MANUAL_USER + " phoneNumber: " + phoneNumber + " rewardValue: " + rewardValue + " currency:" + currency + " status: " + status + " phoneExtension: " + phoneExtension);
		}

		// RequestContext.getCurrentInstance().showMessageInDialog(new
		// FacesMessage(FacesMessage.SEVERITY_INFO, "Result", status));
		showAlert("Result", status);

	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(String rewardValue) {
		this.rewardValue = rewardValue;
	}

	public String getPhoneExtension() {
		return phoneExtension;
	}

	public void setPhoneExtension(String phoneExtension) {
		this.phoneExtension = phoneExtension;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private int requestRewardMode(long internalT, String phoneNumber, String rewardValue, String rewardCurrency) {
		try {
			// --------------------------- handle request to rewarding system
			// ---------------------------------
			// kenya config
			// String bpUser = "bpuser";
			// String bpPass = "6_t89j^2Ht";
			// String url =
			// "http://130.211.67.26:9090/mode/bluepodapi/v1/credit/";

			// india config
			// String bpUser = "bpuser";
			// String bpPass = "gq^5Gr5DA#";
			// String url =
			// "http://146.148.2.61:9090/mode/bluepodapi/v1/credit/";

			RealmEntity realm = daoRealm.findByName("BPM");

			String bpUser = realm.getModeBPUser();
			String bpPass = realm.getModeBPPassword();
			String url = realm.getModeCreditUrl();

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			// add reuqest header
			con.setRequestMethod("POST");
			String urlParameters = "MSISDN=" + phoneExtension + phoneNumber.replaceAll("\\s+", "") + "&OriginTransactionID=" + internalT + // "&OriginTransactionID="+event.getInternalTransactionId()+
					"&Reward=" + rewardValue.replaceAll("\\s+", "") + "&ISOCurrCode=" + rewardCurrency.replaceAll("\\s+", "") + "&User=" + bpUser + "&Password=" + bpPass;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();

			// System.out.println(urlParameters);
			Application.getElasticSearchLogger().indexLog(
					Application.REWARD_MANUAL_USER_WS_RESULT,
					1,
					LogStatus.OK,
					Application.REWARD_MANUAL_USER_WS_RESULT + "\nSending 'POST' request to URL : " + url + "MSISDN=" + phoneExtension + phoneNumber.replaceAll("\\s+", "") + "&OriginTransactionID=" + internalT + "&Reward=" + rewardValue.replaceAll("\\s+", "") + "&ISOCurrCode="
							+ rewardCurrency.replaceAll("\\s+", "") + " Response Code : " + responseCode

			);
			// System.out.println("\nSending 'POST' request to URL : " + url);
			// System.out.println("Post parameters : " + urlParameters);
			// System.out.println("Response Code : " + responseCode);

			String responseString = "OK";
			String statusMessage = responseString;

			return responseCode;

			// --------------------------- handle request to rewarding system
			// ---------------------------------
		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
			// return
			// "{\"response\":\" status: "+RespStatusEnum.FAILED+" code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+"\"}";
		}

	}

	public static long randLong(long min, long max) {
		Random rand = new Random();
		long nextLong = rand.nextLong();
		while (nextLong < 0) {
			nextLong = rand.nextLong();
		}
		return nextLong % (max - min) + min;
	}

	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

}
