package is.web.services.conversion;

import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

public class ConversionData {
	private String internalTransactionId;
	private String offerProviderTransactionId;
	private UserEventEntity userEvent;
	private String ipAddress;
	private RealmEntity realm;
	public String getInternalTransactionId() {
		return internalTransactionId;
	}
	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}
	public String getOfferProviderTransactionId() {
		return offerProviderTransactionId;
	}
	public void setOfferProviderTransactionId(String offerProviderTransactionId) {
		this.offerProviderTransactionId = offerProviderTransactionId;
	}
	public UserEventEntity getUserEvent() {
		return userEvent;
	}
	public void setUserEvent(UserEventEntity userEvent) {
		this.userEvent = userEvent;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public RealmEntity getRealm() {
		return realm;
	}
	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}
	

	
	
}
