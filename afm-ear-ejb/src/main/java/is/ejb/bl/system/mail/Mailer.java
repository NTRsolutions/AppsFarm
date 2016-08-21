package is.ejb.bl.system.mail;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.server.ServerStats;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.MonitoringSetupEntity;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;

public class Mailer {

	public void send(MonitoringSetupEntity monitoringSetup,
						String emailAddress, 
						String emailSubject, 
						String emailContent,
						String emailFromAddress) {
		final String username = monitoringSetup.getMailAccountUserName();
		final String password = monitoringSetup.getMailAccountPassword();
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", monitoringSetup.getSmtpAuth());
		props.put("mail.smtp.starttls.enable", monitoringSetup.getSmtpTTLS());
		props.put("mail.smtp.host", monitoringSetup.getSmtpHost());
		props.put("mail.smtp.port", monitoringSetup.getSmtpPort());
		props.put("mail.smtp.connectiontimeout", monitoringSetup.getSmtpConnectionTimeout());
		props.put("mail.smtp.timeout", monitoringSetup.getSmtpTimeout());
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
			if(emailFromAddress == null || emailFromAddress.length() == 0) {
				emailFromAddress = monitoringSetup.getMailFromAddress();
			}
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailFromAddress)); //read from config
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
			message.setSubject(emailSubject);
			message.setContent(emailContent, "text/html; charset=utf-8");
			message.setSentDate(new Date());
			Transport.send(message);
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, -1, LogStatus.OK, Application.SYSTEM_MONITORING+" status e-mail to: "+emailAddress+" from: "+emailFromAddress+" sent successfully");
		} catch(Exception exc) {
			Application.getElasticSearchLogger().indexLog(Application.SYSTEM_MONITORING, 
					monitoringSetup.getRealm().getId(), 
					LogStatus.ERROR, 
					" error sending mail to: "+emailAddress+" error: "+exc.toString());
			exc.printStackTrace();
		}
	}

}



/*
public void sendEmail(String to, String from, String host, ) {
      // Recipient's email ID needs to be mentioned.
      String to = "abcd@gmail.com";

      // Sender's email ID needs to be mentioned
      String from = "web@gmail.com";

      // Assuming you are sending email from localhost
      String host = "localhost";

      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", host);

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties);

      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO,
                                  new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("This is the Subject Line!");

         // Now set the actual message
         message.setText("This is actual message");

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      }catch (MessagingException mex) {
         mex.printStackTrace();
      }
}
*/

/*
public void send(ServerStats serverStats) {
	final String username = "audrius.vetsikas@gmail.com";
	final String password = "3okidoki3";

	Properties props = new Properties();
	props.put("mail.smtp.auth", "true");
	props.put("mail.smtp.starttls.enable", "true");
	props.put("mail.smtp.host", "smtp.gmail.com");
	props.put("mail.smtp.port", "587");

	Session session = Session.getInstance(props,
	  new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	  });

	try {

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("dc@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,
			InternetAddress.parse("audrius.vetsikas@gmail.com"));
		if(serverStats.getCpuUtilisation() < 60 && serverStats.getMemTotalUsedPercentage() < 60 && serverStats.getLogsSevere() <= 0.0) {
			message.setSubject("DC status (ok)");
		} else if(serverStats.getLogsSevere() > 0.0) {
			message.setSubject("DC status (errors: "+serverStats.getLogsSevere()+")");
		} else if(serverStats.getCpuUtilisation() > 60 || serverStats.getMemTotalUsedPercentage() > 60) {
			message.setSubject("DC status (overutilisation)");	
		}
		
		message.setText("Server status at time: "+serverStats.getCurrentTime()
			+ "\n CPU utilisation (%): "+serverStats.getCpuUtilisation()
			+ "\n Mem used (%): "+serverStats.getMemTotalUsedPercentage()
			+ "\n Java Mem usage ratio (%): "+serverStats.getMemJavaUsageRatio()* (double)100
			+ "\n HTTP load (r/s): "+serverStats.getRequestsCWMP()
			+ "\n DB load (r/s): "+serverStats.getRequestsDB()
			+ "\n ES load (r/s): "+serverStats.getRequestsMonitoring()
			+ "\n Disk reads (MB): "+serverStats.getDiskReads()
			+ "\n Disk writes (MB): "+serverStats.getDiskWrites()
			+ "\n Net upload (KB): "+serverStats.getNetUpload()
			+ "\n Net download (KB): "+serverStats.getNetDownload()
			+ "\n Logs severe: "+serverStats.getLogsSevere()
			+ "\n Logs warning: "+serverStats.getLogsWarning()
		);

		Transport.send(message);
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, LogStatus.OK, "SYSTEM_MONITORING status e-mail sent successfully...");
	} catch (MessagingException e) {
		Application.getElasticSearchLogger().indexLog(Application.GENERIC_SYSTEM_ACTIVITY_LOG, -1, LogStatus.ERROR, "SYSTEM_MONITORING error sending system status e-mail: "+e.toString());
		e.printStackTrace();
	}
}
*/

