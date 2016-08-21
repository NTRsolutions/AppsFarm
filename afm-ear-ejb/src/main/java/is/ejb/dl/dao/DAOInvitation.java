package is.ejb.dl.dao;

import is.ejb.dl.entities.InvitationEntity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
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
public class DAOInvitation {
	@Inject
	private Logger log;
	@Inject
	private EntityManager em;

	public void create(InvitationEntity entity) {
		em.persist(entity);
	}

	public InvitationEntity createOrUpdate(InvitationEntity entity) {
		return em.merge(entity);
	}

	public InvitationEntity findById(Integer id) {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.id = ?1", InvitationEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	
	public InvitationEntity findByInvitingInternalTransactionId(String id) {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.invitingInternalTransactionId = ?1", InvitationEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public InvitationEntity findByInvitedInternalTransactionId(String id) {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.invitedInternalTransactionId = ?1", InvitationEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public InvitationEntity findByCode(String code) {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.code = ?1", InvitationEntity.class);

			query.setParameter(1, code);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public InvitationEntity findByCodeAndInvitedUserEmail(String code, String email) {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.code = ?1 AND o.emailInvited = ?2", InvitationEntity.class);

			query.setParameter(1, code);
			query.setParameter(2, email);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByDateOfInvitation(Timestamp dateOfInvitation) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.dateOfInvitation = ?1", InvitationEntity.class);

			query.setParameter(1, dateOfInvitation);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByDateOfRegistration(Timestamp dateOfRegistration) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.dateOfRegistration = ?1", InvitationEntity.class);

			query.setParameter(1, dateOfRegistration);

			return query.getResultList();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public InvitationEntity findByEmailInvited(String emailInvited) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.emailInvited = ?1", InvitationEntity.class);

			query.setParameter(1, emailInvited);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findAllByEmailInvited(String email) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.emailInvited = ?1", InvitationEntity.class);

			query.setParameter(1, email);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findAllByEmailInviting(String email) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.emailInviting = ?1", InvitationEntity.class);

			query.setParameter(1, email);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public InvitationEntity findByEmailInvitedAndEmailInviting(String emailInvited, String emailInviting) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.emailInvited = ?1 AND o.emailInviting = ?2", InvitationEntity.class);

			query.setParameter(1, emailInvited);
			query.setParameter(2, emailInviting);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByRealized(boolean realized) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.isRealized = ?1", InvitationEntity.class);

			query.setParameter(1, realized);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByPhoneNumberInvited(String phoneNumber) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.phoneNumberInvited = ?1", InvitationEntity.class);

			query.setParameter(1, phoneNumber);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByPhoneNumberInviting(String phoneNumber) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.phoneNumberInviting = ?1", InvitationEntity.class);

			query.setParameter(1, phoneNumber);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByRewardType(String rewardType) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.rewardType = ?1", InvitationEntity.class);

			query.setParameter(1, rewardType);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findByRewardValue(int rewardValue) throws Exception {
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.rewardValue = ?1", InvitationEntity.class);

			query.setParameter(1, rewardValue);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<InvitationEntity> findFiltered(int first, int pageSize, String sortField, String sortOrder, Map<String, String> filters, Timestamp startDate, Timestamp endDate) throws Exception {

		try {
			List<InvitationEntity> data = new ArrayList<InvitationEntity>();

			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitationEntity> accountQuery = criteriaBuilder.createQuery(InvitationEntity.class);
			Root<InvitationEntity> from = accountQuery.from(InvitationEntity.class);

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
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: " + literal);
					predicates.add(criteriaBuilder.equal(from.<Integer> get(filterProperty), literal));
				} else {
					Expression<String> literal = criteriaBuilder.literal((String) (filterValue + "%"));
					log.info("filterProperty: " + filterProperty + " filterValue: " + filterValue + " literal: " + literal);
					predicates.add(criteriaBuilder.like(from.<String> get(filterProperty), literal));
				}
			}

			predicates.add(criteriaBuilder.between(from.<Date> get("dateOfInvitation"), startDate, endDate));

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

		List<InvitationEntity> list = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters, startDate, endDate);
		count = list.size();

		return count;
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
	
	public List<InvitationEntity> findByRewardTypeInDateRange(String rewardType, java.util.Date startDate, java.util.Date endDate){
		try {
			TypedQuery<InvitationEntity> query = em.createQuery("SELECT o FROM InvitationEntity o WHERE o.rewardTypeName = ?1 AND o.dateOfRegistration >= ?2 AND o.dateOfRegistration <= ?3", InvitationEntity.class);

			query.setParameter(1, rewardType);
			query.setParameter(2, startDate);
			query.setParameter(3, endDate);
			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

}
