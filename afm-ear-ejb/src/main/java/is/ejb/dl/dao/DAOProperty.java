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

import is.ejb.dl.entities.ProfilePropertyEntity;
import is.ejb.dl.entities.PropertyEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


@Stateless
public class DAOProperty {

   @Inject
   private EntityManager em;

   @Inject
   private Logger log;

   public void create(PropertyEntity entity) {
	   em.persist(entity);
   }

   public void update(PropertyEntity entity) {
	   em.merge(entity);
   }

   public void delete(PropertyEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }

   public PropertyEntity findByPK(Integer parentId, Integer type, String name) throws Exception {

	   //mzj proper handling of no result exception
	   try{
		   TypedQuery<PropertyEntity> query = em.createQuery(
			        "SELECT o FROM PropertyEntity o WHERE o.pk.parentId = ?1 AND o.pk.type = ?2 AND o.pk.name = ?3", PropertyEntity.class);

		   query.setParameter(1, parentId);
		   query.setParameter(2, type);
		   query.setParameter(3, name);
		   return query.getSingleResult();   

	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
}
