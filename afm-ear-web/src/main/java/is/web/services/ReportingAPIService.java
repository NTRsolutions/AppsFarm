package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.reporting.IEventLog;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elasticsearch.common.joda.time.DateTime;

@Path("/")
public class ReportingAPIService {

	private final String REPORTING_SERVER_LOGIN_PARAM_NAME = "reportingServerLogin";
	private final String REPORTING_SERVER_PASS_PARAM_NAME = "reportingServerPass";
	private final String START_DATE_PARAM_NAME = "startDate";
	private final String END_DATE_PARAM_NAME = "endDate";
	private final String REWARD_TYPE_PARAM_NAME = "rewardType";
	private final String NETWORK_NAME_PARAM_NAME = "networkName";
	private final String HOST_NAME_PARAM_NAME = "hostName";
	private final String ES_CLUSTER_NAME_PARAM_NAME = "esClusterName";

	@Inject
	private Logger logger;
	
	@Inject
	private DAORealm daoRealm;

	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/userClicks/")
	public String getUserClicksAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.USER_CLICKS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/userConversions/")
	public String getUserConversionsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.USER_CONVERSIONS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/userRegistrations/")
	public String getUserRegistrationsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.USER_REGISTRATIONS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/userWallRequests/")
	public String getUserWallRequestsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.USER_WALL_REQUESTS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/crashReports/")
	public String getCrashReportsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.CRASH_REPORTS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/mobileFaults/")
	public String getMobileFaultsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.MOBILE_FAULTS);
	}
	
	@GET
	@Produces("application/json")
	@Path("/v1/reportingApi/supportRequests/")
	public String getSupportRequestsAPI(
		@QueryParam(REPORTING_SERVER_LOGIN_PARAM_NAME) String reportingServerLogin,
		@QueryParam(REPORTING_SERVER_PASS_PARAM_NAME) String reportingServerPass,
		@QueryParam(START_DATE_PARAM_NAME) String startDateString,
		@QueryParam(END_DATE_PARAM_NAME) String endDateString,
		@QueryParam(REWARD_TYPE_PARAM_NAME) String rewardType,
		@QueryParam(NETWORK_NAME_PARAM_NAME) String networkName,
		@QueryParam(HOST_NAME_PARAM_NAME) String hostName,
		@QueryParam(ES_CLUSTER_NAME_PARAM_NAME) String esClusterName) {

		return getReport(reportingServerLogin, reportingServerPass, startDateString, endDateString, 
				rewardType, networkName, hostName, esClusterName, EventType.SUPPORT_REQUESTS);
	}
	
	private String getReport(String reportingServerLogin, String reportingServerPass, String startDateString,
		String endDateString, String rewardType, String networkName, String hostName, String esClusterName, EventType eventType){

		Application.getElasticSearchLogger().indexLog(
				Application.REPORTING_API_ACTIVITY, -1, LogStatus.OK, 
					Application.REPORTING_API_ACTIVITY+ " retrieving report" + 
							" startDate: " + startDateString+
							" endDate: " + endDateString+
							" rewardType: " + rewardType+
							" networkName: " + networkName+
							" hostName: " + hostName+
							" esClusterName: " + esClusterName+
							" eventType: " + eventType
							);

		if (eventType != null) {
			try {
				validateArguments(reportingServerLogin, reportingServerPass, startDateString, endDateString,
						rewardType, networkName, hostName, esClusterName);

				if (authenticate(reportingServerLogin, reportingServerPass, networkName)) {
					DateTime startDate = new DateTime(startDateString);
					DateTime endDate = new DateTime(endDateString);

					ReportingManager reportingManager = new ReportingManager(hostName, esClusterName);
					List<IEventLog> eventLogs = new ArrayList<IEventLog>();

					switch (eventType) {
					case USER_CLICKS:
						eventLogs = reportingManager.getUserClicks(startDate, endDate, rewardType, networkName);
						break;
					case USER_CONVERSIONS:
						eventLogs = reportingManager.getUserConversions(startDate, endDate, rewardType, networkName);
						break;
					case USER_REGISTRATIONS:
						eventLogs = reportingManager.getUserRegistrations(startDate, endDate, rewardType, networkName);
						break;
					case USER_WALL_REQUESTS:
						eventLogs = reportingManager.getUserWallRequests(startDate, endDate, rewardType, networkName);
						break;
					case CRASH_REPORTS:
						eventLogs = reportingManager.getCrashReports(startDate, endDate, rewardType, networkName);
						break;
					case MOBILE_FAULTS:
						eventLogs = reportingManager.getMobileFaults(startDate, endDate, rewardType, networkName);
						break;
					case SUPPORT_REQUESTS:
						eventLogs = reportingManager.getSupportRequests(startDate, endDate, rewardType, networkName);
						break;
					default:
						break;
					}

					String response = eventLogsToFormat(eventLogs);

					Application.getElasticSearchLogger().indexLog(Application.REPORTING_API_ACTIVITY, getRealmId(networkName),
							LogStatus.OK,
							Application.REPORTING_API_ACTIVITY + " " + eventType
									+ " status: " + RespStatusEnum.SUCCESS
									+ " code: " + RespCodesEnum.OK + " content: " + response);

					return response;
				} else {
					throw new AuthenticationException("Wrong reportingServerLogin or reportingServerPass or networkName");
				}
			} catch (Exception e) {
				logger.severe(eventType + ": " + e.toString());
				Application.getElasticSearchLogger().indexLog(Application.REPORTING_API_ACTIVITY, -1,
						LogStatus.ERROR,
						Application.REPORTING_API_ACTIVITY + " " + eventType + " status: " + RespStatusEnum.FAILED
								+ " code: " + RespCodesEnum.ERROR + " error: " + e.getMessage() 
								+ " stacktrace: " + ExceptionUtils.getStackTrace(e));

				return "{\"result\":\"status: " + RespStatusEnum.FAILED
						+ " code: " + RespCodesEnum.ERROR
						+ " error: " + e.getMessage() + "\"}";
			}
		} else {
			logger.severe("Argument event is null");
			return "{\"result\":\"status: " + RespStatusEnum.FAILED
					+ " code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
					+ " error: " + "\"}";
		}
	}
	
	private boolean validateArguments(String reportingServerLogin, String reportingServerPass,
		String startDateString, String endDateString, String rewardType, String networkName,
		String hostName, String esClusterName)
			throws IllegalArgumentException {

		if (reportingServerLogin == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(REPORTING_SERVER_LOGIN_PARAM_NAME));
		}
		if (reportingServerPass == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(REPORTING_SERVER_PASS_PARAM_NAME));
		}
		if (startDateString == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(START_DATE_PARAM_NAME));
		}
		if (endDateString == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(END_DATE_PARAM_NAME));
		}
//		if (rewardType == null) {
//			throw new IllegalArgumentException(getNullParameterExceptionMessage(REWARD_TYPE_PARAM_NAME));
//		}
		if (networkName == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(NETWORK_NAME_PARAM_NAME));
		}
		if (hostName == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(HOST_NAME_PARAM_NAME));
		}
		if (esClusterName == null) {
			throw new IllegalArgumentException(getNullParameterExceptionMessage(ES_CLUSTER_NAME_PARAM_NAME));
		}

		return true;
	}
	
	private boolean authenticate(String login, String password, String networkName) throws Exception{
		RealmEntity realm = daoRealm.findByName(networkName);
		String realmLogin = realm.getReportingServerLogin();
		String realmPassword = realm.getReportingServerPassword();
		
		if(login.equals(realmLogin) && password.equals(realmPassword)){
			return true;
		} else {
			return false;
		}
	}
	
	private String eventLogsToFormat(List<IEventLog> eventLogs){
		String result = "";
		
		result += "<data>";
		for(IEventLog eventLog: eventLogs){
			result += "<dataEntry>" + eventLog.toCSV() + "</dataEntry>";
		}
		result += "</data>";
		
		return result;
	}
	
	private String getNullParameterExceptionMessage(String paramName){
		return "Parameter " + paramName + " is not set";
	}
	
	private int getRealmId(String networkName){
		try {
			return daoRealm.findByName(networkName).getId();
		} catch (Exception e) {
			return -1;
		}
	}
	
	private enum EventType{
		USER_CLICKS, 
		USER_CONVERSIONS,
		USER_REGISTRATIONS, 
		USER_WALL_REQUESTS,
		CRASH_REPORTS,
		MOBILE_FAULTS,
		SUPPORT_REQUESTS
	}
	
}
