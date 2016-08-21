package is.web.beans.tools;

import is.ejb.bl.business.Application;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOSpinnerData;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
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

@ManagedBean(name = "rewardSpinBean")
@SessionScoped
public class RewardSpinBean {

	private String phoneNumber;
	private String rewardValue;

	@Inject
	DAORealm daoRealm;

	@Inject
	DAOAppUser daoAppUser;

	@Inject
	DAOSpinnerData daoSpinnerData;

	@Inject
	SpinnerManager spinnerManager;

	public void reward() {

		try {
			AppUserEntity appUser = daoAppUser.findByPhoneNumber(phoneNumber);

			if (appUser == null) {
				showAlert("Error", "Invalid user with phone number: " + phoneNumber);
				return;
			}

			int rewardValueI = Integer.parseInt(rewardValue);
			if (rewardValueI <= 0) {
				showAlert("Error", "Invalid reward value");
				return;
			}

			executeSpinReward(appUser, rewardValueI);
		} catch (Exception exc) {
			exc.printStackTrace();
			showAlert("Error", exc.getMessage());
		}

	}

	private void executeSpinReward(AppUserEntity appUser, int rewardValue) {
		try {

			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());
			if (spinnerData == null) {
				spinnerData = spinnerManager.insertSpinnerData(appUser);
			}

			spinnerData.setAvailableUses(spinnerData.getAvailableUses() + rewardValue);
			showAlert("Success", "Successfully rewarded user " + phoneNumber + " with " + rewardValue + " spin(s).");

			daoSpinnerData.createOrUpdate(spinnerData);

		} catch (Exception exc) {
			exc.printStackTrace();
			showAlert("Error", exc.getMessage());
		}

	}

	private void showAlert(String title, String message) {
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));

	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(String rewardValue) {
		this.rewardValue = rewardValue;
	}

}
