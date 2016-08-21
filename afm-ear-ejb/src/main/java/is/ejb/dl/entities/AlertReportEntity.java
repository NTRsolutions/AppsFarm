package is.ejb.dl.entities;

import is.ejb.bl.business.Application;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;


@Cacheable(true) 
@Entity
@XmlRootElement
@Table(name = "AlertReports")
public class AlertReportEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
   
   @Id
   @GeneratedValue
   private Integer id;

   private int realmId;
   private int numberOfAlerts;
   
   private Timestamp date;
   private Timestamp dateStartInterval;
   private Timestamp dateEndInterval;

   public AlertReportEntity() {}

	public AlertReportEntity(int realmId, 
			Timestamp date, 
			Timestamp dateStartInterval, 
			Timestamp dateEndInterval,
			int numberOfAlerts)
	{
		setRealmId(realmId);
		setDate(date);
		setDateStartInterval(dateStartInterval);
		setDateEndInterval(dateEndInterval);
		setNumberOfAlerts(numberOfAlerts);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public int getNumberOfAlerts() {
		return numberOfAlerts;
	}

	public void setNumberOfAlerts(int numberOfAlerts) {
		this.numberOfAlerts = numberOfAlerts;
	}

	public Timestamp getDateStartInterval() {
		return dateStartInterval;
	}

	public void setDateStartInterval(Timestamp dateStartInterval) {
		this.dateStartInterval = dateStartInterval;
	}

	public Timestamp getDateEndInterval() {
		return dateEndInterval;
	}

	public void setDateEndInterval(Timestamp dateEndInterval) {
		this.dateEndInterval = dateEndInterval;
	}
	
	public Date getStartIntervalDateString() {
		return new Date(dateStartInterval.getTime());
	}

	public Date getEndIntervalDateString() {
		return new Date(dateEndInterval.getTime());
	}
	
	
}

//@Fetch(value = FetchMode.SUBSELECT)
