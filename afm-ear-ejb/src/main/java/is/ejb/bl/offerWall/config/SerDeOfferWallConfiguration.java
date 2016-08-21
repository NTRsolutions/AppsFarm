package is.ejb.bl.offerWall.config;

import is.ejb.bl.offerWall.content.OfferWallContent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerDeOfferWallConfiguration {
	
	@Inject
	private Logger logger;

	public String serialize(OfferWallConfiguration dataHolder) throws IOException
	{
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		String jsonOutput = gson.toJson(dataHolder);
		//logger.info("=> serialized output: "+jsonOutput);
		return jsonOutput;
	}

	public OfferWallConfiguration deserialize(String content) throws IOException
	{
		//logger.info("=> dserializining content: "+content);
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();//.setPrettyPrinting().create();
		//Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		OfferWallConfiguration dataHolder = gson.fromJson(content, OfferWallConfiguration.class);
		
		return dataHolder;
	}
}
