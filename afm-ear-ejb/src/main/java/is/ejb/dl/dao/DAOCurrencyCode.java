package is.ejb.dl.dao;

import is.ejb.dl.entities.CurrencyCodeEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class DAOCurrencyCode {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(CurrencyCodeEntity entity) {
	   em.persist(entity);
   }

   public CurrencyCodeEntity createOrUpdate(CurrencyCodeEntity entity) {
	   return em.merge(entity);
   }

   public void delete(CurrencyCodeEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public CurrencyCodeEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<CurrencyCodeEntity> query = em.createQuery(
			        "SELECT o FROM CurrencyCodeEntity o WHERE o.id = ?1", CurrencyCodeEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public List<CurrencyCodeEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<CurrencyCodeEntity> query = em.createQuery(
			        "SELECT o FROM CurrencyCodeEntity o", CurrencyCodeEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public CurrencyCodeEntity findByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<CurrencyCodeEntity> query = em.createQuery(
			        "SELECT o FROM CurrencyCodeEntity o WHERE o.realm.id = ?1", CurrencyCodeEntity.class);

		   query.setParameter(1, realmId);
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }


}
