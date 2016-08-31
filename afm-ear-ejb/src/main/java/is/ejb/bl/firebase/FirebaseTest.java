package is.ejb.bl.firebase;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseTest {

	public static void main(String[] args) throws Exception {

		String url = "https://fcm.googleapis.com/fcm/send";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "key=AIzaSyDuN75Jaab1MLm9cwwogFPqHpWsw1FOB8s");
		con.setRequestProperty("Content-Type", "application/json");
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		String json = "{\"to\":\"e49ulsflPPE:APA91bGauuRTpDf928zmJOv9YndviJYX3Dl_HxRSuMXS2rEV0MIYDuMub3PG6BA0tu99vSVIywHS1fjuTvpVDXPTxEh4CfzBffVaHHEVIqBsAH2mAnkimtjPtSXQghuQctJnrx7Lzapm\",\"notification\":{\"title\":\"Yellow\",\"text\":\"Yellow\"}}";
		wr.writeBytes(json);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + json);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

	}

}
