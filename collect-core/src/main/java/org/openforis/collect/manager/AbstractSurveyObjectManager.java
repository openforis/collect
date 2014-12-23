package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public abstract class AbstractSurveyObjectManager
		<T extends PersistedSurveyObject, D extends SurveyObjectMappingJooqDaoSupport<T, ?>> 
		extends AbstractPersistedObjectManager<T, D> {

	protected D dao;
	
	@Override
	public T loadById(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> loadAll() {
		throw new UnsupportedOperationException();
	}
	
	public T loadById(CollectSurvey survey, int id) {
		T obj = dao.loadById(survey, id);
		return obj;
	}

	public List<T> loadBySurvey(CollectSurvey survey) {
		List<T> result = dao.loadBySurvey(survey);
		return result;
	}
	
	@Override
	public void save(T obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		} else {
			dao.update(obj);
		}
	}
	
	@Override
	public void delete(T obj) {
		delete(obj.getId());
	}

	@Override
	public void delete(int id) {
		dao.delete(id);
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
