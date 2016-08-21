package is.ejb.dl.dao;

import is.ejb.dl.entities.MaintenanceConfigurationEntity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

@Stateless
public class DAOMaintenanceConfiguration {
		
   @Inject
   private EntityManager em;

   public void create(MaintenanceConfigurationEntity entity) {
	   em.persist(entity);
   }

   public MaintenanceConfigurationEntity createOrUpdate(MaintenanceConfigurationEntity entity) {
	   return em.merge(entity);
   }

   public void delete(MaintenanceConfigurationEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public MaintenanceConfigurationEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<MaintenanceConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM MaintenanceConfigurationEntity o WHERE o.maintenanceConfigurationId = ?1", MaintenanceConfigurationEntity.class);
		   query.setParameter(1, id);
		   
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public MaintenanceConfigurationEntity findActive() throws Exception {
	   try
	   {
		   TypedQuery<MaintenanceConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM MaintenanceConfigurationEntity o WHERE o.active = ?1", MaintenanceConfigurationEntity.class);
		   query.setParameter(1, true);
		   
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<MaintenanceConfigurationEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<MaintenanceConfigurationEntity> query = em.createQuery(
			        "SELECT o FROM MaintenanceConfigurationEntity o", MaintenanceConfigurationEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
