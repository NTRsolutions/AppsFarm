package is.web.beans.offerRewardTypes;

import is.ejb.bl.business.CountryCode;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.dl.dao.DAOCountries;
import is.ejb.dl.dao.DAOCurrencyCode;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.CountryEntity;
import is.ejb.dl.entities.CurrencyCodeEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.users.LoginBean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@ManagedBean(name = "offerRewardTypesBean")
@SessionScoped
public class OfferRewardTypesBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 
	@Inject
	private Logger logger;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAODenominationModel daoDenominationModel;

	@Inject
	private DAOMobileApplicationType daoMobileApplicationType;
	
	@Inject
	private DAOCountries daoCountries;
	
	@Inject
	private SerDeCurrencyCode serDeCurrencyCode;
	@Inject
	private DAOCurrencyCode daoCurrencyCode;

	private List<RewardTypeEntity> listRewardTypes = new ArrayList<RewardTypeEntity>();

	private RewardTypeEntity editedRewardType = new RewardTypeEntity();

	private RewardTypeEntity createdRewardType = new RewardTypeEntity();

	private RewardTypeDataModelBean rewardTypeDataModelBean;

	private LoginBean loginBean;

	private RealmEntity realm = null;

	private String actionURL;

	private ImageBannerDataModelBean imageBannerDataModelBean;

	private List<ImageBannerEntity> rewardTypeImageBannerList;

	private String imageBannerURL;

	private String imageBannerAction;

	private List<CountryEntity> countries;
	
	private List<CurrencyCode> currencyCodes;
	
	public OfferRewardTypesBean() {
	}
	
	
	

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc,
				"#{loginBean}", LoginBean.class);
		realm = loginBean.getUser().getRealm();
		editedRewardType = new RewardTypeEntity();
		createdRewardType = new RewardTypeEntity();

		try {
			listRewardTypes = daoRewardType.findAllByRealmId(realm.getId());
			rewardTypeDataModelBean = new RewardTypeDataModelBean(
					listRewardTypes);
			//updateImageBannerDataTable(editedRewardType);
			countries = daoCountries.getAll();
			CurrencyCodeEntity currencyCodeEntity = daoCurrencyCode.findByRealmId(loginBean.getUser().getRealm().getId());
			CurrencyCodes currencyCodesObject = serDeCurrencyCode.deserialize(currencyCodeEntity.getSupportedCurrencies());
			currencyCodes = currencyCodesObject.getListCodes();
			
		} catch (Exception e) {
			logger.severe(e.toString());
			sendFacesMessage("Error", "Couldn't init reward type bean.", false);
		}
		refresh();
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			listRewardTypes = daoRewardType.findAllByRealmId(realm.getId());
			rewardTypeDataModelBean = new RewardTypeDataModelBean(
					listRewardTypes);
			RequestContext.getCurrentInstance().update(
					"tabView:idRewarTypeTable");
		} catch (Exception e) {
			logger.severe("Error: " + e.toString());
		}

	}

	public void updateRewardType() {
		try {

			if (editedRewardType.getName() == null
					|| editedRewardType.getName().length() == 0) {
				sendFacesMessage("Failed", "Please provide reward name", false);
				return;
			}

			MobileApplicationTypeEntity applicationType = daoMobileApplicationType
					.findByName(editedRewardType.getApplicationType());
			editedRewardType.setApplicationId(applicationType.getId());
			updateSelectedRewardType();
			editedRewardType = new RewardTypeEntity();
			refresh();

		} catch (Exception e) {
			e.printStackTrace();
			sendFacesMessage("Failed", e.toString(), false);
		} finally {
			RequestContext.getCurrentInstance().execute(
					"widgetRewardTypeEditDialog.hide()");
		}

	}

	public void deleteRewardType(RewardTypeEntity rewardType) {
		logger.info("deleting reward type: " + rewardType);
		try {

			if (daoDenominationModel.findByRewardTypeNameAndRealmId(
					rewardType.getName(), realm.getId()) != null
					&& daoDenominationModel.findByRewardTypeNameAndRealmId(
							rewardType.getName(), realm.getId()).size() > 0) {
				sendFacesMessage(
						"Error",
						"Unable to delete selected reward type as it is assigned to existing denomiantion model(s). Please unassign it before deleting",
						true);
			} else {
				rewardType = daoRewardType.findById(rewardType.getId());
				daoRewardType.delete(rewardType);
				sendFacesMessage("Success",
						"Successfully deleted selected reward type", true);
				refresh();
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			sendFacesMessage(
					"Error",
					"Error performing currency removal, error: "
							+ exc.toString(), false);
		}
	}

	public void setEditedRewardType(RewardTypeEntity rewardType) {
		this.editedRewardType = rewardType;
		//updateImageBannerDataTable(rewardType);
		RequestContext.getCurrentInstance().update("tabView:idEditRewardType");
	}

	public void addRewardType() {

		logger.info("adding new reward type: " + createdRewardType.getName());
		try {
			if (daoRewardType.findByRealmIdAndName(realm.getId(),
					createdRewardType.getName()) != null) {
				sendFacesMessage(
						"Warning",
						"Reward type with given name already exists! Aborting creation!",
						false);
			} else {
				createdRewardType.setRealm(realm);
				createdRewardType.setGenerationDate(new Timestamp(System
						.currentTimeMillis()));

				MobileApplicationTypeEntity applicationType = this.daoMobileApplicationType
						.findByName(createdRewardType.getApplicationType());
				createdRewardType.setApplicationId(applicationType.getId());

				daoRewardType.createOrUpdate(createdRewardType);
				createdRewardType = new RewardTypeEntity();
				sendFacesMessage("Success",
						"Reward type successfully created!", true);
				RequestContext.getCurrentInstance().execute(
						"widgetRewardTypeAddDialog.hide()");
				refresh();
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			sendFacesMessage("Error", "Error creating new reward type, error: "
					+ exc.toString(), false);
		}
	}

	private void updateSelectedRewardType() {
		this.daoRewardType.createOrUpdate(editedRewardType);
		sendFacesMessage("Success", "Reward type updated.", true);
	}

	private void updateImageBannerDataTable(RewardTypeEntity rewardType) {
		String imageBannerJsonContent = rewardType.getImageBannerContent();
		rewardTypeImageBannerList = deserializeImageBanners(imageBannerJsonContent);
		imageBannerDataModelBean = new ImageBannerDataModelBean(
				rewardTypeImageBannerList);
		RequestContext.getCurrentInstance()
				.update("tabView:idImageBannerTable");
	}

	private void sendFacesMessage(String title, String message,
			boolean isSuccess) {
		FacesMessage msg = null;
		if (isSuccess) {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, title, message);
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message);
		}

		FacesContext.getCurrentInstance().addMessage(null, msg);
		RequestContext.getCurrentInstance().update(
				"tabView:idSetupRewardTypesGrowl");
	}
	
	public List<SelectItem> getCountries(){
		try{
			List<SelectItem> countriesSelectionList = new ArrayList<SelectItem>();
			List<CountryEntity> countries = this.daoCountries.getAll();
			for (CountryEntity country: countries){
				countriesSelectionList.add(new SelectItem(country.getCode(),country.getCode()));
			}
			return countriesSelectionList;
		}catch (Exception exc){
			exc.printStackTrace();
			return new ArrayList<SelectItem>();
		}
	}
	

	public List<SelectItem> getApplicationTypeList() {
		try {
			List<MobileApplicationTypeEntity> applicationEntityList = daoMobileApplicationType
					.findAll();
			List<SelectItem> applicationList = new ArrayList<SelectItem>();
			for (MobileApplicationTypeEntity application : applicationEntityList) {
				applicationList.add(new SelectItem(application.getName(),
						application.getName()));
			}
			return applicationList;
		} catch (Exception exc) {
			exc.printStackTrace();
			return new ArrayList<SelectItem>();
		}
	}

	public List<SelectItem> getBannerActionList() {
		List<SelectItem> actionList = new ArrayList<SelectItem>();
		actionList.add(new SelectItem("None", "None"));
		actionList.add(new SelectItem("URL", "Open URL"));
		actionList.add(new SelectItem("Videos", "Open Videos"));
		actionList.add(new SelectItem("Chatz", "Open Chatz"));
		actionList.add(new SelectItem("Wallet", "Open Wallet"));
		actionList.add(new SelectItem("HotOffers","Open Hot Offers"));
		actionList.add(new SelectItem("Spinner","Open Spinner"));
		actionList.add(new SelectItem("Refer","Open Refer a Friend"));
		return actionList;
	}

	private String serializaeImageBanners(
			List<ImageBannerEntity> imageBannerList) {

		String content = new Gson().toJson(imageBannerList,
				new TypeToken<List<ImageBannerEntity>>() {
				}.getType());
		if (content == null || content.length() == 0) {
			content = "{}";
		}
		return content;
	}

	public List<ImageBannerEntity> deserializeImageBanners(String content) {
		List<ImageBannerEntity> imageBannerList = new Gson().fromJson(content,
				new TypeToken<List<ImageBannerEntity>>() {
				}.getType());
		if (imageBannerList == null) {
			imageBannerList = new ArrayList<ImageBannerEntity>();
		}
		return imageBannerList;
	}

	public void createImageBanner() {

		if (imageBannerURL == null || imageBannerURL.length() == 0) {
			sendFacesMessage("Error", "Please specify valid image banner url",
					false);
			return;
		}
		if (imageBannerAction != null
				&& imageBannerAction.toLowerCase().contains("url")
				&& (actionURL == null || actionURL.length() == 0)) {
			sendFacesMessage("Error",
					"Please specify url address for banner click", false);
			return;
		}

		ImageBannerEntity imageBanner = new ImageBannerEntity();
		imageBanner.setId(getUniqueID());
		if (imageBannerAction.toLowerCase().contains("url")) {
			imageBannerAction = imageBannerAction + ":" + actionURL;
		}
		imageBanner.setImageBannerAction(imageBannerAction);
		imageBanner.setImageBannerURL(imageBannerURL);

		rewardTypeImageBannerList.add(imageBanner);
		String jsonContent = this
				.serializaeImageBanners(rewardTypeImageBannerList);
		editedRewardType.setImageBannerContent(jsonContent);
		updateSelectedRewardType();
		//updateImageBannerDataTable(editedRewardType);
		RequestContext.getCurrentInstance().execute(
				"widgetAddImageBannerDialog.hide()");
		clearInputs();

	}

	private void clearInputs() {
		imageBannerURL = "";
		imageBannerAction = "";
		actionURL = "";
		RequestContext.getCurrentInstance().update(
				"tabView:idWidgetAddImageBannerDialog");
	}

	public void deleteImageBanner(ImageBannerEntity imageBanner) {
		for (ImageBannerEntity imageBannerItem : rewardTypeImageBannerList) {
			if (imageBannerItem.getId() == imageBanner.getId()) {
				rewardTypeImageBannerList.remove(imageBannerItem);
				break;
			}
		}
		String jsonContent = this
				.serializaeImageBanners(rewardTypeImageBannerList);
		editedRewardType.setImageBannerContent(jsonContent);
		updateSelectedRewardType();
		updateImageBannerDataTable(editedRewardType);
		sendFacesMessage("Success", "Removed image banner", true);
	}

	private String getUniqueID() {
		final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}

	public String getActionURL() {
		return actionURL;
	}

	public void setActionURL(String actionURL) {
		this.actionURL = actionURL;
	}

	public ImageBannerDataModelBean getImageBannerDataModelBean() {
		return imageBannerDataModelBean;
	}

	public void setImageBannerDataModelBean(
			ImageBannerDataModelBean imageBannerDataModelBean) {
		this.imageBannerDataModelBean = imageBannerDataModelBean;
	}

	public String getImageBannerURL() {
		return imageBannerURL;
	}

	public void setImageBannerURL(String imageBannerURL) {
		this.imageBannerURL = imageBannerURL;
	}

	public String getImageBannerAction() {
		return imageBannerAction;
	}

	public void setImageBannerAction(String imageBannerAction) {
		this.imageBannerAction = imageBannerAction;
	}

	public RewardTypeEntity getEditedRewardType() {
		return editedRewardType;
	}

	public RewardTypeEntity getCreatedRewardType() {
		return createdRewardType;
	}

	public void setCreatedRewardType(RewardTypeEntity createdRewardType) {
		this.createdRewardType = createdRewardType;
	}

	public RewardTypeDataModelBean getRewardTypeDataModelBean() {
		return rewardTypeDataModelBean;
	}

	public void setRewardTypeDataModelBean(
			RewardTypeDataModelBean rewardTypeDataModelBean) {
		this.rewardTypeDataModelBean = rewardTypeDataModelBean;
	}
	
	public List<CurrencyCode> getCurrencyCodes() {
		return currencyCodes;
	}

	public void setCurrencyCodes(List<CurrencyCode> currencyCodes) {
		this.currencyCodes = currencyCodes;
	}

	
	
}
