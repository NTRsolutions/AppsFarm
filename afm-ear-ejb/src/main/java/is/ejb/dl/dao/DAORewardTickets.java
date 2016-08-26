package is.ejb.dl.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import is.ejb.bl.business.RewardTicketStatus;
import is.ejb.dl.entities.RewardTicketEntity;

@Stateless
public class DAORewardTickets {

	@Inject
	private EntityManager entityManager;

	public void create(RewardTicketEntity rewardTicket) {
		entityManager.persist(rewardTicket);
	}

	public RewardTicketEntity createOrUpdate(RewardTicketEntity rewardTicket) {
		return entityManager.merge(rewardTicket);
	}

	public void delete(RewardTicketEntity rewardTicket) {
		rewardTicket = entityManager.merge(rewardTicket);
		entityManager.remove(rewardTicket);
	}

	public List<RewardTicketEntity> getAll() {
		try {
			TypedQuery<RewardTicketEntity> query = entityManager.createQuery("SELECT o FROM RewardTicketEntity o",
					RewardTicketEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	public List<RewardTicketEntity> getAllByUSerId(int userId) {
		TypedQuery<RewardTicketEntity> query = entityManager
				.createQuery("SELECT o FROM RewardTicketEntity o WHERE o.userId = ?1", RewardTicketEntity.class);
		query.setParameter(1, userId);

		return query.getResultList();
	}

	public RewardTicketEntity getByUserId(int userId) {
		TypedQuery<RewardTicketEntity> query = entityManager
				.createQuery("SELECT o FROM RewardTicketEntity o WHERE o.userId = ?1", RewardTicketEntity.class);
		query.setParameter(1, userId);

		return query.getSingleResult();
	}

	public List<RewardTicketEntity> getAllByStatus(RewardTicketStatus status) {
		TypedQuery<RewardTicketEntity> query = entityManager
				.createQuery("SELECT o FROM RewardTicketEntity o WHERE o.status = ?1", RewardTicketEntity.class);
		query.setParameter(1, status.toString());

		return query.getResultList();
	}
	
	public RewardTicketEntity getByHash(String hash) {
		TypedQuery<RewardTicketEntity> query = entityManager
				.createQuery("SELECT o FROM RewardTicketEntity o WHERE o.hash = ?1", RewardTicketEntity.class);
		query.setParameter(1, hash);

		return query.getSingleResult();
	}

	public List<RewardTicketEntity> findFiltered(int first, int pageSize, String sortField, String sortOrder,
			Map<String, String> filters, Timestamp startDate, Timestamp endDate, int realmId) throws Exception {
		try {
			List<RewardTicketEntity> data = new ArrayList<RewardTicketEntity>();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<RewardTicketEntity> accountQuery = criteriaBuilder.createQuery(RewardTicketEntity.class);

			Root<RewardTicketEntity> from = accountQuery.from(RewardTicketEntity.class);

			if (sortField != null) {
				if (sortOrder.equals("ascending")) {
					accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
				} else {
					accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
				}
			}

			List<Predicate> predicates = new ArrayList<Predicate>();
			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = it.next();
				String filterValue = filters.get(filterProperty);

				if (isFilterPropertyInteger(filterProperty)) {
					Expression<Integer> literal = criteriaBuilder.literal(new Integer(filterValue));
					predicates.add(criteriaBuilder.equal(from.<Integer>get(filterProperty), literal));
				} else {
					Expression<String> literal = criteriaBuilder.literal((String) (filterValue + "%"));
					predicates.add(criteriaBuilder.like(from.<String>get(filterProperty), literal));
				}
			}

			predicates.add(criteriaBuilder.between(from.<Date>get("requestDate"), startDate, endDate));

			accountQuery.where(predicates.toArray(new Predicate[predicates.size()]));

			data = entityManager.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize)
					.getResultList();

			return data;
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public int countTotal(Timestamp startDate, Timestamp endDate, Map<String, String> filters, int realmId)
			throws Exception {
		int count = 0;

		final int FIRST = 0;
		final int PAGE_SIZE = Integer.MAX_VALUE;
		final String SORT_FIELD = null;
		final String SORT_ORDER = null;

		List<RewardTicketEntity> list = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters, startDate,
				endDate, realmId);
		count = list.size();

		return count;
	}

	private boolean isFilterPropertyInteger(String filterProperty) {
		switch (filterProperty) {
		case "id":
			return true;
		case "userId":
			return true;
		default:
			return false;
		}
	}

	public double getSumCreditPoints(Timestamp startDate, Timestamp endDate) {
		Query query = entityManager.createQuery("select sum(p.creditPoints) from RewardTicketEntity p "
				+ "WHERE p.requestDate >= ?1 AND p.requestDate < ?2");
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);

		try {
			Number result = (Number) query.getSingleResult();
			return result.doubleValue();
		} catch (Exception exc) {
			return 0;
		}
	}

}
