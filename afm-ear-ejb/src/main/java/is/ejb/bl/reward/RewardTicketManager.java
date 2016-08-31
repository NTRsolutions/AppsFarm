package is.ejb.bl.reward;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.bl.email.EmailHolder;
import is.ejb.bl.email.EmailManager;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.wallet.WalletManager;
import is.ejb.bl.wallet.WalletTransactionType;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAORewardTickets;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.RewardTicketEntity;
import is.ejb.dl.entities.WalletDataEntity;

@Stateless
public class RewardTicketManager {
	@Inject
	private DAORewardTickets daoRewardTickets;
	@Inject
	private WalletManager walletManager;
	@Inject
	private DAOAppUser daoAppUser;
	@Inject
	private DAOApplicationReward daoApplicationReward;
	@Inject
	private EmailManager emailManager;
	@Inject
	private NotificationManager notificationManager;
	@Inject
	private Logger logger;

	public RewardTicketEntity createRewardTicket(HashMap<String, Object> parameters) {
		RewardTicketEntity rewardTicket = new RewardTicketEntity();
		Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
				Application.REWARD_TICKET_CREATE_ACTIVITY + "Creating reward ticket for data: " + parameters);
		try {
			String username = (String) parameters.get("username");
			int rewardId = (Integer) parameters.get("rewardId");
			AppUserEntity appUser = daoAppUser.findByUsername(username);
			ApplicationRewardEntity reward = daoApplicationReward.findById(rewardId);
			rewardTicket.setRealmId(appUser.getRealmId());
			rewardTicket.setUserId(appUser.getId());
			rewardTicket.setEmail(appUser.getEmail());
			rewardTicket.setRewardName(reward.getRewardName());
			rewardTicket.setRewardId(reward.getId());
			rewardTicket.setCreditPoints(reward.getRewardValue());
			rewardTicket.setRequestDate(new Timestamp(System.currentTimeMillis()));
			rewardTicket.setStatus(RewardTicketStatus.AWAITING_PROCESSING);
			rewardTicket.setRewardType(reward.getRewardType());
			rewardTicket.setRewardCategory(reward.getRewardCategory());
			rewardTicket.generateHash();

			daoRewardTickets.createOrUpdate(rewardTicket);
			if (!substractRewardAmountFromWallet(appUser, rewardTicket)) {
				markRewardTicketAsFailed(rewardTicket, "Can't substract reward amount from user wallet.");
				return null;
			}

			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Reward Ticket Created for user id: " + appUser.getId() + " email: " + appUser.getEmail()
							+ " reward name: " + rewardTicket.getRewardName() + " reward id: " + reward.getId()
							+ " credit points: " + reward.getRewardValue() + " request date: "
							+ rewardTicket.getRequestDate() + " reward type: " + rewardTicket.getRewardType()
							+ " reward category: " + rewardTicket.getRewardCategory() + " hash: "
							+ rewardTicket.getHash(),
					rewardTicket);
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Reward ticket created for data: " + parameters);

			return rewardTicket;
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1,
					LogStatus.ERROR, Application.REWARD_TICKET_CREATE_ACTIVITY + "Creating reward ticket for data: "
							+ parameters + " failed. Error: " + exc.toString());
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.ERROR,
					"Request Ticket exception: " + exc.getMessage() + " - " + rewardTicket.getContent(), rewardTicket);
			return null;

		}
	}

	private void markRewardTicketAsFailed(RewardTicketEntity rewardTicket, String comment) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Marking reward type with id: " + rewardTicket.getId()
							+ " as failed. Comment: " + comment);
			Application.getElasticSearchLogger()
					.indexRewardTicket(
							LogStatus.OK, "Marking reward type with id: " + rewardTicket.getId()
									+ " as failed. Comment: " + comment + " hash: " + rewardTicket.getHash(),
							rewardTicket);
			rewardTicket.setComment(comment);
			rewardTicket.setCloseDate(new Timestamp(new Date().getTime()));
			rewardTicket.setStatus(RewardTicketStatus.PROCESSED_FAILED);
			daoRewardTickets.createOrUpdate(rewardTicket);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public boolean addRewardAmountBackToWallet(RewardTicketEntity rewardTicket) {
		AppUserEntity appUser = new AppUserEntity();
		try {
			appUser = daoAppUser.findById(rewardTicket.getUserId());
			WalletDataEntity walletData = walletManager.getWalletData(appUser);
			double balance = walletData.getBalance();
			double balanceAfterAdd = balance + rewardTicket.getCreditPoints();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Add reward amount from wallet for userId : "
							+ appUser.getId() + " balance before: " + balance + " balance after: " + balanceAfterAdd);

			Application.getElasticSearchLogger().indexRewardTicket(
					LogStatus.OK, "Add reward amount from wallet for userId : " + appUser.getId() + " balance before: "
							+ balance + " balance after: " + balanceAfterAdd + " hash: " + rewardTicket.getHash(),
					rewardTicket);

			boolean result = walletManager.createWalletAction(appUser, WalletTransactionType.ADDITION,
					rewardTicket.getCreditPoints(), "Reward credit from reward ticket with id: " + rewardTicket.getId()
							+ " hash: " + rewardTicket.getHash());

			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Add reward amount from wallet for userId"
							+ appUser.getId() + " was " + result + " . Balance after: " + balanceAfterAdd);

			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Add reward amount from wallet for userId" + appUser.getId() + " was " + result
							+ " . Balance after: " + balanceAfterAdd + " hash: " + rewardTicket.getHash(),
					rewardTicket);
			return true;

		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1,
					LogStatus.ERROR,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Add reward amount from wallet for userId : "
							+ appUser.getId() + "was failed. Error:" + exception.toString());
			Application.getElasticSearchLogger().indexRewardTicket(
					LogStatus.ERROR, "Add reward amount from wallet for userId : " + appUser.getId()
							+ "was failed. Error:" + exception.toString() + " hash: " + rewardTicket.getHash(),
					rewardTicket);
			exception.printStackTrace();
			return false;
		}
	}

	private boolean substractRewardAmountFromWallet(AppUserEntity appUser, RewardTicketEntity rewardTicket) {
		try {
			WalletDataEntity walletData = walletManager.getWalletData(appUser);
			double balance = walletData.getBalance();
			double balanceAfterSubstract = balance - rewardTicket.getCreditPoints();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Substract reward amount from wallet for userId : "
							+ appUser.getId() + " balance before: " + balance + " balance after: "
							+ balanceAfterSubstract);

			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Substract reward amount from wallet for userId : " + appUser.getId() + " balance before: "
							+ balance + " balance after: " + balanceAfterSubstract + " hash: " + rewardTicket.getHash(),
					rewardTicket);

			if (balanceAfterSubstract >= 0) {
				boolean result = walletManager.createWalletAction(appUser, WalletTransactionType.SUBTRACTION,
						rewardTicket.getCreditPoints(), "Reward credit from reward ticket with id: "
								+ rewardTicket.getId() + " hash: " + rewardTicket.getHash());
				Application.getElasticSearchLogger()
						.indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
								Application.REWARD_TICKET_CREATE_ACTIVITY
										+ "Substract reward amount from wallet for userId" + appUser.getId() + " was "
										+ result + " . Balance after: " + balanceAfterSubstract);

				Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
						"Substract reward amount from wallet for userId" + appUser.getId() + " was " + result
								+ ". Balance after: " + balanceAfterSubstract + " hash: " + rewardTicket.getHash(),
						rewardTicket);

				return true;
			} else {
				Application.getElasticSearchLogger()
						.indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.ERROR,
								Application.REWARD_TICKET_CREATE_ACTIVITY
										+ "Substract reward amount from wallet for userId" + appUser.getId()
										+ " was failed. Balance after: " + balanceAfterSubstract);

				Application.getElasticSearchLogger().indexRewardTicket(LogStatus.ERROR,
						"Substract reward amount from wallet for userId" + appUser.getId()
								+ " was failed. Balance after: " + balanceAfterSubstract + " hash: "
								+ rewardTicket.getHash(),
						rewardTicket);
				return false;
			}
		} catch (Exception exception) {
			Application.getElasticSearchLogger()
					.indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.ERROR,
							Application.REWARD_TICKET_CREATE_ACTIVITY
									+ "Substract reward amount from wallet for userId : " + appUser.getId()
									+ "was failed. Error:" + exception.toString());

			Application.getElasticSearchLogger().indexRewardTicket(
					LogStatus.ERROR, "Substract reward amount from wallet for userId : " + appUser.getId()
							+ "was failed. Error:" + exception.toString() + " hash: " + rewardTicket.getHash(),
					rewardTicket);
			exception.printStackTrace();
			return false;
		}
	}

	public void updateTicket(RewardTicketEntity rewardTicket) {
		try {
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Updating ticket:" + rewardTicket.toString(), rewardTicket);
			logger.info("Updating ticket:" + rewardTicket);

			daoRewardTickets.createOrUpdate(rewardTicket);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void sendEmail(RewardTicketEntity rewardTicket, int emailTemplateId, String rewardResult) {
		try {
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Sending email for reward ticket id : " + rewardTicket.getId() + " hash: " + rewardTicket.getHash()
							+ " using template id: " + emailTemplateId + " reward result " + rewardResult,
					rewardTicket);
			logger.info(
					"Sending email for reward ticket id : " + rewardTicket.getId() + " hash: " + rewardTicket.getHash()
							+ " using template id: " + emailTemplateId + " reward result " + rewardResult);

			EmailHolder holder = emailManager.setupEmailTemplate(emailTemplateId, rewardTicket, rewardResult);
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK, "Email holder for reward ticket id : "
					+ rewardTicket.getId() + " hash: " + rewardTicket.getHash() + "email holder: " + holder,
					rewardTicket);

			boolean result = emailManager.sendEmail(holder, rewardTicket.getRealmId());
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK, "Email sent with status: " + result
					+ "reward ticket id : " + rewardTicket.getId() + " hash: " + rewardTicket.getHash(), rewardTicket);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.ERROR,
					"Sending email for reward ticket id : " + rewardTicket.getId() + " hash: " + rewardTicket.getHash()
							+ " using template id: " + emailTemplateId + " reward result " + rewardResult
							+ " exception: " + exc.toString(),
					rewardTicket);
		}
	}

	public boolean sendNotification(RewardTicketEntity rewardTicket, String notificationMessage) {
		try {
			AppUserEntity appUser = daoAppUser.findById(rewardTicket.getUserId());
			boolean result = notificationManager.sendNotification(appUser, notificationMessage);
			Application.getElasticSearchLogger().indexRewardTicket(
					LogStatus.OK, "Sending notification: " + notificationMessage + " for reward ticket id : "
							+ rewardTicket.getId() + " hash: " + rewardTicket.getHash() + " result: " + result,
					rewardTicket);
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.ERROR,
					"Error occured during sending notification: " + notificationMessage + " for reward ticket id : "
							+ rewardTicket.getId() + " hash: " + rewardTicket.getHash() + " error: " + exc.toString(),rewardTicket);
			return false;
		}
	}

}
