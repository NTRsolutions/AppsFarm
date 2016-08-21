package is.web.beans.referral;

import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.entities.InvitationEntity;

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

@ManagedBean(name = "referralBean")
@SessionScoped
public class ReferralBean {

	@Inject
	private Logger logger;

	@Inject
	private DAOInvitation daoInvitation;
	//private DAOWalletTransaction daoWalletTransaction;

	private LazyDataModel<InvitationEntity> lazyModel;
	private Date startDate = getDefaultStartDate();
	private Date endDate = getDefaultEndDate();

	private final String DEFAULT_FILTER_TYPE = "All";
	private String filterType = DEFAULT_FILTER_TYPE;
	private String filterValue = "";
	
	private String payoutSum = "0";

	private boolean renderId = true;
	private boolean renderCode = true;
	private boolean renderDateOfInvitation = true;
	private boolean renderDateOfRegistration = true;
	private boolean renderEmailInvited = true;
	private boolean renderEmailInviting = true;
	private boolean renderInvitedInternalTransactionId = false;
	private boolean renderInvitedRewardRequestDate = false;
	private boolean renderInvitedRewardRequestStatus = false;
	private boolean renderInvitedRewardRequestStatusMessage = false;
	private boolean renderInvitedRewardResponseStatus = false;
	private boolean renderInvitedRewardResponseStatusMessage = false;
	private boolean renderInvitingInternalTransactionId = false;
	private boolean renderInvitingRewardRequestDate = false;
	private boolean renderInvitingRewardRequestStatus = false;
	private boolean renderInvitingRewardRequestStatusMessage = false;
	private boolean renderInvitingRewardResponseStatus = false;
	private boolean renderInvitingRewardResponseStatusMessage = false;
	private boolean renderIsRealized = true;
	private boolean renderPhoneNumberExtInvited = false;
	private boolean renderPhoneNumberExtInviting = false;
	private boolean renderPhoneNumberInvited = false;
	private boolean renderPhoneNumberInviting = true;
	private boolean renderProcessingStatus = true;
	private boolean renderProcessingStatusMessage = false;
	private boolean renderRewardType = true;
	private boolean renderRewardValueCurrencyCodeInvited = false;
	private boolean renderRewardValueCurrencyCodeInviting = false;
	private boolean renderRewardValueInvited = true;
	private boolean renderRewardValueInviting = true;
	private boolean renderInvitingFBInviteCode = false;
	private boolean renderIsValid = true;
	private boolean renderReferralSource = true;
	

	public ReferralBean() {

	}

	@PostConstruct
	public void init() {
		lazyModel = new LazyDataModel<InvitationEntity>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<InvitationEntity> load(int first, int pageSize, String sortField,
				SortOrder sortOrder, Map<String, String> filters) {

				Timestamp startTime = new Timestamp(startDate.getTime());
				Timestamp endTime = new Timestamp(endDate.getTime());

				if (isCriterionSelected()) {
					filters.put(filterType, filterValue);
				}

				Collection<InvitationEntity> transactions = new ArrayList<InvitationEntity>();

				try {
					int totalCount = daoInvitation.countTotal(startTime, endTime, filters);
					lazyModel.setRowCount(totalCount);

					logger.info("sort field: " + sortField + " filters: " + filters);
					logger.info("lazy loading devices list from between: " + first + " and " + (first + pageSize) + " total devices count: " + totalCount);

					if (sortField == null) {
						sortField = "dateOfInvitation";
						sortOrder = SortOrder.DESCENDING;
					}
					String sortingOrder = "descending";
					if (sortOrder == SortOrder.ASCENDING) {
						sortingOrder = "ascending";
					} else if (sortOrder == SortOrder.DESCENDING) {
						sortingOrder = "descending";
					}
					
					logger.info("searching for all transactions between " + startTime.toString() + " and " + endTime.toString());
					transactions = daoInvitation.findFiltered(first, pageSize, sortField, sortingOrder, filters, startTime, endTime);
				} catch (Exception e) {
					e.printStackTrace();
					logger.severe(e.toString());
				}

				logger.info("lazy loading completed, current results returned: " + transactions.size());

				List<InvitationEntity> result = (List<InvitationEntity>) transactions;
				
				
				return result;
			}
		};
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			RequestContext.getCurrentInstance().update("tabView:idReferralBeanTable");
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

	public LazyDataModel<InvitationEntity> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<InvitationEntity> lazyModel) {
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

	public boolean isRenderCode() {
		return renderCode;
	}

	public void setRenderCode(boolean renderCode) {
		this.renderCode = renderCode;
	}

	public boolean isRenderDateOfInvitation() {
		return renderDateOfInvitation;
	}

	public void setRenderDateOfInvitation(boolean renderDateOfInvitation) {
		this.renderDateOfInvitation = renderDateOfInvitation;
	}

	public boolean isRenderDateOfRegistration() {
		return renderDateOfRegistration;
	}

	public void setRenderDateOfRegistration(boolean renderDateOfRegistration) {
		this.renderDateOfRegistration = renderDateOfRegistration;
	}

	public boolean isRenderEmailInvited() {
		return renderEmailInvited;
	}

	public void setRenderEmailInvited(boolean renderEmailInvited) {
		this.renderEmailInvited = renderEmailInvited;
	}

	public boolean isRenderEmailInviting() {
		return renderEmailInviting;
	}

	public void setRenderEmailInviting(boolean renderEmailInviting) {
		this.renderEmailInviting = renderEmailInviting;
	}

	public boolean isRenderInvitedInternalTransactionId() {
		return renderInvitedInternalTransactionId;
	}

	public void setRenderInvitedInternalTransactionId(boolean renderInvitedInternalTransactionId) {
		this.renderInvitedInternalTransactionId = renderInvitedInternalTransactionId;
	}

	public boolean isRenderInvitedRewardRequestDate() {
		return renderInvitedRewardRequestDate;
	}

	public void setRenderInvitedRewardRequestDate(boolean renderInvitedRewardRequestDate) {
		this.renderInvitedRewardRequestDate = renderInvitedRewardRequestDate;
	}

	public boolean isRenderInvitedRewardRequestStatus() {
		return renderInvitedRewardRequestStatus;
	}

	public void setRenderInvitedRewardRequestStatus(boolean renderInvitedRewardRequestStatus) {
		this.renderInvitedRewardRequestStatus = renderInvitedRewardRequestStatus;
	}

	public boolean isRenderInvitedRewardRequestStatusMessage() {
		return renderInvitedRewardRequestStatusMessage;
	}

	public void setRenderInvitedRewardRequestStatusMessage(boolean renderInvitedRewardRequestStatusMessage) {
		this.renderInvitedRewardRequestStatusMessage = renderInvitedRewardRequestStatusMessage;
	}

	
	public boolean isRenderInvitingInternalTransactionId() {
		return renderInvitingInternalTransactionId;
	}

	public void setRenderInvitingInternalTransactionId(boolean renderInvitingInternalTransactionId) {
		this.renderInvitingInternalTransactionId = renderInvitingInternalTransactionId;
	}

	public boolean isRenderInvitingRewardRequestDate() {
		return renderInvitingRewardRequestDate;
	}

	public void setRenderInvitingRewardRequestDate(boolean renderInvitingRewardRequestDate) {
		this.renderInvitingRewardRequestDate = renderInvitingRewardRequestDate;
	}

	public boolean isRenderInvitingRewardRequestStatus() {
		return renderInvitingRewardRequestStatus;
	}

	public void setRenderInvitingRewardRequestStatus(boolean renderInvitingRewardRequestStatus) {
		this.renderInvitingRewardRequestStatus = renderInvitingRewardRequestStatus;
	}

	public boolean isRenderInvitingRewardRequestStatusMessage() {
		return renderInvitingRewardRequestStatusMessage;
	}

	public void setRenderInvitingRewardRequestStatusMessage(boolean renderInvitingRewardRequestStatusMessage) {
		this.renderInvitingRewardRequestStatusMessage = renderInvitingRewardRequestStatusMessage;
	}

	

	public boolean isRenderInvitedRewardResponseStatus() {
		return renderInvitedRewardResponseStatus;
	}

	public void setRenderInvitedRewardResponseStatus(boolean renderInvitedRewardResponseStatus) {
		this.renderInvitedRewardResponseStatus = renderInvitedRewardResponseStatus;
	}

	public boolean isRenderInvitedRewardResponseStatusMessage() {
		return renderInvitedRewardResponseStatusMessage;
	}

	public void setRenderInvitedRewardResponseStatusMessage(boolean renderInvitedRewardResponseStatusMessage) {
		this.renderInvitedRewardResponseStatusMessage = renderInvitedRewardResponseStatusMessage;
	}

	public boolean isRenderInvitingRewardResponseStatus() {
		return renderInvitingRewardResponseStatus;
	}

	public void setRenderInvitingRewardResponseStatus(boolean renderInvitingRewardResponseStatus) {
		this.renderInvitingRewardResponseStatus = renderInvitingRewardResponseStatus;
	}

	public boolean isRenderInvitingRewardResponseStatusMessage() {
		return renderInvitingRewardResponseStatusMessage;
	}

	public void setRenderInvitingRewardResponseStatusMessage(boolean renderInvitingRewardResponseStatusMessage) {
		this.renderInvitingRewardResponseStatusMessage = renderInvitingRewardResponseStatusMessage;
	}

	public boolean isRenderIsRealized() {
		return renderIsRealized;
	}

	public void setRenderIsRealized(boolean renderIsRealized) {
		this.renderIsRealized = renderIsRealized;
	}

	public boolean isRenderPhoneNumberExtInvited() {
		return renderPhoneNumberExtInvited;
	}

	public void setRenderPhoneNumberExtInvited(boolean renderPhoneNumberExtInvited) {
		this.renderPhoneNumberExtInvited = renderPhoneNumberExtInvited;
	}

	public boolean isRenderPhoneNumberExtInviting() {
		return renderPhoneNumberExtInviting;
	}

	public void setRenderPhoneNumberExtInviting(boolean renderPhoneNumberExtInviting) {
		this.renderPhoneNumberExtInviting = renderPhoneNumberExtInviting;
	}

	public boolean isRenderPhoneNumberInvited() {
		return renderPhoneNumberInvited;
	}

	public void setRenderPhoneNumberInvited(boolean renderPhoneNumberInvited) {
		this.renderPhoneNumberInvited = renderPhoneNumberInvited;
	}

	public boolean isRenderPhoneNumberInviting() {
		return renderPhoneNumberInviting;
	}

	public void setRenderPhoneNumberInviting(boolean renderPhoneNumberInviting) {
		this.renderPhoneNumberInviting = renderPhoneNumberInviting;
	}

	public boolean isRenderProcessingStatus() {
		return renderProcessingStatus;
	}

	public void setRenderProcessingStatus(boolean renderProcessingStatus) {
		this.renderProcessingStatus = renderProcessingStatus;
	}

	public boolean isRenderProcessingStatusMessage() {
		return renderProcessingStatusMessage;
	}

	public void setRenderProcessingStatusMessage(boolean renderProcessingStatusMessage) {
		this.renderProcessingStatusMessage = renderProcessingStatusMessage;
	}

	public boolean isRenderRewardType() {
		return renderRewardType;
	}

	public void setRenderRewardType(boolean renderRewardType) {
		this.renderRewardType = renderRewardType;
	}

	public boolean isRenderRewardValueCurrencyCodeInvited() {
		return renderRewardValueCurrencyCodeInvited;
	}

	public void setRenderRewardValueCurrencyCodeInvited(boolean renderRewardValueCurrencyCodeInvited) {
		this.renderRewardValueCurrencyCodeInvited = renderRewardValueCurrencyCodeInvited;
	}

	public boolean isRenderRewardValueCurrencyCodeInviting() {
		return renderRewardValueCurrencyCodeInviting;
	}

	public void setRenderRewardValueCurrencyCodeInviting(boolean renderRewardValueCurrencyCodeInviting) {
		this.renderRewardValueCurrencyCodeInviting = renderRewardValueCurrencyCodeInviting;
	}

	public boolean isRenderRewardValueInvited() {
		return renderRewardValueInvited;
	}

	public void setRenderRewardValueInvited(boolean renderRewardValueInvited) {
		this.renderRewardValueInvited = renderRewardValueInvited;
	}

	public boolean isRenderRewardValueInviting() {
		return renderRewardValueInviting;
	}

	public void setRenderRewardValueInviting(boolean renderRewardValueInviting) {
		this.renderRewardValueInviting = renderRewardValueInviting;
	}

	public boolean isRenderInvitingFBInviteCode() {
		return renderInvitingFBInviteCode;
	}

	public void setRenderInvitingFBInviteCode(boolean renderInvitingFBInviteCode) {
		this.renderInvitingFBInviteCode = renderInvitingFBInviteCode;
	}

	public boolean isRenderIsValid() {
		return renderIsValid;
	}

	public void setRenderIsValid(boolean renderIsValid) {
		this.renderIsValid = renderIsValid;
	}

	public boolean isRenderReferralSource() {
		return renderReferralSource;
	}

	public void setRenderReferralSource(boolean renderReferralSource) {
		this.renderReferralSource = renderReferralSource;
	}

	
}
