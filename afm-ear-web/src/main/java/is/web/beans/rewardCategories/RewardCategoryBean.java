package is.web.beans.rewardCategories;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import is.ejb.dl.dao.DAORewardCategory;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardCategoryEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.users.LoginBean;

@ManagedBean(name = "rewardCategoryBean")
@SessionScoped
public class RewardCategoryBean {

	@Inject
	private DAORewardCategory daoRewardCategory;
	@Inject
	private DAORewardType daoRewardType;
	@Inject
	private Logger logger;

	private RewardCategoryEntity createModel = new RewardCategoryEntity();
	private List<RewardTypeEntity> rewardTypeList = new ArrayList<RewardTypeEntity>();
	private List<RewardCategoryEntity> allCategories = new ArrayList<RewardCategoryEntity>();
	private RewardCategoryTableDataModelBean modelBean;

	@PostConstruct
	public void init() {
		loadAllCategories();
		loadRewardTypes();

	}

	private void loadRewardTypes() {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			LoginBean loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}",
					LoginBean.class);
			RealmEntity realm = loginBean.getUser().getRealm();
			rewardTypeList = daoRewardType.findAllByRealmId(realm.getId());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void loadAllCategories() {
		allCategories = daoRewardCategory.getAll();
		modelBean = new RewardCategoryTableDataModelBean(allCategories);
	}

	public void refresh() {
		RequestContext.getCurrentInstance().update("tabView:idApplicationRewardTable");
	}

	public void showError(String message) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", message));
		refresh();

	}

	public void insertCategory() {
		try {
			if (createModel.getName() == null || createModel.getName().length() == 0) {
				showError("Please provide category name.");
				return;
			}

			RequestContext.getCurrentInstance().execute("widgetCreateRewardCategory.hide()");
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Reward category created"));

			daoRewardCategory.createOrUpdate(createModel);
			loadAllCategories();
			refresh();
			createModel = new RewardCategoryEntity();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void remove(RewardCategoryEntity categoryEntity) {
		try {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Successfully deleted category."));
			daoRewardCategory.delete(categoryEntity);
			loadAllCategories();
			refresh();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public RewardCategoryEntity getCreateModel() {
		return createModel;
	}

	public void setCreateModel(RewardCategoryEntity createModel) {
		this.createModel = createModel;
	}

	public RewardCategoryTableDataModelBean getModelBean() {
		return modelBean;
	}

	public void setModelBean(RewardCategoryTableDataModelBean modelBean) {
		this.modelBean = modelBean;
	}

	public List<RewardTypeEntity> getRewardTypeList() {
		return rewardTypeList;
	}

	public void setRewardTypeList(List<RewardTypeEntity> rewardTypeList) {
		this.rewardTypeList = rewardTypeList;
	}

}
