package is.web.util;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.ApplicationBootstraper;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.UserEntity;
import is.web.servlets.ESController;

import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Web application lifecycle listener.
 * @author Administrator
 */

public class Context implements ServletContextListener {

	private String serverStatsResultsDir = null;

	@Inject
	private Logger log;

	@Inject	
	ApplicationBootstraper injectionApplicationBootstraper;

	@Inject	
	Application application;

    public void contextInitialized(ServletContextEvent ctx) {
  	   injectionApplicationBootstraper.init(ctx.getServletContext());
  	   log.info("=========================== Bootstraping Server Application ===========================");
 	   application.init(ctx.getServletContext());

 	   if(Application.isCpeLoggingEnabled()) {
 		    log.info("logging enabled - starting ES connector...");
 	    	ESController.log("ES", "OK", "ESClient successfully initialised...");
 	   } else {
 		  log.info("logging is disabled - ES connector not initialised...");
 	   }

  	   log.info("=========================== Server Application successfully bootstrapped ===========================");
    }

    public void contextDestroyed(ServletContextEvent ctx) {
    	//stopping server stats monitor
    	try
    	{
        	Application.destroy(ctx);
    	}
    	catch(Exception exc) 
    	{
    		System.out.println("Exception when stopping system performance monitor...");
    		exc.printStackTrace();
    	}
    }
}