package is.ejb.bl.gamification;

import is.ejb.bl.business.UserEventCategory;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class GamificationManager {

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private Logger logger;

	public void countEvent(UserEventEntity event) {
		logger.info("**********Gamification manager************");
		logger.info("Count event for event:");
		logger.info(event.toString());
		if (event != null && event.getUserEventCategory() != null) {
			logger.info("Checking event category");
			if (event.getUserEventCategory().equals(UserEventCategory.INSTALL.toString())) {
				updateUserInstallCounter(event);
			}

			if (event.getUserEventCategory().equals(UserEventCategory.VIDEO.toString())) {
				updateUserVideoCounter(event);

			}

		}

	}

	private void updateUserInstallCounter(UserEventEntity event) {
		logger.info("Updating install counter");
		try {
			int userId = event.getUserId();
			AppUserEntity appUser = daoAppUser.findById(userId);
			if (appUser != null) {
				int currentValue = appUser.getInstallConversionCounterVG() + 1;
				appUser.setInstallConversionCounterVG(currentValue);
				daoAppUser.create(appUser);
				logger.info("Updating install counter status success");
			}

			if (checkIfReset(appUser)) {
				resetCounters(appUser);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	private void updateUserVideoCounter(UserEventEntity event) {
		logger.info("Updating video counter");
		try {
			int userId = event.getUserId();
			AppUserEntity appUser = daoAppUser.findById(userId);
			if (appUser != null) {
				int currentValue = appUser.getVideoConversionCounterVG() + 1;
				appUser.setVideoConversionCounterVG(currentValue);
				daoAppUser.createOrUpdate(appUser);
				logger.info("Updating video counter status success");
			}

			if (checkIfReset(appUser)) {
				resetCounters(appUser);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void resetCounters(AppUserEntity appUser) {
		try {
			logger.info("Reseting counters for user:" + appUser.getId());
			if (appUser != null) {
				appUser.setVideoConversionCounterVG(0);
				appUser.setInstallConversionCounterVG(0);
				daoAppUser.createOrUpdate(appUser);
				logger.info("Reseting counters success");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	private boolean checkIfReset(AppUserEntity appUser) {
		try {
			logger.info("Checking if reset for user with id: " + appUser.getId());
			int realmId = appUser.getRealmId();
			String name = appUser.getRewardTypeName();

			RewardTypeEntity rewardType = daoRewardType.findByRealmIdAndName(realmId, name);
			if (rewardType != null) {
				int rewardTypeVideoCounterVG = rewardType.getVideoCounterVG();
				int rewardTypeInstallCounterVG = rewardType.getInstallCounterVG();

				int userVideoCounterVG = appUser.getVideoConversionCounterVG();
				int userInstallCounterVG = appUser.getInstallConversionCounterVG();
				logger.info("User values:");
				logger.info("User video counter:" + userVideoCounterVG);
				logger.info("User install counter:" + userInstallCounterVG);
				logger.info("Reward type values:");
				logger.info("Reward type video counter:" + rewardTypeVideoCounterVG);
				logger.info("Reward type install counter:" + rewardTypeInstallCounterVG);

				if (userVideoCounterVG >= rewardTypeVideoCounterVG) {
					if (userInstallCounterVG != 0) {
						return true;
					} else
						return false;
				} else
					return false;
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return false;

	}

	public boolean checkIfUserCanWatchVideo(AppUserEntity appUser) {
		logger.info("Checking user limits");

		try {
			if (appUser != null) {
				int realmId = appUser.getRealmId();
				String name = appUser.getRewardTypeName();

				RewardTypeEntity rewardType = daoRewardType.findByRealmIdAndName(realmId, name);
				if (rewardType != null) {
					int rewardTypeVideoCounterVG = rewardType.getVideoCounterVG();
					int rewardTypeInstallCounterVG = rewardType.getInstallCounterVG();

					int userVideoCounterVG = appUser.getVideoConversionCounterVG();
					int userInstallCounterVG = appUser.getInstallConversionCounterVG();
					logger.info("User values:");
					logger.info("User video counter:" + userVideoCounterVG);
					logger.info("User install counter:" + userInstallCounterVG);
					logger.info("Reward type values:");
					logger.info("Reward type video counter:" + rewardTypeVideoCounterVG);
					logger.info("Reward type install counter:" + rewardTypeInstallCounterVG);

					boolean result = false;
					if (userInstallCounterVG >= rewardTypeInstallCounterVG) {
						if (userVideoCounterVG < rewardTypeVideoCounterVG) {
							result = true;
						} else {

							result = false;
						}
					} else {
						result = false;
					}

					if (result == false)
						if (checkIfReset(appUser)) {
							resetCounters(appUser);
						}

					return result;

				}
			}

		}

		catch (Exception exc) {
			exc.printStackTrace();
		}

		return false;

	}

}
