package is.ejb.dl.dao;

import is.ejb.dl.entities.OfferFilterEntity;

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
public class DAOOfferFilter {
	
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(OfferFilterEntity entity) {
	   em.persist(entity);
   }

   public OfferFilterEntity createOrUpdate(OfferFilterEntity entity) {
	   return em.merge(entity);
   }

   public void delete(OfferFilterEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public OfferFilterEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<OfferFilterEntity> query = em.createQuery(
			        "SELECT o FROM OfferFilterEntity o WHERE o.id = ?1", OfferFilterEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public List<OfferFilterEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<OfferFilterEntity> query = em.createQuery(
			        "SELECT o FROM OfferFilterEntity o", OfferFilterEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
		   return null;
	   } 
   }

   public OfferFilterEntity findByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<OfferFilterEntity> query = em.createQuery(
			        "SELECT o FROM OfferFilterEntity o WHERE o.realm.id = ?1", OfferFilterEntity.class);

		   query.setParameter(1, realmId);
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }


}
