package is.ejb.bl.system.support.donky;


import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DonkyManager {

	private String apiKey; // key which will be used in authentication

	private final String apiURL = "https://integrator-api.mobiledonky.com/api/content/send";

	public DonkyManager(String apiKey) {
		this.apiKey = apiKey;

	}

	public void sendRichMessage(String message, String title, String userId) {
		HttpsURLConnection connection = prepareConnection();
		String urlParameters = "[{\"audience\": {\"type\": \"SpecifiedUsers\",\"users\": [{\"userId\": \"" + userId
				+ "\"}]},\"content\": {\"type\": \"RichMessage\",\"message\": {\"body\": \"" + message
				+ "\",\"description\": \"" + title + "\",\"canReply\": true}}}]";

		System.out.println(urlParameters);
		String result = sendContent(connection, urlParameters);
		System.out.println(result);

		Application
		.getElasticSearchLogger()
		.indexLog(
				Application.REWARD_ACTIVITY,
				-1,
				LogStatus.OK,
				Application.REWARD_NOTIFICATION_ACTIVITY
						+ " "
						+ Application.REWARD_NOTIFICATION_ACTIVITY
						+ " DONKEY PARAMS DURING NOTIFICATION SENDING params: "+urlParameters
						+ " donkey manager resut: "+result);
	}

	public void sendPushMessage(String message, String senderDisplayName, String userId) {
		HttpsURLConnection connection = prepareConnection();
		String urlParameters = "[{\"audience\": {\"type\": \"SpecifiedUsers\",\"users\": [{\"userId\": \"" + userId
				+ "\"}]},\"content\": {\"type\": \"SimplePushMessage\",\"message\": {\"body\": \"" + message
				+ "\",\"senderDisplayName\": \"" + senderDisplayName + "\",\"canReply\": true}}}]";

		System.out.println(urlParameters);
		String result = sendContent(connection, urlParameters);
		System.out.println(result);

	}

	public void sendChatMessage(String message, String conversationId, String userId) {
		HttpsURLConnection connection = prepareConnection();
		String urlParameters = "[{\"audience\": {\"type\": \"SpecifiedUsers\",\"users\": [{\"userId\": \"" + userId
				+ "\"}]},\"content\": {\"type\": \"ChatMessage\",\"message\": {\"body\": \"" + message + ""
				+ "\",\"conversationId\":\"" + conversationId + "\",\"canReply\": true}}}]";

		System.out.println(urlParameters);
		String result = sendContent(connection, urlParameters);
		System.out.println(result);
	}

	private String sendContent(HttpsURLConnection connection, String jsonData) {
		try {
			StringBuffer resultResponse = new StringBuffer();
			// Send post request
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(jsonData);
			wr.flush();
			wr.close();
			InputStream _is;
			if (connection.getResponseCode() == 200 || connection.getResponseCode() == 204) {
				_is = connection.getInputStream();
			} else {
				/* error from server */
				_is = connection.getErrorStream();
			}

			if (_is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(_is));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					resultResponse.append(inputLine);
				}
				in.close();

			}
			_is.close();
			return resultResponse.toString();
		} catch (Exception exc) {
			exc.printStackTrace();
			
			Application.getElasticSearchLogger().indexLog(
					Application.REWARD_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.REWARD_NOTIFICATION_ACTIVITY
							+ " "
							+ Application.REWARD_NOTIFICATION_ACTIVITY
							+ " DONKEY PARAMS DURING NOTIFICATION SENDING json: "+jsonData
							+ " error: "+exc.toString());

			return exc.getMessage();
		}

	}

	private HttpsURLConnection prepareConnection() {

		try {
			URL obj = new URL(apiURL);
			HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("ApiKey", apiKey);

			return connection;
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

}
