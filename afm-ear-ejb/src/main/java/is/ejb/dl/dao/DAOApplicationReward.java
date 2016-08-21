package is.ejb.dl.dao;

import is.ejb.dl.entities.AccessPointEntity;
import is.ejb.dl.entities.ApplicationRewardEntity;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOApplicationReward {

	 @Inject
	   private EntityManager em;
	 
	 public void create(ApplicationRewardEntity  entity) {
		   em.persist(entity);
	   }

	   public ApplicationRewardEntity createOrUpdate(ApplicationRewardEntity  entity) {
		   //System.out.println("UPDATE");
		   return em.merge(entity);
	   }

	   public void delete(ApplicationRewardEntity  entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public ApplicationRewardEntity  findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<ApplicationRewardEntity > query = em.createQuery(
				        "SELECT o FROM ApplicationRewardEntity  o WHERE o.id = ?1", ApplicationRewardEntity .class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   public List<ApplicationRewardEntity>  findByApplicationName(String applicationName) throws Exception {
		   try
		   {
			   TypedQuery<ApplicationRewardEntity > query = em.createQuery(
				        "SELECT o FROM ApplicationRewardEntity  o WHERE o.applicationName = ?1", ApplicationRewardEntity .class);

			   query.setParameter(1, applicationName);

			   return query.getResultList(); 
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   
	   public List<ApplicationRewardEntity> findAll() throws Exception {
		   try
		   {
			   TypedQuery<ApplicationRewardEntity> query = em.createQuery(
				        "SELECT o FROM ApplicationRewardEntity  o", ApplicationRewardEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
}
