package is.web.beans.countries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import is.ejb.dl.dao.DAOCountries;
import is.ejb.dl.entities.CountryEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.beans.users.LoginBean;

@ManagedBean(name = "countriesBean")
@SessionScoped
public class CountriesBean {

	@Inject
	private Logger logger;

	private LoginBean loginBean;

	@Inject
	private DAOCountries daoCountries;

	private RealmEntity realm;

	private List<String> codes = new ArrayList<>();
	private List<CountryEntity> countries = new ArrayList<>();

	private String newCode = null;
	private String newName = null;

	private CountryEntity editingCountry;

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
		realm = loginBean.getUser().getRealm();

		refresh();
	}

	public void addCountry() {
		if(!validCountryCode(newCode)) {
			return;
		}

		CountryEntity country = new CountryEntity();
		country.setGenerationTime(new Timestamp(System.currentTimeMillis()));
		country.setCode(newCode);
		country.setName(newName);
		country.setRealm(realm);

		daoCountries.createOrUpdate(country);
		refresh();
	}

	public void editCountry() {
		if(!validCountryCode(editingCountry.getCode())) {
			return;
		}
		
		daoCountries.createOrUpdate(editingCountry);
		refresh();
	}

	public void deleteCountry(CountryEntity country) {
		daoCountries.delete(country);
		refresh();
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");

			newCode = null;
			newName = null;
			countries = daoCountries.getAll();
			
			editingCountry = new CountryEntity();
			editingCountry.setCode("");
			editingCountry.setName("");

			RequestContext.getCurrentInstance().update("tabView:idCountriesTable");
			RequestContext.getCurrentInstance().update("tabView:inNewCode");
			RequestContext.getCurrentInstance().update("tabView:inNewName");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}
	
	private boolean validCountryCode(String code) {
		if (code == null || code.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Warning", "Country code is empty!"));
			RequestContext.getCurrentInstance().update("tabView:idCountriesGrowl");
			return false;
		}
		if (daoCountries.findByCode(code) != null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Warning", "Country code: " + code + " already defined!"));
			RequestContext.getCurrentInstance().update("tabView:idCountriesGrowl");
			return false;
		}
		
		return true;
	}

	public List<String> getCodes() {
		return codes;
	}

	public void setCodes(List<String> codes) {
		this.codes = codes;
	}

	public List<CountryEntity> getCountries() {
		return countries;
	}

	public void setCountries(List<CountryEntity> countries) {
		this.countries = countries;
	}

	public String getNewCode() {
		return newCode;
	}

	public void setNewCode(String newCode) {
		this.newCode = newCode;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public CountryEntity getEditingCountry() {
		return editingCountry;
	}

	public void setEditingCountry(CountryEntity editingCountry) {
		this.editingCountry = editingCountry;
	}

}
