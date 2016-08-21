package is.ejb.bl.business;

public enum UserEventCategory {
	INVITE, //when user gets invited to use the app via a referral programme that requires successful registration/activation 
	VIDEO, //when user watches a video and there is a successful watch (conversion)
	INSTALL, //when offer gets installed and reward is instant - this event is selected on the mobile app UI by clicking on the correct install button
	WALLET_REWARD_BUY, //when user buys reward using wallet
	VIDEO_REWARD, // when user watched video
	WALLET_PAY_IN,
	WALLET_PAY_OUT,
	SPINNER,
	QUIDCO,
	SNAPDEAL
	
}
