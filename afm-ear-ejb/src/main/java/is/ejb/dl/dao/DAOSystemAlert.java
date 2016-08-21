package is.ejb.dl.dao;

import is.ejb.dl.entities.SystemAlertEntity;

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
public class DAOSystemAlert {

   @Inject
   private EntityManager em;

   @Inject
   private Logger log;

   public void create(SystemAlertEntity entity) {
	   em.persist(entity);
   }

   public SystemAlertEntity createOrUpdate(SystemAlertEntity entity) {
	   return em.merge(entity);
   }

   public void delete(SystemAlertEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public SystemAlertEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<SystemAlertEntity> query = em.createQuery(
			        "SELECT o FROM SystemAlertEntity o WHERE o.alertId = ?1", SystemAlertEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public SystemAlertEntity findByNodeIdAndAlertType(Integer nodeId, String alertType) {
	   try
	   {
		   TypedQuery<SystemAlertEntity> query = em.createQuery(
			        "SELECT o FROM SystemAlertEntity o WHERE o.nodeId = ?1 AND o.alertType = ?2", SystemAlertEntity.class);

		   query.setParameter(1, nodeId);
		   query.setParameter(2, alertType);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        //throw new Exception(e.toString());
		   return null;
	   } 
   }

   public List<SystemAlertEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<SystemAlertEntity> query = em.createQuery(
			        "SELECT o FROM SystemAlertEntity o ORDER BY o.timestamp DESC", SystemAlertEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
