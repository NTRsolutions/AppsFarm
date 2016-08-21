package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "UserFriendEntity")
public class UserFriendEntity {
	@Id
	@GeneratedValue
	private int id;
	
	@NotNull
	private int userId;
	
	private String phoneNumber;
	
	private String name;
	
	private int payoutCarrierId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	public int getPayoutCarrierId() {
		return payoutCarrierId;
	}

	public void setPayoutCarrierId(int payoutCarrierId) {
		this.payoutCarrierId = payoutCarrierId;
	}

	@Override
	public String toString() {
		return "UserFriendEntity [id=" + id + ", userId=" + userId + ", phoneNumber=" + phoneNumber + ", name=" + name
				+ ", payoutCarrierId=" + payoutCarrierId + "]";
	}

	
	
	
	

}
