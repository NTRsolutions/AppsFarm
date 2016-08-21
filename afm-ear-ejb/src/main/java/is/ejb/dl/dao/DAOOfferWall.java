package is.ejb.dl.dao;

import is.ejb.dl.entities.OfferWallEntity;

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
public class DAOOfferWall {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(OfferWallEntity entity) {
	   em.persist(entity);
   }

   public OfferWallEntity createOrUpdate(OfferWallEntity entity) {
	   return em.merge(entity);
   }

   public void delete(OfferWallEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public OfferWallEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o WHERE o.id = ?1", OfferWallEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public OfferWallEntity findByName(String name) throws Exception {
	   TypedQuery<OfferWallEntity> query = em.createQuery(
		        "SELECT o FROM OfferWallEntity o WHERE o.name = ?1", OfferWallEntity.class);

	   query.setParameter(1, name);

	   List<OfferWallEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public OfferWallEntity findByProviderCodeName(String providerCodeName) throws Exception {
	   TypedQuery<OfferWallEntity> query = em.createQuery(
		        "SELECT o FROM OfferWallEntity o WHERE o.providerCodeName = ?1", OfferWallEntity.class);

	   query.setParameter(1, providerCodeName);

	   List<OfferWallEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<OfferWallEntity> findAllByProviderCodeName(String providerCodeName) throws Exception {
	   TypedQuery<OfferWallEntity> query = em.createQuery(
		        "SELECT o FROM OfferWallEntity o WHERE o.providerCodeName = ?1", OfferWallEntity.class);

	   query.setParameter(1, providerCodeName);

	   List<OfferWallEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list;
   }

   public List<OfferWallEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o", OfferWallEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferWallEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o WHERE o.realm.id = ?1", OfferWallEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferWallEntity> findAllByRealmIdAndActive(int realmId, boolean active) throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o WHERE o.realm.id = ?1 AND o.active = ?2", OfferWallEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, active);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferWallEntity> findAllByRealmIdAndActiveAndCountryAndDevice(int realmId, 
		   boolean active,
		   String supportedCountry,
		   String supportedDevice,
		   String supportedRewardTypeName) throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o WHERE o.realm.id = ?1 AND o.active = ?2"
			        + " AND o.targetCountriesFilter = ?3"
			        + " AND o.targetDevicesFilter = ?4" 
			        + " AND o.rewardTypeName = ?5",			        
			        OfferWallEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, active);
		   query.setParameter(3, supportedCountry);
		   query.setParameter(4, supportedDevice);
		   query.setParameter(5, supportedRewardTypeName);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<OfferWallEntity> findAllByRealmIdAndCodeName(int realmId, String providerCodeName) throws Exception {
	   try
	   {
		   TypedQuery<OfferWallEntity> query = em.createQuery(
			        "SELECT o FROM OfferWallEntity o WHERE o.realm.id = ?1 AND o.providerCodeName=?2", OfferWallEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, providerCodeName);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
