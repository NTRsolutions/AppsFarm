package is.ejb.dl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import is.ejb.dl.entities.CountryEntity;

@Stateless
public class DAOCountries {

	@Inject
	private EntityManager entityManager;

	public void create(CountryEntity entity) {
		entityManager.persist(entity);
	}

	public CountryEntity createOrUpdate(CountryEntity entity) {
		return entityManager.merge(entity);
	}

	public void delete(CountryEntity entity) {
		entity = entityManager.merge(entity);
		entityManager.remove(entity);
	}

	public List<CountryEntity> getAll() {
		try {
			TypedQuery<CountryEntity> query = entityManager.createQuery("SELECT o FROM CountryEntity o",
					CountryEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<CountryEntity>();
		}
	}

	public List<String> getAllCodes() {
		try {
			TypedQuery<String> query = entityManager.createQuery("SELECT o.code FROM CountryEntity o", String.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<String>();
		}
	}

	public CountryEntity findByCode(String code) {
		try {
			TypedQuery<CountryEntity> query = entityManager
					.createQuery("SELECT o FROM CountryEntity o WHERE o.code = ?1", CountryEntity.class);
			query.setParameter(1, code);

			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
