package is.web.beans.ar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Errors {
	
	private Map<String, String> errors;
	
	public Errors(){
		errors = new TreeMap<String, String>();
	}
	
	public void add(String key, String message){
		errors.put(key, message);
	}
	
	public String getOne(String key){
		return errors.get(key);
	}
	
	public List<String> getAll(){
		return new ArrayList<String>(errors.values());
	}
	
	public boolean isError(){
		return !errors.isEmpty();
	}

}
