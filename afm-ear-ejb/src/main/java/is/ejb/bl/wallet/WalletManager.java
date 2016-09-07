package is.ejb.bl.wallet;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.ejb.dl.entities.WalletTransactionEntity;

@Stateless
public class WalletManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAOWalletTransaction daoWalletTransaction;

	public boolean createWalletAction(AppUserEntity appUser, WalletTransactionType type, double transactionAmount,
			String message) {
		String actionData = "AppUser: " + appUser + " walletTransactionType: " + type + " transactionAmount: "
				+ transactionAmount + " message: " + message;
		Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
				Application.WALLET_TRANSACTION_ACTIVITY + " wallet action request: " + actionData);
		logger.info(" wallet action request: " + actionData);
		WalletDataEntity walletData = getWalletData(appUser);
		if (walletData == null) {
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " wallet action failed for request: " + actionData
							+ " cause couldnt select wallet data for user.");
			logger.info(
					"wallet action failed for request: " + actionData + " cause couldnt select wallet data for user.");
			return false;
		} else {
			walletData = prepareWalletDataForAction(walletData, type, transactionAmount);
			if (walletData == null) {
				Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.WALLET_TRANSACTION_ACTIVITY_ABORTED + " wallet action failed for request: "
								+ actionData
								+ " cause wallet data prepared for action returned null(subtraction may cause minus balance).");
				logger.info("wallet action failed for request: " + actionData
						+ " cause wallet data prepared for action returned null(subtraction may cause minus balance)");
				return false;
			} else {
				daoWalletData.createOrUpdate(walletData);
				WalletTransactionEntity walletTransaction = prepareWalletTransaction(appUser, type, transactionAmount,
						message);
				if (walletTransaction != null) {
					logger.info("inserting wallet transaction request");
					daoWalletTransaction.createOrUpdate(walletTransaction);
				}
				logger.info(" wallet action request: " + actionData + "sucessfully completed");
				Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
						Application.WALLET_TRANSACTION_ACTIVITY + " wallet action request: " + actionData
								+ "sucessfully completed");
				return true;
			}
		}
	}

	private WalletTransactionEntity prepareWalletTransaction(AppUserEntity appUser, WalletTransactionType type,
			double transactionAmount, String message) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY + " prepeare wallet transaction for appUser: " + appUser
							+ "wallet transaction type: " + type + " transactionAmount: " + transactionAmount
							+ " message: " + message);
			logger.info(" prepare wallet transaction for appUser: " + appUser + "wallet transaction type: " + type
					+ " transactionAmount: " + transactionAmount + " message: " + message);
			WalletTransactionEntity walletTransactionEntity = new WalletTransactionEntity();
			walletTransactionEntity.setApplicationName(appUser.getApplicationName());
			walletTransactionEntity
					.setInternalTransactionId(generateInternalTransactionId(appUser, type, transactionAmount, message));
			walletTransactionEntity.setPayoutCurrencyCode(appUser.getCountryCode());
			walletTransactionEntity.setPayoutDescription(message);
			walletTransactionEntity.setPayoutValue(transactionAmount);
			walletTransactionEntity.setRewardName(type.toString());
			walletTransactionEntity.setStatus("SUCCESS");
			walletTransactionEntity.setTimestamp(new Timestamp(new Date().getTime()));
			walletTransactionEntity.setType(type.toString());
			walletTransactionEntity.setUserId(appUser.getId());

			return walletTransactionEntity;
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.WALLET_TRANSACTION_ACTIVITY + " prepare walletTransaction error: " + exc.toString()
							+ "for appUser: " + appUser + "wallet transaction type: " + type + " transactionAmount: "
							+ transactionAmount + " message: " + message);
			exc.printStackTrace();
			return null;
		}
	}

	private String generateInternalTransactionId(AppUserEntity appUser, WalletTransactionType type,
			double transactionAmount, String message) {
		String internalTransactionId = "";

		internalTransactionId = DigestUtils.sha1Hex(
				Math.random() * 100000 + System.currentTimeMillis() + message + transactionAmount + appUser.getEmail());
		if (internalTransactionId.length() > 32) {
			internalTransactionId = internalTransactionId.substring(0, 31);
		}

		return internalTransactionId;
	}

	private WalletDataEntity prepareWalletDataForAction(WalletDataEntity walletData, WalletTransactionType type,
			double transactionAmount) {
		if (type.equals(WalletTransactionType.SUBTRACTION)) {
			double amountBeforeSubtraction = walletData.getBalance();
			double amountAfterSubtraction = amountBeforeSubtraction - transactionAmount;
			if (amountAfterSubtraction < 0) {
				return null;
			}
			walletData.setBalance(amountAfterSubtraction);
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY
							+ " Prepare wallet data for action: amountBeforeSubtraction: " + amountBeforeSubtraction
							+ " amount after subtraction: " + amountAfterSubtraction + " wallet data: " + walletData);
			logger.info(" Prepare wallet data for action: amountBeforeSubtraction: " + amountBeforeSubtraction
					+ " amount after subtraction: " + amountAfterSubtraction + " wallet data: " + walletData);
		}

		if (type.equals(WalletTransactionType.ADDITION)) {
			double amountBeforeAddition = walletData.getBalance();
			double amountAfterAddition = amountBeforeAddition + transactionAmount;
			walletData.setBalance(amountAfterAddition);
			Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
					Application.WALLET_TRANSACTION_ACTIVITY + " Prepare wallet data for action: amountBeforAddition: "
							+ amountBeforeAddition + " amount after addition: " + amountAfterAddition + " wallet data: "
							+ walletData);
			logger.info(" Prepare wallet data for action: amountBeforAddition: " + amountBeforeAddition
					+ " amount after addition: " + amountAfterAddition + " wallet data: " + walletData);
		}
		walletData.setTransactionCounter(walletData.getTransactionCounter() + 1);

		return walletData;
	}

	public WalletDataEntity getWalletData(AppUserEntity appUser) {
		WalletDataEntity walletData = null;
		try {
			if (appUser != null) {
				walletData = daoWalletData.findByUserId(appUser.getId());
				if (walletData == null) {
					logger.info("Wallet not existing...");
					insertWalletData(appUser);
					walletData = getWalletData(appUser);
				} else {
					logger.info("Wallet existing...");
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
				Application.WALLET_TRANSACTION_ACTIVITY + " get wallet data for appUser: " + appUser + " wallet data: "
						+ walletData);
		logger.info(" get wallet data for appUser: " + appUser + " wallet data: " + walletData);
		return walletData;
	}

	private WalletDataEntity insertWalletData(AppUserEntity appUser) {
		WalletDataEntity walletData = new WalletDataEntity();
		walletData.setUserId(appUser.getId());
		walletData.setIsoCurrencyCode(appUser.getCountryCode());
		daoWalletData.create(walletData);
		Application.getElasticSearchLogger().indexLog(Application.WALLET_TRANSACTION_ACTIVITY, -1, LogStatus.OK,
				Application.WALLET_TRANSACTION_ACTIVITY + " inserted new walletData for appUser: " + appUser);
		logger.info(" inserted new walletData for appUser: " + appUser);
		return walletData;
	}

}
