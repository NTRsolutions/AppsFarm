package is.ejb.bl.reward;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.bl.system.logging.LogStatus;
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
	private DAOAppUser daoAppUser;
	@Inject
	private DAOApplicationReward daoApplicationReward;
	@Inject
	private DAOWalletData daoWalletData;

	public RewardTicketEntity createRewardTicket(HashMap<String, Object> parameters) {
		RewardTicketEntity rewardTicket = new RewardTicketEntity();
		Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
				Application.REWARD_TICKET_CREATE_ACTIVITY + "Creating reward ticket for data: " + parameters);
		try {
			String username = (String) parameters.get("username");
			int rewardId = (Integer) parameters.get("rewardId");
			AppUserEntity appUser = daoAppUser.findByUsername(username);
			ApplicationRewardEntity reward = daoApplicationReward.findById(rewardId);

			rewardTicket.setUserId(appUser.getId());
			rewardTicket.setEmail(appUser.getEmail());
			rewardTicket.setRewardName(reward.getRewardName());
			rewardTicket.setRewardId(reward.getId());
			rewardTicket.setCreditPoints(reward.getRewardValue());
			rewardTicket.setRequestDate(new Timestamp(System.currentTimeMillis()));
			rewardTicket.setStatus(RewardTicketStatus.AWAITING_PROCESSING);
			rewardTicket.setRewardType(reward.getRewardType());
			rewardTicket.generateHash();

			daoRewardTickets.createOrUpdate(rewardTicket);
			if (!substractRewardAmountFromWallet(appUser, rewardTicket)) {
				markRewardTicketAsFailed(rewardTicket, "Can't substract reward amount from user wallet.");
				return null;
			}

			Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
					"Request Ticket - " + rewardTicket.getContent(), rewardTicket);
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
			rewardTicket.setComment(comment);
			rewardTicket.setCloseDate(new Timestamp(new Date().getTime()));
			rewardTicket.setStatus(RewardTicketStatus.PROCESSED_FAILED);
			daoRewardTickets.createOrUpdate(rewardTicket);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void addRewardAmountBackToWallet(AppUserEntity appUser, RewardTicketEntity rewardTicket) {

	}

	private boolean substractRewardAmountFromWallet(AppUserEntity appUser, RewardTicketEntity rewardTicket) {
		try {
			WalletDataEntity walletData = daoWalletData.findByUserId(appUser.getId());
			double balance = walletData.getBalance();
			double balanceAfterSubstract = balance - rewardTicket.getCreditPoints();
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Substract reward amount from wallet for userId : "
							+ appUser.getId() + " balance before: " + balance + " balance after: "
							+ balanceAfterSubstract);
			if (balanceAfterSubstract >= 0) {
				walletData.setBalance(balanceAfterSubstract);
				walletData.setTransactionCounter(walletData.getTransactionCounter()+1);
				daoWalletData.createOrUpdate(walletData);
				Application.getElasticSearchLogger()
						.indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
								Application.REWARD_TICKET_CREATE_ACTIVITY
										+ "Substract reward amount from wallet for userId" + appUser.getId()
										+ " was success. Balance after: " + balanceAfterSubstract);
				return true;
			} else {
				Application.getElasticSearchLogger()
						.indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.ERROR,
								Application.REWARD_TICKET_CREATE_ACTIVITY
										+ "Substract reward amount from wallet for userId" + appUser.getId()
										+ " was failed. Balance after: " + balanceAfterSubstract);
				return false;
			}
		} catch (Exception exception) {
			Application.getElasticSearchLogger().indexLog(Application.REWARD_TICKET_CREATE_ACTIVITY, -1, LogStatus.OK,
					Application.REWARD_TICKET_CREATE_ACTIVITY + "Substract reward amount from wallet for userId : "
							+ appUser.getId() + "was failed. Error:" + exception.toString());
			exception.printStackTrace();
			return false;
		}
	}

}
