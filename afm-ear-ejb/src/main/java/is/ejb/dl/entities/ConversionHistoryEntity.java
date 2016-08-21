package is.ejb.dl.entities;

import is.ejb.bl.conversionHistory.ConversionHistoryHolder;

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
import javax.persistence.Transient;
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

/**
	Class used to store conversion history for every user 
	Each ConversionHistory row is unique for each user and contains the whole conversion history
	that is stored in json format
**/

@Entity
@XmlRootElement
@Table(name = "ConversionHistory")
public class ConversionHistoryEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;
   private int realmId;
   private int userId; //database row
   private Timestamp generationTime;
  
   //http://stackoverflow.com/questions/3503841/jpa-mysql-blob-returns-data-too-long
   //http://stackoverflow.com/questions/13932750/tinytext-text-mediumtext-and-longtext-maximum-storage-sizes
   //http://stackoverflow.com/questions/3868096/jpa-how-do-i-persist-a-string-into-a-database-field-type-mysql-text
   @Lob
   @Column 
   private String conversionHistory=null; //url that mobile app will redirect user to (this url is augmented by the server with additional params)

   @Transient
   private ConversionHistoryHolder conversionHistoryHolder = new ConversionHistoryHolder(); //we deserialise json into this in DAO

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRealmId() {
		return realmId;
	}
	
	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public ConversionHistoryHolder getConversionHistoryHolder() {
		return conversionHistoryHolder;
	}
	
	public void setConversionHistoryHolder(
			ConversionHistoryHolder conversionHistoryHolder) {
		this.conversionHistoryHolder = conversionHistoryHolder;
	}
	
	public String getConversionHistory() {
		return conversionHistory;
	}
	
	public void setConversionHistory(String conversionHistory) {
		this.conversionHistory = conversionHistory;
	}

	public Timestamp getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(Timestamp generationTime) {
		this.generationTime = generationTime;
	}

	@Override
	public String toString() {
		return "ConversionHistoryEntity [id=" + id + ", realmId=" + realmId + ", userId=" + userId + ", generationTime=" + generationTime
				+ ", conversionHistory=" + conversionHistory + ", conversionHistoryHolder=" + conversionHistoryHolder + "]";
	}
	
	
	
	
}



