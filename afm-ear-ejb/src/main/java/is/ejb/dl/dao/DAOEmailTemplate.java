package is.ejb.dl.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import is.ejb.dl.entities.EmailTemplateEntity;

@Stateless
public class DAOEmailTemplate {
	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public void create(EmailTemplateEntity entity) {
		em.persist(entity);
	}

	public EmailTemplateEntity createOrUpdate(EmailTemplateEntity entity) {
		return em.merge(entity);
	}

	public void delete(EmailTemplateEntity entity) {
		entity = em.merge(entity);
		em.remove(entity);
	}

	public EmailTemplateEntity findById(Integer id) throws Exception {
		try {
			TypedQuery<EmailTemplateEntity> query = em.createQuery("SELECT o FROM EmailTemplateEntity o WHERE o.id = ?1",
					EmailTemplateEntity.class);

			query.setParameter(1, id);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<EmailTemplateEntity> findAll() throws Exception {
		try {
			TypedQuery<EmailTemplateEntity> query = em.createQuery("SELECT o FROM EmailTemplateEntity o",
					EmailTemplateEntity.class);

			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
}
