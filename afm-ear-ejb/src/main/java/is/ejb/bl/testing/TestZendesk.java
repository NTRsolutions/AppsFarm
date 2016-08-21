package is.ejb.bl.testing;

import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Ticket;
import org.zendesk.client.v2.model.Ticket.Requester;

public class TestZendesk {
	public static void main(String[] args) {
		new TestZendesk();
	}
	
	public TestZendesk() {
		Zendesk zd = new Zendesk.Builder("https://airrewardz.zendesk.com")
        .setUsername("tony.ford@bluepodmedia.com")
        .setPassword("Aef280409") //.setToken("...") 
        .build();
		
		Requester requester = new Requester("mariusz","mariusz.jacyno@gmail.com");
		Ticket ticket = new Ticket();
		ticket.setRequester(requester);
		ticket.setSubject("testSubject3");
		ticket.setDescription("testDesc3");
		Ticket createdTicket = zd.createTicket(ticket);
		System.out.println("test ticket issued to zendesk at time: "+createdTicket.getCreatedAt().toString()+" Status: "+createdTicket.getStatus().toString());

		for (Ticket t: zd.getTickets()) {
			System.out.println(t.getDescription());
		}

		zd.close();
		
	}
}
