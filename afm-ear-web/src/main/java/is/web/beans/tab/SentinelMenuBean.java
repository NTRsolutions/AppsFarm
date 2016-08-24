package is.web.beans.tab;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.web.beans.tab.SentinelTabBean.SingleTabBean;
import is.web.beans.users.LoginBean;
import is.web.geo.GeoLocation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

@ManagedBean(name="sentinelMenuBean")
@SessionScoped
public class SentinelMenuBean implements Serializable  {
	
//	<p:submenu label="CWMP Mass Management" >
//	<p:menuitem value="Profiling" actionListener="#{sentinelMenuBean.loadTab('tabSentinelCWMPMassManagementProfiling')}" ajax="false" icon="ui-icon-folder-open" />
//	<p:menuitem value="Monitoring" actionListener="#{sentinelMenuBean.loadTab('tabSentinelCWMPMassManagementMonitoring')}" ajax="false" icon="ui-icon-folder-open" />
//	<p:menuitem value="Auto-configuration" actionListener="#{sentinelMenuBean.loadTab('tabSentinelCWMPMassManagementAutoconfiguration')}" ajax="false" icon="ui-icon-folder-open" />
//	<p:menuitem value="Provisioning" actionListener="#{sentinelMenuBean.loadTab('tabSentinelCWMPMassManagementProvisioning')}" ajax="false" icon="ui-icon-folder-open" />
//	</p:submenu>
	  
	@Inject
	Logger logger;

	@Inject
	LoginBean loginBean;
	
	SentinelTabBean tabBean;
	
	public SentinelMenuBean() {
	}

   @PostConstruct
   public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   tabBean = (SentinelTabBean)  fc.getApplication().evaluateExpressionGet(fc, "#{sentinelTabBean}", SentinelTabBean.class);
   }
	
   private void verifyUserAuth() {
	   logger.info("authenticating user...");
	   boolean authenticated = loginBean.isAuthenticated();
	   if(!authenticated) {
		   try {
			   Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, -1, LogStatus.WARNING, "LOGIN_ACTIVITY user not authenticated or no session found, redirecting to logon page...");
			   logger.info("user not authenticated or no session found, redirecting to logon page...");
			   FacesContext.getCurrentInstance().getExternalContext().redirect("index.jsf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
	   }
   }
   
   public void loadTab(String tabType) {
	   
	   //logon
	   verifyUserAuth();
	   
	   logger.info("loading tab with name: "+tabType+" number of existing tabs: "+tabBean.getTabList().size());
	   SingleTabBean foundTab = tabBean.findTabByTabType(tabType); //identify if tab does not already exit
	   if(foundTab != null) {
		   tabBean.setActiveIndex(foundTab.getIndex());
		   logger.info("identified already existing tab with name: "+foundTab.getTitle()+" at index: "+tabBean.getActiveIndex());
		   //refresh tab data
		   tabBean.refreshDisplayedTabDataModel(foundTab);
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SUPPORTED_REWARD_TYPES)) { 
		   tabBean.addTab(SentinelTabs.TAB_SUPPORTED_REWARD_TYPES,"SetupRewardTypes","setupRewardTypes.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_CUSTOM_DENOMINATION_MODELS)) { 
		   tabBean.addTab(SentinelTabs.TAB_CUSTOM_DENOMINATION_MODELS,"CustomDenominationModels","customDenominationModels.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_DISPLAY_APP_USERS)) { 
		   tabBean.addTab(SentinelTabs.TAB_DISPLAY_APP_USERS,"RegisteredUsers","displayAppUsers.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_DISPLAY_GENERATED_OFFERS)) { 
		   tabBean.addTab(SentinelTabs.TAB_DISPLAY_GENERATED_OFFERS,"GeneratedOffers","generatedOffers.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_BLOCKED_OFFERS)) { 
		   tabBean.addTab(SentinelTabs.TAB_BLOCKED_OFFERS,"BlockedOffers","blockedOffers.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_AD_PROVIDERS)) { 
		   tabBean.addTab(SentinelTabs.TAB_AD_PROVIDERS,"ConfigureAdProviders","setupAdProviders.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_DENOMINATION_MODELS)) { 
		   tabBean.addTab(SentinelTabs.TAB_DENOMINATION_MODELS,"DenominationModels","setupDenominationModels.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SETUP_CURRENCIES)) { 
		   tabBean.addTab(SentinelTabs.TAB_SETUP_CURRENCIES,"SetupCurrencies","setupCurrencies.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SETUP_OFFER_FILTERS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SETUP_OFFER_FILTERS,"OfferFilters","setupOfferFilters.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_OFFER_WALLS)) { 
		   tabBean.addTab(SentinelTabs.TAB_OFFER_WALLS,"ConfigureOfferWalls","setupOfferWalls.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SYSTEM_SETTINGS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SYSTEM_SETTINGS,"SystemSettings","systemSettings.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_MANAGE_USERS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_MANAGE_USERS,"ManageUsers","manageUsers.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_NETWORK_SETTINGS)) { 
		   tabBean.addTab(SentinelTabs.TAB_NETWORK_SETTINGS,"NetworkSettings","networkSettings.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_QUIDCO_EVENT_BROWSER)) {
		   tabBean.addTab(SentinelTabs.TAB_QUIDCO_EVENT_BROWSER, "QuidcoEventBrowser", "quidcoEventBrowser.xhtml", "");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SNAPDEAL_EVENT_BROWSER)) { 
		   tabBean.addTab(SentinelTabs.TAB_SNAPDEAL_EVENT_BROWSER,"SnapdealEventBrowser","snapdealEventBrowser.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_EVENT_BROWSER)) { 
		   tabBean.addTab(SentinelTabs.TAB_EVENT_BROWSER,"EventBrowser","eventBrowser.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_REWARD_TICKETS)) { 
		   tabBean.addTab(SentinelTabs.TAB_REWARD_TICKETS, "RewardTickets", "rewardTickets.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_CONVERSION_STATS)) { 
		   tabBean.addTab(SentinelTabs.TAB_CONVERSION_STATS,"ConversionStats","conversionStats.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SYSTEM_STATUS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SYSTEM_STATUS,"SystemStatus","systemStatus.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_LICENSE_MANAGEMENT)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_LICENSE_MANAGEMENT,"LicenseManagement","licenseManagement.xhtml","");
	   }
	   
	   else if(tabType.equals(SentinelTabs.TAB_TOOLS_REWARD_USERS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_TOOLS_REWARD_USERS,"RewardUser","rewardUser.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SYSTEM_WALLET_SETTINGS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SYSTEM_WALLET_SETTINGS,"WalletSettings","walletSettings.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_WALLET_TRANSACTION)) {
		   tabBean.addTab(SentinelTabs.TAB_WALLET_TRANSACTION, "WalletTransactions", "walletTransactions.xhtml", "");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_PROVIDER_REPORT)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_PROVIDER_REPORT,"ProviderReport","providerReport.xhtml","");
	   }
	   
	   else if(tabType.equals(SentinelTabs.TAB_APPLICATIONS_REWARD)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_APPLICATIONS_REWARD,"ApplicationRewards","applicationRewards.xhtml","");
	   }
	   
	   else if(tabType.equals(SentinelTabs.TAB_REFERRALS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_REFERRALS,"Referrals","referrals.xhtml","");
	   }
	   
	   else if (tabType.equals(SentinelTabs.TAB_FAILED_EVENTS_BROWSER)){
		   tabBean.addTab(SentinelTabs.TAB_FAILED_EVENTS_BROWSER,"FailedEventsBrowser","failedEventsBrowser.xhtml","");
	   }
	   
	   else if (tabType.equals(SentinelTabs.TAB_VIDEO_GAMIFICATIONS_SETTINGS)){
		   tabBean.addTab(SentinelTabs.TAB_VIDEO_GAMIFICATIONS_SETTINGS,"VideoGamificationSettings","videoGamificationSettings.xhtml","");
	   }
	  
	   else if (tabType.equals(SentinelTabs.TAB_SPINNER_GAMIFICATIONS_SETTINGS)){
		   tabBean.addTab(SentinelTabs.TAB_SPINNER_GAMIFICATIONS_SETTINGS,"SpinnerGamificationSettings","spinnerGamificationSettings.xhtml","");
	   }
	   
	   else if(tabType.equals(SentinelTabs.TAB_TOOLS_REWARD_SPIN)) { 
		   tabBean.addTab(SentinelTabs.TAB_TOOLS_REWARD_SPIN,"RewardSpin","rewardSpin.xhtml","");
	   }
	   
	   else if(tabType.equals(SentinelTabs.TAB_SPINNER_STATS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SPINNER_STATS,"SpinnerStats","spinnerStats.xhtml","");
	   }
	 

	   //to be deleted in future
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_AUTOREGISTER)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_AUTOREGISTER,"Autoregister","sentinel/autoregister.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_DEVICES_LIST)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_DEVICES_LIST,"Devices","sentinel/devicesList.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_FIND_DEVICE)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_FIND_DEVICE,"FindDevice","sentinel/findDevice.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_DEVICES_STATUS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_DEVICES_STATUS,"DevicesStatus","sentinel/devicesStatus.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_DOMAINS_ASSIGN_DEVICES_TO_DOMAINS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_DOMAINS_ASSIGN_DEVICES_TO_DOMAINS,"AssignDevices","sentinel/assignDevicesToDomains.xhtml","");
	   } //cwmp config
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_MONITORING_SCRIPTS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_MONITORING_SCRIPTS,"SetupInformSchema","sentinel/cwmpConfigureMonitoringScripts.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_DEVICE_PROFILES)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_DEVICE_PROFILES,"SetupDeviceSchema","sentinel/cwmpConfigureDeviceProfiles.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_HARDWARE_MODELS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_HARDWARE_MODELS,"SetupHardwareModels","sentinel/cwmpConfigureHardwareModels.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_AUTOCONFIGURATION_SCRIPTS)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_AUTOCONFIGURATION_SCRIPTS,"SetupAutoconfiguration","sentinel/cwmpConfigureAutoconfigurationScripts.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_PROVISIONING)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_CONFIGURE_PROVISIONING,"SetupUpgrades","sentinel/cwmpConfigureProvisioning.xhtml","");
	   } //cwmp mass management config
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_PROFILING)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_PROFILING,"MassProfiling","sentinel/cwmpMassManagementProfiling.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_MONITORING)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_MONITORING,"MassMonitoring","sentinel/cwmpMassManagementMonitoring.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_AUTOCONFIGURATION)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_AUTOCONFIGURATION,"MassAutoconfiguration","sentinel/cwmpMassManagementAutoconfiguration.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_PROVISIONING)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_CWMP_MASS_MANAGEMENT_PROVISIONING,"MassManagementProfiling","sentinel/cwmpMassManagementProvisioning.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_MONITORING_DEVICES_MAP)) { 
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_MONITORING_DEVICES_MAP,"DevicesMap","sentinel/devicesMap.xhtml","");
	   } 
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_MONITORING_SETUP)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_MONITORING_SETUP,"MonitoringSetup","sentinel/monitoringSetup.xhtml","");
	   }	   
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_MONITORING_DEVICES_ALERTS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_MONITORING_DEVICES_ALERTS,"DeviceAlerts","sentinel/deviceAlerts.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_SENTINEL_MONITORING_SYSTEM_ALERTS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_SENTINEL_MONITORING_SYSTEM_ALERTS,"SystemAlerts","sentinel/systemAlerts.xhtml","");
	   }
	   else if(tabType.equals(SentinelTabs.TAB_REPORTS_ALERTS)) { //otherwise we create a new tab
		   tabBean.addTab(SentinelTabs.TAB_REPORTS_ALERTS,"AlertsReporting","sentinel/reportsAlerts.xhtml","");
	   }
	  

   }

   public void loadIndividualNodeTab(String tabName) {
	   logger.info("loading individual node tab with node name: "+tabName+" number of existing tabs: "+tabBean.getTabList().size());

	   //String uniqueIndividualNodeTabType = "tabIndividualDevice"+tabName;
	   String uniqueIndividualNodeTabType = "tabIndividualDevice";
	   SingleTabBean foundTab = tabBean.findTabByTabType(uniqueIndividualNodeTabType); //identify if tab does not already exit
	   //treeBean.addDomainNode(); //for testing

	   if(foundTab != null) {
		   tabBean.setActiveIndex(foundTab.getIndex());
		   logger.info("identified already existing tab with name: "+foundTab.getTitle()+" at index: "+tabBean.getActiveIndex());
		   //enforce data refresh on displayd device tab
		   foundTab.setId(tabName);
		   tabBean.refreshDisplayedTabDataModel(foundTab);
	   } else {
		   tabBean.addTab(uniqueIndividualNodeTabType,"MonitoredDevice","sentinel/device.xhtml",tabName);
	   }
   }

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}
                    