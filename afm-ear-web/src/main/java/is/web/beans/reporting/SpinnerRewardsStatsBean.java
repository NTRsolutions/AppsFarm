package is.web.beans.reporting;

import is.ejb.bl.rewardSystems.radius.SpinnerRewardsReport;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

@ManagedBean(name = "spinnerRewardsBean")
@SessionScoped
public class SpinnerRewardsStatsBean {

	@Inject
	private Logger logger;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private SpinnerManager spinnerManager;

	private List<RewardTypeEntity> rewardTypeList;
	private List<SelectItem> rewardTypeSelectItemList;

	private Date startDate;
	private Date endDate;
	private String selectedRewardType;
	private String spinnerReportResult = "";

	@PostConstruct
	public void init() {
		try {
			rewardTypeList = daoRewardType.findAll();
			if (rewardTypeList != null) {
				rewardTypeSelectItemList = new ArrayList<SelectItem>();
				for (RewardTypeEntity rewardType : rewardTypeList) {
					rewardTypeSelectItemList.add(new SelectItem(rewardType.getName(), rewardType.getName()));
				}
			}

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -7);
			startDate = cal.getTime();
			endDate = new Date();

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void generateReport() {
		logger.info("Generating report for startDate:" + startDate + " endDate:" + endDate + " selectedRewardType: "
				+ selectedRewardType);
		SpinnerRewardsReport report = spinnerManager.generateReportInDateRange(startDate, endDate, selectedRewardType);
		spinnerReportResult = "<b>Report:</b>";
		spinnerReportResult += "<br/> Reward type: " + report.getRewardType();
		spinnerReportResult += "<br/> Total spins: " + report.getTotalSpins();
		spinnerReportResult += "<br/> Loss from money events:" + report.getLoss();
		spinnerReportResult += "<br/> Start date:" + report.getStartDate();
		spinnerReportResult += "<br/> End date:" + report.getEndDate();
		spinnerReportResult += "<br/> Total unique user count: " + report.getUserCount();
		spinnerReportResult += "<br/><br/>";
		for (SpinnerRewardEntity spinnerReward : report.getSpinRewardsMap().keySet()) {
			spinnerReportResult += "<br/>===============================";
			spinnerReportResult += "<br/> Spinner reward name: " + spinnerReward.getRewardName();
			spinnerReportResult += "<br/> Spinner reward type: " + spinnerReward.getRewardType();
			spinnerReportResult += "<br/> Spinner reward value: " + spinnerReward.getRewardValue();
			int count = report.getSpinRewardsMap().get(spinnerReward);
			spinnerReportResult += "<br/> Event count: " + count;
			float percentage = (count * 100f) / report.getTotalSpins();
			spinnerReportResult += "<br/> Event percentage: " + percentage + "%";
			spinnerReportResult += "<br/> User unique count: " + report.getSpinRewardsUserMap().get(spinnerReward).size();

		}

		RequestContext.getCurrentInstance().update("tabView:tabReportStats");
		System.out.println("UPDATE UPDATE!!!");

	}

	public List<RewardTypeEntity> getRewardTypeList() {
		return rewardTypeList;
	}

	public void setRewardTypeList(List<RewardTypeEntity> rewardTypeList) {
		this.rewardTypeList = rewardTypeList;
	}

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

	public String getSelectedRewardType() {
		return selectedRewardType;
	}

	public void setSelectedRewardType(String selectedRewardType) {
		this.selectedRewardType = selectedRewardType;
	}

	public List<SelectItem> getRewardTypeSelectItemList() {
		return rewardTypeSelectItemList;
	}

	public void setRewardTypeSelectItemList(List<SelectItem> rewardTypeSelectItemList) {
		this.rewardTypeSelectItemList = rewardTypeSelectItemList;
	}

	public String getSpinnerReportResult() {
		return spinnerReportResult;
	}

	public void setSpinnerReportResult(String spinnerReportResult) {
		this.spinnerReportResult = spinnerReportResult;
	}

}
