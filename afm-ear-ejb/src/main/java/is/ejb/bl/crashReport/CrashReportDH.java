package is.ejb.bl.crashReport;

import is.ejb.bl.reporting.ReportPeriodName;

import java.util.Date;

public class CrashReportDH {
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
	
	
	
}
