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
@Table(name = "OfferFilter")
public class OfferFilterEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @ManyToOne
   @JoinColumn(name = "realm", referencedColumnName = "id")
   private RealmEntity realm;

   private boolean rejectOfferWithMissingCurrency = true;
   private boolean rejectOfferWithMissingImage = true;
   private boolean rejectOfferWithMissingUrl = true;
   private boolean rejectOfferWithMissingPayout = true;
   private boolean rejectNotIncentivisedOffers = true;
   private boolean rejectDuplicateOffers = true;
   private boolean rejectBlockedOffers = true;
   private boolean rejectStaleOffers = true;
   
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

	public boolean isRejectOfferWithMissingCurrency() {
		return rejectOfferWithMissingCurrency;
	}

	public void setRejectOfferWithMissingCurrency(
			boolean rejectOfferWithMissingCurrency) {
		this.rejectOfferWithMissingCurrency = rejectOfferWithMissingCurrency;
	}

	public boolean isRejectOfferWithMissingImage() {
		return rejectOfferWithMissingImage;
	}

	public void setRejectOfferWithMissingImage(boolean rejectOfferWithMissingImage) {
		this.rejectOfferWithMissingImage = rejectOfferWithMissingImage;
	}

	public boolean isRejectOfferWithMissingUrl() {
		return rejectOfferWithMissingUrl;
	}

	public void setRejectOfferWithMissingUrl(boolean rejectOfferWithMissingUrl) {
		this.rejectOfferWithMissingUrl = rejectOfferWithMissingUrl;
	}

	public Timestamp getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public boolean isRejectOfferWithMissingPayout() {
		return rejectOfferWithMissingPayout;
	}

	public void setRejectOfferWithMissingPayout(boolean rejectOfferWithMissingPayout) {
		this.rejectOfferWithMissingPayout = rejectOfferWithMissingPayout;
	}

	public boolean isRejectDuplicateOffers() {
		return rejectDuplicateOffers;
	}

	public void setRejectDuplicateOffers(boolean rejectDuplicateOffers) {
		this.rejectDuplicateOffers = rejectDuplicateOffers;
	}

	public boolean isRejectNotIncentivisedOffers() {
		return rejectNotIncentivisedOffers;
	}

	public void setRejectNotIncentivisedOffers(boolean rejectNotIncentivisedOffers) {
		this.rejectNotIncentivisedOffers = rejectNotIncentivisedOffers;
	}

	public boolean isRejectBlockedOffers() {
		return rejectBlockedOffers;
	}

	public void setRejectBlockedOffers(boolean rejectBlockedOffers) {
		this.rejectBlockedOffers = rejectBlockedOffers;
	}

	public boolean isRejectStaleOffers() {
		return rejectStaleOffers;
	}

	public void setRejectStaleOffers(boolean rejectStaleOffers) {
		this.rejectStaleOffers = rejectStaleOffers;
	}
	
}



