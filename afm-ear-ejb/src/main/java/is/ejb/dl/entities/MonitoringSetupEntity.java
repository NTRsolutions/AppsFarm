package is.ejb.dl.entities;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Properties;

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
@Table(name = "MonitoringSetup")
public class MonitoringSetupEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @ManyToOne
   @JoinColumn(name = "realm", referencedColumnName = "id")
   private RealmEntity realm;

   private boolean emailNotificationActive; //if false - no status and alerts sent via email
   private String operationStatusReportEmails;
   private String alertEmails;
   
   //mail account settings that will act as a proxy for sending alerts/status reports
   private String mailFromAddress;
   private String mailAccountUserName;
   private String mailAccountPassword;
   private String smtpAuth;
   private String smtpTTLS;
   private String smtpHost;
   private String smtpPort;
   
   private int smtpTimeout=10000;
   private int smtpConnectionTimeout=10000;
   
   @Lob
   @Column(length=1048576) 
   private String configuration = null;
   
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

	public String getOperationStatusReportEmails() {
		return operationStatusReportEmails;
	}

	public void setOperationStatusReportEmails(String operationStatusReportEmails) {
		this.operationStatusReportEmails = operationStatusReportEmails;
	}

	public String getAlertEmails() {
		return alertEmails;
	}

	public void setAlertEmails(String alertEmails) {
		this.alertEmails = alertEmails;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getMailAccountUserName() {
		return mailAccountUserName;
	}

	public void setMailAccountUserName(String mailAccountUserName) {
		this.mailAccountUserName = mailAccountUserName;
	}

	public String getMailAccountPassword() {
		return mailAccountPassword;
	}

	public void setMailAccountPassword(String mailAccountPassword) {
		this.mailAccountPassword = mailAccountPassword;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpTTLS() {
		return smtpTTLS;
	}

	public void setSmtpTTLS(String smtpTTLS) {
		this.smtpTTLS = smtpTTLS;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean isEmailNotificationActive() {
		return emailNotificationActive;
	}

	public void setEmailNotificationActive(boolean emailNotificationActive) {
		this.emailNotificationActive = emailNotificationActive;
	}

	public String getMailFromAddress() {
		return mailFromAddress;
	}

	public void setMailFromAddress(String mailFromAddress) {
		this.mailFromAddress = mailFromAddress;
	}

	public int getSmtpTimeout() {
		return smtpTimeout;
	}

	public void setSmtpTimeout(int smtpTimeout) {
		this.smtpTimeout = smtpTimeout;
	}

	public int getSmtpConnectionTimeout() {
		return smtpConnectionTimeout;
	}

	public void setSmtpConnectionTimeout(int smtpConnectionTimeout) {
		this.smtpConnectionTimeout = smtpConnectionTimeout;
	}
	
}



