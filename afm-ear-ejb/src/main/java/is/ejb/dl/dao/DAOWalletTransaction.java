package is.ejb.dl.dao;

import is.ejb.dl.entities.WalletTransactionEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Stateless
public class DAOWalletTransaction {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(WalletTransactionEntity entity) {
		em.persist(entity);
	}

	public WalletTransactionEntity createOrUpdate(WalletTransactionEntity entity) {
		return em.merge(entity);
	}

	public void delete(WalletTransactionEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public WalletTransactionEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<WalletTransactionEntity> query = em.createQuery(
					"SELECT o FROM WalletTransactionEntity o WHERE o.id = ?1", WalletTransactionEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
			// throw new Exception(e.toString());
		}
	}

	public WalletTransactionEntity findByInternalTransactionId(String id) throws Exception {
		try {
			TypedQuery<WalletTransactionEntity> query = em.createQuery(
					"SELECT o FROM WalletTransactionEntity o WHERE o.internalTransactionId = ?1",
					WalletTransactionEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public WalletTransactionEntity findByTicketIdAndApplicationName(long ticketId, String applicationName)
			throws Exception {
		try {
			TypedQuery<WalletTransactionEntity> query = em.createQuery(
					"SELECT o FROM WalletTransactionEntity o WHERE o.ticketId = ?1 AND o.applicationName=?2",
					WalletTransactionEntity.class);

			query.setParameter(1, ticketId);
			query.setParameter(2, applicationName);
			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<WalletTransactionEntity> findByUserId(int userId) throws Exception {
		TypedQuery<WalletTransactionEntity> query = em.createQuery(
				"SELECT o FROM WalletTransactionEntity o WHERE o.userId = ?1", WalletTransactionEntity.class);

		query.setParameter(1, userId);

		List<WalletTransactionEntity> list = query.getResultList();
		System.out.println("LISTA MA:" + list.size());
		if (list.size() == 0)
			return null;
		else
			return list;
	}

	public WalletTransactionEntity findById(int id) throws Exception {
		TypedQuery<WalletTransactionEntity> query = em
				.createQuery("SELECT o FROM WalletTransactionEntity o WHERE o.id = ?1", WalletTransactionEntity.class);

		query.setParameter(1, id);

		List<WalletTransactionEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);

	}

	public List<WalletTransactionEntity> findAll() throws Exception {
		TypedQuery<WalletTransactionEntity> query = em.createQuery("SELECT o FROM WalletTransactionEntity o",
				WalletTransactionEntity.class);

		List<WalletTransactionEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list;
	}

	public List<WalletTransactionEntity> findFiltered(int first, int pageSize, String sortField, String sortOrder,
			Map<String, String> filters, Timestamp startDate, Timestamp endDate) throws Exception {

		try {
			List<WalletTransactionEntity> data = new ArrayList<WalletTransactionEntity>();

			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<WalletTransactionEntity> accountQuery = criteriaBuilder
					.createQuery(WalletTransactionEntity.class);
			Root<WalletTransactionEntity> from = accountQuery.from(WalletTransactionEntity.class);

			if (sortField != null) {
				if (sortOrder.equals("ascending")) {
					accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
				} else {
					accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
				}
			}

			List<Predicate> predicates = new ArrayList<Predicate>();
			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = (String) it.next();
				String filterValue = (String) filters.get(filterProperty);

				if (isFilterPropertyInteger(filterProperty)) {
					Expression<Integer> literal = criteriaBuilder.literal(new Integer(filterValue));
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: "
							+ literal);
					predicates.add(criteriaBuilder.equal(from.<Integer> get(filterProperty), literal));
				} else {
					Expression<String> literal = criteriaBuilder.literal((String) (filterValue + "%"));
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: "
							+ literal);
					predicates.add(criteriaBuilder.like(from.<String> get(filterProperty), literal));
				}
			}

			predicates.add(criteriaBuilder.between(from.<Date> get("timestamp"), startDate, endDate));

			accountQuery.where((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));

			data = em.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize).getResultList();

			return data;
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public int countTotal(Timestamp startDate, Timestamp endDate, Map<String, String> filters) throws Exception {
		int count = 0;

		final int FIRST = 0;
		final int PAGE_SIZE = Integer.MAX_VALUE;
		final String SORT_FIELD = null;
		final String SORT_ORDER = null;

		List<WalletTransactionEntity> list = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters, startDate,
				endDate);
		count = list.size();

		return count;
	}

	public double getSumPayout(Timestamp startDate, Timestamp endDate, Map<String, String> filters) throws Exception {

		final int FIRST = 0;
		final int PAGE_SIZE = Integer.MAX_VALUE;
		final String SORT_FIELD = null;
		final String SORT_ORDER = null;

		List<WalletTransactionEntity> transactions = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters,
				startDate, endDate);
		return getSumPayout(transactions);
	}

	public double getSumPayout(List<WalletTransactionEntity> transactions) throws Exception {
		double payoutSum = 0;

		for (WalletTransactionEntity transaction : transactions) {
			payoutSum += transaction.getPayoutValue();
		}

		payoutSum = round(payoutSum, 2);

		return payoutSum;
	}

	private double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		return bd.doubleValue();
	}

	private boolean isFilterPropertyInteger(String filter) {
		switch (filter) {
		case "id":
		case "userId":
			return true;
		default:
			return false;
		}
	}

	public List<WalletTransactionEntity> findByUserIdFiltered(int userId, int page, int size) {
		TypedQuery<WalletTransactionEntity> query = em.createQuery(
				"SELECT o FROM WalletTransactionEntity o WHERE o.userId = ?1 ORDER BY o.timestamp DESC",
				WalletTransactionEntity.class);
		query.setParameter(1, userId);
		query.setFirstResult(page * size);
		query.setMaxResults(size);

		List<WalletTransactionEntity> list = query.getResultList();
		if (list == null){
			list =  Collections.emptyList();
		}
		return list;
		
	}

}
