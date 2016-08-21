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
@Table(name = "SnapdealOffers")
public class SnapdealOffersEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @ManyToOne
   @JoinColumn(name = "realm", referencedColumnName = "id")
   private RealmEntity realm;

   @NotNull
   private String categoryName;
   private int numberOfOffers;
   private Timestamp generationDate; //represents last time the wall content was generated
   
   @Lob
   @Column 
   private String offersJson = null;

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
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public int getNumberOfOffers() {
		return numberOfOffers;
	}
	
	public void setNumberOfOffers(int numberOfOffers) {
		this.numberOfOffers = numberOfOffers;
	}
	
	public Timestamp getGenerationDate() {
		return generationDate;
	}
	
	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}
	
	public String getOffersJson() {
		return offersJson;
	}
	
	public void setOffersJson(String offersJson) {
		this.offersJson = offersJson;
	}

	@Override
	public String toString() {
		return "SnapdealOffersEntity [id=" + id + ", realm=" + realm + ", categoryName=" + categoryName
				+ ", numberOfOffers=" + numberOfOffers + ", generationDate=" + generationDate + ", offersJson="
				+ offersJson + "]";
	}
   
}



