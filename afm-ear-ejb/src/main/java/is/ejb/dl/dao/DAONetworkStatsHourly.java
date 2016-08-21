package is.ejb.dl.dao;

import is.ejb.dl.entities.NetworkStatsHourlyEntity;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

@Stateless
public class DAONetworkStatsHourly {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(NetworkStatsHourlyEntity entity) {
	   em.persist(entity);
   }

   public NetworkStatsHourlyEntity createOrUpdate(NetworkStatsHourlyEntity entity) {
	   return em.merge(entity);
   }

   public void delete(NetworkStatsHourlyEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public NetworkStatsHourlyEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.id = ?1", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public NetworkStatsHourlyEntity findByUserId(String userId) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.userId = ?1", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, userId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public NetworkStatsHourlyEntity findByOfferId(String offerId) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.offerId = ?1", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, offerId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public NetworkStatsHourlyEntity findByUserIdAndOfferId(String userId, String offerId) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.userId = ?1 AND o.offerId=?2", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, userId);
		   query.setParameter(2, offerId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public NetworkStatsHourlyEntity findByUserIdAndPhoneNumberAndOfferIdAndAdProviderCodeName(String userId, 
		   String phoneNumber, 
		   String offerId, 
		   String adProviderCodeName) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE "
			        + "o.userId = ?1 AND "
			        + "o.phoneNumber=?2 AND "
			        + "o.offerId=?3 AND "
			        + "o.adProviderCodeName=?4", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, userId);
		   query.setParameter(2, phoneNumber);
		   query.setParameter(3, offerId);
		   query.setParameter(4, adProviderCodeName);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	       return null;
	   } 
   }

   public NetworkStatsHourlyEntity findByInternalTransactionId(String internalTransactionId) throws Exception {
	   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
		        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.internalTransactionId = ?1", NetworkStatsHourlyEntity.class);

	   query.setParameter(1, internalTransactionId);
	   List<NetworkStatsHourlyEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public NetworkStatsHourlyEntity findByPhone(String phoneNumber) throws Exception {
	   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
		        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.phoneNumber = ?1", NetworkStatsHourlyEntity.class);

	   query.setParameter(1, phoneNumber);

	   List<NetworkStatsHourlyEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public NetworkStatsHourlyEntity findByProviderCodeName(String providerCodeName) throws Exception {
	   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
		        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.adProviderCodeName = ?1", NetworkStatsHourlyEntity.class);

	   query.setParameter(1, providerCodeName);

	   List<NetworkStatsHourlyEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public NetworkStatsHourlyEntity getLastEntry() throws Exception {
	   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
		        "SELECT o FROM NetworkStatsHourlyEntity o order by o.id desc", NetworkStatsHourlyEntity.class);

	   List<NetworkStatsHourlyEntity> list = query.setMaxResults(1).getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<NetworkStatsHourlyEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o", NetworkStatsHourlyEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<NetworkStatsHourlyEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.realmId = ?1", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<NetworkStatsHourlyEntity> findAllByRealmIdAndCodeName(int realmId, String providerCodeName) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.realmId = ?1 AND o.providerCodeName=?2", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, providerCodeName);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<NetworkStatsHourlyEntity> findAllByRealmIdAndTimeRange(int realmId, Timestamp startDate, Timestamp endDate) throws Exception {
	   try
	   {
		   TypedQuery<NetworkStatsHourlyEntity> query = em.createQuery(
			        "SELECT o FROM NetworkStatsHourlyEntity o WHERE o.realmId = ?1 "
			        + "AND o.generationEndDate > ?2 AND o.generationEndDate < ?3", NetworkStatsHourlyEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, startDate);
		   query.setParameter(3, endDate);
		   return query.getResultList();
	   } catch(NoResultException e) {
		   return new ArrayList<NetworkStatsHourlyEntity>();
	   } 
   }

   public int countTotal(int realmId) {
       Query query = em.createQuery("select COUNT(p) from NetworkStatsHourlyEntity p WHERE p.realmId = ?1");
       query.setParameter(1, realmId);
       Number result = (Number) query.getSingleResult();
       return result.intValue();
   }

   
   //remove history data based on provided threshold date
   public int deleteHistoryData(Timestamp timestamp) {
	   Query query = em.createQuery(
			      "DELETE FROM NetworkStatsHourlyEntity u WHERE u.clickDate < ?1");
	   query.setParameter(1, timestamp);
	   int deletedCount = query.executeUpdate();
       return deletedCount;
   }

}
