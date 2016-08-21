package is.ejb.dl.dao;

import is.ejb.dl.entities.BlockedOffersEntity;

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
public class DAOBlockedOffers {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(BlockedOffersEntity entity) {
	   em.persist(entity);
   }

   public BlockedOffersEntity createOrUpdate(BlockedOffersEntity entity) {
	   return em.merge(entity);
   }

   public void delete(BlockedOffersEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public BlockedOffersEntity findByRealmId(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<BlockedOffersEntity> query = em.createQuery(
			        "SELECT o FROM BlockedOffersEntity o WHERE o.realmId = ?1", BlockedOffersEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<BlockedOffersEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<BlockedOffersEntity> query = em.createQuery(
			        "SELECT o FROM BlockedOffersEntity o", BlockedOffersEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }


}
