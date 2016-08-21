package is.web.services.application;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.web.beans.offerRewardTypes.ImageBannerEntity;

import java.util.List;

public class ApplicationConfiguration {

	private RespStatusEnum status;
	private RespCodesEnum code;

	private boolean wallStatus;
	private boolean videoStatus;
	private boolean walletStatus;
	private boolean referStatus;
	private boolean spinnerStatus;

	private String wallMessage;
	private String videoMessage;
	private String walletMessage;
	private String referMessage;
	private String spinnerMessage;

	private List<ImageBannerEntity> imageBannerList;
	private double quidcoCommisionPercentage;
	
	 
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ApplicationConfiguration [status=");
		builder.append(status);
		builder.append(", code=");
		builder.append(code);
		builder.append(", wallStatus=");
		builder.append(wallStatus);
		builder.append(", videoStatus=");
		builder.append(videoStatus);
		builder.append(", walletStatus=");
		builder.append(walletStatus);
		builder.append(", referStatus=");
		builder.append(referStatus);
		builder.append(", spinnerStatus=");
		builder.append(spinnerStatus);
		builder.append(", wallMessage=");
		builder.append(wallMessage);
		builder.append(", videoMessage=");
		builder.append(videoMessage);
		builder.append(", walletMessage=");
		builder.append(walletMessage);
		builder.append(", referMessage=");
		builder.append(referMessage);
		builder.append(", spinnerMessage=");
		builder.append(spinnerMessage);
		builder.append(", imageBannerList=");
		builder.append(imageBannerList);
		builder.append(", quidcoCommisionPercentage=");
		builder.append(quidcoCommisionPercentage);
		builder.append("]");
		return builder.toString();
	}

	public RespStatusEnum getStatus() {
		return status;
	}

	public void setStatus(RespStatusEnum status) {
		this.status = status;
	}

	public RespCodesEnum getCode() {
		return code;
	}

	public void setCode(RespCodesEnum code) {
		this.code = code;
	}

	public boolean isWallStatus() {
		return wallStatus;
	}

	public void setWallStatus(boolean wallStatus) {
		this.wallStatus = wallStatus;
	}

	public boolean isVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(boolean videoStatus) {
		this.videoStatus = videoStatus;
	}

	public boolean isWalletStatus() {
		return walletStatus;
	}

	public void setWalletStatus(boolean walletStatus) {
		this.walletStatus = walletStatus;
	}

	public boolean isReferStatus() {
		return referStatus;
	}

	public void setReferStatus(boolean referStatus) {
		this.referStatus = referStatus;
	}

	public boolean isSpinnerStatus() {
		return spinnerStatus;
	}

	public void setSpinnerStatus(boolean spinnerStatus) {
		this.spinnerStatus = spinnerStatus;
	}

	public String getWallMessage() {
		return wallMessage;
	}

	public void setWallMessage(String wallMessage) {
		this.wallMessage = wallMessage;
	}

	public String getVideoMessage() {
		return videoMessage;
	}

	public void setVideoMessage(String videoMessage) {
		this.videoMessage = videoMessage;
	}

	public String getWalletMessage() {
		return walletMessage;
	}

	public void setWalletMessage(String walletMessage) {
		this.walletMessage = walletMessage;
	}

	public String getReferMessage() {
		return referMessage;
	}

	public void setReferMessage(String referMessage) {
		this.referMessage = referMessage;
	}

	public String getSpinnerMessage() {
		return spinnerMessage;
	}

	public void setSpinnerMessage(String spinnerMessage) {
		this.spinnerMessage = spinnerMessage;
	}

	public List<ImageBannerEntity> getImageBannerList() {
		return imageBannerList;
	}

	public void setImageBannerList(List<ImageBannerEntity> imageBannerList) {
		this.imageBannerList = imageBannerList;
	}

	public double getQuidcoCommisionPercentage() {
		return quidcoCommisionPercentage;
	}

	public void setQuidcoCommisionPercentage(double quidcoCommisionPercentage) {
		this.quidcoCommisionPercentage = quidcoCommisionPercentage;
	}

	

}
