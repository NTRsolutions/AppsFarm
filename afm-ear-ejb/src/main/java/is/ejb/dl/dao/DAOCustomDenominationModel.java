package is.ejb.dl.dao;

import is.ejb.dl.entities.CustomDenominationModelEntity;

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
public class DAOCustomDenominationModel {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(CustomDenominationModelEntity entity) {
	   em.persist(entity);
   }

   public CustomDenominationModelEntity createOrUpdate(CustomDenominationModelEntity entity) {
	   return em.merge(entity);
   }

   public void delete(CustomDenominationModelEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public CustomDenominationModelEntity findByRealmId(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<CustomDenominationModelEntity> query = em.createQuery(
			        "SELECT o FROM CustomDenominationModelEntity o WHERE o.realmId = ?1", CustomDenominationModelEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<CustomDenominationModelEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<CustomDenominationModelEntity> query = em.createQuery(
			        "SELECT o FROM CustomDenominationModelEntity o", CustomDenominationModelEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }


}
