package is.ejb.bl.offerFilter;

import java.sql.Timestamp;

public class BlockedOffer {
	private boolean active;
	private boolean renderConversionStats = false;
	
	private String id;
	private String sourceId;
	private String rewardType;
	private String title;
	private String command;
	private String adProviderCodeName;
	private Timestamp timestamp; 
	private Timestamp lastUpdateTime;
	private String blockType;
	private int sumClicks;
	private int sumConversions;
	private double convRatio;

	
	private String rowKey;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAdProviderCodeName() {
		return adProviderCodeName;
	}
	public void setAdProviderCodeName(String adProviderCodeName) {
		this.adProviderCodeName = adProviderCodeName;
	}
	public String getRowKey() {
		return title+adProviderCodeName+id;
	}
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	public String getBlockType() {
		return blockType;
	}
	public void setBlockType(String blockType) {
		this.blockType = blockType;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public int getSumClicks() {
		return sumClicks;
	}
	public void setSumClicks(int sumClicks) {
		this.sumClicks = sumClicks;
	}
	public int getSumConversions() {
		return sumConversions;
	}
	public void setSumConversions(int sumConversions) {
		this.sumConversions = sumConversions;
	}
	public double getConvRatio() {
		return convRatio;
	}
	public void setConvRatio(double convRatio) {
		this.convRatio = convRatio;
	}
	public boolean isRenderConversionStats() {
		return renderConversionStats;
	}
	public void setRenderConversionStats(boolean renderConversionStats) {
		this.renderConversionStats = renderConversionStats;
	}
	public Timestamp getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(Timestamp lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}

	
}
