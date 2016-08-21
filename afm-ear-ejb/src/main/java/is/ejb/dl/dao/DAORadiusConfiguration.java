package is.ejb.dl.dao;

import is.ejb.dl.entities.AccessPointEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAORadiusConfiguration {
	 @Inject
	   private EntityManager em;
	   
	   @Inject
	   private Logger log;
	   
	   public void create(RadiusConfigurationEntity  entity) {
		   em.persist(entity);
	   }

	   public RadiusConfigurationEntity createOrUpdate(RadiusConfigurationEntity  entity) {
		   //System.out.println("UPDATE");
		   return em.merge(entity);
	   }

	   public void delete(RadiusConfigurationEntity  entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public RadiusConfigurationEntity  findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<RadiusConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM RadiusConfigurationEntity  o WHERE o.id = ?1", RadiusConfigurationEntity .class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   public List<RadiusConfigurationEntity > findAll() throws Exception {
		   try
		   {
			   TypedQuery<RadiusConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM RadiusConfigurationEntity  o", RadiusConfigurationEntity .class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   public List <RadiusConfigurationEntity>  findByCloudtraxId(Integer cloudtraxId) throws Exception {
		   try
		   {
			   TypedQuery<RadiusConfigurationEntity > query = em.createQuery(
				        "SELECT o FROM RadiusConfigurationEntity  o WHERE o.cloudtraxId = ?1", RadiusConfigurationEntity .class);

			   query.setParameter(1, cloudtraxId);

			   return query.getResultList();  
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
}
