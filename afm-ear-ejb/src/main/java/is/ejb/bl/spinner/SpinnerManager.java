package is.ejb.bl.spinner;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferCurrency;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.rewardSystems.radius.SpinnerRewardsReport;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.wallet.WalletManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOSpinnerData;
import is.ejb.dl.dao.DAOSpinnerReward;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

@Stateless
public class SpinnerManager {
	@Inject
	private Logger logger;
	@Inject
	private DAOSpinnerData daoSpinnerData;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOSpinnerReward daoSpinnerReward;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoEvent;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private UniformDistributionSpinnerAlgorithm generator;

	@PostConstruct
	public void init() {

	}

	private String getSpinnerRewardsConfigurationLog(List<SpinnerRewardEntity> spinnerRewardList) {
		String log = "Spinner configuration:";
		if (spinnerRewardList != null)
			for (SpinnerRewardEntity spinnerReward : spinnerRewardList) {
				log += "[id: " + spinnerReward.getId() + " name: " + spinnerReward.getRewardName() + " probability: "
						+ spinnerReward.getRewardProbability() + "]";
			}
		return log;
	}

	public SpinnerRewardEntity generateReward(AppUserEntity appUser) {

		List<SpinnerRewardEntity> spinnerRewardList = getSpinnerRewardListForRealGenerate(appUser);
		if (spinnerRewardList == null) {
			log(LogStatus.ERROR, "Can't generate because list is null for user" + appUser.getId());

		}
		log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY, "For user with id: " + appUser.getId()
				+ " generated list is: " + spinnerRewardList + getSpinnerRewardsConfigurationLog(spinnerRewardList));
		SpinnerRewardEntity generated = null;
		while (true) {
			generated = generator.generateReward(spinnerRewardList);
			if (generated.getMonthLimit() == 0) {
				log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY,
						"************** Month limit for reward is 0 : " + generated
								+ getSpinnerRewardsConfigurationLog(spinnerRewardList));
				break;
			}

			if (generated.getMonthLimit() > generated.getMonthLimitCount()) {
				generated.setMonthLimitCount(generated.getMonthLimitCount() + 1);

				log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY,
						"************** Month limit for reward not passed and rewarding: " + generated
								+ getSpinnerRewardsConfigurationLog(spinnerRewardList));
				generated.setMonthLimitLastRewardTimestamp(new Timestamp(new Date().getTime()));
				daoSpinnerReward.createOrUpdate(generated);
				break;

			} else {
				log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY,
						"********** Month limit for reward passed: " + generated
								+ getSpinnerRewardsConfigurationLog(spinnerRewardList));
				Calendar c = new GregorianCalendar();
				c.add(Calendar.DATE, -30);
				Date date = c.getTime();
				if (generated.getMonthLimitLastRewardTimestamp() != null)
					if (date.after(new Date(generated.getMonthLimitLastRewardTimestamp().getTime()))) {
						log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY,
								"********** Month limit reset for reward: " + generated + " date:" + date
										+ getSpinnerRewardsConfigurationLog(spinnerRewardList));
						generated.setMonthLimitCount(0);
						daoSpinnerReward.createOrUpdate(generated);
					} else {
						log(LogStatus.OK, Application.SPINNER_USER_REWARD_LIST_ACTIVITY,
								"********** No month limit reset for reward: " + generated + " date:" + date
										+ getSpinnerRewardsConfigurationLog(spinnerRewardList));
					}

			}
		}

		SpinnerDataEntity spinnerData = getSpinnerDataForUser(appUser);

		if (!generated.getRewardType().equals(SpinnerRewardType.FAIL.toString())) {
			log(LogStatus.OK, Application.SPINNER_USER_REWARD_ACTIVITY, "Generated " + generated + " for user:"
					+ appUser.getId() + getSpinnerRewardsConfigurationLog(spinnerRewardList));
		}

		updateSpinnerDataCounters(spinnerData);
		UserEventEntity event = prepareUserEventForRewarding(appUser, generated);
		rewardUser(event);
		indexSpinnerEvent(event);
		return generated;

	}



	private void indexSpinnerEvent(UserEventEntity event) {
		try{
		RealmEntity realmEntity = daoRealm.findById(event.getRealmId());
		if (realmEntity != null) {
			logger.info("Indexing spinner click");
			Application.getElasticSearchLogger().indexUserClick(event.getRealmId(), event.getPhoneNumber(),
					event.getEmail(), event.getDeviceType(), event.getOfferId(), event.getRewardResponseStatus(),
					event.getOfferTitle(), event.getAdProviderCodeName(), event.getRewardTypeName(),
					event.getOfferPayout(), event.getRewardValue(), event.getRewardIsoCurrencyCode(),
					event.getProfilSplitFraction(), realmEntity.getName(), null, UserEventType.click.toString(),
					event.getInternalTransactionId(), event.getCarrierName(), event.getUserEventCategory(), null,
					null, null, event.getCountryCode(), false, event.getApplicationName(), event.getAdvertisingId(),
					event.getIdfa(), realmEntity.isTestMode(), event.getCustomRewardValue(),
					event.getCustomRewardCurrencyCode());
		}
		}
		catch (Exception exception){
			exception.printStackTrace();
		}

		
	}

	private List<SpinnerRewardEntity> getSpinnerRewardListForRealGenerate(AppUserEntity appUser) {
		try {
			if (appUser == null) {
				log(LogStatus.ERROR, "Cant generate for null appuser");
				return null;
			}

			log(LogStatus.OK, "Generating reward for " + appUser.getId());
			SpinnerDataEntity spinnerData = getSpinnerDataForUser(appUser);
			if (spinnerData == null) {
				log(LogStatus.ERROR, "Spinner data is empty for user:" + appUser.getId());
				return null;
			}

			if (spinnerData.getAvailableUses() == 0) {
				log(LogStatus.ERROR, "No available uses for user :" + appUser.getId());
				return null;
			}

			RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
			if (rewardType == null) {
				log(LogStatus.ERROR, "Reward type is null for user :" + appUser.getId());
				return null;
			}

			List<SpinnerRewardEntity> spinnerRewardList = getSpinnerRewardListForRewardType(rewardType);
			if (spinnerRewardList == null || spinnerRewardList.size() == 0) {
				log(LogStatus.ERROR, "Spinner Reward list is null or empty for reward type: " + rewardType.getName()
						+ " for user :" + appUser.getId());
				return null;
			}

			return spinnerRewardList;

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}

		return null;
	}

	private SpinnerDataEntity getSpinnerDataForUser(AppUserEntity appUser) {
		try {
			if (appUser == null) {
				log(LogStatus.ERROR, "Cant generate spinner data for user because argument is null.");
				return null;
			}

			log(LogStatus.OK, "Selecting spinner data for user:" + appUser.getId());

			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());
			if (spinnerData == null) {
				spinnerData = this.insertSpinnerData(appUser);
			}

			return spinnerData;

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}

		return null;

	}

	private List<SpinnerRewardEntity> getSpinnerRewardListForRewardType(RewardTypeEntity rewardType) {
		try {
			if (rewardType == null) {
				log(LogStatus.ERROR, "Cant generate spinner reward list argument is null.");
				return null;
			}

			log(LogStatus.OK, "Selecting spinner reward list for reward type:" + rewardType.getId());
			return daoSpinnerReward.findByRewardTypeId(rewardType.getId());

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}
		return null;

	}

	private void updateSpinnerDataCounters(SpinnerDataEntity spinnerData) {

		if (spinnerData == null) {
			log(LogStatus.ERROR, "Cant update spinnerData, because argument is null.");
			return;
		}

		log(LogStatus.OK, "Updating spinnerData for userId:" + spinnerData.getId());
		spinnerData.setTotalUses(spinnerData.getTotalUses() + 1);
		spinnerData.setAvailableUses(spinnerData.getAvailableUses() - 1);
		daoSpinnerData.createOrUpdate(spinnerData);
	}

	public SpinnerRewardEntity generateRewardForTest(RewardTypeEntity rewardType) {
		try {
			if (rewardType == null) {
				log(LogStatus.ERROR, "Reward type is null");
				return null;
			}
			List<SpinnerRewardEntity> spinnerRewardList = getSpinnerRewardListForTest(rewardType);

			SpinnerRewardEntity generated = generator.generateReward(spinnerRewardList);
			log(LogStatus.OK, "Test generated " + generated + " for reward type: " + rewardType.getName());

			return generated;

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}

		return null;
	}

	private List<SpinnerRewardEntity> getSpinnerRewardListForTest(RewardTypeEntity rewardType) throws Exception {
		if (rewardType == null) {
			log(LogStatus.ERROR, "Cant get spinner reward list for test, because argument is null");
			return null;
		}

		List<SpinnerRewardEntity> spinnerRewardList = getSpinnerRewardListForRewardType(rewardType);
		if (spinnerRewardList == null || spinnerRewardList.size() == 0) {
			log(LogStatus.ERROR,
					"Spinner reward list is null or empty for " + rewardType.getName() + ". Adding fail reward.");
			if (spinnerRewardList == null)
				spinnerRewardList = new ArrayList<SpinnerRewardEntity>();

			spinnerRewardList.add(generator.produceFailedSpinnerReward(new BigDecimal(0.0)));
		}

		return spinnerRewardList;
	}

	private String getIsoCurrencyCode(AppUserEntity appUser) {
		if (appUser.getRewardTypeName().equals("AirRewardz-India"))
			return OfferCurrency.INR.toString();
		if (appUser.getRewardTypeName().equals("AirRewardz-Kenya"))
			OfferCurrency.KSH.toString();
		if (appUser.getRewardTypeName().equals("AirRewardz-SouthAfrica"))
			OfferCurrency.ZAR.toString();

		return OfferCurrency.INR.toString();
	}

	private void rewardUser(final UserEventEntity event) {
		try {
			if (event != null) {
				
				log(LogStatus.OK, "Giving user:" + event.getUserId() + " reward name: " + event.getRewardName()
				+ " reward value: " + event.getRewardValue());

				final RealmEntity realm = daoRealm.findById(event.getRealmId());
				
				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						rewardManager.issueReward(realm, event, null, false);
					}

				}, 10000);

			}

		} catch (Exception exc) {
			exc.printStackTrace();
			log(LogStatus.ERROR,
					" Error occured in rewardUserWithMoneyToWallet:" + Arrays.toString(exc.getStackTrace()));
		}

	}

	private UserEventEntity prepareUserEventForRewarding(AppUserEntity appUser, SpinnerRewardEntity spinnerReward) {
		if (appUser == null || spinnerReward == null) {
			log(LogStatus.OK, "Cant prepare user event for rewarding, one from arguments is null");
			return null;
		}

		log(LogStatus.OK, "Preparing user event for rewarding for user:" + appUser.getId());
		UserEventEntity event = new UserEventEntity();
		event.setUserEventCategory(UserEventCategory.SPINNER.toString());
		event.setOfferId(spinnerReward.getRewardType());
		event.setInstant(false);
		event.setEmail(appUser.getEmail());
		event.setPhoneNumberExt(appUser.getPhoneNumberExtension());
		event.setPhoneNumber(appUser.getPhoneNumber());
		event.setApplicationName(appUser.getApplicationName());
		event.setRewardName(spinnerReward.getRewardName());
		event.setRewardIsoCurrencyCode(getIsoCurrencyCode(appUser));
		event.setUserId(appUser.getId());
		event.setRewardValue(spinnerReward.getRewardValue().doubleValue());
		event.setInternalTransactionId(this.generateInternalTransactionId(appUser));
		event.setOfferPayoutInTargetCurrencyIsoCurrencyCode(getIsoCurrencyCode(appUser));
		event.setOfferTitle(spinnerReward.getRewardName());
		event.setOfferSourceId(String.valueOf(spinnerReward.getId()));
		event.setRewardDate(new Timestamp(new Date().getTime()));
		event.setRewardTypeName(appUser.getRewardTypeName());
		event.setRealmId(appUser.getRealmId());
		event.setClickDate(new Timestamp(new Date().getTime()));
		event.setRewardDate(new Timestamp(new Date().getTime()));
		event.setConversionDate(new Timestamp(new Date().getTime()));
		event.setApproved(true);
		event.setDeviceType(appUser.getDeviceType());
		event.setAndroidDeviceToken(appUser.getAndroidDeviceToken());
		if (spinnerReward.getRewardType().equals(SpinnerRewardType.MONEY.toString())) {
			event.setRewardValue(spinnerReward.getRewardValue().doubleValue());
			event.setProfitValue(-event.getRewardValue());
		} else {
			if (spinnerReward.getRewardType().equals(SpinnerRewardType.SPIN_AGAIN.toString())) {
				event.setCustomRewardValue(spinnerReward.getRewardValue().doubleValue());
			}
			event.setCustomRewardCurrencyCode(spinnerReward.getRewardType());
		}
		event.setQueueStatus("SUCCESS");

		event = daoEvent.createOrUpdate(event, 0);
		log(LogStatus.OK,
				"Created event with internalT:" + event.getInternalTransactionId() + " event: " + event.toString());
		return event;
	}

	private String generateInternalTransactionId(AppUserEntity user) {
		String internalTransactionId = DigestUtils
				.sha1Hex(user.getId() + Math.random() * 100000 + System.currentTimeMillis() + user.getPhoneNumber()
						+ user.getPhoneNumberExtension() + user.getEmail());
		return internalTransactionId;
	}

	/*
	 * This method will be called when there is no row in database for user
	 */
	public SpinnerDataEntity insertSpinnerData(AppUserEntity appUser) {
		if (appUser == null) {
			log(LogStatus.ERROR, "Cant insert spinner data for user, because argument is null");
		}

		log(LogStatus.OK, "Inserting spinner data for user:" + appUser.getId());
		SpinnerDataEntity spinnerData = new SpinnerDataEntity();
		spinnerData.setUserId(appUser.getId());
		spinnerData.setAvailableUses(spinnerData.getAvailableUses() + 1);
		spinnerData.setLastDailyBonus(new Timestamp(new Date().getDate()));
		return daoSpinnerData.createOrUpdate(spinnerData);
	}

	private void log(LogStatus logStatus, String tag, String message) {
		if (logStatus == null || message == null) {
			return;
		}

		logger.info(tag + " " + logStatus + " " + message);
		Application.getElasticSearchLogger().indexLog(tag, -1, logStatus, tag + " " + message);
	}

	private void log(LogStatus logStatus, String message) {
		if (logStatus == null || message == null) {
			return;
		}

		logger.info(Application.SPINNER_MANAGER_ACTIVITY + " " + logStatus + " " + message);
		Application.getElasticSearchLogger().indexLog(Application.SPINNER_MANAGER_ACTIVITY, -1, logStatus,
				Application.SPINNER_MANAGER_ACTIVITY + message);
	}

	public List<RewardInterval> getPreparedIntervalsForSpinnerRewards(List<SpinnerRewardEntity> spinnerRewardList) {
		return generator.prepareSpinnerRewardIntervals(spinnerRewardList);
	}

	public SpinnerRewardEntity produceFailedSpinnerReward(double probability) {
		return generator.produceFailedSpinnerReward(new BigDecimal(probability));

	}

	public SpinnerRewardEntity produceFailedSpinnerReward(BigDecimal probability) {
		return generator.produceFailedSpinnerReward(probability);

	}

	public String getNotificationMessageForSpinnerRewardWithId(String id) {
		String notificationMessage = "Spinner reward failed. Please try again.";

		try {
			log(LogStatus.OK, "Getting notification message for spinner reward with id:" + id);
			if (id != null) {
				int spinnerRewardID = Integer.valueOf(id);
				SpinnerRewardEntity spinnerReward = daoSpinnerReward.findById(spinnerRewardID);
				if (spinnerReward != null)
					notificationMessage = spinnerReward.getNotificationMessage();
			}

		} catch (Exception exc) {
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			exc.printStackTrace();
		}

		log(LogStatus.OK,
				"Returning notification message for spinner reward with id:" + id + " message:" + notificationMessage);

		return notificationMessage;

	}

	public List<UserEventEntity> getTopRewards(int results, int realmId, String rewardTypeName) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -7);
			log(LogStatus.OK, "Loading top rewards from" + new Timestamp(calendar.getTimeInMillis()) + " to "
					+ new Timestamp(new Date().getTime()));
			List<UserEventEntity> eventsList = daoEvent.getSpinnerRewardInInterval(
					new Timestamp(calendar.getTimeInMillis()), new Timestamp(new Date().getTime()), realmId, results,
					rewardTypeName);
			if (eventsList != null) {
				log(LogStatus.OK, "Received top rewards list with " + eventsList.size() + "elements");
			} else {
				log(LogStatus.ERROR, "Received top rewards list returned null");
			}
			return eventsList;
		} catch (Exception exc) {
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
			return null;
		}

	}

	private void issueSpinnerDailyBonus(AppUserEntity appUser) {
		try {
			log(LogStatus.OK, "Issuing daily spinner bonus for user: " + appUser.getId());
			SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());
			RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
			int amountToAdd = 1;
			if (rewardType != null) {
				amountToAdd = (int) rewardType.getSpinnerDailyRewardUseValue();
			}
			spinnerData.setAvailableUses(spinnerData.getAvailableUses() + amountToAdd);

			Timestamp timestamp = new Timestamp(new Date().getTime());
			spinnerData.setLastDailyBonus(timestamp);
			log(LogStatus.OK, "Setting last daily bonus time " + timestamp + " for :" + appUser.getId());
			daoSpinnerData.createOrUpdate(spinnerData);
			notificationManager.sendSpinnerDailyBonusNotification(appUser);

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void checkDailyBonus(String version, AppUserEntity appUser) {
		try {
			if (appUser != null) {

				RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
				if (rewardType != null && rewardType.isSpinnerDailyRewardEnabled()) {

					log(LogStatus.OK, "Checking daily spinner bonus for user: " + appUser.getId());
					SpinnerDataEntity spinnerData = daoSpinnerData.findByUserId(appUser.getId());

					if (spinnerData == null) {
						spinnerData = this.insertSpinnerData(appUser);
					}

					Timestamp lastBonusTime = spinnerData.getLastDailyBonus();
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					Timestamp todayMinusOneDay = new Timestamp(calendar.getTimeInMillis());
					log(LogStatus.OK, "Checking daily spinner bonus for user: " + appUser.getId() + " lastBonusTime:"
							+ lastBonusTime + " todayMinusOneDay: " + todayMinusOneDay + " version: " + version);
					if (todayMinusOneDay.after(lastBonusTime) && version != null && version.contains("3.0")) {
						issueSpinnerDailyBonus(appUser);
					}
				}
				log(LogStatus.OK,
						"Could not issue daily spinner bonus because reward type is nulll or reward type name spinner daily reward is disabled.");

			}
		} catch (Exception exc) {
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}
	}

	public String getNotificationMessageForDailyBonus(AppUserEntity appUser) {
		String message = "You have received your daily spinner bonus!";
		try {
			if (appUser != null) {
				RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
				if (rewardType != null) {
					if (rewardType.getSpinnerDailyRewardNotificationMessage() != null
							&& rewardType.getSpinnerDailyRewardNotificationMessage().length() > 0) {
						message = rewardType.getSpinnerDailyRewardNotificationMessage();
					}
				}
			}
		} catch (Exception exc) {
			log(LogStatus.ERROR, Arrays.toString(exc.getStackTrace()));
		}

		return message;

	}

	public List<UserEventEntity> selectSpinnerRewardsInDateRange(Timestamp startDate, Timestamp endDate) {
		List<UserEventEntity> selectedEventsList = null;
		selectedEventsList = daoEvent.findSpinnerEventsInRange(startDate, endDate);
		return selectedEventsList;
	}

	public List<UserEventEntity> selectSpinnerRewardsInDateRangeAndForRewardType(Date startDate, Date endDate,
			String rewardType) {
		List<UserEventEntity> selectedEventsList = null;
		selectedEventsList = daoEvent.findSpinnerEventsInRangeAndForRewardType(startDate, endDate, rewardType);
		return selectedEventsList;
	}

	public SpinnerRewardsReport generateReportInDateRange(Date startDate, Date endDate, String rewardTypeName) {
		logger.info("Generate report in date range: " +startDate + " "+endDate + " " + rewardTypeName);
		SpinnerRewardsReport report = new SpinnerRewardsReport();
		try{
		List<UserEventEntity> selectedSpinnerRewardsList = selectSpinnerRewardsInDateRangeAndForRewardType(startDate,
				endDate, rewardTypeName);

		RewardTypeEntity rewardType = getRewardTypeWithName(rewardTypeName);
		report.setRewardType(rewardTypeName);
		report.setStartDate(startDate);
		report.setEndDate(endDate);
		report.setLoss(0);
		List<SpinnerRewardEntity> spinnerRewardsTypes = this.getSpinnerRewardListForRewardType(rewardType);
		HashMap<SpinnerRewardEntity, Integer> spinnerRewardsMap = new HashMap<SpinnerRewardEntity, Integer>();
		HashMap<SpinnerRewardEntity, ArrayList<Integer>> spinnerRewardsUserMap = new HashMap<SpinnerRewardEntity, ArrayList<Integer>>();
		report.setSpinRewardsMap(spinnerRewardsMap);
		report.setSpinRewardsUserMap(spinnerRewardsUserMap);
		if (selectedSpinnerRewardsList != null && selectedSpinnerRewardsList.size() > 0) {
			report.setTotalSpins(selectedSpinnerRewardsList.size());

			for (UserEventEntity event : selectedSpinnerRewardsList) {
				if (!isEventInSpinnerRewardsMap(report.getSpinRewardsMap(), event)) {
					SpinnerRewardEntity spinnerReward = prepareSpinnerRewardForEvent(event);
					report.getSpinRewardsMap().put(spinnerReward, 0);
					report.getSpinRewardsUserMap().put(spinnerReward, new ArrayList<Integer>());
				}

				if (isEventRewardedWithMoney(event)) {
					report.setLoss(report.getLoss() - event.getRewardValue());

				}
				for (SpinnerRewardEntity spinnerRewardTypeM : report.getSpinRewardsMap().keySet()) {
					if (spinnerRewardTypeM.getId() == Integer.valueOf(event.getOfferSourceId())) {
						spinnerRewardsMap.put(spinnerRewardTypeM,
								(int) (spinnerRewardsMap.get(spinnerRewardTypeM)) + 1);
						ArrayList<Integer> usersList = spinnerRewardsUserMap.get(spinnerRewardTypeM);
						if (!isUserInList(usersList, event.getUserId())) {
							usersList.add(event.getUserId());
						}
						break;
					}
				}
				List<Integer> userUniqueList = prepareUniqueUserList(report);

				report.setUserCount(userUniqueList.size());

			}
		}
		report.setSpinRewardsMap(spinnerRewardsMap);
		}
		catch (Exception exception){
			exception.printStackTrace();
		}
		return report;
	}

	private List<Integer> prepareUniqueUserList(SpinnerRewardsReport report) {
		List<Integer> userUniqueList = new ArrayList<Integer>();
		for (SpinnerRewardEntity spinnerReward : report.getSpinRewardsUserMap().keySet()) {
			for (int userId : report.getSpinRewardsUserMap().get(spinnerReward)) {
				if (!isUserInList(userUniqueList, userId)) {
					userUniqueList.add(userId);
				}
			}
		}
		return userUniqueList;
	}

	private SpinnerRewardEntity prepareSpinnerRewardForEvent(UserEventEntity event) {
		SpinnerRewardEntity spinnerReward = new SpinnerRewardEntity();
		spinnerReward.setId(Integer.parseInt(event.getOfferSourceId()));
		spinnerReward.setRewardName(event.getOfferTitle());
		spinnerReward.setRewardType(event.getOfferId());
		spinnerReward.setRewardValue(BigDecimal.valueOf(event.getRewardValue()));
		return spinnerReward;
	}

	private boolean isUserInList(List<Integer> usersList, int userId) {
		for (int userIdInList : usersList) {
			if (userIdInList == userId) {
				return true;
			}
		}
		return false;
	}

	private boolean isEventInSpinnerRewardsMap(Map<SpinnerRewardEntity, Integer> map, UserEventEntity event) {
		for (SpinnerRewardEntity spinnerReward : map.keySet()) {
			if (spinnerReward.getId() == Integer.valueOf(event.getOfferSourceId())) {
				return true;
			}
		}
		return false;
	}

	private RewardTypeEntity getRewardTypeWithName(String rewardTypeName) {
		try {
			return daoRewardType.findByName(rewardTypeName);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private boolean isEventRewardedWithMoney(UserEventEntity event) {
		if (event.getOfferId() != null) {
			if (event.getOfferId().toLowerCase().contains("money")) {
				return true;
			}
		}
		return false;
	}

}
