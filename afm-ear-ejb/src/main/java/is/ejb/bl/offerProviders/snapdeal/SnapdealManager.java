package is.ejb.bl.offerProviders.snapdeal;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.CategoryOffers;
import is.ejb.bl.offerProviders.snapdeal.CategoryOffers.ProductsEntry;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOSnapdealOffers;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.SnapdealOffersEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Stateless
public class SnapdealManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private MailManager mailManager;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOSnapdealOffers daoSnapdealOffers;

	private final String SNAPDEAL_REPORTING_API_URL = "http://affiliate-feeds.snapdeal.com/feed/api/order";
	private final String SNAPDEAL_ACCESS_TOKEN = "700e338e91da7397893a497b6c0f45";
	private final String SNAPDEAL_ACCESS_ID = "88743";
	private final int SNAPDEAL_AWAIT_TIME = 42;

	// this method should be called in timer
	// it can be called every hour
	public void updateSnapdealReports() {
		logger.info("************** SNAPDEAL REPORT UPDATE **************");
		RealmEntity realm = getRealmWithId(4);
		if (!realm.isSnapdealReportTimerEnabled()) {
			logger.info("Snapdeal report timer is disabled.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + " Snapdeal report timer is disabled");

			return;
		}
		logger.info("Updating snapdeal reports.");
		Date date = new Date();

		SnapdealReportResponse approvedReport = getSnapdealReports(date, date, SnapdealReportType.approved);
		if (approvedReport != null) {
			logger.info("Successfully downloaded approved report.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + " Downloaded approved report: " + approvedReport.toString());

			processSnapdealReport(approvedReport, SnapdealReportType.approved);
		} else {
			logger.info("Approved report is null");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_REPORT_TIMER + " Downloaded approved failed - report is null");
		}

		SnapdealReportResponse cancelledReport = getSnapdealReports(date, date, SnapdealReportType.cancelled);
		if (cancelledReport != null) {
			logger.info("Successfully downloaded cancelled report.");
			logger.info(cancelledReport.toString());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + " Downloaded cancelled report: " + cancelledReport.toString());
			processSnapdealReport(cancelledReport, SnapdealReportType.cancelled);
		} else {
			logger.info("Cancelled report is null");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_REPORT_TIMER + " Downloaded cancelled failed - report is null");
		}
	}

	public void manuallyRewardOffer(UserEventEntity event) {
		logger.info("Completing manually event with internalTransactionId: " + event.getInternalTransactionId());
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
				Application.SNAPDEAL_MANAGER_ACTIVITY + "Completing manually event with internalT: "
						+ event.getInternalTransactionId());

		event.setRewardResponseStatus("SUCCESS");
		event.setRewardResponseStatusMessage("COMPLETED MANUALLY: " + event.getRewardDate());
		daoUserEvent.createOrUpdate(event, 0);
		RealmEntity realm = findRealmWithId(event.getRealmId());
		updateConversionHistory(event);
		rewardManager.issueReward(realm, event, null, false);
		indexSnapdealConversion(event);

	}

	public void updateSnapdealApprovedOffers() {
		logger.info("Updating snapdeal approved offers");
		RealmEntity realmEntity = getRealmWithId(4);
		if (!realmEntity.isSnapdealReportTimerEnabled()) {
			logger.info("Snapdeal approved offers timer is disabled.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_6_WEEK_TIMER + " Timer is disabled");
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -50);
		Timestamp startTime = new Timestamp(calendar.getTime().getTime());
		Timestamp endTime = new Timestamp(new Date().getTime());
		List<UserEventEntity> events = getApprovedSnapdealEventsInRange(startTime, endTime);

		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
				Application.SNAPDEAL_6_WEEK_TIMER + " Selecting snapdeal offers in range startDate: " + startTime
						+ ", endTime: " + endTime + ". Selected : " + events.size() + " events.");

		logger.info("Selected : " + events.size() + " events");
		/**
		 * TODO mzj make sure that once processed after 6 weeks we do not repeat
		 * processing of this transaction (e.g, do not reward the user multiple
		 * times) possibly need to add flag (if not already added) to mark that
		 * this user event (transaction) was already processed for approval
		 **/

		for (UserEventEntity event : events) {
			if (isEventPassedAwaitTime(event) && !event.getRewardResponseStatus().equals("SUCCESS")
					&& !event.getRewardResponseStatus().equals("FAILED")) {
				if (event.getRewardResponseStatus().equals(SnapdealReportType.approved.toString())) {
					logger.info("Completing approved event with internalT: " + event.getInternalTransactionId());
					Application.getElasticSearchLogger()
							.indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
									Application.SNAPDEAL_6_WEEK_TIMER
											+ "Completing approved event with internalTransactionId: "
											+ event.getInternalTransactionId());

					event.setRewardResponseStatus("SUCCESS");
					event.setRewardResponseStatusMessage("COMPLETED AFTER 6 WEEK: " + event.getRewardDate());
					daoUserEvent.createOrUpdate(event, 0);
					RealmEntity realm = findRealmWithId(event.getRealmId());
					updateConversionHistory(event);
					rewardManager.issueReward(realm, event, null, false);
					indexSnapdealConversion(event);

				}
				if (event.getRewardResponseStatus().equals(SnapdealReportType.cancelled.toString())) {
					logger.info("Completing cancelled event with internalT: " + event.getInternalTransactionId());
					Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4,
							LogStatus.OK, Application.SNAPDEAL_6_WEEK_TIMER
									+ "Completing cancelled event with internalT: " + event.getInternalTransactionId());

					event.setRewardResponseStatus("FAILED");
					event.setRewardResponseStatusMessage("COMPLETED AFTER 6 WEEK: " + event.getRewardDate());
					daoUserEvent.createOrUpdate(event, 0);
					updateConversionHistory(event);
					indexSnapdealConversion(event);

				}
			} else {
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
						Application.SNAPDEAL_6_WEEK_TIMER + "Not completing event with internalT: "
								+ event.getInternalTransactionId());
			}
		}

	}

	private RealmEntity findRealmWithId(int id) {
		try {
			return daoRealm.findById(id);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private boolean isEventPassedAwaitTime(UserEventEntity event) {
		logger.info("Calculating days since last event update for event with internalTransactionId: "
				+ event.getInternalTransactionId());
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
				Application.SNAPDEAL_6_WEEK_TIMER
						+ "Calculating days since last event update for event with internalT: "
						+ event.getInternalTransactionId());
		Timestamp lastUpdateTime = event.getConversionDate();
		if (lastUpdateTime == null) {
			logger.info("Couldnt check if event passwrd await time because conversion date is null");
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastUpdateTime.getTime());
		calendar.add(Calendar.DAY_OF_MONTH, SNAPDEAL_AWAIT_TIME);
		logger.info("calendar time: " + calendar.getTime());
		if (calendar.getTime().compareTo(new Date()) <= 0) {
			return true;
		} else {
			return false;
		}

	}

	private List<UserEventEntity> getApprovedSnapdealEventsInRange(Timestamp startTime, Timestamp endTime) {
		try {
			logger.info("Selecting approved snapdeal events in range startTime: " + startTime + " endTIme: " + endTime);
			return daoUserEvent.findSnapdealApprovedEventsByTimeRange(startTime, endTime);
		} catch (Exception exception) {
			exception.printStackTrace();
			return new ArrayList<UserEventEntity>();
		}
	}

	public SnapdealReportResponse getSnapdealReports(Date startDate, Date endDate, SnapdealReportType type) {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		CloseableHttpResponse response = null;
		String reportResponseInJson = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SnapdealReportResponse reportResponse = null;

		String reportingApiUrl = SNAPDEAL_REPORTING_API_URL + "?startDate=" + dateFormat.format(startDate) + "&endDate="
				+ dateFormat.format(endDate) + "&status=" + type.toString();
		// String reportingApiUrl = SNAPDEAL_REPORTING_API_URL +
		// "?startDate=2016-05-01" + "&endDate="
		// + dateFormat.format(endDate) + "&status=" + type.toString();

		try {
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + "Making a url request for: " + reportingApiUrl);
			logger.info("Making a url request for: " + reportingApiUrl);
			httpClient = HttpClients.createDefault();
			httpPost = new HttpPost(reportingApiUrl);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("Snapdeal-Affiliate-Id", SNAPDEAL_ACCESS_ID));
			nvps.add(new BasicNameValuePair("Snapdeal-Token-Id", SNAPDEAL_ACCESS_TOKEN));
			nvps.add(new BasicNameValuePair("Accept", "application/json"));
			for (NameValuePair h : nvps) {
				httpPost.addHeader(h.getName(), h.getValue());
			}
			response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			logger.info("Output from Server .... \n");
			String inputLine;
			StringBuffer resp = new StringBuffer();

			while ((inputLine = br.readLine()) != null) {
				resp.append(inputLine);
			}
			reportResponseInJson = resp.toString();
			logger.info("Got resposne: " + reportResponseInJson);
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + " Response from snapdeal api: " + reportResponseInJson);
			if (reportResponseInJson != null && !reportResponseInJson.toLowerCase().contains("error")) {
				Gson gson = new GsonBuilder().setDateFormat("MM/dd/yyyy hh:mm:ss").create();
				reportResponse = gson.fromJson(reportResponseInJson, SnapdealReportResponse.class);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
				httpClient.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return reportResponse;
	}

	private void processSnapdealReport(SnapdealReportResponse report, SnapdealReportType type) {
		if (report == null) {
			logger.info("Couldn't process null report.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_REPORT_TIMER + "Couldn't process null report");
			return;
		}
		if (report.getProductDetails() == null || report.getProductDetails().size() == 0) {
			logger.info("There is no product details to process.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_REPORT_TIMER + "Couldn't process null report");
			return;
		}
		for (SnapdealProductDetail detail : report.getProductDetails()) {
			processSnapdealProduct(detail, type);
		}

	}

	private void processSnapdealProduct(SnapdealProductDetail detail, SnapdealReportType type) {
		logger.info("Processing snapdeal product detail with internalTransactionId: " + detail.getAffiliateSubId1());
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
				Application.SNAPDEAL_REPORT_TIMER + " Processing snapdeal product detail" + detail.toString()
						+ " Type: " + type.toString());

		UserEventEntity event = getUserEventWithTransactionId(detail.getAffiliateSubId1());
		if (event == null) {
			logger.info("Couldn't find event with transactionId: " + detail.getAffiliateSubId1());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_REPORT_TIMER + " Couldn't find event with internalT: "
							+ detail.getAffiliateSubId1());
			return;
		}

		if (event.getRewardResponseStatus() == null || ((!event.getRewardResponseStatus().equals("SUCCESS")
				&& !event.getRewardResponseStatus().equals("FAILED"))
				&& !event.getRewardResponseStatus().equals(type.toString()))) {
			logger.info("Event with transactionId: " + detail.getAffiliateSubId1() + " changed status. Current status: "
					+ event.getRewardResponseStatus() + " New status: " + type.toString());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + "Event with internalT: " + detail.getAffiliateSubId1()
							+ " changed status. Current status: " + event.getRewardResponseStatus() + " New status: "
							+ type.toString());

			event.setRewardResponseStatus(type.toString());
			event.setRewardResponseStatusMessage(detail.getDateTime().toString());
			event.setRewardRequestDate(detail.getDateTime());
			event.setConversionDate(detail.getDateTime());
			if (detail.getCommissionEarned() > 0) {
				double[] denominatedCommision = denominateCommision(detail.getCommissionEarned(), event);
				event.setOfferPayout(detail.getCommissionEarned());
				event.setOfferPayoutInTargetCurrency(detail.getCommissionEarned());
				event.setOfferPayoutInTargetCurrencyIsoCurrencyCode("INR");
				event.setOfferPayoutIsoCurrencyCode("INR");
				event.setRewardValue(denominatedCommision[0]);
				event.setRewardIsoCurrencyCode("INR");
				event.setProfitValue(denominatedCommision[1]);
			}
			daoUserEvent.createOrUpdate(event, 0);
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + "Event with internalT: " + detail.getAffiliateSubId1()
							+ " updated. " + event.toString());

			notificationManager.sendRewardNotification(event, true, false);
			updateConversionHistory(event);
			indexSnapdealConversion(event);

		} else {
			logger.info("Event with transactionId: " + detail.getAffiliateSubId1() + "not changed status. Skipping.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_REPORT_TIMER + "Event with internalT: " + detail.getAffiliateSubId1()
							+ "not changed status. Skipping.");
		}

	}

	private void indexSnapdealConversion(UserEventEntity event) {
		RealmEntity realmEntity = findRealmWithId(event.getRealmId());
		if (realmEntity != null) {
			logger.info("Indexing snapdeal conversion");
			String eventStatus = "";
			if (event.getRewardResponseStatus() != null) {
				if (event.getRewardResponseStatus().equals(SnapdealReportType.approved.toString())) {
					eventStatus = "SNAPDEAL_APPROVED";
				}
				if (event.getRewardResponseStatus().equals(SnapdealReportType.cancelled.toString())) {
					eventStatus = "SNAPDEAL_CANCELLED";
				}
				if (event.getRewardResponseStatus().equals("SUCCESS")) {
					eventStatus = "SNAPDEAL_SUCCESS";
				}
				if (event.getRewardResponseStatus().equals("FAILED")) {
					eventStatus = "SNAPDEAL_FAILED";
				}
			}

			Application.getElasticSearchLogger().indexUserClick(event.getRealmId(), event.getPhoneNumber(),
					event.getEmail(), event.getDeviceType(), event.getOfferId(), event.getRewardResponseStatus(),
					event.getOfferTitle(), event.getAdProviderCodeName(), event.getRewardTypeName(),
					event.getOfferPayout(), event.getRewardValue(), event.getRewardIsoCurrencyCode(),
					event.getProfilSplitFraction(), realmEntity.getName(), null, UserEventType.conversion.toString(),
					event.getInternalTransactionId(), event.getCarrierName(), eventStatus, null, null, null,
					event.getCountryCode(), false, event.getApplicationName(), event.getAdvertisingId(),
					event.getIdfa(), realmEntity.isTestMode(), event.getCustomRewardValue(),
					event.getCustomRewardCurrencyCode());
		}

	}

	private void updateConversionHistory(UserEventEntity event) {
		logger.info("Updating conversion history for event with internalTransactionId: "
				+ event.getInternalTransactionId());
		Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
				Application.SNAPDEAL_MANAGER_ACTIVITY + "Updating conversion history for event with internalT: "
						+ event.getInternalTransactionId());

		ConversionHistoryEntity conversionHistory = getConversionHistoryWithUserId(event.getUserId());
		if (conversionHistory == null) {
			logger.info("Couldn't update conversion history because is null");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_MANAGER_ACTIVITY + "Couldn't update conversion history because is null");
			createUserConversionHistory(event);
			updateConversionHistory(event);
			return;
		}

		List<ConversionHistoryEntry> conversionHistoryEntries = conversionHistory.getConversionHistoryHolder()
				.getListConversionHistoryEntries();
		ConversionHistoryEntry foundEntry = null;
		for (ConversionHistoryEntry entry : conversionHistoryEntries) {
			if (entry.getInternalTransactionId().equals(event.getInternalTransactionId())) {
				foundEntry = entry;
				break;
			}
		}
		if (foundEntry == null) {
			createUserConversionHistory(event);
			updateConversionHistory(event);
			return;
		}

		if (foundEntry != null) {
			logger.info("Current status: " + event.getRewardResponseStatus());
			if (event.getRewardResponseStatus().equals(SnapdealReportType.approved.toString())) {
				logger.info("Updating conversion as approved.");
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
						Application.SNAPDEAL_MANAGER_ACTIVITY + "Updating conversion as approved for internalT:"
								+ event.getInternalTransactionId());
				foundEntry.setApproved(true);
				foundEntry.setSuccessful(false);
				sendTrackedOfferEmail(event);
			}
			if (event.getRewardResponseStatus().equals(SnapdealReportType.cancelled.toString())) {
				logger.info("Updating conversion as cancelled.");
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
						Application.SNAPDEAL_MANAGER_ACTIVITY + "Updating conversion as cancelled for internalT:"
								+ event.getInternalTransactionId());
				foundEntry.setApproved(false);
				foundEntry.setSuccessful(false);
				sendCancelledOfferEmail(event);

			}
			if (event.getRewardResponseStatus().equals("SUCCESS")) {
				logger.info("Updating conversion as success.");
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
						Application.SNAPDEAL_MANAGER_ACTIVITY + "Updating conversion as success for internalT:"
								+ event.getInternalTransactionId());
				foundEntry.setApproved(true);
				foundEntry.setSuccessful(true);
				foundEntry.setConversionTimestamp(event.getConversionDate());
				foundEntry.setClickTimestamp(event.getClickDate());
				foundEntry.setRewardStatus(event.getRewardResponseStatus());
				foundEntry.setRewardStatusMessage(event.getRewardResponseStatusMessage());
				sendApprovedOfferEmail(event);
			}
			if (event.getRewardResponseStatus().equals("FAILED")) {
				logger.info("Updating conversion as failed.");
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
						Application.SNAPDEAL_MANAGER_ACTIVITY + "Updating conversion as failed for internalT:"
								+ event.getInternalTransactionId());
				foundEntry.setApproved(false);
				foundEntry.setSuccessful(false);
				foundEntry.setConversionTimestamp(event.getConversionDate());
				foundEntry.setClickTimestamp(event.getClickDate());
				foundEntry.setRewardStatus(event.getRewardResponseStatus());
				foundEntry.setRewardStatusMessage(event.getRewardResponseStatusMessage());

			}

			logger.info("Updated entry with internalTransactionId: " + event.getInternalTransactionId());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_MANAGER_ACTIVITY + "Updated entry with internalT: "
							+ event.getInternalTransactionId());
		}

		try {
			daoConversionHistory.createOrUpdate(conversionHistory);
		} catch (IOException e) {
			e.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.OK,
					Application.SNAPDEAL_MANAGER_ACTIVITY + Arrays.toString(e.getStackTrace()));
		}
	}

	private RealmEntity getRealmWithId(int id) {
		try {
			return daoRealm.findById(id);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private AppUserEntity getAppUserWithId(int id) {
		try {
			return daoAppUser.findById(id);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private void sendApprovedOfferEmail(UserEventEntity event) {
		try {
			logger.info("Sending approved offer email.");

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_MANAGER_ACTIVITY + "Sending approved offer email to user id: "
							+ event.getUserId() + " internalT:" + event.getInternalTransactionId());
			RealmEntity realm = getRealmWithId(event.getRealmId());
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setCashbackAmount("" + event.getRewardValue());
			mailParamsHolder.setRetailer(event.getOfferTitle());
			AppUserEntity appUser = getAppUserWithId(event.getUserId());
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.SNAPDEAL_OFFER_APPROVED);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void sendCancelledOfferEmail(UserEventEntity event) {
		try {
			logger.info("Sending cancelled offer email.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_MANAGER_ACTIVITY + "Sending cancelled offer email to user id: "
							+ event.getUserId() + " internalT:" + event.getInternalTransactionId());
			RealmEntity realm = getRealmWithId(event.getRealmId());
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setCashbackAmount("" + event.getRewardValue());
			mailParamsHolder.setRetailer(event.getOfferTitle());
			AppUserEntity appUser = getAppUserWithId(event.getUserId());
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.SNAPDEAL_OFFER_CANCELLED);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void sendTrackedOfferEmail(UserEventEntity event) {
		try {
			logger.info("Sending tracked offer email.");
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, 4, LogStatus.ERROR,
					Application.SNAPDEAL_MANAGER_ACTIVITY + "Sending tracked offer email to user id: "
							+ event.getUserId() + " internalT:" + event.getInternalTransactionId());
			RealmEntity realm = getRealmWithId(event.getRealmId());
			MailParamsHolder mailParamsHolder = new MailParamsHolder();
			mailParamsHolder.setCashbackAmount("" + event.getRewardValue());
			mailParamsHolder.setRetailer(event.getOfferTitle());
			AppUserEntity appUser = getAppUserWithId(event.getUserId());
			mailParamsHolder.setEmailRecipientAddress(appUser.getEmail());
			mailManager.sendEmail(realm, mailParamsHolder, EmailType.SNAPDEAL_OFFER_TRACKED);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private ConversionHistoryEntity getConversionHistoryWithUserId(int userId) {
		try {
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(userId);
			return conversionHistory;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private double[] denominateCommision(double commisionEarned, UserEventEntity event) {
		RealmEntity realm = this.findRealmWithId(event.getRealmId());
		logger.info("Commision is : " + realm.getSnapdealPercentageCommision());
		double[] commisionDenominated = new double[2];
		commisionDenominated[0] = commisionEarned * realm.getSnapdealPercentageCommision();
		commisionDenominated[1] = commisionEarned - commisionDenominated[0];
		return commisionDenominated;
	}

	private UserEventEntity getUserEventWithTransactionId(String transactionId) {
		try {
			logger.info("Selecting event with transactionId: " + transactionId);
			return daoUserEvent.findByInternalTransactionId(transactionId);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public double calculatePotentialRewardForUser(int userId) {
		List<UserEventEntity> snapdealEvents = getSnapdealEventsForUser(userId);
		double potentialReward = 0.0;
		try {
			if (snapdealEvents != null) {
				logger.info("Calculate potential rewards for user " + userId + ": selected list with elements "
						+ snapdealEvents.size());
				Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, -1, LogStatus.OK,
						Application.SNAPDEAL_MANAGER_ACTIVITY + "Calculate potential rewards for user " + userId
								+ ": selected list with elements " + snapdealEvents.size() + " events: "
								+ snapdealEvents);

				for (UserEventEntity event : snapdealEvents) {

					String eventStatus = event.getRewardResponseStatus();
					logger.info("Event status: " + eventStatus);
					if (eventStatus != null) {
						logger.info("Event status is not null");
						if (eventStatus.equals(SnapdealReportType.approved.toString())
								|| eventStatus.equals(SnapdealReportType.cancelled.toString())) {
							logger.info("Adding : " + event.getRewardValue());
							potentialReward += event.getRewardValue();
						}
					}
				}
			} else {
				logger.info("Calculate potential rewards for user " + userId + "  failed - array null");
			}

			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, -1, LogStatus.OK,
					Application.SNAPDEAL_MANAGER_ACTIVITY + " calculate potential reward for user with user id: "
							+ userId + " result: " + potentialReward);
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL_MANAGER_ACTIVITY, -1, LogStatus.ERROR,
					Application.SNAPDEAL_MANAGER_ACTIVITY + " error during caluclating potential reward for user: "
							+ userId + ": " + Arrays.asList(exception.getStackTrace()));
		}

		return potentialReward;
	}

	private List<UserEventEntity> getSnapdealEventsForUser(int userId) {
		try {
			return daoUserEvent.findSnapdealEventsByUserId(userId);
		} catch (Exception exception) {
			exception.printStackTrace();
			return new ArrayList<UserEventEntity>();
		}
	}

	private void createUserConversionHistory(UserEventEntity event) {
		try {
			logger.info("Creating conversion history for event with internalTransacionId:"
					+ event.getInternalTransactionId());
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, event.getRealmId(), LogStatus.OK,
					Application.SNAPDEAL_MANAGER_ACTIVITY + " " + Application.DOWNLOAD_HISTORY_UPDATE
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE + " "
							+ " adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId());
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(event.getUserId());
			if (conversionHistory == null) {
				conversionHistory = new ConversionHistoryEntity();
			}

			conversionHistory.setUserId(event.getUserId());
			conversionHistory.setRealmId(event.getRealmId());
			conversionHistory.setGenerationTime(new Timestamp(System.currentTimeMillis()));

			// create new entry for this conversion
			ConversionHistoryEntry newConversionHistoryEntry = new ConversionHistoryEntry();
			newConversionHistoryEntry.setAdProviderCodeName(event.getAdProviderCodeName());
			newConversionHistoryEntry.setApproved(false);
			newConversionHistoryEntry.setClickTimestamp(event.getClickDate());
			newConversionHistoryEntry.setInternalTransactionId(event.getInternalTransactionId());
			newConversionHistoryEntry.setOfferId(event.getOfferId());
			newConversionHistoryEntry.setOfferTitle(event.getOfferTitle());
			newConversionHistoryEntry.setRewardCurrency(event.getRewardIsoCurrencyCode());
			newConversionHistoryEntry.setRewardTypeName(event.getRewardTypeName());

			newConversionHistoryEntry.setRewardValue(event.getRewardValue());

			newConversionHistoryEntry.setSourceOfferId(event.getOfferSourceId());
			newConversionHistoryEntry.setUserEventCategory(UserEventCategory.SNAPDEAL.toString());
			// add to the existing conversion history list of this user
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
			conversionHistoryHolder.getListConversionHistoryEntries().add(0, newConversionHistoryEntry);
			// persist in db (dao takes care of json serialisation)
			daoConversionHistory.createOrUpdate(conversionHistory);
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, event.getRealmId(), LogStatus.ERROR,
					Application.SNAPDEAL_CLICK + " " + Application.DOWNLOAD_HISTORY_UPDATE + " "
							+ Application.DOWNLOAD_HISTORY_CLICK_UPDATE
							+ " error adding conversion history for user with id: " + event.getUserId() + " event: "
							+ event.getOfferTitle() + " offer provider: " + event.getAdProviderCodeName()
							+ " internalT: " + event.getInternalTransactionId() + " error: " + exc.toString());
		}
	}

	public List<ProductsEntry> filterOffers(String categoryName, String searchString) {
		logger.info("Filter offers from categoryName: " + categoryName + " and searchString: :" + searchString);
		List<ProductsEntry> filteredOffers = new ArrayList<ProductsEntry>();

		try {
			if (categoryName != null && categoryName.length() > 0 && !categoryName.toLowerCase().equals("all")) {
				SnapdealOffersEntity categoryOffersEntity = daoSnapdealOffers.findByCategory(categoryName);
				List<ProductsEntry> filteredCacheList = filterOffersFromCategory(categoryOffersEntity, searchString);
				logger.info("Filtered returned: " + filteredCacheList.size());
				filteredOffers.addAll(filteredCacheList);
			} else {
				logger.info("Searching in all categories for : " + searchString);
				List<SnapdealOffersEntity> categoryOffersList = daoSnapdealOffers.findAll();
				for (SnapdealOffersEntity offer : categoryOffersList) {
					List<ProductsEntry> filteredCacheList = filterOffersFromCategory(offer, searchString);
					logger.info("Filtered returned: " + filteredCacheList.size());
					filteredOffers.addAll(filteredCacheList);
				}

			}

		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.ERROR,
					Application.SNAPDEAL_FLITER + " Error occured during filter categoryName: " + categoryName
							+ " searchString: " + searchString + " exception: " + exception.getMessage());
		}
		logger.info("Returning total " + filteredOffers.size() + " filtered offers.");
		return filteredOffers;
	}

	public List<ProductsEntry> loadProductsFromSnapdealOffers(SnapdealOffersEntity categoryOffersEntity) {
		List<ProductsEntry> productList = new ArrayList<ProductsEntry>();
		try {
			ObjectMapper mapperForOffers = new ObjectMapper();
			CategoryOffers categoryOffersHolder = mapperForOffers.readValue(categoryOffersEntity.getOffersJson(),
					CategoryOffers.class);
			productList = categoryOffersHolder.getProducts();
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.ERROR,
					Application.SNAPDEAL_FLITER + " Error occured during load products from snapdeal offers: "
							+ categoryOffersEntity.toString() + " exception: " + exception.getMessage());
		}

		return productList;
	}

	private List<ProductsEntry> filterOffersFromCategory(SnapdealOffersEntity categoryOffersEntity,
			String searchString) {
		List<ProductsEntry> filteredOffers = new ArrayList<ProductsEntry>();
		try {
			List<ProductsEntry> productList = loadProductsFromSnapdealOffers(categoryOffersEntity);
			logger.info("Got offers list: " + productList.size());
			for (ProductsEntry product : productList) {
				if (searchString != null && searchString.length() > 0) {
					if (product != null && product.getTitle() != null
							&& product.getTitle().toLowerCase().contains(searchString.toLowerCase())) {
						logger.info("Adding product: " + product.getTitle() + " substring: " + searchString);
						filteredOffers.add(product);
					}
				} else {
					filteredOffers.add(product);

				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.SNAPDEAL, -1, LogStatus.ERROR,
					Application.SNAPDEAL_FLITER + " Error occured during filter offers from category"
							+ " searchString: " + searchString + " exception: " + exception.getMessage());
		}

		return filteredOffers;
	}
	
	
	
	
	

}
