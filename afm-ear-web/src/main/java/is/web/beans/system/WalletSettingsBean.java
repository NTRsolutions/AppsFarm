package is.web.beans.system;

import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOWalletPayoutCarrier;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.web.beans.denomination.DenominationDataModelBean;
import is.web.beans.denomination.DenominationTableDataModelBean;
import is.web.beans.users.LoginBean;

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

@ManagedBean(name = "walletSettingsBean")
@SessionScoped
public class WalletSettingsBean {

	@Inject
	private Logger logger;

	@Inject
	private DAOWalletPayoutCarrier daoWallet;

	private LoginBean loginBean;

	@Inject
	private DAORewardType daoRewardType;
	private List<RewardTypeEntity> rewardTypeList;

	private List<WalletPayoutCarrierEntity> walletPayoutCarriers = new ArrayList<WalletPayoutCarrierEntity>();
	private WalletSettingsTableDataModelBean walletSettingsTableDataModelBean;
	private WalletPayoutCarrierEntity editModel = new WalletPayoutCarrierEntity();
	private WalletPayoutCarrierEntity createModel = new WalletPayoutCarrierEntity();

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		try {
			walletPayoutCarriers = daoWallet.findAll();

		} catch (Exception e) {
			e.printStackTrace();
		}
		walletSettingsTableDataModelBean = new WalletSettingsTableDataModelBean(walletPayoutCarriers);

		loadRewardTypes();
		refresh();

	}

	public void refresh() {

		loadRewardTypes();
		try {
			logger.info("refreshing wallet settings bean...");
			walletPayoutCarriers = daoWallet.findAll();
			walletSettingsTableDataModelBean = new WalletSettingsTableDataModelBean(walletPayoutCarriers);
			// refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idWalletPayoutCarriers");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public WalletSettingsTableDataModelBean getWalletSettingsTableDataModelBean() {
		return walletSettingsTableDataModelBean;
	}

	public void setWalletSettingsTableDataModelBean(WalletSettingsTableDataModelBean walletSettingsTableDataModelBean) {
		this.walletSettingsTableDataModelBean = walletSettingsTableDataModelBean;
	}

	public void clearTable() throws Exception {
		logger.info("clearing denomination table rows as create button was clicked...");
		RequestContext.getCurrentInstance().update("tabView:idWalletPayoutCarriers");

	}

	public WalletPayoutCarrierEntity getEditModel() {
		return editModel;
	}

	public void setEditModel(WalletPayoutCarrierEntity editModel) {
		this.editModel = editModel;
	}

	public WalletPayoutCarrierEntity getCreateModel() {
		return createModel;
	}

	public void setCreateModel(WalletPayoutCarrierEntity createModel) {
		this.createModel = createModel;
	}

	public void create() {
		try {


			if (createModel.getName().trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide carrier name."));
				RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
				return;
			}

			if (String.valueOf(createModel.getRewardTypeId()).trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward type."));
				RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
				return;
			}
			createModel.setRealmId(loginBean.getUser().getRealm().getId());
			daoWallet.createOrUpdate(createModel);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Wallet payout carrier created."));
			RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateWalletPayoutCarrier.hide()");
			createModel = new WalletPayoutCarrierEntity();
			refresh();

		} catch (Exception e) {
			System.out.println(e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateWalletPayoutCarrier.hide()");

		}
	}

	public void setEditingModel(WalletPayoutCarrierEntity model) {
		this.editModel = model;
	}

	public void edit() {
		try {
			

			if (editModel.getName().trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide carrier name."));
				RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
				return;
			}

			if (String.valueOf(editModel.getRewardTypeId()).trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward type."));
				RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
				return;
			}

			daoWallet.createOrUpdate(editModel);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Wallet payout carrier created."));
			RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
			RequestContext.getCurrentInstance().execute("widgetEditWalletPayoutCarrier.hide()");
			editModel = new WalletPayoutCarrierEntity();
			refresh();

		} catch (Exception e) {
			System.out.println(e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
			RequestContext.getCurrentInstance().execute("widgetEditWalletPayoutCarrier.hide()");

		}

	}

	public void delete() {

		daoWallet.delete(editModel);
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Wallet payout carrier deleted."));
		RequestContext.getCurrentInstance().update("tabView:idWalletSettingsGrowl");
		RequestContext.getCurrentInstance().execute("widgetEditWalletPayoutCarrier.hide()");
		editModel = new WalletPayoutCarrierEntity();
		refresh();
	}

	public void loadRewardTypes() {
		try {
			this.rewardTypeList = daoRewardType.findAll();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<RewardTypeEntity> getRewardType() {
		return rewardTypeList;
	}
	
	
	public String getRewardTypeName(int id){
		for (RewardTypeEntity rewardType : rewardTypeList){
			if (rewardType.getId() == id){
				return rewardType.getName();
			}
		}
		return "";
	}

}
