package is.ejb.bl.offerProviders.snapdeal;


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

public class SerDeSnapdealProviderConfiguration {
	
	@Inject
	private Logger logger;

	public String serialize(SnapdealProviderConfig dataHolder) throws IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(dataHolder);
		//logger.info("=> serialized output: "+jsonOutput);
		return jsonOutput;
	}

	public SnapdealProviderConfig deserialize(String content) throws IOException
	{
		//logger.info("=> dserializining content: "+content);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		SnapdealProviderConfig dataHolder = gson.fromJson(content, SnapdealProviderConfig.class);
		
		return dataHolder;
	}
}
