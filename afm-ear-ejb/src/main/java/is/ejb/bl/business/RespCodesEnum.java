package is.ejb.bl.business;

/*
Successful request

200 OK
200 No Content (correct and successful request, but no offers are available for this user)

Error during request

400 Bad Request (e.g. invalid or missing mandatory request parameters)
401 Unauthorized (e.g. wrong hashkey for this request and appid)
404 Not found (e.g. feed.xml instead of offers.xml, or using a non-­-existent version in the URL)
500 Internal Server Error (Error on the Fyber server)
502 Bad Gateway (Error on the Fyber server)
*/

public enum RespCodesEnum {
	//general codes
	OK, 
	OK_NO_CONTENT, 
	ERROR,
	ERROR_INVALID_API_KEY,
	//for user registration
	ERROR_USER_UNDER_GIVEN_PHONE_NUMBER_ALREADY_REGISTERED,
	ERROR_USER_UNDER_GIVEN_MAC_ALREADY_REGISTERED,
	ERROR_USER_UNDER_GIVEN_EMAIL_ALREADY_REGISTERED,
	ERROR_USER_WITH_GIVEN_PHONE_NUMBER_NOT_FOUND,
	ERROR_USER_WITH_GIVEN_PASSWORD_NOT_FOUND,
	ERROR_USER_CURRENT_PASSWORD_MISMATCH,
	ERROR_USER_WITH_GIVEN_ID_NOT_FOUND,
	ERROR_USER_INVALID_MAC,
	ERROR_USER_WITH_GIVEN_MAC_NOT_FOUND,
	ERROR_INVALID_CONTENT_MESSAGE,
	ERROR_INVALID_DEVICE_TYPE,
	ERROR_INVALID_ANDROID_DEVICE_TOKEN,
	ERROR_INVALID_IOS_DEVICE_TOKEN,
	ERROR_USER_INVALID_EMAIL,
	ERROR_USER_INVALID_PHONE_NUMBER,
	ERROR_USER_INVALID_PASSWORD,
	ERROR_INVALID_CODE,
	ERROR_USER_INVALID_USERNAME,
	ERROR_USER_INVALID_COUNTRY,
	ERROR_USER_UNDER_GIVEN_USERNAME_ALREADY_REGISTERED,
	ERROR_USER_INVALID_ADVERTISING_ID,
	
	
	ERROR_INVITED_USER_ALREADY_REGISTERED,
	ERROR_INVITATION_ALREADY_SENT_TO_THIS_USER,
	ERROR_EXCEEDED_TOTAL_NUMBER_OF_ALLOWED_REFERRAL_INVITATIONS,
	ERROR_REQUEST_VALIDATION_FAILED, //means hash code for request url is different than expected - possibly someone tampering with url parameters!
	
	//for user event interception
	ERROR_MISSING_OFFER_CURRENCY_CODE,
	ERROR_INCORRECT_PHONE_NUMBER,
	ERROR_NO_DENOMINATION_MODEL_REGISTERED_FOR_GIVEN_REWARD_TYPE_NAME,
	ERROR_NO_REWARD_TYPE_NAME_DEFINED,
	ERROR_OFFER_NOT_FOUND,
	ERROR_INVALID_OFFER,
	ERROR_OFFER_WALL_NOT_FOUND,
	ERROR_AD_NETWORK_NOT_FOUND,
	ERROR_DUPLICATE_TRANSACTION_IDENTIFIED, 
	ERROR_INVALID_AD_PROVIDER,
	ERROR_INVALID_DEVICE_ID,
	ERROR_INVALID_PHONE_ID,
	ERROR_INVALID_ADVERTISING_ID,
	
	//for conversion inerception  
	ERROR_DUPLICATE_CONVERSION_IDENTIFIED,
	ERROR_UNABLE_TO_IDENTIFY_TRANSACTION,
	ERROR_USER_FRAUD_DUPLICATE_CONVERSION_DETECTED,
	ERROR_UNABLE_TO_ISSUE_USER_REWARD,
	//for reward interception
	ERROR_INVALID_TRANSACTION_ID, 
	ERROR_ITEM_NOT_FOUND,
	ERROR_UNKNOWN_RESPONSE_CODE,
	ERROR_AUTHENTICATION_FAILURE,
	ERROR_INTERNAL_SERVER_ERROR,
	WARNING_REWARD_ALREADY_INTERCEPTED,
	ERROR_UNABLE_TO_IDENTIFY_MATCHING_DENOMINATION_MODEL,
	ERROR_UNABLE_TO_IDENTIFY_MATCHING_REWARD_TYPE,
	ERROR_UNKNOWN_REWARD_STATUS_CODE,
	
	//application version
	ERROR_INVALID_VERSION,
	ERROR_INVALID_APPLICATION,
	
	ERROR_INVALID_USER,
	//unknown reward type
	ERROR_INVALID_REWARD_TYPE,
	
	//wallet:
	ERROR_INVALID_USERNAME_PASSWORD_COMBINATION,
	ERROR_INVALID_WALLET_PAYOUT_CARRIER,
	ERROR_INVALID_WALLET_DATA,
	ERROR_INSUFFICIENT_WALLET_BALANCE,
	ERROR_INSUFFICIENT_AMOUNT_FOR_PAYOUT,
	ERROR_INVALID_WALLET_PAYOUT_OFFER,
	ERROR_INVALID_APPLICATION_NAME,
	ERROR_MINIMAL_GEO_PAYOUT_VALUE,
	
	ERROR_DEVICE_ALREADY_LISTED,
	ERROR_NO_MIGRATION,
	
	//login with device info
	ERROR_USER_NOT_FOUND,
	ERROR_INVALID_USER_DATA,
	
	//fb invitation
	ERROR_GENERATING_FB_INVITATION_CODE,
	
	//spinner
	ERROR_SPINNER_NO_USES,
	ERROR_SPINNER_GENERATING_REWARD,
	
	//friend
	ERROR_INVALID_FRIEND_DATA,
	ERROR_FRIEND_IS_IN_LIST,
	ERROR_FRIEND_IS_NOT_IN_LIST,
	
	//external server manager
	ERROR_SERVER_IS_NOT_LISTED,
	
	//snapdeal
	ERROR_INVALID_OFFER_DATA,
	
	//reward ticket
	ERROR_INVALID_REWARD_NAME,
	ERROR_INVALID_CREDIT_POINTS, 
}
