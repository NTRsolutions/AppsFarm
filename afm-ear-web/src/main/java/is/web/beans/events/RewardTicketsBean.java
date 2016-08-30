package is.web.beans.events;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.bl.email.EmailHolder;
import is.ejb.bl.email.EmailManager;
import is.ejb.bl.reporting.LogEntry;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.reward.RewardTicketManager;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAORewardTickets;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.EmailTemplateEntity;
import is.ejb.dl.entities.RewardTicketEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
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
	@Inject
	private RewardTicketManager rewardTicketManager;
	@Inject
	private DAOUser daoUser;
	@Inject
	private EmailManager emailManager;

	private final String DEFAULT_FILTER_REWARD_NAME = "All";
	private String filterRewardName = DEFAULT_FILTER_REWARD_NAME;
	private List<String> rewardNames = new ArrayList<String>();

	private final String DEFAULT_FILTER_TYPE = "All";
	private String filterType = DEFAULT_FILTER_TYPE;
	private String filterValue = "";
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();

	private boolean renderIdColumn = true;
	private boolean renderUserIdColumn = false;
	private boolean renderUserEmailColumn = true;
	private boolean renderRewardNameColumn = true;
	private boolean renderCreditPointsColumn = true;
	private boolean renderRequestDateColumn = true;
	private boolean renderProcessingDateColumn = false;
	private boolean renderCloseDateColumn = false;
	private boolean renderStatusColumn = true;
	private boolean renderCommentColumn = false;
	private boolean renderTicketOwnerColumn = true;
	private boolean renderHashColumn = false;
	private boolean renderRewardIdColumn = false;
	private boolean renderRewardTypeColumn = true;
	private boolean renderRewardCategoryColumn = true;
	private LazyDataModel<RewardTicketEntity> rewardTicketsLazy;
	private RewardTicketEntity selectedTicket = new RewardTicketEntity();
	private List<LogEntry> logs = new ArrayList<>();

	private double sumCreditPoints = 0;
	private String sumTotalRows = "";

	private int selectedEmailTemplate = 0;
	private String emailRewardResult = "";
	private String emailPreview;

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
			RequestContext.getCurrentInstance().update("tabView:idRewardTicketsGrowl");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
	}

	public void loadLogs(RewardTicketEntity ticket) {
		logger.info("load logs for: " + ticket.toString());
		String hostName = loginBean.getUser().getRealm().getEsPrimaryStorageIp();
		selectedTicket = ticket;

		ReportingManager reportingManager = new ReportingManager(hostName, ReportingManager.DEFAULT_CLUSTER_NAME);
		logs = reportingManager.getRewardTicketsLogs(ticket.getHash());
		reportingManager.closeESClient();
		logger.info("logs count: " + logs.size());

		RequestContext.getCurrentInstance().update("tabView:idDialogRewardTicketLogs");
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
		for (RewardTicketEntity ticket : tickets) {
			sum += ticket.getCreditPoints();
		}
		return round(sum, 2);
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

	private double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public List<SelectItem> getTicketStatuses() {
		List<SelectItem> statusList = new ArrayList<SelectItem>();
		statusList.add(new SelectItem(RewardTicketStatus.AWAITING_PROCESSING.toString(),
				RewardTicketStatus.AWAITING_PROCESSING.toString()));
		statusList.add(new SelectItem(RewardTicketStatus.CURRENTLY_PROCESSED.toString(),
				RewardTicketStatus.CURRENTLY_PROCESSED.toString()));
		statusList.add(new SelectItem(RewardTicketStatus.PROCESSED_SUCCESS.toString(),
				RewardTicketStatus.PROCESSED_SUCCESS.toString()));
		statusList.add(new SelectItem(RewardTicketStatus.PROCESSED_FAILED.toString(),
				RewardTicketStatus.PROCESSED_FAILED.toString()));

		return statusList;
	}

	public void saveSelectedTicket() {
		if (selectedTicket.getStatus() == RewardTicketStatus.CURRENTLY_PROCESSED
				&& selectedTicket.getProcessingDate() == null) {
			selectedTicket.setProcessingDate(new Timestamp(new Date().getTime()));
		}

		if (selectedTicket.getStatus() == RewardTicketStatus.PROCESSED_FAILED
				|| selectedTicket.getStatus() == RewardTicketStatus.PROCESSED_SUCCESS
						&& selectedTicket.getCloseDate() == null) {
			selectedTicket.setCloseDate(new Timestamp(new Date().getTime()));
			if (selectedTicket.getProcessingDate() == null) {
				selectedTicket.setProcessingDate(new Timestamp(new Date().getTime()));
			}
		}
		Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
				"User: " + loginBean.getUser().getName() + " (" + loginBean.getUser().getEmail()
						+ ") Updated  in ticket with id: " + selectedTicket.getId() + " hash: "
						+ selectedTicket.getHash(),
				selectedTicket);
		rewardTicketManager.updateTicket(selectedTicket);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Ticket updated"));
		refresh();
	}

	public List<SelectItem> getAdministrators() {
		List<SelectItem> adminList = new ArrayList<SelectItem>();
		try {
			List<UserEntity> userList = daoUser.findAll();
			adminList.add(new SelectItem("", ""));
			for (UserEntity user : userList) {
				adminList.add(new SelectItem(user.getName(), user.getName()));
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return adminList;
	}

	public void sendCreditsBackToWallet(RewardTicketEntity rewardTicket) {

		Application.getElasticSearchLogger().indexRewardTicket(LogStatus.OK,
				"User: " + loginBean.getUser().getName() + " (" + loginBean.getUser().getEmail()
						+ ") Send credit back to wallet from ticket with id: " + rewardTicket.getId() + " hash: "
						+ rewardTicket.getHash(),
				rewardTicket);
		boolean result = rewardTicketManager.addRewardAmountBackToWallet(rewardTicket);
		if (result) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Success", "Credits transfered successfully"));
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Credits transfered failed"));
		}
		refresh();
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public void setLogs(List<LogEntry> logs) {
		this.logs = logs;
	}

	public RewardTicketEntity getSelectedTicket() {
		return selectedTicket;
	}

	public void setSelectedTicket(RewardTicketEntity selectedTicket) {
		this.selectedTicket = selectedTicket;
		RequestContext.getCurrentInstance().update("tabView:idWidgetEditTicketComment");
		RequestContext.getCurrentInstance().update("tabView:idWidgetEditTicketStatus");
		RequestContext.getCurrentInstance().update("tabView:idWidgetEditTicketOwner");
	}

	public List<SelectItem> getEmailTemplates() {
		List<EmailTemplateEntity> emails = emailManager.getAllTemplates();
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		for (EmailTemplateEntity emailTemplateEntity : emails) {
			selectItems.add(new SelectItem(emailTemplateEntity.getId(), emailTemplateEntity.getName()));
		}
		return selectItems;
	}

	public void setupEmailPreview(){
		EmailHolder holder = emailManager.setupEmailTemplate(selectedEmailTemplate, selectedTicket, emailRewardResult);
		logger.info("Email preview: " + emailPreview);
		emailPreview = "";
		emailPreview += "<br/>Title: " + holder.getTitle() +"<br/>";
		emailPreview += "Recipent: <i> "+holder.getRecipent()+"</i><br/>";
		emailPreview += "=====================================================================<br/><br/>";
		emailPreview += holder.getContent();
		
		RequestContext.getCurrentInstance().update("tabView:idWidgetEmailPreviewGrid");
		
	}
	
	public void sendEmail(){
		rewardTicketManager.sendEmail(selectedTicket, selectedEmailTemplate, emailRewardResult);
		RequestContext.getCurrentInstance().execute("widgetSendEmail.hide()");
		RequestContext.getCurrentInstance().execute("widgetEmailPreview.hide()");
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Email has been sent"));
		refresh();
		
	}
	

	public int getSelectedEmailTemplate() {
		return selectedEmailTemplate;
	}

	public void setSelectedEmailTemplate(int selectedEmailTemplate) {
		this.selectedEmailTemplate = selectedEmailTemplate;
	}

	public String getEmailRewardResult() {
		return emailRewardResult;
	}

	public void setEmailRewardResult(String emailRewardResult) {
		this.emailRewardResult = emailRewardResult;
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

	public boolean isRenderHashColumn() {
		return renderHashColumn;
	}

	public void setRenderHashColumn(boolean renderHashColumn) {
		this.renderHashColumn = renderHashColumn;
	}

	public boolean isRenderRewardIdColumn() {
		return renderRewardIdColumn;
	}

	public void setRenderRewardIdColumn(boolean renderRewardIdColumn) {
		this.renderRewardIdColumn = renderRewardIdColumn;
	}

	public boolean isRenderRewardTypeColumn() {
		return renderRewardTypeColumn;
	}

	public void setRenderRewardTypeColumn(boolean renderRewardTypeColumn) {
		this.renderRewardTypeColumn = renderRewardTypeColumn;
	}

	public boolean isRenderRewardCategoryColumn() {
		return renderRewardCategoryColumn;
	}

	public void setRenderRewardCategoryColumn(boolean renderRewardCategoryColumn) {
		this.renderRewardCategoryColumn = renderRewardCategoryColumn;
	}

	public String getEmailPreview() {
		return emailPreview;
	}

	public void setEmailPreview(String emailPreview) {
		this.emailPreview = emailPreview;
	}

}
