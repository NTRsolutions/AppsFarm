package is.ejb.dl.dao;

import is.ejb.dl.entities.OfferEntity;

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
public class DAOOffer {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(OfferEntity entity) {
	   em.persist(entity);
   }

   public OfferEntity createOrUpdate(OfferEntity entity) {
	   return em.merge(entity);
   }

   public void delete(OfferEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public OfferEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<OfferEntity> query = em.createQuery(
			        "SELECT o FROM OfferEntity o WHERE o.id = ?1", OfferEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public OfferEntity findByOfferId(String offerId) throws Exception {
	   try
	   {
		   TypedQuery<OfferEntity> query = em.createQuery(
			        "SELECT o FROM OfferEntity o WHERE o.offerId = ?1", OfferEntity.class);

		   query.setParameter(1, offerId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public OfferEntity findByName(String name) throws Exception {
	   TypedQuery<OfferEntity> query = em.createQuery(
		        "SELECT o FROM OfferEntity o WHERE o.name = ?1", OfferEntity.class);

	   query.setParameter(1, name);

	   List<OfferEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public OfferEntity findByProviderCodeName(String providerCodeName) throws Exception {
	   TypedQuery<OfferEntity> query = em.createQuery(
		        "SELECT o FROM OfferEntity o WHERE o.adProviderCodeName = ?1", OfferEntity.class);

	   query.setParameter(1, providerCodeName);

	   List<OfferEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<OfferEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<OfferEntity> query = em.createQuery(
			        "SELECT o FROM OfferEntity o", OfferEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<OfferEntity> query = em.createQuery(
			        "SELECT o FROM OfferEntity o WHERE o.realm.id = ?1", OfferEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferEntity> findAllByRealmIdAndCodeName(int realmId, String providerCodeName) throws Exception {
	   try
	   {
		   TypedQuery<OfferEntity> query = em.createQuery(
			        "SELECT o FROM OfferEntity o WHERE o.realm.id = ?1 AND o.providerCodeName=?2", OfferEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, providerCodeName);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   //remove history data based on provided threshold date
   public int deleteHistoryData(Timestamp timestamp) {
	   Query query = em.createQuery(
			      "DELETE FROM OfferEntity o WHERE o.generationDate < ?1");
	   query.setParameter(1, timestamp);
	   int deletedCount = query.executeUpdate();
       return deletedCount;
   }

}
