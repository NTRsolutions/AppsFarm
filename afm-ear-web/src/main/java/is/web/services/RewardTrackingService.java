package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.DeviceType;
import is.ejb.bl.business.EventQueueStatus;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.RewardStatus;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.apns.IOSNotificationSender;
import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOEventQueueEntity;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOUserEventFailed;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.EventQueueEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.UserEventFailedEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * A simple REST service which is able to say hello to someone using
 * HelloService Please take a look at the web.xml where JAX-RS is enabled
 */

@Path("/")
public class RewardTrackingService {
	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOUserEventFailed daoUserEventFailed;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Context
	private ServletContext context;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private DAOEventQueueEntity daoEventQueue;
	
	@Inject
	private DAOAppUser daoAppUser;

	
	@GET
	@Produces("application/json")
	@Path("/v1/reward/{internalTransactionId}/{status}/{statusMessage}/")
	public String trackReward(@PathParam("internalTransactionId") int internalTransactionId, @PathParam("status") String status,
			@PathParam("statusMessage") String statusMessage) {

		return executeTrackReward(internalTransactionId, status, statusMessage, null);
	}

	@GET
	@Produces("application/json")
	@Path("/v1/reward/{internalTransactionId}/{status}/{statusMessage}/{vouchers}/")
	public String trackReward(@PathParam("internalTransactionId") int internalTransactionId, @PathParam("status") String status,
			@PathParam("statusMessage") String statusMessage, @PathParam("vouchers") PathSegment vouchersPathSegment) {

		return executeTrackReward(internalTransactionId, status, statusMessage, vouchersPathSegment);
	}

	private String executeTrackReward(int internalTransactionId, String status, String statusMessage, PathSegment vouchersPathSegment) {
		// http://127.0.0.1:8080/ab/svc/tr/67602cc2d832/SUCCESS
		int realmId = -1;

		try {
			boolean isVoucherRewardIssued = false;
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}

			String statusMessageValue = "";
			if (statusMessage != null && statusMessage.length() > 0) {
				statusMessageValue = statusMessage;
			}
			String dataContent = "intercepted reward notification: " + "internalTransactionId: " + internalTransactionId + " ip: " + ipAddress
					+ " status: [" + status + "]" + " statusMessage: [" + statusMessage + "]" + " vouchers: [" + vouchersPathSegment + "]";
			logger.info(dataContent);

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.OK, Application.REWARD_ACTIVITY + " "
					+ Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_IDENTIFIED + " " + dataContent);

			// validate received data
			// TODO for testing we pass UserEvent id
			// UserEventEntity event =
			// daoUserEvent.findByInternalTransactionId(internalTransactionId);
			UserEventEntity event = daoUserEvent.findById(internalTransactionId);
			if (event == null) {
				// logger.info("REWARD_DATA_UPDATE event with internal
				// transaction id: "+internalTransactionId+" not found");
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.ERROR,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED
								+ " internalT: " + event.getInternalTransactionId() + " event with internal transaction id: " + internalTransactionId
								+ " not found " + "status: " + RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_INVALID_TRANSACTION_ID);
				return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_INVALID_TRANSACTION_ID + "\"}";
			}
			if (vouchersPathSegment != null) {
				List<String> voucherCodes = new ArrayList<String>();

				String vouchersPath = vouchersPathSegment.getPath();
				logger.info("Vouchers path: " + vouchersPath);
				if (vouchersPath.length() > 0) {
					String[] vouchersPathSplit = vouchersPath.split(";");
					for (int i = 0; i < vouchersPathSplit.length; i++) {
						if (i != 0) {
							voucherCodes.add(vouchersPathSplit[i]);
						}
					}
				}
				logger.info("Received voucherCodes:" + voucherCodes);
				if (voucherCodes.size() > 0) {
					requestVoucherReward(event, voucherCodes);
					isVoucherRewardIssued = true;
				}
			}

			// logger.info("REWARD_DATA_UPDATE updating user event with reward
			// notification data");
			// generate event object and persist it in db
			if (event.getRewardDate() != null) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.WARNING,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " reward date already set for internal trans id: "
								+ internalTransactionId + ": " + " internalT: " + event.getInternalTransactionId() + event.getRewardDate().toString()
								+ " udpating with new time");
				// do no process it - return response at this stage
				return "{\"response\":\" status: " + RespStatusEnum.WARNING + " code: " + RespCodesEnum.WARNING_REWARD_ALREADY_INTERCEPTED
						+ " transactionId: " + event.getId() + "\"}";
			}

			if (status.toUpperCase().equals(RewardStatus.SUCCESS.toString())) {
				event.setRewardResponseStatus(RewardStatus.SUCCESS.toString());
			} else if (status.toUpperCase().equals(RewardStatus.FAILED.toString())) {
				event.setRewardResponseStatus(RewardStatus.FAILED.toString());
			} else if (status.toUpperCase().equals(RewardStatus.PENDING.toString())) {
				event.setRewardResponseStatus(RewardStatus.PENDING.toString());
			} else {
				event.setRewardResponseStatus(RewardStatus.UNKNOWN.toString());
				event.setQueueStatus(EventQueueStatus.PROCESSED.toString());

				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.ERROR,
						Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED
								+ " Unknown reward status code" + " status: " + RespStatusEnum.FAILED + " code: "
								+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);

				// update reward date
				event.setRewardResponseStatusMessage(statusMessageValue);
				event.setRewardDate(new Timestamp(System.currentTimeMillis()));
				event.setApproved(false);
				daoUserEvent.createOrUpdate(event, 8);

				// update conversion history (needed to filter out already
				// clicked offers for particular user)
				rewardManager.updateUserConversionHistory(event);

				if (event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
					Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, realmId, LogStatus.ERROR,
							Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED
									+ " Unknown reward status code" + " status: " + RespStatusEnum.FAILED + " code: "
									+ RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE);
				}
				return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_UNKNOWN_REWARD_STATUS_CODE + "\"}";
			}

			// update reward date
			event.setRewardResponseStatusMessage(statusMessageValue);
			event.setRewardDate(new Timestamp(System.currentTimeMillis()));
			event.setQueueStatus(EventQueueStatus.PROCESSED.toString());

			if (event.getRewardResponseStatus().equals(RewardStatus.SUCCESS.toString())) {
				event.setApproved(true);
			} else {
				event.setApproved(false);
			}
			event = daoUserEvent.createOrUpdate(event, 8);

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " reward response for transaction id: "
							+ internalTransactionId + " internalT: " + event.getInternalTransactionId() + " status: "
							+ event.getRewardResponseStatus() + " code: " + RespCodesEnum.OK_NO_CONTENT);

			// update conversion history (needed to filter out already clicked
			// offers for particular user)
			rewardManager.updateUserConversionHistory(event);

			// handle wallet payout status update
			if (event.isInstant() && event.getUserEventCategory().equals(UserEventCategory.WALLET_PAY_OUT.toString())) {
				RealmEntity realm = daoRealm.findById(event.getRealmId());
				// we only udpate status and if failed - return cash back to
				// user wallet here
				//rewardManager.updateWalletPayoutTransactionStatus(realm, event, false);
			}

			// send notification to user
			// if reward wasn't issued via vouchers
			if (isVoucherRewardIssued == false) {
				if (event.getRewardResponseStatus().equals(RewardStatus.SUCCESS.toString())) {
					//notificationManager.sendRewardNotification(event, true, false);
				} else {
					//notificationManager.sendRewardNotification(event, false, false);
				}
			}

			if (event.getUserEventCategory().equals(UserEventCategory.INVITE.toString())) {
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.OK,
						Application.INVITATION_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY
								+ " successfully rewarded event with internal transaction id: " + internalTransactionId + " internalT: "
								+ event.getInternalTransactionId() + " status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT);
			}

			// if transaction has FAILED status as a response from MODE then
			// store it in failed transactions table so we could retry it
			// another time
			if (event.getRewardResponseStatus().equals(RewardStatus.FAILED.toString())) {
				pushToFailedTransactionsTable(event);
			}

			// process mode queue mechanism
			RealmEntity realm = daoRealm.findById(event.getRealmId());
			if (realm.isModeQueueing()) {
				// remove already processed event from the queue
				EventQueueEntity processedEventQueueElement = daoEventQueue.findByEventId(event.getId());
				if (processedEventQueueElement != null) {
					daoEventQueue.delete(processedEventQueueElement);

					Application.getElasticSearchLogger().indexLog(Application.EVENT_QUEUE_ACTIVITY, event.getRealmId(), LogStatus.OK,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_DELETE + " "
									+ " deleting processed event from queue with internalT: " + event.getInternalTransactionId() + " payout: "
									+ event.getOfferPayout() + " (in target currency: " + event.getRevenueValue() + ") " + " phone: "
									+ event.getPhoneNumberExt() + " " + event.getPhoneNumber() + " rewardType: " + event.getRewardTypeName()
									+ " reward: " + event.getRewardValue() + " reward currency: " + event.getRewardIsoCurrencyCode());
				}

				// trigger another processing using rewardManager
				EventQueueEntity newEventQueueElementToProcessFromQueue = daoEventQueue.getNextEventByUserId(event.getUserId());
				if (newEventQueueElementToProcessFromQueue != null) {
					UserEventEntity newEventToSendToMode = daoUserEvent.findById(newEventQueueElementToProcessFromQueue.getEventId());
					//directly request reward for this transaction
					//rewardManager.requestRewardMode(newEventToSendToMode, null, false);
				} else {
					Application.getElasticSearchLogger().indexLog(Application.EVENT_QUEUE_ACTIVITY, event.getRealmId(), LogStatus.OK,
							Application.EVENT_QUEUE_ACTIVITY + " " + Application.EVENT_QUEUE_NO_ELEMENTS_TO_PROCESS + " "
									+ " no further elements to process found for user with" + " phone: " + event.getPhoneNumberExt() + " "
									+ event.getPhoneNumber() + " rewardType: " + event.getRewardTypeName() + " userId: " + event.getUserId());
				}
			}

			return "{\"response\":\" status: " + RespStatusEnum.SUCCESS + " code: " + RespCodesEnum.OK_NO_CONTENT + "\"}";
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED + " Error: "
							+ exc.toString() + " status: " + RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
							+ " error: " + exc.toString());

			return "{\"response\":\" status: " + RespStatusEnum.FAILED + " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: "
					+ exc.toString() + "\"}";
		}
	}

	private void pushToFailedTransactionsTable(UserEventEntity event) {
		RealmEntity realm = null;
		int realmId = -1;
		try {
			realm = daoRealm.findById(event.getRealmId());
			realmId = realm.getId();

			// create copy of event and store it in failed transactions table
			UserEventFailedEntity uef = new UserEventFailedEntity();
			uef.setAdProviderCodeName(event.getAdProviderCodeName());
			uef.setAdvertisingId(event.getAdvertisingId());
			uef.setAfaNetworkName(event.getAfaNetworkName());
			uef.setAndroidDeviceToken(event.getAndroidDeviceToken());
			uef.setApplicationName(event.getApplicationName());
			uef.setApproved(event.isApproved());
			uef.setCarrierName(event.getCarrierName());
			uef.setClickDate(event.getClickDate());
			uef.setConversionDate(event.getConversionDate());
			uef.setCountryCode(event.getCountryCode());
			uef.setDeviceId(event.getDeviceId());
			uef.setDeviceType(event.getDeviceType());
			uef.setEmail(event.getEmail());
			uef.setId(event.getId());
			uef.setIdfa(event.getIdfa());
			uef.setInstant(event.isInstant());
			uef.setInternalTransactionId(event.getInternalTransactionId());
			uef.setIosDeviceToken(event.getIosDeviceToken());
			uef.setIpAddress(event.getIpAddress());
			uef.setLoginName(event.getLoginName());
			uef.setMobileAppNotificationDate(event.getMobileAppNotificationDate());
			uef.setMobileAppNotificationStatus(event.getMobileAppNotificationStatus());
			uef.setMobileAppNotificationStatusMessage(event.getMobileAppNotificationStatusMessage());
			uef.setOfferId(event.getOfferId());
			uef.setOfferPayout(event.getOfferPayout());
			uef.setOfferPayoutInTargetCurrency(event.getOfferPayoutInTargetCurrency());
			uef.setOfferPayoutInTargetCurrencyIsoCurrencyCode(event.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
			uef.setOfferPayoutIsoCurrencyCode(event.getOfferPayoutIsoCurrencyCode());
			uef.setOfferPayoutInTargetCurrencyIsoCurrencyCode(event.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
			uef.setOfferRedirectUrl(event.getOfferRedirectUrl());
			uef.setOfferSourceId(event.getOfferSourceId());
			uef.setOfferTitle(event.getOfferTitle());
			uef.setPhoneNumber(event.getPhoneNumber());
			uef.setPhoneNumberExt(event.getPhoneNumberExt());
			uef.setProfilSplitFraction(event.getProfilSplitFraction());
			uef.setProfitValue(event.getProfitValue());
			uef.setRealmId(event.getRealmId());
			uef.setRevenueValue(event.getRevenueValue());
			uef.setRewardDate(event.getRewardDate());
			uef.setRewardIsoCurrencyCode(event.getRewardIsoCurrencyCode());
			uef.setRewardName(event.getRewardName());
			uef.setRewardRequestDate(event.getRewardRequestDate());
			uef.setRewardRequestStatus(event.getRewardRequestStatus());
			uef.setRewardRequestStatusMessage(event.getRewardRequestStatusMessage());
			uef.setRewardResponseStatus(event.getRewardResponseStatus());
			uef.setRewardResponseStatusMessage(event.getRewardResponseStatusMessage());
			uef.setRewardTypeName(event.getRewardTypeName());
			uef.setRewardValue(event.getRewardValue());
			uef.setTestMode(event.isTestMode());
			uef.setTransactionId(event.getTransactionId());
			uef.setUserEventCategory(event.getUserEventCategory());
			uef.setUserId(event.getUserId());

			// create db entry
			daoUserEventFailed.createOrUpdate(uef, 1);

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.OK,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED + " "
							+ Application.REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE
							+ " successfully pushed to failed transactions table event with transaction id: " + event.getId() + " internalT: "
							+ event.getInternalTransactionId());

		} catch (Exception exc) {
			exc.printStackTrace();
			logger.severe(exc.toString());

			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, realmId, LogStatus.ERROR,
					Application.REWARD_ACTIVITY + " " + Application.REWARD_RESPONSE_ACTIVITY + " " + Application.REWARD_RESPONSE_FAILED + " "
							+ Application.REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE + " "
							+ Application.REWARD_RESPONSE_PUSH_TO_FAILED_TRANSACTIONS_TABLE_FAILED + " Error: " + exc.toString());
		}
	}
	
	
	private void requestVoucherReward(UserEventEntity event, List<String> voucherCodesList) {
		try {
			if (event != null) {
				AppUserEntity appUser = daoAppUser.findById(event.getUserId());

				logger.info("Issuing voucher codes for user: " + appUser.getId() + " codes:" + voucherCodesList);
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.OK,
						Application.REWARD_ACTIVITY + "Issuing voucher codes for user: " + appUser.getId() + " codes:" + voucherCodesList);
				
				event.setMobileAppNotificationStatus("SUCCESS");
				event.setMobileAppNotificationStatus(new Timestamp(new Date().getTime()).toString());
				event.setMobileAppNotificationStatusMessage("Vouchers sent via Donkey successfully");
				daoUserEvent.createOrUpdate(event, 0);
				//notificationManager.sendVouchers(appUser, voucherCodesList);
			}
			else{
				logger.info("Cant issue voucher reward because event is null");
				Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, -1, LogStatus.ERROR,
						Application.REWARD_ACTIVITY + "Cant issue voucher reward because event is null");
				event.setMobileAppNotificationStatus("FAILED");
				event.setMobileAppNotificationStatus(new Timestamp(new Date().getTime()).toString());
				event.setMobileAppNotificationStatusMessage("Null event");
				daoUserEvent.createOrUpdate(event, 0);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_ACTIVITY, event.getRealmId(), LogStatus.ERROR,
					Application.REWARD_ACTIVITY + exc.toString());
			event.setMobileAppNotificationStatus("FAILED");
			event.setMobileAppNotificationStatus(new Timestamp(new Date().getTime()).toString());
			event.setMobileAppNotificationStatusMessage(exc.toString());
			daoUserEvent.createOrUpdate(event, 0);
		}
	}
}