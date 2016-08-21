package is.ejb.dl.dao;

import is.ejb.dl.entities.UserEventFailedEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

@Stateless
public class DAOUserEventFailed {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(UserEventFailedEntity entity) {
	   em.persist(entity);
   }

   public UserEventFailedEntity createOrUpdate(UserEventFailedEntity entity, int step) {
	   //System.out.println("UPDATING !!!!!!!!!!!!!!!!!!!! "+step+" "+entity.getRewardResponseStatusMessage()+" "+entity.getRewardDate());
	   return em.merge(entity);
   }

   public void delete(UserEventFailedEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public UserEventFailedEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.id = ?1", UserEventFailedEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public UserEventFailedEntity findByUserId(String userId) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.userId = ?1", UserEventFailedEntity.class);

		   query.setParameter(1, userId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public UserEventFailedEntity findByOfferId(String offerId) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.offerId = ?1", UserEventFailedEntity.class);

		   query.setParameter(1, offerId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public UserEventFailedEntity findByUserIdAndOfferId(String userId, String offerId) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.userId = ?1 AND o.offerId=?2", UserEventFailedEntity.class);

		   query.setParameter(1, userId);
		   query.setParameter(2, offerId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public UserEventFailedEntity findByUserIdAndPhoneNumberAndOfferIdAndAdProviderCodeName(String userId, 
		   String phoneNumber, 
		   String offerId, 
		   String adProviderCodeName) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE "
			        + "o.userId = ?1 AND "
			        + "o.phoneNumber=?2 AND "
			        + "o.offerId=?3 AND "
			        + "o.adProviderCodeName=?4", UserEventFailedEntity.class);

		   query.setParameter(1, userId);
		   query.setParameter(2, phoneNumber);
		   query.setParameter(3, offerId);
		   query.setParameter(4, adProviderCodeName);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	       return null;
	   } 
   }

   public UserEventFailedEntity findByInternalTransactionId(String internalTransactionId) throws Exception {
	   TypedQuery<UserEventFailedEntity> query = em.createQuery(
		        "SELECT o FROM UserEventFailedEntity o WHERE o.internalTransactionId = ?1", UserEventFailedEntity.class);

	   query.setParameter(1, internalTransactionId);
	   List<UserEventFailedEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public UserEventFailedEntity findByPhone(String phoneNumber) throws Exception {
	   TypedQuery<UserEventFailedEntity> query = em.createQuery(
		        "SELECT o FROM UserEventFailedEntity o WHERE o.phoneNumber = ?1", UserEventFailedEntity.class);

	   query.setParameter(1, phoneNumber);

	   List<UserEventFailedEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public UserEventFailedEntity findByProviderCodeName(String providerCodeName) throws Exception {
	   TypedQuery<UserEventFailedEntity> query = em.createQuery(
		        "SELECT o FROM UserEventFailedEntity o WHERE o.adProviderCodeName = ?1", UserEventFailedEntity.class);

	   query.setParameter(1, providerCodeName);

	   List<UserEventFailedEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list.get(0);
   }

   public List<UserEventFailedEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o", UserEventFailedEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<UserEventFailedEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.realmId = ?1", UserEventFailedEntity.class);

		   query.setParameter(1, realmId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<UserEventFailedEntity> findAllByUserIdAndProviderOfferId(int userId, String providerOfferId) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.userId = ?1 AND o.offerSourceId=?2", UserEventFailedEntity.class);

		   query.setParameter(1, userId);
		   query.setParameter(2, providerOfferId);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<UserEventFailedEntity> findAllByRealmIdAndCodeName(int realmId, String providerCodeName) throws Exception {
	   try
	   {
		   TypedQuery<UserEventFailedEntity> query = em.createQuery(
			        "SELECT o FROM UserEventFailedEntity o WHERE o.realmId = ?1 AND o.providerCodeName=?2", UserEventFailedEntity.class);

		   query.setParameter(1, realmId);
		   query.setParameter(2, providerCodeName);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public int countTotal(int realmId) {
       Query query = em.createQuery("select COUNT(p) from UserEventFailedEntity p WHERE p.realmId = ?1");
       query.setParameter(1, realmId);
       Number result = (Number) query.getSingleResult();
       return result.intValue();
   }
   
   public int countTotal(Timestamp startDate, Timestamp endDate, 
		   Map<String, String> filters, int realmId) throws Exception {
	   int count = 0;
	   
	   final int FIRST = 0;
	   final int PAGE_SIZE = Integer.MAX_VALUE;
	   final String SORT_FIELD = null;
	   final String SORT_ORDER = null;
	   
	   List<UserEventFailedEntity> list = findFiltered(FIRST, PAGE_SIZE, SORT_FIELD, SORT_ORDER, filters, startDate, endDate, realmId);
	   count = list.size();
	   
	   return count;
   }

 //http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-lazyloading-using-hibernate-criteria-api/
   public List<UserEventFailedEntity> findFiltered(int first, int pageSize, 
		   String sortField, String sortOrder, Map<String, String> filters,
		   Timestamp startDate, Timestamp endDate,
		   int realmId) throws Exception {
	   try
	   {
		   List<UserEventFailedEntity> data = new ArrayList<UserEventFailedEntity>();
		   
		   // Criteria
		   CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		   CriteriaQuery<UserEventFailedEntity> accountQuery = criteriaBuilder.createQuery(UserEventFailedEntity.class);
		   // From
		   Root<UserEventFailedEntity> from = accountQuery.from(UserEventFailedEntity.class);

  		   //sort
		   if(sortField != null) {
			   if (sortOrder.equals("ascending")) {
				   accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
			   } else {
				   accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
			   }
		   }
		   
  		   //filters
		   List<Predicate> predicates = new ArrayList<Predicate>();
		   for(Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
			   String filterProperty = (String)it.next(); // table column name = field name
			   String filterValue = (String)filters.get(filterProperty);
	
			   if(isFilterPropertyInteger(filterProperty)){
				   Expression<Integer> literal = criteriaBuilder.literal(new Integer(filterValue));
				   log.info("filterProperty: "+filterProperty+" filterValue: "+filterValue+" literal: "+literal);
				   predicates.add(criteriaBuilder.equal(from.<Integer>get(filterProperty), literal));
			   } else {
				   //Expression literal = criteriaBuilder.literal((String)filterValue);
				   Expression<String> literal = criteriaBuilder.literal((String)(filterValue+"%"));
				   log.info("filterProperty: "+filterProperty+" filterValue: "+filterValue+" literal: "+literal);
				   predicates.add(criteriaBuilder.like(from.<String>get(filterProperty), literal));
			   }
		   }
		   //only for given realm
		   predicates.add(criteriaBuilder.equal(from.get("realmId"), realmId));

		   //only from selected time range
		   predicates.add(criteriaBuilder.between(from.<Date>get("clickDate"), startDate, endDate));
	
		   accountQuery.where((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));

		   // paginate
		   data = em.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize).getResultList();
		
		   return data;
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
   
   //remove history data based on provided threshold date
   public int deleteHistoryData(Timestamp timestamp) {
	   Query query = em.createQuery(
			      "DELETE FROM UserEventFailedEntity u WHERE u.clickDate < ?1");
	   query.setParameter(1, timestamp);
	   int deletedCount = query.executeUpdate();
       return deletedCount;
   }

   
   public int countTotal(Timestamp startDate, Timestamp endDate, int realmId) {
       Query query = em.createQuery("select COUNT(p) from UserEventFailedEntity p WHERE p.realmId = ?1 AND p.clickDate >= ?2 AND p.clickDate < ?3");
       query.setParameter(1, realmId);
       query.setParameter(2, startDate);
       query.setParameter(3, endDate);
       Number result = (Number) query.getSingleResult();
       return result.intValue();
   }

   public double getSumPayout(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
       Query query = em.createQuery("select sum(p.offerPayoutInTargetCurrency) from UserEventFailedEntity p "
       		+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, approved);
       query.setParameter(4, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.doubleValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }

   public double getSumPayoutInOriginalCurrency(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
       Query query = em.createQuery("select sum(p.offerPayout) from UserEventFailedEntity p "
       		+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, approved);
       query.setParameter(4, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.doubleValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }

   public double getSumProfit(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
       Query query = em.createQuery("select sum(p.profitValue) from UserEventFailedEntity p "
       		+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, approved);
       query.setParameter(4, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.doubleValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }

   public double getSumReward(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
       Query query = em.createQuery("select sum(p.rewardValue) from UserEventFailedEntity p "
       		+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, approved);
       query.setParameter(4, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.doubleValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }

   public int getConversionsCount(Timestamp startDate, Timestamp endDate, boolean approved, int realmId) {
       Query query = em.createQuery("select COUNT(p) from UserEventFailedEntity p "
       		+ "WHERE p.rewardDate >= ?1 AND p.rewardDate < ?2 AND p.approved = ?3 AND p.realmId = ?4");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, approved);
       query.setParameter(4, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.intValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }

   public int getClicksCount(Timestamp startDate, Timestamp endDate, int realmId) {
       Query query = em.createQuery("select COUNT(p) from UserEventFailedEntity p "
       		+ "WHERE p.clickDate >= ?1 AND p.clickDate < ?2 AND p.realmId = ?3");
       query.setParameter(1, startDate);
       query.setParameter(2, endDate);
       query.setParameter(3, realmId);
       
       try {
           Number result = (Number) query.getSingleResult();
           return result.intValue();
       } catch(Exception exc) {
    	   //exc.printStackTrace();
    	   return 0;
       }
   }
   
   private boolean isFilterPropertyInteger(String filterProperty){
	   boolean state = false;
	   
	   switch (filterProperty) {
		case "id":
			state = true;
			break;
		case "realmId":
			state = true;
			break;
		case "userId":
			state = true;
			break;
	
		default:
			state = false;
			break;
		}
	   
	   return state;
   }

   /*
   
  public int countTotalActive(int realmId, Timestamp lastContactTimeWindow) {
      Query query = em.createQuery("select COUNT(p) from HostsEntity p WHERE p.lastcontact > ?1 AND p.realmId = ?2");
      query.setParameter(1, lastContactTimeWindow);
      query.setParameter(2, realmId);
      
      Number result = (Number) query.getSingleResult();
      return result.intValue();
  }
   
   public List<HostsEntity> findByPartialSN(Integer hwid, String snprefix) throws Exception {
	   try
	   {
		   TypedQuery<HostsEntity> query = em.createQuery(
			        "SELECT o FROM HostsEntity o WHERE o.hwid = ?1 AND o.serialno like concat(?2, '%')", HostsEntity.class);

		   query.setParameter(1, hwid);
		   query.setParameter(2, snprefix);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
    
   //http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-lazyloading-using-hibernate-criteria-api/
   public List<HostsEntity> findByProfileNameAndMostRecentContact(String profileName, int realmId) throws Exception {
	   try
	   {
		   //lastInform
		   //lastcontact
		   TypedQuery<HostsEntity> query = em.createQuery(
			        "SELECT o FROM HostsEntity o WHERE o.profileName = ?1 AND o.realmId = ?2 ORDER BY o.lastInform DESC", HostsEntity.class);

		   query.setParameter(1, profileName);
		   query.setParameter(2, realmId);
		   query.setMaxResults(10);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

    
   */
}
