package is.web.beans.rewardCategories;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import is.ejb.dl.dao.DAORewardCategory;
import is.ejb.dl.entities.RewardCategoryEntity;

@ManagedBean(name = "rewardCategoryBean")
@SessionScoped
public class RewardCategoryBean {

	@Inject
	private DAORewardCategory daoRewardCategory;
	private RewardCategoryEntity createModel = new RewardCategoryEntity();

	private List<RewardCategoryEntity> allCategories = new ArrayList<RewardCategoryEntity>();
	private RewardCategoryTableDataModelBean modelBean;

	@PostConstruct
	public void init() {
		loadAllCategories();

	}

	public void loadAllCategories() {
		allCategories = daoRewardCategory.getAll();
		modelBean = new RewardCategoryTableDataModelBean(allCategories);
	}

	public void refresh() {
		RequestContext.getCurrentInstance().update("tabView:idApplicationRewardTable");
	}

	public void insertCategory() {
		try {
			daoRewardCategory.createOrUpdate(createModel);
			loadAllCategories();
			refresh();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public void remove(RewardCategoryEntity categoryEntity){
		try{
			daoRewardCategory.delete(categoryEntity);
			loadAllCategories();
			refresh();
		}
		catch (Exception exc){
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

}
