package is.ejb.bl.reporting;

import is.ejb.bl.rewardSystems.radius.SpinnerRewardsReport;

import java.util.Date;

public class ReportDH {
	private String rewardTypeName;
	private ReportPeriodName reportPeriodName;
	private String reportPeriodNameString;
	private Date dateStart;
	private Date dateEnd;
	
	private long clicksSum;
	private long conversionsSum;
	private long registrationsSum;
	
	private double conversionRate;
	
	private double rewardSumInTargetCurrency;
	private double profitSumInTargetCurrency;

	private double[][] userRetentionMatrix = null;
	
	private long snapdealClicksSum;
	private long snapdealConversionSum;
	private long snapdealConversionApprovedSum;
	
	private long quidcoClicksSum;
	private long quidcoConversionSum;
	private long quidcoConversionApprovedSum;
	
	private long spinSum;
	private SpinnerRewardsReport spinRewards;
	private long referralClicksSum;
	private long referralSuccessSum;
	private double referralLoseSum;
	private double profitFromVideos;
	private double profitFromSnapdeal;
	private double profitFromQuidco;
	
	private int videosCount;
	private int snapdealCount;
	private int quidcoCount;
	private long wallSelectionsSum;
	private double totalPayout;
	private double totalExpenses;
	private double totalProfit;
	
	private double totalInstallPayout;
	private double totalInstallExpenses;
	private double totalInstallProfit;
	
	private long totalClicksDao;
	private long totalUniqueClicksDao;
	private long totalConversionsDao;
	private double conversionRateDao;
	private long totalSpinProfit;
	
	public Date getDateStart() {
		return dateStart;
	}
	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}
	public Date getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}
	public long getClicksSum() {
		return clicksSum;
	}
	public void setClicksSum(long clicksSum) {
		this.clicksSum = clicksSum;
	}
	public long getConversionsSum() {
		return conversionsSum;
	}
	public void setConversionsSum(long conversionsSum) {
		this.conversionsSum = conversionsSum;
	}
	public double getConversionRate() {
		return conversionRate;
	}
	public void setConversionRate(double conversionRate) {
		this.conversionRate = conversionRate;
	}
	public double getRewardSumInTargetCurrency() {
		return rewardSumInTargetCurrency;
	}
	public void setRewardSumInTargetCurrency(double rewardSumInTargetCurrency) {
		this.rewardSumInTargetCurrency = rewardSumInTargetCurrency;
	}
	public double getProfitSumInTargetCurrency() {
		return profitSumInTargetCurrency;
	}
	public void setProfitSumInTargetCurrency(double profitSumInTargetCurrency) {
		this.profitSumInTargetCurrency = profitSumInTargetCurrency;
	}
	public ReportPeriodName getReportPeriodName() {
		return reportPeriodName;
	}
	public void setReportPeriodName(ReportPeriodName reportPeriodName) {
		this.reportPeriodName = reportPeriodName;
	}
	public String getReportPeriodNameString() {
		return reportPeriodNameString;
	}
	public void setReportPeriodNameString(String reportPeriodNameString) {
		this.reportPeriodNameString = reportPeriodNameString;
	}
	public String getRewardTypeName() {
		return rewardTypeName;
	}
	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}
	public double[][] getUserRetentionMatrix() {
		return userRetentionMatrix;
	}
	public void setUserRetentionMatrix(double[][] userRetentionMatrix) {
		this.userRetentionMatrix = userRetentionMatrix;
	}
	public long getRegistrationsSum() {
		return registrationsSum;
	}
	public void setRegistrationsSum(long registrationsSum) {
		this.registrationsSum = registrationsSum;
	}
	public long getSnapdealClicksSum() {
		return snapdealClicksSum;
	}
	public void setSnapdealClicksSum(long snapdealClicksSum) {
		this.snapdealClicksSum = snapdealClicksSum;
	}
	public long getSnapdealConversionSum() {
		return snapdealConversionSum;
	}
	public void setSnapdealConversionSum(long snapdealConversionSum) {
		this.snapdealConversionSum = snapdealConversionSum;
	}
	public long getSnapdealConversionApprovedSum() {
		return snapdealConversionApprovedSum;
	}
	public void setSnapdealConversionApprovedSum(long snapdealConversionApprovedSum) {
		this.snapdealConversionApprovedSum = snapdealConversionApprovedSum;
	}
	public long getQuidcoClicksSum() {
		return quidcoClicksSum;
	}
	public void setQuidcoClicksSum(long quidcoClicksSum) {
		this.quidcoClicksSum = quidcoClicksSum;
	}
	public long getQuidcoConversionSum() {
		return quidcoConversionSum;
	}
	public void setQuidcoConversionSum(long quidcoConversionSum) {
		this.quidcoConversionSum = quidcoConversionSum;
	}
	public long getQuidcoConversionApprovedSum() {
		return quidcoConversionApprovedSum;
	}
	public void setQuidcoConversionApprovedSum(long quidcoConversionApprovedSum) {
		this.quidcoConversionApprovedSum = quidcoConversionApprovedSum;
	}
	public long getSpinSum() {
		return spinSum;
	}
	public void setSpinSum(long spinSum) {
		this.spinSum = spinSum;
	}
	public SpinnerRewardsReport getSpinRewards() {
		return spinRewards;
	}
	public void setSpinRewards(SpinnerRewardsReport spinRewards) {
		this.spinRewards = spinRewards;
	}
	public long getReferralClicksSum() {
		return referralClicksSum;
	}
	public void setReferralClicksSum(long referralClicksSum) {
		this.referralClicksSum = referralClicksSum;
	}
	public long getReferralSuccessSum() {
		return referralSuccessSum;
	}
	public void setReferralSuccessSum(long referralSuccessSum) {
		this.referralSuccessSum = referralSuccessSum;
	}
	public double getProfitFromVideos() {
		return profitFromVideos;
	}
	public void setProfitFromVideos(double profitFromVideos) {
		this.profitFromVideos = profitFromVideos;
	}
	public double getProfitFromSnapdeal() {
		return profitFromSnapdeal;
	}
	public void setProfitFromSnapdeal(double profitFromSnapdeal) {
		this.profitFromSnapdeal = profitFromSnapdeal;
	}
	public double getProfitFromQuidco() {
		return profitFromQuidco;
	}
	public void setProfitFromQuidco(double profitFromQuidco) {
		this.profitFromQuidco = profitFromQuidco;
	}
	public int getVideosCount() {
		return videosCount;
	}
	public void setVideosCount(int videosCount) {
		this.videosCount = videosCount;
	}
	public int getSnapdealCount() {
		return snapdealCount;
	}
	public void setSnapdealCount(int snapdealCount) {
		this.snapdealCount = snapdealCount;
	}
	public int getQuidcoCount() {
		return quidcoCount;
	}
	public void setQuidcoCount(int quidcoCount) {
		this.quidcoCount = quidcoCount;
	}
	public double getReferralLoseSum() {
		return referralLoseSum;
	}
	public void setReferralLoseSum(double referralLoseSum) {
		this.referralLoseSum = referralLoseSum;
	}
	public long getWallSelectionsSum() {
		return wallSelectionsSum;
	}
	public void setWallSelectionsSum(long wallSelectionsSum) {
		this.wallSelectionsSum = wallSelectionsSum;
	}
	public double getTotalPayout() {
		return totalPayout;
	}
	public void setTotalPayout(double totalPayout) {
		this.totalPayout = totalPayout;
	}
	public double getTotalExpenses() {
		return totalExpenses;
	}
	public void setTotalExpenses(double totalExpenses) {
		this.totalExpenses = totalExpenses;
	}
	public double getTotalProfit() {
		return totalProfit;
	}
	public void setTotalProfit(double totalProfit) {
		this.totalProfit = totalProfit;
	}
	public double getTotalInstallPayout() {
		return totalInstallPayout;
	}
	public void setTotalInstallPayout(double totalInstallPayout) {
		this.totalInstallPayout = totalInstallPayout;
	}
	public double getTotalInstallExpenses() {
		return totalInstallExpenses;
	}
	public void setTotalInstallExpenses(double totalInstallExpenses) {
		this.totalInstallExpenses = totalInstallExpenses;
	}
	public double getTotalInstallProfit() {
		return totalInstallProfit;
	}
	public void setTotalInstallProfit(double totalInstallProfit) {
		this.totalInstallProfit = totalInstallProfit;
	}
	public long getTotalClicksDao() {
		return totalClicksDao;
	}
	public void setTotalClicksDao(long totalClicksDao) {
		this.totalClicksDao = totalClicksDao;
	}
	public long getTotalUniqueClicksDao() {
		return totalUniqueClicksDao;
	}
	public void setTotalUniqueClicksDao(long totalUniqueClicksDao) {
		this.totalUniqueClicksDao = totalUniqueClicksDao;
	}
	public long getTotalConversionsDao() {
		return totalConversionsDao;
	}
	public void setTotalConversionsDao(long totalConversionsDao) {
		this.totalConversionsDao = totalConversionsDao;
	}
	public double getConversionRateDao() {
		return conversionRateDao;
	}
	public void setConversionRateDao(double conversionRateDao) {
		this.conversionRateDao = conversionRateDao;
	}
	public long getTotalSpinProfit() {
		return totalSpinProfit;
	}
	public void setTotalSpinProfit(long totalSpinProfit) {
		this.totalSpinProfit = totalSpinProfit;
	}

	
	
	
}
