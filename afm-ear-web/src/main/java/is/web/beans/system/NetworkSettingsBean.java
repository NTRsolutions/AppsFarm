package is.web.beans.system;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.bl.external.ExternalServerManager;
import is.ejb.bl.external.ExternalServerType;
import is.ejb.bl.offerFilter.CurrencyCode;
import is.ejb.bl.offerFilter.CurrencyCodes;
import is.ejb.bl.offerFilter.SerDeCurrencyCode;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.dl.dao.DAOApplicationReward;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.dao.DAOLicense;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.dao.DAOMonitoringSetup;
import is.ejb.dl.dao.DAOOfferFilter;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AFAConfiguration;
import is.ejb.dl.entities.ApplicationRewardEntity;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.ExternalServerAddressEntity;
import is.ejb.dl.entities.LicenseEntity;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.ejb.dl.entities.MonitoringSetupEntity;
import is.ejb.dl.entities.OfferFilterEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.web.beans.denomination.DenominationDataModelBean;
import is.web.beans.denomination.DenominationTableDataModelBean;
import is.web.beans.license.License;
import is.web.beans.license.LicenseManager;
import is.web.beans.offerRewardTypes.RewardTypeDataModelBean;
import is.web.beans.tab.SentinelTabBean;
import is.web.beans.tab.SentinelTabs;
import is.web.beans.tab.SentinelTabBean.SingleTabBean;
import is.web.beans.users.LoginBean;
import is.web.geo.GeoLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.print.attribute.standard.Severity;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.UploadedFile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@ManagedBean(name = "networkSettingsBean")
@SessionScoped
public class NetworkSettingsBean implements Serializable {

	@Inject
	Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEvent daoUserEvent;

	private RealmEntity realm = null;

	@Inject
	private DAOMonitoringSetup daoMonitoringSetup;
	MonitoringSetupEntity monitoringSetup = null;
	private LoginBean loginBean = null;

	private String apiKey = null;
	private String networkId = null;
	private int monitoringInterval = 0;

	@Inject
	DAORewardType daoRewardType;
	private List<RewardTypeEntity> rewardTypeEntityList;
	private RewardTypeDataModelBean rewardTypeDataModelBean;

	// @Inject
	// private DAOExternalServerAddress daoExternalServerAddress;

	@Inject
	private ExternalServerManager externalServerManager;

	@Inject
	private DAOMobileApplicationType daoMobileApplicationType;

	List<MobileApplicationTypeEntity> mobileApplicationList;
	private MobileApplicationTypeTableDataModelBean mobileApplicationTableDataModelBean;
	private MobileApplicationTypeEntity mobileApplicationTypeEditModel = new MobileApplicationTypeEntity();
	private MobileApplicationTypeEntity mobileApplicationTypeCreateModel = new MobileApplicationTypeEntity();

	@Inject
	private DAOApplicationReward daoApplicationReward;

	private ReferralRewardTableDataModelBean referralRewardTableDataModel;
	private RewardTypeEntity referralEditModel = new RewardTypeEntity();

	private Date reportStartDate;
	private int reportOffset;
	private String reportRewardType = "AirRewardz-India";
	private String reportResult = "";

	public NetworkSettingsBean() {
	}

	private AFAConfiguration afaConfiguration;
	private String snapdealCategoryName;
	private List<String> snapdealCategories;

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
		networkId = loginBean.getUser().getRealm().getName();
		apiKey = loginBean.getUser().getRealm().getApiKey();
		realm = loginBean.getUser().getRealm();

		try {

			rewardTypeEntityList = daoRewardType.findAll();
			rewardTypeDataModelBean = new RewardTypeDataModelBean(rewardTypeEntityList);

			referralRewardTableDataModel = new ReferralRewardTableDataModelBean(rewardTypeEntityList);

			mobileApplicationList = daoMobileApplicationType.findAll();
			mobileApplicationTableDataModelBean = new MobileApplicationTypeTableDataModelBean(mobileApplicationList);

			monitoringSetup = daoMonitoringSetup.findByRealmId(realm.getId());
			if (monitoringSetup == null) {
				monitoringSetup = new MonitoringSetupEntity();
				monitoringSetup.setRealm(realm);
				monitoringSetup = daoMonitoringSetup.createOrUpdate(monitoringSetup);
			}

			afaConfiguration = getAFAConfigurationFromString(realm.getAFAConfiguration());
			if (afaConfiguration == null) {
				afaConfiguration = new AFAConfiguration();
			}

			laodSnapdealCategories();
		} catch (Exception e) {
			logger.severe(e.toString());
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Error", "Unable to retrieve monitoring setup: " + e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		}
		monitoringInterval = Application.getMonitoringIntervals();

	}

	private AFAConfiguration getAFAConfigurationFromString(String config) {
		Gson gson = new Gson();
		AFAConfiguration afaConfiguration = gson.fromJson(config, AFAConfiguration.class);

		return afaConfiguration;
	}

	private String getStringFromAFAConfiguration(AFAConfiguration afaConfiguration) {
		Gson gson = new Gson();
		String json = gson.toJson(afaConfiguration);
		logger.info(json);
		return json;
	}

	public void refresh() {
		try {
			rewardTypeEntityList = daoRewardType.findAll();
			rewardTypeDataModelBean = new RewardTypeDataModelBean(rewardTypeEntityList);
			referralRewardTableDataModel = new ReferralRewardTableDataModelBean(rewardTypeEntityList);
			monitoringInterval = Application.getMonitoringIntervals();
			networkId = loginBean.getUser().getRealm().getName();
			apiKey = loginBean.getUser().getRealm().getApiKey();

			mobileApplicationList = daoMobileApplicationType.findAll();
			mobileApplicationTableDataModelBean = new MobileApplicationTypeTableDataModelBean(mobileApplicationList);

			if (apiKey == null || apiKey.length() == 0) {
				apiKey = "Not generated";
			}

			// FacesContext.getCurrentInstance().addMessage(null, new
			// FacesMessage("Success", "Refreshed API information"));
			// RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.toString());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Error", "Unable to retrieve API information: " + e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		}
	}

	public void regenerate() {
		try {
			// for testing
			// daoTest.createTestEntries();

			logger.info("regenerating API key for realm: " + loginBean.getUser().getRealm().getName());
			boolean generated = false;
			int maxCounter = 100;
			int currentCounter = 0;
			while (!generated) {
				logger.info("api generation counter: " + currentCounter);
				currentCounter = currentCounter + 1;
				if (currentCounter > maxCounter) {
					break;
				}

				String sha1 = DigestUtils
						.sha1Hex(loginBean.getUser().getRealm().getName() + System.currentTimeMillis());
				if (daoRealm.findByApiKey(sha1) == null) {
					RealmEntity realm = loginBean.getUser().getRealm();
					realm.setApiKey(sha1);
					apiKey = sha1;
					daoRealm.createOrUpdate(realm);
					generated = true;
				}
			}

			if (generated) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Success", "API key successfully generated"));
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
				RequestContext.getCurrentInstance().update("tabView:idApiDataGrid");
			} else {
				FacesMessage msg = new FacesMessage("Error",
						"Error regenerating API Key, unable to generate unique key after: " + currentCounter
								+ " retries...");
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			}
		} catch (Exception exc) {
			FacesMessage msg = new FacesMessage("Error", "Error regenerating API Key, error: " + exc.toString());
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		}
	}

	public void updateQuidco() {

		this.saveSnapdealCategories();

		if (!(realm.getQuidcoPercentageCommision() >= 0.0 && realm.getQuidcoPercentageCommision() <= 1.0)) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Failed", "Quidco percentage commision can be value from 0.0 to 1.0."));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		} else {
			update();
		}
	}

	public void update() {
		try {
			realm.setAFAConfiguration(getStringFromAFAConfiguration(afaConfiguration));
			logger.info("updating API data for for realm: " + loginBean.getUser().getRealm().getName());
			// update
			daoRealm.createOrUpdate(realm);
			monitoringSetup = daoMonitoringSetup.createOrUpdate(monitoringSetup);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Success", "Data successfully updated"));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			RequestContext.getCurrentInstance().update("tabView:idModeDataGrid");
			RequestContext.getCurrentInstance().update("tabView:idOfferWallSettingsDataGrid");
			RequestContext.getCurrentInstance().update("tabView:idMonitoringSettingsDataGrid");
			RequestContext.getCurrentInstance().update("tabView:idRewardTypesEnableTable");
			RequestContext.getCurrentInstance().update("tabView:idCallbackServersSettings");
			RequestContext.getCurrentInstance().update("tabView:idMobileApplicationTypes");
			RequestContext.getCurrentInstance().update("tabView:idReferralRewardsTable");
			RequestContext.getCurrentInstance().update("tabView:idSnapdealOrderList");
			RequestContext.getCurrentInstance().update("tabView:idQuidcoSnapdealPanel");
			RequestContext.getCurrentInstance().update("tabView:idSnapdealGrid");
			RequestContext.getCurrentInstance().update("tabView:tabNetworkSettings");

			logger.info("Update completed");
		} catch (Exception exc) {
			FacesMessage msg = new FacesMessage("Error", "Error performing update, error: " + exc.toString());
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		}
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}

	public MonitoringSetupEntity getMonitoringSetup() {
		return monitoringSetup;
	}

	public void setMonitoringSetup(MonitoringSetupEntity monitoringSetup) {
		this.monitoringSetup = monitoringSetup;
	}

	public int getMonitoringInterval() {
		return monitoringInterval;
	}

	public void setMonitoringInterval(int monitoringInterval) {
		this.monitoringInterval = monitoringInterval;
	}

	public RewardTypeDataModelBean getRewardTypeDataModelBean() {
		return rewardTypeDataModelBean;
	}

	public void setRewardTypeDataModelBean(RewardTypeDataModelBean rewardTypeDataModelBean) {
		this.rewardTypeDataModelBean = rewardTypeDataModelBean;
	}

	public void updateRewardTypeStatus(RewardTypeEntity entity) {
		boolean currentStatus = entity.isTestMode();
		entity.setTestMode(!currentStatus);
		daoRewardType.createOrUpdate(entity);
		update();

	}

	public MobileApplicationTypeTableDataModelBean getMobileApplicationTableDataModelBean() {
		return mobileApplicationTableDataModelBean;
	}

	public void setMobileApplicationTableDataModelBean(
			MobileApplicationTypeTableDataModelBean mobileApplicationTableDataModelBean) {
		this.mobileApplicationTableDataModelBean = mobileApplicationTableDataModelBean;
	}

	public MobileApplicationTypeEntity getMobileApplicationTypeEditModel() {
		return mobileApplicationTypeEditModel;
	}

	public void setMobileApplicationTypeEditModel(MobileApplicationTypeEntity mobileApplicationTypeEditModel) {
		this.mobileApplicationTypeEditModel = mobileApplicationTypeEditModel;
	}

	public MobileApplicationTypeEntity getMobileApplicationTypeCreateModel() {
		return mobileApplicationTypeCreateModel;
	}

	public void setMobileApplicationTypeCreateModel(MobileApplicationTypeEntity mobileApplicationTypeCreateModel) {
		this.mobileApplicationTypeCreateModel = mobileApplicationTypeCreateModel;
	}

	public List<SelectItem> getRealmList() {
		try {
			List<RealmEntity> realmEntityList = daoRealm.findAll();
			List<SelectItem> realmList = new ArrayList<SelectItem>();
			for (RealmEntity realm : realmEntityList) {
				realmList.add(new SelectItem(String.valueOf(realm.getId()), String.valueOf(realm.getId())));
			}

			return realmList;
		} catch (Exception exc) {
			exc.printStackTrace();
			return new ArrayList<SelectItem>();
		}
	}

	public void createMobileApplicationType() {
		try {

			if (mobileApplicationTypeCreateModel.getName().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide application name."));
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
				return;
			}

			mobileApplicationTypeCreateModel.setCreationTime(new Timestamp(new Date().getTime()));
			mobileApplicationTypeCreateModel.setRealmId(realm.getId());
			daoMobileApplicationType.createOrUpdate(mobileApplicationTypeCreateModel);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Application type created."));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationType.hide()");
			mobileApplicationTypeCreateModel = new MobileApplicationTypeEntity();
			refresh();
			update();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationType.hide()");

		}

	}

	public void editMobileApplicationType() {
		try {

			if (mobileApplicationTypeEditModel.getName().length() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Please provide application name."));
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
				return;
			}

			mobileApplicationTypeCreateModel.setRealmId(realm.getId());
			daoMobileApplicationType.createOrUpdate(mobileApplicationTypeEditModel);

			// update reward types application name
			List<RewardTypeEntity> rewardTypeList = daoRewardType.findAll();
			for (RewardTypeEntity rewardType : rewardTypeList) {
				if (rewardType.getApplicationId() == mobileApplicationTypeEditModel.getId()) {
					rewardType.setApplicationType(mobileApplicationTypeEditModel.getName());
					daoRewardType.createOrUpdate(rewardType);
				}
			}

			// update application reward application name
			List<ApplicationRewardEntity> applicationRewardType = daoApplicationReward.findAll();
			for (ApplicationRewardEntity rewardEntity : applicationRewardType) {
				if (rewardEntity.getApplicationId() == mobileApplicationTypeEditModel.getId()) {
					rewardEntity.setApplicationName(mobileApplicationTypeEditModel.getName());
					daoApplicationReward.createOrUpdate(rewardEntity);
				}
			}

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Application type updated."));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationType.hide()");
			mobileApplicationTypeEditModel = new MobileApplicationTypeEntity();
			refresh();
			update();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
			RequestContext.getCurrentInstance().execute("widgetCreateApplicationType.hide()");

		}

	}

	public void deleteMobileApplicationType() {
		this.daoMobileApplicationType.delete(mobileApplicationTypeEditModel);
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Mobile application type deleted."));
		RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		RequestContext.getCurrentInstance().execute("widgetEditApplicationType.hide()");
		mobileApplicationTypeEditModel = new MobileApplicationTypeEntity();
		refresh();
		update();
	}

	public ReferralRewardTableDataModelBean getReferralRewardTableDataModel() {
		return referralRewardTableDataModel;
	}

	public void setReferralRewardTableDataModel(ReferralRewardTableDataModelBean referralRewardTableDataModel) {
		this.referralRewardTableDataModel = referralRewardTableDataModel;
	}

	public RewardTypeEntity getReferralEditModel() {
		return referralEditModel;
	}

	public void setReferralEditModel(RewardTypeEntity referralEditModel) {
		this.referralEditModel = referralEditModel;
	}

	public double countReferralRewardsTotalValue(RewardTypeEntity model) {
		return model.getReferralValueAtFirstThresholdInvite() + model.getReferralValueAtSecondThresholdInvite();
	}

	public void editReferralReward() {

		try {

			if (this.referralEditModel.getReferralValueAtFirstThresholdInvite() < 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Failed", "Please provide 1st value above or equals 0."));
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
				return;
			}

			if (this.referralEditModel.getReferralValueAtSecondThresholdInvite() < 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Failed", "Please provide 5th value above or equals 0."));
				RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
				return;
			}
			this.daoRewardType.createOrUpdate(referralEditModel);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Referral reward changed."));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");

			refresh();
			update();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");

		}
	}

	public AFAConfiguration getAfaConfiguration() {
		return afaConfiguration;
	}

	public void setAfaConfiguration(AFAConfiguration afaConfiguration) {
		this.afaConfiguration = afaConfiguration;
	}

	public List<SelectItem> getExternalServerTypes() {
		List<SelectItem> externalServerTypeList = new ArrayList<SelectItem>();
		for (ExternalServerType type : ExternalServerType.values()) {
			externalServerTypeList.add(new SelectItem(type.toString(), type.toString()));
		}
		return externalServerTypeList;
	}

	private void laodSnapdealCategories() {
		String snapdealConfiguration = realm.getSnapdealCategoryConfiguration();
		if (snapdealConfiguration == null || snapdealConfiguration.length() == 0) {
			this.snapdealCategories = new ArrayList<String>();
		} else {
			this.snapdealCategories = new Gson().fromJson(snapdealConfiguration, new TypeToken<List<String>>() {
			}.getType());
		}

	}

	private void saveSnapdealCategories() {
		for (String category : snapdealCategories) {
			System.out.println(category);
		}
		String json = new Gson().toJson(snapdealCategories);
		realm.setSnapdealCategoryConfiguration(json);
		daoRealm.createOrUpdate(realm);
		update();

	}

	public void addSnapdealCategory() {
		if (snapdealCategoryName != null && snapdealCategoryName.length() > 0) {
			snapdealCategories.add(snapdealCategoryName);
			saveSnapdealCategories();
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", " Can't add empty category."));
			RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
		}
	}

	public void removeSnapdealCategory() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedElement = params.get("selectedElement");
		for (String snapdealCategory : snapdealCategories) {
			if (snapdealCategory.equals(selectedElement)) {
				snapdealCategories.remove(snapdealCategory);
				break;
			}
		}

		saveSnapdealCategories();

	}

	public String getSnapdealCategoryName() {
		return snapdealCategoryName;
	}

	public void setSnapdealCategoryName(String snapdealCategoryName) {
		this.snapdealCategoryName = snapdealCategoryName;
	}

	public List<String> getSnapdealCategories() {
		return snapdealCategories;
	}

	public void setSnapdealCategories(List<String> snapdealCategories) {
		this.snapdealCategories = snapdealCategories;
	}

	public Date getReportStartDate() {
		return reportStartDate;
	}

	public void setReportStartDate(Date reportStartDate) {
		this.reportStartDate = reportStartDate;
	}

	public int getReportOffset() {
		return reportOffset;
	}

	public void setReportOffset(int reportOffset) {
		this.reportOffset = reportOffset;
	}

	public void generateReport() {
		
		Client client = null;
		String hostName = "10.240.3.192";
		String clusterName = "airrewardz";
		try {
			logger.info("Generating report from " + reportStartDate + " offset: " + reportOffset);
			Date reportDate = (Date) reportStartDate.clone();
			Date currentDate = new Date();

			// once we find one node in the cluster ask about the others
			Builder settingsBuilder = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true);
			settingsBuilder.put("cluster.name", clusterName);
			settingsBuilder.put("client.transport.ping_timeout", "10s");
			settingsBuilder.put("http.enabled", "false");
			settingsBuilder.put("transport.tcp.port", "9300-9400");
			settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
			settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostName);
			Settings settings = settingsBuilder.build();

			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostName, 9300));
			reportResult = "";
			if (reportOffset > 0) {
				reportResult += " Start date: " + reportDate + " offset:" + reportOffset + " reward type: "
						+ reportRewardType;
				reportResult += "<table cellpadding='10'>";
				reportResult += "<tr><td> Date </td><td> Install </td><td> Snapdeal </td><td> Video </td><td> Unique users using spinner </td><td> Bought spins </td></tr>";
				while (reportDate.before(currentDate)) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(reportDate);
					cal.add(Calendar.DATE, reportOffset);
					Date reportDateWithOffset = cal.getTime();

					double installProfit = daoUserEvent.sumProfitValueFromEventCategoryInDateRange(
							UserEventCategory.INSTALL, reportRewardType, reportDate, reportDateWithOffset);

					double snapdealProfit = daoUserEvent.sumProfitValueFromEventCategoryInDateRange(
							UserEventCategory.SNAPDEAL, reportRewardType, reportDate, reportDateWithOffset);

					double videoProfit = daoUserEvent.sumProfitValueFromEventCategoryInDateRange(
							UserEventCategory.VIDEO, reportRewardType, reportDate, reportDateWithOffset);

					List<UserEventEntity> spinnerEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
							UserEventCategory.SPINNER, reportDate, reportDateWithOffset, reportRewardType);
					HashMap<Integer, Integer> uniqueSpinnerUsersMap = new HashMap<Integer, Integer>();
					for (UserEventEntity event : spinnerEvents) {
						if (event != null) {
							if (uniqueSpinnerUsersMap.containsKey(event.getUserId())) {
								uniqueSpinnerUsersMap.put(event.getUserId(),
										uniqueSpinnerUsersMap.get(event.getUserId() + 1));
							} else {
								uniqueSpinnerUsersMap.put(event.getUserId(), 1);
							}
						}

					}
					// System.out.println("->"+uniqueSpinnerUsersMap.size());

					QueryBuilder query;

					BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
					boolQueryBuilder
							.must(QueryBuilders.rangeQuery("@timestamp").from(reportDate).to(reportDateWithOffset));
					boolQueryBuilder.must(QueryBuilders.wildcardQuery("message", "*bought*"));

					query = boolQueryBuilder;
					CountResponse response = client.prepareCount("*").setTypes("ab-log_data").setQuery(query).execute()
							.actionGet();

					// System.out.println("Response: " + response.getCount());
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					reportResult += "<tr><td>" + format.format(reportDate) + " - " + format.format(reportDateWithOffset)
							+ " </td><td>" + installProfit + "</td><td>" + snapdealProfit + "</td><td>" + videoProfit
							+ "</td><td>" + uniqueSpinnerUsersMap.size() + "</td><td> " + response.getCount()
							+ "</td></tr>";

					reportDate = reportDateWithOffset;
				}
				
				reportResult += "</table>";
				refresh();
				update();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}finally{
			client.close();
		}

	}

	public String getReportResult() {
		return reportResult;
	}

	public void setReportResult(String reportResult) {
		this.reportResult = reportResult;
	}

	public String getReportRewardType() {
		return reportRewardType;
	}

	public void setReportRewardType(String reportRewardType) {
		this.reportRewardType = reportRewardType;
	}

	public List<SelectItem> getRewardTypes() {
		List<SelectItem> rewardTypeList = new ArrayList<SelectItem>();
		for (RewardTypeEntity entity : this.rewardTypeEntityList) {
			rewardTypeList.add(new SelectItem(entity.getName(), entity.getName()));
		}
		return rewardTypeList;
	}

}
