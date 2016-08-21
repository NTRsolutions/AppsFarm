package is.ejb.dl.dao;

import is.ejb.dl.entities.SpinnerRewardEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
@Stateless
public class DAOSpinnerReward {
	 @Inject
	   private EntityManager em;
	   
	   @Inject
	   private Logger log;
	   
	   public void create(SpinnerRewardEntity  entity) {
		   em.persist(entity);
	   }

	   public SpinnerRewardEntity createOrUpdate(SpinnerRewardEntity  entity) {
		   //System.out.println("UPDATE");
		   return em.merge(entity);
	   }

	   public void delete(SpinnerRewardEntity  entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public SpinnerRewardEntity  findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<SpinnerRewardEntity > query = em.createQuery(
				        "SELECT o FROM SpinnerRewardEntity  o WHERE o.id = ?1", SpinnerRewardEntity .class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   
	   public List<SpinnerRewardEntity>  findByRewardTypeId(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<SpinnerRewardEntity > query = em.createQuery(
				        "SELECT o FROM SpinnerRewardEntity  o WHERE o.rewardTypeId = ?1", SpinnerRewardEntity .class);

			   query.setParameter(1, id);

			   return query.getResultList();  
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   
	 

	   public List<SpinnerRewardEntity > findAll() throws Exception {
		   try
		   {
			   TypedQuery<SpinnerRewardEntity > query = em.createQuery(
				        "SELECT o FROM SpinnerRewardEntity  o", SpinnerRewardEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

}
