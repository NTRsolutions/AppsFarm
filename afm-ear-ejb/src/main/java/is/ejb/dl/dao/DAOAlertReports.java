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

import is.ejb.dl.entities.AlertReportEntity;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


@Stateless
public class DAOAlertReports {

   @Inject
   private EntityManager em;

   @Inject
   private Logger log;

   public void create(AlertReportEntity entity) {
	   em.persist(entity);
   }

   public AlertReportEntity createOrUpdate(AlertReportEntity entity) {
	   return em.merge(entity);
   }

   public void delete(AlertReportEntity entity) {
	   entity = em.merge(entity);
	   em.remove(entity);
   }
   
   
   public AlertReportEntity findId(Integer id, Integer realmId) throws Exception {
	   try
	   {
		   TypedQuery<AlertReportEntity> query = em.createQuery(
			        "SELECT o FROM AlertReportEntity o WHERE o.id = ?1 AND o.realmId = ?2", AlertReportEntity.class);

		   query.setParameter(1, id);
		   query.setParameter(2, realmId);

		   return query.getSingleResult();   
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

   public List<AlertReportEntity> findAllByRealmId(int realmId) throws Exception {
	   try
	   {
		   TypedQuery<AlertReportEntity> query = em.createQuery(
			        "SELECT o FROM AlertReportEntity o WHERE o.realmId= ?1", AlertReportEntity.class);
		   query.setParameter(1, realmId);
		   
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
   
   public List<AlertReportEntity> findAllByRealmId(int firstResultCount, int pageSize, int realmId) throws Exception {
	   try
	   {
		   TypedQuery<AlertReportEntity> query = em.createQuery("SELECT o FROM AlertReportEntity o WHERE o.realmId= ?1", AlertReportEntity.class);
		   query.setParameter(1, realmId);
		   //for pagination
		   query.setFirstResult(firstResultCount);
		   query.setMaxResults(pageSize);
		   return query.getResultList();
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }
   
   //http://javaeeinsights.wordpress.com/2011/04/07/primefaces-datatable-lazyloading-using-hibernate-criteria-api/
   public List<AlertReportEntity> findAllWithSorting(int first, int pageSize, String sortField, String sortOrder, Map filters, int realmId) throws Exception {
	   try
	   {
		   List data = new ArrayList();
		   
		   // Criteria
		   CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		   CriteriaQuery accountQuery = criteriaBuilder.createQuery(AlertReportEntity.class);
		   // From
		   Root from = accountQuery.from(AlertReportEntity.class);

  		   //sort
		   if(sortField != null) {
			   if (sortOrder.equals("ascending")) {
				   accountQuery.orderBy(criteriaBuilder.asc(from.get(sortField)));
			   } else {
				   accountQuery.orderBy(criteriaBuilder.desc(from.get(sortField)));
			   }
		   } else { //by default always sort by date in desc order
			   accountQuery.orderBy(criteriaBuilder.desc(from.get("date")));
		   }
		   
  		   //filters
		   List predicates = new ArrayList();
		   for(Iterator it = filters.keySet().iterator(); it.hasNext();) {
			   String filterProperty = (String)it.next(); // table column name = field name
			   String filterValue = (String)filters.get(filterProperty);
	
			   //Expression literal = criteriaBuilder.literal((String)filterValue);
			   Expression literal = criteriaBuilder.literal((String)(filterValue+"%"));
			   System.out.println("filterProperty: "+filterProperty+" filterValue: "+filterValue+" literal: "+literal);
			   predicates.add(criteriaBuilder.like(from.get(filterProperty), literal));
		   }
		   //only for given realm
		   predicates.add(criteriaBuilder.equal(from.get("realmId"), realmId));

		   accountQuery.where((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));

		   // paginate
		   data = em.createQuery(accountQuery).setFirstResult(first).setMaxResults(pageSize).getResultList();
		
		   return data;
		   
	   } catch(NoResultException e) {
	        throw new Exception(e.toString());
	   } 
   }

  public int countTotal(int realmId) {
       Query query = em.createQuery("select COUNT(p) from AlertReportEntity p WHERE p.realmId = ?1");
       query.setParameter(1, realmId);
       Number result = (Number) query.getSingleResult();
       return result.intValue();
   }
   
}
