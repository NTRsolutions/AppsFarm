package is.ejb.dl.dao;

import is.ejb.dl.entities.DonkySupportEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAODonkySupport {
	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(DonkySupportEntity entity) {
		em.persist(entity);
	}

	public DonkySupportEntity createOrUpdate(DonkySupportEntity entity) {
		return em.merge(entity);
	}

	public void delete(DonkySupportEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public DonkySupportEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<DonkySupportEntity> query = em.createQuery("SELECT o FROM DonkySupportEntity o WHERE o.id = ?1",
					DonkySupportEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public DonkySupportEntity findByConversationId(String id) throws Exception {
		try {
			TypedQuery<DonkySupportEntity> query = em.createQuery(
					"SELECT o FROM DonkySupportEntity o WHERE o.conversationId = ?1", DonkySupportEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public DonkySupportEntity findByTicketId(String id) throws Exception {
		try {
			TypedQuery<DonkySupportEntity> query = em
					.createQuery("SELECT o FROM DonkySupportEntity o WHERE o.ticketId = ?1", DonkySupportEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public DonkySupportEntity findByTicketIdAndRewardType(String id, String rewardType) {
		try {
			log.info("Find by ticket id and reward type : "+ id + " , " + rewardType);
			TypedQuery<DonkySupportEntity> query = em.createQuery(
					"SELECT o FROM DonkySupportEntity o WHERE o.ticketId = ?1 AND o.rewardType = ?2",
					DonkySupportEntity.class);

			query.setParameter(1, id);
			query.setParameter(2, rewardType);

			return query.getSingleResult();

		} catch (NoResultException e) {
			//e.printStackTrace();
			return null;
		}

	}

	public List<DonkySupportEntity> findAll() throws Exception {
		try {
			TypedQuery<DonkySupportEntity> query = em.createQuery("SELECT o FROM DonkySupportEntity o",
					DonkySupportEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	public DonkySupportEntity findByRealmId(int realmId) throws Exception {
		try {
			TypedQuery<DonkySupportEntity> query = em
					.createQuery("SELECT o FROM DonkySupportEntity o WHERE o.realm.id = ?1", DonkySupportEntity.class);

			query.setParameter(1, realmId);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
