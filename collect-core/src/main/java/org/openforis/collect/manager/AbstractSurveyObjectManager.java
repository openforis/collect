package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
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
		extends AbstractPersistedObjectManager<T, Integer, D> {

//	private PersistedSurveyObjectCache<T> cache;
	
	@Override
	public T loadById(Integer id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> loadAll() {
		throw new UnsupportedOperationException();
	}
	
	public T loadById(CollectSurvey survey, int id) {
		T obj = dao.loadById(survey, id);
		initializeItem(obj);
		return obj;
	}

	public List<T> loadBySurvey(CollectSurvey survey) {
		List<T> result = dao.loadBySurvey(survey);
		for (T item : result) {
			initializeItem(item);
		}
		return result;
	}
	
	public void deleteBySurvey(CollectSurvey survey) {
		dao.deleteBySurvey(survey);
	}
	
	protected final void initializeItems(Collection<T> items) {
		for (T i : items) {
			initializeItem(i);
		}
	}
	
	protected void initializeItem(T i) {
	}

	@Override
	public T save(T obj, User modifiedByUser) {
		Date now = new Date();
		if (obj.getId() == null) {
			if (obj.getCreationDate() == null) {
				obj.setCreationDate(now);
				obj.setModifiedDate(now);
			}
			dao.insert(obj);
		} else {
			obj.setModifiedDate(now);
			dao.update(obj);
		}
		initializeItem(obj);
		return obj;
	}
	
	@Override
	public void delete(T obj) {
		deleteById(obj.getId());
	}

	@Override
	public void deleteById(Integer id) {
		dao.delete(id);
	}
	
//	private static class PersistedSurveyObjectCache<S extends PersistedSurveyObject> {
//		
//		private Map<Integer, List<S>> typesBySurvey;
//		private Map<Integer, S> typesById;
//		
//		public PersistedSurveyObjectCache() {
//			typesBySurvey = new HashMap<Integer, List<S>>();
//			typesById = new HashMap<Integer, S>();
//		}
//		
//		public void put(S e) {
//			typesById.put(e.getId(), e);
//			Integer surveyId = e.getSurvey().getId();
//			List<S> surveyErrorTypes = typesBySurvey.get(surveyId);
//			if (surveyErrorTypes == null) {
//				surveyErrorTypes = new ArrayList<S>();
//				typesBySurvey.put(surveyId, surveyErrorTypes);
//			}
//			surveyErrorTypes.add(e);
//		}
//		
//		public void update(S t) {
//			remove(t);
//			put(t);
//		}
//		
//		public S get(int id) {
//			return typesById.get(id);
//		}
//		
//		public List<S> getBySurvey(CollectSurvey survey) {
//			return typesBySurvey.get(survey.getId());
//		}
//		
//		public void remove(S e) {
//			typesById.remove(e.getId());
//			List<S> list = typesBySurvey.get(e.getSurvey().getId());
//			list.remove(e);
//		}
//	}

}
