package is.ejb.bl.notificationSystems.apns;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/*
import com.relayrides.pushy.apns.ApnsEnvironment;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.PushManagerConfiguration;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.MalformedTokenStringException;
import com.relayrides.pushy.apns.util.SSLContextUtil;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import ejb.bl.notificationSystems.gcm.GoogleNotificationSender;

*/

//https://github.com/relayrides/pushy
//https://developer.apple.com/library/mac/technotes/tn2265/_index.html
public class IOSNotificationSenderTest {
/*
	protected static final Logger logger = Logger.getLogger(IOSNotificationSenderTest.class.getName());
	//@Inject
	//private Logger logger;
	
	public IOSNotificationSenderTest() throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, MalformedTokenStringException, InterruptedException {
		logger.info("Started ios notification sender...");
		
		final PushManager<SimpleApnsPushNotification> pushManager =
			    new PushManager<SimpleApnsPushNotification>(
			        ApnsEnvironment.getSandboxEnvironment(),
			        SSLContextUtil.createDefaultSSLContext("/home/mzj/Downloads/ios-production.p12", 
			        		"Fredsuki1!"),
			        null, // Optional: custom event loop group
			        null, // Optional: custom ExecutorService for calling listeners
			        null, // Optional: custom BlockingQueue implementation
			        new PushManagerConfiguration(),
			        "ExamplePushManager");

			pushManager.start();
				
			//final byte[] token = TokenUtil.tokenStringToByteArray(
			//	    "<5f6aa01d 8e335894 9b7c25d4 61bb78ad 740f4707 462c7eaf bebcf74f a5ddb387>");
			final byte[] token = TokenUtil.tokenStringToByteArray(
				    "<d19622fe 2a2af0cb ab1c2324 0edb5e39 7abca5ab 6e566bdb 87d0757b 20bd57f5>");
			
			final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

			payloadBuilder.setAlertBody("Ring ring, Neo.");
			payloadBuilder.setSoundFileName("ring-ring.aiff");

			final String payload = payloadBuilder.buildWithDefaultMaximumLength();

			pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));
			logger.info("data sent!");
			pushManager.shutdown();
	}


	public static void main(String[] args) throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, MalformedTokenStringException, InterruptedException {
		IOSNotificationSenderTest pusher = new IOSNotificationSenderTest();
	}
*/	
}
