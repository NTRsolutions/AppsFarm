package is.ejb.dl.dao;

import is.ejb.dl.entities.WalletPayoutOfferTransactionEntity;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
@Stateless
public class DAOWalletPayoutOfferTransaction {

	 @Inject
	   private EntityManager em;
	   
	   @Inject
	   private Logger log;

	   public void create(WalletPayoutOfferTransactionEntity entity) {
		   em.persist(entity);
	   }

	   public WalletPayoutOfferTransactionEntity createOrUpdate(WalletPayoutOfferTransactionEntity entity) {
		    return em.merge(entity);
	   }

	   public void delete(WalletPayoutOfferTransactionEntity entity) {
		   entity = em.merge(entity);
		   em.remove(entity);
	   }

	   public WalletPayoutOfferTransactionEntity findById(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<WalletPayoutOfferTransactionEntity> query = em.createQuery(
				        "SELECT o FROM WalletPayoutOfferTransactionEntity o WHERE o.id = ?1", WalletPayoutOfferTransactionEntity.class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }
	   
	   public List<WalletPayoutOfferTransactionEntity> findByUserId(Integer id) throws Exception {
		   try
		   {
			   TypedQuery<WalletPayoutOfferTransactionEntity> query = em.createQuery(
				        "SELECT o FROM WalletPayoutOfferTransactionEntity o WHERE o.userId = ?1", WalletPayoutOfferTransactionEntity.class);

			   query.setParameter(1, id);

			   return query.getResultList();
			   
		   } catch(NoResultException e) {
		        return null;
		   } 
	   }
	   
	   
	   public WalletPayoutOfferTransactionEntity findByTicketId(Long id) throws Exception {
		   try
		   {
			   TypedQuery<WalletPayoutOfferTransactionEntity> query = em.createQuery(
				        "SELECT o FROM WalletPayoutOfferTransactionEntity o WHERE o.ticketId = ?1", WalletPayoutOfferTransactionEntity.class);

			   query.setParameter(1, id);

			   return query.getSingleResult();   
			   
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }

	   
	   

	   public List<WalletPayoutOfferTransactionEntity> findAll() throws Exception {
		   try
		   {
			   TypedQuery<WalletPayoutOfferTransactionEntity> query = em.createQuery(
				        "SELECT o FROM WalletPayoutOfferTransactionEntity o", WalletPayoutOfferTransactionEntity.class);

			   return query.getResultList();
		   } catch(NoResultException e) {
		        throw new Exception(e.toString());
		   } 
	   }
}
