/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package is.ejb.dl.dao;

import is.ejb.dl.entities.DeviceProfileEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

//@ApplicationScoped
@Stateless
public class DAODeviceProfile {
	
   //@Inject
   @PersistenceContext 
   private EntityManager em;

   @Inject
   private Logger log;

   public void createOrUpdate(DeviceProfileEntity entity) {
	   em.merge(entity);
   }

   public void update(DeviceProfileEntity entity) {
	   em.merge(entity);
   }

   public void delete(DeviceProfileEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public DeviceProfileEntity findById(int id) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.id = ?1", DeviceProfileEntity.class);

		   query.setParameter(1, id);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public DeviceProfileEntity findByNameAndRealmId(String name, int realmId) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.name = ?1 AND o.realmId = ?2", DeviceProfileEntity.class);

		   query.setParameter(1, name);
		   query.setParameter(2, realmId);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<DeviceProfileEntity> findByInformSchemaNameAndRealmId(String informSchemaName, int realmId) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.scriptname = ?1 AND o.realmId = ?2", DeviceProfileEntity.class);

		   query.setParameter(1, informSchemaName);
		   query.setParameter(2, realmId);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public DeviceProfileEntity findByName(String name) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.name = ?1", DeviceProfileEntity.class);

		   query.setParameter(1, name);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        return null;
	   } 
   }

   public List<DeviceProfileEntity> findByMonitoringFlag(boolean isMonitoringEnabled) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.enableMonitoring = ?1", DeviceProfileEntity.class);

		   query.setParameter(1, isMonitoringEnabled);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<DeviceProfileEntity> findAll(int realmId) throws Exception {
	   try{
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o WHERE o.realmId = ?1", DeviceProfileEntity.class);

		   query.setParameter(1, realmId);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<DeviceProfileEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<DeviceProfileEntity> query = em.createQuery(
			        "SELECT o FROM DeviceProfileEntity o", DeviceProfileEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
