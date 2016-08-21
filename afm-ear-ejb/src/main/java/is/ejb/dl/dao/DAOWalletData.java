package is.ejb.dl.dao;

import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
public class DAOWalletData {
	 @Inject
	   private EntityManager em;
	   
	   @Inject
	   private Logger log;

	   public void create(WalletDataEntity entity) {
		   em.persist(entity);
		   em.flush(); //enable this to immediately update db
	   }

	   public WalletDataEntity createOrUpdate(WalletDataEntity entity) {
		    //return em.merge(entity);
		    
		    WalletDataEntity walletDataEntity = em.merge(entity);
		    em.flush();
		    return walletDataEntity;
	   }

	   public void delete(WalletDataEntity entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public WalletDataEntity findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<WalletDataEntity> query = em.createQuery(
				        "SELECT o FROM WalletDataEntity o WHERE o.id = ?1", WalletDataEntity.class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }
	   
	   public WalletDataEntity findByUserId(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<WalletDataEntity> query = em.createQuery(
				        "SELECT o FROM WalletDataEntity o WHERE o.userId = ?1", WalletDataEntity.class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
		        return null;
		   } 
	   }

	   /*
	   public WalletDataEntity findByUserId(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<WalletDataEntity> query = em.createQuery(
				        "SELECT o FROM WalletDataEntity o WHERE o.userId = ?1", WalletDataEntity.class);

			   query.setParameter(1, id);

			   List<WalletDataEntity> listWallet = query.getResultList();
			   if(listWallet.size()==0) {
				   return null;
			   } else {
				   return listWallet.get(0);
			   }
		   } catch(Exception e) {
		        return null;
		   } 
	   }
	    */
	   
	   public List<WalletDataEntity> findAll() throws Exception {
		   try
		   {
			   TypedQuery<WalletDataEntity> query = em.createQuery(
				        "SELECT o FROM WalletDataEntity o", WalletDataEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
			   return null;
		   } 
	   }

}
