package is.web.beans.events;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.reporting.LogEntry;
import is.ejb.bl.reporting.ReportingManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOUserEventFailed;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.UserEventFailedEntity;
import is.web.beans.offers.AdProviderDataModelBean;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.SortOrder;

import java.util.Map;

@ManagedBean(name = "failedEventsBrowserBean")
@SessionScoped
public class FailedEventsBrowserBean implements Serializable {

	@Inject
	private Logger logger;

	private LoginBean loginBean;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOUserEventFailed daoUserEventFailed;

	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private RewardManager rewardManager;

	private UserEventFailedEntity logUserEvent = null;
	private List<LogEntry> logs = new ArrayList<LogEntry>();

	private final String DEFAULT_FILTER_REWARD_TYPE_NAME = "All";
	private String filterRewardTypeName = DEFAULT_FILTER_REWARD_TYPE_NAME;
	private List<String> rewardTypeNames = new ArrayList<String>();

	private final String DEFAULT_FILTER_TYPE = "All";
	private String filterType = DEFAULT_FILTER_TYPE;
	private String filterValue = "";
	private boolean isFilterValueRendered = false;
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();

	private LazyDataModel<UserEventFailedEntity> lazyModel;

	private boolean renderEventIdCol = false;
	private boolean renderApprovedCol = true;
	private boolean renderOfferPayoutCol = false;
	private boolean renderOfferPayoutCC = false;

	private boolean renderOfferPayoutInTargetCurrencyCol = true;
	private boolean renderOfferPayoutInTargetCCCol = false;

	private boolean renderRewardCol = true;
	private boolean renderProfitCol = true;
	private boolean renderProfitCCCol = false;
	private boolean renderProfitSplitCol = true;
	private boolean renderPayoutCCCol = false;
	private boolean renderUserIdCol = false;
	private boolean renderPhoneCol = true;
	private boolean renderOfferTitleCol = true;
	private boolean renderOfferProviderCol = true;
	private boolean renderClickDateCol = true;
	private boolean renderInternalTransactionIdCol = true;
	private boolean renderProviderTransactionIdCol = false;
	private boolean renderConversionDateCol = true;
	private boolean renderRewardReqDateCol = false;
	private boolean renderRewardReqStatusCol = false;
	private boolean renderRewardReqStatusMessageCol = false;
	private boolean renderRewardRespDateCol = true;
	private boolean renderRewardRespStatusCol = true;
	private boolean renderRewardRespStatusMessageCol = true;
	private boolean renderMobileAppNotificationDateCol = true;
	private boolean renderMobileAppNotificationStatusCol = false;
	private boolean renderMobileAppNotificationStatusMessageCol = false;
	private boolean renderRewardTypeName = false;
	private boolean renderRewardName = false;
	private boolean renderUserEventCategory = false;
	private boolean renderEmail = false;
	private boolean renderInstant = true;
	private boolean renderApplicationName = false;

	private String sumTotalRows = "23423";
	private String sumOfferPayoutInOriginalCurrency = "23423";
	private String sumOfferPayoutInTargetCurrency = "23423";
	private String sumReward = "23423";
	private String sumProfit = "23423";

	public FailedEventsBrowserBean() {
	}

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		try {
			loadAllRewardTypeNames(loginBean.getUser().getRealm().getId());

			// init lazy model
			lazyModel = new LazyDataModel<UserEventFailedEntity>() {

				private static final long serialVersionUID = 1L;

				@Override
				public List<UserEventFailedEntity> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

					Timestamp startTime = new Timestamp(startDate.getTime());
					Timestamp endTime = new Timestamp(endDate.getTime());

					if (isRewardTypeSelected()) {
						filters.put("rewardTypeName", filterRewardTypeName);
					}

					if (isCriterionSelected()) {
						filters.put(filterType, filterValue);
					}

					Collection<UserEventFailedEntity> listEvents = new ArrayList<UserEventFailedEntity>();

					try {
						int totalCount = daoUserEventFailed.countTotal(startTime, endTime, filters, loginBean.getUser().getRealm().getId());
						lazyModel.setRowCount(totalCount);

						logger.info("sort field: " + sortField + " filters: " + filters);
						logger.info("lazy loading devices list between: " + first + " and " + (first + pageSize) + " total devices count: "
								+ totalCount);

						if (sortField == null) { // by default sort by click
													// date
							sortField = "clickDate";
							sortOrder = SortOrder.DESCENDING;
						}
						String sortingOrder = "descending";
						if (sortOrder == SortOrder.ASCENDING) {
							sortingOrder = "ascending";
						} else if (sortOrder == SortOrder.DESCENDING) {
							sortingOrder = "descending";
						}

						sumTotalRows = "Identified transactions since: " + startDate.toString() + ": " + totalCount;

						logger.info("searching for all events between " + startTime.toString() + " and " + endTime.toString());
						listEvents = daoUserEventFailed.findFiltered(first, pageSize, sortField, sortingOrder, filters, startTime, endTime, loginBean
								.getUser().getRealm().getId());

						double sProfit = daoUserEventFailed.getSumProfit(startTime, endTime, true, loginBean.getUser().getRealm().getId());
						sProfit = round(sProfit, 2);
						sumProfit = sProfit + "";
						double sReward = daoUserEventFailed.getSumReward(startTime, endTime, true, loginBean.getUser().getRealm().getId());
						sReward = round(sReward, 2);
						sumReward = sReward + "";
						double sPayoutTC = daoUserEventFailed.getSumPayout(startTime, endTime, true, loginBean.getUser().getRealm().getId());
						sPayoutTC = round(sPayoutTC, 2);
						sumOfferPayoutInTargetCurrency = sPayoutTC + "";
						double sPayoutOC = daoUserEventFailed.getSumPayoutInOriginalCurrency(startTime, endTime, true, loginBean.getUser().getRealm()
								.getId());
						sPayoutOC = round(sPayoutOC, 2);
						sumOfferPayoutInOriginalCurrency = sPayoutOC + "";

						logger.info(sumProfit + " " + sumOfferPayoutInTargetCurrency + " " + sumOfferPayoutInOriginalCurrency + " " + sumReward);

					} catch (Exception e) {
						e.printStackTrace();
						logger.severe(e.toString());
					}
					logger.info("lazy loading completed, current results returned: " + listEvents.size());

					return (List<UserEventFailedEntity>) listEvents;
				}
			};
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.severe(e1.toString());
		}

	}

	public void refreshList() {
		refresh();
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
		// refresh tab GUI after model update
		// RequestContext.getCurrentInstance().scrollTo("formFoundProducts:idProductsNagivatorBar");
		// RequestContext.getCurrentInstance().update("formFoundProducts:idProductsNagivatorBar");
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			// refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idUserFailedEventsTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public void displayLogs(UserEventFailedEntity userEvent) {
		final String CLUSTER_NAME = "airrewardz";
		String hostName = loginBean.getUser().getRealm().getEsPrimaryStorageIp();

		logUserEvent = userEvent;

		ReportingManager reportingManager = new ReportingManager(hostName, CLUSTER_NAME);
		logs = reportingManager.getLogs(userEvent.getInternalTransactionId());
		reportingManager.closeESClient();

		RequestContext.getCurrentInstance().update("tabView:idFailedDialogLogs");
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

	private boolean isRewardTypeSelected() {
		if (filterRewardTypeName.equals(DEFAULT_FILTER_REWARD_TYPE_NAME)) {
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

	private void loadAllRewardTypeNames(int realmId) throws Exception {
		rewardTypeNames.clear();
		rewardTypeNames.add(DEFAULT_FILTER_REWARD_TYPE_NAME);
		List<RewardTypeEntity> rewardTypes = daoRewardType.findAllByRealmId(realmId);
		for (RewardTypeEntity rewardType : rewardTypes) {
			rewardTypeNames.add(rewardType.getName());
		}
	}

	private Date getDefaultStartDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -3);
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

	private UserEventEntity getUserEventEntityFromUserEventFailed(UserEventFailedEntity failedEvent) {
		UserEventEntity event = new UserEventEntity();
		event.setAdProviderCodeName(failedEvent.getAdProviderCodeName());
		event.setAdvertisingId(failedEvent.getAdvertisingId());
		event.setAfaNetworkName(failedEvent.getAfaNetworkName());
		event.setAndroidDeviceToken(failedEvent.getAndroidDeviceToken());
		event.setApplicationName(failedEvent.getApplicationName());
		event.setApproved(failedEvent.isApproved());
		event.setCarrierName(failedEvent.getCarrierName());
		event.setClickDate(failedEvent.getClickDate());
		event.setConversionDate(failedEvent.getConversionDate());
		event.setCountryCode(failedEvent.getCountryCode());
		event.setDeviceId(failedEvent.getDeviceId());
		event.setDeviceType(failedEvent.getDeviceType());
		event.setEmail(failedEvent.getEmail());
		event.setId(failedEvent.getId());
		event.setIdfa(failedEvent.getIdfa());
		event.setInstant(failedEvent.isInstant());
		event.setInternalTransactionId(failedEvent.getInternalTransactionId());
		event.setIosDeviceToken(failedEvent.getIosDeviceToken());
		event.setIpAddress(failedEvent.getIpAddress());
		event.setLoginName(failedEvent.getLoginName());
		event.setMobileAppNotificationDate(failedEvent.getMobileAppNotificationDate());
		event.setMobileAppNotificationStatus(failedEvent.getMobileAppNotificationStatus());
		event.setMobileAppNotificationStatusMessage(failedEvent.getMobileAppNotificationStatusMessage());
		event.setOfferId(failedEvent.getOfferId());
		event.setOfferPayout(failedEvent.getOfferPayout());
		event.setOfferPayoutInTargetCurrency(failedEvent.getOfferPayoutInTargetCurrency());
		event.setOfferPayoutInTargetCurrencyIsoCurrencyCode(failedEvent.getOfferPayoutInTargetCurrencyIsoCurrencyCode());
		event.setOfferPayoutIsoCurrencyCode(failedEvent.getOfferPayoutIsoCurrencyCode());
		event.setOfferRedirectUrl(failedEvent.getOfferRedirectUrl());
		event.setOfferSourceId(failedEvent.getOfferSourceId());
		event.setOfferTitle(failedEvent.getOfferTitle());
		event.setPhoneNumber(failedEvent.getPhoneNumber());
		event.setPhoneNumberExt(failedEvent.getPhoneNumberExt());
		event.setProfilSplitFraction(failedEvent.getProfilSplitFraction());
		event.setProfitValue(failedEvent.getProfitValue());
		event.setRealmId(failedEvent.getRealmId());
		event.setRevenueValue(failedEvent.getRevenueValue());
		event.setRewardDate(failedEvent.getRewardDate());
		event.setRewardIsoCurrencyCode(failedEvent.getRewardIsoCurrencyCode());
		event.setRewardName(failedEvent.getRewardName());
		event.setRewardRequestDate(failedEvent.getRewardRequestDate());
		event.setRewardRequestStatus(failedEvent.getRewardRequestStatus());
		event.setRewardRequestStatusMessage(failedEvent.getRewardRequestStatusMessage());
		event.setRewardResponseStatus(failedEvent.getRewardResponseStatus());
		event.setRewardResponseStatusMessage(failedEvent.getRewardResponseStatusMessage());
		event.setTestMode(failedEvent.isTestMode());
		event.setTransactionId(failedEvent.getTransactionId());
		event.setUserEventCategory(failedEvent.getUserEventCategory());
		event.setUserId(failedEvent.getUserId());

		return event;
	}

	public void retryEvent(UserEventFailedEntity entity) {
		String msg = "";
		if (entity != null) {

			String type = entity.getUserEventCategory();
			if (type != null && type.equals(UserEventCategory.INSTALL.toString())) {

				UserEventEntity event = getUserEventEntityFromUserEventFailed(entity);
				//rewardManager.retryFailedReward(event);
				msg = "Event successfully retransmissed.";
			} else {
				msg = "Event not supported:" + entity.getUserEventCategory();
			}
		} else {
			msg = "Event is empty";
		}

		showAlert("Information",msg);
	}

	private void showAlert(String title, String message) {
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));

	}

	public LazyDataModel<UserEventFailedEntity> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<UserEventFailedEntity> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public boolean isRenderApprovedCol() {
		return renderApprovedCol;
	}

	public void setRenderApprovedCol(boolean renderApprovedCol) {
		this.renderApprovedCol = renderApprovedCol;
	}

	public boolean isRenderOfferPayoutCol() {
		return renderOfferPayoutCol;
	}

	public void setRenderOfferPayoutCol(boolean renderOfferPayoutCol) {
		this.renderOfferPayoutCol = renderOfferPayoutCol;
	}

	public boolean isRenderOfferPayoutCC() {
		return renderOfferPayoutCC;
	}

	public void setRenderOfferPayoutCC(boolean renderOfferPayoutCC) {
		this.renderOfferPayoutCC = renderOfferPayoutCC;
	}

	public boolean isRenderRewardCol() {
		return renderRewardCol;
	}

	public void setRenderRewardCol(boolean renderRewardCol) {
		this.renderRewardCol = renderRewardCol;
	}

	public boolean isRenderProfitCol() {
		return renderProfitCol;
	}

	public void setRenderProfitCol(boolean renderProfitCol) {
		this.renderProfitCol = renderProfitCol;
	}

	public boolean isRenderProfitCCCol() {
		return renderProfitCCCol;
	}

	public void setRenderProfitCCCol(boolean renderProfitCCCol) {
		this.renderProfitCCCol = renderProfitCCCol;
	}

	public boolean isRenderProfitSplitCol() {
		return renderProfitSplitCol;
	}

	public void setRenderProfitSplitCol(boolean renderProfitSplitCol) {
		this.renderProfitSplitCol = renderProfitSplitCol;
	}

	public boolean isRenderPayoutCCCol() {
		return renderPayoutCCCol;
	}

	public void setRenderPayoutCCCol(boolean renderPayoutCCCol) {
		this.renderPayoutCCCol = renderPayoutCCCol;
	}

	public boolean isRenderUserIdCol() {
		return renderUserIdCol;
	}

	public void setRenderUserIdCol(boolean renderUserIdCol) {
		this.renderUserIdCol = renderUserIdCol;
	}

	public boolean isRenderPhoneCol() {
		return renderPhoneCol;
	}

	public void setRenderPhoneCol(boolean renderPhoneCol) {
		this.renderPhoneCol = renderPhoneCol;
	}

	public boolean isRenderOfferTitleCol() {
		return renderOfferTitleCol;
	}

	public void setRenderOfferTitleCol(boolean renderOfferTitleCol) {
		this.renderOfferTitleCol = renderOfferTitleCol;
	}

	public boolean isRenderOfferProviderCol() {
		return renderOfferProviderCol;
	}

	public void setRenderOfferProviderCol(boolean renderOfferProviderCol) {
		this.renderOfferProviderCol = renderOfferProviderCol;
	}

	public boolean isRenderClickDateCol() {
		return renderClickDateCol;
	}

	public void setRenderClickDateCol(boolean renderClickDateCol) {
		this.renderClickDateCol = renderClickDateCol;
	}

	public boolean isRenderInternalTransactionIdCol() {
		return renderInternalTransactionIdCol;
	}

	public void setRenderInternalTransactionIdCol(boolean renderInternalTransactionIdCol) {
		this.renderInternalTransactionIdCol = renderInternalTransactionIdCol;
	}

	public boolean isRenderProviderTransactionIdCol() {
		return renderProviderTransactionIdCol;
	}

	public void setRenderProviderTransactionIdCol(boolean renderProviderTransactionIdCol) {
		this.renderProviderTransactionIdCol = renderProviderTransactionIdCol;
	}

	public boolean isRenderConversionDateCol() {
		return renderConversionDateCol;
	}

	public void setRenderConversionDateCol(boolean renderConversionDateCol) {
		this.renderConversionDateCol = renderConversionDateCol;
	}

	public boolean isRenderRewardReqDateCol() {
		return renderRewardReqDateCol;
	}

	public void setRenderRewardReqDateCol(boolean renderRewardReqDateCol) {
		this.renderRewardReqDateCol = renderRewardReqDateCol;
	}

	public boolean isRenderRewardReqStatusCol() {
		return renderRewardReqStatusCol;
	}

	public void setRenderRewardReqStatusCol(boolean renderRewardReqStatusCol) {
		this.renderRewardReqStatusCol = renderRewardReqStatusCol;
	}

	public boolean isRenderRewardReqStatusMessageCol() {
		return renderRewardReqStatusMessageCol;
	}

	public void setRenderRewardReqStatusMessageCol(boolean renderRewardReqStatusMessageCol) {
		this.renderRewardReqStatusMessageCol = renderRewardReqStatusMessageCol;
	}

	public boolean isRenderRewardRespDateCol() {
		return renderRewardRespDateCol;
	}

	public void setRenderRewardRespDateCol(boolean renderRewardRespDateCol) {
		this.renderRewardRespDateCol = renderRewardRespDateCol;
	}

	public boolean isRenderRewardRespStatusCol() {
		return renderRewardRespStatusCol;
	}

	public void setRenderRewardRespStatusCol(boolean renderRewardRespStatusCol) {
		this.renderRewardRespStatusCol = renderRewardRespStatusCol;
	}

	public boolean isRenderRewardRespStatusMessageCol() {
		return renderRewardRespStatusMessageCol;
	}

	public void setRenderRewardRespStatusMessageCol(boolean renderRewardRespStatusMessageCol) {
		this.renderRewardRespStatusMessageCol = renderRewardRespStatusMessageCol;
	}

	public boolean isRenderMobileAppNotificationDateCol() {
		return renderMobileAppNotificationDateCol;
	}

	public void setRenderMobileAppNotificationDateCol(boolean renderMobileAppNotificationDateCol) {
		this.renderMobileAppNotificationDateCol = renderMobileAppNotificationDateCol;
	}

	public boolean isRenderMobileAppNotificationStatusCol() {
		return renderMobileAppNotificationStatusCol;
	}

	public void setRenderMobileAppNotificationStatusCol(boolean renderMobileAppNotificationStatusCol) {
		this.renderMobileAppNotificationStatusCol = renderMobileAppNotificationStatusCol;
	}

	public boolean isRenderMobileAppNotificationStatusMessageCol() {
		return renderMobileAppNotificationStatusMessageCol;
	}

	public void setRenderMobileAppNotificationStatusMessageCol(boolean renderMobileAppNotificationStatusMessageCol) {
		this.renderMobileAppNotificationStatusMessageCol = renderMobileAppNotificationStatusMessageCol;
	}

	public boolean isRenderEventIdCol() {
		return renderEventIdCol;
	}

	public void setRenderEventIdCol(boolean renderEventIdCol) {
		this.renderEventIdCol = renderEventIdCol;
	}

	public boolean isRenderRewardTypeName() {
		return renderRewardTypeName;
	}

	public void setRenderRewardTypeName(boolean renderRewardTypeName) {
		this.renderRewardTypeName = renderRewardTypeName;
	}

	public boolean isRenderOfferPayoutInTargetCurrencyCol() {
		return renderOfferPayoutInTargetCurrencyCol;
	}

	public void setRenderOfferPayoutInTargetCurrencyCol(boolean renderOfferPayoutInTargetCurrencyCol) {
		this.renderOfferPayoutInTargetCurrencyCol = renderOfferPayoutInTargetCurrencyCol;
	}

	public boolean isRenderOfferPayoutInTargetCCCol() {
		return renderOfferPayoutInTargetCCCol;
	}

	public void setRenderOfferPayoutInTargetCCCol(boolean renderOfferPayoutInTargetCCCol) {
		this.renderOfferPayoutInTargetCCCol = renderOfferPayoutInTargetCCCol;
	}

	public boolean isRenderUserEventCategory() {
		return renderUserEventCategory;
	}

	public void setRenderUserEventCategory(boolean renderUserEventCategory) {
		this.renderUserEventCategory = renderUserEventCategory;
	}

	public boolean isRenderRewardName() {
		return renderRewardName;
	}

	public void setRenderRewardName(boolean renderRewardName) {
		this.renderRewardName = renderRewardName;
	}

	public boolean isRenderEmail() {
		return renderEmail;
	}

	public void setRenderEmail(boolean renderEmail) {
		this.renderEmail = renderEmail;
	}

	public boolean isRenderInstant() {
		return renderInstant;
	}

	public void setRenderInstant(boolean renderInstant) {
		this.renderInstant = renderInstant;
	}

	public boolean isRenderApplicationName() {
		return renderApplicationName;
	}

	public void setRenderApplicationName(boolean renderApplicationName) {
		this.renderApplicationName = renderApplicationName;
	}

	// -------

	public String getSumTotalRows() {
		return sumTotalRows;
	}

	public void setSumTotalRows(String sumTotalRows) {
		this.sumTotalRows = sumTotalRows;
	}

	public String getSumOfferPayoutInOriginalCurrency() {
		return sumOfferPayoutInOriginalCurrency;
	}

	public void setSumOfferPayoutInOriginalCurrency(String sumOfferPayoutInOriginalCurrency) {
		this.sumOfferPayoutInOriginalCurrency = sumOfferPayoutInOriginalCurrency;
	}

	public String getSumOfferPayoutInTargetCurrency() {
		return sumOfferPayoutInTargetCurrency;
	}

	public void setSumOfferPayoutInTargetCurrency(String sumOfferPayoutInTargetCurrency) {
		this.sumOfferPayoutInTargetCurrency = sumOfferPayoutInTargetCurrency;
	}

	public String getSumReward() {
		return sumReward;
	}

	public void setSumReward(String sumReward) {
		this.sumReward = sumReward;
	}

	public String getSumProfit() {
		return sumProfit;
	}

	public void setSumProfit(String sumProfit) {
		this.sumProfit = sumProfit;
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

	public boolean isFilterValueRendered() {
		return isFilterValueRendered;
	}

	public void setFilterValueRendered(boolean isFilterValueRendered) {
		this.isFilterValueRendered = isFilterValueRendered;
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

	public UserEventFailedEntity getLogUserEvent() {
		return logUserEvent;
	}

	public void setLogUserEvent(UserEventFailedEntity logUserEvent) {
		this.logUserEvent = logUserEvent;
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public void setLogs(List<LogEntry> logs) {
		this.logs = logs;
	}

	public String getFilterRewardTypeName() {
		return filterRewardTypeName;
	}

	public void setFilterRewardTypeName(String filterRewardTypeName) {
		this.filterRewardTypeName = filterRewardTypeName;
	}

	public List<String> getRewardTypeNames() {
		return rewardTypeNames;
	}

	public void setRewardTypeNames(List<String> rewardTypeNames) {
		this.rewardTypeNames = rewardTypeNames;
	}

}
