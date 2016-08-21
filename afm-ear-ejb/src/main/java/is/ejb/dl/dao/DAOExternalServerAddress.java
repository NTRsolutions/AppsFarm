package is.ejb.dl.dao;

import is.ejb.bl.external.ExternalServerType;
import is.ejb.dl.entities.ExternalServerAddressEntity;
import is.ejb.dl.entities.UserEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOExternalServerAddress {

	  @Inject
	   private EntityManager em;
	 

	   public void create(ExternalServerAddressEntity entity) {
		   em.persist(entity);
	   }

	   public ExternalServerAddressEntity createOrUpdate(ExternalServerAddressEntity entity) {
		   return em.merge(entity);
	   }

	   public void delete(ExternalServerAddressEntity entity) {
		   entity = em.merge(entity);
		   em.createQuery("DELETE FROM ExternalServerAddressEntity e WHERE e.id = :id")
	       .setParameter("id", entity.getId())
	       .executeUpdate();
	   }

	   public ExternalServerAddressEntity findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<ExternalServerAddressEntity> query = em.createQuery(
				        "SELECT o FROM ExternalServerAddressEntity o WHERE o.id = ?1", ExternalServerAddressEntity.class);
			   query.setParameter(1, id);
			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }
	   
	   public ExternalServerAddressEntity findByIp(String ip) throws Exception {
		   try
		   {
			   TypedQuery<ExternalServerAddressEntity> query = em.createQuery(
				        "SELECT o FROM ExternalServerAddressEntity o WHERE o.ip = ?1", ExternalServerAddressEntity.class);

			   query.setParameter(1, ip);
			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }
	   
	   
	   public List<ExternalServerAddressEntity> findAll() throws Exception {
		   try
		   {
			   TypedQuery<ExternalServerAddressEntity> query = em.createQuery(
				        "SELECT o FROM ExternalServerAddressEntity o", ExternalServerAddressEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }
	   
	   public ExternalServerAddressEntity findByExternalServerType(ExternalServerType type) throws Exception {
		   try
		   {
			   TypedQuery<ExternalServerAddressEntity> query = em.createQuery(
				        "SELECT o FROM ExternalServerAddressEntity o WHERE o.externalServerType = ?1", ExternalServerAddressEntity.class);

			   query.setParameter(1, type.toString());
			   return query.getSingleResult();
			   
		   } catch(NoResultException e) {
			   throw new Exception(e.toString());
		   } catch (Exception e){
			   throw new Exception(e.toString());
		   }
	   }
}
