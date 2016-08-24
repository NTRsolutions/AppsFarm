package is.web.beans.events;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import is.ejb.dl.dao.DAORewardTickets;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.RewardTicketEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.web.beans.users.LoginBean;

@ManagedBean(name = "rewardTicketsBean")
@SessionScoped
public class RewardTicketsBean {

	@Inject
	private Logger logger;

	private LoginBean loginBean;

	@Inject
	private DAORewardTickets daoRewardTickets;
	
	@Inject
	private DAORewardType daoRewardType;

	private final String DEFAULT_FILTER_REWARD_NAME = "All";
	private String filterRewardName = DEFAULT_FILTER_REWARD_NAME;
	private List<String> rewardNames = new ArrayList<String>();

	private final String DEFAULT_FILTER_TYPE = "All";
	private String filterType = DEFAULT_FILTER_TYPE;
	private String filterValue = "";
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();

	private boolean renderIdColumn = true;
	private boolean renderUserIdColumn = true;
	private boolean renderUserEmailColumn = true;
	private boolean renderRewardNameColumn = true;
	private boolean renderCreditPointsColumn = true;
	private boolean renderRequestDateColumn = true;
	private boolean renderProcessingDateColumn = true;
	private boolean renderCloseDateColumn = true;
	private boolean renderStatusColumn = true;
	private boolean renderCommentColumn = true;
	private boolean renderTicketOwnerColumn = true;

	private LazyDataModel<RewardTicketEntity> rewardTicketsLazy;

	private double sumCreditPoints = 0;
	private String sumTotalRows = "";

	public RewardTicketsBean() {
	}

	@PostConstruct
	public void init() {
		System.out.println("-- reward ticket bean init");

		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		try {
			loadRewardNames(loginBean.getUser().getRealm().getId());
			
			rewardTicketsLazy = new LazyDataModel<RewardTicketEntity>() {
				private static final long serialVersionUID = 1L;

				@Override
				public List<RewardTicketEntity> load(int first, int pageSize, String sortField, SortOrder sortOrder,
						Map<String, String> filters) {

					Timestamp startTime = new Timestamp(startDate.getTime());
					Timestamp endTime = new Timestamp(endDate.getTime());
					
					if (isRewardTypeSelected()) {
						filters.put("rewardName", filterRewardName);
					}

					if (isCriterionSelected()) {
						filters.put(filterType, filterValue);
					}

					Collection<RewardTicketEntity> tickets = new ArrayList<RewardTicketEntity>();

					try {
						int totalCount = daoRewardTickets.countTotal(startTime, endTime, filters,
								loginBean.getUser().getRealm().getId());
						rewardTicketsLazy.setRowCount(totalCount);
						
						logger.info("sort field: " + sortField + " filters: " + filters);
						logger.info("lazy loading ticket list between: " + first + " and " + (first + pageSize)
								+ " total tickets count: " + totalCount);

						if (sortField == null) {
							sortField = "requestDate";
							sortOrder = SortOrder.DESCENDING;
						}
						String sortingOrder = sortOrder.toString().toLowerCase();

						sumTotalRows = "Identified reward tickets since: " + startDate.toString() + ": " + totalCount;

						logger.info("searching for all events between " + startTime.toString() + " and "
								+ endTime.toString());
						tickets = daoRewardTickets.findFiltered(first, pageSize, sortField, sortingOrder, filters,
								startTime, endTime, loginBean.getUser().getRealm().getId());
						
						sumCreditPoints = sumCreditPoints((List<RewardTicketEntity>) tickets);
						logger.info("sumCreditPoints: " + sumCreditPoints);
					} catch (Exception e) {
						e.printStackTrace();
					}
					logger.info("lazy loading completed, current results returned: " + tickets.size());
					return (List<RewardTicketEntity>) tickets;
				}
			};
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.severe(e1.toString());
		}
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			RequestContext.getCurrentInstance().update("tabView:idRewardTicketsTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
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
	
	private double sumCreditPoints(List<RewardTicketEntity> tickets) {
		double sum = 0;
		for(RewardTicketEntity ticket: tickets) {
			sum += ticket.getCreditPoints();
		}
		return sum;
	}

	private boolean isRewardTypeSelected() {
		if (filterRewardName.equals(DEFAULT_FILTER_REWARD_NAME)) {
			return false;
		} else {
			return true;
		}
	}

	private boolean isCriterionSelected() {
		if (filterType.equals(DEFAULT_FILTER_TYPE)) {
			return false;
		} else {
			return true;
		}
	}
	
	private void loadRewardNames(int realmId) throws Exception {
		rewardNames.clear();
		rewardNames.add(DEFAULT_FILTER_REWARD_NAME);
		List<RewardTypeEntity> rewardTypes = daoRewardType.findAllByRealmId(realmId);
		for (RewardTypeEntity rewardType : rewardTypes) {
			rewardNames.add(rewardType.getName());
		}
	}

	public String getSumTotalRows() {
		return sumTotalRows;
	}

	public void setSumTotalRows(String sumTotalRows) {
		this.sumTotalRows = sumTotalRows;
	}

	public double getSumCreditPoints() {
		return sumCreditPoints;
	}

	public void setSumCreditPoints(double sumCreditPoints) {
		this.sumCreditPoints = sumCreditPoints;
	}

	public LazyDataModel<RewardTicketEntity> getRewardTicketsLazy() {
		return rewardTicketsLazy;
	}

	public void setRewardTicketsLazy(LazyDataModel<RewardTicketEntity> lazyModel) {
		this.rewardTicketsLazy = lazyModel;
	}

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public List<String> getRewardNames() {
		return rewardNames;
	}

	public void setRewardNames(List<String> rewardNames) {
		this.rewardNames = rewardNames;
	}

	public String getFilterRewardName() {
		return filterRewardName;
	}

	public void setFilterRewardName(String filterRewardName) {
		this.filterRewardName = filterRewardName;
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

	public boolean isRenderIdColumn() {
		return renderIdColumn;
	}

	public void setRenderIdColumn(boolean renderIdColumn) {
		this.renderIdColumn = renderIdColumn;
	}

	public boolean isRenderUserIdColumn() {
		return renderUserIdColumn;
	}

	public void setRenderUserIdColumn(boolean renderUserIdColumn) {
		this.renderUserIdColumn = renderUserIdColumn;
	}

	public boolean isRenderUserEmailColumn() {
		return renderUserEmailColumn;
	}

	public void setRenderUserEmailColumn(boolean renderUserEmailColumn) {
		this.renderUserEmailColumn = renderUserEmailColumn;
	}

	public boolean isRenderRewardNameColumn() {
		return renderRewardNameColumn;
	}

	public void setRenderRewardNameColumn(boolean renderRewardNameColumn) {
		this.renderRewardNameColumn = renderRewardNameColumn;
	}

	public boolean isRenderCreditPointsColumn() {
		return renderCreditPointsColumn;
	}

	public void setRenderCreditPointsColumn(boolean renderCreditPointsColumn) {
		this.renderCreditPointsColumn = renderCreditPointsColumn;
	}

	public boolean isRenderRequestDateColumn() {
		return renderRequestDateColumn;
	}

	public void setRenderRequestDateColumn(boolean renderRequestDateColumn) {
		this.renderRequestDateColumn = renderRequestDateColumn;
	}

	public boolean isRenderProcessingDateColumn() {
		return renderProcessingDateColumn;
	}

	public void setRenderProcessingDateColumn(boolean renderProcessingDateColumn) {
		this.renderProcessingDateColumn = renderProcessingDateColumn;
	}

	public boolean isRenderCloseDateColumn() {
		return renderCloseDateColumn;
	}

	public void setRenderCloseDateColumn(boolean renderCloseDateColumn) {
		this.renderCloseDateColumn = renderCloseDateColumn;
	}

	public boolean isRenderStatusColumn() {
		return renderStatusColumn;
	}

	public void setRenderStatusColumn(boolean renderStatusColumn) {
		this.renderStatusColumn = renderStatusColumn;
	}

	public boolean isRenderCommentColumn() {
		return renderCommentColumn;
	}

	public void setRenderCommentColumn(boolean renderCommentColumn) {
		this.renderCommentColumn = renderCommentColumn;
	}

	public boolean isRenderTicketOwnerColumn() {
		return renderTicketOwnerColumn;
	}

	public void setRenderTicketOwnerColumn(boolean renderTicketOwnerColumn) {
		this.renderTicketOwnerColumn = renderTicketOwnerColumn;
	}

}