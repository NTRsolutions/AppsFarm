package is.ejb.bl.email;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import is.ejb.bl.system.mail.MailManager;
import is.ejb.dl.dao.DAOEmailTemplate;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.EmailTemplateEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTicketEntity;

@Stateless
public class EmailManager {

	@Inject
	private DAOEmailTemplate daoEmailTemplate;
	@Inject
	private MailManager mailManager;
	@Inject
	private DAORealm daoRealm;
	
	public List<EmailTemplateEntity> getAllTemplates() {
		List<EmailTemplateEntity> emailTemplates;
		try {
			emailTemplates = daoEmailTemplate.findAll();
		} catch (Exception exc) {
			exc.printStackTrace();
			emailTemplates = new ArrayList<EmailTemplateEntity>();
		}
		return emailTemplates;
	}
	
	public boolean deleteEmailTemplate(EmailTemplateEntity emailTemplate) {
		try {
			daoEmailTemplate.delete(emailTemplate);
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	public boolean insertOrUpdateEmailTemplate(EmailTemplateEntity emailTemplate) {
		try {
			daoEmailTemplate.createOrUpdate(emailTemplate);
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}
	
	public EmailTemplateEntity getEmailTemplateByID(int id){
		try{
			EmailTemplateEntity emailTemplate = daoEmailTemplate.findById(id);
			return emailTemplate;
		}catch (Exception exc){
			exc.printStackTrace();
			return null;
		}
	}
	
	
	public EmailHolder setupEmailTemplate(int templateId, RewardTicketEntity rewardTicket, String rewardResult){
		String result = "";
		EmailTemplateEntity emailTemplate = getEmailTemplateByID(templateId);
		result = emailTemplate.getContent();
		result = result.replaceAll("\\{rewardName\\}", rewardTicket.getRewardName());
		result = result.replaceAll("\\{rewardValue\\}", ""+rewardTicket.getCreditPoints());
		result = result.replaceAll("\\{rewardResult\\}", rewardResult);
		EmailHolder holder = new EmailHolder();
		holder.setContent(result);
		holder.setRecipent(rewardTicket.getEmail());
		holder.setTitle(emailTemplate.getTitle());
		return holder;
	}
	
	public boolean sendEmail(EmailHolder holder, int realmId){
		try{
			RealmEntity realm = daoRealm.findById(realmId);
			boolean result = mailManager.sendEmail(realm, holder);
			return result;
		} catch(Exception exc){
			exc.printStackTrace();
			return false;
		}
	}
	
	
	
	

}
