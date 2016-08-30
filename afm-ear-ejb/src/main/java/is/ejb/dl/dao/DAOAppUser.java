package is.ejb.dl.dao;

import is.ejb.dl.entities.AppUserEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class DAOAppUser {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(AppUserEntity entity) {
		em.persist(entity);
	}

	public AppUserEntity createOrUpdate(AppUserEntity entity) {
		return em.merge(entity);
	}

	public void delete(AppUserEntity entity) {
		entity = em.merge(entity);
		// em.remove(entity);
		em.createQuery("DELETE FROM AppUserEntity e WHERE e.id = :id").setParameter("id", entity.getId())
				.executeUpdate();
	}

	public AppUserEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.id = ?1",
					AppUserEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public AppUserEntity findByMac(String mac) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.mac = ?1",
					AppUserEntity.class);

			query.setParameter(1, mac);

			if (query.getResultList().size() > 0)
				return query.getResultList().get(0);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public AppUserEntity findByEmail(String email) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.email = ?1",
					AppUserEntity.class);

			query.setParameter(1, email);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			return null;
		}

	}

	public AppUserEntity findByPhoneNumber(String phoneNumber) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.phoneNumber = ?1",
					AppUserEntity.class);

			query.setParameter(1, phoneNumber);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	
	
	public AppUserEntity findByUsername(String username) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.username = ?1",
					AppUserEntity.class);

			query.setParameter(1, username);

			return query.getSingleResult();

		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		}
	}

	public AppUserEntity findByReferralCode(String referralCode) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.referralCode = ?1",
					AppUserEntity.class);

			query.setParameter(1, referralCode);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public AppUserEntity findByName(String name) throws Exception {
		TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.name = ?1",
				AppUserEntity.class);

		query.setParameter(1, name);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByDeviceId(String deviceId) throws Exception {
		TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.deviceId = ?1",
				AppUserEntity.class);

		query.setParameter(1, deviceId);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByPhoneId(String phoneId) throws Exception {
		TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.phoneId = ?1",
				AppUserEntity.class);

		query.setParameter(1, phoneId);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByFBInvitationCode(String fbInvitationCode) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em
					.createQuery("SELECT o FROM AppUserEntity o WHERE o.fbInvitationCode = ?1", AppUserEntity.class);

			query.setParameter(1, fbInvitationCode);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public int countTotal(int realmId) {
		Query query = em.createQuery("select COUNT(p) from AppUserEntity p WHERE p.realmId = ?1");
		query.setParameter(1, realmId);
		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	// http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-lazyloading-using-hibernate-criteria-api/
	public List<AppUserEntity> findFiltered(int first, int pageSize, String sortField, String sortOrder, Map filters,
			int realmId) throws Exception {
		try {
			List data = new ArrayList();

			// Criteria
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery accountQuery = criteriaBuilder.createQuery(AppUserEntity.class);
			// From
			Root from = accountQuery.from(AppUserEntity.class);

			// sort
			if (sortField != null) {
				if (sortOrder.equals("ascending")) {
					accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
				} else {
					accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
				}
			}

			// filters
			List predicates = new ArrayList();
			for (Iterator it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = (String) it.next(); // table column name
															// = field name
				String filterValue = (String) filters.get(filterProperty);
				System.out.println("filterProperty: " + filterProperty + " filterValue: " + filterValue);
				// Expression literal =
				// criteriaBuilder.literal((String)filterValue);
				if (filterProperty.equals("id")) {
					Expression literal = criteriaBuilder.literal(new Integer(filterValue));
					System.out.println("filterProperty: " + filterProperty + " filterValue: " + filterValue
							+ " literal: " + literal);
					predicates.add(criteriaBuilder.like(from.get(filterProperty), literal));
				} else {
					Expression literal = criteriaBuilder.literal((String) (filterValue + "%"));
					System.out.println("filterProperty: " + filterProperty + " filterValue: " + filterValue
							+ " literal: " + literal);
					predicates.add(criteriaBuilder.like(from.get(filterProperty), literal));
				}
			}
			// only for given realm
			predicates.add(criteriaBuilder.equal(from.get("realmId"), realmId));

			accountQuery.where((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));

			// paginate
			data = em.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize).getResultList();

			return data;

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public AppUserEntity findByDeviceIdAndApplicationName(String deviceId, String applicationName) {
		TypedQuery<AppUserEntity> query = em.createQuery(
				"SELECT o FROM AppUserEntity o WHERE o.deviceId = ?1 AND o.applicationName=?2", AppUserEntity.class);

		query.setParameter(1, deviceId);
		query.setParameter(2, applicationName);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByPhoneIdAndApplicationName(String phoneId, String applicationName) throws Exception {
		TypedQuery<AppUserEntity> query = em.createQuery(
				"SELECT o FROM AppUserEntity o WHERE o.phoneId = ?1 AND o.applicationName=?2", AppUserEntity.class);

		query.setParameter(1, phoneId);
		query.setParameter(2, applicationName);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByMacAndApplicationName(String mac, String applicationName) {
		TypedQuery<AppUserEntity> query = em.createQuery(
				"SELECT o FROM AppUserEntity o WHERE o.mac = ?1 AND o.applicationName=?2", AppUserEntity.class);

		query.setParameter(1, mac);
		query.setParameter(2, applicationName);

		List<AppUserEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public AppUserEntity findByQuidcoUserId(Integer id) throws Exception {
		try {
			TypedQuery<AppUserEntity> query = em.createQuery("SELECT o FROM AppUserEntity o WHERE o.quidcoUserId = ?1",
					AppUserEntity.class);

			query.setParameter(1, ""+id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

}
