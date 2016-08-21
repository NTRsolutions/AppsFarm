package is.web.beans.ar;

import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.web.services.ResponseConversionHistory;
import is.web.util.WebResources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.primefaces.context.RequestContext;

import com.google.gson.Gson;

@ManagedBean(name = "arBean")
@SessionScoped
public class ArBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private WebResources webResources;

	@Inject
	private DAOAppUser daoAppUser;
	
	@Inject
	private DAOWalletData daoWalletData;

	// if you change any path, then don't forget change the path in theme.css
	// file

	// private final String SERVER_ADDRESS = "http://104.155.72.76:8080";
	private final String SERVER_ADDRESS = "http://test.adjockey.net";
//	private final String SERVER_ADDRESS = "http://mode-rewardz.com";
//	private final String SERVER_ADDRESS = "http://127.0.0.1:8080";
	private final String BASE_PATH = SERVER_ADDRESS + "/ab/ar";
	private final String HOME_PAGE_PATH = BASE_PATH + "/index.jsf";
	

	private AppUserEntity loggedAppUser;

	private String login;
	private String password;

	// change password
	private String currentPassword;
	private String newPassword1;
	private String newPassword2;

	private final String DISPLAY_NONE = "none";
	private final String DISPLAY_BLOCK = "block";

	private String errorLoginDisplay = DISPLAY_NONE;
	private String successDisplay = DISPLAY_NONE;
	private String errorsDisplay = DISPLAY_NONE;
	private Errors errors = new Errors();

	private String currencyCode = "";

	private List<RewardHistory> rewardHistory;

	
	
	@PostConstruct
	public void init() {
	}

	public void authenticate() {
		logger.info("authenticate");
		try {
			if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
				logger.info("Login or password is null or empty");
				showErrorLogin();
				return;
			} else {
				AppUserEntity appUser = daoAppUser.findByPhoneNumber(login);
				if (appUser != null) {
					if (validatePassword(appUser.getPassword())) {
						loggedAppUser = appUser;
						String url = BASE_PATH + "/account/profile.jsf";
						ExternalContext external = webResources.produceFacesContext().getExternalContext();
						try {
							external.redirect(url);
						} catch (IOException e) {
							logger.info("redirect failed");
						}
						errorLoginDisplay = DISPLAY_NONE;
					} else {
						logger.info("Validate: false");
						showErrorLogin();
						return;
					}
				} else {
					logger.info("App user not found");
					showErrorLogin();
					return;
				}
			}
		} catch (Exception e) {
			logger.info("Authentication exeption");
			return;
		}
	}

	public void validateAccountAccess() {
		ExternalContext external = webResources.produceFacesContext().getExternalContext();
		try {
			if (!isLogged()) {
				external.redirect(HOME_PAGE_PATH);
			}
		} catch (IOException e) {
			logger.info("validation acount access failed");
		}
	}

	public void logout() {
		logger.info("logout");
		loggedAppUser = null;
		String url = HOME_PAGE_PATH;
		ExternalContext external = webResources.produceFacesContext().getExternalContext();
		try {
			external.redirect(url);
		} catch (IOException e) {
			logger.info("redirect failed");
		}
	}

	public String getErrorLoginDisplay() {
		return errorLoginDisplay;
	}
	
	public void showErrorLogin(){
		errorLoginDisplay = DISPLAY_BLOCK;
		RequestContext.getCurrentInstance().update("idLoginForm");
		RequestContext.getCurrentInstance().update("idmLoginForm");
	}

	public String getUserWalletValue(){
		try {
			WalletDataEntity walletData = daoWalletData.findByUserId(loggedAppUser.getId());
			currencyCode = walletData.getIsoCurrencyCode();
			
			DecimalFormatSymbols decSymbols = new DecimalFormatSymbols();
			decSymbols.setDecimalSeparator('.');
			DecimalFormat walletValueFormat = new DecimalFormat(".##", decSymbols);
			
			return walletValueFormat.format(walletData.getBalance());
		} catch (Exception e) {
			currencyCode = "";
			return "0";
		}
	}
	
	public String getUserWalletCurrency(){
		return currencyCode;
	}

	public String getDisplayLoginButton() {
		if (isLogged()) {
			return "display: none";
		} else {
			return "display: block";
		}
	}

	public String getDisplayAccountButton() {
		if (isLogged()) {
			return "display: block";
		} else {
			return "display: none";
		}
	}
	
	public void changePassword() {
		logger.info("--- changing password");
		errors = new Errors();
		validatePasswords(errors);
		logger.info("--- after validation");
		
		if(errors.isError()){
			errorsDisplay = DISPLAY_BLOCK;
			successDisplay = DISPLAY_NONE;
			logger.info("---ERRR = " + errors.getAll().toString());
		} else {
			errorsDisplay = DISPLAY_NONE;
			successDisplay = DISPLAY_BLOCK;
			logger.info("---ERRR = no error");
			
			String newPasswordHash = getPasswordHash(newPassword1);
			loggedAppUser.setPassword(newPasswordHash);
			daoAppUser.createOrUpdate(loggedAppUser);
		}
		
		RequestContext.getCurrentInstance().update("idChangePasswordForm");
		RequestContext.getCurrentInstance().update("idSuccess");
		RequestContext.getCurrentInstance().update("idErrors");
		logger.info("=== ++ === " + loggedAppUser.getPassword());
	}
	
	public String getSuccessDisplay(){
		return successDisplay;
	}
	
	public String getErrorsDisplay(){
		return errorsDisplay;
	}
	
	public List<String> getErrorsList(){
		return errors.getAll();
	}


	public void updateRewardHistory() {
		ResponseConversionHistory conversionHistory = null;
		try {
			URL myURL = new URL(SERVER_ADDRESS + "/ab/svc/v1/getClickHistory?userId=" + loggedAppUser.getId());
			URLConnection myURLConnection = myURL.openConnection();
			myURLConnection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

			Gson gson = new Gson();
			conversionHistory = gson.fromJson(reader, ResponseConversionHistory.class);

			ConversionHistoryHolder historyHolder = conversionHistory.getConversionHistoryHolder();
			List<ConversionHistoryEntry> historyList = historyHolder.getListConversionHistoryEntries();

			List<RewardHistory> tempRewardHistory = new ArrayList<ArBean.RewardHistory>();
			for (ConversionHistoryEntry entry : historyList) {
				tempRewardHistory.add(new RewardHistory(entry.getOfferTitle(),
						entry.getClickTimestamp(), entry.getRewardValue(), entry.getRewardCurrency()));
			}

			rewardHistory = tempRewardHistory;
		} catch (MalformedURLException e) {
			logger.info("updateRewardHistory - MalformedURLException");
			e.printStackTrace();
			rewardHistory = new ArrayList<ArBean.RewardHistory>();
		} catch (IOException e) {
			logger.info("updateRewardHistory - IOException");
			e.printStackTrace();
			rewardHistory = new ArrayList<ArBean.RewardHistory>();
		}
	}

	public void clearChangePasswordContent(){
		errorsDisplay = DISPLAY_NONE;
		successDisplay = DISPLAY_NONE;
	}

	private void validatePasswords(Errors errors){
		if(currentPassword == null || currentPassword.isEmpty()){
			errors.add("password.empty", "Password is empty");
		} else if(!getPasswordHash(currentPassword).equals(loggedAppUser.getPassword())){
			errors.add("password.no_match", "Incorrerct password");
		}
		
		if(newPassword1 == null || newPassword2 == null || newPassword1.isEmpty() || newPassword2.isEmpty()){
			errors.add("new_password.empty", "New password is empty");
		} else if(!newPassword1.equals(newPassword2)){
			errors.add("new_password.no_match", "New passwords are different");
		}
	}

	private boolean validatePassword(String userPassword) {
		return userPassword.equals(getPasswordHash(password));
	}

	private String getPasswordHash(String password) {
		String saltValue = "Salt string";
		return DigestUtils.sha1Hex(password + saltValue);
	}

	private boolean isLogged() {
		if (loggedAppUser == null) {
			return false;
		} else {
			return true;
		}
	}

	public List<RewardHistory> getRewardHistory() {
		return rewardHistory;
	}

	public void setRewardHistory(List<RewardHistory> list) {
		rewardHistory = list;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword1() {
		return newPassword1;
	}

	public void setNewPassword1(String newPassword1) {
		this.newPassword1 = newPassword1;
	}

	public String getNewPassword2() {
		return newPassword2;
	}

	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}

	public AppUserEntity getLoggedAppUser() {
		return loggedAppUser;
	}

	public void setLoggedAppUser(AppUserEntity loggedAppUser) {
		this.loggedAppUser = loggedAppUser;
	}

	public String getBasePath() {
		return BASE_PATH;
	}

	public String getHomePagePath() {
		return HOME_PAGE_PATH;
	}


	// ----------------------------------

	public class RewardHistory {

		private String name;
		private Timestamp date;
		private double rewardValue;
		private String currency;

		public RewardHistory(String name, Timestamp date, double rewardValue, String currency) {
			super();
			this.name = name;
			this.date = date;
			this.rewardValue = rewardValue;
			this.currency = currency;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Timestamp getDate() {
			return date;
		}

		public void setDate(Timestamp date) {
			this.date = date;
		}

		public double getRewardValue() {
			return rewardValue;
		}

		public void setRewardValue(double rewardValue) {
			this.rewardValue = rewardValue;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

	}

	// ///////////////////
	// ///////////////////

	public ArBean() {

	}

	public static void main(String... args) {
		new ArBean(1l);
	}
	
	private ArBean(long j){
		double [] qq = {1231231.2313221, 23.43443, 45, 45.511, 34.555, 22.550, 4324.4565, 123.456, 12.0000};
		
		DecimalFormatSymbols decSymbols = new DecimalFormatSymbols();
		decSymbols.setDecimalSeparator('.');
		decSymbols.setGroupingSeparator(',');
		DecimalFormat df = new DecimalFormat("###,###.##", decSymbols);
		df.setGroupingSize(3);
		for(double d: qq){
			System.out.println(df.format(d).replace(',', ' '));
		}
	}

	private ArBean(int i) {
		URL myURL;
		String jsonHistory = "aaa";
		Gson gson = new Gson();
		ResponseConversionHistory conversionHistory = null;
		try {
			myURL = new URL(SERVER_ADDRESS + "/ab/svc/v1/getClickHistory?userId=81");
			URLConnection myURLConnection = myURL.openConnection();
			myURLConnection.connect();
			// Scanner scanner = new Scanner(myURLConnection.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
			// while(scanner.hasNext()){
			// jsonHistory += scanner.nextLine();
			// }
			// conversionHistory = gson.fromJson(jsonHistory,
			// ResponseConversionHistory.class);
			conversionHistory = gson.fromJson(reader, ResponseConversionHistory.class);
		} catch (MalformedURLException e) {
			System.out.println("--- " + "jsonHistory malformed url exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("--- " + "jsonHistory io exception");
			e.printStackTrace();
		}

		System.out.println("--- " + jsonHistory);

		System.out.println(conversionHistory.getCode());
		System.out.println(conversionHistory.getStatus());

		ConversionHistoryHolder historyHolder = conversionHistory.getConversionHistoryHolder();
		List<ConversionHistoryEntry> historyList = historyHolder.getListConversionHistoryEntries();

		for (ConversionHistoryEntry entry : historyList) {
			System.out.println("offerId: " + entry.getOfferId());
			System.out.println("offerTitle: " + entry.getOfferTitle());
			System.out.println("rewardValue: " + entry.getRewardValue());
		}
	}

}
