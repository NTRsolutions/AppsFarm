package is.ejb.dl.dao;

import is.ejb.dl.entities.RealmEntity;

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
public class DAORealm {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(RealmEntity entity) {
	   em.persist(entity);
   }

   public RealmEntity createOrUpdate(RealmEntity entity) {
	   return em.merge(entity);
   }

   public void delete(RealmEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public RealmEntity findById(Integer id) throws Exception {
	   try
	   {
		   log.info("Selecting realm with id: " + id);
		   TypedQuery<RealmEntity> query = em.createQuery(
			        "SELECT o FROM RealmEntity o WHERE o.id = ?1", RealmEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
		   e.printStackTrace();
		   return null;
	       //throw new Exception(e.toString());
	   } 
   }

   public RealmEntity findByName(String name) throws Exception {
	   TypedQuery<RealmEntity> query = em.createQuery(
		        "SELECT o FROM RealmEntity o WHERE o.name = ?1", RealmEntity.class);

	   query.setParameter(1, name);

	   List<RealmEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public RealmEntity findByApiKey(String key) throws Exception {
	   TypedQuery<RealmEntity> query = em.createQuery(
		        "SELECT o FROM RealmEntity o WHERE o.apiKey = ?1", RealmEntity.class);

	   query.setParameter(1, key);

	   List<RealmEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<RealmEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<RealmEntity> query = em.createQuery(
			        "SELECT o FROM RealmEntity o", RealmEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
