package is.web.services;

import is.ejb.bl.uiStateManager.UIStateHolder;
import is.ejb.dl.entities.AppUserEntity;

import java.sql.Timestamp;
import java.util.ArrayList;

public class SnapdealGetCategories {
	private String status;
	private String code;
	private ArrayList<String> categoriesList = new ArrayList<String>();
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ArrayList<String> getCategoriesList() {
		return categoriesList;
	}
	public void setCategoriesList(ArrayList<String> categoriesList) {
		this.categoriesList = categoriesList;
	}
	
}
