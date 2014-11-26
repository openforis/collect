package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.datacleansing.persistence.DataErrorQueryDao;
import org.openforis.collect.datacleansing.persistence.DataErrorTypeDao;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public class DataCleansingManager {

	@Autowired
	private DataErrorTypeDao errorTypeDao;
	@Autowired
	private DataErrorQueryDao errorQueryDao;
	
	private ErrorTypeCache errorTypeCache;
	
	public DataCleansingManager() {
		errorTypeCache = new ErrorTypeCache();
	}
	
	public List<ErrorType> getErrorTypesBySurvey(CollectSurvey survey) {
		List<ErrorType> types = errorTypeCache.getBySurvey(survey);
		if (types == null) {
			types = errorTypeDao.loadBySurvey(survey);
			if (types != null) {
				for (ErrorType t : types) {
					errorTypeCache.put(t);
				}
			}
		}
		return types;
	}
	
	public ErrorType getErrorTypeById(CollectSurvey survey, int id) {
		ErrorType e = errorTypeCache.get(id);
		if (e == null) {
			e = errorTypeDao.loadById(survey, id);
			if (e != null) {
				errorTypeCache.put(e);
			}
		}
		return e;
	}
	
	public void save(ErrorType errorType) {
		if (errorType.getId() == null) {
			errorTypeDao.insert(errorType);
		} else {
			errorTypeDao.update(errorType);
		}
		errorTypeCache.update(errorType);
	}
	
	public void delete(ErrorType errorType) {
		errorTypeDao.delete(errorType.getId());
		errorTypeCache.remove(errorType);
	}
	
	public List<ErrorQuery> loadErrorQueriesBySurvey(CollectSurvey survey) {
		List<ErrorQuery> queries = errorQueryDao.loadBySurvey(survey);
		if (queries != null) {
			for (ErrorQuery q : queries) {
				initializeErrorType(survey, q);
			}
		}
		return queries;
	}

	public ErrorQuery loadErrorQueryById(CollectSurvey survey, int id) {
		ErrorQuery q = errorQueryDao.loadById(survey, id);
		initializeErrorType(survey, q);
		return q;
	}
	
	public void save(ErrorQuery errorQuery) {
		if (errorQuery.getId() == null) {
			errorQueryDao.insert(errorQuery);
		} else {
			errorQueryDao.update(errorQuery);
		}
	}
	
	public void delete(ErrorQuery errorQuery) {
		errorQueryDao.delete(errorQuery.getId());
	}
	
	private void initializeErrorType(CollectSurvey survey, ErrorQuery q) {
		ErrorType errorType = getErrorTypeById(survey, q.getTypeId());
		q.setType(errorType);
	}
	
	private static class ErrorTypeCache {
		
		private Map<Integer, List<ErrorType>> typesBySurvey;
		private Map<Integer, ErrorType> typesById;
		
		public ErrorTypeCache() {
			typesBySurvey = new HashMap<Integer, List<ErrorType>>();
			typesById = new HashMap<Integer, ErrorType>();
		}
		
		public void put(ErrorType e) {
			typesById.put(e.getId(), e);
			Integer surveyId = e.getSurvey().getId();
			List<ErrorType> surveyErrorTypes = typesBySurvey.get(surveyId);
			if (surveyErrorTypes == null) {
				surveyErrorTypes = new ArrayList<ErrorType>();
				typesBySurvey.put(surveyId, surveyErrorTypes);
			}
			surveyErrorTypes.add(e);
		}
		
		public ErrorType get(int id) {
			return typesById.get(id);
		}
		
		public List<ErrorType> getBySurvey(CollectSurvey survey) {
			return typesBySurvey.get(survey.getId());
		}
		
		public void update(ErrorType e) {
			remove(e);
			put(e);
		}
		
		public void remove(ErrorType e) {
			typesById.remove(e.getId());
			List<ErrorType> list = typesBySurvey.get(e.getSurvey().getId());
			list.remove(e);
		}
	}
	
}
