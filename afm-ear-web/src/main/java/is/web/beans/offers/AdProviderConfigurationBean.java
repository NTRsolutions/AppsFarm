package is.web.beans.offers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.clickey.ClickeyProviderConfig;
import is.ejb.bl.offerProviders.clickey.SerDeClickeyProviderConfiguration;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffersExt.HasoffersExtProviderConfig;
import is.ejb.bl.offerProviders.hasoffersExt.SerDeHasoffersExtProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffersNativex.HasoffersNativexProviderConfig;
import is.ejb.bl.offerProviders.hasoffersNativex.SerDeHasoffersNativexProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffersVC.HasoffersVCProviderConfig;
import is.ejb.bl.offerProviders.hasoffersVC.SerDeHasoffersVCProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.personaly.PersonalyProviderConfig;
import is.ejb.bl.offerProviders.personaly.SerDePersonalyProviderConfiguration;
import is.ejb.bl.offerProviders.snapdeal.SerDeSnapdealProviderConfiguration;
import is.ejb.bl.offerProviders.snapdeal.SnapdealProviderConfig;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerProviders.trialpay.SerDeTrialPayProviderConfiguration;
import is.ejb.bl.offerProviders.trialpay.TrialPayProviderConfig;
import is.ejb.bl.offerProviders.woobi.SerDeWoobiProviderConfiguration;
import is.ejb.bl.offerProviders.woobi.WoobiProviderConfig;
import is.ejb.bl.offerWall.config.OfferWallConfiguration;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.primefaces.context.RequestContext;

@ManagedBean(name="adProviderConfigurationBean")
@SessionScoped
public class AdProviderConfigurationBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;
	
	private AdProviderDataModelBean domainDataModel;
	private List<AdProviderEntity> listDomains = new ArrayList<AdProviderEntity>();
	
	@Inject
	private DAOAdProvider daoAdProvider;
	@Inject
	private DAORealm daoRealm;

	@Inject
	private SerDeTrialPayProviderConfiguration serDeTrialPay;
	@Inject
	private SerDeClickeyProviderConfiguration serDeClickey;
	@Inject
	private SerDeWoobiProviderConfiguration serDeWoobi;
	@Inject
	private SerDeSupersonicProviderConfiguration serDeSupersonic;
	@Inject
	private SerDeAarkiProviderConfiguration serDeAaarki;
	@Inject
	private SerDeMinimobProviderConfiguration serDeMinimob;
	@Inject
	private SerDeFyberProviderConfiguration serDeFyber;
	@Inject
	private SerDeHasoffersProviderConfiguration serDeHasoffers;
	@Inject
	private SerDeHasoffersExtProviderConfiguration serDeHasoffersExt;
	@Inject
	private SerDeHasoffersNativexProviderConfiguration serDeHasoffersNativex;
	@Inject
	private SerDeHasoffersVCProviderConfiguration serDeHasoffersVC;
	@Inject
	private SerDePersonalyProviderConfiguration serDePersonaly;
	@Inject
	private SerDeSnapdealProviderConfiguration serDeSnapdeal;
	
	private TrialPayProviderConfig configTrialPay;
	private ClickeyProviderConfig configClickey;
	private WoobiProviderConfig configWoobiAndroid;
	private WoobiProviderConfig configWoobiIOS;
	private SupersonicProviderConfig configSupersonic;
	private AarkiProviderConfig configAarki;
	private MinimobProviderConfig configMinimob;
	private FyberProviderConfig configFyber;
	private HasoffersProviderConfig configHasoffers;
	private HasoffersExtProviderConfig configHasoffersExt;
	private HasoffersNativexProviderConfig configHasoffersNativex;
	private HasoffersVCProviderConfig configHasoffersVC;
	private PersonalyProviderConfig configPersonaly;
	private SnapdealProviderConfig configSnapdeal;
	
	private AdProviderEntity editedDomain = new AdProviderEntity();
	private AdProviderEntity createdDomain = new AdProviderEntity();
	
	private UserEntity customer;

	private boolean renderTrialPay = false;
	private boolean renderClickey = false;
	private boolean renderWoobiAndroid = false;
	private boolean renderWoobiIOS = false;
	private boolean renderSupersonic = false;
	private boolean renderAarki = false;
	private boolean renderMinimob = false;
	private boolean renderFyber = false;
	private boolean renderHasoffers = false;
	private boolean renderHasoffersExt = false;
	private boolean renderHasoffersNativex = false;
	private boolean renderHasoffersVC = false;
	private boolean renderPersonaly = false;
	private boolean renderSnapdeal = false;
	
	//create fields
	private String snapdealTokenCreate = "";
	private String snapdealIdCreate = "";

	private String personalyAppHashCreate = "";
	private int personalyRecordsPerPageCreate = 1000;

	private String trialPayVicCreate = "";
	private int trialPayNmberOfPulledOffersCreate = 1000;
	
	private String clickeyApiUrlCreate = "";

	private String woobiIOSApiUrlCreate = "";
	private String woobiAndroidApiUrlCreate = "";

	private String supersonicAccessKeyCreate = "";
	private String supersonicApplicationKeyCreate = "";
	private String supersonicSecretKeyCreate = "";
	private String supersonicPlatformCreate = "";
	private int supersonicNumberOfPulledOffersCreate = 1000;
	
	private String minimobApiKeyCreate = "";
	private long minimobServiceQueryIntervalCreate; //in ms
	
	private String fyberParam1Create = "";
	
	private String hasoffersNetworkIdCreate = "";
	private String hasoffersNetworkTokenCreate = "";
	private String hasoffersAffIdCreate = "";
	private String hasoffersGroupNameCreate = "";
	private long hasoffersServiceQueryIntervalCreate; //in ms

	private String hasoffersExtNetworkIdCreate = "";
	private String hasoffersExtNetworkTokenCreate = "";
	private String hasoffersExtCategoryNameCreate = "";
	private String hasoffersExtAffIdCreate = "";
	private long hasoffersExtServiceQueryIntervalCreate; //in ms

	private String hasoffersNativexNetworkIdCreate = "";
	private String hasoffersNativexNetworkTokenCreate = "";
	private String hasoffersNativexCategoryNameCreate = "";
	private String hasoffersNativexAffIdCreate = "";
	private long hasoffersNativexServiceQueryIntervalCreate; //in ms

	private String hasoffersVCNetworkIdCreate = "";
	private String hasoffersVCNetworkTokenCreate = "";
	private String hasoffersVCCategoryNameCreate = "";
	private String hasoffersVCAffIdCreate = "";
	private long hasoffersVCServiceQueryIntervalCreate; //in ms

	private String fyberApiIdCreate = "";
	private String fyberApiKeyCreate = "";
	private String offerTypesCreate = "";
	
	private String aarkiPlacementIdCreate = ""; 
	private int aarkiNumberOfPulledOffersCreate = 1000;

	//edit fields
	private String snapdealToken = "";
	private String snapdealId = "";

	private String personalyAppHash = "";
	private int personalyRecordsPerPage = 1000;

	private String trialPayVic = "";
	private int trialPayNmberOfPulledOffers = 0;

	private String clickeyApiUrl = "";
	
	private String woobiIOSApiUrl = "";
	private String woobiAndroidApiUrl = "";

	private String supersonicAccessKey = "";
	private String supersonicApplicationKey = "";
	private String supersonicSecretKey = "";
	private String supersonicPlatform = "";
	private int supersonicNumberOfPulledOffers = 0;
	
	private String minimobApiKey = "";
	private long minimobServiceQueryInterval; //in ms
	
	private String fyberParam1 = "";
	
	private String hasoffersNetworkId = "";
	private String hasoffersNetworkToken = "";
	private String hasoffersAffId = "";
	private String hasoffersGroupName = "";
	private long hasoffersServiceQueryInterval; //in ms

	private String hasoffersExtNetworkId = "";
	private String hasoffersExtCategoryName = "";
	private String hasoffersExtNetworkToken = "";
	private String hasoffersExtAffId = "";
	private long hasoffersExtServiceQueryInterval; //in ms

	private String hasoffersNativexNetworkId = "";
	private String hasoffersNativexCategoryName = "";
	private String hasoffersNativexNetworkToken = "";
	private String hasoffersNativexAffId = "";
	private long hasoffersNativexServiceQueryInterval; //in ms

	private String hasoffersVCNetworkId = "";
	private String hasoffersVCCategoryName = "";
	private String hasoffersVCNetworkToken = "";
	private String hasoffersVCAffId = "";
	private long hasoffersVCServiceQueryInterval; //in ms

	private String fyberApiId = "";
	private String fyberApiKey = "";
	private String offerTypes = "";
	
	private String aarkiPlacementId = ""; 
	private int aarkiNumberOfPulledOffers = 0;

	//creation of new offer providers
	private ArrayList<SelectItem> listOfferProviderTypes = new ArrayList<SelectItem>();
	@Inject
	private DAOOfferWall daoOfferWall;
	@Inject
	private SerDeOfferWallConfiguration serDeOfferWallConfiguration;
	private ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
	
	public AdProviderConfigurationBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

	   //create fake 
	   createdDomain = new AdProviderEntity();
	   createdDomain.setName(" ");
	   createdDomain.setDescription(" ");
	   
	   editedDomain = new AdProviderEntity();
	   editedDomain.setName(" ");
	   editedDomain.setDescription(" ");

	   refresh();
   }
   
	public void refresh() {
		try {
			createOfferProviderTypes();
			logger.info("refreshing bean...");
			List<AdProviderEntity>listDomains = (List<AdProviderEntity>)daoAdProvider.findAllByRealmId(loginBean.getUser().getRealm().getId());
			logger.info("identified domains: "+listDomains.size());
			domainDataModel = new AdProviderDataModelBean(listDomains);

			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idDomainConfiguration");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			listDomains = new ArrayList<AdProviderEntity>();
			domainDataModel = new AdProviderDataModelBean(listDomains);

			//e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}
	
	private void createOfferProviderTypes() {
		listOfferProviderTypes = new ArrayList<SelectItem>();
		listOfferProviderTypes.add(new SelectItem("Select one", "Select one"));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.FYBER.toString(), OfferProviderCodeNames.FYBER.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.TRIALPAY.toString(), OfferProviderCodeNames.TRIALPAY.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.AARKI.toString(), OfferProviderCodeNames.AARKI.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.HASOFFERS.toString(), OfferProviderCodeNames.HASOFFERS.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.HASOFFERS_EXT.toString(), OfferProviderCodeNames.HASOFFERS_EXT.toString()));		
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString(), OfferProviderCodeNames.HASOFFERS_NATIVEX.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.HASOFFERS_VC.toString(), OfferProviderCodeNames.HASOFFERS_VC.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.MINIMOB.toString(), OfferProviderCodeNames.MINIMOB.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.SUPERSONIC.toString(), OfferProviderCodeNames.SUPERSONIC.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.WOOBI_ANDROID.toString(), OfferProviderCodeNames.WOOBI_ANDROID.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.WOOBI_IOS.toString(), OfferProviderCodeNames.WOOBI_IOS.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.CLICKKY.toString(), OfferProviderCodeNames.CLICKKY.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.PERSONALY.toString(), OfferProviderCodeNames.PERSONALY.toString()));
		listOfferProviderTypes.add(new SelectItem(OfferProviderCodeNames.SNAPDEAL.toString(), OfferProviderCodeNames.SNAPDEAL.toString()));
	}

	public void adjustUIForSelectedProviderType() {
		logger.info("adjusting UI for selected provider type: "+createdDomain.getCodeName());
		createdDomain.setTags(createdDomain.getCodeName());
		renderFyber = false;
		renderTrialPay = false;
		renderClickey = false;
		renderWoobiIOS = false;
		renderWoobiAndroid = false;
		renderSupersonic = false;
		renderAarki = false;
		renderMinimob = false;
		renderFyber = false;
		renderHasoffers = false;
		renderHasoffersExt = false;
		renderHasoffersNativex = false;
		renderHasoffersVC = false;
		renderPersonaly = false;
		renderSnapdeal = false;

		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.SNAPDEAL.toString())) {
			renderSnapdeal = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())) {
			renderPersonaly = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())) {
			renderFyber = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())) {
			renderTrialPay = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())) {
			renderClickey = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())) {
			renderWoobiIOS = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())) {
			renderWoobiAndroid = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())) {
			renderSupersonic = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.AARKI.toString())) {
			renderAarki = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())) {
			renderMinimob = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())) {
			renderHasoffers = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())) {
			renderHasoffersExt = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())) {
			renderHasoffersNativex = true;
		}
		if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())) {
			renderHasoffersVC = true;
		}

		RequestContext.getCurrentInstance().update("tabView:idSelectProviderTypeData");
		RequestContext.getCurrentInstance().update("tabView:idSelectProviderDetails");
	}
	
	public void update() {
		logger.info("updating AdProvider configuration: "+editedDomain.getName());
		try {
			//set configuration parameters according to specific AdProvider schemas
			if(editedDomain.getCodeName().equals(OfferProviderCodeNames.SNAPDEAL.toString())){
				//serialise config 
				try {
					configSnapdeal.setId(snapdealId);
					configSnapdeal.setToken(snapdealToken);
					String strConfigContent = serDeSnapdeal.serialize(configSnapdeal);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			if(editedDomain.getCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())){
				//serialise config 
				try {
					configPersonaly.setAppHash(personalyAppHash);
					configPersonaly.setRecordsPerPage(personalyRecordsPerPage);;
					String strConfigContent = serDePersonaly.serialize(configPersonaly);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			if(editedDomain.getCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())){
				//serialise config 
				try {
					configTrialPay.setVic(trialPayVic);;
					configTrialPay.setNumberOfPulledOffers(trialPayNmberOfPulledOffers);;
					String strConfigContent = serDeTrialPay.serialize(configTrialPay);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())){
				//serialise config 
				try {
					configClickey.setApiUrl(clickeyApiUrl);;
					String strConfigContent = serDeClickey.serialize(configClickey);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())){
				//serialise config 
				try {
					configWoobiIOS.setApiUrl(woobiIOSApiUrl);;
					String strConfigContent = serDeWoobi.serialize(configWoobiIOS);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())){
				//serialise config 
				try {
					configWoobiAndroid.setApiUrl(woobiAndroidApiUrl);;
					String strConfigContent = serDeWoobi.serialize(configWoobiAndroid);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())){
				//serialise config 
				try {
					configSupersonic.setAccessKey(supersonicAccessKey);
					configSupersonic.setApplicationKey(supersonicApplicationKey);
					configSupersonic.setSecretKey(supersonicSecretKey);
					configSupersonic.setPlatform(supersonicPlatform);
					configSupersonic.setNumberOfPulledOffers(supersonicNumberOfPulledOffers);
					String strConfigContent = serDeSupersonic.serialize(configSupersonic);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			//set configuration parameters according to specific AdProvider schemas
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.AARKI.toString())){
				//serialise config 
				try {
					configAarki.setPlacementId(aarkiPlacementId);;
					configAarki.setNumberOfPulledOffers(aarkiNumberOfPulledOffers);
					String strConfigContent = serDeAaarki.serialize(configAarki);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			//set configuration parameters according to specific AdProvider schemas
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())){
				//serialise config 
				try {
					configMinimob.setApiKey(minimobApiKey);
					configMinimob.setServiceQueryInterval(minimobServiceQueryInterval);
					String strConfigContent = serDeMinimob.serialize(configMinimob);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())){
				//serialise config 
				try {
					configFyber.setApiId(fyberApiId);
					configFyber.setApiKey(fyberApiKey);
					configFyber.setOfferTypes(offerTypes);
					String strConfigContent = serDeFyber.serialize(configFyber);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())){
				//serialise config 
				try {
					configHasoffers.setNetworkId(hasoffersNetworkId);
					configHasoffers.setNetworkToken(hasoffersNetworkToken);
					configHasoffers.setAffiliateId(hasoffersAffId);;
					configHasoffers.setOfferGroupName(hasoffersGroupName);
					configHasoffers.setServiceQueryInterval(hasoffersServiceQueryInterval);
					String strConfigContent = serDeHasoffers.serialize(configHasoffers);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())){
				//serialise config 
				try {
					configHasoffersExt.setNetworkId(hasoffersExtNetworkId);
					configHasoffersExt.setNetworkToken(hasoffersExtNetworkToken);
					configHasoffersExt.setCategoryName(hasoffersExtCategoryName);
					configHasoffersExt.setAffiliateId(hasoffersExtAffId);
					configHasoffersExt.setServiceQueryInterval(hasoffersExtServiceQueryInterval);
					String strConfigContent = serDeHasoffersExt.serialize(configHasoffersExt);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())){
				//serialise config 
				try {
					configHasoffersNativex.setNetworkId(hasoffersNativexNetworkId);
					configHasoffersNativex.setNetworkToken(hasoffersNativexNetworkToken);
					configHasoffersNativex.setCategoryName(hasoffersNativexCategoryName);
					configHasoffersNativex.setAffiliateId(hasoffersNativexAffId);
					configHasoffersNativex.setServiceQueryInterval(hasoffersNativexServiceQueryInterval);
					String strConfigContent = serDeHasoffersNativex.serialize(configHasoffersNativex);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())){
				//serialise config 
				try {
					configHasoffersVC.setNetworkId(hasoffersVCNetworkId);
					configHasoffersVC.setNetworkToken(hasoffersVCNetworkToken);
					configHasoffersVC.setCategoryName(hasoffersVCCategoryName);
					configHasoffersVC.setAffiliateId(hasoffersVCAffId);
					configHasoffersVC.setServiceQueryInterval(hasoffersVCServiceQueryInterval);
					String strConfigContent = serDeHasoffersVC.serialize(configHasoffersVC);
					editedDomain.setConfiguration(strConfigContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				}
			}
			
			daoAdProvider.createOrUpdate(editedDomain);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Ad provider: "+editedDomain.getName()+" successfully updated"));
			RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			//todo growl display
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to update domain: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			refresh();
		}
	}

	public void setEditedDomain(AdProviderEntity domain) {
		logger.info("setting edited domain: "+domain.getName());
		this.editedDomain = domain;

		//display correct configuration parameters according to specific AdProvider schemas
		if(editedDomain.getCodeName().equals(OfferProviderCodeNames.SNAPDEAL.toString())){
			renderSnapdeal = true;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				logger.info("deserialising: "+editedDomain.getConfiguration());
				configSnapdeal = serDeSnapdeal.deserialize(editedDomain.getConfiguration());
				snapdealId = configSnapdeal.getId();
				snapdealToken = configSnapdeal.getToken();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}		
		if(editedDomain.getCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())){
			renderSnapdeal = false;
			renderPersonaly = true;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				logger.info("deserialising: "+editedDomain.getConfiguration());
				configPersonaly = serDePersonaly.deserialize(editedDomain.getConfiguration());
				personalyAppHash = configPersonaly.getAppHash();
				personalyRecordsPerPage = configPersonaly.getRecordsPerPage();
				logger.info("personaly: "+personalyAppHash+" "+personalyRecordsPerPage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		} if(editedDomain.getCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = true;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configTrialPay = serDeTrialPay.deserialize(editedDomain.getConfiguration());
				trialPayVic = configTrialPay.getVic();
				trialPayNmberOfPulledOffers = configTrialPay.getNumberOfPulledOffers();
				logger.info("vic: "+trialPayVic+" "+trialPayNmberOfPulledOffers);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = true;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configClickey = serDeClickey.deserialize(editedDomain.getConfiguration());
				clickeyApiUrl = configClickey.getApiUrl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = true;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configWoobiIOS = serDeWoobi.deserialize(editedDomain.getConfiguration());
				woobiIOSApiUrl = configWoobiIOS.getApiUrl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = true;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configWoobiAndroid = serDeWoobi.deserialize(editedDomain.getConfiguration());
				woobiAndroidApiUrl = configWoobiAndroid.getApiUrl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		//display correct configuration parameters according to specific AdProvider schemas
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = true;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configSupersonic = serDeSupersonic.deserialize(editedDomain.getConfiguration());
				supersonicAccessKey = configSupersonic.getAccessKey();
				supersonicApplicationKey = configSupersonic.getApplicationKey();
				supersonicPlatform = configSupersonic.getPlatform();
				supersonicSecretKey = configSupersonic.getSecretKey();
				supersonicNumberOfPulledOffers = configSupersonic.getNumberOfPulledOffers();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		//display correct configuration parameters according to specific AdProvider schemas
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.AARKI.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = true;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configAarki = serDeAaarki.deserialize(editedDomain.getConfiguration());
				aarkiNumberOfPulledOffers = configAarki.getNumberOfPulledOffers();
				aarkiPlacementId = configAarki.getPlacementId();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		//display correct configuration parameters according to specific AdProvider schemas
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = true;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configMinimob = serDeMinimob.deserialize(editedDomain.getConfiguration());
				minimobApiKey = configMinimob.getApiKey();
				minimobServiceQueryInterval = configMinimob.getServiceQueryInterval();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = true;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configFyber = serDeFyber.deserialize(editedDomain.getConfiguration());
				fyberApiId = configFyber.getApiId();
				fyberApiKey = configFyber.getApiKey();
				offerTypes = configFyber.getOfferTypes();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = true;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configHasoffers = serDeHasoffers.deserialize(editedDomain.getConfiguration());
				hasoffersNetworkId = configHasoffers.getNetworkId();
				hasoffersNetworkToken = configHasoffers.getNetworkToken();
				hasoffersAffId = configHasoffers.getAffiliateId();
				hasoffersGroupName = configHasoffers.getOfferGroupName();
				hasoffersServiceQueryInterval = configHasoffers.getServiceQueryInterval();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = true;
			renderHasoffersNativex = false;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configHasoffersExt = serDeHasoffersExt.deserialize(editedDomain.getConfiguration());
				hasoffersExtNetworkId = configHasoffersExt.getNetworkId();
				hasoffersExtNetworkToken = configHasoffersExt.getNetworkToken();
				hasoffersExtCategoryName = configHasoffersExt.getCategoryName();
				hasoffersExtAffId = configHasoffersExt.getAffiliateId();
				hasoffersExtServiceQueryInterval = configHasoffersExt.getServiceQueryInterval();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = true;
			renderHasoffersVC = false;
			//deserialise config 
			try {
				configHasoffersNativex = serDeHasoffersNativex.deserialize(editedDomain.getConfiguration());
				hasoffersNativexNetworkId = configHasoffersNativex.getNetworkId();
				hasoffersNativexNetworkToken = configHasoffersNativex.getNetworkToken();
				hasoffersNativexCategoryName = configHasoffersNativex.getCategoryName();
				hasoffersNativexAffId = configHasoffersNativex.getAffiliateId();
				hasoffersNativexServiceQueryInterval = configHasoffersNativex.getServiceQueryInterval();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
		else if(editedDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())){
			renderSnapdeal = false;
			renderPersonaly = false;
			renderTrialPay = false;
			renderClickey = false;
			renderWoobiIOS = false;
			renderWoobiAndroid = false;
			renderSupersonic = false;
			renderAarki = false;
			renderMinimob = false;
			renderFyber = false;
			renderHasoffers = false;
			renderHasoffersExt = false;
			renderHasoffersNativex = false;
			renderHasoffersVC = true;
			//deserialise config 
			try {
				configHasoffersVC = serDeHasoffersVC.deserialize(editedDomain.getConfiguration());
				hasoffersVCNetworkId = configHasoffersVC.getNetworkId();
				hasoffersVCNetworkToken = configHasoffersVC.getNetworkToken();
				hasoffersVCCategoryName = configHasoffersVC.getCategoryName();
				hasoffersVCAffId = configHasoffersVC.getAffiliateId();
				hasoffersVCServiceQueryInterval = configHasoffersVC.getServiceQueryInterval();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to retrieve Ad provider configuration: "+e.toString()));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			}
		}
	}

	public void preCreate() {
		logger.info("creating new object for new offer provider data...");
		createdDomain = new AdProviderEntity();
	}

	public void create() {
		logger.info("creating domain: "+createdDomain.getName()+" type: "+createdDomain.getCodeName());
		
		try {
			boolean exists = false;
			//find if domain with given name inside existing realm does not already exist
			List<AdProviderEntity>listDomains = (List<AdProviderEntity>)daoAdProvider.findAllByRealmId(loginBean.getUser().getRealm().getId());
			for(AdProviderEntity de:listDomains) {
				if(de.getName().trim().equals(createdDomain.getName().trim())){
					exists = true;
				}
			}
			
			if(exists) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Offer provider: "+createdDomain.getName()+" with given name already exists"));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			} else {
				logger.info("creating AdProvider configuration: "+createdDomain.getName());
				try {
					//set configuration parameters according to specific AdProvider schemas
					if(createdDomain.getCodeName().equals(OfferProviderCodeNames.SNAPDEAL.toString())){
						//serialise config 
						try {
							configSnapdeal = new SnapdealProviderConfig();
							configSnapdeal.setId(snapdealIdCreate);
							configSnapdeal.setToken(snapdealTokenCreate);
							String strConfigContent = serDeSnapdeal.serialize(configSnapdeal);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					if(createdDomain.getCodeName().equals(OfferProviderCodeNames.PERSONALY.toString())){
						//serialise config 
						try {
							configPersonaly = new PersonalyProviderConfig();
							configPersonaly.setAppHash(personalyAppHashCreate);
							configPersonaly.setRecordsPerPage(personalyRecordsPerPageCreate);
							String strConfigContent = serDePersonaly.serialize(configPersonaly);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					if(createdDomain.getCodeName().equals(OfferProviderCodeNames.TRIALPAY.toString())){
						//serialise config 
						try {
							configTrialPay = new TrialPayProviderConfig();
							configTrialPay.setVic(trialPayVicCreate);
							configTrialPay.setNumberOfPulledOffers(trialPayNmberOfPulledOffersCreate);
							String strConfigContent = serDeTrialPay.serialize(configTrialPay);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					//set configuration parameters according to specific AdProvider schemas
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.CLICKKY.toString())){
						//serialise config 
						try {
							configClickey = new ClickeyProviderConfig();
							configClickey.setApiUrl(clickeyApiUrlCreate);;
							String strConfigContent = serDeClickey.serialize(configClickey);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_IOS.toString())){
						//serialise config 
						try {
							configWoobiIOS = new WoobiProviderConfig();
							configWoobiIOS.setApiUrl(woobiIOSApiUrlCreate);;
							String strConfigContent = serDeWoobi.serialize(configWoobiIOS);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.WOOBI_ANDROID.toString())){
						//serialise config 
						try {
							configWoobiAndroid = new WoobiProviderConfig();
							configWoobiAndroid.setApiUrl(woobiAndroidApiUrlCreate);;
							String strConfigContent = serDeWoobi.serialize(configWoobiAndroid);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.SUPERSONIC.toString())){
						//serialise config 
						try {
							configSupersonic = new SupersonicProviderConfig();
							configSupersonic.setAccessKey(supersonicAccessKeyCreate);
							configSupersonic.setApplicationKey(supersonicApplicationKeyCreate);
							configSupersonic.setSecretKey(supersonicSecretKeyCreate);
							configSupersonic.setPlatform(supersonicPlatformCreate);
							configSupersonic.setNumberOfPulledOffers(supersonicNumberOfPulledOffersCreate);
							String strConfigContent = serDeSupersonic.serialize(configSupersonic);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					//set configuration parameters according to specific AdProvider schemas
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.AARKI.toString())){
						//serialise config 
						try {
							configAarki = new AarkiProviderConfig();
							configAarki.setPlacementId(aarkiPlacementIdCreate);
							configAarki.setNumberOfPulledOffers(aarkiNumberOfPulledOffersCreate);
							String strConfigContent = serDeAaarki.serialize(configAarki);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					//set configuration parameters according to specific AdProvider schemas
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.MINIMOB.toString())){
						//serialise config 
						try {
							configMinimob = new MinimobProviderConfig();
							configMinimob.setApiKey(minimobApiKeyCreate);
							configMinimob.setServiceQueryInterval(minimobServiceQueryIntervalCreate);
							String strConfigContent = serDeMinimob.serialize(configMinimob);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.FYBER.toString())){
						//serialise config 
						try {
							configFyber = new FyberProviderConfig();
							configFyber.setApiId(fyberApiIdCreate);
							configFyber.setApiKey(fyberApiKeyCreate);
							configFyber.setOfferTypes(offerTypesCreate);
							String strConfigContent = serDeFyber.serialize(configFyber);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS.toString())){
						//serialise config 
						try {
							configHasoffers = new HasoffersProviderConfig();
							configHasoffers.setNetworkId(hasoffersNetworkIdCreate);
							configHasoffers.setNetworkToken(hasoffersNetworkTokenCreate);
							configHasoffers.setAffiliateId(hasoffersAffIdCreate);
							configHasoffers.setOfferGroupName(hasoffersGroupNameCreate);
							configHasoffers.setServiceQueryInterval(hasoffersServiceQueryIntervalCreate);
							String strConfigContent = serDeHasoffers.serialize(configHasoffers);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_EXT.toString())){
						//serialise config 
						try {
							configHasoffersExt = new HasoffersExtProviderConfig();
							configHasoffersExt.setNetworkId(hasoffersExtNetworkIdCreate);
							configHasoffersExt.setNetworkToken(hasoffersExtNetworkTokenCreate);
							configHasoffersExt.setCategoryName(hasoffersExtCategoryNameCreate);
							configHasoffersExt.setAffiliateId(hasoffersExtAffIdCreate);
							configHasoffersExt.setServiceQueryInterval(hasoffersExtServiceQueryIntervalCreate);
							String strConfigContent = serDeHasoffersExt.serialize(configHasoffersExt);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_NATIVEX.toString())){
						//serialise config 
						try {
							configHasoffersNativex = new HasoffersNativexProviderConfig();
							configHasoffersNativex.setNetworkId(hasoffersNativexNetworkIdCreate);
							configHasoffersNativex.setNetworkToken(hasoffersNativexNetworkTokenCreate);
							configHasoffersNativex.setCategoryName(hasoffersNativexCategoryNameCreate);
							configHasoffersNativex.setAffiliateId(hasoffersNativexAffIdCreate);
							configHasoffersNativex.setServiceQueryInterval(hasoffersNativexServiceQueryIntervalCreate);
							String strConfigContent = serDeHasoffersNativex.serialize(configHasoffersNativex);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					else if(createdDomain.getCodeName().equals(OfferProviderCodeNames.HASOFFERS_VC.toString())){
						//serialise config 
						try {
							configHasoffersVC = new HasoffersVCProviderConfig();
							configHasoffersVC.setNetworkId(hasoffersVCNetworkIdCreate);
							configHasoffersVC.setNetworkToken(hasoffersVCNetworkTokenCreate);
							configHasoffersVC.setCategoryName(hasoffersVCCategoryNameCreate);
							configHasoffersVC.setAffiliateId(hasoffersVCAffIdCreate);
							configHasoffersVC.setServiceQueryInterval(hasoffersVCServiceQueryIntervalCreate);
							String strConfigContent = serDeHasoffersVC.serialize(configHasoffersVC);
							createdDomain.setConfiguration(strConfigContent);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to serialize Ad provider configuration: "+e.toString()));
							RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
						}
					}
					
					createdDomain.setRealm(loginBean.getUser().getRealm());
					createdDomain = daoAdProvider.createOrUpdate(createdDomain);
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer provider: "+createdDomain.getName()+" created."));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");

					//reset values for next domain creation
					createdDomain = new AdProviderEntity();
					createdDomain.setName(" ");
					createdDomain.setDescription("");
					
					//todo growl message
					refresh();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create offer provider: "+e.toString()));
					RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
					refresh();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to create domain: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
		}
	}

	public void delete() {
		logger.info("deleting domain: "+editedDomain.getName()+" desc: "+editedDomain.getName());
		
		try {
			boolean isProviderAssignedToOfferWall = false;
			List<OfferWallEntity> listOfferWalls = daoOfferWall.findAllByRealmId(loginBean.getUser().getRealm().getId());
			for(int i=0;i<listOfferWalls.size();i++) {
				OfferWallEntity ow = listOfferWalls.get(i);
				OfferWallConfiguration offerWallConfiguration = serDeOfferWallConfiguration.deserialize(ow.getConfiguration());
				ArrayList<SingleOfferWallConfiguration> listSingleOW = offerWallConfiguration.getConfigurations();
				for(int j=0;j<listSingleOW.size();j++) {
					SingleOfferWallConfiguration soc = listSingleOW.get(j);
					if(soc.getAdProviderConfigurationName().equals(editedDomain.getName())) {
						logger.info("! identified offer wall configuration relying on provider config: "+soc.getName()+" in ow: "+ow.getName());
						isProviderAssignedToOfferWall = true;
						break;
					}
				}
			}
			
			if(isProviderAssignedToOfferWall) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Unable to delete offer provider as it there exist offer walls that use it. Please adjust offer wall configuration first."));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			} 
			else {
				editedDomain = daoAdProvider.findById(editedDomain.getId());
				editedDomain.setRealm(null);
				editedDomain = daoAdProvider.createOrUpdate(editedDomain);
				logger.info("Deleting domain: "+editedDomain.getName()+" id: "+editedDomain.getId());
				daoAdProvider.delete(editedDomain);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Offer provider: "+editedDomain.getName()+" successfully deleted."));
				RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
				refresh();
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to delete offer provider: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idDomainConfigurationGrowl");
			refresh();
		}
	}

	public AdProviderDataModelBean getDomainDataModel() {
		return domainDataModel;
	}

	public void setDomainDataModel(AdProviderDataModelBean domainDataModel) {
		this.domainDataModel = domainDataModel;
	}

	public AdProviderEntity getCreatedDomain() {
		return createdDomain;
	}

	public void setCreatedDomain(AdProviderEntity createdDomain) {
		this.createdDomain = createdDomain;
	}

	public AdProviderEntity getEditedDomain() {
		return editedDomain;
	}

	
	public String getMinimobApiKey() {
		return minimobApiKey;
	}

	public void setMinimobApiKey(String minimobApiKey) {
		this.minimobApiKey = minimobApiKey;
	}

	public String getFyberParam1() {
		return fyberParam1;
	}

	public void setFyberParam1(String fyberParam1) {
		this.fyberParam1 = fyberParam1;
	}

	public boolean isRenderMinimob() {
		return renderMinimob;
	}

	public void setRenderMinimob(boolean renderMinimob) {
		this.renderMinimob = renderMinimob;
	}

	public boolean isRenderFyber() {
		return renderFyber;
	}

	public void setRenderFyber(boolean renderFyber) {
		this.renderFyber = renderFyber;
	}

	public boolean isRenderHasoffers() {
		return renderHasoffers;
	}

	public void setRenderHasoffers(boolean renderHasoffers) {
		this.renderHasoffers = renderHasoffers;
	}

	public String getHasoffersNetworkId() {
		return hasoffersNetworkId;
	}

	public void setHasoffersNetworkId(String hasoffersNetworkId) {
		this.hasoffersNetworkId = hasoffersNetworkId;
	}

	public String getHasoffersNetworkToken() {
		return hasoffersNetworkToken;
	}

	public void setHasoffersNetworkToken(String hasoffersNetworkToken) {
		this.hasoffersNetworkToken = hasoffersNetworkToken;
	}

	public String getFyberApiId() {
		return fyberApiId;
	}

	public void setFyberApiId(String fyberApiId) {
		this.fyberApiId = fyberApiId;
	}

	public String getFyberApiKey() {
		return fyberApiKey;
	}

	public void setFyberApiKey(String fyberApiKey) {
		this.fyberApiKey = fyberApiKey;
	}

	public String getOfferTypes() {
		return offerTypes;
	}

	public void setOfferTypes(String offerTypes) {
		this.offerTypes = offerTypes;
	}

	public String getAarkiPlacementId() {
		return aarkiPlacementId;
	}

	public void setAarkiPlacementId(String aarkiPlacementId) {
		this.aarkiPlacementId = aarkiPlacementId;
	}

	public int getAarkiNumberOfPulledOffers() {
		return aarkiNumberOfPulledOffers;
	}

	public void setAarkiNumberOfPulledOffers(int aarkiNumberOfPulledOffers) {
		this.aarkiNumberOfPulledOffers = aarkiNumberOfPulledOffers;
	}

	public boolean isRenderAarki() {
		return renderAarki;
	}

	public void setRenderAarki(boolean renderAarki) {
		this.renderAarki = renderAarki;
	}

	public long getMinimobServiceQueryInterval() {
		return minimobServiceQueryInterval;
	}

	public void setMinimobServiceQueryInterval(long minimobServiceQueryInterval) {
		this.minimobServiceQueryInterval = minimobServiceQueryInterval;
	}

	public long getHasoffersServiceQueryInterval() {
		return hasoffersServiceQueryInterval;
	}

	public void setHasoffersServiceQueryInterval(long hasoffersServiceQueryInterval) {
		this.hasoffersServiceQueryInterval = hasoffersServiceQueryInterval;
	}

	public boolean isRenderSupersonic() {
		return renderSupersonic;
	}

	public void setRenderSupersonic(boolean renderSupersonic) {
		this.renderSupersonic = renderSupersonic;
	}

	public String getSupersonicAccessKey() {
		return supersonicAccessKey;
	}

	public void setSupersonicAccessKey(String supersonicAccessKey) {
		this.supersonicAccessKey = supersonicAccessKey;
	}

	public String getSupersonicApplicationKey() {
		return supersonicApplicationKey;
	}

	public void setSupersonicApplicationKey(String supersonicApplicationKey) {
		this.supersonicApplicationKey = supersonicApplicationKey;
	}

	public String getSupersonicSecretKey() {
		return supersonicSecretKey;
	}

	public void setSupersonicSecretKey(String supersonicSecretKey) {
		this.supersonicSecretKey = supersonicSecretKey;
	}

	public String getSupersonicPlatform() {
		return supersonicPlatform;
	}

	public void setSupersonicPlatform(String supersonicPlatform) {
		this.supersonicPlatform = supersonicPlatform;
	}

	public int getSupersonicNumberOfPulledOffers() {
		return supersonicNumberOfPulledOffers;
	}

	public void setSupersonicNumberOfPulledOffers(int supersonicNumberOfPulledOffers) {
		this.supersonicNumberOfPulledOffers = supersonicNumberOfPulledOffers;
	}

	public boolean isRenderWoobiAndroid() {
		return renderWoobiAndroid;
	}

	public void setRenderWoobiAndroid(boolean renderWoobiAndroid) {
		this.renderWoobiAndroid = renderWoobiAndroid;
	}

	public String getWoobiAndroidApiUrl() {
		return woobiAndroidApiUrl;
	}

	public void setWoobiAndroidApiUrl(String woobiAndroidApiUrl) {
		this.woobiAndroidApiUrl = woobiAndroidApiUrl;
	}

	public boolean isRenderWoobiIOS() {
		return renderWoobiIOS;
	}

	public void setRenderWoobiIOS(boolean renderWoobiIOS) {
		this.renderWoobiIOS = renderWoobiIOS;
	}

	public String getWoobiIOSApiUrl() {
		return woobiIOSApiUrl;
	}

	public void setWoobiIOSApiUrl(String woobiIOSApiUrl) {
		this.woobiIOSApiUrl = woobiIOSApiUrl;
	}

	public ArrayList<SelectItem> getListOfferProviderTypes() {
		return listOfferProviderTypes;
	}

	public void setListOfferProviderTypes(
			ArrayList<SelectItem> listOfferProviderTypes) {
		this.listOfferProviderTypes = listOfferProviderTypes;
	}

	public String getWoobiIOSApiUrlCreate() {
		return woobiIOSApiUrlCreate;
	}

	public void setWoobiIOSApiUrlCreate(String woobiIOSApiUrlCreate) {
		this.woobiIOSApiUrlCreate = woobiIOSApiUrlCreate;
	}

	public String getWoobiAndroidApiUrlCreate() {
		return woobiAndroidApiUrlCreate;
	}

	public void setWoobiAndroidApiUrlCreate(String woobiAndroidApiUrlCreate) {
		this.woobiAndroidApiUrlCreate = woobiAndroidApiUrlCreate;
	}

	public String getSupersonicAccessKeyCreate() {
		return supersonicAccessKeyCreate;
	}

	public void setSupersonicAccessKeyCreate(String supersonicAccessKeyCreate) {
		this.supersonicAccessKeyCreate = supersonicAccessKeyCreate;
	}

	public String getSupersonicApplicationKeyCreate() {
		return supersonicApplicationKeyCreate;
	}

	public void setSupersonicApplicationKeyCreate(
			String supersonicApplicationKeyCreate) {
		this.supersonicApplicationKeyCreate = supersonicApplicationKeyCreate;
	}

	public String getSupersonicSecretKeyCreate() {
		return supersonicSecretKeyCreate;
	}

	public void setSupersonicSecretKeyCreate(String supersonicSecretKeyCreate) {
		this.supersonicSecretKeyCreate = supersonicSecretKeyCreate;
	}

	public String getSupersonicPlatformCreate() {
		return supersonicPlatformCreate;
	}

	public void setSupersonicPlatformCreate(String supersonicPlatformCreate) {
		this.supersonicPlatformCreate = supersonicPlatformCreate;
	}

	public int getSupersonicNumberOfPulledOffersCreate() {
		return supersonicNumberOfPulledOffersCreate;
	}

	public void setSupersonicNumberOfPulledOffersCreate(
			int supersonicNumberOfPulledOffersCreate) {
		this.supersonicNumberOfPulledOffersCreate = supersonicNumberOfPulledOffersCreate;
	}

	public String getMinimobApiKeyCreate() {
		return minimobApiKeyCreate;
	}

	public void setMinimobApiKeyCreate(String minimobApiKeyCreate) {
		this.minimobApiKeyCreate = minimobApiKeyCreate;
	}

	public long getMinimobServiceQueryIntervalCreate() {
		return minimobServiceQueryIntervalCreate;
	}

	public void setMinimobServiceQueryIntervalCreate(
			long minimobServiceQueryIntervalCreate) {
		this.minimobServiceQueryIntervalCreate = minimobServiceQueryIntervalCreate;
	}

	public String getFyberParam1Create() {
		return fyberParam1Create;
	}

	public void setFyberParam1Create(String fyberParam1Create) {
		this.fyberParam1Create = fyberParam1Create;
	}

	public String getHasoffersNetworkIdCreate() {
		return hasoffersNetworkIdCreate;
	}

	public void setHasoffersNetworkIdCreate(String hasoffersNetworkIdCreate) {
		this.hasoffersNetworkIdCreate = hasoffersNetworkIdCreate;
	}

	public String getHasoffersNetworkTokenCreate() {
		return hasoffersNetworkTokenCreate;
	}

	public void setHasoffersNetworkTokenCreate(String hasoffersNetworkTokenCreate) {
		this.hasoffersNetworkTokenCreate = hasoffersNetworkTokenCreate;
	}

	public long getHasoffersServiceQueryIntervalCreate() {
		return hasoffersServiceQueryIntervalCreate;
	}

	public void setHasoffersServiceQueryIntervalCreate(
			long hasoffersServiceQueryIntervalCreate) {
		this.hasoffersServiceQueryIntervalCreate = hasoffersServiceQueryIntervalCreate;
	}

	public String getFyberApiIdCreate() {
		return fyberApiIdCreate;
	}

	public void setFyberApiIdCreate(String fyberApiIdCreate) {
		this.fyberApiIdCreate = fyberApiIdCreate;
	}

	public String getFyberApiKeyCreate() {
		return fyberApiKeyCreate;
	}

	public void setFyberApiKeyCreate(String fyberApiKeyCreate) {
		this.fyberApiKeyCreate = fyberApiKeyCreate;
	}

	public String getOfferTypesCreate() {
		return offerTypesCreate;
	}

	public void setOfferTypesCreate(String offerTypesCreate) {
		this.offerTypesCreate = offerTypesCreate;
	}

	public String getAarkiPlacementIdCreate() {
		return aarkiPlacementIdCreate;
	}

	public void setAarkiPlacementIdCreate(String aarkiPlacementIdCreate) {
		this.aarkiPlacementIdCreate = aarkiPlacementIdCreate;
	}

	public int getAarkiNumberOfPulledOffersCreate() {
		return aarkiNumberOfPulledOffersCreate;
	}

	public void setAarkiNumberOfPulledOffersCreate(
			int aarkiNumberOfPulledOffersCreate) {
		this.aarkiNumberOfPulledOffersCreate = aarkiNumberOfPulledOffersCreate;
	}

	public String getHasoffersAffId() {
		return hasoffersAffId;
	}

	public void setHasoffersAffId(String hasoffersAffId) {
		this.hasoffersAffId = hasoffersAffId;
	}

	public String getHasoffersExtNetworkIdCreate() {
		return hasoffersExtNetworkIdCreate;
	}

	public void setHasoffersExtNetworkIdCreate(String hasoffersExtNetworkIdCreate) {
		this.hasoffersExtNetworkIdCreate = hasoffersExtNetworkIdCreate;
	}

	public String getHasoffersExtNetworkTokenCreate() {
		return hasoffersExtNetworkTokenCreate;
	}

	public void setHasoffersExtNetworkTokenCreate(
			String hasoffersExtNetworkTokenCreate) {
		this.hasoffersExtNetworkTokenCreate = hasoffersExtNetworkTokenCreate;
	}

	public String getHasoffersExtAffId() {
		return hasoffersExtAffId;
	}

	public void setHasoffersExtAffId(String hasoffersExtAffId) {
		this.hasoffersExtAffId = hasoffersExtAffId;
	}

	public long getHasoffersExtServiceQueryIntervalCreate() {
		return hasoffersExtServiceQueryIntervalCreate;
	}

	public void setHasoffersExtServiceQueryIntervalCreate(
			long hasoffersExtServiceQueryIntervalCreate) {
		this.hasoffersExtServiceQueryIntervalCreate = hasoffersExtServiceQueryIntervalCreate;
	}

	public String getHasoffersExtNetworkId() {
		return hasoffersExtNetworkId;
	}

	public void setHasoffersExtNetworkId(String hasoffersExtNetworkId) {
		this.hasoffersExtNetworkId = hasoffersExtNetworkId;
	}

	public String getHasoffersExtNetworkToken() {
		return hasoffersExtNetworkToken;
	}

	public void setHasoffersExtNetworkToken(String hasoffersExtNetworkToken) {
		this.hasoffersExtNetworkToken = hasoffersExtNetworkToken;
	}

	public long getHasoffersExtServiceQueryInterval() {
		return hasoffersExtServiceQueryInterval;
	}

	public void setHasoffersExtServiceQueryInterval(
			long hasoffersExtServiceQueryInterval) {
		this.hasoffersExtServiceQueryInterval = hasoffersExtServiceQueryInterval;
	}

	public boolean isRenderHasoffersExt() {
		return renderHasoffersExt;
	}

	public void setRenderHasoffersExt(boolean renderHasoffersExt) {
		this.renderHasoffersExt = renderHasoffersExt;
	}

	public String getHasoffersExtCategoryNameCreate() {
		return hasoffersExtCategoryNameCreate;
	}

	public void setHasoffersExtCategoryNameCreate(
			String hasoffersExtCategoryNameCreate) {
		this.hasoffersExtCategoryNameCreate = hasoffersExtCategoryNameCreate;
	}

	public String getHasoffersExtCategoryName() {
		return hasoffersExtCategoryName;
	}

	public void setHasoffersExtCategoryName(String hasoffersExtCategoryName) {
		this.hasoffersExtCategoryName = hasoffersExtCategoryName;
	}

	public String getHasoffersExtAffIdCreate() {
		return hasoffersExtAffIdCreate;
	}

	public void setHasoffersExtAffIdCreate(String hasoffersExtAffIdCreate) {
		this.hasoffersExtAffIdCreate = hasoffersExtAffIdCreate;
	}

	public boolean isRenderClickey() {
		return renderClickey;
	}

	public void setRenderClickey(boolean renderClickey) {
		this.renderClickey = renderClickey;
	}

	public String getClickeyApiUrlCreate() {
		return clickeyApiUrlCreate;
	}

	public void setClickeyApiUrlCreate(String clickeyApiUrlCreate) {
		this.clickeyApiUrlCreate = clickeyApiUrlCreate;
	}

	public String getClickeyApiUrl() {
		return clickeyApiUrl;
	}

	public void setClickeyApiUrl(String clickeyApiUrl) {
		this.clickeyApiUrl = clickeyApiUrl;
	}

	public String getHasoffersAffIdCreate() {
		return hasoffersAffIdCreate;
	}

	public void setHasoffersAffIdCreate(String hasoffersAffIdCreate) {
		this.hasoffersAffIdCreate = hasoffersAffIdCreate;
	}

	public String getHasoffersGroupNameCreate() {
		return hasoffersGroupNameCreate;
	}

	public void setHasoffersGroupNameCreate(String hasoffersGroupNameCreate) {
		this.hasoffersGroupNameCreate = hasoffersGroupNameCreate;
	}

	public String getHasoffersGroupName() {
		return hasoffersGroupName;
	}

	public void setHasoffersGroupName(String hasoffersGroupName) {
		this.hasoffersGroupName = hasoffersGroupName;
	}

	public String getTrialPayVicCreate() {
		return trialPayVicCreate;
	}

	public void setTrialPayVicCreate(String trialPayVicCreate) {
		this.trialPayVicCreate = trialPayVicCreate;
	}

	public int getTrialPayNmberOfPulledOffersCreate() {
		return trialPayNmberOfPulledOffersCreate;
	}

	public void setTrialPayNmberOfPulledOffersCreate(
			int trialPayNmberOfPulledOffersCreate) {
		this.trialPayNmberOfPulledOffersCreate = trialPayNmberOfPulledOffersCreate;
	}

	public String getTrialPayVic() {
		return trialPayVic;
	}

	public void setTrialPayVic(String trialPayVic) {
		this.trialPayVic = trialPayVic;
	}

	public int getTrialPayNmberOfPulledOffers() {
		return trialPayNmberOfPulledOffers;
	}

	public void setTrialPayNmberOfPulledOffers(int trialPayNmberOfPulledOffers) {
		this.trialPayNmberOfPulledOffers = trialPayNmberOfPulledOffers;
	}

	public boolean isRenderTrialPay() {
		return renderTrialPay;
	}

	public void setRenderTrialPay(boolean renderTrialPay) {
		this.renderTrialPay = renderTrialPay;
	}

	public boolean isRenderHasoffersNativex() {
		return renderHasoffersNativex;
	}

	public void setRenderHasoffersNativex(boolean renderHasoffersNativex) {
		this.renderHasoffersNativex = renderHasoffersNativex;
	}

	public String getHasoffersNativexNetworkIdCreate() {
		return hasoffersNativexNetworkIdCreate;
	}

	public void setHasoffersNativexNetworkIdCreate(
			String hasoffersNativexNetworkIdCreate) {
		this.hasoffersNativexNetworkIdCreate = hasoffersNativexNetworkIdCreate;
	}

	public String getHasoffersNativexNetworkTokenCreate() {
		return hasoffersNativexNetworkTokenCreate;
	}

	public void setHasoffersNativexNetworkTokenCreate(
			String hasoffersNativexNetworkTokenCreate) {
		this.hasoffersNativexNetworkTokenCreate = hasoffersNativexNetworkTokenCreate;
	}

	public String getHasoffersNativexCategoryNameCreate() {
		return hasoffersNativexCategoryNameCreate;
	}

	public void setHasoffersNativexCategoryNameCreate(
			String hasoffersNativexCategoryNameCreate) {
		this.hasoffersNativexCategoryNameCreate = hasoffersNativexCategoryNameCreate;
	}

	public String getHasoffersNativexAffIdCreate() {
		return hasoffersNativexAffIdCreate;
	}

	public void setHasoffersNativexAffIdCreate(String hasoffersNativexAffIdCreate) {
		this.hasoffersNativexAffIdCreate = hasoffersNativexAffIdCreate;
	}

	public long getHasoffersNativexServiceQueryIntervalCreate() {
		return hasoffersNativexServiceQueryIntervalCreate;
	}

	public void setHasoffersNativexServiceQueryIntervalCreate(
			long hasoffersNativexServiceQueryIntervalCreate) {
		this.hasoffersNativexServiceQueryIntervalCreate = hasoffersNativexServiceQueryIntervalCreate;
	}

	public String getHasoffersNativexNetworkId() {
		return hasoffersNativexNetworkId;
	}

	public void setHasoffersNativexNetworkId(String hasoffersNativexNetworkId) {
		this.hasoffersNativexNetworkId = hasoffersNativexNetworkId;
	}

	public String getHasoffersNativexCategoryName() {
		return hasoffersNativexCategoryName;
	}

	public void setHasoffersNativexCategoryName(String hasoffersNativexCategoryName) {
		this.hasoffersNativexCategoryName = hasoffersNativexCategoryName;
	}

	public String getHasoffersNativexNetworkToken() {
		return hasoffersNativexNetworkToken;
	}

	public void setHasoffersNativexNetworkToken(String hasoffersNativexNetworkToken) {
		this.hasoffersNativexNetworkToken = hasoffersNativexNetworkToken;
	}

	public String getHasoffersNativexAffId() {
		return hasoffersNativexAffId;
	}

	public void setHasoffersNativexAffId(String hasoffersNativexAffId) {
		this.hasoffersNativexAffId = hasoffersNativexAffId;
	}

	public long getHasoffersNativexServiceQueryInterval() {
		return hasoffersNativexServiceQueryInterval;
	}

	public void setHasoffersNativexServiceQueryInterval(
			long hasoffersNativexServiceQueryInterval) {
		this.hasoffersNativexServiceQueryInterval = hasoffersNativexServiceQueryInterval;
	}

	public String getHasoffersVCNetworkIdCreate() {
		return hasoffersVCNetworkIdCreate;
	}

	public void setHasoffersVCNetworkIdCreate(String hasoffersVCNetworkIdCreate) {
		this.hasoffersVCNetworkIdCreate = hasoffersVCNetworkIdCreate;
	}

	public String getHasoffersVCNetworkTokenCreate() {
		return hasoffersVCNetworkTokenCreate;
	}

	public void setHasoffersVCNetworkTokenCreate(
			String hasoffersVCNetworkTokenCreate) {
		this.hasoffersVCNetworkTokenCreate = hasoffersVCNetworkTokenCreate;
	}

	public String getHasoffersVCCategoryNameCreate() {
		return hasoffersVCCategoryNameCreate;
	}

	public void setHasoffersVCCategoryNameCreate(
			String hasoffersVCCategoryNameCreate) {
		this.hasoffersVCCategoryNameCreate = hasoffersVCCategoryNameCreate;
	}

	public String getHasoffersVCAffIdCreate() {
		return hasoffersVCAffIdCreate;
	}

	public void setHasoffersVCAffIdCreate(String hasoffersVCAffIdCreate) {
		this.hasoffersVCAffIdCreate = hasoffersVCAffIdCreate;
	}

	public long getHasoffersVCServiceQueryIntervalCreate() {
		return hasoffersVCServiceQueryIntervalCreate;
	}

	public void setHasoffersVCServiceQueryIntervalCreate(
			long hasoffersVCServiceQueryIntervalCreate) {
		this.hasoffersVCServiceQueryIntervalCreate = hasoffersVCServiceQueryIntervalCreate;
	}

	public boolean isRenderHasoffersVC() {
		return renderHasoffersVC;
	}

	public void setRenderHasoffersVC(boolean renderHasoffersVC) {
		this.renderHasoffersVC = renderHasoffersVC;
	}

	public String getHasoffersVCNetworkId() {
		return hasoffersVCNetworkId;
	}

	public void setHasoffersVCNetworkId(String hasoffersVCNetworkId) {
		this.hasoffersVCNetworkId = hasoffersVCNetworkId;
	}

	public String getHasoffersVCCategoryName() {
		return hasoffersVCCategoryName;
	}

	public void setHasoffersVCCategoryName(String hasoffersVCCategoryName) {
		this.hasoffersVCCategoryName = hasoffersVCCategoryName;
	}

	public String getHasoffersVCNetworkToken() {
		return hasoffersVCNetworkToken;
	}

	public void setHasoffersVCNetworkToken(String hasoffersVCNetworkToken) {
		this.hasoffersVCNetworkToken = hasoffersVCNetworkToken;
	}

	public String getHasoffersVCAffId() {
		return hasoffersVCAffId;
	}

	public void setHasoffersVCAffId(String hasoffersVCAffId) {
		this.hasoffersVCAffId = hasoffersVCAffId;
	}

	public long getHasoffersVCServiceQueryInterval() {
		return hasoffersVCServiceQueryInterval;
	}

	public void setHasoffersVCServiceQueryInterval(
			long hasoffersVCServiceQueryInterval) {
		this.hasoffersVCServiceQueryInterval = hasoffersVCServiceQueryInterval;
	}

	public boolean isRenderPersonaly() {
		return renderPersonaly;
	}

	public void setRenderPersonaly(boolean renderPersonaly) {
		this.renderPersonaly = renderPersonaly;
	}

	public String getPersonalyAppHashCreate() {
		return personalyAppHashCreate;
	}

	public void setPersonalyAppHashCreate(String personalyAppHashCreate) {
		this.personalyAppHashCreate = personalyAppHashCreate;
	}

	public int getPersonalyRecordsPerPageCreate() {
		return personalyRecordsPerPageCreate;
	}

	public void setPersonalyRecordsPerPageCreate(int personalyRecordsPerPageCreate) {
		this.personalyRecordsPerPageCreate = personalyRecordsPerPageCreate;
	}

	public String getPersonalyAppHash() {
		return personalyAppHash;
	}

	public void setPersonalyAppHash(String personalyAppHash) {
		this.personalyAppHash = personalyAppHash;
	}

	public int getPersonalyRecordsPerPage() {
		return personalyRecordsPerPage;
	}

	public void setPersonalyRecordsPerPage(int personalyRecordsPerPage) {
		this.personalyRecordsPerPage = personalyRecordsPerPage;
	}

	public boolean isRenderSnapdeal() {
		return renderSnapdeal;
	}

	public void setRenderSnapdeal(boolean renderSnapdeal) {
		this.renderSnapdeal = renderSnapdeal;
	}

	public String getSnapdealTokenCreate() {
		return snapdealTokenCreate;
	}

	public void setSnapdealTokenCreate(String snapdealTokenCreate) {
		this.snapdealTokenCreate = snapdealTokenCreate;
	}

	public String getSnapdealIdCreate() {
		return snapdealIdCreate;
	}

	public void setSnapdealIdCreate(String snapdealIdCreate) {
		this.snapdealIdCreate = snapdealIdCreate;
	}

	public String getSnapdealToken() {
		return snapdealToken;
	}

	public void setSnapdealToken(String snapdealToken) {
		this.snapdealToken = snapdealToken;
	}

	public String getSnapdealId() {
		return snapdealId;
	}

	public void setSnapdealId(String snapdealId) {
		this.snapdealId = snapdealId;
	}

	
	
	
}

