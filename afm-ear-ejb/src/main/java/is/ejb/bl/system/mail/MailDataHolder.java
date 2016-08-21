package is.ejb.bl.system.mail;

import is.ejb.dl.entities.MonitoringSetupEntity;

import java.io.Serializable;

public class MailDataHolder implements Serializable {
	private String emailAddress;
	private String emailSubject;
	private String emailContent;
	private MonitoringSetupEntity mailboxSetup;
	private String emailFromAddress;
	private String emailType;
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getEmailSubject() {
		return emailSubject;
	}
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	public String getEmailContent() {
		return emailContent;
	}
	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}
	public MonitoringSetupEntity getMailboxSetup() {
		return mailboxSetup;
	}
	public void setMailboxSetup(MonitoringSetupEntity mailboxSetup) {
		this.mailboxSetup = mailboxSetup;
	}
	public String getEmailFromAddress() {
		return emailFromAddress;
	}
	public void setEmailFromAddress(String emailFromAddress) {
		this.emailFromAddress = emailFromAddress;
	}
	public String getEmailType() {
		return emailType;
	}
	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}
	
}
