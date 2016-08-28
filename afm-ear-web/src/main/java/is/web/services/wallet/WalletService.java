package is.web.services.wallet;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;

import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.wallet.WalletManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIValidator;

import is.web.services.user.validators.PasswordValidator;
import is.web.services.user.validators.UsernameValidator;
import is.web.services.wall.validators.RequestValidator;
import is.web.services.wallet.validators.UsernamePasswordCombinationValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;



@Path("/")
public class WalletService {

	@Inject
	private Logger logger;
	@Context
	private HttpServletRequest httpRequest;

	
	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	DAOWalletData daoWalletData;

	@Inject
	WalletManager walletManager;

	@Inject
	private UsernameValidator usernameValidator;
	@Inject
	private PasswordValidator passwordValidator;
	@Inject
	private UsernamePasswordCombinationValidator usernamePasswordCombinationValidator;
	@Inject
	private RequestValidator requestValidator;
	@Inject
	private APIHelper apiHelper;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/wallet/")
	public String getWalletData(final APIRequestDetails details) {
		WalletDataResponse response = new WalletDataResponse();
		try {
			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_DATA_ACTIVITY + "Received wallet data request from ipAddress: "
							+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + details);

			HashMap<String, Object> parameters = details.getParameters();
			for (APIValidator validator : getWalletDataValidators()) {
				if (validator.validate(parameters)) {
					logger.info("Validator: " + validator.getClass() + " OK");
				} else {
					logger.info("Validator: " + validator.getClass() + " FAILED");
					Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
							Application.WALLET_DATA_ACTIVITY + " Validator FAILED: " + validator.getClass()
									+ " for request: " + details);
					apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
					return apiHelper.getGson().toJson(response);
				}
			}

			AppUserEntity appUser = daoAppUser.findByUsername((String) parameters.get("username"));
			WalletDataEntity walletData = selectWalletData(appUser);
			apiHelper.setupSuccessResponse(response);
			response.setWalletData(walletData);

			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_DATA_ACTIVITY + " returned wallet for request: " + details + " walletData:"
							+ walletData);
			return apiHelper.getGson().toJson(response);

		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.ERROR,
					Application.WALLET_DATA_ACTIVITY + " exception : " + exc.toString() + " for request " + details);
			return apiHelper.getGson().toJson(response);
		}
	}

	private WalletDataEntity selectWalletData(AppUserEntity appUser) throws Exception {
		WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
		if (walletData == null) {
			walletData = insertWalletData(appUser);
		}
		return walletData;
	}

	private WalletDataEntity insertWalletData(AppUserEntity appUser) {
		WalletDataEntity walletData = new WalletDataEntity();
		try {
			logger.info("Inserting new wallet data for user: " + appUser.getId());
			Application.getElasticSearchLogger().indexLog(Application.WALLET_DATA_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_DATA_ACTIVITY + "Inserting new wallet data for user: " + appUser.getId());
			double roundBalance = round(walletData.getBalance(), 2);
			walletData.setBalance(roundBalance);
			walletData.setUserId(appUser.getId());
			daoWalletData.createOrUpdate(walletData);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return walletData;

	}

	private List<APIValidator> getWalletDataValidators() {
		List<APIValidator> apiValidators = new ArrayList<APIValidator>();
		apiValidators.add(usernameValidator);
		apiValidators.add(passwordValidator);
		apiValidators.add(usernamePasswordCombinationValidator);
		apiValidators.add(requestValidator);
		return apiValidators;
	}


	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

	

}
