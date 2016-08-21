package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@XmlRootElement
@Table(name = "SpinnerData")
public class SpinnerDataEntity {
	@Id
	@GeneratedValue
	private int id;
	private int userId;
	private int availableUses;
	private int totalUses;
	private Timestamp lastDailyBonus;

	
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
	public int getAvailableUses() {
		return availableUses;
	}
	public void setAvailableUses(int availableUses) {
		this.availableUses = availableUses;
	}
	public int getTotalUses() {
		return totalUses;
	}
	public void setTotalUses(int totalUses) {
		this.totalUses = totalUses;
	}
	public Timestamp getLastDailyBonus() {
		return lastDailyBonus;
	}
	public void setLastDailyBonus(Timestamp lastDailyBonus) {
		this.lastDailyBonus = lastDailyBonus;
	}
	
	
	
	
}
