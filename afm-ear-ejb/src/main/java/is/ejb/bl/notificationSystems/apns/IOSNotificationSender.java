package is.ejb.bl.notificationSystems.apns;

import is.ejb.bl.notificationSystems.gcm.GoogleNotificationSender;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

//https://github.com/notnoop/java-apns
//https://github.com/notnoop/java-apns/wiki/Compare-with-Javapns
public class IOSNotificationSender {

	protected static final Logger logger = Logger.getLogger(IOSNotificationSender.class.getName());
	//@Inject
	//private Logger logger;

	public IOSNotificationSender() {
		logger.info("Started ios notification sender...");
		
	}

	public String pushNotification(String certDir, String tokenId, String content) throws IOException {
		String current = new java.io.File( "." ).getCanonicalPath();
        String currentDir = System.getProperty("user.dir");
        
		ApnsService service =
			    APNS.newService()
			    .withCert(certDir+"/certs/ios-production.p12", "Fredsuki1!")
			    //.withSandboxDestination()
			    .withAppleDestination(true)
			    .build();
		String payload = APNS.newPayload().alertBody(content).category("[adbroker]").build();
		service.push(tokenId, payload);		
		
		/*
		Map<String, Date> inactiveDevices = service.getInactiveDevices();
		for (String deviceToken : inactiveDevices.keySet()) {
		    Date inactiveAsOf = inactiveDevices.get(deviceToken);
		    System.out.println("feedback service called..");
		}
		*/
		/*
		String payload = APNS.newPayload()
            .badge(3)
            .customField("secret", "what do you think?")
            .localizedKey("GAME_PLAY_REQUEST_FORMAT")
            .localizedArguments("Jenna", "Frank")
            .actionKey("Play").build();

		service.push(token, payload);
		 */
		return payload;
	}

	public void pushNotificationTest(String content) throws IOException {
		String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println("Current dir:"+current);
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" +currentDir);
        
		ApnsService service =
			    APNS.newService()
			    .withCert("/home/mzj/Downloads/ios-production.p12", "Fredsuki1!")
			    //.withSandboxDestination()
			    .withAppleDestination(true)
			    .build();
		String payload = APNS.newPayload().alertBody(content).category("[adbroker]").build();
		System.out.println("Payload: "+payload);
		String token = "57b29fdba6991a262cc90083868c2c4477e12b82031fc01c1ca22478e48b8ed0";
		service.push(token, payload);		
		
		/*
		Map<String, Date> inactiveDevices = service.getInactiveDevices();
		for (String deviceToken : inactiveDevices.keySet()) {
		    Date inactiveAsOf = inactiveDevices.get(deviceToken);
		    System.out.println("feedback service called..");
		}
		*/
		/*
		String payload = APNS.newPayload()
            .badge(3)
            .customField("secret", "what do you think?")
            .localizedKey("GAME_PLAY_REQUEST_FORMAT")
            .localizedArguments("Jenna", "Frank")
            .actionKey("Play").build();

		service.push(token, payload);
		 */
	}

	public static void main(String[] args) throws IOException {
		IOSNotificationSender pusher = new IOSNotificationSender();
		pusher.pushNotificationTest("Test1");
	}
}
