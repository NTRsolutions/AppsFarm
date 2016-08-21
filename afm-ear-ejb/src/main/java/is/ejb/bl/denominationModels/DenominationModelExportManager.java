package is.ejb.bl.denominationModels;

import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.RealmEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DenominationModelExportManager {
	
	public static final String ENCODING = "UTF-8";
	
	private final String FIELD_CONTENT = "content";
	
	private final String CSV_SEPARATOR = " ";
	
	private final String ERROR_INVALID_CONTENT = "Invalid content";
	private final String ERROR_NO_CONTENT_FIELD_FOUND = "No content field found";
	private final String ERROR_UNSUPPORTED_TYPE = "Unsupported type";
	
	public DenominationModelExportManager(){
		
	}
	
	public String exportModel(DenominationModelEntity model){
		String fileContent = "";
		Field[] fields = model.getClass().getDeclaredFields();
		
		for(int i=0; i<fields.length; i++){
			if(fields[i].isAnnotationPresent(DenominationExport.class)){
				String fieldName = fields[i].getName();
				String value = invokeGetter(fieldName, model);
				if (fieldName.equals(FIELD_CONTENT)) {
					fileContent += fieldName + ": " + parseContentToCSV(value) + "\n";
				} else {
					fileContent += fieldName + ": " + value + "\n";
				}
			}
		}
		
		return fileContent;
	}
	
	public DenominationModelEntity importModel(String content, RealmEntity realm) throws Exception{
		final String REGEX = ":";
		boolean isContentField = false;
		DenominationModelEntity denominationModel = new DenominationModelEntity();
		
		InputStream inStream = new ByteArrayInputStream(content.getBytes(ENCODING));
		Scanner scanner = new Scanner(inStream);
		
		all:
		while(scanner.hasNext()){
			String line = scanner.nextLine();
			String[] parts = line.split(REGEX);
			if(parts.length != 2){
				scanner.close();
				throw new Exception(ERROR_INVALID_CONTENT);
			} else {
				String property = parts[0].trim();
				String value = parts[1].trim();
				
				if(property.equals(FIELD_CONTENT)){
					isContentField = true;
					break all;
				} else {
					if(value.isEmpty()){
						value = null;
					}
					invokeSetter(property, value, denominationModel);
				}
			}
		}
		
		if(isContentField){
			DenominationContent denCont = parseContentFromCSV(scanner);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String gsonContent = gson.toJson(denCont);
			invokeSetter(FIELD_CONTENT, gsonContent, denominationModel);
		} else {
			scanner.close();
			throw new Exception(ERROR_NO_CONTENT_FIELD_FOUND);
		}
		
		scanner.close();
		
		Calendar calendar = Calendar.getInstance();
		Timestamp generationTime = new Timestamp(calendar.getTimeInMillis());
		
		denominationModel.setGenerationDate(generationTime);
		denominationModel.setRealm(realm);
		denominationModel.setDefaultModel(false);
		
		return denominationModel;
	}
	
	private String invokeGetter(String fieldName, Object object){
		String value = "";
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Method method = null;
		try {
			method = object.getClass().getMethod(methodName);
			if (method.invoke(object) != null) {
				value = method.invoke(object).toString();
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			value = "";
		}
		return value;
	}
	
	private void invokeSetter(String fieldName, String value, Object object) throws Exception{
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			Method method = object.getClass().getMethod(methodName, field.getType());
			if(value == null){
				method.invoke(object, value);
			} else {
				method.invoke(object, convertValueToAppriopriateType(value, field.getType()));
			}
		} catch (NoSuchMethodException e) {
			throw new Exception(ERROR_INVALID_CONTENT);
		}
	}

	private String parseContentToCSV(String content){
		String line = "\n";
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		DenominationContent rowsDen = gson.fromJson(content, DenominationContent.class);
		
		List<DenominationContentRow> values = rowsDen.getRows();
		for(DenominationContentRow value: values){
			line += value.getSourceOfferPayoffValue() + CSV_SEPARATOR
					 + value.getTargetOfferPayoffValue() + CSV_SEPARATOR
					 + value.getRevenueSpit() + CSV_SEPARATOR
					 + value.getAirtimePayoff() + "\n";
		}
		
		return line;
	}
	
	private DenominationContent parseContentFromCSV(Scanner scanner) throws Exception{
		final String REGEX = CSV_SEPARATOR;
		DenominationContent content = new DenominationContent();
		List<DenominationContentRow> rows = new ArrayList<DenominationModelExportManager.DenominationContentRow>();
		while(scanner.hasNext()){
			String line = scanner.nextLine();
			line = line.replaceAll("\\t+", REGEX);
			line = line.replaceAll(REGEX + "+", REGEX);
			String[] parts = line.split(REGEX);
			if(parts.length != 4){
				throw new Exception(ERROR_INVALID_CONTENT);
			} else {
				try {
					double sourceOfferPayoffValue = Double.valueOf(parts[0].trim());
					double targetOfferPayoffValue = Double.valueOf(parts[1].trim());
					double revenueSpit = Double.valueOf(parts[2].trim());
					double airtimePayoff = Double.valueOf(parts[3].trim());
					String name = String.valueOf(sourceOfferPayoffValue)
							+ String.valueOf(targetOfferPayoffValue)
							+ String.valueOf(revenueSpit)
							+ String.valueOf(airtimePayoff);
					
					DenominationContentRow row = new DenominationContentRow();
					row.setSourceOfferPayoffValue(sourceOfferPayoffValue);
					row.setTargetOfferPayoffValue(targetOfferPayoffValue);
					row.setRevenueSpit(revenueSpit);
					row.setAirtimePayoff(airtimePayoff);
					row.setName(name);
					
					rows.add(row);
				} catch (NumberFormatException e) {
					throw new Exception(ERROR_INVALID_CONTENT);
				}
			}
		}
		content.setRows(rows);
		return content;
	}
	
	private Object convertValueToAppriopriateType(String value, Class<?> type) throws Exception{
		if(type.equals(String.class)){
			return new String(value);
		} else if(type.equals(int.class) || type.equals(Integer.class)){
			return Integer.valueOf(value);
		} else if(type.equals(double.class) || type.equals(Double.class)){
			return Double.valueOf(value);
		} else if(type.equals(float.class) || type.equals(Float.class)){
			return Float.valueOf(value);
		} else if(type.equals(long.class) || type.equals(Long.class)){
			return Long.valueOf(value);
		} else if(type.equals(boolean.class) || type.equals(Boolean.class)){
			if(value.equals("true") || value.equals("false")){
				return Boolean.valueOf(value);
			} else {
				throw new Exception(ERROR_INVALID_CONTENT);
			}
		} else if(type.equals(byte.class) || type.equals(Byte.class)){
			return Byte.valueOf(value);
		} else if(type.equals(short.class) || type.equals(Short.class)){
			return Short.valueOf(value);
		} else {
			throw new Exception(ERROR_UNSUPPORTED_TYPE);
		}
	}

	
//--------------

	protected class DenominationContent {
		
		private List<DenominationContentRow> rows;

		public List<DenominationContentRow> getRows() {
			return rows;
		}

		public void setRows(List<DenominationContentRow> rows) {
			this.rows = rows;
		}
		
	}
	
	protected class DenominationContentRow {
		
		private String name;
		private double sourceOfferPayoffValue;
		private double targetOfferPayoffValue;
		private double revenueSpit;
		private double airtimePayoff;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getSourceOfferPayoffValue() {
			return sourceOfferPayoffValue;
		}
		public void setSourceOfferPayoffValue(double sourceOfferPayoffValue) {
			this.sourceOfferPayoffValue = sourceOfferPayoffValue;
		}
		public double getTargetOfferPayoffValue() {
			return targetOfferPayoffValue;
		}
		public void setTargetOfferPayoffValue(double targetOfferPayoffValue) {
			this.targetOfferPayoffValue = targetOfferPayoffValue;
		}
		public double getRevenueSpit() {
			return revenueSpit;
		}
		public void setRevenueSpit(double revenueSpit) {
			this.revenueSpit = revenueSpit;
		}
		public double getAirtimePayoff() {
			return airtimePayoff;
		}
		public void setAirtimePayoff(double airtimePayoff) {
			this.airtimePayoff = airtimePayoff;
		}
		
	}
	
}
