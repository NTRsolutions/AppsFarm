package is.ejb.dl.entities;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
@Table(name = "NetworkStatsHourly")
public class NetworkStatsHourlyEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   private int realmId;
   @NotNull
   //@Size(min = 1, max = 25)
   //@Column(unique = true)
   private double payout; //what we get for conversion
   private double profit; //what is our revenue
   private double reward; //how much we spend on rewards
   private String payoutIsoCurrencyCode;
   
   private int clicks;
   private int conversions;
   private Timestamp generationStartDate; //represents time the offer was generated
   private Timestamp generationEndDate; //represents time the offer was generated
   
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public double getPayout() {
		return payout;
	}

	public void setPayout(double payout) {
		this.payout = payout;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String getPayoutIsoCurrencyCode() {
		return payoutIsoCurrencyCode;
	}

	public void setPayoutIsoCurrencyCode(String payoutIsoCurrencyCode) {
		this.payoutIsoCurrencyCode = payoutIsoCurrencyCode;
	}

	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	public int getConversions() {
		return conversions;
	}

	public void setConversions(int conversions) {
		this.conversions = conversions;
	}

	public Timestamp getGenerationStartDate() {
		return generationStartDate;
	}

	public void setGenerationStartDate(Timestamp generationStartDate) {
		this.generationStartDate = generationStartDate;
	}

	public Timestamp getGenerationEndDate() {
		return generationEndDate;
	}

	public void setGenerationEndDate(Timestamp generationEndDate) {
		this.generationEndDate = generationEndDate;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}
	
}



