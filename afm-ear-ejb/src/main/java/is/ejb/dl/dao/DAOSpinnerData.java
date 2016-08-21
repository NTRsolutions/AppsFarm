package is.ejb.dl.dao;

import is.ejb.dl.entities.SpinnerDataEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOSpinnerData {
	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(SpinnerDataEntity entity) {
		em.persist(entity);
	}

	public SpinnerDataEntity createOrUpdate(SpinnerDataEntity entity) {
		return em.merge(entity);
	}

	public void delete(SpinnerDataEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public SpinnerDataEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<SpinnerDataEntity> query = em.createQuery("SELECT o FROM SpinnerDataEntity o WHERE o.id = ?1", SpinnerDataEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}



	public List<SpinnerDataEntity> findAll() throws Exception {
		try {
			TypedQuery<SpinnerDataEntity> query = em.createQuery("SELECT o FROM SpinnerDataEntity o", SpinnerDataEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public SpinnerDataEntity findByUserId(Integer id) throws Exception {
		try {
			TypedQuery<SpinnerDataEntity> query = em.createQuery("SELECT o FROM SpinnerDataEntity o WHERE o.userId = ?1", SpinnerDataEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}


}
