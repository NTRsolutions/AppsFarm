package is.ejb.bl.business;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class ApplicationBootstraper {

	@Inject
	private Logger logger;
    
	@Inject
	private CustomerManager managerCustomer;
	
    private static ApplicationBootstraper app;

    public ApplicationBootstraper() { 
    }
    
    public void init(ServletContext ctx) {
        app = this;//new Application();
    	logger.info("=== Starting application container ===");
    	
    	//create 'default' realm for test customer (in production this is setup through GUI)
    	try{
    		//managerCustomer.createDefaultCustomerRealm();
    	}catch(Exception exc){
    		exc.printStackTrace();
    		logger.severe("Error when creating default customer realm: "+exc.toString());
    	}
    	
    }

    private void _destroy(ServletContextEvent ctx) {
    }

    public static void destroy(ServletContextEvent ctx) {
        app._destroy(ctx);
        System.out.println("Application.contextDestroyed " + ctx.getServletContext().getContextPath());
    }

    public static ApplicationBootstraper getApplication() {
        return app;
    }
   
}