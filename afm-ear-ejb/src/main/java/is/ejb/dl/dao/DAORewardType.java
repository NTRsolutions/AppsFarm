package is.ejb.dl.dao;

import is.ejb.dl.entities.RewardTypeEntity;

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
public class DAORewardType {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(RewardTypeEntity entity) {
	   em.persist(entity);
   }

   public RewardTypeEntity createOrUpdate(RewardTypeEntity entity) {
	   return em.merge(entity);
   }

   public void delete(RewardTypeEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public RewardTypeEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<RewardTypeEntity> query = em.createQuery(
			        "SELECT o FROM RewardTypeEntity o WHERE o.id = ?1", RewardTypeEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public RewardTypeEntity findByRealmIdAndName(int realmId, String name) throws Exception {
	   try
	   {
		   TypedQuery<RewardTypeEntity> query = em.createQuery(
			        "SELECT o FROM RewardTypeEntity o WHERE o.realm.id = ?1 AND o.name=?2", RewardTypeEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, name);
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<RewardTypeEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<RewardTypeEntity> query = em.createQuery(
			        "SELECT o FROM RewardTypeEntity o", RewardTypeEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<RewardTypeEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<RewardTypeEntity> query = em.createQuery(
			        "SELECT o FROM RewardTypeEntity o WHERE o.realm.id = ?1", RewardTypeEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
   public RewardTypeEntity findByName(String name) throws Exception {
	   try
	   {
		   TypedQuery<RewardTypeEntity> query = em.createQuery(
			        "SELECT o FROM RewardTypeEntity o WHERE o.name = ?1", RewardTypeEntity.class);

		   query.setParameter(1, name);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        e.printStackTrace();
	        return null;
	   } 
   }

   
   

}
