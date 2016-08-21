package is.web.beans.applications;

import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.WalletPayoutCarrierEntity;
import is.web.beans.system.WalletSettingsTableDataModelBean;
import is.web.beans.users.LoginBean;

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

@ManagedBean(name = "applicationRewardBean")
@SessionScoped
public class ApplicationRewardBean {
	@Inject
	private LoginBean loginBean;

	private RealmEntity realm = null;

	@Inject
	private DAOApplicationReward daoApplicationReward;

	@Inject
	DAOMobileApplicationType daoApplicationType;

	private ApplicationRewardTableDataModelBean applicationRewardTableDataModel;

	private ApplicationRewardEntity createModel;
	private ApplicationRewardEntity editModel;

	private List<ApplicationRewardEntity> applicationRewardEntityList;
	private List<MobileApplicationTypeEntity> mobileApplicationTypeEntityList;

	private List<SelectItem> mobileAppSelectItemList;

	@Inject
	private DAOCurrencyCode daoCurrencyCode;

	@Inject
	private DAORewardType daoRewardType;
	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;

	private List<SelectItem> currencyCodeSelectItemList;

	private List<SelectItem> rewardTypesList;

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		editModel = new ApplicationRewardEntity();
		createModel = new ApplicationRewardEntity();
		try {
			List<RewardTypeEntity> rewardTypeEntityList = daoRewardType.findAll();
			rewardTypesList = new ArrayList<SelectItem>();
			for (RewardTypeEntity ent : rewardTypeEntityList) {
				rewardTypesList.add(new SelectItem(ent.getName(), ent.getName()));
			}
			applicationRewardEntityList = daoApplicationReward.findAll();
			this.applicationRewardTableDataModel = new ApplicationRewardTableDataModelBean(applicationRewardEntityList);
			mobileApplicationTypeEntityList = daoApplicationType.findAll();
			
			mobileAppSelectItemList = new ArrayList<SelectItem>();
			for (MobileApplicationTypeEntity ent : mobileApplicationTypeEntityList) {
				mobileAppSelectItemList.add(new SelectItem(ent.getName(), ent.getName()));
			}

			CurrencyCodeEntity currencyCodeEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());

			CurrencyCodes currencyCodes = serDeCurrencyCode.deserialize(currencyCodeEntity.getSupportedCurrencies());
			List<CurrencyCode> currencyCodeList = currencyCodes.getListCodes();
			currencyCodeSelectItemList = new ArrayList<SelectItem>();
			for (CurrencyCode code : currencyCodeList) {
				currencyCodeSelectItemList.add(new SelectItem(code.getCode(), code.getCode()));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ApplicationRewardTableDataModelBean getApplicationRewardTableDataModel() {
		return applicationRewardTableDataModel;
	}

	public void setApplicationRewardTableDataModel(ApplicationRewardTableDataModelBean applicationRewardTableDataModel) {
		this.applicationRewardTableDataModel = applicationRewardTableDataModel;
	}

	public void refresh() {

		try {

			applicationRewardEntityList = daoApplicationReward.findAll();
			this.applicationRewardTableDataModel = new ApplicationRewardTableDataModelBean(applicationRewardEntityList);
			mobileApplicationTypeEntityList = daoApplicationType.findAll();
			//System.out.println(mobileApplicationTypeEntityList.size() + "<<<SIZE !!!!!");
			mobileAppSelectItemList = new ArrayList<SelectItem>();
			for (MobileApplicationTypeEntity ent : mobileApplicationTypeEntityList) {
				System.out.println(ent);
				mobileAppSelectItemList.add(new SelectItem(ent.getName(), ent.getName()));
			}

			CurrencyCodeEntity currencyCodeEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());

			CurrencyCodes currencyCodes = serDeCurrencyCode.deserialize(currencyCodeEntity.getSupportedCurrencies());
			List<CurrencyCode> currencyCodeList = currencyCodes.getListCodes();

			currencyCodeSelectItemList = new ArrayList<SelectItem>();
			for (CurrencyCode code : currencyCodeList) {

				currencyCodeSelectItemList.add(new SelectItem(code.getCode(), code.getCode()));
			}

			// refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public ApplicationRewardEntity getCreateModel() {
		return createModel;
	}

	public void setCreateModel(ApplicationRewardEntity createModel) {
		this.createModel = createModel;
	}

	public ApplicationRewardEntity getEditModel() {
		return editModel;
	}

	public void setEditModel(ApplicationRewardEntity editModel) {
		this.editModel = editModel;
	}

	public void edit() {
		try {

			if (editModel.getRewardName().trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward name."));
				RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
				return;
			}

			if (editModel.getRewardValue() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward value."));
				RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
				return;
			}

			MobileApplicationTypeEntity applicationTypeEntity = daoApplicationType.findByName(editModel.getApplicationName());
			editModel.setApplicationId(applicationTypeEntity.getId());
			editModel.setRealmId(loginBean.getUser().getRealm().getId());

			daoApplicationReward.createOrUpdate(editModel);
			editModel = new ApplicationRewardEntity();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Application reward carrier created."));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationReward.hide()");

			refresh();
		} catch (Exception exc) {
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", exc.getMessage()));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetEditApplicationReward.hide()");
		}

	}

	public void create() {
		try {

			if (createModel.getRewardName().trim().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward name."));
				RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
				return;
			}

			if (createModel.getRewardValue() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide reward value."));
				RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
				return;
			}

			//System.out.println(createModel);

			MobileApplicationTypeEntity applicationTypeEntity = daoApplicationType.findByName(createModel.getApplicationName());

			//System.out.println("***********");
			//System.out.println("***********");
			//System.out.println("***********");
		

			createModel.setApplicationId(applicationTypeEntity.getId());
			createModel.setRealmId(loginBean.getUser().getRealm().getId());

			daoApplicationReward.create(createModel);
			createModel = new ApplicationRewardEntity();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Application reward carrier created."));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationReward.hide()");

			refresh();
		} catch (Exception exc) {
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", exc.getMessage()));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationReward.hide()");
		}
	}

	public void delete() {
		try {

			daoApplicationReward.delete(editModel);
			editModel = new ApplicationRewardEntity();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Application reward  deleted."));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationReward.hide()");

			refresh();
		} catch (Exception exc) {
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", exc.getMessage()));
			RequestContext.getCurrentInstance().update("tabView:idApplicationReward");
			RequestContext.getCurrentInstance().execute("widgetEditApplicationReward.hide()");
		}
	}

	public List<MobileApplicationTypeEntity> getMobileApplicationTypeEntityList() {
		return mobileApplicationTypeEntityList;
	}

	public void setMobileApplicationTypeEntityList(List<MobileApplicationTypeEntity> mobileApplicationTypeEntityList) {
		this.mobileApplicationTypeEntityList = mobileApplicationTypeEntityList;
	}

	public List<SelectItem> getMobileAppSelectItemList() {
		return mobileAppSelectItemList;
	}

	public void setMobileAppSelectItemList(List<SelectItem> mobileAppSelectItemList) {
		this.mobileAppSelectItemList = mobileAppSelectItemList;
	}

	public List<SelectItem> getCurrencyCodeSelectItemList() {
		return currencyCodeSelectItemList;
	}

	public void setCurrencyCodeSelectItemList(List<SelectItem> currencyCodeSelectItemList) {
		this.currencyCodeSelectItemList = currencyCodeSelectItemList;
	}

	public List<SelectItem> getRewardTypesList() {
		return rewardTypesList;
	}

	public void setRewardTypesList(List<SelectItem> rewardTypesList) {
		this.rewardTypesList = rewardTypesList;
	}

}
