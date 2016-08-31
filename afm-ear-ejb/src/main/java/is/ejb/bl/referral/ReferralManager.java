package is.ejb.bl.referral;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.notificationSystems.NotificationManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

@Stateless
public class ReferralManager {

	@Inject
	private Logger logger;

	@Inject
	private DAOInvitation daoInvitation;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOUserEvent daoUserEvent;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private ReferralAbuseDetector referralAbuseDetector;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAORealm daoRealm;

	// private int successfulConversionsRewardThreshold1 = 1;
	// private int successfulConversionsRewardThreshold2 = 5;

	@Inject
	private NotificationManager notificationManager;

	public boolean persistReferralDataForInvitedUser(RealmEntity realm, String invitedUserReferralCode, String invitedUserEmail,
			String invitedUserPhoneNumber, String invitedUserPhoneNumberExt) {

		try {
			logger.info("referral persisting referral-based registration data: " + invitedUserReferralCode + " " + invitedUserEmail);

			// get invitation object
			// used to work with email invitation
			// InvitationEntity invitation =
			// daoInvitation.findByCodeAndInvitedUserEmail(invitedUserReferralCode,
			// invitedUserEmail);
			InvitationEntity invitation = daoInvitation.findByCode(invitedUserReferralCode);
			logger.info("referral identitified invitation object: " + invitation.getCode());

			// check referral abuse and block user that registers as invited
			// from the same device
			boolean detectedReferralAbuser = referralAbuseDetector.checkInvitation(invitation);
			if (detectedReferralAbuser) {

				//notificationManager.sendReferralAbuseNotification(invitation);

				invitation.setValid(false); // if inviation is invalid - it will
											// not trigger reward in the future!
				invitation = daoInvitation.createOrUpdate(invitation);
				return false; // if false - there are no rewards

			}

			// update invite object
			invitation.setPhoneNumberInvited(invitedUserPhoneNumber);
			invitation.setPhoneNumberExtInvited(invitedUserPhoneNumberExt);
			invitation = daoInvitation.createOrUpdate(invitation);

			// check if invitation is valid
			if (invitation != null && invitedUserReferralCode != null && invitedUserReferralCode.length() > 0
					&& invitation.getCode().equals(invitedUserReferralCode) &&
					// invitation.getEmailInvited().equals(invitedUserEmail) &&
					invitation.getDateOfRegistration() == null) {

				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.OK,
						Application.INVITATION_ACTIVITY_SUCCESSFULLY_VALIDATED + " " + "user referral invitation validation successful: "
								+ " invitedUserReferralCode: " + invitedUserReferralCode + " invitedUserPhoneNumber: " + invitedUserPhoneNumber
								+ " invitedUserPhoneNumberExt: " + invitedUserPhoneNumberExt + " invitedUserEmail: " + invitedUserEmail
								+ " invitingUserEmail: " + invitation.getEmailInviting() + " date of registration: "
								+ invitation.getDateOfRegistration());
				invitation.setValid(true); // if inviation is invalid - it will
											// not trigger reward in the future!
				invitation = daoInvitation.createOrUpdate(invitation);

				
				
				return true; // if all ok we can process this invitation and
								// send rewards
			} else {
				// explain why
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY_ABORTED + " " + "user referral invitation processing aborted, data received: "
								+ " invitedUserReferralCode: " + invitedUserReferralCode + " invitedUserPhoneNumber: " + invitedUserPhoneNumber
								+ " invitedUserPhoneNumberExt: " + invitedUserPhoneNumberExt + " invitedUserEmail: " + invitedUserEmail
								+ " invitingUserEmail: " + invitation.getEmailInviting() + " date of registration: "
								+ invitation.getDateOfRegistration());

				invitation.setValid(false); // if inviation is invalid - it will
											// not trigger reward in the future!
				invitation = daoInvitation.createOrUpdate(invitation);

				return false; // if false - there are no rewards
			}
		} catch (Exception exc) { // we catch exceptions to avoid situation when
									// registration is aborted due to referral
									// programme errors
			logger.severe(exc.toString());
			exc.printStackTrace();

			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
							+ " error: " + exc.toString());

			return false;
		}
	}

	private void indexSuccessfullInvitationToES(InvitationEntity invitation) {
		logger.info("Indexing success invitation in es");
		try {
			AppUserEntity appUser = daoAppUser.findByEmail(invitation.getEmailInviting());

			RealmEntity realm = daoRealm.findById(appUser.getRealmId());

			Application.getElasticSearchLogger().indexUserClick(realm.getId(), invitation.getEmailInvited(),
					invitation.getEmailInviting(), appUser.getDeviceType(), invitation.getReferralSource().toString(), null, null, null,
					appUser.getRewardTypeName(), 0, 0, null, 0, realm.getName(), "", "REFERRAL_SUCCESS",
					invitation.getInvitedInternalTransactionId(), null, UserEventCategory.INVITE.toString(), null, null,
					null, appUser.getCountryCode(), false, appUser.getApplicationName(), appUser.getAdvertisingId(),
					appUser.getIdfa(), realm.isTestMode(), 0, null);
			logger.info("Indexed success  invitation in es");

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
	}

	public boolean isReferralRewardApproved(RealmEntity realm, AppUserEntity invitedUser) {

		try {
			// get invitation object (assume only one invitation can be created
			// for the same invited user email)
			if (invitedUser.getReferralCode() == null || invitedUser.getReferralCode().length() == 0) {
				return false;
			}
			logger.info("looking for invitation with referral code: " + invitedUser.getReferralCode());

			InvitationEntity invitation = daoInvitation.findByCode(invitedUser.getReferralCode());
			logger.info("identitified invitation object: " + invitation);
			logger.info("identitified invitation object with code: " + invitation.getCode());

			// check referral abuse and block user that registers as invited
			// from the same device
			boolean detectedReferralAbuser = referralAbuseDetector.checkInvitation(invitation);
			if (detectedReferralAbuser) {

				//notificationManager.sendReferralAbuseNotification(invitation);
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ABUSE_DETECTOR_CHECK,
						-1,
						LogStatus.OK,
						Application.INVITATION_ABUSE_DETECTOR_CHECK + " " + Application.INVITATION_ABUSE_DETECTED + " "
								+ Application.INVITATION_ABUSE_DETECTED_REFERRAL_REWARD_REJECTED + " referral reward for user: " + " invited phone: "
								+ invitedUser.getPhoneNumber() + " invited email: " + invitedUser.getEmail()
								+ " not approved as a result of abuse detection");

				return false; // if false - there are no rewards
			}

			int successfulConversionsRewardThreshold1 = 1;
			int successfulConversionsRewardThreshold2 = 5;
			if (invitedUser != null) {
				RewardTypeEntity rewardType = daoRewardType.findByName(invitedUser.getRewardTypeName());
				if (rewardType != null) {
					successfulConversionsRewardThreshold1 = rewardType.getReferralFirstThreshold();
					successfulConversionsRewardThreshold2 = rewardType.getReferralSecondThreshold();
				}
			}
			if (invitedUser.getSuccessfulInstallConversions() == successfulConversionsRewardThreshold1
					|| invitedUser.getSuccessfulInstallConversions() == successfulConversionsRewardThreshold2) {
				logger.info("referral reward for user: " + invitedUser.getEmail() + " approved " + invitedUser.getSuccessfulInstallConversions());
				return true;
			} else {
				logger.info("referral reward for user: " + invitedUser.getEmail() + " NOT approved " + invitedUser.getSuccessfulInstallConversions());
				return false;
			}
		} catch (Exception exc) { // we catch exceptions to avoid situation when
									// registration is aborted due to referral
									// programme errors
			logger.severe(exc.toString());
			exc.printStackTrace();

			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.REFERRAL_MONITOR_ACTIVITY + " " + Application.REFERRAL_MONITOR_ACTIVITY_ERROR + " "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());
			return false;
		}
	}

	public boolean processReferralRewardRequest(RealmEntity realm, AppUserEntity invitedUser) {

		InvitationEntity invitation = null;
		try {
			// get invitation object
			invitation = daoInvitation.findByCode(invitedUser.getReferralCode());
			// update invite object
			invitation.setDateOfRegistration(new Timestamp(System.currentTimeMillis()));
			daoInvitation.createOrUpdate(invitation);

			try {

				int successfulConversionsRewardThreshold1 = 1;
				int successfulConversionsRewardThreshold2 = 5;
				if (invitedUser != null) {
					RewardTypeEntity rewardType = daoRewardType.findByName(invitedUser.getRewardTypeName());
					if (rewardType != null) {
						successfulConversionsRewardThreshold1 = rewardType.getReferralFirstThreshold();
						successfulConversionsRewardThreshold2 = rewardType.getReferralSecondThreshold();
					}
				}

				if (invitedUser.getSuccessfulInstallConversions() == successfulConversionsRewardThreshold1
						|| invitedUser.getSuccessfulInstallConversions() == successfulConversionsRewardThreshold2) {
					// rewardUser(invitedUserEmail, realm, invitedUserEmail,
					// invitation, true); //reward invited user (we no longer
					// reward invited users - only the inviters!)

					if (realm == null) {
						realm = daoRealm.findById(invitedUser.getRealmId());
					}
					if (invitedUser != null && invitedUser.getActivationCode() != null && invitedUser.getActivationCode().length() > 0
							&& !realm.isReferralRewardWithoutAccountActivated()) {
						// user account is not activated
						Application.getElasticSearchLogger().indexLog(
								Application.INVITATION_ACTIVITY,
								-1,
								LogStatus.ERROR,
								Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " " + "invited user not activated"
										+ invitedUser.getPhoneNumber());
						//notificationManager.sendNoActivatedAccountNotification(invitation);

					} else {

						if (isInvitingExceededLimit(invitation)) {
							//user invitation limit exceeded
							Application.getElasticSearchLogger().indexLog(
									Application.INVITATION_ACTIVITY,
									-1,
									LogStatus.ERROR,
									Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " "
											+ "inviting user exceeded limit" + invitedUser.getPhoneNumber());
							//notificationManager.sendExceededLimitNotification(invitation);

						} else {

							rewardUser(invitation.getEmailInviting(), realm, invitation.getEmailInvited(), invitation, false,
									invitedUser.getSuccessfulInstallConversions()); // reward inviting user
																					
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				logger.severe(exc.toString());
				Application.getElasticSearchLogger().indexLog(
						Application.INVITATION_ACTIVITY,
						-1,
						LogStatus.ERROR,
						Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " "
								+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR + " error: " + exc.toString());
				return false;
			}

			// update invitation ojbect
			invitation.setRealized(true);
			invitation.setProcessingStatus(RespStatusEnum.SUCCESS.toString());
			invitation.setProcessingStatusMessage(RespStatusEnum.SUCCESS.toString());
			daoInvitation.createOrUpdate(invitation);

			// //increment successful invitations counter for user the sent
			// invitation
			// AppUserEntity appUserInviting =
			// daoAppUser.findByEmail(invitation.getEmailInviting());
			// int numberOfSuccessfulInvitations =
			// appUserInviting.getNumberOfSuccessfulInvitations();
			// numberOfSuccessfulInvitations++;
			// appUserInviting.setNumberOfSuccessfulInvitations(numberOfSuccessfulInvitations);
			// daoAppUser.createOrUpdate(appUserInviting);

			return true;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
							+ " error: " + exc.toString());

			invitation.setRealized(false);
			invitation.setProcessingStatus(RespStatusEnum.FAILED.toString());
			invitation.setProcessingStatusMessage(exc.toString());
			daoInvitation.createOrUpdate(invitation);

			return false;
		}
	}

	private boolean isInvitingExceededLimit(InvitationEntity entity) {

		boolean result = false;
		try {
			if (entity != null && entity.getEmailInviting() != null) {
				String emailInviting = entity.getEmailInviting();
				AppUserEntity appUser = daoAppUser.findByEmail(emailInviting);
				if (appUser != null) {
					RewardTypeEntity rewardType = daoRewardType.findByName(appUser.getRewardTypeName());
					if (rewardType != null) {
						int referralMaxCountForRewardType = rewardType.getMaxReferralCount();
						int userReferralCount = appUser.getSuccessfulReferralsCounter() + appUser.getPendingReferralsCounter();
						if (userReferralCount >= referralMaxCountForRewardType) {
							result = true;
						}
					}
				}
			}
		} catch (Exception exc) {
			Application.getElasticSearchLogger().indexLog(
					Application.INVITATION_ACTIVITY,
					-1,
					LogStatus.ERROR,
					Application.INVITATION_ACTIVITY + " " + Application.INVITATION_ACTIVITY_ABORTED + " " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR
							+ " error in checking user limits: " + exc.toString());

		}

		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.OK,
				Application.INVITATION_ACTIVITY + " returning " + result + " in inviting user limit check for entity:" + entity.getId());

		return result;
	}

	private void rewardUser(String userEmail, RealmEntity realm, String invitedEmail, InvitationEntity invitation,
			boolean isEventFromUserThatWasInviting, int numberOfSuccessfulConversionsPerformedByInvitedUser) throws Exception {

		indexSuccessfullInvitationToES(invitation);
		// handle this for inviting user from which referral email was sent
		AppUserEntity user = daoAppUser.findByEmail(userEmail);
		String userCountryCode = user.getCountryCode();
		Application.getElasticSearchLogger().indexLog(Application.INVITATION_ACTIVITY, -1, LogStatus.OK,
				Application.INVITATION_ACTIVITY + " " + " identified user for referral reward: " + user.getEmail());

		double rewardTargetCurrencyValue = this.getReferralRewardValueByGeoBasedOnConversionCount(userCountryCode,
				numberOfSuccessfulConversionsPerformedByInvitedUser);
		logger.info("getting referral reward value for following number of successful conversions: "
				+ numberOfSuccessfulConversionsPerformedByInvitedUser + " value: " + rewardTargetCurrencyValue);

		String rewardTargetCurrencyCode = getRewardCurrencyCodeByGeo(userCountryCode);

		Application.getElasticSearchLogger().indexLog(
				Application.INVITATION_ACTIVITY,
				-1,
				LogStatus.OK,
				Application.INVITATION_ACTIVITY + " " + " rewarding inviting user: " + isEventFromUserThatWasInviting + " email: " + user.getEmail()
						+ " country code: " + userCountryCode + " rewardTargetCurrencyValue: " + rewardTargetCurrencyValue
						+ " rewardTargetCurrencyCode: " + rewardTargetCurrencyCode + " for following number of successful conversions: "
						+ user.getSuccessfulInstallConversions());

		String internalTransactionId = DigestUtils.sha1Hex(user.getId() + Math.random() * 100000 + System.currentTimeMillis() + user.getPhoneNumber()
				+ user.getPhoneNumberExtension() + user.getEmail());

		UserEventEntity invitingUserEvent = new UserEventEntity();
		// generate event object and pesrsist it in db
		UserEventEntity event = new UserEventEntity();
		event.setUserId(user.getId());
		event.setOfferId(invitation.getCode());
		event.setDeviceType(user.getDeviceType());
		event.setInternalTransactionId(internalTransactionId);
		event.setPhoneNumber(user.getPhoneNumber());
		event.setPhoneNumberExt(user.getPhoneNumberExtension());
		event.setRewardTypeName(UserEventCategory.INVITE.toString()); // needed
																		// for
																		// denomination
																		// model
																		// to
																		// calculate
																		// reward
		event.setRealmId(realm.getId());
		event.setApplicationName(user.getApplicationName());
		// event.setOfferTitle("Referral rewardinvitation for "+invitedEmail);
		event.setOfferTitle("Referral reward from " + invitation.getPhoneNumberInvited() + " REWARD_TYPE: " + user.getRewardTypeName());
		event.setOfferPayout(0.0); // needed for denomination model to calculate
									// reward
		event.setOfferPayoutInTargetCurrency(0);
		event.setRewardIsoCurrencyCode(rewardTargetCurrencyCode);
		event.setRewardValue(rewardTargetCurrencyValue);
		event.setProfilSplitFraction(0);
		event.setProfitValue(0);
		event.setIosDeviceToken(user.getiOSDeviceToken());
		event.setAndroidDeviceToken(user.getAndroidDeviceToken());
		event.setRevenueValue(0);
		event.setClickDate(new Timestamp(System.currentTimeMillis()));
		event.setConversionDate(new Timestamp(System.currentTimeMillis()));
		event.setCountryCode(userCountryCode);
		event.setUserEventCategory(UserEventCategory.INVITE.toString());
		event.setEmail(user.getEmail());
		event.setInstant(false); // reward user via wallet payin

		daoUserEvent.create(event); // persist event in db

		// add conversion event to conversion index in es
		Application.getElasticSearchLogger().indexUserClick(realm.getId(), event.getPhoneNumber(), "", event.getDeviceType(), event.getOfferId(),
				event.getOfferSourceId(), event.getOfferTitle(), event.getAdProviderCodeName(), user.getRewardTypeName(),
				event.getOfferPayoutInTargetCurrency(), event.getRewardValue(), event.getRewardIsoCurrencyCode(), event.getProfitValue(),
				realm.getName(), "", UserEventType.conversion.toString(), event.getInternalTransactionId(), "", UserEventCategory.INVITE.toString(),
				"", "", "", event.getCountryCode(), event.isInstant(), event.getApplicationName(), 
				"", //gaid
				"", //idfa
				event.isTestMode(),0,"");

		// TODO create es invitation entry log
		if (isEventFromUserThatWasInviting) { // persist transaction ids for
												// each user inside invitation
												// object
			invitation.setInvitingInternalTransactionId(internalTransactionId);
			invitation.setPhoneNumberExtInviting(user.getPhoneNumberExtension());
			invitation.setRewardValueInviting(event.getRewardValue());
			invitation.setRewardValueCurrencyCodeInviting(event.getRewardIsoCurrencyCode());
		} else {
			invitation.setInvitedInternalTransactionId(internalTransactionId);
			invitation.setPhoneNumberExtInvited(user.getPhoneNumberExtension());
			invitation.setRewardValueInvited(event.getRewardValue());
			invitation.setRewardValueCurrencyCodeInvited(event.getRewardIsoCurrencyCode());
		}
		daoInvitation.createOrUpdate(invitation);

		//rewardManager.createUserConversionHistory(event); // update conversion
															// history (needed
															// to filter out
															// already clicked
															// offers for
															// particular user)

		// increment the counter of successful referrals
		// do this only for the first referral trigger (during the first
		// successful install)
		if (numberOfSuccessfulConversionsPerformedByInvitedUser == 1) {
			user.setSuccessfulReferralsCounter(user.getSuccessfulReferralsCounter() + 1);
			daoAppUser.createOrUpdate(user);
		}

		rewardManager.issueReward(realm, event, invitation, isEventFromUserThatWasInviting); // issue
																								// reward
		// rewardManager.requestRewardMode(event, invitation,
		// isEventFromUserThatWasInviting); //issue reward to mode
	}

	// TODO in future expose UI for adjusting payouts for different geos
	public String getRewardCurrencyCodeByGeo(String countryCode) {
		if (countryCode.equals(CountryCode.KE.toString())) {
			return "KSH";
		} else if (countryCode.equals(CountryCode.IN.toString())) {
			return "INR";
		} else if (countryCode.equals(CountryCode.ZA.toString())) {
			return "ZAR";
		} else if (countryCode.equals(CountryCode.GB.toString())) {
			return "GBP";
		} else if (countryCode.equals(CountryCode.PL.toString())) {
			return "ZL";
		} else
			return CountryCode.UNKNOWN.toString();
	}

	// TODO in future expose UI for adjusting payouts for different geos
	/*
	 * public double getRewardCurrencyValueByGeo(String countryCode) { if
	 * (countryCode.equals(CountryCode.KE.toString())) { return 5.5; } else if
	 * (countryCode.equals(CountryCode.IN.toString())) { return 10.5; } else if
	 * (countryCode.equals(CountryCode.ZA.toString())) { return 2.5; } else if
	 * (countryCode.equals(CountryCode.GB.toString())) { return 0.0; } else if
	 * (countryCode.equals(CountryCode.PL.toString())) { return 0.0; } else
	 * return 0.0; }
	 */

	/*
	 * public double getRewardCurrencyValueByGeo(String countryCode, int
	 * numberOfSuccessfulRewards) {
	 * if(countryCode.equals(CountryCode.KE.toString())) {
	 * if(numberOfSuccessfulRewards == successfulConversionsRewardThreshold1) {
	 * return 0.5; } else if(numberOfSuccessfulRewards ==
	 * successfulConversionsRewardThreshold2) { return 5; } else { return 0; } }
	 * else if(countryCode.equals(CountryCode.IN.toString())) {
	 * if(numberOfSuccessfulRewards == successfulConversionsRewardThreshold1) {
	 * return 0.5; } else if(numberOfSuccessfulRewards ==
	 * successfulConversionsRewardThreshold2) { return 10; } else { return 0; }
	 * } else if(countryCode.equals(CountryCode.ZA.toString())) {
	 * if(numberOfSuccessfulRewards == successfulConversionsRewardThreshold1) {
	 * return 0.5; } else if(numberOfSuccessfulRewards ==
	 * successfulConversionsRewardThreshold2) { return 2; } else { return 0; } }
	 * else if(countryCode.equals(CountryCode.GB.toString())) { return 0.0; }
	 * else if(countryCode.equals(CountryCode.PL.toString())) { return 0.0; }
	 * else return 0.0; }
	 */

	public int getReferralRewardThreshold(String countryCode, int threshold) {
		try {
			logger.info("Getting reward value based on geo:" + countryCode);
			if (countryCode != null) {
				String rewardTypeName = getRewardTypeByGeo(countryCode);
				logger.info("Finding reward type with name: " + rewardTypeName);
				RewardTypeEntity rewardType = daoRewardType.findByName(rewardTypeName);
				if (rewardType != null) {
					logger.info("Reward type found. Returning values...");
					if (threshold == 1) {
						return rewardType.getReferralFirstThreshold();
					}
					if (threshold == 2) {
						return rewardType.getReferralSecondThreshold();
					}
				} else {
					logger.info("Invalid reward type...");
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return 0;
	}

	public double getReferralRewardValueByGeoBasedOnConversionCount(String countryCode, int conversions) {
		try {
			logger.info("Getting reward value based on geo:" + countryCode);
			if (countryCode != null) {
				String rewardTypeName = getRewardTypeByGeo(countryCode);
				logger.info("Finding reward type with name: " + rewardTypeName);
				RewardTypeEntity rewardType = daoRewardType.findByName(rewardTypeName);
				if (rewardType != null) {
					logger.info("Reward type found. Returning values...");
					if (conversions == rewardType.getReferralFirstThreshold())
						return rewardType.getReferralValueAtFirstThresholdInvite();
					if (conversions == rewardType.getReferralSecondThreshold())
						;
					return rewardType.getReferralValueAtSecondThresholdInvite();
				} else {
					logger.info("Invalid reward type!");
				}

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return 0;
	}

	public double getReferralRewardValueByGeo(String countryCode, int threshold) {
		try {
			logger.info("Getting reward value based on geo:" + countryCode);
			if (countryCode != null) {
				String rewardTypeName = getRewardTypeByGeo(countryCode);
				logger.info("Finding reward type with name: " + rewardTypeName);
				RewardTypeEntity rewardType = daoRewardType.findByName(rewardTypeName);
				if (rewardType != null) {
					logger.info("Reward type found. Returning values...");
					if (threshold == 1)
						return rewardType.getReferralValueAtFirstThresholdInvite();
					if (threshold == 2)
						return rewardType.getReferralValueAtSecondThresholdInvite();
				} else {
					logger.info("Invalid reward type!");
				}

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return 0;

	}

	private String getRewardTypeByGeo(String countryCode) {
		String rewardType = "AirRewardz-India";
		if (countryCode.equals(CountryCode.IN.toString())) {
			rewardType = "AirRewardz-India";
		}
		if (countryCode.equals(CountryCode.KE.toString())) {
			rewardType = "AirRewardz-Kenya";
		}
		if (countryCode.equals(CountryCode.ZA.toString())) {
			rewardType = "AirRewardz-SouthAfrica";
		}
		if (countryCode.equals(CountryCode.PL.toString())) {

		}
		if (countryCode.equals(CountryCode.GB.toString())) {

		}

		return rewardType;
	}

	public int getMaxReferralCount(String countryCode) {
		int maxCount = 1;
		try {
			if (countryCode != null) {
				String rewardTypeName = getRewardTypeByGeo(countryCode);

				RewardTypeEntity rewardType = daoRewardType.findByName(rewardTypeName);
				if (rewardType != null) {
					maxCount = rewardType.getMaxReferralCount();
				}

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return maxCount;
	}

}
