package is.ejb.bl.spinner;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.math.BigDecimal;

public class RewardInterval {

	private SpinnerRewardEntity spinnerRewardEntity;
	private BigDecimal fromValue;
	private boolean fromClosed;
	private BigDecimal toValue;
	private boolean toClosed;
	public SpinnerRewardEntity getSpinnerRewardEntity() {
		return spinnerRewardEntity;
	}
	public void setSpinnerRewardEntity(SpinnerRewardEntity spinnerRewardEntity) {
		this.spinnerRewardEntity = spinnerRewardEntity;
	}
	public BigDecimal getFromValue() {
		return fromValue;
	}
	public void setFromValue(BigDecimal fromValue) {
		this.fromValue = fromValue;
	}
	public boolean isFromClosed() {
		return fromClosed;
	}
	public void setFromClosed(boolean fromClosed) {
		this.fromClosed = fromClosed;
	}
	public BigDecimal getToValue() {
		return toValue;
	}
	public void setToValue(BigDecimal toValue) {
		this.toValue = toValue;
	}
	public boolean isToClosed() {
		return toClosed;
	}
	public void setToClosed(boolean toClosed) {
		this.toClosed = toClosed;
	}
	@Override
	public String toString() {
		return "RewardInterval [spinnerRewardEntity=" + spinnerRewardEntity + ", fromValue=" + fromValue
				+ ", fromClosed=" + fromClosed + ", toValue=" + toValue + ", toClosed=" + toClosed + "]";
	}
	
	
}

