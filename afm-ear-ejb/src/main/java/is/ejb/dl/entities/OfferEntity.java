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
@Table(name = "Offer")
public class OfferEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @ManyToOne
   @JoinColumn(name = "realm", referencedColumnName = "id")
   private RealmEntity realm;
   @NotNull
   //@Size(min = 1, max = 25)
   //@Column(unique = true)
   private String offerId; //our internal id matching the id of an offer from offer wall with this one (they are the same)
   private String affiliateId;
   private String sourceId;
   private int networkId; //our internal realm id 
   private String name;
   private String adProviderCodeName;
   @Size(min = 1, max = 500)
   private String url;
   private String previewUrl;

   private double payout;
   private String payoutIsoCurrencyCode;
   private double offerPayoutInTargetCurrency;

   private String rewardCurrency;
   private double rewardValue;
	
   private double revenueSplitValue;
   private double revenueValue;
	
   private double profitValue;

   private String rewardType;

   @Lob
   @Column 
   private String description;
   private String callToAction;
   
   private Timestamp generationDate; //represents time the offer was generated
   
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public Timestamp getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}

	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}

	public double getPayout() {
		return payout;
	}

	public void setPayout(double payout) {
		this.payout = payout;
	}

	public String getPayoutIsoCurrencyCode() {
		return payoutIsoCurrencyCode;
	}

	public void setPayoutIsoCurrencyCode(String payoutIsoCurrencyCode) {
		this.payoutIsoCurrencyCode = payoutIsoCurrencyCode;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getRewardCurrency() {
		return rewardCurrency;
	}

	public void setRewardCurrency(String rewardCurrency) {
		this.rewardCurrency = rewardCurrency;
	}

	public double getRevenueSplitValue() {
		return revenueSplitValue;
	}

	public void setRevenueSplitValue(double revenueSplitValue) {
		this.revenueSplitValue = revenueSplitValue;
	}

	public double getRevenueValue() {
		return revenueValue;
	}

	public void setRevenueValue(double revenueValue) {
		this.revenueValue = revenueValue;
	}

	public double getProfitValue() {
		return profitValue;
	}

	public void setProfitValue(double profitValue) {
		this.profitValue = profitValue;
	}

	public double getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}

	public String getRewardType() {
		return rewardType;
	}

	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}

	public double getOfferPayoutInTargetCurrency() {
		return offerPayoutInTargetCurrency;
	}

	public void setOfferPayoutInTargetCurrency(double offerPayoutInTargetCurrency) {
		this.offerPayoutInTargetCurrency = offerPayoutInTargetCurrency;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCallToAction() {
		return callToAction;
	}

	public void setCallToAction(String callToAction) {
		this.callToAction = callToAction;
	}

	public String getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(String affiliateId) {
		this.affiliateId = affiliateId;
	}
	
}



