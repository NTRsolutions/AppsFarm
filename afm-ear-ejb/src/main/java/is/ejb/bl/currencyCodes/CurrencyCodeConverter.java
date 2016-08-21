package is.ejb.bl.currencyCodes;

import is.ejb.bl.business.CountryCode;
import is.ejb.bl.business.OfferCurrency;

public class CurrencyCodeConverter {

	//mobile apps are hardcoded to receive following currency codes and display correct currency characters based on geo code
	// TODO in future expose UI for adjusting payouts for different geos
	public static synchronized String getRewardCurrencyCodeByGeo(String countryCode) {
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

	//on server-side we display user friendly currency name during notifications via gcm and donkey
	public static synchronized String getUserFriendlyCurrencyCodeByGeo(String countryCode) {
		if (countryCode.equals(OfferCurrency.KSH.toString())) {
			return "Ksh";
		} else if (countryCode.equals(OfferCurrency.INR.toString())) {
			return "Rs";
		} else if (countryCode.equals(OfferCurrency.ZAR.toString())) {
			return "ZAR";
		} else if (countryCode.equals(OfferCurrency.GBP.toString())) {
			return "GBP";
		} else
			return "";//CountryCode.UNKNOWN.toString();
	}

}
