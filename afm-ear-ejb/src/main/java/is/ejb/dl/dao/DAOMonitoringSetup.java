package is.ejb.dl.dao;

import is.ejb.dl.entities.MonitoringSetupEntity;

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
public class DAOMonitoringSetup {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(MonitoringSetupEntity entity) {
	   em.persist(entity);
   }

   public MonitoringSetupEntity createOrUpdate(MonitoringSetupEntity entity) {
	   return em.merge(entity);
   }

   public void delete(MonitoringSetupEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public MonitoringSetupEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<MonitoringSetupEntity> query = em.createQuery(
			        "SELECT o FROM MonitoringSetupEntity o WHERE o.id = ?1", MonitoringSetupEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public List<MonitoringSetupEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<MonitoringSetupEntity> query = em.createQuery(
			        "SELECT o FROM MonitoringSetupEntity o", MonitoringSetupEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public MonitoringSetupEntity findByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<MonitoringSetupEntity> query = em.createQuery(
			        "SELECT o FROM MonitoringSetupEntity o WHERE o.realm.id = ?1", MonitoringSetupEntity.class);

		   query.setParameter(1, realmId);
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }


}
