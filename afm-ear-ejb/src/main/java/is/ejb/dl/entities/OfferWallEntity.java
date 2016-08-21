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
@Table(name = "OfferWall")
public class OfferWallEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @ManyToOne
   @JoinColumn(name = "realm", referencedColumnName = "id")
   private RealmEntity realm;

   private boolean active=false;
   
   private boolean sortOffers = true;
   private boolean removeEmptyOfferWalls = true;
   private boolean appendOfferNumbering = true;
   private boolean generateSingleWallListing = false;
   private boolean appendPositioning = false;
   
   @NotNull
   //@Size(min = 1, max = 25)
   //@Column(unique = true)
   private String name;
   private String providerCodeName;
   private String tags;
   private String description;
   private int numberOfOffers; //deprecated - remove once multi-offer pages are provided

   private String targetCountriesFilter;
   private String targetDevicesFilter;
   private String rewardTypeName;
   
   private Timestamp generationDate; //represents last time the wall content was generated

   private long numberOfGeneratedOffers=0;
   private long numberOfRequestedOffers=0;
   private long numberOfOffersInSelectionPool=0;
   
   @Lob
   @Column(length=1048576) 
   private String configuration = null;
   
   @Lob
   @Column(length=1048576) 
   private String content = null;

   @Lob
   @Column 
   private String blockedKeywords;

   @Lob
   @Column(length=1048576) 
   private String positioning = null;

   public void resetOfferWallStats(){
	   numberOfGeneratedOffers = 0;
	   numberOfOffersInSelectionPool = 0;
	   numberOfRequestedOffers = 0;
   }
   
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getProviderCodeName() {
		return providerCodeName;
	}

	public void setProviderCodeName(String providerCodeName) {
		this.providerCodeName = providerCodeName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getNumberOfOffers() {
		return numberOfOffers;
	}

	public void setNumberOfOffers(int numberOfOffers) {
		this.numberOfOffers = numberOfOffers;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public Timestamp getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public String getTargetCountriesFilter() {
		return targetCountriesFilter;
	}

	public void setTargetCountriesFilter(String targetCountriesFilter) {
		this.targetCountriesFilter = targetCountriesFilter;
	}

	public String getTargetDevicesFilter() {
		return targetDevicesFilter;
	}

	public void setTargetDevicesFilter(String targetDevicesFilter) {
		this.targetDevicesFilter = targetDevicesFilter;
	}

	public boolean isSortOffers() {
		return sortOffers;
	}

	public void setSortOffers(boolean sortOffers) {
		this.sortOffers = sortOffers;
	}

	public boolean isGenerateSingleWallListing() {
		return generateSingleWallListing;
	}

	public void setGenerateSingleWallListing(boolean generateSingleWallListing) {
		this.generateSingleWallListing = generateSingleWallListing;
	}

	public long getNumberOfGeneratedOffers() {
		return numberOfGeneratedOffers;
	}

	public void setNumberOfGeneratedOffers(long numberOfGeneratedOffers) {
		this.numberOfGeneratedOffers = numberOfGeneratedOffers;
	}

	public long getNumberOfRequestedOffers() {
		return numberOfRequestedOffers;
	}

	public void setNumberOfRequestedOffers(long numberOfRequestedOffers) {
		this.numberOfRequestedOffers = numberOfRequestedOffers;
	}

	public long getNumberOfOffersInSelectionPool() {
		return numberOfOffersInSelectionPool;
	}

	public void setNumberOfOffersInSelectionPool(long numberOfOffersInSelectionPool) {
		this.numberOfOffersInSelectionPool = numberOfOffersInSelectionPool;
	}

	public boolean isRemoveEmptyOfferWalls() {
		return removeEmptyOfferWalls;
	}

	public void setRemoveEmptyOfferWalls(boolean removeEmptyOfferWalls) {
		this.removeEmptyOfferWalls = removeEmptyOfferWalls;
	}

	public boolean isAppendOfferNumbering() {
		return appendOfferNumbering;
	}

	public void setAppendOfferNumbering(boolean appendOfferNumbering) {
		this.appendOfferNumbering = appendOfferNumbering;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	public String getBlockedKeywords() {
		return blockedKeywords;
	}

	public void setBlockedKeywords(String blockedKeywords) {
		this.blockedKeywords = blockedKeywords;
	}

	public String getPositioning() {
		return positioning;
	}

	public void setPositioning(String positioning) {
		this.positioning = positioning;
	}

	public boolean isAppendPositioning() {
		return appendPositioning;
	}

	public void setAppendPositioning(boolean appendPositioning) {
		this.appendPositioning = appendPositioning;
	}

	
	
}



