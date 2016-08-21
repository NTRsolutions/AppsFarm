package is.ejb.bl.system.support;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Ticket;
import org.zendesk.client.v2.model.Ticket.Requester;
import org.zendesk.client.v2.model.User;

@Stateless
public class ZendeskManager {

	@Inject
	private Logger logger;

	private String zendeskUrlG = "https://goahead.zendesk.com/";
	private String zendeskUserG = "support@trippareward.com";
	private String zendeskPasswordG = "Blu3p0d";
	// String zendeskUserG = "mariusz.jacyno@bluepodmedia.com";
	// String zendeskPasswordG = "goaheadTest";

	private String zendeskUrlC = "https://cinetreatshelp.zendesk.com/";
	private String zendeskUserC = "sam.armour@bluepodmedia.com";
	private String zendeskPasswordC = "cantona";

	public Ticket createTicket(String userName, String userEmail, String subject, String content, String zendeskUrl,
			String zendeskUser, String zendeskPassword, String applicationName) {

		logger.info("Creating ticket with data: userName: " + userName + " userEmail: " + userEmail + " subject: "
				+ subject + " content: " + content + " zedndeskUrl: " + zendeskUrl + " zendeskUser: " + zendeskUser
				+ " zendeskPassword:" + zendeskPassword + " application: " + applicationName);

		Zendesk zd = null;

		if (applicationName != null) {
			if (applicationName.toLowerCase().contains("goahead")) {
				zd = new Zendesk.Builder(zendeskUrlG).setUsername(zendeskUserG).setPassword(zendeskPasswordG) // .setToken("...")
						.build();
			}
			if (applicationName.toLowerCase().contains("airrewardz")) {
				zd = new Zendesk.Builder(zendeskUrl).setUsername(zendeskUser).setPassword(zendeskPassword) // .setToken("...")
						.build();
			}

			if (applicationName.toLowerCase().contains("cine")) {
				zd = new Zendesk.Builder(zendeskUrlC).setUsername(zendeskUserC).setPassword(zendeskPasswordC) // .setToken("...")
						.build();
			}

		} else {
			zd = new Zendesk.Builder(zendeskUrl).setUsername(zendeskUser).setPassword(zendeskPassword) // .setToken("...")
					.build();
		}

		Requester requester = new Requester(userName, userEmail);
		Ticket ticket = new Ticket();
		ticket.setRequester(requester);
		ticket.setSubject(subject);
		ticket.setDescription(content);
		ticket.setRecipient("support@airrewardz.zendesk.com"); // test if should
																// be here
		// ticket.setComment(new Comment("testComment2")); //test
		Ticket createdTicket = zd.createTicket(ticket);

		if (zd != null)
			zd.close();
		// System.out.println("test ticket issued to zendesk at time:
		// "+createdTicket.getCreatedAt().toString()+" Status:
		// "+createdTicket.getStatus().toString());

		logger.info("Ticket issued to zendesk at time: " + createdTicket.getCreatedAt().toString() + " Status:"
				+ createdTicket.getStatus().toString() + " ticket id: " + createdTicket.getId());
		return createdTicket;
	}

	public void AddCommentToTicket(int ticketId, String username, String message, String zendeskUrl, String zendeskUser,
			String zendeskPassword, String applicationName) {

		Zendesk zd = null;
		System.out.println(applicationName);
		if (applicationName != null) {
			if (applicationName.toLowerCase().contains("goahead")) {
				zd = new Zendesk.Builder(zendeskUrlG).setUsername(zendeskUserG).setPassword(zendeskPasswordG) // .setToken("...")
						.build();
			}
			if (applicationName.toLowerCase().contains("cine")) {
				zd = new Zendesk.Builder(zendeskUrlC).setUsername(zendeskUserC).setPassword(zendeskPasswordC) // .setToken("...")
						.build();
			}

		} else {
			zd = new Zendesk.Builder(zendeskUrl).setUsername(zendeskUser).setPassword(zendeskPassword) // .setToken("...")
					.build();
		}

		// check if is there Ticket with this id:
		Ticket ticket = zd.getTicket(Long.valueOf(ticketId));
		if (ticket != null) {

			Iterable<Comment> iterable = zd.getTicketComments(Long.valueOf(ticketId));
			Iterator<Comment> iterator = iterable.iterator();
			Long userId = 0L;
			while (iterator.hasNext()) {
				Comment comment = iterator.next();
				if (comment != null) {
					User user = zd.getUser(comment.getAuthorId());
					if (user.getName().equals(username)) {
						userId = comment.getAuthorId();
						break;
					}
				}

			}

			if (userId != 0L) {
				// create new comment
				Comment comment = new Comment();
				comment.setAuthorId(userId);
				comment.setBody(message);
				ticket.setComment(comment);
				zd.updateTicket(ticket);

			}
		}
		if (zd != null)
			zd.close();
	}

	public String getTicketStatus(int ticketId, String zendeskUrl, String zendeskUser, String zendeskPassword,
			String applicationName) {
		Zendesk zd = null;

		if (applicationName != null) {

			if (applicationName.toLowerCase().contains("goahead")) {

				zd = new Zendesk.Builder(zendeskUrlG).setUsername(zendeskUserG).setPassword(zendeskPasswordG) // .setToken("...")
						.build();
			}
			if (applicationName.toLowerCase().contains("cine")) {

				zd = new Zendesk.Builder(zendeskUrlC).setUsername(zendeskUserC).setPassword(zendeskPasswordC) // .setToken("...")
						.build();

			}
		} else {
			zd = new Zendesk.Builder(zendeskUrl).setUsername(zendeskUser).setPassword(zendeskPassword) // .setToken("...")
					.build();
		}

		String status = zd.getTicket(ticketId).getStatus().toString();
		if (zd != null)
			zd.close();
		return status;
	}

	public void setTicketTags(Ticket ticket, List<String> tags, String zendeskUrl, String zendeskUser,
			String zendeskPassword, String applicationName) {
		Zendesk zd = null;

		if (applicationName != null) {
			if (applicationName.toLowerCase().contains("goahead")) {
				zd = new Zendesk.Builder(zendeskUrlG).setUsername(zendeskUserG).setPassword(zendeskPasswordG) // .setToken("...")
						.build();
			}
			if (applicationName.toLowerCase().contains("cine")) {
				zd = new Zendesk.Builder(zendeskUrlC).setUsername(zendeskUserC).setPassword(zendeskPasswordC) // .setToken("...")
						.build();
			}
		} else {
			zd = new Zendesk.Builder(zendeskUrl).setUsername(zendeskUser).setPassword(zendeskPassword) // .setToken("...")
					.build();
		}

		ticket.setTags(tags);
		zd.updateTicket(ticket);
		if (zd != null)
			zd.close();
	}
}