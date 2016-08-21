package is.web.beans.tab;

import is.ejb.bl.business.Constants;
import is.ejb.bl.business.UserRoles;
import is.ejb.dl.entities.RoleEntity;
import is.web.beans.appusers.DisplayAppUsersBean;
import is.web.beans.denomination.CustomDenominationModelsBean;
import is.web.beans.denomination.DenominationModelBean;
import is.web.beans.events.EventBrowserBean;
import is.web.beans.events.FailedEventsBrowserBean;
import is.web.beans.license.LicenseManagementBean;
import is.web.beans.monitoring.SystemAlertsListBean;
import is.web.beans.offerCurrencies.OfferCurrenciesBean;
import is.web.beans.offerFilters.OfferFiltersBean;
import is.web.beans.offerRewardTypes.OfferRewardTypesBean;
import is.web.beans.offers.AdProviderConfigurationBean;
import is.web.beans.offers.BlockedOffersBean;
import is.web.beans.offers.GeneratedOffersBean;
import is.web.beans.offers.OfferWallConfigurationBean;
import is.web.beans.referral.ReferralBean;
import is.web.beans.reporting.ConversionStatsBean;
import is.web.beans.system.NetworkSettingsBean;
import is.web.beans.system.SpinnerGamificationBean;
import is.web.beans.system.SystemSettingsBean;
import is.web.beans.system.VideoGamificationBean;
import is.web.beans.system.WalletSettingsBean;
import is.web.beans.system.status.SystemStatusBean;
import is.web.beans.users.LoginBean;
import is.web.beans.users.UsersManagementBean;
import is.web.geo.DevicesMapBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

@ManagedBean(name="sentinelTabBean")
@SessionScoped
public class SentinelTabBean implements Serializable {
	
	@Inject
	Logger logger;
	
	private LoginBean loginBean = null;
	private List<SingleTabBean> tabList;
	private String loggedUserRoleName = "";
	private int activeIndex = 0;
	
	public SentinelTabBean() {
	} 

	@PostConstruct
	public void init() {
		try {
			//retrieve reference of an objection from session
			logger.info("initialising tabs container...");
			tabList = new ArrayList<SingleTabBean>();
			FacesContext fc = FacesContext.getCurrentInstance();
			loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
			//extract logged user role name
			List<RoleEntity> loggedUserRoles = (List<RoleEntity>) loginBean.getUser().getRoles();
			loggedUserRoleName = loggedUserRoles.get(0).getName(); //at the moment support only single role assignment

			/*
			SingleTabBean tab = new SingleTabBean();
			tab.setId("Domains");
			tab.setTitle("Domains");
			tab.setType(SentinelTabs.TAB_SENTINEL_DOMAINS_LIST);
			tab.setPage("sentinel/domains.xhtml");
			tabList.add(tab);
			SingleTabBean tab = new SingleTabBean();
			tab.setId("Devices");
			tab.setTitle("Devices");
			tab.setType(SentinelTabs.TAB_SENTINEL_DEVICES_LIST);
			tab.setPage("sentinel/devicesList.xhtml");
			tabList.add(tab);
	*/

			if(loggedUserRoleName.equals(UserRoles.SUPER_USER.toString())){
	/*			
				SingleTabBean tab = new SingleTabBean();
				tab.setId("SystemSettings");
				tab.setTitle("SystemSettings");
				tab.setType(SentinelTabs.TAB_SYSTEM_SETTINGS);
				tab.setPage("systemSettings.xhtml");
				tabList.add(tab);
	*/			
			} 
			else {
	/*
				SingleTabBean tab = new SingleTabBean();
				tab.setId("Conversions");
				tab.setTitle("ConversionStats");
				tab.setType(SentinelTabs.TAB_CONVERSION_STATS);
				tab.setPage("conversionStats.xhtml");
				tabList.add(tab);
	 */
			}
		} catch(Exception exc) {
			logger.severe(exc.toString());
		}


   }

	public List<SingleTabBean> getTabList() {
		return tabList;
	}

	public void setTabList(List<SingleTabBean> tabList) {
		this.tabList = tabList;
	}

	public SingleTabBean findTabByTabType(String tabType) {
		for(int i=0;i<tabList.size();i++) {
			SingleTabBean tabBean = tabList.get(i); 
			if(tabBean.getType().equals(tabType)) {
				tabBean.setIndex(i);
				return tabList.get(i);
			}
		}
		
		return null;
	}

	public SingleTabBean findTabByTabTitle(String tabName) {
		for(int i=0;i<tabList.size();i++) {
			SingleTabBean tabBean = tabList.get(i); 
			if(tabBean.getTitle().equals(tabName)) {
				tabBean.setIndex(i);
				return tabList.get(i);
			}
		}
		
		return null;
	}
	
	public void addTab(String tabType, String tabTitle, String templateName, String tabId) {
		SingleTabBean tab = new SingleTabBean();
		tab.setType(tabType);
		if(tabId.equals("")){
			tab.setId(tabTitle);
		} else {
			tab.setId(tabId);
		}
	    tab.setTitle(tabTitle);
        tab.setPage(templateName);
        tab.setIndex(tabList.size()-1);

        tabList.add(tab);
        activeIndex = tabList.size()-1;
        
        SingleTabBean tabBean = tabList.get(activeIndex);
	    logger.info("dynamically added tab actvice index: "+activeIndex+" tab type: "+tabBean.getType()+ "tab title: "+tab.getTitle());

	    //triggers refresh of session beans data (e.g. list of scripts are pulled to refresh data when tab is opened/switched to)
		refreshDisplayedTabDataModel(tabBean);
	}

	public void onTabChange(TabChangeEvent event) { 
		logger.info("changed to tab: "+event.getTab().getClientId()+" title: "+event.getTab().getTitle());
		SingleTabBean tabBean = findTabByTabTitle(event.getTab().getTitle());
		activeIndex = tabBean.getIndex();
		//logger.info("changed tab to: "+tabBean.getTitle()+" at index: "+tabBean.getIndex());
		//triggers refresh of session beans data (e.g. list of scripts are pulled to refresh data when tab is opened/switched to)
		refreshDisplayedTabDataModel(tabBean);
	} 

	public void onTabClose(TabCloseEvent event) { 
		logger.info("dynamically closing tab: "+event.getTab().getTitle());
        FacesMessage msg = new FacesMessage("Tab Closed", "Closed tab: " + event.getTab().getTitle());  
        FacesContext.getCurrentInstance().addMessage(null, msg);  

        String closedTabTitle = event.getTab().getTitle();
        for(int i=0;i<tabList.size();i++) {
        	if(tabList.get(i).getTitle() == closedTabTitle && !tabList.get(i).getTitle().equals("Domains")) {
        		tabList.remove(i);
        	}
        }
        
        activeIndex = tabList.size()-1;
    }

    //refresh tab data
	public void refreshDisplayedTabDataModel(SingleTabBean tabBean) {
		FacesContext fc = FacesContext.getCurrentInstance();

		if(tabBean.getType().startsWith(SentinelTabs.TAB_SUPPORTED_REWARD_TYPES)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			OfferRewardTypesBean backingBean = (OfferRewardTypesBean)  fc.getApplication().evaluateExpressionGet(fc, "#{offerRewardTypesBean}", OfferRewardTypesBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_CUSTOM_DENOMINATION_MODELS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			CustomDenominationModelsBean backingBean = (CustomDenominationModelsBean)  fc.getApplication().evaluateExpressionGet(fc, "#{customDenominationModelsBean}", CustomDenominationModelsBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_DISPLAY_GENERATED_OFFERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			GeneratedOffersBean backingBean = (GeneratedOffersBean)  fc.getApplication().evaluateExpressionGet(fc, "#{generatedOffersBean}", GeneratedOffersBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_DISPLAY_APP_USERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			DisplayAppUsersBean backingBean = (DisplayAppUsersBean)  fc.getApplication().evaluateExpressionGet(fc, "#{displayAppUsersBean}", DisplayAppUsersBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_BLOCKED_OFFERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			BlockedOffersBean backingBean = (BlockedOffersBean)  fc.getApplication().evaluateExpressionGet(fc, "#{blockedOffersBean}", BlockedOffersBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_AD_PROVIDERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			AdProviderConfigurationBean backingBean = (AdProviderConfigurationBean)  fc.getApplication().evaluateExpressionGet(fc, "#{adProviderConfigurationBean}", AdProviderConfigurationBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_DENOMINATION_MODELS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			DenominationModelBean backingBean = (DenominationModelBean)  fc.getApplication().evaluateExpressionGet(fc, "#{denominationModelBean}", DenominationModelBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_SETUP_CURRENCIES)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			OfferCurrenciesBean backingBean = (OfferCurrenciesBean)  fc.getApplication().evaluateExpressionGet(fc, "#{offerCurrenciesBean}", OfferCurrenciesBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_SETUP_OFFER_FILTERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			OfferFiltersBean backingBean = (OfferFiltersBean)  fc.getApplication().evaluateExpressionGet(fc, "#{offerFiltersBean}", OfferFiltersBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_OFFER_WALLS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			OfferWallConfigurationBean backingBean = (OfferWallConfigurationBean)  fc.getApplication().evaluateExpressionGet(fc, "#{offerWallConfigurationBean}", OfferWallConfigurationBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_SYSTEM_SETTINGS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			SystemSettingsBean backingBean = (SystemSettingsBean)  fc.getApplication().evaluateExpressionGet(fc, "#{systemSettingsBean}", SystemSettingsBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_SYSTEM_STATUS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			SystemStatusBean backingBean = (SystemStatusBean)  fc.getApplication().evaluateExpressionGet(fc, "#{systemStatusBean}", SystemStatusBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_MANAGE_USERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			UsersManagementBean backingBean = (UsersManagementBean)  fc.getApplication().evaluateExpressionGet(fc, "#{usersManagementBean}", UsersManagementBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_NETWORK_SETTINGS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			NetworkSettingsBean backingBean = (NetworkSettingsBean)  fc.getApplication().evaluateExpressionGet(fc, "#{networkSettingsBean}", NetworkSettingsBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_EVENT_BROWSER)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			EventBrowserBean backingBean = (EventBrowserBean)  fc.getApplication().evaluateExpressionGet(fc, "#{eventBrowserBean}", EventBrowserBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_CONVERSION_STATS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			ConversionStatsBean backingBean = (ConversionStatsBean)  fc.getApplication().evaluateExpressionGet(fc, "#{conversionStatsBean}", ConversionStatsBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_LICENSE_MANAGEMENT)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			LicenseManagementBean backingBean = (LicenseManagementBean)  fc.getApplication().evaluateExpressionGet(fc, "#{licenseManagementBean}", LicenseManagementBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		
		if(tabBean.getType().startsWith(SentinelTabs.TAB_TOOLS_REWARD_USERS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			}
		
		if(tabBean.getType().startsWith(SentinelTabs.TAB_SYSTEM_WALLET_SETTINGS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			WalletSettingsBean backingBean = (WalletSettingsBean)  fc.getApplication().evaluateExpressionGet(fc, "#{walletSettingsBean}", WalletSettingsBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		}
		
		if(tabBean.getType().startsWith(SentinelTabs.TAB_PROVIDER_REPORT)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			}
		
		if(tabBean.getType().startsWith(SentinelTabs.TAB_APPLICATIONS_REWARD)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			}
		if(tabBean.getType().startsWith(SentinelTabs.TAB_REFERRALS)) {
			//by calling below line we initialise bean and cause the GUI to be refreshed (as the bean is request scoped)
			ReferralBean backingBean = (ReferralBean)  fc.getApplication().evaluateExpressionGet(fc, "#{referralBean}", ReferralBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!
		
		}
		
		if (tabBean.getType().startsWith(SentinelTabs.TAB_VIDEO_GAMIFICATIONS_SETTINGS))
		{
			VideoGamificationBean backingBean = (VideoGamificationBean)  fc.getApplication().evaluateExpressionGet(fc, "#{videoGamificationBean}", VideoGamificationBean.class);
		    backingBean.refresh();
		}
		
		if (tabBean.getType().startsWith(SentinelTabs.TAB_SPINNER_GAMIFICATIONS_SETTINGS))
		{
			SpinnerGamificationBean backingBean = (SpinnerGamificationBean)  fc.getApplication().evaluateExpressionGet(fc, "#{spinnerGamificationBean}", SpinnerGamificationBean.class);
		    backingBean.refresh();
		}
        if (tabBean.getType().startsWith(SentinelTabs.TAB_FAILED_EVENTS_BROWSER)){
        	FailedEventsBrowserBean backingBean = (FailedEventsBrowserBean)  fc.getApplication().evaluateExpressionGet(fc, "#{failedEventsBrowserBean}", FailedEventsBrowserBean.class);
			backingBean.refresh(); //no need to call refresh on this as long as the backing bean is request scoped!

		}
        if (tabBean.getType().startsWith(SentinelTabs.TAB_SPINNER_STATS)){
        	
		}
        
        
	}
	
	public int getActiveIndex() {
		//logger.info("returning active index: "+activeIndex);
		return activeIndex;
	}

	public void setActiveIndex(int activeIndex) {
		this.activeIndex = activeIndex;
	}

	public class SingleTabBean extends Tab {
		private String id;
		private String type;
		private String title;
		private String page;
		private int index;
		   
		public SingleTabBean() {
			super();
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getPage() {
			return page;
		}
		public void setPage(String page) {
			this.page = page;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}



