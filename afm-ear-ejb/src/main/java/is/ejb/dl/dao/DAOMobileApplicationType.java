package is.ejb.dl.dao;

import is.ejb.dl.entities.MobileApplicationTypeEntity;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOMobileApplicationType {

	   @Inject
	   private EntityManager em;
	   
	  

	   public void create(MobileApplicationTypeEntity entity) {
		   em.persist(entity);
	   }

	   public MobileApplicationTypeEntity createOrUpdate(MobileApplicationTypeEntity entity) {
		   return em.merge(entity);
	   }

	   public void delete(MobileApplicationTypeEntity entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public MobileApplicationTypeEntity findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<MobileApplicationTypeEntity> query = em.createQuery(
				        "SELECT o FROM MobileApplicationTypeEntity o WHERE o.id = ?1", MobileApplicationTypeEntity.class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   public List<MobileApplicationTypeEntity> findAll() throws Exception {
		   try
		   {
			   TypedQuery<MobileApplicationTypeEntity> query = em.createQuery(
				        "SELECT o FROM MobileApplicationTypeEntity o", MobileApplicationTypeEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

	   public List<MobileApplicationTypeEntity> findByRealmId(int realmId) throws Exception {
		   try
		   {
			   TypedQuery<MobileApplicationTypeEntity> query = em.createQuery(
				        "SELECT o FROM MobileApplicationTypeEntity o WHERE o.realmId = ?1", MobileApplicationTypeEntity.class);

			   query.setParameter(1, realmId);
			   return query.getResultList();
		   } catch(NoResultException e) {
		        return null;
		   } 
	   }

	   
	   public MobileApplicationTypeEntity findByName(String name) throws Exception {
		   try
		   {
			   TypedQuery<MobileApplicationTypeEntity> query = em.createQuery(
				        "SELECT o FROM MobileApplicationTypeEntity o WHERE o.name = ?1", MobileApplicationTypeEntity.class);

			   query.setParameter(1, name);
			   return query.getSingleResult();
		   } catch(NoResultException e) {
		        return null;
		   } 
	   }
}
