package is.ejb.bl.monitoring.operation;

import is.ejb.bl.business.Application;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SystemOpsStatsHolder {
	//TODO ERROR SIDE
	//total count of rejected offers (due to low payout treshold)
	//total count of rejected offers (due to lack of currency support)
	//total count of rejected offers (due to no currency data)
	//total count of failed clicks (due to badly formatted phone number)
	//total count of failed conversions (due to duplicate triggers)

	private long intervalTime; //in minutes during which below KPI's are calculated
	private double offerSingleGenerationThroughput=0;
	private double offerCOWGenerationThroughput=0;
	private double offerCOWRequestThroughput=0;
	private double offerIndividualGenerationThroughput=0;
	private double offerClickThroughput=0;
	private double offerConversionThroughput=0;
	private double offerRewardRequestThroughput=0;
	private double offerRewardResponseThroughput=0;
	
	//errors / warnings
	private long errorsCount;//total count of all errors (predefined history longerval)
	private long warningsCount;//total count of all warnings (predefined history longerval)

	//cow selection
	private long offersCOWIdsSelectCount;
	private long offersCOWIdsSelectFailedCount;
	private long offersCOWSelectByIdCount;
	private long offersCOWSelectByIdFailedCount;
	
	//offers generation
	private long offersSingleWallGenerationCount;//total count of composite offers generation of which failed
	private long offersSingleWallGenerationFailedCount;//total count of composite offers generation of which failed

	private long offersCompositeWallGenerationCount;//total count of composite offers generation of which failed
	private long offersCompositeWallGenerationFailedCount;//total count of composite offers generation of which failed
	private long offersCreatedCount;
	private long offersRejectedCount; //total count of rejected offers 
	private long offersInsufficientCount;//total count of Application.INSUFFICIENT_OFFERS errors
	private long offersPayoutBelowTresholdCount;//total count of Application.INSUFFICIENT_OFFERS errors
	private long offersNoCurrencyDefinedCount;//total count of Application.INSUFFICIENT_OFFERS errors

	private long offersNoImageDefinedCount;
	private long offersNoCountryCodesSuppliedByOfferProviderCount;
	private long offersNoTargetDevicesSuppliedByOfferProviderCount;
	private long offersNoSupportedPayoutCurrencyDefinedCount;
	private long offersRejectedDuplicatesCount;
	
	//clicks
	private long clicksIdentifiedCount;
	private long clicksSuccessfulCount;
	private long clicksFailedCount;
	
	//conversions
	private long conversionsIdentifiedCount;
	private long conversionsSuccessfulCount;
	private long conversionsFailedCount;

	//reward requests
	private long rewardRequestsIdentifiedCount;
	private long rewardRequestsSuccessfulCount;
	private long rewardRequestsFailedCount;

	//reward responses
	private long rewardResponsesIdentifiedCount;
	private long rewardResponsesSuccessfulCount;
	private long rewardResponsesFailedCount;
	
	//reward status values
	private long rewardReponsesesWithSuccessStatusIdentifiedCount;

	//reward notifications to mobile app
	private long rewardNotificationRequestsIdentifiedCount;
	private long rewardNotificationRequestsSuccessCount;
	private long rewardNotificationRequestsFailedCount;

	
	private long rowsTransactionsCount;//total count of rows in UserEvent table
	private long rowsOffersCount; //total count of rows in Offer table

	public long getIntervalTime() {
		return intervalTime;
	}
	public void setIntervalTime(long intervalTime) {
		this.intervalTime = intervalTime;
	}
	public long getErrorsCount() {
		return errorsCount;
	}
	public void setErrorsCount(long errorsCount) {
		this.errorsCount = errorsCount;
	}
	public long getWarningsCount() {
		return warningsCount;
	}
	public void setWarningsCount(long warningsCount) {
		this.warningsCount = warningsCount;
	}
	public long getOffersRejectedCount() {
		return offersRejectedCount;
	}
	public void setOffersRejectedCount(long offersRejectedCount) {
		this.offersRejectedCount = offersRejectedCount;
	}
	public long getClicksFailedCount() {
		return clicksFailedCount;
	}
	public void setClicksFailedCount(long clicksFailedCount) {
		this.clicksFailedCount = clicksFailedCount;
	}
	public long getRewardRequestsIdentifiedCount() {
		return rewardRequestsIdentifiedCount;
	}
	public void setRewardRequestsIdentifiedCount(long rewardRequestsIdentifiedCount) {
		this.rewardRequestsIdentifiedCount = rewardRequestsIdentifiedCount;
	}
	public long getRewardRequestsSuccessfulCount() {
		return rewardRequestsSuccessfulCount;
	}
	public void setRewardRequestsSuccessfulCount(long rewardRequestsSuccessfulCount) {
		this.rewardRequestsSuccessfulCount = rewardRequestsSuccessfulCount;
	}
	public long getRewardRequestsFailedCount() {
		return rewardRequestsFailedCount;
	}
	public void setRewardRequestsFailedCount(long rewardRequestsFailedCount) {
		this.rewardRequestsFailedCount = rewardRequestsFailedCount;
	}
	public long getRowsTransactionsCount() {
		return rowsTransactionsCount;
	}
	public void setRowsTransactionsCount(long rowsTransactionsCount) {
		this.rowsTransactionsCount = rowsTransactionsCount;
	}
	public long getRowsOffersCount() {
		return rowsOffersCount;
	}
	public void setRowsOffersCount(long rowsOffersCount) {
		this.rowsOffersCount = rowsOffersCount;
	}
	public long getOffersInsufficientCount() {
		return offersInsufficientCount;
	}
	public void setOffersInsufficientCount(long offersInsufficientCount) {
		this.offersInsufficientCount = offersInsufficientCount;
	}
	public long getOffersPayoutBelowTresholdCount() {
		return offersPayoutBelowTresholdCount;
	}
	public void setOffersPayoutBelowTresholdCount(
			long offersPayoutBelowTresholdCount) {
		this.offersPayoutBelowTresholdCount = offersPayoutBelowTresholdCount;
	}
	public long getOffersNoCurrencyDefinedCount() {
		return offersNoCurrencyDefinedCount;
	}
	public void setOffersNoCurrencyDefinedCount(long offersNoCurrencyDefinedCount) {
		this.offersNoCurrencyDefinedCount = offersNoCurrencyDefinedCount;
	}
	public long getOffersCompositeWallGenerationFailedCount() {
		return offersCompositeWallGenerationFailedCount;
	}
	public void setOffersCompositeWallGenerationFailedCount(
			long offersCompositeWallGenerationFailedCount) {
		this.offersCompositeWallGenerationFailedCount = offersCompositeWallGenerationFailedCount;
	}
	public long getClicksIdentifiedCount() {
		return clicksIdentifiedCount;
	}
	public void setClicksIdentifiedCount(long clicksIdentifiedCount) {
		this.clicksIdentifiedCount = clicksIdentifiedCount;
	}
	public long getClicksSuccessfulCount() {
		return clicksSuccessfulCount;
	}
	public void setClicksSuccessfulCount(long clicksSuccessfulCount) {
		this.clicksSuccessfulCount = clicksSuccessfulCount;
	}
	public long getConversionsIdentifiedCount() {
		return conversionsIdentifiedCount;
	}
	public void setConversionsIdentifiedCount(long conversionsIdentifiedCount) {
		this.conversionsIdentifiedCount = conversionsIdentifiedCount;
	}
	public long getConversionsSuccessfulCount() {
		return conversionsSuccessfulCount;
	}
	public void setConversionsSuccessfulCount(long conversionsSuccessfulCount) {
		this.conversionsSuccessfulCount = conversionsSuccessfulCount;
	}
	public long getConversionsFailedCount() {
		return conversionsFailedCount;
	}
	public void setConversionsFailedCount(long conversionsFailedCount) {
		this.conversionsFailedCount = conversionsFailedCount;
	}
	public long getRewardResponsesIdentifiedCount() {
		return rewardResponsesIdentifiedCount;
	}
	public void setRewardResponsesIdentifiedCount(
			long rewardResponsesIdentifiedCount) {
		this.rewardResponsesIdentifiedCount = rewardResponsesIdentifiedCount;
	}
	public long getRewardResponsesSuccessfulCount() {
		return rewardResponsesSuccessfulCount;
	}
	public void setRewardResponsesSuccessfulCount(
			long rewardResponsesSuccessfulCount) {
		this.rewardResponsesSuccessfulCount = rewardResponsesSuccessfulCount;
	}
	public long getRewardResponsesFailedCount() {
		return rewardResponsesFailedCount;
	}
	public void setRewardResponsesFailedCount(long rewardResponsesFailedCount) {
		this.rewardResponsesFailedCount = rewardResponsesFailedCount;
	}
	public long getOffersCOWIdsSelectCount() {
		return offersCOWIdsSelectCount;
	}
	public void setOffersCOWIdsSelectCount(long offersCOWIdsSelectCount) {
		this.offersCOWIdsSelectCount = offersCOWIdsSelectCount;
	}
	public long getOffersCOWIdsSelectFailedCount() {
		return offersCOWIdsSelectFailedCount;
	}
	public void setOffersCOWIdsSelectFailedCount(long offersCOWIdsSelectFailedCount) {
		this.offersCOWIdsSelectFailedCount = offersCOWIdsSelectFailedCount;
	}
	public long getOffersCOWSelectByIdCount() {
		return offersCOWSelectByIdCount;
	}
	public void setOffersCOWSelectByIdCount(long offersCOWSelectByIdCount) {
		this.offersCOWSelectByIdCount = offersCOWSelectByIdCount;
	}
	public long getOffersCOWSelectByIdFailedCount() {
		return offersCOWSelectByIdFailedCount;
	}
	
	public long getOffersCompositeWallGenerationCount() {
		return offersCompositeWallGenerationCount;
	}
	public void setOffersCompositeWallGenerationCount(
			long offersCompositeWallGenerationCount) {
		this.offersCompositeWallGenerationCount = offersCompositeWallGenerationCount;
	}
	public void setOffersCOWSelectByIdFailedCount(
			long offersCOWSelectByIdFailedCount) {
		this.offersCOWSelectByIdFailedCount = offersCOWSelectByIdFailedCount;
	}
	public double getThroughputOfferCOWRequest() {
		if (offersCOWSelectByIdCount>0) {
			offerCOWRequestThroughput = (double)offersCOWSelectByIdCount/((double)intervalTime*(double)60);
		}
		return round(offerCOWRequestThroughput,2);
	}
	public void setOfferCOWRequestThroughput(double offerCOWRequestThroughput) {
		this.offerCOWRequestThroughput = offerCOWRequestThroughput;
	}
	public double getThroughputOfferClick() {
		if(clicksIdentifiedCount>0){
			offerClickThroughput = (double)clicksIdentifiedCount/((double)intervalTime*(double)60); 
		}
		return round(offerClickThroughput,2);
	}
	public void setOfferClickThroughput(double offerClickThroughput) {
		this.offerClickThroughput = offerClickThroughput;
	}
	public double getThroughputOfferConversion() {
		if(conversionsIdentifiedCount > 0){
			offerConversionThroughput = (double)conversionsIdentifiedCount/((double)intervalTime*(double)60); 
		}
		return round(offerConversionThroughput,2);
	}
	public void setOfferConversionThroughput(double offerConversionThroughput) {
		this.offerConversionThroughput = offerConversionThroughput;
	}
	public double getThroughputOfferRewardRequest() {
		if(rewardRequestsIdentifiedCount > 0) {
			offerRewardRequestThroughput = (double)rewardRequestsIdentifiedCount/((double)intervalTime*(double)60); 
		}
		return round(offerRewardRequestThroughput,2);
	}
	public void setOfferRewardRequestThroughput(double offerRewardRequestThroughput) {
		this.offerRewardRequestThroughput = offerRewardRequestThroughput;
	}
	public double getThroughputOfferRewardResponse() {
		if(rewardResponsesIdentifiedCount > 0) {
			offerRewardResponseThroughput = (double)rewardResponsesIdentifiedCount/((double)intervalTime*(double)60); 
		}
		return round(offerRewardResponseThroughput,2);
	}
	public void setOfferRewardResponseThroughput(
			double offerRewardResponseThroughput) {
		this.offerRewardResponseThroughput = offerRewardResponseThroughput;
	}

    public double getThroughputOfferCOWGeneration() {
		if(offersCompositeWallGenerationCount > 0) {
			offerCOWGenerationThroughput = (double)offersCompositeWallGenerationCount/((double)intervalTime*(double)60); 
		}
		return round(offerCOWGenerationThroughput,2);
	}
	public void setOfferCOWGenerationThroughput(double offerCOWGenerationThroughput) {
		this.offerCOWGenerationThroughput = offerCOWGenerationThroughput;
	}
	
	public double getThroughputOfferSingleWallGeneration() {
		if(offersSingleWallGenerationCount > 0) {
			offerSingleGenerationThroughput = (double)offersSingleWallGenerationCount/((double)intervalTime*(double)60); 
		}
		return round(offerSingleGenerationThroughput,2);
	}
	public void setOfferSingleGenerationThroughput(
			double offerSingleGenerationThroughput) {
		this.offerSingleGenerationThroughput = offerSingleGenerationThroughput;
	}
	public long getOffersSingleWallGenerationCount() {
		return offersSingleWallGenerationCount;
	}
	public void setOffersSingleWallGenerationCount(
			long offersSingleWallGenerationCount) {
		this.offersSingleWallGenerationCount = offersSingleWallGenerationCount;
	}
	public long getOffersSingleWallGenerationFailedCount() {
		return offersSingleWallGenerationFailedCount;
	}
	public void setOffersSingleWallGenerationFailedCount(
			long offersSingleWallGenerationFailedCount) {
		this.offersSingleWallGenerationFailedCount = offersSingleWallGenerationFailedCount;
	}
	
	public long getOffersCreatedCount() {
		return offersCreatedCount;
	}
	public void setOffersCreatedCount(long offersCreatedCount) {
		this.offersCreatedCount = offersCreatedCount;
	}
	
	
	public double getThroughputOfferIndividualGeneration() {
		if(offersCreatedCount > 0) {
			offerIndividualGenerationThroughput = (double)offersCreatedCount/((double)intervalTime*(double)60); 
		}
		return round(offerIndividualGenerationThroughput,2);
	}
	public void setOfferIndividualGenerationThroughput(
			double offerIndividualGenerationThroughput) {
		this.offerIndividualGenerationThroughput = offerIndividualGenerationThroughput;
	}
	
	public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }
	
	public long getRewardReponsesesWithSuccessStatusIdentifiedCount() {
		return rewardReponsesesWithSuccessStatusIdentifiedCount;
	}
	public void setRewardReponsesesWithSuccessStatusIdentifiedCount(
			long rewardReponsesesWithSuccessStatusIdentifiedCount) {
		this.rewardReponsesesWithSuccessStatusIdentifiedCount = rewardReponsesesWithSuccessStatusIdentifiedCount;
	}
	public long getRewardNotificationRequestsIdentifiedCount() {
		return rewardNotificationRequestsIdentifiedCount;
	}
	public void setRewardNotificationRequestsIdentifiedCount(
			long rewardNotificationRequestsIdentifiedCount) {
		this.rewardNotificationRequestsIdentifiedCount = rewardNotificationRequestsIdentifiedCount;
	}
	public long getRewardNotificationRequestsFailedCount() {
		return rewardNotificationRequestsFailedCount;
	}
	public void setRewardNotificationRequestsFailedCount(
			long rewardNotificationRequestsFailedCount) {
		this.rewardNotificationRequestsFailedCount = rewardNotificationRequestsFailedCount;
	}
	public long getRewardNotificationRequestsSuccessCount() {
		return rewardNotificationRequestsSuccessCount;
	}
	public void setRewardNotificationRequestsSuccessCount(
			long rewardNotificationRequestsSuccessCount) {
		this.rewardNotificationRequestsSuccessCount = rewardNotificationRequestsSuccessCount;
	}
	public long getOffersNoImageDefinedCount() {
		return offersNoImageDefinedCount;
	}
	public void setOffersNoImageDefinedCount(long offersNoImageDefinedCount) {
		this.offersNoImageDefinedCount = offersNoImageDefinedCount;
	}
	public long getOffersNoCountryCodesSuppliedByOfferProviderCount() {
		return offersNoCountryCodesSuppliedByOfferProviderCount;
	}
	public void setOffersNoCountryCodesSuppliedByOfferProviderCount(
			long offersNoCountryCodesSuppliedByOfferProviderCount) {
		this.offersNoCountryCodesSuppliedByOfferProviderCount = offersNoCountryCodesSuppliedByOfferProviderCount;
	}
	public long getOffersNoTargetDevicesSuppliedByOfferProviderCount() {
		return offersNoTargetDevicesSuppliedByOfferProviderCount;
	}
	public void setOffersNoTargetDevicesSuppliedByOfferProviderCount(
			long offersNoTargetDevicesSuppliedByOfferProviderCount) {
		this.offersNoTargetDevicesSuppliedByOfferProviderCount = offersNoTargetDevicesSuppliedByOfferProviderCount;
	}
	public long getOffersNoSupportedPayoutCurrencyDefinedCount() {
		return offersNoSupportedPayoutCurrencyDefinedCount;
	}
	public void setOffersNoSupportedPayoutCurrencyDefinedCount(
			long offersNoSupportedPayoutCurrencyDefinedCount) {
		this.offersNoSupportedPayoutCurrencyDefinedCount = offersNoSupportedPayoutCurrencyDefinedCount;
	}
	public long getOffersRejectedDuplicatesCount() {
		return offersRejectedDuplicatesCount;
	}
	public void setOffersRejectedDuplicatesCount(long offersRejectedDuplicatesCount) {
		this.offersRejectedDuplicatesCount = offersRejectedDuplicatesCount;
	}

	
}
