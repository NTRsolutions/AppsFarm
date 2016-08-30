package is.web.beans.emailTemplates;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import is.ejb.bl.email.EmailManager;
import is.ejb.dl.entities.EmailTemplateEntity;

@ManagedBean(name="emailTemplatesBean")
@SessionScoped
public class EmailTemplatesBean {

	@Inject
	private EmailManager emailManager;
	
	private EmailTemplateTableDataModelBean emailTemplateTableDataModelBean;
	private EmailTemplateEntity createModel;
	private EmailTemplateEntity editModel;
	@PostConstruct
	public void init(){
		createModel = new EmailTemplateEntity();
		editModel = new EmailTemplateEntity();
		loadEmailTemplateDataModelBean();
	}
	
	
	 
	private void loadEmailTemplateDataModelBean(){
		List<EmailTemplateEntity> emailTemplateList = emailManager.getAllTemplates();
		emailTemplateTableDataModelBean = new EmailTemplateTableDataModelBean(emailTemplateList);
	}

	public List<SelectItem> getEmailTypes(){
		List<SelectItem> emailTypes = new ArrayList<SelectItem>();
		emailTypes.add(new SelectItem("EMAIL_REPORT_TICKET_SUCCESS","Sucess report ticket"));
		emailTypes.add(new SelectItem("EMAIL_REPORT_TICKET_FAILED","Failed report ticket"));
		return emailTypes;
	}
	
	public void insertEmailTemplate(){
		if (createModel.getName() == null || createModel.getName().length() == 0){
			raiseError("Please specify email template name.");
			return;
		}
		if (createModel.getTitle() == null || createModel.getTitle().length() == 0){
			raiseError("Please specify email template title.");
			return;
		}
		if (createModel.getContent() == null || createModel.getContent().length() == 0){
			raiseError("Please specify email template content");
			return;
		}
		raiseInfo("Email Template created!");
		
		
		createModel.setLastModifiedTime(new Timestamp(new Date().getTime()));
		emailManager.insertOrUpdateEmailTemplate(createModel);
		createModel = new EmailTemplateEntity();
		loadEmailTemplateDataModelBean();
		refresh();
		RequestContext.getCurrentInstance().execute("widgetCreateEmailTemplate.hide()");
		
	}
	
	
	public void updateEmailTemplate(){
		if (editModel.getName() == null || editModel.getName().length() == 0){
			raiseError("Please specify email template name.");
			return;
		}
		if (editModel.getTitle() == null || editModel.getTitle().length() == 0){
			raiseError("Please specify email template title.");
			return;
		}
		if (editModel.getContent() == null || editModel.getContent().length() == 0){
			raiseError("Please specify email template content.");
			return;
		}
		raiseInfo("Email Template updated!");
		
		
		editModel.setLastModifiedTime(new Timestamp(new Date().getTime()));
		emailManager.insertOrUpdateEmailTemplate(editModel);
		editModel = new EmailTemplateEntity();
		loadEmailTemplateDataModelBean();
		refresh();
		RequestContext.getCurrentInstance().execute("widgetEditEmailTemplate.hide()");
		
	}
	
	private void raiseInfo(String message){
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
		RequestContext.getCurrentInstance().update("tabView:idEmailTemplateGrowl");
	}
	
	private void raiseError(String message){
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", message));
		RequestContext.getCurrentInstance().update("tabView:idEmailTemplateGrowl");
	}
	
	public void refresh(){
		RequestContext.getCurrentInstance().update("tabView:idEmailTemplateTable");
	}
	
	public void deleteModel(EmailTemplateEntity emailTemplate){
		emailManager.deleteEmailTemplate(emailTemplate);
		loadEmailTemplateDataModelBean();
		refresh();
		raiseInfo("Email Template deleted.");
	}
	
	
	
	public EmailTemplateTableDataModelBean getEmailTemplateTableDataModelBean() {
		return emailTemplateTableDataModelBean;
	}


	public void setEmailTemplateTableDataModelBean(EmailTemplateTableDataModelBean emailTemplateTableDataModelBean) {
		this.emailTemplateTableDataModelBean = emailTemplateTableDataModelBean;
	}



	public EmailTemplateEntity getCreateModel() {
		return createModel;
	}



	public void setCreateModel(EmailTemplateEntity createModel) {
		this.createModel = createModel;
	}



	public EmailTemplateEntity getEditModel() {
		return editModel;
	}



	public void setEditModel(EmailTemplateEntity editModel) {
		this.editModel = editModel;
	}

 


	
	
	
	
	
	
	
	
	
}
