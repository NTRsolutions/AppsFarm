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

import is.ejb.dl.entities.LicenseEntity;

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
public class DAOLicense {

   @Inject
   private EntityManager em;

   @Inject
   private Logger log;

   public LicenseEntity createOrUpdate(LicenseEntity entity) {
	   return em.merge(entity);
   }

   public void delete(LicenseEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public LicenseEntity findById(int id) throws Exception {
	   try
	   {
		   TypedQuery<LicenseEntity> query = em.createQuery(
			        "SELECT o FROM LicenseEntity o WHERE o.id = ?1", LicenseEntity.class);

		   query.setParameter(1, id);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public LicenseEntity findByType(String type) throws Exception {
	   try
	   {
		   TypedQuery<LicenseEntity> query = em.createQuery(
			        "SELECT o FROM LicenseEntity o WHERE o.type = ?1", LicenseEntity.class);

		   query.setParameter(1, type);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
		   e.printStackTrace();
		   return null;
	   } 
   }

   public LicenseEntity findByTypeAndRealmId(String type, int realmId) throws Exception {
	   try
	   {
		   TypedQuery<LicenseEntity> query = em.createQuery(
			        "SELECT o FROM LicenseEntity o WHERE o.type = ?1 AND o.realmId = ?2", LicenseEntity.class);

		   query.setParameter(1, type);
		   query.setParameter(2, realmId);
		   
		   return query.getSingleResult();
	   } catch(NoResultException e) {
		   e.printStackTrace();
		   return null;
	   } 
   }

   public List<LicenseEntity> findAll() throws Exception {
	   try
	   {
		   TypedQuery<LicenseEntity> query = em.createQuery(
			        "SELECT o FROM LicenseEntity o", LicenseEntity.class);

		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

}
