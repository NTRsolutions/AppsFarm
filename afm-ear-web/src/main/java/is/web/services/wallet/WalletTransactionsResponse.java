package is.web.services.wallet;

import java.util.List;

import is.ejb.dl.entities.WalletTransactionEntity;
import is.web.services.APIResponse;

public class WalletTransactionsResponse extends APIResponse {
	private List<WalletTransactionEntity> walletTransactions;

	public List<WalletTransactionEntity> getWalletTransactions() {
		return walletTransactions;
	}

	public void setWalletTransactions(List<WalletTransactionEntity> walletTransactions) {
		this.walletTransactions = walletTransactions;
	}
	
	
}
