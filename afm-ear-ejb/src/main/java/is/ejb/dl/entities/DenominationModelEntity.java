package is.ejb.dl.entities;

import is.ejb.bl.denominationModels.DenominationExport;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;

@Entity
@XmlRootElement
@Table(name = "DenominationModel")
public class DenominationModelEntity implements Serializable {
   
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	@JoinColumn(name = "realm", referencedColumnName = "id")
	private RealmEntity realm;

	@DenominationExport
	@NotNull
	//@Size(min = 1, max = 25)
	//@Column(unique = true)
	private String name;
   
	@DenominationExport
	@NotNull
	private String rewardTypeName; //denomination model is associated with specific rewardType - rewardType is provided directly from the installed app

	@DenominationExport
	private String countryCode; //usually denonmination models will be associated with country code - provide this infor here as well

	private Timestamp generationDate; //represents last time the wall content was generated
   
	@DenominationExport
	private String sourcePayoutCurrencyCode;
   
	@DenominationExport
	private String targetPayoutCurrencyCode;
   
	/**
	 * default model is the one that is applied globally across all offers
	 * there can be only single default model for the same payout currency code
	 */
	private boolean defaultModel;  
   
	@DenominationExport
	@Lob
	@Column(length=1048576) 
	private String content = null;
   
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	public Timestamp getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getSourcePayoutCurrencyCode() {
		return sourcePayoutCurrencyCode;
	}

	public void setSourcePayoutCurrencyCode(String sourcePayoutCurrencyCode) {
		this.sourcePayoutCurrencyCode = sourcePayoutCurrencyCode;
	}

	public String getTargetPayoutCurrencyCode() {
		return targetPayoutCurrencyCode;
	}

	public void setTargetPayoutCurrencyCode(String targetPayoutCurrencyCode) {
		this.targetPayoutCurrencyCode = targetPayoutCurrencyCode;
	}

	public boolean isDefaultModel() {
		return defaultModel;
	}

	public void setDefaultModel(boolean defaultModel) {
		this.defaultModel = defaultModel;
	}
	
}



