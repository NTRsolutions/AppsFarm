package is.ejb.dl.dao;

import is.ejb.dl.entities.CloudtraxConfigurationEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOCloudtraxConfiguration {
	 @Inject
	   private EntityManager em;
	   
	   @Inject
	   private Logger log;
	   
	   public void create(CloudtraxConfigurationEntity  entity) {
		   em.persist(entity);
	   }

	   public CloudtraxConfigurationEntity createOrUpdate(CloudtraxConfigurationEntity  entity) {
		   System.out.println("UPDATE");
		   return em.merge(entity);
	   }

	   public void delete(CloudtraxConfigurationEntity  entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public CloudtraxConfigurationEntity  findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<CloudtraxConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM CloudtraxConfigurationEntity  o WHERE o.id = ?1", CloudtraxConfigurationEntity .class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   public CloudtraxConfigurationEntity  findByNetworkName(String networkName) throws Exception {
		   try
		   {
			   TypedQuery<CloudtraxConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM CloudtraxConfigurationEntity  o WHERE o.networkName = ?1", CloudtraxConfigurationEntity .class);

			   query.setParameter(1, networkName);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   public List<CloudtraxConfigurationEntity > findAll() throws Exception {
		   try
		   {
			   TypedQuery<CloudtraxConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM CloudtraxConfigurationEntity  o", CloudtraxConfigurationEntity .class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   
}
