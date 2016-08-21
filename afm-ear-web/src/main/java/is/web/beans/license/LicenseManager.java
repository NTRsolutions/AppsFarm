package is.web.beans.license;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;

public class LicenseManager {

//	@Inject
//	private Logger logger;

//	@Inject
//	private LicenseSerDe licSerDe;

	private LicenseSerDe licSerDe = new LicenseSerDe();
	public static String licenseFileName = "license.txt";
	
	public static void main(String[] args) {
		new LicenseManager();
	}
	
	public LicenseManager() {
		//generateLicense();
	}
	
	//accepts license configuration and generates corresponding license containing readable content and serialised license object
	public void generateLicense() {
		//license lease dates
		Date licenseStartDate = new Date(System.currentTimeMillis());
		Date licenseEndDate = new Date(System.currentTimeMillis());
		licenseEndDate.setYear(114);
		licenseEndDate.setMonth(12);

		//customer details
		String customerName = "Marstone's";
		String customerAddress = "";

		//license specifics
		int numberOfManagedDevices = 100;
		int numberOfServerInstances = 1;
		ArrayList<String> modulesSet = new ArrayList<String>();//if module exists in the set- then AccessFilter will render associated UI content
		modulesSet.add(LicenseModules.simpleReporting.toString());
		modulesSet.add(LicenseModules.backups.toString());
		modulesSet.add(LicenseModules.tracking.toString());
		modulesSet.add(LicenseModules.upgrades.toString());
		modulesSet.add(LicenseModules.configuration.toString());
		modulesSet.add(LicenseModules.geolocation.toString());
		modulesSet.add(LicenseModules.monitoring.toString());
		modulesSet.add(LicenseModules.advancedReporting.toString());
		modulesSet.add(LicenseModules.forecasting.toString());
		
		//create license
		License lic = new License();
		lic.setDateStart(licenseStartDate);
		lic.setDateEnd(licenseEndDate);
		lic.setCustomerName(customerName);
		lic.setCustomerAddress(customerAddress);
		lic.setNumberOfManagedDevices(numberOfManagedDevices);
		lic.setNumberOfServerInstances(numberOfServerInstances);
		lic.setModulesSet(modulesSet);
		
		//serialise license into json 
		try {
			String strLic = licSerDe.serialize(lic);
			System.out.println("serialised content: ");
			System.out.println(strLic);
			
			//encrypt data on your side using BASE64
			byte[] licenseAsBytes = Base64.encodeBase64(strLic.getBytes());
			String encodedLicenseJson = new String(licenseAsBytes);
			System.out.println("Ecncoded value is " + encodedLicenseJson);

			//prepare license content to be stored in file
			String strOutput = "";
			//strOutput = strOutput + "License information" + " \n";
			strOutput = strOutput + "License holder name: "+lic.getCustomerName() + " \n";
			strOutput = strOutput + "License holder address: "+lic.getCustomerAddress() + " \n";
			strOutput = strOutput + "License start date: "+lic.getDateStart().toString() + " \n";
			strOutput = strOutput + "License end date:   "+lic.getDateEnd().toString() + " \n";
			strOutput = strOutput + "Managed devices limit (per server instance): "+lic.getNumberOfManagedDevices() + " \n";
			strOutput = strOutput + "Server instances limit: "+lic.getNumberOfServerInstances() + " \n";
			strOutput = strOutput + "Licensed software modules: " + " \n";
			if(lic.getModulesSet().contains(LicenseModules.backups.toString())) {
				strOutput = strOutput + "-backups" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.geolocation.toString())) {
				strOutput = strOutput + "-geolocation" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.tracking.toString())) {
				strOutput = strOutput + "-tracking" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.simpleReporting.toString())) {
				strOutput = strOutput + "-simpleReporting" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.configuration.toString())) {
				strOutput = strOutput + "-configuration" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.upgrades.toString())) {
				strOutput = strOutput + "-upgrades" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.monitoring.toString())) {
				strOutput = strOutput + "-monitoring" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.advancedReporting.toString())) {
				strOutput = strOutput + "-advancedReporting" + " \n";
			}
			if(lic.getModulesSet().contains(LicenseModules.forecasting.toString())) {
				strOutput = strOutput + "-forecasting" + " \n";
			}
		
			strOutput = strOutput + " \n";
			strOutput = strOutput + "<key>" + encodedLicenseJson + "</key>" +" \n";
			System.out.println(strOutput);
			
			//store the license in file 
			//create folder insided the app to which it will be saved / and read from once user uploads the license
			String licenseFilePath = "home"+File.separator+"mzj"+File.separator+"tmp";
//			String licenseFilePath = "home"+File.separator+"balrog"+File.separator+"tmp";
			storeLicenseInFile(strOutput, licenseFileName, licenseFilePath);

			//reload the license from file and recreate object (use folder within app for this)
			String strLicenseFileContent = readLicenseFromFile(licenseFileName, licenseFilePath);
			License decodedLicense = decodeLicense(strLicenseFileContent);
			//System.out.println("Decoded license content: "+decodedLicense.getCustomerName()+" "+decodedLicense.getNumberOfManagedDevices()+" "+decodedLicense.getModulesSet().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void storeLicenseInFile(String strFileContent, String fileName, String filePath) {
        try{
        	//String outptuFilePath = new File(".").getAbsolutePath()+File.separator+fileName+File.separator+fileName;
        	String outptuFilePath = File.separator+filePath+File.separator+fileName;
        	System.out.println("storing file in output path: "+outptuFilePath);
        	FileWriter fstream = new FileWriter(outptuFilePath,false);
        	BufferedWriter out = new BufferedWriter(fstream);
        	out.write(strFileContent);
        	out.close();
        } catch(Exception exc) {
        	exc.printStackTrace();
        }
	}
	
	public String readLicenseFromFile(String fileName, String filePath) {
		String strFileContent = null;
		String strFilePathToReadFrom = File.separator+filePath+File.separator+fileName;
		System.out.println("reading license file from path: "+strFilePathToReadFrom);
		try(BufferedReader br = new BufferedReader(new FileReader(strFilePathToReadFrom))) {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        strFileContent = sb.toString();
		} catch(Exception exc) {
			exc.printStackTrace();
		}
        return strFileContent;
	}

	public License decodeLicense(String strLicenseContent) throws Exception {
		License decodedLicense = null;
		
		//first extract only license content
		String strKey = strLicenseContent.substring(strLicenseContent.indexOf("<key>")+5, strLicenseContent.indexOf("</key>"));
		System.out.println("got encoded license key:");
		System.out.println(strKey);
		
		// Decrypt data on other side, by processing encoded data
		byte[] encodedLicenseBytes = strKey.getBytes();
		byte[] decodedLiceseBytes= Base64.decodeBase64(encodedLicenseBytes);
		String decodedLicenseJson = new String(decodedLiceseBytes);
		//System.out.println("Decoded value is " + decodedLicenseJson);
		//decode into object
		decodedLicense = licSerDe.deserialize(decodedLicenseJson);
		
		return decodedLicense;
	}

	public long getDaysToExpire(License lic) {
		//Date startDate = lic.getDateStart();
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = lic.getDateEnd();
		long diff = endDate.getTime() - startDate.getTime();
		//return 0;
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
}
