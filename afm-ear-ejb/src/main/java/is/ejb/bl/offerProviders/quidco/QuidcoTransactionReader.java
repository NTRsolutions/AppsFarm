package is.ejb.bl.offerProviders.quidco;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.sql.Date;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.bluepodmedia.sdk.quidco.offer.OfferManager;
import com.bluepodmedia.sdk.quidco.offer.dto.Activity;
import com.bluepodmedia.sdk.quidco.offer.dto.ActivityState;
import com.bluepodmedia.sdk.quidco.offer.dto.ActivityStates;
import com.bluepodmedia.sdk.quidco.offer.dto.Delta;
import com.bluepodmedia.sdk.quidco.offer.utils.DeltaEntity;

@Stateless
public class QuidcoTransactionReader {

	private OfferManager offerManager;

	@Inject
	private Logger logger;

	@Inject
	private QuidcoManager quidcoManager;

	@Inject
	private DAORealm daoRealm;
	
	@PostConstruct
	public void init() {
		offerManager = new OfferManager();
	}

	// this should be timer method
	public void loadTransactions() {
		RealmEntity realmEntity = findRealmWithId(4);
		if (!realmEntity.isQuidcoTimerEnabled()){
			logger.info("Quidco delta timer is disabled.");
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		Date endDate = new Date(new java.util.Date().getTime());
		//Date startDate = endDate;
	    Date startDate = new Date(calendar.getTime().getTime());
		Delta delta = callDeltaEndpoint(startDate, endDate);
		logger.info("Received Delta: " + delta.getActivity());
		Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4, LogStatus.OK,
				"Received delta for startDate: " + startDate + " endDate:" + endDate + " with result: "
						+ delta.toString());

		if (delta != null && delta.getActivity() != null && delta.getActivity().size() > 0) {
			logger.info("Calling activity/states for all activities.");

			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
					LogStatus.OK, "Calling activity/states for all activities: " + delta.getActivity());

			for (Activity activity : delta.getActivity()) {
				callActivityState(activity.getUserId(), activity.getTransactionId());
			}
		}
	}

	private Delta callDeltaEndpoint(Date startDate, Date endDate) {
		try {
			logger.info("Calling delta endpoint with startDate: " + startDate + " endDate: " + endDate);

			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
					LogStatus.OK, "Calling delta endpoint with startDate: " + startDate + " endDate: " + endDate);
			Delta delta = offerManager.getDelta(DeltaEntity.activity,startDate, endDate);
			return delta;
		} catch (Exception exception) {
			exception.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
					LogStatus.OK, "Calling delta endpoint with startDate: " + startDate + " endDate: " + endDate
							+ " failed with exception: " + exception.toString());
			return new Delta();
		}
	}

	private void callActivityState(int userId, int transactionId) {
		try {
			logger.info("Calling activity state for userId: " + userId + " and transactionId: " + transactionId);
			ActivityStates activityStates = offerManager.getActivityStates(userId, transactionId, true);

			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
					LogStatus.OK, "Called activity/state for userId: " + userId + " and transactionId: " + transactionId
							+ " with result : " + activityStates.toString());

			if (activityStates != null && activityStates.getActivityStates() != null
					&& activityStates.getActivityStates().size() > 0) {
				logger.info("Sending activity state object to process...");
				for (ActivityState activityState : activityStates.getActivityStates()) {
					
					Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
							LogStatus.OK, "Sending activity state object to process : " + activityState.toString());
					quidcoManager.processQuidcoEvent(activityState);
				}

			}
		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.QUIDCO_TRANSACTION_READER_ACTIVITY, 4,
					LogStatus.OK, "Called activity/state for userId: " + userId + " and transactionId: " + transactionId
							+ " failed with exception: " + exception.toString());
			exception.printStackTrace();
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

}
