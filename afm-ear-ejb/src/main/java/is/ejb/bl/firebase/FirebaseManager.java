package is.ejb.bl.firebase;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;

@Stateless
public class FirebaseManager {

	@Inject
	private Logger logger;
	private Gson gson;
	private final String FIREBASE_API_SEND_ENDPOINT_ADDRESS = "https://fcm.googleapis.com/fcm/send";

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	public boolean sendMessage(FirebaseMessage message) {
		try {
			HttpsURLConnection con = (HttpsURLConnection)setupConnection(message);
			logger.info("Executing connection...");
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			String json = gson.toJson(message.getRequest());
			logger.info(json);
			wr.writeBytes(json);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			con.disconnect();
			logger.info("Firebase response code: " + responseCode + " result: " + response);
			return true;
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}

	}

	private HttpsURLConnection setupConnection(FirebaseMessage message) {
		try {
			String url = FIREBASE_API_SEND_ENDPOINT_ADDRESS;
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "key=" + message.getApiKey());
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			logger.info("Setup firebase with apikey: " + message.getApiKey() );
			return con;
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}
	
	
	
	public FirebaseMessage prepareFirebaseMessage(String apiKey,String title,String body,String deviceToken ){
		FirebaseMessage message = new FirebaseMessage();
		message.setApiKey(apiKey);
		FirebaseRequest request = new FirebaseRequest();
		request.setTime_to_live(2419200);
		request.setPriority(10);
		request.setDelay_while_idle(false);
		request.setTo(deviceToken);
		FirebaseNotification notification = new FirebaseNotification();
		notification.setText(body);
		notification.setTitle(title);
		
		request.setNotification(notification);
		message.setRequest(request);
		return message;
		
	}

}
