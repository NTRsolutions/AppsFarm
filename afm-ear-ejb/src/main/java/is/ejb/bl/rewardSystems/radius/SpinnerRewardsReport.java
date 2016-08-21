package is.ejb.bl.rewardSystems.radius;

import is.ejb.dl.entities.SpinnerRewardEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinnerRewardsReport {

	private Date startDate;
	private Date endDate;
	private String rewardType;
	private int totalSpins;
	private HashMap<SpinnerRewardEntity,Integer> spinRewardsMap;
	private HashMap<SpinnerRewardEntity, ArrayList<Integer>> spinRewardsUserMap;
	private int userCount;
	private double profit;
	private double loss;
	
	
	
	
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public int getTotalSpins() {
		return totalSpins;
	}
	public void setTotalSpins(int totalSpins) {
		this.totalSpins = totalSpins;
	}
	
	public double getProfit() {
		return profit;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}
	public double getLoss() {
		return loss;
	}
	public void setLoss(double loss) {
		this.loss = loss;
	}
	
	
	public HashMap<SpinnerRewardEntity, Integer> getSpinRewardsMap() {
		return spinRewardsMap;
	}
	public void setSpinRewardsMap(HashMap<SpinnerRewardEntity, Integer> spinRewardsMap) {
		this.spinRewardsMap = spinRewardsMap;
	}
	public HashMap<SpinnerRewardEntity, ArrayList<Integer>> getSpinRewardsUserMap() {
		return spinRewardsUserMap;
	}
	public void setSpinRewardsUserMap(HashMap<SpinnerRewardEntity, ArrayList<Integer>> spinRewardsUserMap) {
		this.spinRewardsUserMap = spinRewardsUserMap;
	}
	public int getUserCount() {
		return userCount;
	}
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	@Override
	public String toString() {
		return "SpinnerRewardsReport [startDate=" + startDate + ", endDate=" + endDate + ", rewardType=" + rewardType
				+ ", totalSpins=" + totalSpins + ", spinRewardsMap=" + spinRewardsMap + ", spinRewardsUserMap="
				+ spinRewardsUserMap + ", userCount=" + userCount + ", profit=" + profit + ", loss=" + loss + "]";
	}
	
	
	
	

	
	
	
	
	
}
