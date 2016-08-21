package is.ejb.dl.dao;

import is.ejb.dl.entities.AdProviderEntity;

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
public class DAOAdProvider {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(AdProviderEntity entity) {
	   em.persist(entity);
   }

   public AdProviderEntity createOrUpdate(AdProviderEntity entity) {
	   return em.merge(entity);
   }

   public void delete(AdProviderEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public AdProviderEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<AdProviderEntity> query = em.createQuery(
			        "SELECT o FROM AdProviderEntity o WHERE o.id = ?1", AdProviderEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public AdProviderEntity findByName(String name) throws Exception {
	   TypedQuery<AdProviderEntity> query = em.createQuery(
		        "SELECT o FROM AdProviderEntity o WHERE o.name = ?1", AdProviderEntity.class);

	   query.setParameter(1, name);

	   List<AdProviderEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public AdProviderEntity findByCodeName(String codeName) throws Exception {
	   TypedQuery<AdProviderEntity> query = em.createQuery(
		        "SELECT o FROM AdProviderEntity o WHERE o.codeName = ?1", AdProviderEntity.class);

	   query.setParameter(1, codeName);

	   List<AdProviderEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<AdProviderEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<AdProviderEntity> query = em.createQuery(
			        "SELECT o FROM AdProviderEntity o", AdProviderEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<AdProviderEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<AdProviderEntity> query = em.createQuery(
			        "SELECT o FROM AdProviderEntity o WHERE o.realm.id = ?1", AdProviderEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<AdProviderEntity> findAllByRealmId(int realmId, boolean active) throws Exception {
	   try
	   {
		   TypedQuery<AdProviderEntity> query = em.createQuery(
			        "SELECT o FROM AdProviderEntity o WHERE o.realm.id = ?1 AND o.active = ?2", AdProviderEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, active);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
