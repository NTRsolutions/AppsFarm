package is.ejb.dl.dao;

import is.ejb.dl.entities.UserEventFailedEntity;
import is.ejb.dl.entities.UserFriendEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOUserFriend {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(UserFriendEntity entity) {
		em.persist(entity);
	}

	public UserFriendEntity createOrUpdate(UserFriendEntity entity) {
		return em.merge(entity);
	}

	public void delete(UserFriendEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public UserFriendEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<UserFriendEntity> query = em.createQuery("SELECT o FROM UserFriendEntity o WHERE o.id = ?1",
					UserFriendEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}

	public List<UserFriendEntity> findByUserId(int userId) throws Exception {
		try {
			TypedQuery<UserFriendEntity> query = em
					.createQuery("SELECT o FROM UserFriendEntity o WHERE o.userId = ?1", UserFriendEntity.class);

			query.setParameter(1, userId);

			return query.getResultList();

		} catch (NoResultException e) {
			throw new Exception(e.toString());
		}
	}
}
