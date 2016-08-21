package is.ejb.dl.dao;

import is.ejb.dl.entities.SnapdealOffersEntity;

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
public class DAOSnapdealOffers {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(SnapdealOffersEntity entity) {
	   em.persist(entity);
	   em.flush();
   }

   public SnapdealOffersEntity createOrUpdate(SnapdealOffersEntity entity) {
	   return em.merge(entity);
   }

   public void delete(SnapdealOffersEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
	   em.flush();
   }

   public SnapdealOffersEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<SnapdealOffersEntity> query = em.createQuery(
			        "SELECT o FROM SnapdealOffersEntity o WHERE o.id = ?1", SnapdealOffersEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public SnapdealOffersEntity findByCategory(String categoryName) throws Exception {
	   TypedQuery<SnapdealOffersEntity> query = em.createQuery(
		        "SELECT o FROM SnapdealOffersEntity o WHERE o.categoryName = ?1", SnapdealOffersEntity.class);

	   query.setParameter(1, categoryName);

	   List<SnapdealOffersEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   
   
   public int deleteAll() throws Exception {
	   try
	   {
		   int deletedCount = em.createQuery("DELETE FROM SnapdealOffersEntity").executeUpdate();
		   return deletedCount;
	   } catch(Exception e) {
	        throw new Exception(e.toString());
	   } 
   }
   
   public List<SnapdealOffersEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<SnapdealOffersEntity> query = em.createQuery(
			        "SELECT o FROM SnapdealOffersEntity o", SnapdealOffersEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<SnapdealOffersEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<SnapdealOffersEntity> query = em.createQuery(
			        "SELECT o FROM SnapdealOffersEntity o WHERE o.realm.id = ?1", SnapdealOffersEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
