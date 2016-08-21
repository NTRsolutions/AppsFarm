package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "WalletPayoutCarrier")
public class WalletPayoutCarrierEntity {
	@Id
	@GeneratedValue
	private int id;

	private int rewardTypeId;
	
	private String name;
	
	private int realmId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRewardTypeId() {
		return rewardTypeId;
	}

	public void setRewardTypeId(int rewardTypeId) {
		this.rewardTypeId = rewardTypeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	@Override
	public String toString() {
		return "WalletPayoutCarrierEntity [id=" + id + ", rewardTypeId=" + rewardTypeId + ", name=" + name
				+ ", realmId=" + realmId + "]";
	}

	

	

}
