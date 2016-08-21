package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.mail.EmailType;
import is.ejb.bl.system.mail.MailManager;
import is.ejb.bl.system.mail.MailParamsHolder;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.codec.digest.DigestUtils;
import org.jgroups.protocols.BPING;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple REST service which is able to say hello to someone using HelloService Please take a look at the web.xml where JAX-RS
 * is enabled
 */

@Path("/")
public class TokenService {
	@Inject
	private Logger logger;

    @Inject
	private DAORealm daoRealm;

    @GET
    @Produces("application/json") 
    @Path("/v1/validateToken/")
    public String registerUserWithQueryRouting(@QueryParam("bhuser") String bhuser,
			@QueryParam("bhpass") String bhpass,
			@QueryParam("token") String token) { 

    	String responseMessage = "";
    	int realmId = -1;
		String dataContent = ""; 
		
    	try {
    		dataContent = 
					" bhuser: "+bhuser+
					//" bhpass: "+bhpass+
					" token: "+token;
    		logger.info(dataContent);
    		
			Application.getElasticSearchLogger().indexLog(Application.TOKEN_VALIDATION_ACTIVITY, -1, 
					LogStatus.OK, 
					Application.TOKEN_VALIDATION_ACTIVITY+" received request: "+dataContent);

    		//authorise
			if(!bhuser.equals("bhuser123") && !bhpass.equals("pa55word!")) {
				//return authorisation error
				ResponseTokenValidation responseObject = new ResponseTokenValidation();
				responseObject.setCode(RespCodesEnum.ERROR_AUTHENTICATION_FAILURE.toString());
				responseObject.setStatus(RespStatusEnum.FAILED.toString());
				responseObject.setValidatedToken(token);
				responseObject.setTokenSuccessfullyValidated(false);
				
				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;
			} else {
				//return ok!
				ResponseTokenValidation responseObject = new ResponseTokenValidation();
				responseObject.setCode(RespCodesEnum.OK.toString());
				responseObject.setStatus(RespStatusEnum.SUCCESS.toString());
				responseObject.setValidatedToken(token);
				//TODO validated against list of generated tokens for this one:
				responseObject.setTokenSuccessfullyValidated(true);
				
				//serialize into string
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String jsonResponseContent = gson.toJson(responseObject);

				return jsonResponseContent;
			}
    	} catch(Exception exc){
    		exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.TOKEN_VALIDATION_ACTIVITY+" error creating new appUser: "+dataContent+" "+exc.toString());

			ResponseTokenValidation responseObject = new ResponseTokenValidation();
			responseObject.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			responseObject.setStatus(RespStatusEnum.FAILED.toString());
			responseObject.setValidatedToken(token);
			responseObject.setTokenSuccessfullyValidated(false);
			
			//serialize into string
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonResponseContent = gson.toJson(responseObject);

			return jsonResponseContent;
    	}
    }
   
}