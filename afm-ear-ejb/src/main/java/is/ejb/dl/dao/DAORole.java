package is.ejb.dl.dao;

import is.ejb.dl.entities.RoleEntity;

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
public class DAORole {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(RoleEntity entity) {
	   em.persist(entity);
   }

   public RoleEntity createOrUpdate(RoleEntity entity) {
	   return em.merge(entity);
   }

   public void delete(RoleEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public RoleEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<RoleEntity> query = em.createQuery(
			        "SELECT o FROM RoleEntity o WHERE o.id = ?1", RoleEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public RoleEntity findByName(String name) throws Exception {
	   TypedQuery<RoleEntity> query = em.createQuery(
		        "SELECT o FROM RoleEntity o WHERE o.name = ?1", RoleEntity.class);

	   query.setParameter(1, name);

	   List<RoleEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<RoleEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<RoleEntity> query = em.createQuery(
			        "SELECT o FROM RoleEntity o", RoleEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
