package is.web.services.wallet;

import is.ejb.dl.entities.WalletDataEntity;
import is.web.services.APIResponse;

public class WalletDataResponse extends APIResponse {
	private WalletDataEntity walletData;

	public WalletDataEntity getWalletData() {
		return walletData;
	}

	public void setWalletData(WalletDataEntity walletData) {
		this.walletData = walletData;
	}
	
}
