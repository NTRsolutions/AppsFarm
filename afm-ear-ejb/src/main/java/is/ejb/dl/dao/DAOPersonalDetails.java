package is.ejb.dl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import is.ejb.dl.entities.PersonalDetailsEntity;

@Stateless
public class DAOPersonalDetails {
	@Inject
	private EntityManager entityManager;

	public void create(PersonalDetailsEntity rewardCategory) {
		entityManager.persist(rewardCategory);
	}

	public PersonalDetailsEntity createOrUpdate(PersonalDetailsEntity rewardCategory) {
		return entityManager.merge(rewardCategory);
	}

	public void delete(PersonalDetailsEntity rewardCategory) {
		rewardCategory = entityManager.merge(rewardCategory);
		entityManager.remove(rewardCategory);
	}

	public PersonalDetailsEntity findByUserId(int userId) {
		try {
			TypedQuery<PersonalDetailsEntity> query = entityManager.createQuery(
					"SELECT o FROM PersonalDetailsEntity o WHERE o.userId = ?1", PersonalDetailsEntity.class);
			query.setParameter(1, userId);
			return query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		}
	}
}
