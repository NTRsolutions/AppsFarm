package is.ejb.dl.dao;

import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.conversionHistory.SerDeConversionHistory;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class DAOConversionHistory {

   @Inject
   private EntityManager em;

   @Inject
   private SerDeConversionHistory serDeConverisonHistory;
   
   @Inject
   private Logger log;

   public void create(ConversionHistoryEntity entity) {
	   em.persist(entity);
	   em.flush(); //enforce immediate db write
   }

   public ConversionHistoryEntity createOrUpdate(ConversionHistoryEntity entity) throws IOException {
	   //entity = serializeData(entity);
	   //return em.merge(entity);
	   
	   entity = serializeData(entity);
	   ConversionHistoryEntity returnedEntity = em.merge(entity);
	   em.flush(); //enforce immediate db write
	   log.info("Create or update in conversion history.");
	   return returnedEntity;
   }

   public void delete(ConversionHistoryEntity entity) {
	   entity = em.merge(entity);
	   //em.remove(entity);
	   em.createQuery("DELETE FROM ConversionHistoryEntity e WHERE e.id = :id")
       .setParameter("id", entity.getId())
       .executeUpdate();
   }

   public ConversionHistoryEntity findByUserId(Integer id) throws Exception {
	   try
	   {
		   ConversionHistoryEntity entity = null;
		   TypedQuery<ConversionHistoryEntity> query = em.createQuery(
			        "SELECT o FROM ConversionHistoryEntity o WHERE o.userId = ?1", ConversionHistoryEntity.class);

		   query.setParameter(1, id);

		   List<ConversionHistoryEntity> list = query.getResultList();
		   if(list.size() == 0) {
			   return null;
		   } else {
			   entity = list.get(0);
		   }
		   
		   entity = deserializeData(entity);
		   return entity;   
		   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public ConversionHistoryEntity findByUserId(String userId) throws Exception {
	   try
	   {
		   TypedQuery<ConversionHistoryEntity> query = em.createQuery(
			        "SELECT o FROM ConversionHistoryEntity o WHERE o.userId = ?1", ConversionHistoryEntity.class);

		   query.setParameter(1, userId);

		   ConversionHistoryEntity entity = query.getSingleResult();
		   entity = deserializeData(entity);
		   return entity;   
		   
	   } catch(NoResultException e) {
	        return null;
	   } 
   }
      
   private ConversionHistoryEntity deserializeData(ConversionHistoryEntity entity) throws IOException {
	   //deserialize conversion history object
	   if(entity.getConversionHistory()!=null && entity.getConversionHistory().length()>0) {
		   entity.setConversionHistoryHolder(serDeConverisonHistory.deserialize(entity.getConversionHistory()));
	   }
	   //fill it in the entity object
	   return entity;
   }

   private ConversionHistoryEntity serializeData(ConversionHistoryEntity entity) throws IOException {
	   //deserialize conversion history object
	   entity.setConversionHistory(serDeConverisonHistory.serialize(entity.getConversionHistoryHolder()));
	   //fill it in the entity object
	   return entity;
   }

   public ConversionHistoryEntry getConversionEntryToUpdate(UserEventEntity event, 
		   ConversionHistoryEntity conversionHistory) throws IOException {
	   ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
	   ArrayList<ConversionHistoryEntry> listConversions = conversionHistoryHolder.getListConversionHistoryEntries();
	   for(int i=0;i<listConversions.size();i++) {
		   ConversionHistoryEntry conversionEntry = listConversions.get(i);
		   if(conversionEntry.getOfferId().equals(event.getOfferId())){ //update identified entry with conversion date
			   return conversionEntry;
		   }
	   }
	   return null;
   }
      
}
