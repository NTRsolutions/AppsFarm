package is.ejb.bl.cloudtraxConfiguration;

import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.dl.entities.CloudtraxConfigurationEntity;

import java.io.IOException;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerDeCloudtraxConfiguration {

	
	public CloudtraxConfigurationEntity deserialize(CloudtraxConfigurationEntity entity) throws IOException
	{
		
		
		//logger.info("=> dserializining content: "+content);
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();//.setPrettyPrinting().create();
		//Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		AllowedDomains allowedDomains = gson.fromJson(entity.getAllowedDomains(), AllowedDomains.class);
		entity.setAllowedDomainsList(allowedDomains.getAllowedDomains());
		
		//String [] accessPoints = gson.fromJson(entity.getAccessPoints(), String[].class);
		//configuration.setAccessPoints(Arrays.asList(accessPoints));
		
		AccessPoints accessPoints = gson.fromJson(entity.getAccessPoints(), AccessPoints.class);
		entity.setAccessPointsList(accessPoints.getAccessPoints());
		System.out.println("CONFIG"+entity);
		
		return entity;
	}
	
	public CloudtraxConfigurationEntity serialize(CloudtraxConfigurationEntity configuration)
	{
		
		try {
			configuration.setAllowedDomains(serializeAllowedDomains(configuration));
			configuration.setAccessPoints(serializeAccessPoints(configuration));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return configuration;
		
	}
	
	
	
	public String serializeAllowedDomains(CloudtraxConfigurationEntity configuration) throws IOException
	{
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		AllowedDomains ad = new AllowedDomains();
		ad.setAllowedDomains(configuration.getAllowedDomainsList());
		String jsonOutput = gson.toJson(ad);
		//logger.info("=> serialized output: "+jsonOutput);
		return jsonOutput;
	}
	
	
	public String serializeAccessPoints(CloudtraxConfigurationEntity configuration) throws IOException
	{
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		AccessPoints aps = new AccessPoints();
		aps.setAccessPoints(configuration.getAccessPointsList());
		String jsonOutput = gson.toJson(aps);
		//logger.info("=> serialized output: "+jsonOutput);
		return jsonOutput;
	}
}
