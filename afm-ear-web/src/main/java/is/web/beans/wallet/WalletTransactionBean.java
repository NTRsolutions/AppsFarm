package is.web.beans.wallet;

import is.ejb.dl.dao.DAOWalletTransaction;
import is.ejb.dl.entities.WalletTransactionEntity;

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
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@ManagedBean(name = "walletTransactionsBean")
@SessionScoped
public class WalletTransactionBean {

	@Inject
	private Logger logger;

	@Inject
	private DAOWalletTransaction daoWalletTransaction;

	private LazyDataModel<WalletTransactionEntity> lazyModel;
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();

	private final String DEFAULT_FILTER_TYPE = "All";
	private String filterType = DEFAULT_FILTER_TYPE;
	private String filterValue = "";
	
	private String payoutSum = "0";

	private boolean renderId = true;
	private boolean renderPayoutDescription = true;
	private boolean renderPayoutValue = true;
	private boolean renderStatus = true;
	private boolean renderUserId = true;
	private boolean renderInternalTransactionId = true;
	private boolean renderPayoutCurrencyCode = true;
	private boolean renderTimestamp = true;

	public WalletTransactionBean() {

	}

	@PostConstruct
	public void init() {
		lazyModel = new LazyDataModel<WalletTransactionEntity>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<WalletTransactionEntity> load(int first, int pageSize, String sortField,
				SortOrder sortOrder, Map<String, String> filters) {

				Timestamp startTime = new Timestamp(startDate.getTime());
				Timestamp endTime = new Timestamp(endDate.getTime());

				if (isCriterionSelected()) {
					filters.put(filterType, filterValue);
				}

				Collection<WalletTransactionEntity> transactions = new ArrayList<WalletTransactionEntity>();

				try {
					int totalCount = daoWalletTransaction.countTotal(startTime, endTime, filters);
					lazyModel.setRowCount(totalCount);

					logger.info("sort field: " + sortField + " filters: " + filters);
					logger.info("lazy loading devices list from between: " + first + " and " + (first + pageSize) + " total devices count: " + totalCount);

					if (sortField == null) {
						sortField = "timestamp";
						sortOrder = SortOrder.DESCENDING;
					}
					String sortingOrder = "descending";
					if (sortOrder == SortOrder.ASCENDING) {
						sortingOrder = "ascending";
					} else if (sortOrder == SortOrder.DESCENDING) {
						sortingOrder = "descending";
					}
					
					logger.info("searching for all transactions between " + startTime.toString() + " and " + endTime.toString());
					transactions = daoWalletTransaction.findFiltered(first, pageSize, sortField, sortingOrder, filters, startTime, endTime);
				} catch (Exception e) {
					e.printStackTrace();
					logger.severe(e.toString());
				}

				logger.info("lazy loading completed, current results returned: " + transactions.size());

				List<WalletTransactionEntity> result = (List<WalletTransactionEntity>) transactions;
				try {
					double payoutSumD = daoWalletTransaction.getSumPayout(startTime, endTime, filters);
					payoutSum = String.valueOf(payoutSumD);
				} catch (Exception e) {
					e.printStackTrace();
					logger.severe(e.toString());
				}
				
				return result;
			}
		};
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			RequestContext.getCurrentInstance().update("tabView:idWalletTransactionTable");
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
	
	private boolean isCriterionSelected(){
		if(filterType.equals(DEFAULT_FILTER_TYPE)){
			return false;
		} else {
			return true;
		}
	}

	public LazyDataModel<WalletTransactionEntity> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<WalletTransactionEntity> lazyModel) {
		this.lazyModel = lazyModel;
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

	public String getPayoutSum() {
		return payoutSum;
	}

	public void setPayoutSum(String payoutSum) {
		this.payoutSum = payoutSum;
	}

//--render methods

	public boolean isRenderId() {
		return renderId;
	}

	public void setRenderId(boolean renderId) {
		this.renderId = renderId;
	}

	public boolean isRenderPayoutDescription() {
		return renderPayoutDescription;
	}

	public void setRenderPayoutDescription(boolean renderPayoutDescription) {
		this.renderPayoutDescription = renderPayoutDescription;
	}

	public boolean isRenderPayoutValue() {
		return renderPayoutValue;
	}

	public void setRenderPayoutValue(boolean renderPayoutValue) {
		this.renderPayoutValue = renderPayoutValue;
	}

	public boolean isRenderStatus() {
		return renderStatus;
	}

	public void setRenderStatus(boolean renderStatus) {
		this.renderStatus = renderStatus;
	}

	public boolean isRenderUserId() {
		return renderUserId;
	}

	public void setRenderUserId(boolean renderUserId) {
		this.renderUserId = renderUserId;
	}

	public boolean isRenderInternalTransactionId() {
		return renderInternalTransactionId;
	}

	public void setRenderInternalTransactionId(boolean renderInternalTransactionId) {
		this.renderInternalTransactionId = renderInternalTransactionId;
	}

	public boolean isRenderPayoutCurrencyCode() {
		return renderPayoutCurrencyCode;
	}

	public void setRenderPayoutCurrencyCode(boolean renderPayoutCurrencyCode) {
		this.renderPayoutCurrencyCode = renderPayoutCurrencyCode;
	}

	public boolean isRenderTimestamp() {
		return renderTimestamp;
	}

	public void setRenderTimestamp(boolean renderTimestamp) {
		this.renderTimestamp = renderTimestamp;
	}

}
