package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractSurveyObjectManager<T extends PersistedSurveyObject, D extends SurveyObjectMappingJooqDaoSupport<T, ?>> {

	private D dao;
	
	public T loadById(CollectSurvey survey, int id) {
		T obj = dao.loadById(survey, id);
		return obj;
	}

	public List<T> loadBySurvey(CollectSurvey survey) {
		List<T> result = dao.loadBySurvey(survey);
		return result;
	}
		
	public void save(T obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		} else {
			dao.update(obj);
		}
	}
	
	public void delete(T obj) {
		delete(obj.getId());
	}

	public void delete(int id) {
		dao.delete(id);
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
