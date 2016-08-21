package is.ejb.bl.offerFilter;

import java.util.ArrayList;

public class BlockedOffers {
	private ArrayList<BlockedOffer> listBlockedOffers = new ArrayList<BlockedOffer>();

	public ArrayList<BlockedOffer> getListBlockedOffers() {
		return listBlockedOffers;
	}
	public void setListBlockedOffers(ArrayList<BlockedOffer> listBlockedOffers) {
		this.listBlockedOffers = listBlockedOffers;
	}
	
}
