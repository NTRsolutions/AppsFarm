package is.ejb.dl.dao;

import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.offerProviders.snapdeal.SnapdealReportType;
import is.ejb.dl.entities.UserEventEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

@Stateless
public class DAOUserEvent {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(UserEventEntity entity) {
		em.persist(entity);
	}

	public UserEventEntity createOrUpdate(UserEventEntity entity, int step) {
		// System.out.println("UPDATING !!!!!!!!!!!!!!!!!!!! "+step+"
		// "+entity.getRewardResponseStatusMessage()+"
		// "+entity.getRewardDate());
		return em.merge(entity);
	}

	public void delete(UserEventEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public UserEventEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE o.id = ?1",
					UserEventEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public UserEventEntity findByUserId(String userId) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE o.userId = ?1",
					UserEventEntity.class);

			query.setParameter(1, userId);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public UserEventEntity findByOfferId(String offerId) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE o.offerId = ?1",
					UserEventEntity.class);

			query.setParameter(1, offerId);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public UserEventEntity findByUserIdAndOfferId(String userId, String offerId) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery(
					"SELECT o FROM UserEventEntity o WHERE o.userId = ?1 AND o.offerId=?2", UserEventEntity.class);

			query.setParameter(1, userId);
			query.setParameter(2, offerId);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public UserEventEntity findByUserIdAndPhoneNumberAndOfferIdAndAdProviderCodeName(String userId, String phoneNumber,
			String offerId, String adProviderCodeName) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE "
					+ "o.userId = ?1 AND " + "o.phoneNumber=?2 AND " + "o.offerId=?3 AND " + "o.adProviderCodeName=?4",
					UserEventEntity.class);

			query.setParameter(1, userId);
			query.setParameter(2, phoneNumber);
			query.setParameter(3, offerId);
			query.setParameter(4, adProviderCodeName);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEventEntity findByInternalTransactionId(String internalTransactionId) throws Exception {

		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.internalTransactionId = ?1", UserEventEntity.class);

		query.setParameter(1, internalTransactionId);
		List<UserEventEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}
	
	public UserEventEntity findByInternalTransactionIdSafe(String internalTransactionId)  {
		try{
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.internalTransactionId = ?1", UserEventEntity.class);

		query.setParameter(1, internalTransactionId);
		List<UserEventEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
		}
		catch (Exception exc){
			exc.printStackTrace();
			return null;
		}
	}


	public UserEventEntity findByPhone(String phoneNumber) throws Exception {
		TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE o.phoneNumber = ?1",
				UserEventEntity.class);

		query.setParameter(1, phoneNumber);

		List<UserEventEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public UserEventEntity findByProviderCodeName(String providerCodeName) throws Exception {
		TypedQuery<UserEventEntity> query = em
				.createQuery("SELECT o FROM UserEventEntity o WHERE o.adProviderCodeName = ?1", UserEventEntity.class);

		query.setParameter(1, providerCodeName);

		List<UserEventEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public List<UserEventEntity> findAll() throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o",
					UserEventEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public List<UserEventEntity> findAllByRealmId(int realmId) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery("SELECT o FROM UserEventEntity o WHERE o.realmId = ?1",
					UserEventEntity.class);

			query.setParameter(1, realmId);
			return query.getResultList();
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public List<UserEventEntity> findAllByUserIdAndProviderOfferId(int userId, String providerOfferId)
			throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery(
					"SELECT o FROM UserEventEntity o WHERE o.userId = ?1 AND o.offerSourceId=?2",
					UserEventEntity.class);

			query.setParameter(1, userId);
			query.setParameter(2, providerOfferId);
			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<UserEventEntity> findAllByRealmIdAndCodeName(int realmId, String providerCodeName) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery(
					"SELECT o FROM UserEventEntity o WHERE o.realmId = ?1 AND o.providerCodeName=?2",
					UserEventEntity.class);

			query.setParameter(1, realmId);
			query.setParameter(2, providerCodeName);
			return query.getResultList();
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	// check if there is a request that was send in the last 72 minutes and has
	// not gotten response back for the same user
	public int countStillProcessingEventsOnMode(int userId, Timestamp requestDateToMode, String requestResponseStatus) {
		Query query = em.createQuery(
				"select COUNT(p) from UserEventEntity p WHERE p.userId = ?1 AND p.rewardRequestDate >= ?2 AND p.rewardDate IS NULL AND p.rewardRequestStatus = ?3");

		query.setParameter(1, userId);
		query.setParameter(2, requestDateToMode);
		query.setParameter(3, requestResponseStatus);

		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	public int countTotal(int realmId) {
		Query query = em.createQuery("select COUNT(p) from UserEventEntity p WHERE p.realmId = ?1");
		query.setParameter(1, realmId);
		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	public int countTotal(Timestamp startDate, Timestamp endDate, Map<String, String> filters, int realmId)
			throws Exception {
		int count = 0;

		final int FIRST = 0;
		final int PAGE_SIZE = Integer.MAX_VALUE;
		final String SORT_FIELD = null;
		final String SORT_ORDER = null;

		List<UserEventEntity> list = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters, startDate, endDate,
				realmId);
		count = list.size();

		return count;
	}

	public List<UserEventEntity> findSpinnerEventsInRange(Timestamp startDate, Timestamp endDate) {

		Query query = em.createQuery(
				"select COUNT(p) from UserEventEntity p WHERE p.userEventCategory = ?1 AND p.clickDate >= ?2 AND p.clickDate <= ?3");

		query.setParameter(1, "SPINNER");
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);

		return query.getResultList();

	}

	public List<UserEventEntity> findSpinnerEventsInRangeAndForRewardType(Date startDate, Date endDate,
			String rewardType) {

		Query query = em.createQuery(
				"select p from UserEventEntity p WHERE p.userEventCategory = ?1 AND p.clickDate >= ?2 AND p.clickDate <= ?3 AND rewardTypeName= ?4");

		query.setParameter(1, "SPINNER");
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		query.setParameter(4, rewardType);

		return query.getResultList();

	}

	// http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-lazyloading-using-hibernate-criteria-api/
	public List<UserEventEntity> findFiltered(int first, int pageSize, String sortField, String sortOrder,
			Map<String, String> filters, Timestamp startDate, Timestamp endDate, int realmId) throws Exception {
		try {
			List<UserEventEntity> data = new ArrayList<UserEventEntity>();

			// Criteria
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<UserEventEntity> accountQuery = criteriaBuilder.createQuery(UserEventEntity.class);
			// From
			Root<UserEventEntity> from = accountQuery.from(UserEventEntity.class);

			// sort
			if (sortField != null) {
				if (sortOrder.equals("ascending")) {
					accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
				} else {
					accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
				}
			}

			// filters
			List<Predicate> predicates = new ArrayList<Predicate>();
			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = it.next(); // table column name
													// = field name
				String filterValue = filters.get(filterProperty);

				if (isFilterPropertyInteger(filterProperty)) {
					Expression<Integer> literal = criteriaBuilder.literal(new Integer(filterValue));
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: "
							+ literal);
					predicates.add(criteriaBuilder.equal(from.<Integer> get(filterProperty), literal));
				} else {
					// Expression literal =
					// criteriaBuilder.literal((String)filterValue);
					Expression<String> literal = criteriaBuilder.literal((String) (filterValue + "%"));
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: "
							+ literal);
					predicates.add(criteriaBuilder.like(from.<String> get(filterProperty), literal));
				}
			}
			// only for given realm
			predicates.add(criteriaBuilder.equal(from.get("realmId"), realmId));

			// only from selected time range
			predicates.add(criteriaBuilder.between(from.<Date> get("clickDate"), startDate, endDate));

			accountQuery.where(predicates.toArray(new Predicate[predicates.size()]));

			// paginate
			data = em.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize).getResultList();

			return data;

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public UserEventEntity findMostRecentOfferClickEvent(int userId, String userEventCategory) throws Exception {
		try {
			TypedQuery<UserEventEntity> query = em.createQuery(
					"SELECT o FROM UserEventEntity o WHERE o.userId = ?1 AND o.userEventCategory = ?2 ORDER BY o.clickDate desc",
					UserEventEntity.class);

			query.setMaxResults(1);
			query.setParameter(1, userId);
			query.setParameter(2, userEventCategory);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	// remove history data based on provided threshold date
	public int deleteHistoryData(Timestamp timestamp) {
		Query query = em.createQuery("DELETE FROM UserEventEntity u WHERE u.clickDate < ?1");
		query.setParameter(1, timestamp);
		int deletedCount = query.executeUpdate();
		return deletedCount;
	}

	public int countTotal(Timestamp startDate, Timestamp endDate, int realmId) {
		Query query = em.createQuery(
				"select COUNT(p) from UserEventEntity p WHERE p.realmId = ?1 AND p.clickDate >= ?2 AND p.clickDate < ?3");
		query.setParameter(1, realmId);
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	public double getSumPayout(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
		Query query = em.createQuery("select sum(p.offerPayoutInTargetCurrency) from UserEventEntity p "
				+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, approved);
		query.setParameter(4, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.doubleValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	public double getSumPayoutInOriginalCurrency(Timestamp startDate, Timestamp endDate, boolean approved,
			int realmId) {
		Query query = em.createQuery("select sum(p.offerPayout) from UserEventEntity p "
				+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, approved);
		query.setParameter(4, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.doubleValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	public double getSumProfit(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
		Query query = em.createQuery("select sum(p.profitValue) from UserEventEntity p "
				+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, approved);
		query.setParameter(4, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.doubleValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	public double getSumReward(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
		Query query = em.createQuery("select sum(p.rewardValue) from UserEventEntity p "
				+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, approved);
		query.setParameter(4, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.doubleValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	public int getConversionsCount(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
		Query query = em.createQuery("select COUNT(p) from UserEventEntity p "
				+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, approved);
		query.setParameter(4, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.intValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	public int getClicksCount(Timestamp startDate, Timestamp endDate, int realmId) {
		Query query = em.createQuery("select COUNT(p) from UserEventEntity p "
				+ "WHERE p.clickDate >= ?1 AND p.clickDate < ?2 AND p.realmId = ?3");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, realmId);

		try {
			Number result = (Number) query.getSingleResult();
			return result.intValue();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return 0;
		}
	}

	private boolean isFilterPropertyInteger(String filterProperty) {
		boolean state = false;

		switch (filterProperty) {
		case "id":
			state = true;
			break;
		case "realmId":
			state = true;
			break;
		case "userId":
			state = true;
			break;

		default:
			state = false;
			break;
		}

		return state;
	}

	public List<UserEventEntity> getSpinnerRewardInInterval(Timestamp startDate, Timestamp endDate, int realmId,
			int maxResults, String rewardTypeName) {

		try {

			TypedQuery<UserEventEntity> query = em.createQuery(
					"select p from UserEventEntity p WHERE p.userEventCategory = ?1 AND p.realmId = ?2 AND "
							+ "p.rewardDate >= ?3 AND p.rewardDate < ?4 AND p.offerId = ?5 AND p.rewardTypeName = ?6 ORDER BY p.rewardValue DESC",
					UserEventEntity.class).setMaxResults(maxResults);

			query.setParameter(1, "SPINNER");
			query.setParameter(2, realmId);
			query.setParameter(3, startDate);
			query.setParameter(4, endDate);
			query.setParameter(5, "MONEY");
			System.out.println("Selcting rewardType:" + rewardTypeName);
			query.setParameter(6, rewardTypeName);

			List<UserEventEntity> list = query.getResultList();

			return list;

		} catch (NoResultException e) {
			return null;

		}

	}

	public List<UserEventEntity> findByPhoneNumberAndTimeRange(String phoneNumber, int realmId, Timestamp startTime,
			Timestamp endTime) {

		try {
			log.info("Searching for events with phoneNumber: " + phoneNumber + " realmid: " + realmId + " startTime: "
					+ startTime + " endTime: " + endTime);

			TypedQuery<UserEventEntity> query = em.createQuery(
					"select p from UserEventEntity p WHERE p.userEventCategory = ?1 AND p.phoneNumber = ?2 AND p.realmId = ?3 AND "
							+ "p.clickDate >= ?4 AND p.clickDate < ?5 ",
					UserEventEntity.class);

			query.setParameter(1, "INSTALL");
			query.setParameter(2, phoneNumber);
			query.setParameter(2, realmId);
			query.setParameter(3, startTime);
			query.setParameter(4, endTime);

			List<UserEventEntity> list = query.getResultList();

			return list;

		} catch (NoResultException e) {
			return null;

		}

	}

	public List<UserEventEntity> findQuidcoEventsByUserId(int userId) {
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.userEventCategory = ?1 AND o.userId = ?2 ",
				UserEventEntity.class);
		query.setParameter(1, UserEventCategory.QUIDCO.toString());
		query.setParameter(2, userId);

		return query.getResultList();
	}

	public List<UserEventEntity> findSnapdealEventsByUserId(int userId) {
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.userEventCategory = ?1 AND o.userId = ?2 ",
				UserEventEntity.class);
		query.setParameter(1, UserEventCategory.SNAPDEAL.toString());
		query.setParameter(2, userId);

		return query.getResultList();
	}

	public List<UserEventEntity> findSnapdealApprovedEventsByTimeRange(Timestamp startTime, Timestamp endTime) {
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.userEventCategory = ?1 AND (o.rewardResponseStatus = ?2 OR o.rewardResponseStatus = ?3) AND"
						+ " o.rewardDate >= ?4 AND o.rewardDate <= ?5",
				UserEventEntity.class);
		query.setParameter(1, UserEventCategory.SNAPDEAL.toString());
		query.setParameter(2, SnapdealReportType.approved.toString());
		query.setParameter(3, SnapdealReportType.cancelled.toString());
		query.setParameter(4, startTime);
		query.setParameter(5, endTime);

		return query.getResultList();
	}

	public List<UserEventEntity> findEventsWithCategoryAndDateRangeAndRewardType(UserEventCategory category,
			Date startTime, Date endTime, String rewardType) {
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.userEventCategory = ?1 AND o.rewardTypeName = ?2 AND"
						+ " o.rewardDate >= ?3 AND o.rewardDate <= ?4",
				UserEventEntity.class);
		query.setParameter(1, category.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);

		return query.getResultList();
	}

	public List<UserEventEntity> findEventsWithCategoryAndDateRangeAndRewardTypeNotRewarded(UserEventCategory category,
			Date startTime, Date endTime, String rewardType) {
		TypedQuery<UserEventEntity> query = em.createQuery(
				"SELECT o FROM UserEventEntity o WHERE o.userEventCategory = ?1 AND o.rewardTypeName = ?2 AND"
						+ " o.clickDate >= ?3 AND o.clickDate <= ?4",
				UserEventEntity.class);
		query.setParameter(1, category.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);

		return query.getResultList();
	}

	public long countTotalClicksForRewardTypeInDateRange(Date startTime, Date endTime, String rewardType) {

		Query query = em.createQuery("select COUNT(p) from UserEventEntity p "
				+ "WHERE p.userEventCategory = ?1 AND p.rewardTypeName = ?2 AND"
				+ " p.clickDate >= ?3 AND p.clickDate <= ?4");
		query.setParameter(1, UserEventCategory.INSTALL.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);

		try {
			Number result = (Number) query.getSingleResult();
			return result.intValue();
		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
		}

	}

	public long countTotalUniqueClicksForRewardTypeInDateRange(Date startTime, Date endTime, String rewardType) {
		

		Query query = em.createQuery("select DISTINCT p.phoneNumber,p.offerTitle from UserEventEntity p "
				+ "WHERE p.userEventCategory = ?1 AND p.rewardTypeName = ?2 AND"
				+ " p.clickDate >= ?3 AND p.clickDate <= ?4");

		query.setParameter(1, UserEventCategory.INSTALL.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);

		try {
			List<UserEventEntity> events = query.getResultList();
			return events.size();

		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
		}

	}

	public long countTotalConversionsForRewardTypeInDateRange(Date startTime, Date endTime, String rewardType) {
		Query query = em.createQuery(
				"SELECT COUNT(p) FROM UserEventEntity p WHERE p.userEventCategory = ?1 AND p.rewardTypeName = ?2 AND"
						+ " p.clickDate >= ?3 AND p.clickDate <= ?4 AND conversionDate is not null");
		query.setParameter(1, UserEventCategory.INSTALL.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);
		Number result = (Number) query.getSingleResult();
		return result.longValue();
	}
//#SELECT SUM(profitValue) FROM adbroker2.UserEvent where clickDate > '2016-06-22'  and clickDate < '2016-06-29' and userEventCategory = 'install' and conversionDate != '' and rewardTypeName='AirRewardz-India'

	public double sumProfitValueFromEventCategoryInDateRange(UserEventCategory category, String rewardType, Date startTime, Date endTime){
		Query query = em.createQuery(
				"SELECT p FROM UserEventEntity p WHERE p.userEventCategory = ?1 AND p.rewardTypeName = ?2 AND"
						+ " p.clickDate >= ?3 AND p.clickDate <= ?4 AND conversionDate is not null");
		query.setParameter(1, category.toString());
		query.setParameter(2, rewardType);
		query.setParameter(3, startTime);
		query.setParameter(4, endTime);
		
		try {
			List<UserEventEntity> events = query.getResultList();
			double result = 0.0;
			for (UserEventEntity event: events){
				result += event.getProfitValue();
			}
			return result;
		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
		}
	}
	
	

	/*
	 * 
	 * public int countTotalActive(int realmId, Timestamp lastContactTimeWindow)
	 * { Query query = em.createQuery(
	 * "select COUNT(p) from HostsEntity p WHERE p.lastcontact > ?1 AND p.realmId = ?2"
	 * ); query.setParameter(1, lastContactTimeWindow); query.setParameter(2,
	 * realmId);
	 * 
	 * Number result = (Number) query.getSingleResult(); return
	 * result.intValue(); }
	 * 
	 * public List<HostsEntity> findByPartialSN(Integer hwid, String snprefix)
	 * throws Exception { try { TypedQuery<HostsEntity> query = em.createQuery(
	 * "SELECT o FROM HostsEntity o WHERE o.hwid = ?1 AND o.serialno like concat(?2, '%')"
	 * , HostsEntity.class);
	 * 
	 * query.setParameter(1, hwid); query.setParameter(2, snprefix);
	 * 
	 * return query.getResultList(); } catch(NoResultException e) { throw new
	 * Exception(e.toString()); } }
	 * 
	 * //http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-
	 * lazyloading-using-hibernate-criteria-api/ public List<HostsEntity>
	 * findByProfileNameAndMostRecentContact(String profileName, int realmId)
	 * throws Exception { try { //lastInform //lastcontact
	 * TypedQuery<HostsEntity> query = em.createQuery(
	 * "SELECT o FROM HostsEntity o WHERE o.profileName = ?1 AND o.realmId = ?2 ORDER BY o.lastInform DESC"
	 * , HostsEntity.class);
	 * 
	 * query.setParameter(1, profileName); query.setParameter(2, realmId);
	 * query.setMaxResults(10); return query.getResultList(); }
	 * catch(NoResultException e) { throw new Exception(e.toString()); } }
	 */
}
