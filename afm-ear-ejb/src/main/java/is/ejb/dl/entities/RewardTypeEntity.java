package is.ejb.dl.entities;

import java.io.Serializable;
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
@Table(name = "RewardType")
public class RewardTypeEntity implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	@JoinColumn(name = "realm", referencedColumnName = "id")
	private RealmEntity realm;

	@NotNull
	private String name;
	private String countryCode;
	private Timestamp generationDate; // represents time the offer was generated
	private boolean enabled = true; //flag that will allow to enable / disable this rewardType from UI display
	
	private boolean testMode = false;
	
	private int applicationId;
	private String applicationType; //aka applicationName
	
	private double minimalOfferPayoutThresholdInSourceCurrency; //e.g, 0.16 USD 
	private double minimalInstantRewardThresholdInTargetCurrency; //eg., 5 ZAR in ZA
	
	private int referralFirstThreshold;
	private int referralSecondThreshold;
	private double referralValueAtFirstThresholdInvite;
	private double referralValueAtSecondThresholdInvite;
	
	private int installCounterVG;
	private int videoCounterVG;
	private int maxReferralCount;
	
	private boolean spinnerEnabled;
	
	private double spinnerUseValue;
	private boolean spinnerDailyRewardEnabled;
	private double spinnerDailyRewardUseValue;
	private String spinnerDailyRewardNotificationMessage;
	
	
	private double referralValueAtFirstInvite;
	private double referralValueAtFifthInvite;
	
	private boolean wallStatus;
	private boolean videoStatus;
	private boolean referStatus;
	private boolean walletStatus;
	private boolean spinnerStatus;
	
	private String wallStatusMessage;
	private String videoStatusMessage;
	private String referStatusMessage;
	private String walletStatusMessage;
	private String spinnerStatusMessage;
	
	private double attendanceValue;
	
	
	private String imageBannerContent; 
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Timestamp getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Timestamp generationDate) {
		this.generationDate = generationDate;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public double getMinimalOfferPayoutThresholdInSourceCurrency() {
		return minimalOfferPayoutThresholdInSourceCurrency;
	}

	public void setMinimalOfferPayoutThresholdInSourceCurrency(
			double minimalOfferPayoutThresholdInSourceCurrency) {
		this.minimalOfferPayoutThresholdInSourceCurrency = minimalOfferPayoutThresholdInSourceCurrency;
	}

	public double getMinimalInstantRewardThresholdInTargetCurrency() {
		return minimalInstantRewardThresholdInTargetCurrency;
	}

	public void setMinimalInstantRewardThresholdInTargetCurrency(
			double minimalInstantRewardThresholdInTargetCurrency) {
		this.minimalInstantRewardThresholdInTargetCurrency = minimalInstantRewardThresholdInTargetCurrency;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public int getReferralFirstThreshold() {
		return referralFirstThreshold;
	}

	public void setReferralFirstThreshold(int referralFirstThreshold) {
		this.referralFirstThreshold = referralFirstThreshold;
	}

	public int getReferralSecondThreshold() {
		return referralSecondThreshold;
	}

	public void setReferralSecondThreshold(int referralSecondThreshold) {
		this.referralSecondThreshold = referralSecondThreshold;
	}

	public double getReferralValueAtFirstThresholdInvite() {
		return referralValueAtFirstThresholdInvite;
	}

	public void setReferralValueAtFirstThresholdInvite(double referralValueAtFirstThresholdInvite) {
		this.referralValueAtFirstThresholdInvite = referralValueAtFirstThresholdInvite;
	}

	public double getReferralValueAtSecondThresholdInvite() {
		return referralValueAtSecondThresholdInvite;
	}

	public void setReferralValueAtSecondThresholdInvite(double referralValueAtSecondThresholdInvite) {
		this.referralValueAtSecondThresholdInvite = referralValueAtSecondThresholdInvite;
	}

	public int getInstallCounterVG() {
		return installCounterVG;
	}

	public void setInstallCounterVG(int installCounterVG) {
		this.installCounterVG = installCounterVG;
	}

	public int getVideoCounterVG() {
		return videoCounterVG;
	}

	public void setVideoCounterVG(int videoCounterVG) {
		this.videoCounterVG = videoCounterVG;
	}

	public int getMaxReferralCount() {
		return maxReferralCount;
	}

	public void setMaxReferralCount(int maxReferralCount) {
		this.maxReferralCount = maxReferralCount;
	}

	public boolean isSpinnerEnabled() {
		return spinnerEnabled;
	}

	public void setSpinnerEnabled(boolean spinnerEnabled) {
		this.spinnerEnabled = spinnerEnabled;
	}

	public double getSpinnerUseValue() {
		return spinnerUseValue;
	}

	public void setSpinnerUseValue(double spinnerUseValue) {
		this.spinnerUseValue = spinnerUseValue;
	}

	
	public boolean isSpinnerDailyRewardEnabled() {
		return spinnerDailyRewardEnabled;
	}

	public void setSpinnerDailyRewardEnabled(boolean spinnerDailyRewardEnabled) {
		this.spinnerDailyRewardEnabled = spinnerDailyRewardEnabled;
	}

	public double getSpinnerDailyRewardUseValue() {
		return spinnerDailyRewardUseValue;
	}

	public void setSpinnerDailyRewardUseValue(double spinnerDailyRewardUseValue) {
		this.spinnerDailyRewardUseValue = spinnerDailyRewardUseValue;
	}

	public String getSpinnerDailyRewardNotificationMessage() {
		return spinnerDailyRewardNotificationMessage;
	}

	public void setSpinnerDailyRewardNotificationMessage(String spinnerDailyRewardNotificationMessage) {
		this.spinnerDailyRewardNotificationMessage = spinnerDailyRewardNotificationMessage;
	}
	

	public double getReferralValueAtFirstInvite() {
		return referralValueAtFirstInvite;
	}

	public void setReferralValueAtFirstInvite(double referralValueAtFirstInvite) {
		this.referralValueAtFirstInvite = referralValueAtFirstInvite;
	}

	public double getReferralValueAtFifthInvite() {
		return referralValueAtFifthInvite;
	}

	public void setReferralValueAtFifthInvite(double referralValueAtFifthInvite) {
		this.referralValueAtFifthInvite = referralValueAtFifthInvite;
	}

	public String getImageBannerContent() {
		return imageBannerContent;
	}

	public void setImageBannerContent(String imageBannerContent) {
		this.imageBannerContent = imageBannerContent;
	}

	public boolean isWallStatus() {
		return wallStatus;
	}

	public void setWallStatus(boolean wallStatus) {
		this.wallStatus = wallStatus;
	}

	public boolean isVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(boolean videoStatus) {
		this.videoStatus = videoStatus;
	}

	public boolean isReferStatus() {
		return referStatus;
	}

	public void setReferStatus(boolean referStatus) {
		this.referStatus = referStatus;
	}

	public boolean isWalletStatus() {
		return walletStatus;
	}

	public void setWalletStatus(boolean walletStatus) {
		this.walletStatus = walletStatus;
	}

	public boolean isSpinnerStatus() {
		return spinnerStatus;
	}

	public void setSpinnerStatus(boolean spinnerStatus) {
		this.spinnerStatus = spinnerStatus;
	}

	public String getWallStatusMessage() {
		return wallStatusMessage;
	}

	public void setWallStatusMessage(String wallStatusMessage) {
		this.wallStatusMessage = wallStatusMessage;
	}

	public String getVideoStatusMessage() {
		return videoStatusMessage;
	}

	public void setVideoStatusMessage(String videoStatusMessage) {
		this.videoStatusMessage = videoStatusMessage;
	}

	public String getReferStatusMessage() {
		return referStatusMessage;
	}

	public void setReferStatusMessage(String referStatusMessage) {
		this.referStatusMessage = referStatusMessage;
	}

	public String getWalletStatusMessage() {
		return walletStatusMessage;
	}

	public void setWalletStatusMessage(String walletStatusMessage) {
		this.walletStatusMessage = walletStatusMessage;
	}

	public String getSpinnerStatusMessage() {
		return spinnerStatusMessage;
	}

	public void setSpinnerStatusMessage(String spinnerStatusMessage) {
		this.spinnerStatusMessage = spinnerStatusMessage;
	}

	public double getAttendanceValue() {
		return attendanceValue;
	}

	public void setAttendanceValue(double attendanceValue) {
		this.attendanceValue = attendanceValue;
	}

	

}
