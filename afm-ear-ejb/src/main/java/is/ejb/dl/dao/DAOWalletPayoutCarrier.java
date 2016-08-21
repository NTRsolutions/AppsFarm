package is.ejb.dl.dao;

import is.ejb.dl.entities.WalletPayoutCarrierEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOWalletPayoutCarrier {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(WalletPayoutCarrierEntity entity) {
		em.persist(entity);
	}

	public WalletPayoutCarrierEntity createOrUpdate(
			WalletPayoutCarrierEntity entity) {
		return em.merge(entity);
	}

	public void delete(WalletPayoutCarrierEntity entity) {
		entity = em.merge(entity);
		// em.remove(entity);
		em.createQuery(
				"DELETE FROM WalletPayoutCarrierEntity e WHERE e.id = :id")
				.setParameter("id", entity.getId()).executeUpdate();
	}

	public WalletPayoutCarrierEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<WalletPayoutCarrierEntity> query = em
					.createQuery(
							"SELECT o FROM WalletPayoutCarrierEntity o WHERE o.id = ?1",
							WalletPayoutCarrierEntity.class);

			query.setParameter(1, id);

			if (query == null)
				return null;
			else
				return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<WalletPayoutCarrierEntity> findByRealmId(int realmId)
			throws Exception {
		TypedQuery<WalletPayoutCarrierEntity> query = em
				.createQuery(
						"SELECT o FROM WalletPayoutCarrierEntity o WHERE o.realmId = ?1",
						WalletPayoutCarrierEntity.class);

		query.setParameter(1, realmId);

		List<WalletPayoutCarrierEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list;
	}

	public List<WalletPayoutCarrierEntity> findByRewardTypeId(int rewardTypeId)
			throws Exception {
		TypedQuery<WalletPayoutCarrierEntity> query = em
				.createQuery(
						"SELECT o FROM WalletPayoutCarrierEntity o WHERE o.rewardTypeId = ?1",
						WalletPayoutCarrierEntity.class);

		query.setParameter(1, rewardTypeId);

		List<WalletPayoutCarrierEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list;
	}

	public List<WalletPayoutCarrierEntity> findAll() throws Exception {
		TypedQuery<WalletPayoutCarrierEntity> query = em.createQuery(
				"SELECT o FROM WalletPayoutCarrierEntity o",
				WalletPayoutCarrierEntity.class);

		List<WalletPayoutCarrierEntity> list = query.getResultList();
		if (list.size() == 0)
			return null;
		else
			return list;
	}

	public WalletPayoutCarrierEntity findByCarrierName(String carrierName) throws Exception {
		try {
			TypedQuery<WalletPayoutCarrierEntity> query = em
					.createQuery(
							"SELECT o FROM WalletPayoutCarrierEntity o WHERE o.carrierName = ?1",
							WalletPayoutCarrierEntity.class);

			query.setParameter(1, carrierName);

			if (query == null)
				return null;
			else
				return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	
}
