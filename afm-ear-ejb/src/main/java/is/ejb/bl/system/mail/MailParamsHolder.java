package is.ejb.bl.system.mail;

import is.ejb.bl.reporting.ReportDH;

import java.util.ArrayList;

public class MailParamsHolder {
	
	private String emailRecipientFullName;
	private String emailRecipientAddress;
	private String emailRecipientNewPassword;
	
	private String problemType;
	private String problemMessage;
	private String adminPanelLink;
	
	private String code;
	private String emailInviting;
	private String activationLink;
	
	private String retailer;
	private String purchaseAmount;
	private String cashbackAmount;
	
	private ArrayList<ReportDH> reports;

	
	public String getEmailRecipientFullName() {
		return emailRecipientFullName;
	}

	public void setEmailRecipientFullName(String emailReceipientFullName) {
		this.emailRecipientFullName = emailReceipientFullName;
	}

	public String getEmailRecipientAddress() {
		return emailRecipientAddress;
	}

	public void setEmailRecipientAddress(String emailReceipientAddress) {
		this.emailRecipientAddress = emailReceipientAddress;
	}

	public String getEmailRecipientNewPassword() {
		return emailRecipientNewPassword;
	}

	public void setEmailRecipientNewPassword(String emailReceipientNewPassword) {
		this.emailRecipientNewPassword = emailReceipientNewPassword;
	}

	public String getProblemType() {
		return problemType;
	}

	public void setProblemType(String problemType) {
		this.problemType = problemType;
	}

	public String getProblemMessage() {
		return problemMessage;
	}

	public void setProblemMessage(String problemMessage) {
		this.problemMessage = problemMessage;
	}

	public String getAdminPanelLink() {
		return adminPanelLink;
	}

	public void setAdminPanelLink(String adminPanelLink) {
		this.adminPanelLink = adminPanelLink;
	}

	public ArrayList<ReportDH> getReports() {
		return reports;
	}

	public void setReports(ArrayList<ReportDH> reports) {
		this.reports = reports;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEmailInviting() {
		return emailInviting;
	}

	public void setEmailInviting(String emailInviting) {
		this.emailInviting = emailInviting;
	}

	public String getActivationLink() {
		return activationLink;
	}

	public void setActivationLink(String activationLink) {
		this.activationLink = activationLink;
	}

	public String getRetailer() {
		return retailer;
	}

	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}

	public String getPurchaseAmount() {
		return purchaseAmount;
	}

	public void setPurchaseAmount(String purchaseAmount) {
		this.purchaseAmount = purchaseAmount;
	}

	public String getCashbackAmount() {
		return cashbackAmount;
	}

	public void setCashbackAmount(String cashbackAmount) {
		this.cashbackAmount = cashbackAmount;
	}
	
}
