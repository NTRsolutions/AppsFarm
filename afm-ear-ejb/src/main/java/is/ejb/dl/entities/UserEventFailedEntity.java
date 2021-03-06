package is.ejb.dl.entities;

import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
@Table(name = "UserEventFailed")
public class UserEventFailedEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;
   
   @NotNull
   //@Size(min = 1, max = 25)
   //@Column(unique = true)
   private int userId; //unique user id (provided by AirRewardz app) For mobile app tracking, the DEVICE ID of the Android device hashed with SHA1 algorithm. The value should be generated based on the original value lower case. sha1(aaaaaaaa1111111).)

   private String deviceType;
   private String deviceId;
   private String idfa;
   private String iosDeviceToken;
   private String androidDeviceToken;
   private String advertisingId; //aka gaid
   private String carrierName; //name of telco that manages the phone number 

   @Column(unique=true)
   private String internalTransactionId; //(provided by AirRewardz app) and passed during user click event - this has to be unique for every transaction!  
   private String offerId; //id of an offer (we first search if the user hasn't clicked on the same offer before)
   private String offerSourceId; 
   private String transactionId; //id of the transaction generated by Ad provider (e.g, Hasoffers)
   private int realmId;

   private String countryCode;
   
   @Lob
   @Column(length=148576) 
   private String offerRedirectUrl; //url that mobile app will redirect user to (this url is augmented by the server with additional params)
   private String offerTitle; 
   private String adProviderCodeName;
   private String applicationName;
   private String rewardName; //for example: airtime, offer 1 name, offer 2 name
   private String rewardTypeName; //this links the event with one of denomination models assigned to this particular reward (there may be multiple denomination models for the same reward type only if they have different source payout currency code)

   private String phoneNumber;  
   private String phoneNumberExt; 
   private String loginName; //user login name
   private String email;
   
   private String rewardRequestStatus;
   @Lob
   @Column(length=148576) 
   private String rewardRequestStatusMessage; //message provided by mode when responding with reward confirmation (SUCCESS/FAILED)
   private String rewardResponseStatus;
   @Lob
   @Column(length=148576) 
   private String rewardResponseStatusMessage; //message provided by mode when responding with reward confirmation (SUCCESS/FAILED)

   private String mobileAppNotificationStatus; //message provided by AR when sending notification update (SUCCESS/FAILED)
   @Lob
   @Column(length=148576) 
   private String mobileAppNotificationStatusMessage; //message provided by AR during notification update

   private double offerPayout = 0;
   private String offerPayoutIsoCurrencyCode = "";

   private double offerPayoutInTargetCurrency = 0;
   private String offerPayoutInTargetCurrencyIsoCurrencyCode = "";

   private double profilSplitFraction = 0; //what is profit split fraction (e.g, 0.3 of reward goes to user -> hence 0.7 to us) 
   private double profitValue = 0; //how much we profited
   private double revenueValue = 0; //how much we profited
   private double rewardValue = 0; //how much we paid a user
   private String rewardIsoCurrencyCode = ""; 
   
   private Timestamp clickDate = null; 
   private Timestamp conversionDate = null;
   private Timestamp rewardRequestDate = null;
   private Timestamp rewardDate = null;
   private Timestamp mobileAppNotificationDate = null;
   
   private boolean approved = false;
   private boolean instant; //if instant - reward is generated instantly, otherwise earned payout goes to wallet 
   //web based click info
   private String afaNetworkName; //ssid for cloutrax network from which click traffic was intercepted
   
   //valide defined by UserEventCategory enum and can be of type: [install, invite]
   private String userEventCategory;
   
   private boolean testMode = false;

   private String ipAddress="";  

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}

	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public Timestamp getClickDate() {
		return clickDate;
	}

	public void setClickDate(Timestamp clickDate) {
		this.clickDate = clickDate;
	}

	public Timestamp getConversionDate() {
		return conversionDate;
	}

	public void setConversionDate(Timestamp conversionDate) {
		this.conversionDate = conversionDate;
	}

	public Timestamp getRewardDate() {
		return rewardDate;
	}

	public void setRewardDate(Timestamp rewardDate) {
		this.rewardDate = rewardDate;
	}

	public String getOfferTitle() {
		return offerTitle;
	}

	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	public String getInternalTransactionId() {
		return internalTransactionId;
	}

	public void setInternalTransactionId(String internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}

	public Timestamp getRewardRequestDate() {
		return rewardRequestDate;
	}

	public void setRewardRequestDate(Timestamp rewardRequestDate) {
		this.rewardRequestDate = rewardRequestDate;
	}

	public Timestamp getMobileAppNotificationDate() {
		return mobileAppNotificationDate;
	}

	public void setMobileAppNotificationDate(Timestamp mobileAppNotificationDate) {
		this.mobileAppNotificationDate = mobileAppNotificationDate;
	}

	public String getRewardRequestStatus() {
		return rewardRequestStatus;
	}

	public void setRewardRequestStatus(String rewardRequestStatus) {
		this.rewardRequestStatus = rewardRequestStatus;
	}

	public String getRewardResponseStatus() {
		return rewardResponseStatus;
	}

	public void setRewardResponseStatus(String rewardResponseStatus) {
		this.rewardResponseStatus = rewardResponseStatus;
	}

	public String getRewardRequestStatusMessage() {
		return rewardRequestStatusMessage;
	}

	public void setRewardRequestStatusMessage(String rewardRequestStatusMessage) {
		this.rewardRequestStatusMessage = rewardRequestStatusMessage;
	}

	public String getRewardResponseStatusMessage() {
		return rewardResponseStatusMessage;
	}

	public void setRewardResponseStatusMessage(String rewardResponseStatusMessage) {
		this.rewardResponseStatusMessage = rewardResponseStatusMessage;
	}

	public String getMobileAppNotificationStatus() {
		return mobileAppNotificationStatus;
	}

	public void setMobileAppNotificationStatus(String mobileAppNotificationStatus) {
		this.mobileAppNotificationStatus = mobileAppNotificationStatus;
	}

	public String getMobileAppNotificationStatusMessage() {
		return mobileAppNotificationStatusMessage;
	}

	public void setMobileAppNotificationStatusMessage(
			String mobileAppNotificationStatusMessage) {
		this.mobileAppNotificationStatusMessage = mobileAppNotificationStatusMessage;
	}

	public double getProfitValue() {
		return profitValue;
	}

	public void setProfitValue(double profitValue) {
		this.profitValue = profitValue;
	}

	public double getProfilSplitFraction() {
		return profilSplitFraction;
	}

	public void setProfilSplitFraction(double profilSplitFraction) {
		this.profilSplitFraction = profilSplitFraction;
	}

	public double getOfferPayout() {
		return offerPayout;
	}

	public void setOfferPayout(double offerPayout) {
		this.offerPayout = offerPayout;
	}

	public String getOfferPayoutIsoCurrencyCode() {
		return offerPayoutIsoCurrencyCode;
	}

	public void setOfferPayoutIsoCurrencyCode(String offerPayoutIsoCurrencyCode) {
		this.offerPayoutIsoCurrencyCode = offerPayoutIsoCurrencyCode;
	}

	public double getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}

	public String getRewardIsoCurrencyCode() {
		return rewardIsoCurrencyCode;
	}

	public void setRewardIsoCurrencyCode(String rewardIsoCurrencyCode) {
		this.rewardIsoCurrencyCode = rewardIsoCurrencyCode;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	public double getOfferPayoutInTargetCurrency() {
		return offerPayoutInTargetCurrency;
	}

	public void setOfferPayoutInTargetCurrency(double offerPayoutInTargetCurrency) {
		this.offerPayoutInTargetCurrency = offerPayoutInTargetCurrency;
	}

	public String getOfferPayoutInTargetCurrencyIsoCurrencyCode() {
		return offerPayoutInTargetCurrencyIsoCurrencyCode;
	}

	public void setOfferPayoutInTargetCurrencyIsoCurrencyCode(
			String offerPayoutInTargetCurrencyIsoCurrencyCode) {
		this.offerPayoutInTargetCurrencyIsoCurrencyCode = offerPayoutInTargetCurrencyIsoCurrencyCode;
	}

	public String getOfferRedirectUrl() {
		return offerRedirectUrl;
	}

	public void setOfferRedirectUrl(String offerRedirectUrl) {
		this.offerRedirectUrl = offerRedirectUrl;
	}

	public String getOfferSourceId() {
		return offerSourceId;
	}

	public void setOfferSourceId(String offerSourceId) {
		this.offerSourceId = offerSourceId;
	}

	public double getRevenueValue() {
		return revenueValue;
	}

	public void setRevenueValue(double revenueValue) {
		this.revenueValue = revenueValue;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public String getIosDeviceToken() {
		return iosDeviceToken;
	}

	public void setIosDeviceToken(String iosDeviceToken) {
		this.iosDeviceToken = iosDeviceToken;
	}

	public String getAdvertisingId() {
		return advertisingId;
	}

	public void setAdvertisingId(String advertisingId) {
		this.advertisingId = advertisingId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getAfaNetworkName() {
		return afaNetworkName;
	}

	public void setAfaNetworkName(String afaNetworkName) {
		this.afaNetworkName = afaNetworkName;
	}

	public String getAndroidDeviceToken() {
		return androidDeviceToken;
	}

	public void setAndroidDeviceToken(String androidDeviceToken) {
		this.androidDeviceToken = androidDeviceToken;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getUserEventCategory() {
		return userEventCategory;
	}

	public void setUserEventCategory(String userEventCategory) {
		this.userEventCategory = userEventCategory;
	}

	public String getPhoneNumberExt() {
		return phoneNumberExt;
	}

	public void setPhoneNumberExt(String phoneNumberExt) {
		this.phoneNumberExt = phoneNumberExt;
	}

	public String getRewardName() {
		return rewardName;
	}

	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isInstant() {
		return instant;
	}

	public void setInstant(boolean instant) {
		this.instant = instant;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String toString() {
		return "UserEventEntity [id=" + id + ", userId=" + userId + ", deviceType=" + deviceType + ", deviceId=" + deviceId + ", idfa=" + idfa + ", iosDeviceToken=" + iosDeviceToken + ", androidDeviceToken=" + androidDeviceToken + ", advertisingId=" + advertisingId + ", carrierName=" + carrierName
				+ ", internalTransactionId=" + internalTransactionId + ", offerId=" + offerId + ", offerSourceId=" + offerSourceId + ", transactionId=" + transactionId + ", realmId=" + realmId + ", countryCode=" + countryCode + ", offerRedirectUrl=" + offerRedirectUrl + ", offerTitle=" + offerTitle
				+ ", adProviderCodeName=" + adProviderCodeName + ", applicationName=" + applicationName + ", rewardName=" + rewardName + ", rewardTypeName=" + rewardTypeName + ", phoneNumber=" + phoneNumber + ", phoneNumberExt=" + phoneNumberExt + ", loginName=" + loginName + ", email=" + email
				+ ", rewardRequestStatus=" + rewardRequestStatus + ", rewardRequestStatusMessage=" + rewardRequestStatusMessage + ", rewardResponseStatus=" + rewardResponseStatus + ", rewardResponseStatusMessage=" + rewardResponseStatusMessage + ", mobileAppNotificationStatus="
				+ mobileAppNotificationStatus + ", mobileAppNotificationStatusMessage=" + mobileAppNotificationStatusMessage + ", offerPayout=" + offerPayout + ", offerPayoutIsoCurrencyCode=" + offerPayoutIsoCurrencyCode + ", offerPayoutInTargetCurrency=" + offerPayoutInTargetCurrency
				+ ", offerPayoutInTargetCurrencyIsoCurrencyCode=" + offerPayoutInTargetCurrencyIsoCurrencyCode + ", profilSplitFraction=" + profilSplitFraction + ", profitValue=" + profitValue + ", revenueValue=" + revenueValue + ", rewardValue=" + rewardValue + ", rewardIsoCurrencyCode="
				+ rewardIsoCurrencyCode + ", clickDate=" + clickDate + ", conversionDate=" + conversionDate + ", rewardRequestDate=" + rewardRequestDate + ", rewardDate=" + rewardDate + ", mobileAppNotificationDate=" + mobileAppNotificationDate + ", approved=" + approved + ", instant=" + instant
				+ ", afaNetworkName=" + afaNetworkName + ", userEventCategory=" + userEventCategory + ", testMode=" + testMode + ", ipAddress=" + ipAddress + "]";
	}
	
	
	
}



