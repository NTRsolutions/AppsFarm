package is.web.beans.reporting;

import is.ejb.bl.reporting.ReportDH;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.users.LoginBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

@ManagedBean(name = "providerReportBean")
@SessionScoped
public class ProviderReportBean {
	
	@Inject
	private Logger logger;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private ReportingManager reportingManager;
	
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();
	private List<SelectItem> rewardTypes;
	private String rewardTypeName;
	private ReportDH report;
	
	@PostConstruct
	public void init(){
		try {
			loadRewardTypes();
			rewardTypeName = rewardTypes.get(0).getValue().toString();
			refreshReport();
		} catch (Exception exc) {
			logger.severe("Load reward types exception");
		}
	}
	
	public void refreshReport(){
		report = reportingManager.getReportData(getRealm(), startDate, endDate, rewardTypeName);
		RequestContext.getCurrentInstance().update("tabView:idProviderReportPanel");
	}
	
	private RealmEntity getRealm(){
		FacesContext fc = FacesContext.getCurrentInstance();
		LoginBean loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
		RealmEntity realm = loginBean.getUser().getRealm();
		
		return realm;
	}
	
	private void loadRewardTypes() throws Exception{
		List<RewardTypeEntity> rewardTypeEntityList = daoRewardType.findAll();
		this.rewardTypes = new ArrayList<SelectItem>();
		
		for (RewardTypeEntity entity : rewardTypeEntityList) {
			this.rewardTypes.add(new SelectItem(entity.getName(),entity.getName()));
		}
	}
	
	private Date getDefaultStartDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -2);
		
		return calendar.getTime();
	}

	private Date getDefaultEndDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		
		return calendar.getTime();
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

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	public List<SelectItem> getRewardTypes() {
		return rewardTypes;
	}

	public void setRewardTypes(List<SelectItem> rewardTypes) {
		this.rewardTypes = rewardTypes;
	}

	public ReportDH getReport() {
		return report;
	}
	
	public void setReport(ReportDH report) {
		this.report = report;
	}
	
}
