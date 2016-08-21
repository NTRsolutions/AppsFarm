package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.reporting.LogEntry;
import is.ejb.bl.reporting.LogsResponse;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class LogsViewerService {
	
	@Inject
	private DAORealm daoRealm;

	@GET
	@Produces("application/json")
	@Path("/v1/logsViewer/")
	public String getLogs(
			@QueryParam("internalTransactionId") String internalTransactionId) {
		
		try {
			if(internalTransactionId == null){
				logES(LogStatus.ERROR, "null");
				return buildFailedResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INVALID_TRANSACTION_ID);
			}
			
			RealmEntity realm = daoRealm.findByName("BPM");
			final String HOST_NAME = realm.getEsPrimaryStorageIp();
			final String CLUSTER_NAME = "airrewardz";
			
			ReportingManager reportingManager = new ReportingManager(HOST_NAME, CLUSTER_NAME);
			List<LogEntry> logs = reportingManager.getLogs(internalTransactionId);
			reportingManager.closeESClient();
			
			if(logs.isEmpty()){
				return buildFailedResponse(RespStatusEnum.FAILED, RespCodesEnum.OK_NO_CONTENT);
			}
			
			String jsonResponse = buildJsonResponse(logs, internalTransactionId);

			logES(LogStatus.OK, internalTransactionId);
			
			return jsonResponse;
		} catch (Exception e) {
			String content = internalTransactionId + " " + e.toString();
			logES(LogStatus.ERROR, content);
			return buildFailedResponse(RespStatusEnum.FAILED, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}
	}
	
	private String buildJsonResponse(List<LogEntry> logs, String id){
		LogsResponse logsJson = new LogsResponse();
		logsJson.setStatus("OK");
		logsJson.setId(id);
		logsJson.setLogs(logs);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonResponseContent = gson.toJson(logsJson);
		
		return jsonResponseContent;
	}
	
	private String buildFailedResponse(RespStatusEnum status, RespCodesEnum code){
		return "{\"status\":\"" + status + "\", " + "\"code\":\"" + code + "\"}";
	}
	
	private void logES(LogStatus logStatus, String content){
		final String ACTIVITY = Application.LOGS_VIEWER_ACTIVITY;
		Application.getElasticSearchLogger().indexLog(ACTIVITY, -1, 
				logStatus, 
				ACTIVITY + " getting logs for id: " + content);
	}
	
}
