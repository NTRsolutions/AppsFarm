package is.ejb.dl.dao;

import is.ejb.dl.entities.DeviceAlertsConfigurationEntity;

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
public class DAODeviceAlertsConfiguration {
		
   @Inject
   private EntityManager em;

   @Inject
   private Logger log;

   public void create(DeviceAlertsConfigurationEntity entity) {
	   em.persist(entity);
   }

   public DeviceAlertsConfigurationEntity createOrUpdate(DeviceAlertsConfigurationEntity entity) {
	   return em.merge(entity);
   }

   public void delete(DeviceAlertsConfigurationEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public DeviceAlertsConfigurationEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.alertsConfigurationId = ?1", DeviceAlertsConfigurationEntity.class);
		   query.setParameter(1, id);
		   
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public DeviceAlertsConfigurationEntity findByIdAndRealmId(Integer id, int realmId) throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.alertsConfigurationId = ?1 AND o.realmId = ?2", DeviceAlertsConfigurationEntity.class);

		   query.setParameter(1, id);
		   query.setParameter(2, realmId);
		   
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public DeviceAlertsConfigurationEntity findByName(String name) throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.name = ?1", DeviceAlertsConfigurationEntity.class);

		   query.setParameter(1, name);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public DeviceAlertsConfigurationEntity findByNameAndRealmId(String name, int realmId) throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.name = ?1 AND o.realmId = ?2", DeviceAlertsConfigurationEntity.class);

		   query.setParameter(1, name);
		   query.setParameter(2, realmId);
		   
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public DeviceAlertsConfigurationEntity findByDeviceId(Integer id) {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.deviceId = ?1", DeviceAlertsConfigurationEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   //e.printStackTrace();
		   System.out.println("ERROR DAODeviceAlert: "+e.toString());
		   
		   return null;
	   }  
   }

   public List<DeviceAlertsConfigurationEntity> findAll(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.realmId = ?1", DeviceAlertsConfigurationEntity.class);
		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<DeviceAlertsConfigurationEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<DeviceAlertsConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM DeviceAlertsConfigurationEntity o", DeviceAlertsConfigurationEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

//   public List<DeviceAlertsConfigurationEntity> findByNodeId(int nodeId, boolean isSortAscending) throws Exception {
//	   try
//	   {
//		   TypedQuery<DeviceAlertsConfigurationEntity> query = null;
//		   
//		   if(isSortAscending) {
//			   query = em.createQuery("SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.nodeId = ?1 ORDER BY o.timestamp ASC", DeviceAlertsConfigurationEntity.class);
//		   } 
//		   else {
//			   query = em.createQuery("SELECT o FROM DeviceAlertsConfigurationEntity o WHERE o.nodeId = ?1 ORDER BY o.timestamp DESC", DeviceAlertsConfigurationEntity.class);
//		   }
//			   
//		   query.setParameter(1, nodeId);
//
//		   return query.getResultList();
//	   } catch(NoResultException e) {
//	        throw new Exception(e.toString());
//	   } 
//   }

}
