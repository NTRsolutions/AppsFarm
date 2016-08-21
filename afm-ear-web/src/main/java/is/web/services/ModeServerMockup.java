package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

@Path("/")
public class ModeServerMockup {

	@Inject
	private Logger log;

	@Inject
	private DAORealm daoRealm;

	private final String serverAddress = "http://localhost:8080/ab/svc/v1/reward/";

	@POST
	@Path("/v1/modeServerMockup/")
	public Response requestRewardModeServerMockup(final MultivaluedMap<String, String> formParams) throws Exception {

		String data = "";
		final Map<String, String> parameters = new HashMap<String, String>();

		Iterator<String> it = formParams.keySet().iterator();

		while (it.hasNext()) {
			String theKey = (String) it.next();
			parameters.put(theKey, formParams.getFirst(theKey));
			data += " " + theKey + ":" + formParams.getFirst(theKey);

		}

		final String dataForTask = data;
		RealmEntity realm = daoRealm.findByName("BPM");

		log(LogStatus.OK, "Received data for mode server mockup: " + data);
		log(LogStatus.OK, "Event will be returned in " + realm.getModeMockupTimer() + " seconds..." + data);

		setupTimer(parameters, dataForTask, realm);

		return Response.accepted().build();
	}

	private void setupTimer(final Map<String, String> parameters, final String dataForTask, RealmEntity realm) {
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				InputStream is = null;
				try {
					log(LogStatus.OK, "Running reward event for:" + dataForTask);
					String urlPassed = serverAddress + parameters.get("OriginTransactionID") + "/SUCCESS/successStatus/vouchers;123;456;789/";
					log(LogStatus.OK, "Calling ws:" + urlPassed);
					URLConnection conn = new URL(urlPassed).openConnection();
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
					is = conn.getInputStream();

					BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
					String requestResult = readAll(rd);
					log(LogStatus.OK, "Executed url: " + urlPassed + "result:" + requestResult);

				} catch (Exception exc) {
					log(LogStatus.ERROR, exc.getMessage());
					exc.printStackTrace();

				} finally {
					try {
						is.close();
					} catch (Exception exc) {
						exc.printStackTrace();
					}

				}
			}
		}, realm.getModeMockupTimer());
	}

	private void log(LogStatus logStatus, String message) {
		Application.getElasticSearchLogger().indexLog(Application.MODE_MOCKUP_ACTIVITY, -1, logStatus, message);
		log.info(message);
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
