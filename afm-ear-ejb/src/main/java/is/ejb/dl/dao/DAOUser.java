package is.ejb.dl.dao;

import is.ejb.dl.entities.UserEntity;

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
public class DAOUser {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(UserEntity entity) {
	   em.persist(entity);
   }

   public UserEntity createOrUpdate(UserEntity entity) {
	   return em.merge(entity);
   }

   public void delete(UserEntity entity) {
	   entity = em.merge(entity);
	   //em.remove(entity);
	   em.createQuery("DELETE FROM UserEntity e WHERE e.id = :id")
       .setParameter("id", entity.getId())
       .executeUpdate();
   }

   public UserEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<UserEntity> query = em.createQuery(
			        "SELECT o FROM UserEntity o WHERE o.id = ?1", UserEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public UserEntity findByName(String name) throws Exception {
	   TypedQuery<UserEntity> query = em.createQuery(
		        "SELECT o FROM UserEntity o WHERE o.name = ?1", UserEntity.class);

	   query.setParameter(1, name);

	   List<UserEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public UserEntity findByCredentials(String login) throws Exception {
	   TypedQuery<UserEntity> query = em.createQuery(
		        "SELECT o FROM UserEntity o WHERE o.login = ?1", UserEntity.class);

	   query.setParameter(1, login);

	   List<UserEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public UserEntity findByNameAndCredentials(String name, String login, String password) throws Exception {
	   TypedQuery<UserEntity> query = em.createQuery(
		        "SELECT o FROM UserEntity o WHERE o.name = ?1 AND o.login = ?2 AND o.password = ?3", UserEntity.class);

	   query.setParameter(1, name);
	   query.setParameter(2, login);
	   query.setParameter(3, password);

	   List<UserEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public UserEntity findByLogin(String login) throws Exception {
	   TypedQuery<UserEntity> query = em.createQuery(
		        "SELECT o FROM UserEntity o WHERE o.login = ?1", UserEntity.class);

	   query.setParameter(1, login);

	   List<UserEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<UserEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<UserEntity> query = em.createQuery(
			        "SELECT o FROM UserEntity o", UserEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<UserEntity> findAll(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<UserEntity> query = em.createQuery(
			        "SELECT o FROM UserEntity o WHERE o.realm.id = ?1", UserEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
