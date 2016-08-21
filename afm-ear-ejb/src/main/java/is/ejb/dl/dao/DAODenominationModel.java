package is.ejb.dl.dao;

import is.ejb.dl.entities.DenominationModelEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class DAODenominationModel {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(DenominationModelEntity entity) {
	   em.persist(entity);
   }

   public DenominationModelEntity createOrUpdate(DenominationModelEntity entity) {
	   return em.merge(entity);
   }

   public void delete(DenominationModelEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public DenominationModelEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<DenominationModelEntity> query = em.createQuery(
			        "SELECT o FROM DenominationModelEntity o WHERE o.id = ?1", DenominationModelEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public DenominationModelEntity findByName(String name, int realmId) throws Exception {
	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE o.name = ?1 AND o.realm.id = ?2", DenominationModelEntity.class);

	   query.setParameter(1, name);
	   query.setParameter(2, realmId);

	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public DenominationModelEntity findByRewardTypeNameAndRealmId(boolean defaultModelAppliedGlobally, 
		   String rewardTypeName, 
		   int realmId) throws Exception {
	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE "
		        + "o.defaultModel = ?1 AND "
		        + "o.rewardTypeName = ?2 AND "
		        + "o.realm.id = ?3",		         
		        DenominationModelEntity.class);

	   query.setParameter(1, defaultModelAppliedGlobally);
	   query.setParameter(2, rewardTypeName);
	   query.setParameter(3, realmId);

	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public DenominationModelEntity findBySourcePayoutCurrencyCodeAndRewardTypeNameAndRealmId(boolean defaultModelAppliedGlobally, 
		   String sourcePayoutCurrencyCode, 
		   String rewardTypeName, 
		   int realmId) throws Exception {
	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE "
		        + "o.defaultModel = ?1 AND "
		        + "o.sourcePayoutCurrencyCode = ?2 AND "
		        + "o.rewardTypeName = ?3 AND "
		        + "o.realm.id = ?4",		         
		        DenominationModelEntity.class);

	   query.setParameter(1, defaultModelAppliedGlobally);
	   query.setParameter(2, sourcePayoutCurrencyCode);
	   query.setParameter(3, rewardTypeName);
	   query.setParameter(4, realmId);


	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public DenominationModelEntity findByTargetPayoutCurrencyCodeAndRewardTypeNameAndRealmId(boolean defaultModelAppliedGlobally,
		   String targetPayoutCurrencyCode, 
		   String rewardTypeName, int realmId) throws Exception {

	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE "
		        + "o.defaultModel = ?1 AND "
		        + "o.targetPayoutCurrencyCode = ?2 AND "
		        + "o.rewardTypeName = ?3 AND "
		        + "o.realm.id = ?4", DenominationModelEntity.class);

	   query.setParameter(1, defaultModelAppliedGlobally);
	   query.setParameter(2, targetPayoutCurrencyCode);
	   query.setParameter(3, rewardTypeName);
	   query.setParameter(4, realmId);

	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public DenominationModelEntity findByNameAndRealmId(String name, int realmId) throws Exception {
	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE o.name = ?1 AND o.realm.id = ?2", DenominationModelEntity.class);

	   query.setParameter(1, name);
	   query.setParameter(2, realmId);

	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<DenominationModelEntity> findByRewardTypeNameAndRealmId(String rewardTypeName, int realmId) throws Exception {
	   TypedQuery<DenominationModelEntity> query = em.createQuery(
		        "SELECT o FROM DenominationModelEntity o WHERE o.rewardTypeName = ?1 AND o.realm.id = ?2", DenominationModelEntity.class);

	   query.setParameter(1, rewardTypeName);
	   query.setParameter(2, realmId);

	   List<DenominationModelEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list;
   }

   public int getRegisteredDenominationModelsNumberByRewardTypeNameAndRealmId(String rewardTypeName, int realmId) throws Exception {
       Query query = em.createQuery("select COUNT(o) from DenominationModelEntity o WHERE o.rewardTypeName = ?1 AND o.realm.id = ?2");
       query.setParameter(1, rewardTypeName);
       query.setParameter(2, realmId);
       Number result = (Number) query.getSingleResult();
       return result.intValue();
   }

   public List<DenominationModelEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<DenominationModelEntity> query = em.createQuery(
			        "SELECT o FROM DenominationModelEntity o", DenominationModelEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<DenominationModelEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<DenominationModelEntity> query = em.createQuery(
			        "SELECT o FROM DenominationModelEntity o WHERE o.realm.id = ?1", DenominationModelEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
