package is.ejb.dl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import is.ejb.dl.entities.RewardCategoryEntity;

@Stateless
public class DAORewardCategory {
	@Inject
	private EntityManager entityManager;

	public void create(RewardCategoryEntity rewardCategory) {
		entityManager.persist(rewardCategory);
	}

	public RewardCategoryEntity createOrUpdate(RewardCategoryEntity rewardCategory) {
		return entityManager.merge(rewardCategory);
	}

	public void delete(RewardCategoryEntity rewardCategory) {
		rewardCategory = entityManager.merge(rewardCategory);
		entityManager.remove(rewardCategory);
	}

	public List<RewardCategoryEntity> getAll() {
		try {
			TypedQuery<RewardCategoryEntity> query = entityManager.createQuery("SELECT o FROM RewardCategoryEntity o",
					RewardCategoryEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	public RewardCategoryEntity getById(int id) {
		TypedQuery<RewardCategoryEntity> query = entityManager
				.createQuery("SELECT o FROM RewardCategoryEntity o WHERE o.id = ?1", RewardCategoryEntity.class);
		query.setParameter(1, id);

		return query.getSingleResult();
	}
	
	public List<RewardCategoryEntity> getByRewardType(String rewardType) {
		TypedQuery<RewardCategoryEntity> query = entityManager
				.createQuery("SELECT o FROM RewardCategoryEntity o WHERE o.rewardType = ?1", RewardCategoryEntity.class);
		query.setParameter(1, rewardType);

		return query.getResultList();
	}
}
