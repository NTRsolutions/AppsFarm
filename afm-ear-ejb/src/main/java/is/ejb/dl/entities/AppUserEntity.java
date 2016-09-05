package is.ejb.dl.entities;

import is.ejb.bl.business.UserRoles;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@Table(name = "AppUser")
public class AppUserEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;


   @Id
   @GeneratedValue
   private int id;
   private int altabelUserId;
   private int realmId;
   private String fullName;
   private String gender;
   
   @NotNull
   @NotEmpty
   private String email;

   private String ageRange;
   private boolean male;


   private String phoneNumber;

   @NotNull
   @NotEmpty
   @Column(unique=true)
   private String username;
   private String firstName;
   private String lastName;
   @NotNull
   @NotEmpty
   private String password;

   private String phoneNumberExtension;
   private String secretQuestion;
   private String securityAnswer;
   private String locale;
   private String systemInfo;
   
   private String mac;
   private String idfa;
   private String phoneId;
   private String deviceId;
   private String iOSDeviceToken;
   private String androidDeviceToken;
   private String advertisingId;
   private String applicationName;
   private String rewardTypeName;
   
   private Timestamp registrationTime;
   
   private String deviceType;
   private String countryCode;

   private int successfulInstallConversions = 0; //only install of offers are counted here and used for referral rewards identification (e.g, after 1 or 4 successfull rewards)

   private String referralCode = ""; //store referral code passed during registration (needed to identify invitation object)
   //count it as a successfuly only when invited user registers using the activation code
   private int numberOfSuccessfulInvitations;
   
   private int successfulReferralsCounter;
   private int pendingReferralsCounter;
   
   private String fbInvitationCode;
   
   @Lob
   private String mobileDetails;
   
   private boolean overEighteen;
   
   private int videoConversionCounterVG;
   private int installConversionCounterVG;
   
   private String activationCode;
   private String quidcoUserId;
   private Timestamp attendanceLastBonusTime;
   private boolean guest;
   
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhoneId() {
		return phoneId;
	}
	
	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getAdvertisingId() {
		return advertisingId;
	}
	
	public void setAdvertisingId(String advertisingId) {
		this.advertisingId = advertisingId;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public String getPhoneNumberExtension() {
		return phoneNumberExtension;
	}

	public void setPhoneNumberExtension(String phoneNumberExtension) {
		this.phoneNumberExtension = phoneNumberExtension;
	}

	public String getSecretQuestion() {
		return secretQuestion;
	}

	public void setSecretQuestion(String secretQuestion) {
		this.secretQuestion = secretQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getSystemInfo() {
		return systemInfo;
	}

	public void setSystemInfo(String systemInfo) {
		this.systemInfo = systemInfo;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public int getRealmId() {
		return realmId;
	}

	public void setRealmId(int realmId) {
		this.realmId = realmId;
	}

	public Timestamp getRegistrationTime() {
		return registrationTime;
	}

	public void setRegistrationTime(Timestamp registrationTime) {
		this.registrationTime = registrationTime;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public int getAltabelUserId() {
		return altabelUserId;
	}

	public void setAltabelUserId(int altabelUserId) {
		this.altabelUserId = altabelUserId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getNumberOfSuccessfulInvitations() {
		return numberOfSuccessfulInvitations;
	}

	public void setNumberOfSuccessfulInvitations(int numberOfSuccessfulInvitations) {
		this.numberOfSuccessfulInvitations = numberOfSuccessfulInvitations;
	}

	public String getiOSDeviceToken() {
		return iOSDeviceToken;
	}

	public void setiOSDeviceToken(String iOSDeviceToken) {
		this.iOSDeviceToken = iOSDeviceToken;
	}

	public String getAndroidDeviceToken() {
		return androidDeviceToken;
	}

	public void setAndroidDeviceToken(String androidDeviceToken) {
		this.androidDeviceToken = androidDeviceToken;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	public String getMobileDetails() {
		return mobileDetails;
	}

	public void setMobileDetails(String mobileDetails) {
		this.mobileDetails = mobileDetails;
	}

	public String getFbInvitationCode() {
		return fbInvitationCode;
	}

	public void setFbInvitationCode(String fbInvitationCode) {
		this.fbInvitationCode = fbInvitationCode;
	}

	public int getSuccessfulReferralsCounter() {
		return successfulReferralsCounter;
	}

	public void setSuccessfulReferralsCounter(int successfulReferralsCounter) {
		this.successfulReferralsCounter = successfulReferralsCounter;
	}

	public int getPendingReferralsCounter() {
		return pendingReferralsCounter;
	}

	public void setPendingReferralsCounter(int pendingReferralsCounter) {
		this.pendingReferralsCounter = pendingReferralsCounter;
	}

	public int getSuccessfulInstallConversions() {
		return successfulInstallConversions;
	}

	public void setSuccessfulInstallConversions(int successfulInstallConversions) {
		this.successfulInstallConversions = successfulInstallConversions;
	}

	public String getReferralCode() {
		return referralCode;
	}

	public void setReferralCode(String referralCode) {
		this.referralCode = referralCode;
	}

	public boolean isOverEighteen() {
		return overEighteen;
	}

	public void setOverEighteen(boolean overEighteen) {
		this.overEighteen = overEighteen;
	}

	public int getVideoConversionCounterVG() {
		return videoConversionCounterVG;
	}

	public void setVideoConversionCounterVG(int videoConversionCounterVG) {
		this.videoConversionCounterVG = videoConversionCounterVG;
	}

	public int getInstallConversionCounterVG() {
		return installConversionCounterVG;
	}

	public void setInstallConversionCounterVG(int installConversionCounterVG) {
		this.installConversionCounterVG = installConversionCounterVG;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public String getQuidcoUserId() {
		return quidcoUserId;
	}

	public void setQuidcoUserId(String quidcoUserId) {
		this.quidcoUserId = quidcoUserId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Timestamp getAttendanceLastBonusTime() {
		return attendanceLastBonusTime;
	}

	public void setAttendanceLastBonusTime(Timestamp attendanceLastBonusTime) {
		this.attendanceLastBonusTime = attendanceLastBonusTime;
	}

	@Override
	public String toString() {
		return "AppUserEntity [id=" + id + "]";
	}

	public boolean isGuest() {
		return guest;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	

	
	
}



