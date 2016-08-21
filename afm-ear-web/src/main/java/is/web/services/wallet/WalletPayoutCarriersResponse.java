package is.web.services.wallet;

import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.web.services.Response;

import java.util.List;

public class WalletPayoutCarriersResponse extends Response{
	private List<WalletPayoutCarrierEntity> walletPayoutCarrierList;

	public List<WalletPayoutCarrierEntity> getWalletPayoutCarrierList() {
		return walletPayoutCarrierList;
	}

	public void setWalletPayoutCarrierList(List<WalletPayoutCarrierEntity> walletPayoutCarrierList) {
		this.walletPayoutCarrierList = walletPayoutCarrierList;
	}

	@Override
	public String toString() {
		return "WalletPayoutCarriersResponse [walletPayoutCarrierList="
				+ walletPayoutCarrierList + "]";
	}
	
	
	
	
}
