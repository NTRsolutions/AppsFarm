package is.web.geo;

import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.UserEntity;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;  
import org.primefaces.model.map.DefaultMapModel;  
import org.primefaces.model.map.LatLng;  
import org.primefaces.model.map.MapModel;  
import org.primefaces.model.map.Marker;  

@ManagedBean(name="devicesMapBean")
@SessionScoped
public class DevicesMapBean implements Serializable {

	@Inject
	private Logger logger;
	
	@Inject
	private DAOUser daoCustomer;
	@Resource(name="DefaultDS", mappedName="java:jboss/datasources/iWebDS") 	
	private DataSource ds;
	
	private UserEntity customer;

	private MapModel advancedModel;  
    private Marker marker;  	
	
    private int selectedDomainIndex=1;
    private int selectedDeviceStatusFilterIndex=1;
    
    private String mapCenterCoordinates = "36.890257,30.707417";
    //private String deviceAlertsLink = "devicesMapBean"
    
	public DevicesMapBean() {
	}
	
    @PostConstruct
    public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
	   logger.info("DevicesMapBean initialised...");
	   //refresh();
    }

    public void refresh() {
    	logger.info("refreshing DevicesMapBean...");
        RequestContext.getCurrentInstance().update("tabView:idDeviceMap");
        loadMap();
    }
    
    private void loadMap() {
    	advancedModel = new DefaultMapModel();  
    	PreparedStatement ps = null;
    	Connection con = null;
    	try {
    		con = ds.getConnection();
    		//0-page 1-page size (number of rows)
    		String strHostQuery = "SELECT serialno, lastcontact, lastInform, domainName, geoLocation FROM Hosts";
    		Statement stmt = con.createStatement();
            
    		//get cpeId from hosts table
    		ResultSet rs = stmt.executeQuery(strHostQuery);
    		while (rs.next()) {
    			String sn = rs.getString("serialno");
    			String lastContact = rs.getString("lastcontact");
    			String lastInform = rs.getString("lastInform");
    			String domainName = rs.getString("domainName");
    			String geoLocation = rs.getString("geoLocation");
    			logger.info("-> SN: "+sn+" DN: "+domainName+" GEO: "+geoLocation+" LI: "+lastInform+" LC: "+lastContact);
    			
    			try {
        			//coordinates
        			String lat = geoLocation.substring(0, geoLocation.lastIndexOf(","));
        			String lon = geoLocation.substring(geoLocation.lastIndexOf(",")+1, geoLocation.length());
        			
        			logger.info("extracted: "+lat+" "+lon);
        			
        			if(!lat.toLowerCase().equals("null") && !lon.toLowerCase().equals("null")) {
            			LatLng coord = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            			mapCenterCoordinates = (coord.getLat())+","+(coord.getLng());
            			//check if there are any alerts generated and unresolved for this device - if so then 
            	        advancedModel.addOverlay(new Marker(coord, sn, "konyaalti.png", "http://maps.google.com/mapfiles/ms/micons/blue-dot.png"));
            	        logger.info("added cpe: "+sn+" to map display...");
        			} else {
        				//raise alert for this device that it was unable to display it on map
        				logger.severe("unable to display cpe with sn: "+sn+" on map using geoLoc: "+geoLocation);
        			}
    			} catch(Exception exc) {
    				//exc.printStackTrace();
    				logger.severe(exc.toString());
    			}
              }     		
	    } catch (SQLException sql) {
    		sql.printStackTrace();
	    }
    	
	    finally {
	       try { if (ps != null) ps.close(); } catch (Exception e) {}
	       try { if (con != null) con.close(); } catch (Exception e) {}
	    }    		

    	//center map based on selected domain
    	//mapCenterCoordinates = (coord1.getLat()+(Math.random()*0.01))+","+(coord1.getLng()+(Math.random()*0.01));
        logger.info("map center coordinates are: "+mapCenterCoordinates);
          
        //Icons and Data  
//        advancedModel.addOverlay(new Marker(coord1, "CPE1", "konyaalti.png", "http://maps.google.com/mapfiles/ms/micons/blue-dot.png"));  
//        advancedModel.addOverlay(new Marker(coord2, "CPE2", "ataturkparki.png"));  
//        advancedModel.addOverlay(new Marker(coord4, "CPE3", "kaleici.png", "http://maps.google.com/mapfiles/ms/micons/pink-dot.png"));  
//        advancedModel.addOverlay(new Marker(coord3, "CPE4", "karaalioglu.png", "http://maps.google.com/mapfiles/ms/micons/yellow-dot.png"));

        RequestContext.getCurrentInstance().update("tabView:idDeviceMap");
        
        logger.info("loaded map and refreshed tab panes!");
    }
    
    public MapModel getAdvancedModel() {  
        return advancedModel;  
    }  
      
    public void onMarkerSelect(OverlaySelectEvent event) {
    	logger.info("Selected marker on map! ");
        marker = (Marker) event.getOverlay();
//        RequestContext.getCurrentInstance().update("tabView:idMapDialog");
//    	RequestContext.getCurrentInstance().execute("PF('mapWidget').show()");
    }  
      
    public void updateMap(){
    	logger.info("updating map, selected domain index: "+selectedDomainIndex+
    			" selected device filter index: "+selectedDeviceStatusFilterIndex);
/*    	
		advancedModel = new DefaultMapModel();  
        
        //Shared coordinates  
        LatLng coord1 = new LatLng(36.879466+(Math.random()*0.1), 30.667648+(Math.random()*0.1));  
        LatLng coord2 = new LatLng(36.883707+(Math.random()*0.1), 30.689216+(Math.random()*0.1));  
        LatLng coord3 = new LatLng(36.879703+(Math.random()*0.1), 30.706707+(Math.random()*0.1));  
        LatLng coord4 = new LatLng(36.885233+(Math.random()*0.1), 30.702323+(Math.random()*0.1));  
    
        
        mapCenterCoordinates = (coord1.getLat()+(Math.random()*0.01))+","+(coord1.getLng()+(Math.random()*0.01));
        logger.info("coordinates are: "+mapCenterCoordinates);
        
        //Icons and Data  
        advancedModel.addOverlay(new Marker(coord1, "CPE1", "konyaalti.png", "http://maps.google.com/mapfiles/ms/micons/blue-dot.png"));  
        advancedModel.addOverlay(new Marker(coord2, "CPE2", "ataturkparki.png"));  
        advancedModel.addOverlay(new Marker(coord4, "CPE3", "kaleici.png", "http://maps.google.com/mapfiles/ms/micons/pink-dot.png"));  
        advancedModel.addOverlay(new Marker(coord3, "CPE4", "karaalioglu.png", "http://maps.google.com/mapfiles/ms/micons/yellow-dot.png"));
    	
        //RequestContext.getCurrentInstance().update("tabView:tabDevicesMap");
    	//RequestContext.getCurrentInstance().update("tabView:idMapDialog");
    	RequestContext.getCurrentInstance().update("tabView:idDeviceMap");
*/    	
    }
    
    public Marker getMarker() {  
        return marker;  
    }

	public int getSelectedDomainIndex() {
		return selectedDomainIndex;
	}

	public void setSelectedDomainIndex(int selectedDomainIndex) {
		this.selectedDomainIndex = selectedDomainIndex;
	}

	public int getSelectedDeviceStatusFilterIndex() {
		return selectedDeviceStatusFilterIndex;
	}

	public void setSelectedDeviceStatusFilterIndex(
			int selectedDeviceStatusFilterIndex) {
		this.selectedDeviceStatusFilterIndex = selectedDeviceStatusFilterIndex;
	}

	public String getMapCenterCoordinates() {
		return mapCenterCoordinates;
	}

	public void setMapCenterCoordinates(String mapCenterCoordinates) {
		this.mapCenterCoordinates = mapCenterCoordinates;
	}  
	
}
				
