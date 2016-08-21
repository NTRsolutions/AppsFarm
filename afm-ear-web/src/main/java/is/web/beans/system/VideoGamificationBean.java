package is.web.beans.system;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.offerRewardTypes.RewardTypeDataModelBean;
import is.web.beans.users.LoginBean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;


@ManagedBean(name = "videoGamificationBean")
@SessionScoped
public class VideoGamificationBean {
	@Inject
	DAORewardType daoRewardType;
	private List<RewardTypeEntity> rewardTypeEntityList;
	private VideoGamificationRewardTypeTableDataModelBean rewardTypeDataModelBean;

	private RewardTypeEntity editRewardTypeEntity;

	@PostConstruct
	public void init() {

		String esLog = "";
		try {
			editRewardTypeEntity = new RewardTypeEntity();
			esLog += "Finding all reward types.";
			rewardTypeEntityList = daoRewardType.findAll();
			esLog += "Found total " + rewardTypeEntityList.size() + " elements: " + rewardTypeEntityList + " ... Creating data model bean.";
			rewardTypeDataModelBean = new VideoGamificationRewardTypeTableDataModelBean(rewardTypeEntityList);
			esLog += "  data model bean row count:" + rewardTypeDataModelBean.getRowCount();
			esLog += " Sucessfully created data model bean.";

		} catch (Exception exc) {
			esLog += exc.toString();
			exc.printStackTrace();
		} finally {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_GAMIFICATION_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_GAMIFICATION_ACTIVITY + " " + esLog);
		}
	}

	public VideoGamificationRewardTypeTableDataModelBean getRewardTypeDataModelBean() {
		return rewardTypeDataModelBean;
	}

	public void setRewardTypeDataModelBean(VideoGamificationRewardTypeTableDataModelBean rewardTypeDataModelBean) {
		this.rewardTypeDataModelBean = rewardTypeDataModelBean;
	}

	public RewardTypeEntity getEditRewardTypeEntity() {
		return editRewardTypeEntity;
	}

	public void setEditRewardTypeEntity(RewardTypeEntity editRewardTypeEntity) {
		this.editRewardTypeEntity = editRewardTypeEntity;
	}

	public void processEdit() {
		String esLog = "";
		try {
			esLog += " Editing " + editRewardTypeEntity;
			if (editRewardTypeEntity.getVideoCounterVG() < 0 || editRewardTypeEntity.getInstallCounterVG() < 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Values are invalid!"));
				RequestContext.getCurrentInstance().update("tabView:idVideoGamificationGrowl");
				esLog += "Invalid video counter or install counter value";
				return;
			}

			esLog += " Updating reward type.";
			this.daoRewardType.createOrUpdate(editRewardTypeEntity);

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Values set sucessfully."));
			RequestContext.getCurrentInstance().update("tabView:idVideoGamificationGrowl");
			RequestContext.getCurrentInstance().execute("widgetEditVideoGamification.hide()");
			this.editRewardTypeEntity = new RewardTypeEntity();
			refresh();
			update();
			esLog += " Sucessfully updated reward type.";

		} catch (Exception e) {
			e.printStackTrace();
			esLog += e.toString();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", e.toString()));

			RequestContext.getCurrentInstance().update("tabView:idVideoGamificationGrowl");
			RequestContext.getCurrentInstance().execute("widgetEditVideoGamification.hide()");

		} finally {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_GAMIFICATION_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_GAMIFICATION_ACTIVITY + " " + esLog);
		}
	}

	public void update() {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Data successfully updated"));
		RequestContext.getCurrentInstance().execute("widgetEditVideoGamification.hide()");

	}

	public void refresh() {
		String esLog = "";
		try {
			esLog += "Refreshing reward type list";
			rewardTypeEntityList = daoRewardType.findAll();
			esLog += "Finding all reward types.";
			rewardTypeEntityList = daoRewardType.findAll();
			esLog += "Found total " + rewardTypeEntityList.size() + " elements: " + rewardTypeEntityList + " ... Creating data model bean.";
			rewardTypeDataModelBean = new VideoGamificationRewardTypeTableDataModelBean(rewardTypeEntityList);
			esLog += " Sucessfully created data model bean.";
		} catch (Exception e) {
			e.printStackTrace();
			esLog += e.toString();
		} finally {
			Application.getElasticSearchLogger().indexLog(Application.VIDEO_GAMIFICATION_ACTIVITY, -1, LogStatus.OK,
					Application.VIDEO_GAMIFICATION_ACTIVITY + " " + esLog);
		}

	}

}
