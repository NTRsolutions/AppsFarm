package is.ejb.bl.attendance;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.firebase.FirebaseResponse;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.notificationSystems.NotificationMessageDictionary;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.WalletDataEntity;

@Stateless
public class AttendanceManager {

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private Logger logger;

	@Inject
	private NotificationManager notificationManager;

	@Asynchronous
	public void checkAttendance(AppUserEntity givenAppUser) {
		try {
			AppUserEntity appUser = selectAppUser(givenAppUser.getId());
			if (appUser != null) {
				
				Timestamp lastBonusTime = appUser.getAttendanceLastBonusTime();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(lastBonusTime.getTime());
				calendar.add(Calendar.DAY_OF_MONTH, +1);
				Timestamp lastBonusTimePlusOneDay = new Timestamp(calendar.getTimeInMillis());
				Timestamp currentTime = new Timestamp(new Date().getTime());

				Application.getElasticSearchLogger().indexLog(Application.ATTENDANCE_ACTIVITY, -1, LogStatus.OK,
						Application.ATTENDANCE_ACTIVITY + "Checking attendance for userId: " + appUser.getId()
								+ " last bonus time: " + lastBonusTime + "last bonus time plus one day: "
								+ lastBonusTimePlusOneDay + " current time : " + currentTime);
				logger.info("Checking attendance for userId: " + appUser.getId() + " last bonus time: " + lastBonusTime
						+ "last bonus time plus one day: " + lastBonusTimePlusOneDay + " current time : "
						+ currentTime);

				if (lastBonusTime == null || currentTime.after(lastBonusTimePlusOneDay)) {
					double attendanceValue = getAttendaceValue(appUser.getRewardTypeName());
					sendUserAttendanceBonus(appUser,attendanceValue);
					notifyUser(appUser,attendanceValue);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private AppUserEntity selectAppUser(int id){
		AppUserEntity appUser = null;
		try{
			appUser = daoAppUser.findById(id);
			logger.info("Appuser selected with id: " + id);
		}catch (Exception exc){
			exc.printStackTrace();
		}
		return appUser;
	}

	private double getAttendaceValue(String rewardType) {
		try {
			RewardTypeEntity rewardTypeEntity = daoRewardType.findByName(rewardType);
			return rewardTypeEntity.getAttendanceValue();
		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
		}
	}

	public void sendUserAttendanceBonus(AppUserEntity appUser,double attendanceValue) {
		try {

			WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
			if (walletData == null) {
				walletData = new WalletDataEntity();
				walletData.setUserId(appUser.getId());
			}
			
			walletData.setBalance(walletData.getBalance() + attendanceValue);
			daoWalletData.createOrUpdate(walletData);

			Application.getElasticSearchLogger().indexLog(Application.ATTENDANCE_ACTIVITY, -1, LogStatus.OK,
					Application.ATTENDANCE_ACTIVITY + " User id: " + appUser.getId() + " received attendance bonus: "
							+ attendanceValue);
			logger.info(" User id: " + appUser.getId() + " received attendance bonus: " + attendanceValue);
			appUser.setAttendanceLastBonusTime(new Timestamp(new Date().getTime()));
			daoAppUser.createOrUpdate(appUser);

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void notifyUser(AppUserEntity appUser, double attendanceValue) {
		Application.getElasticSearchLogger().indexLog(Application.ATTENDANCE_ACTIVITY, -1, LogStatus.OK,
				Application.ATTENDANCE_ACTIVITY + " User id: " + appUser.getId()
						+ " is being notified attendance bonus: " + attendanceValue);
		String message = NotificationMessageDictionary.ATTENDANCE_BONUS;
		message = message.replaceAll("\\{reward\\}","" + attendanceValue + " points");
		FirebaseResponse result = notificationManager.sendNotification(appUser, message);
		Application.getElasticSearchLogger().indexLog(Application.ATTENDANCE_ACTIVITY, -1, LogStatus.OK,
				Application.ATTENDANCE_ACTIVITY + "Notified User id: " + appUser.getId() + " messgae:" + message + " result: " + result);
		logger.info( "Notified User id: " + appUser.getId() + " messgae:" + message + " result: " + result);

	}
}
