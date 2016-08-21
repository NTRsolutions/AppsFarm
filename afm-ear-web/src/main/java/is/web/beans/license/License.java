package is.web.beans.license;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class License {
	//license lease dates
	private Date dateStart;
	private Date dateEnd;

	//customer details
	private String customerName;
	private String customerAddress;

	//system configuration
	private int numberOfManagedDevices;
	private int numberOfServerInstances;
	private ArrayList<String> modulesSet = new ArrayList<String>();//if module exists in the set- then AccessFilter will render associated UI content
	
	public Date getDateStart() {
		return dateStart;
	}
	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}
	public Date getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerAddress() {
		return customerAddress;
	}
	public void setCustomerAddress(String customerAddress) {
		this.customerAddress = customerAddress;
	}
	public int getNumberOfManagedDevices() {
		return numberOfManagedDevices;
	}
	public void setNumberOfManagedDevices(int numberOfManagedDevices) {
		this.numberOfManagedDevices = numberOfManagedDevices;
	}
	public ArrayList<String> getModulesSet() {
		return modulesSet;
	}
	public void setModulesSet(ArrayList<String> modulesSet) {
		this.modulesSet = modulesSet;
	}
	public int getNumberOfServerInstances() {
		return numberOfServerInstances;
	}
	public void setNumberOfServerInstances(int numberOfServerInstances) {
		this.numberOfServerInstances = numberOfServerInstances;
	}
	
}
