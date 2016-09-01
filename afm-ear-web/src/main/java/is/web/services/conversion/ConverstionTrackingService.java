package is.web.services.conversion;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;

import is.ejb.bl.reward.RewardManager;

import is.ejb.bl.system.logging.LogStatus;



import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;

import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.services.APIHelper;
import is.web.services.APIResponse;
import is.web.services.conversion.validators.ConversionValidator;
import is.web.services.conversion.validators.EventValidator;
import is.web.services.conversion.validators.FraudDetectionValidator;
import is.web.services.conversion.validators.ServerAddressValidator;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;



@Path("/")
public class ConverstionTrackingService {
	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;


	@Context
	private ServletContext context;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private APIHelper apiHelper;

	@Inject
	private ServerAddressValidator serverAddressValidator;

	@Inject
	private EventValidator eventValidator;

	@Inject
	private FraudDetectionValidator fraudDetectionValidator;

	@GET
	@Produces("application/json")
	@Path("/v1/conversion/")
	public String registerEventUsingQueryRouting(@QueryParam("internalTransactionId") String internalTransactionId,
			@QueryParam("offerProviderTransactionId") String offerProviderTransactionId) {
		return executeConversion(internalTransactionId, offerProviderTransactionId);
	}

	@GET
	@Produces("application/json")
	@Path("/v1/conversion/{internalTransactionId}/{offerProviderTransactionId}")
	public String registerEvent(@PathParam("internalTransactionId") String internalTransactionId,
			@PathParam("offerProviderTransactionId") String offerProviderTransactionId) {
		return executeConversion(internalTransactionId, offerProviderTransactionId);
	}

	private List<ConversionValidator> getConversionValidators() {
		List<ConversionValidator> validators = new ArrayList<ConversionValidator>();
		validators.add(eventValidator);
		validators.add(serverAddressValidator);
		validators.add(fraudDetectionValidator);
		return validators;
	}

	private String executeConversion(String internalTransactionId, String offerProviderTransactionId) {
		int realmId = -1;
		APIResponse response = new APIResponse();
		try {
			String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
			ConversionData data = prepareConversionData(internalTransactionId, offerProviderTransactionId, ipAddress);
			logConversionRequest(data);
			for (ConversionValidator validator : getConversionValidators()) {
				if (validator.validate(data)) {
					logger.info("Validator: " + validator.getClass() + " OK");
				} else {
					logger.info("Validator: " + validator.getClass() + " FAILED");
					Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, -1, LogStatus.ERROR,
							Application.CONVERSION_ACTIVITY + " status: " + RespStatusEnum.FAILED + " Validator: "
									+ validator.getClass() + " FAILED for data:" + data);
					if (!(validator instanceof ServerAddressValidator)) {
						apiHelper.setupFailedResponseForError(response, validator.getInvalidValueErrorCode());
						return apiHelper.getGson().toJson(response);
					}
				}
			}

			loadRealm(data);

			if (data.getUserEvent().getConversionDate() != null) {
				logInvalidConversion(data);
				apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_DUPLICATE_CONVERSION_IDENTIFIED);
				return apiHelper.getGson().toJson(response);
			} else {
				recordSuccessfullConversion(data);
				String result = rewardManager.issueReward(data.getRealm(), data.getUserEvent(), null, false);
				logger.info(result);
				return result;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, LogStatus.ERROR,
					Application.CONVERSION_ACTIVITY + " status: " + RespStatusEnum.FAILED + "  code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			return apiHelper.getGson().toJson(response);
		}
	}

	private void loadRealm(ConversionData data) {
		try {
			int realmId = data.getUserEvent().getRealmId();
			RealmEntity realm = daoRealm.findById(realmId);
			data.setRealm(realm);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private ConversionData prepareConversionData(String internalTransactionId, String offerProviderTransactionId,
			String ipAddress) {
		ConversionData data = new ConversionData();
		data.setInternalTransactionId(internalTransactionId);
		data.setOfferProviderTransactionId(offerProviderTransactionId);
		data.setIpAddress(ipAddress);
		return data;
	}

	private void logConversionRequest(ConversionData data) {
		String dataContent = "retrieved successful conversion data:" + data;
		logger.info(dataContent);
		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, -1, LogStatus.OK,
				Application.CONVERSION_ACTIVITY + " " + Application.CONVERSION_IDENTIFIED + " received request: "
						+ dataContent);
	}

	private void logInvalidConversion(ConversionData data) {
		int realmId = data.getRealm().getId();
		UserEventEntity event = data.getUserEvent();
		String ipAddress = data.getIpAddress();
		String internalTransactionId = data.getInternalTransactionId();

		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, LogStatus.ERROR,
				Application.CONVERSION_ACTIVITY + " status: " + RespStatusEnum.FAILED + " code: "
						+ RespCodesEnum.ERROR_DUPLICATE_CONVERSION_IDENTIFIED + " ip: " + ipAddress + " internalT: "
						+ internalTransactionId
						+ " aborting conversion registration - user already triggered reward request at: "
						+ event.getRewardRequestDate().toString() + " internal transaction id: "
						+ event.getInternalTransactionId() + " phone number: " + event.getPhoneNumber() + " userId: "
						+ event.getUserId());
		logger.severe(
				"CONVERSION_REGISTRATION aborting conversion registration - conversion already identified for this transaction id request at: "
						+ event.getConversionDate().toString() + " internal transaction id: "
						+ event.getInternalTransactionId() + " phone number: " + event.getPhoneNumber() + " userId: "
						+ event.getUserId() + " transaction id: " + event.getInternalTransactionId());
	}

	private void recordSuccessfullConversion(ConversionData data) {
		RealmEntity realm = data.getRealm();
		int realmId = data.getRealm().getId();
		UserEventEntity event = data.getUserEvent();
		String ipAddress = data.getIpAddress();
		String internalTransactionId = data.getInternalTransactionId();
		String offerProviderTransactionId = data.getOfferProviderTransactionId();
		
		event.setTransactionId(offerProviderTransactionId);
		event.setConversionDate(new Timestamp(System.currentTimeMillis()));

		daoUserEvent.createOrUpdate(event, 1);
		Application.getElasticSearchLogger().indexLog(Application.CONVERSION_ACTIVITY, realmId, LogStatus.OK,
				Application.CONVERSION_ACTIVITY + " " + Application.CONVERSION_ACTIVITY_SUCCESS
						+ " successfully persisted conversion: internalT: " + internalTransactionId + " phone number: "
						+ event.getPhoneNumber() + " userId: " + event.getUserId());

		Application.getElasticSearchLogger().indexUserClick(realmId, event.getPhoneNumber(), "", event.getDeviceType(),
				event.getOfferId(), event.getOfferSourceId().toLowerCase(), event.getOfferTitle(),
				event.getAdProviderCodeName(), event.getRewardTypeName(), event.getOfferPayoutInTargetCurrency(),
				event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(), realm.getName(), "",
				UserEventType.conversion.toString(), event.getInternalTransactionId(), "",
				UserEventCategory.INSTALL.toString(), "", "", ipAddress, event.getCountryCode(), event.isInstant(),
				event.getApplicationName(), event.getAdvertisingId(), event.getIdfa(), event.isTestMode(),
				event.getCustomRewardValue(), event.getCustomRewardCurrencyCode());

		rewardManager.updateUserConversionHistory(event);
		
	}

}
