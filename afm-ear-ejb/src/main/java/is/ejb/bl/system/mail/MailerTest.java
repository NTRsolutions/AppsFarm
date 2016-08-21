package is.ejb.bl.system.mail;

import is.ejb.bl.business.Application;
import is.ejb.bl.monitoring.server.ServerStats;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.MonitoringSetupEntity;

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

public class MailerTest {

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

	public static void main(String[] args) {
		MailerTest mt = new MailerTest();
	}
	
	public MailerTest() {
		send(new ServerStats());
	}
	
	public void send(ServerStats serverStats) {
		final String username = "mariusz.jacyno@bluepodmedia.com";
		final String password = "2kxnylJ3";
 		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "pro.turbo-smtp.com");
		props.put("mail.smtp.port", "2525");
		props.put("mail.smtp.connectiontimeout", 10000);
		props.put("mail.smtp.timeout", 10000);
		
//		final String username = "audrius.vetsikas@gmail.com";
//		final String password = "3okidoki3";
// 		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("dc@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("audrius.vetsikas@gmail.com"));

			message.setSubject("Test email");	
			
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
			System.out.println("email sent...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void send(String emailAddress, 
						String emailSubject, 
						String emailContent,
						MonitoringSetupEntity monitoringSetup) {
		final String username = monitoringSetup.getMailAccountUserName();
		final String password = monitoringSetup.getMailAccountPassword();
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", monitoringSetup.getSmtpAuth());
		props.put("mail.smtp.starttls.enable", monitoringSetup.getSmtpTTLS());
		props.put("mail.smtp.host", monitoringSetup.getSmtpHost());
		props.put("mail.smtp.port", monitoringSetup.getSmtpPort());
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("AdBroker@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
			message.setSubject(emailSubject);
			message.setText(emailContent);
			Transport.send(message);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

}
