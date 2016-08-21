package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "WalletPayoutOfferTransaction")
public class WalletPayoutOfferTransactionEntity {

	@Id
	@GeneratedValue
	private int id;

	private long ticketId;

	private int userId;

	private String offerName;

	private double offerValue;

	private String status;
	
	private int networkId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	

	public long getTicketId() {
		return ticketId;
	}

	public void setTicketId(long ticketId) {
		this.ticketId = ticketId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getOfferName() {
		return offerName;
	}

	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}

	public double getOfferValue() {
		return offerValue;
	}

	public void setOfferValue(double offerValue) {
		this.offerValue = offerValue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}



	

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	@Override
	public String toString() {
		return "WalletPayoutOfferTransactionEntity [id=" + id + ", ticketId="
				+ ticketId + ", userId=" + userId + ", offerName=" + offerName
				+ ", offerValue=" + offerValue + ", status=" + status
				+ ", networkId=" + networkId + "]";
	}

	
	
}
