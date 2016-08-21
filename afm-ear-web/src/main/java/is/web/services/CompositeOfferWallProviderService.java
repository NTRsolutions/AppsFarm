package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * A simple REST service which is able to say hello to someone using HelloService Please take a look at the web.xml where JAX-RS
 * is enabled
 */

@Path("/")
public class CompositeOfferWallProviderService {
	@Inject
	private Logger logger;

    @Inject
	private DAORealm daoRealm;

    @Inject
	private DAOOfferWall daoOfferWall;
 
    @GET
    @Produces("application/json")
    @Path("/v1/getCOWIds/{networkName}")
    public ResponseOWIds getCompositeOfferWallIds(@PathParam("networkName") String networkName) {
		ResponseOWIds response = new ResponseOWIds();

    	try {
    		//get realm by key
    		RealmEntity realm = daoRealm.findByName(networkName);
    		if(realm != null) {
    			//get all offers by realm and pick one at random
    			List<OfferWallEntity> listOffers = daoOfferWall.findAllByRealmIdAndActive(realm.getId(),true);
    			int[] ids = new int[listOffers.size()];
    			for(int i=0;i<listOffers.size();i++) {
    				ids[i]=listOffers.get(i).getId();
    			}
    			
    			response.setIdList(ids);
    			response.setCode(RespCodesEnum.OK.toString());
    			response.setErrorMessage("");
    			response.setNetworkName(networkName);
    			response.setStatus(RespStatusEnum.SUCCESS.toString());
    			
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(), 
						LogStatus.OK, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_IDS_SELECTION+" "+
						Application.COW_IDS_SELECTION_IDENTIFIED+" "+
						" returning existing offer wall ids for realm: "+ realm.getName()+" network id: "+realm.getId());
				
        		return response;
    		} else {
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+
						Application.COW_IDS_SELECTION+
						" Unable to identify network, please make sure that network with provided name is correct "+
						" status: "+RespStatusEnum.FAILED.toString()+
						" error code: "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
    		
				response.setErrorMessage("Unable to identify network, please make sure that network with provided name is correct");
				response.setCode(RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());
    			response.setStatus(RespStatusEnum.FAILED.toString());
    			
    			return response;
    		}
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.COW_SELECTION_ACTIVITY+" "+Application.COW_IDS_SELECTION+" Error selecting offer: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED.toString()+
					" error code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

			response.setErrorMessage("Error: "+exc.toString());
			response.setStatus(RespStatusEnum.FAILED.toString());
			response.setCode(RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
			
    		return response;
    	}
    	
    }
    
    @GET
    @Produces("application/json")
    @Path("/v1/getCOW/{networkName}/{offerId}")
    public String getCompositeOfferWallContent(@PathParam("networkName") String networkName,
    									@PathParam("offerId") int offerId) {
    	try {
    		//get realm by key
    		RealmEntity realm = daoRealm.findByName(networkName);
    		if(realm != null) {
    			//get all offers by realm and pick one at random
    			
    			OfferWallEntity offerWall = daoOfferWall.findById(offerId);
    			if(offerWall != null) {
    				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(), 
    						LogStatus.OK, 
    						Application.COW_SELECTION_ACTIVITY+" "+
    						Application.COW_SELECTION_BY_ID+" "+
    						Application.COW_SELECTION_BY_ID_IDENTIFIED+" "+
    						" selecting offer wall with id: "+offerId+ " for realm: "+ realm.getName()+" network id: "+realm.getId());
        			logger.info("COW_SELECTION selecting offer wall with id: "+offerId+ " for realm: "+ realm.getName());
            		//return "{\"result\":\"" + offerWall.getContent() + "\"}";
            		return offerWall.getContent();
    			} else { //no offer wall with id found
    				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
    						LogStatus.ERROR, 
    						Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Unable to identify offer wall with provided id: "+offerId+" please make sure that offer id is correct"+
    								" status: "+RespStatusEnum.FAILED.toString()+
    								" error code: "+RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
    				
        			return "{\"result\":\" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_OFFER_NOT_FOUND+" error: "+ "Unable to identify offer wall with provided id: "+offerId+" please make sure that offer id is correct" +"\"}";
    			}
    		} else { //no network with network name found
				Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
						LogStatus.ERROR, 
						Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Unable to identify network, please make sure that network with provided name is correct"+
								" status: "+RespStatusEnum.FAILED.toString()+
								" error code: "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND.toString());

    			return "{\"result\":\" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND+" error: "+ "Unable to identify network, please make sure that network with provided name is correct" +"\"}";
    		}
    	} catch(Exception exc) {
    		exc.printStackTrace();
    		logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, 
					LogStatus.ERROR, 
					Application.COW_SELECTION_ACTIVITY+" "+Application.COW_SELECTION_BY_ID+" Error selecting offer: "+exc.toString()+
					" status: "+RespStatusEnum.FAILED.toString()+
					" error code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());

			return "{\"result\":\" status: "+RespStatusEnum.FAILED+"  code: "+RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR+" error: "+ exc.toString()+"\"}";
    	}
    	
    }
    

    /*
    @GET
    @Path("/json")
    @Produces({ "application/json" })
    public String getHelloWorldJSON() {
    	logger.info("returning response from json service...");
        return "{\"result\":\"" + helloService.createHelloMessage("World") + "\"}";
    }

    @GET
    @Path("/rec/{id}")
    public String getRec(@PathParam("id") String id) {
    	return "{\"result\":\"" + helloService.createHelloMessage("id="+id) + "\"}";
    }
    
    @GET
    @Path("/xml")
    @Produces({ "application/xml" })
    public String getHelloWorldXML() {
    	logger.info("returning response from xml service...");
        return "<xml><result>" + helloService.createHelloMessage("World") + "</result></xml>";
    }
    */
}