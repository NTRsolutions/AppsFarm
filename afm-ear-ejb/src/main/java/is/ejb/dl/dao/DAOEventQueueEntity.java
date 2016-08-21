package is.ejb.dl.dao;

import is.ejb.dl.entities.EventQueueEntity;
import is.ejb.dl.entities.UserEventEntity;

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
public class DAOEventQueueEntity {

   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;

   public void create(EventQueueEntity entity) {
	   em.persist(entity);
   }

   public EventQueueEntity createOrUpdate(EventQueueEntity entity) {
	   //return em.merge(entity);
	   
	   EventQueueEntity eventQueueEntity  = em.merge(entity);
	   em.flush();
	   
	   return eventQueueEntity;
   }

   public void delete(EventQueueEntity entity) {
	   entity = em.merge(entity);
	   //em.remove(entity);
	   em.createQuery("DELETE FROM EventQueueEntity e WHERE e.id = :id")
       .setParameter("id", entity.getId())
       .executeUpdate();
   }

   public EventQueueEntity findById(Integer id) throws Exception {
	   try
	   {
		   TypedQuery<EventQueueEntity> query = em.createQuery(
			        "SELECT o FROM EventQueueEntity o WHERE o.id = ?1", EventQueueEntity.class);

		   query.setParameter(1, id);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public EventQueueEntity findByEventId(Integer eventId) throws Exception {
	   try
	   {
		   TypedQuery<EventQueueEntity> query = em.createQuery(
			        "SELECT o FROM EventQueueEntity o WHERE o.eventId = ?1", EventQueueEntity.class);

		   query.setParameter(1, eventId);
		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   /*
   public EventQueueEntity findByEventId(Integer eventId) throws Exception {
	   try
	   {
		   //there may be few events with the same id because they could be queued manually
		   TypedQuery<EventQueueEntity> query = em.createQuery(
			        "SELECT o FROM EventQueueEntity o WHERE o.eventId = ?1", EventQueueEntity.class);

		   query.setParameter(1, eventId);

		   List<EventQueueEntity> list = query.getResultList();
		   if(list.size() == 0)
			   return null;
		   else
			   return list.get(0);
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }
    */
   
   public EventQueueEntity getNextEventByUserId(Integer userId) throws Exception {
	   try
	   {
		   TypedQuery<EventQueueEntity> query = em.createQuery(
			        "SELECT o FROM EventQueueEntity o WHERE o.userId = ?1 ORDER BY o.generationDate asc", EventQueueEntity.class);

		   query.setMaxResults(1);
		   query.setParameter(1, userId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public EventQueueEntity getNextEventByPhoneDetails(String phoneNumberExt, String phoneNumber) throws Exception {
	   try
	   {
		   TypedQuery<EventQueueEntity> query = em.createQuery(
			        "SELECT o FROM EventQueueEntity o WHERE o.phoneNumberExtension = ?1 AND o.phoneNumber = ?2 ORDER BY o.generationDate desc", EventQueueEntity.class);

		   query.setMaxResults(1);
		   query.setParameter(1, phoneNumberExt);
		   query.setParameter(2, phoneNumber);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<EventQueueEntity> findAllByUserId(Integer userId) throws Exception {
	   TypedQuery<EventQueueEntity> query = em.createQuery(
		        "SELECT o FROM EventQueueEntity o WHERE o.userId = ?1", EventQueueEntity.class);

	   query.setParameter(1, userId);

	   List<EventQueueEntity> list = query.getResultList();
	   if(list.size() == 0)
		   return null;
	   else
		   return list;
   }

}
