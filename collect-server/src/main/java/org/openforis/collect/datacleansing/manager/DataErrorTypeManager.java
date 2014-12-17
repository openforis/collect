/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.persistence.DataErrorTypeDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataErrorTypeManager extends AbstractSurveyObjectManager<DataErrorType, DataErrorTypeDao> {
	
	private ErrorTypeCache cache;
	
	public DataErrorTypeManager() {
		cache = new ErrorTypeCache();
	}
	
	@Autowired
	@Override
	public void setDao(DataErrorTypeDao dao) {
		super.setDao(dao);
	}
	
	@Override
	public List<DataErrorType> loadBySurvey(CollectSurvey survey) {
		List<DataErrorType> types = cache.getBySurvey(survey);
		if (types == null) {
			types = super.loadBySurvey(survey);
			if (types != null) {
				for (DataErrorType t : types) {
					cache.put(t);
				}
			}
		}
		return types;
	}
	
	@Override
	public DataErrorType loadById(CollectSurvey survey, int id) {
		DataErrorType e = cache.get(id);
		if (e == null) {
			e = super.loadById(survey, id);
			if (e != null) {
				cache.put(e);
			}
		}
		return e;
	}

	@Override
	public void save(DataErrorType obj) {
		boolean newItem = obj.getId() == null;
		super.save(obj);
		if (newItem) {
			cache.put(obj);
		} else {
			cache.update(obj);
		}
	}
	
	@Override
	public void delete(int id) {
		DataErrorType errorType = cache.get(id);
		super.delete(errorType.getId());
		cache.remove(errorType);
	} 
	
	private static class ErrorTypeCache {
		
		private Map<Integer, List<DataErrorType>> typesBySurvey;
		private Map<Integer, DataErrorType> typesById;
		
		public ErrorTypeCache() {
			typesBySurvey = new HashMap<Integer, List<DataErrorType>>();
			typesById = new HashMap<Integer, DataErrorType>();
		}
		
		public void put(DataErrorType e) {
			typesById.put(e.getId(), e);
			Integer surveyId = e.getSurvey().getId();
			List<DataErrorType> surveyErrorTypes = typesBySurvey.get(surveyId);
			if (surveyErrorTypes == null) {
				surveyErrorTypes = new ArrayList<DataErrorType>();
				typesBySurvey.put(surveyId, surveyErrorTypes);
			}
			surveyErrorTypes.add(e);
		}
		
		public void update(DataErrorType t) {
			remove(t);
			put(t);
		}
		
		public DataErrorType get(int id) {
			return typesById.get(id);
		}
		
		public List<DataErrorType> getBySurvey(CollectSurvey survey) {
			return typesBySurvey.get(survey.getId());
		}
		
		public void remove(DataErrorType e) {
			typesById.remove(e.getId());
			List<DataErrorType> list = typesBySurvey.get(e.getSurvey().getId());
			list.remove(e);
		}
	}

}
