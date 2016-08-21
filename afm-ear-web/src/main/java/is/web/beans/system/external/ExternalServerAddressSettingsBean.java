package is.web.beans.system.external;

import is.ejb.bl.external.ExternalServerManager;
import is.ejb.bl.external.ExternalServerType;
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.entities.ExternalServerAddressEntity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

@ManagedBean(name = "externalServerAddressSettingsBean")
@SessionScoped
public class ExternalServerAddressSettingsBean {

	@Inject
	private DAOExternalServerAddress daoExternalServerAddress;
	private ExternalServerAddressTableDataModelBean externalServerAddressTableDataModelBean;
	private ExternalServerAddressEntity createModel, editModel;
	@Inject
	private ExternalServerManager externalServerManager;

	@PostConstruct
	public void init() {
		try {
			createModel = new ExternalServerAddressEntity();
			editModel = new ExternalServerAddressEntity();
			List<ExternalServerAddressEntity> externalServerAddressList = daoExternalServerAddress.findAll();
			externalServerAddressTableDataModelBean = new ExternalServerAddressTableDataModelBean(
					externalServerAddressList);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void editIpAddress() {

		try {

			if (editModel.getIpContent().trim().length() == 0) {
				pushFacesMessage(true, "Failed", "Please provide ip address list.");
				return;
			}

			externalServerManager.insertOrUpdateExternalServerAddress(editModel);
			pushFacesMessage(false, "Success", "External server address updated.");
			RequestContext.getCurrentInstance().execute("widgetEditExternalServerAddress.hide()");
			editModel = new ExternalServerAddressEntity();
			refresh();
			update();

		} catch (Exception e) {
			e.printStackTrace();
			pushFacesMessage(true, "Failed", e.toString());
			RequestContext.getCurrentInstance().execute("widgetEditExternalServerAddress.hide()");

		}
	}

	public void createIpAddress() {

		try {

			if (!isExternalServerTypeInList(createModel.getExternalServerType())) {
				pushFacesMessage(true, "Failed",
						"Provider type: " + createModel.getExternalServerType() + " is already in list.");
				return;
			}

			if (createModel.getIpContent().trim().length() == 0) {
				pushFacesMessage(true, "Failed", "Please provide ip address list.");
				return;
			}

			externalServerManager.insertOrUpdateExternalServerAddress(createModel);
			pushFacesMessage(false, "Success", "External server address created.");
			RequestContext.getCurrentInstance().execute("widgetCreateExternalServerAddress.hide()");
			createModel = new ExternalServerAddressEntity();
			refresh();
			update();
		} catch (Exception e) {
			e.printStackTrace();
			pushFacesMessage(true, "Failed", e.toString());
			RequestContext.getCurrentInstance().execute("widgetCreateExternalServerAddress.hide()");

		}
	}

	private boolean isExternalServerTypeInList(String externalType) {
		ExternalServerType type = getExternalServerTypeFromString(externalType);
		ExternalServerAddressEntity externalServerAddress = externalServerManager.getExternalServerAddressForType(type);
		if (externalServerAddress == null) {
			return true;
		} else {
			return false;
		}
	}

	private ExternalServerType getExternalServerTypeFromString(String externalServerTypeString) {
		return ExternalServerType.valueOf(externalServerTypeString);
	}

	public void deleteIpAddress() {

		externalServerManager.deleteExternalServerAddress(editModel);
		pushFacesMessage(false, "Success", "External server address deleted.");
		RequestContext.getCurrentInstance().execute("widgetEditExternalServerAddress.hide()");
		editModel = new ExternalServerAddressEntity();
		refresh();
		update();
	}

	public void refresh() {
		try {
			List<ExternalServerAddressEntity> externalServerAddressList = daoExternalServerAddress.findAll();
			externalServerAddressTableDataModelBean = new ExternalServerAddressTableDataModelBean(
					externalServerAddressList);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void pushFacesMessage(boolean failed, String title, String message) {
		if (failed) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
		}
		RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
	}

	public void update() {
		pushFacesMessage(false,"Success", "Data successfully updated");
		RequestContext.getCurrentInstance().update("tabView:idExternalServerSettings");
	}

	public List<SelectItem> getExternalServerTypes() {
		List<SelectItem> externalServerTypeList = new ArrayList<SelectItem>();
		for (ExternalServerType type : ExternalServerType.values()) {
			externalServerTypeList.add(new SelectItem(type.toString(), type.toString()));
		}
		return externalServerTypeList;
	}

	public ExternalServerAddressTableDataModelBean getExternalServerAddressTableDataModelBean() {
		return externalServerAddressTableDataModelBean;
	}

	public void setExternalServerAddressTableDataModelBean(
			ExternalServerAddressTableDataModelBean externalServerAddressTableDataModelBean) {
		this.externalServerAddressTableDataModelBean = externalServerAddressTableDataModelBean;
	}

	public ExternalServerAddressEntity getCreateModel() {
		return createModel;
	}

	public void setCreateModel(ExternalServerAddressEntity createModel) {
		this.createModel = createModel;
	}

	public ExternalServerAddressEntity getEditModel() {
		return editModel;
	}

	public void setEditModel(ExternalServerAddressEntity editModel) {
		this.editModel = editModel;
	}

}
