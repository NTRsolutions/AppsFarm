package is.web.servlets;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;

public class ESController {
	
	private static LogStatus logStatus;
	
	public static void log(String informData, String logStatusStr, String msg ) {
		
		if(logStatusStr.equals(LogStatus.ERROR+"")) {
			logStatus = LogStatus.ERROR;
		}
		if(logStatusStr.equals(LogStatus.FATAL+"")) {
			logStatus = LogStatus.FATAL;
		}
		if(logStatusStr.equals(LogStatus.OK+"")) {
			logStatus = LogStatus.OK;
		}
		if(logStatusStr.equals(LogStatus.WARNING+"")) {
			logStatus = LogStatus.WARNING;
		}
		
		//System.out.println("logging stagus: "+logStatusStr+" "+logStatus);
		Application.getElasticSearchLogger().indexLog(informData, -1, logStatus, msg);	
	}
	
	public static void stop() {
		Application.getElasticSearchLogger().stop();
	}
}
