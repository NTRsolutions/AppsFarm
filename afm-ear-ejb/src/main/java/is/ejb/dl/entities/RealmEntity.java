package is.ejb.dl.entities;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
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
@Table(name = "Realm")
public class RealmEntity implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@NotNull
	@Size(min = 1, max = 25)
	private String name;

	private String description;

	private boolean enabled = true;
	private boolean testMode = false;
	private boolean modeQueueing = false; // if enabled then all requests are
											// queued so that mode could process
											// only one at a time
	private String testModeUrl;

	private String apiKey;

	private String modeBPUser;
	private String modeBPPassword;
	private String modeCreditUrl;

	private String cowRegenerationNotificationUrl;
	private String conversionNotificationUrl;
	private String rewardNotificationUrl;
	private String externalNotificationUrl;

	private String bannerFilePath;
	private String bannerStyle;

	private String googleNotificationsAccessKey;

	private String supportSystemUrl;
	private String supportSystemUserName;
	private String supportSystemPassword;

	private boolean crfEnabled = false;
	private int crfMinimalClickRate = 10; // click value above which crf filter
											// will act for specific offer
	private double crfCRThreshold = 30; // value below which offer is blocked

	private int connectionTimeout = 20; // in s
	private int readTimeout = 10; // in s

	private int wallLifetime = 10; // in minutes - when exceeded AB will return
									// empty wall to the AR app

	// reporting enabled
	private boolean reportingEnabled = false;
	private String reportingEmails;

	// user app invitation (aka referral programme) parameters
	private boolean isInvitationEnabled;
	private int maxInvitationsLimit; // max number of SUCCESSFUL invitation that
										// can be issued from a single user

	// application version check
	private boolean versionCheck = false;
	private String versionCode;
	private String versionErrorMessage;

	// reporting server access
	private String reportingServerLogin;
	private String reportingServerPassword;

	// ES logs
	private String esPrimaryStorageIp;

	@Lob
	@Column
	private String donkyAutoRespondMessage;

	private boolean referralRewardWithoutAccountActivated;

	@Lob
	@Column
	private String messageReferralRewardWithoutAccountActivatedToInvited;

	@Lob
	@Column
	private String messageReferralRewardWithoutAccountActivatedToInviting;

	@Lob
	@Column
	private String messageReferralAbuseDetectedToInviting;

	@Lob
	@Column
	private String messageReferralExceededLimitToInviting;

	private boolean modeMockupEnabled;
	private String modeMockupUrl;
	private int modeMockupTimer;

	@Lob
	private String AFAConfiguration;
	private double quidcoPercentageCommision;
	private double snapdealPercentageCommision;

	private boolean quidcoTimerEnabled;
	private boolean snapdealReportTimerEnabled;
	private boolean snapdealApprovedOffersTimerEnabled;
	
	private String snapdealCategoryConfiguration;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModeBPUser() {
		return modeBPUser;
	}

	public void setModeBPUser(String modeBPUser) {
		this.modeBPUser = modeBPUser;
	}

	public String getModeBPPassword() {
		return modeBPPassword;
	}

	public void setModeBPPassword(String modeBPPassword) {
		this.modeBPPassword = modeBPPassword;
	}

	public String getModeCreditUrl() {
		return modeCreditUrl;
	}

	public void setModeCreditUrl(String modeCreditUrl) {
		this.modeCreditUrl = modeCreditUrl;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getCowRegenerationNotificationUrl() {
		return cowRegenerationNotificationUrl;
	}

	public void setCowRegenerationNotificationUrl(String cowRegenerationNotificationUrl) {
		this.cowRegenerationNotificationUrl = cowRegenerationNotificationUrl;
	}

	public String getConversionNotificationUrl() {
		return conversionNotificationUrl;
	}

	public void setConversionNotificationUrl(String conversionNotificationUrl) {
		this.conversionNotificationUrl = conversionNotificationUrl;
	}

	public String getRewardNotificationUrl() {
		return rewardNotificationUrl;
	}

	public void setRewardNotificationUrl(String rewardNotificationUrl) {
		this.rewardNotificationUrl = rewardNotificationUrl;
	}

	public String getExternalNotificationUrl() {
		return externalNotificationUrl;
	}

	public void setExternalNotificationUrl(String externalNotificationUrl) {
		this.externalNotificationUrl = externalNotificationUrl;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public String getTestModeUrl() {
		return testModeUrl;
	}

	public void setTestModeUrl(String testModeUrl) {
		this.testModeUrl = testModeUrl;
	}

	public String getBannerFilePath() {
		return bannerFilePath;
	}

	public void setBannerFilePath(String bannerFilePath) {
		this.bannerFilePath = bannerFilePath;
	}

	public String getBannerStyle() {
		return bannerStyle;
	}

	public void setBannerStyle(String bannerStyle) {
		this.bannerStyle = bannerStyle;
	}

	public String getSupportSystemUrl() {
		return supportSystemUrl;
	}

	public void setSupportSystemUrl(String supportSystemUrl) {
		this.supportSystemUrl = supportSystemUrl;
	}

	public String getSupportSystemUserName() {
		return supportSystemUserName;
	}

	public void setSupportSystemUserName(String supportSystemUserName) {
		this.supportSystemUserName = supportSystemUserName;
	}

	public String getSupportSystemPassword() {
		return supportSystemPassword;
	}

	public void setSupportSystemPassword(String supportSystemPassword) {
		this.supportSystemPassword = supportSystemPassword;
	}

	public String getGoogleNotificationsAccessKey() {
		return googleNotificationsAccessKey;
	}

	public void setGoogleNotificationsAccessKey(String googleNotificationsAccessKey) {
		this.googleNotificationsAccessKey = googleNotificationsAccessKey;
	}

	public int getCrfMinimalClickRate() {
		return crfMinimalClickRate;
	}

	public void setCrfMinimalClickRate(int crfMinimalClickRate) {
		this.crfMinimalClickRate = crfMinimalClickRate;
	}

	public double getCrfCRThreshold() {
		return crfCRThreshold;
	}

	public void setCrfCRThreshold(double crfCRThreshold) {
		this.crfCRThreshold = crfCRThreshold;
	}

	public boolean isCrfEnabled() {
		return crfEnabled;
	}

	public void setCrfEnabled(boolean crfEnabled) {
		this.crfEnabled = crfEnabled;
	}

	public int getWallLifetime() {
		return wallLifetime;
	}

	public void setWallLifetime(int wallLifetime) {
		this.wallLifetime = wallLifetime;
	}

	public boolean isReportingEnabled() {
		return reportingEnabled;
	}

	public void setReportingEnabled(boolean reportingEnabled) {
		this.reportingEnabled = reportingEnabled;
	}

	public String getReportingEmails() {
		return reportingEmails;
	}

	public void setReportingEmails(String reportingEmails) {
		this.reportingEmails = reportingEmails;
	}

	public boolean isInvitationEnabled() {
		return isInvitationEnabled;
	}

	public void setInvitationEnabled(boolean isInvitationEnabled) {
		this.isInvitationEnabled = isInvitationEnabled;
	}

	public int getMaxInvitationsLimit() {
		return maxInvitationsLimit;
	}

	public void setMaxInvitationsLimit(int maxInvitationsLimit) {
		this.maxInvitationsLimit = maxInvitationsLimit;
	}

	public boolean isVersionCheck() {
		return versionCheck;
	}

	public void setVersionCheck(boolean versionCheck) {
		this.versionCheck = versionCheck;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionErrorMessage() {
		return versionErrorMessage;
	}

	public void setVersionErrorMessage(String versionErrorMessage) {
		this.versionErrorMessage = versionErrorMessage;
	}

	public String getReportingServerLogin() {
		return reportingServerLogin;
	}

	public void setReportingServerLogin(String reportingServerLogin) {
		this.reportingServerLogin = reportingServerLogin;
	}

	public String getReportingServerPassword() {
		return reportingServerPassword;
	}

	public void setReportingServerPassword(String reportingServerPassword) {
		this.reportingServerPassword = reportingServerPassword;
	}

	public String getEsPrimaryStorageIp() {
		return esPrimaryStorageIp;
	}

	public void setEsPrimaryStorageIp(String esPrimaryStorageIp) {
		this.esPrimaryStorageIp = esPrimaryStorageIp;
	}

	public String getDonkyAutoRespondMessage() {
		return donkyAutoRespondMessage;
	}

	public void setDonkyAutoRespondMessage(String donkyAutoRespondMessage) {
		this.donkyAutoRespondMessage = donkyAutoRespondMessage;
	}

	public boolean isReferralRewardWithoutAccountActivated() {
		return referralRewardWithoutAccountActivated;
	}

	public void setReferralRewardWithoutAccountActivated(boolean referralRewardWithoutAccountActivated) {
		this.referralRewardWithoutAccountActivated = referralRewardWithoutAccountActivated;
	}

	public String getMessageReferralRewardWithoutAccountActivatedToInvited() {
		return messageReferralRewardWithoutAccountActivatedToInvited;
	}

	public void setMessageReferralRewardWithoutAccountActivatedToInvited(String messageReferralRewardWithoutAccountActivatedToInvited) {
		this.messageReferralRewardWithoutAccountActivatedToInvited = messageReferralRewardWithoutAccountActivatedToInvited;
	}

	public String getMessageReferralRewardWithoutAccountActivatedToInviting() {
		return messageReferralRewardWithoutAccountActivatedToInviting;
	}

	public void setMessageReferralRewardWithoutAccountActivatedToInviting(String messageReferralRewardWithoutAccountActivatedToInviting) {
		this.messageReferralRewardWithoutAccountActivatedToInviting = messageReferralRewardWithoutAccountActivatedToInviting;
	}

	public String getMessageReferralAbuseDetectedToInviting() {
		return messageReferralAbuseDetectedToInviting;
	}

	public void setMessageReferralAbuseDetectedToInviting(String messageReferralAbuseDetectedToInviting) {
		this.messageReferralAbuseDetectedToInviting = messageReferralAbuseDetectedToInviting;
	}

	public String getMessageReferralExceededLimitToInviting() {
		return messageReferralExceededLimitToInviting;
	}

	public void setMessageReferralExceededLimitToInviting(String messageReferralExceededLimitToInviting) {
		this.messageReferralExceededLimitToInviting = messageReferralExceededLimitToInviting;
	}

	public boolean isModeQueueing() {
		return modeQueueing;
	}

	public void setModeQueueing(boolean modeQueueing) {
		this.modeQueueing = modeQueueing;
	}

	public boolean isModeMockupEnabled() {
		return modeMockupEnabled;
	}

	public void setModeMockupEnabled(boolean modeMockupEnabled) {
		this.modeMockupEnabled = modeMockupEnabled;
	}

	public String getModeMockupUrl() {
		return modeMockupUrl;
	}

	public void setModeMockupUrl(String modeMockupUrl) {
		this.modeMockupUrl = modeMockupUrl;
	}

	public int getModeMockupTimer() {
		return modeMockupTimer;
	}

	public void setModeMockupTimer(int modeMockupTimer) {
		this.modeMockupTimer = modeMockupTimer;
	}

	public String getAFAConfiguration() {
		return AFAConfiguration;
	}

	public void setAFAConfiguration(String aFAConfiguration) {
		AFAConfiguration = aFAConfiguration;
	}

	public double getQuidcoPercentageCommision() {
		return quidcoPercentageCommision;
	}

	public void setQuidcoPercentageCommision(double quidcoPercentageCommision) {
		this.quidcoPercentageCommision = quidcoPercentageCommision;
	}

	public double getSnapdealPercentageCommision() {
		return snapdealPercentageCommision;
	}

	public void setSnapdealPercentageCommision(double snapdealPercentageCommision) {
		this.snapdealPercentageCommision = snapdealPercentageCommision;
	}

	public boolean isQuidcoTimerEnabled() {
		return quidcoTimerEnabled;
	}

	public void setQuidcoTimerEnabled(boolean quidcoTimerEnabled) {
		this.quidcoTimerEnabled = quidcoTimerEnabled;
	}

	public boolean isSnapdealReportTimerEnabled() {
		return snapdealReportTimerEnabled;
	}

	public void setSnapdealReportTimerEnabled(boolean snapdealReportTimerEnabled) {
		this.snapdealReportTimerEnabled = snapdealReportTimerEnabled;
	}

	public boolean isSnapdealApprovedOffersTimerEnabled() {
		return snapdealApprovedOffersTimerEnabled;
	}

	public void setSnapdealApprovedOffersTimerEnabled(boolean snapdealApprovedOffersTimerEnabled) {
		this.snapdealApprovedOffersTimerEnabled = snapdealApprovedOffersTimerEnabled;
	}

	public String getSnapdealCategoryConfiguration() {
		return snapdealCategoryConfiguration;
	}

	public void setSnapdealCategoryConfiguration(String snapdealCategoryConfiguration) {
		this.snapdealCategoryConfiguration = snapdealCategoryConfiguration;
	}

	
	
}
