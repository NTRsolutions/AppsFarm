package is.web.services.application;


import is.web.beans.offerRewardTypes.ImageBannerEntity;
import is.web.services.APIResponse;

import java.util.List;

public class ApplicationConfiguration {

	private double attendanceValue;
	private String currencyCode;
	private double videoRewardAmount;
	
	public double getAttendanceValue() {
		return attendanceValue;
	}

	public void setAttendanceValue(double attendanceValue) {
		this.attendanceValue = attendanceValue;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public double getVideoRewardAmount() {
		return videoRewardAmount;
	}

	public void setVideoRewardAmount(double videoRewardAmount) {
		this.videoRewardAmount = videoRewardAmount;
	}
	
	

}
