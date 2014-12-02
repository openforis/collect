package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.datacleansing.persistence.DataErrorQueryDao;
import org.openforis.collect.datacleansing.persistence.DataErrorReportDao;
import org.openforis.collect.datacleansing.persistence.DataErrorReportItemDao;
import org.openforis.collect.datacleansing.persistence.DataErrorTypeDao;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Transactional
public class DataCleansingManager {

	@Autowired
	private DataErrorTypeDao errorTypeDao;
	@Autowired
	private DataErrorQueryDao errorQueryDao;
	@Autowired
	private DataErrorReportDao errorReportDao;
	@Autowired
	private DataErrorReportItemDao errorReportItemDao;
	
	private ErrorTypeCache errorTypeCache;
	
	public DataCleansingManager() {
		errorTypeCache = new ErrorTypeCache();
	}
	
	//Start of DataErrorType methods
	public List<DataErrorType> getErrorTypesBySurvey(CollectSurvey survey) {
		List<DataErrorType> types = errorTypeCache.getBySurvey(survey);
		if (types == null) {
			types = errorTypeDao.loadBySurvey(survey);
			if (types != null) {
				for (DataErrorType t : types) {
					errorTypeCache.put(t);
				}
			}
		}
		return types;
	}
	
	public DataErrorType getErrorTypeById(CollectSurvey survey, int id) {
		DataErrorType e = errorTypeCache.get(id);
		if (e == null) {
			e = errorTypeDao.loadById(survey, id);
			if (e != null) {
				errorTypeCache.put(e);
			}
		}
		return e;
	}
	
	public void save(DataErrorType errorType) {
		if (errorType.getId() == null) {
			errorTypeDao.insert(errorType);
			errorTypeCache.put(errorType);
		} else {
			errorTypeDao.update(errorType);
			errorTypeCache.update(errorType);
		}
	}
	
	public void delete(DataErrorType errorType) {
		errorTypeDao.delete(errorType.getId());
		errorTypeCache.remove(errorType);
	}
	//End of DataErrorType methods
	
	//Start of DataErrorQuery methods
	public List<DataErrorQuery> loadErrorQueriesBySurvey(CollectSurvey survey) {
		List<DataErrorQuery> queries = errorQueryDao.loadBySurvey(survey);
		if (queries != null) {
			for (DataErrorQuery q : queries) {
				initializeErrorType(survey, q);
			}
		}
		return queries;
	}

	public DataErrorQuery loadErrorQueryById(CollectSurvey survey, int id) {
		DataErrorQuery q = errorQueryDao.loadById(survey, id);
		initializeErrorType(survey, q);
		return q;
	}
	
	public void save(DataErrorQuery errorQuery) {
		if (errorQuery.getId() == null) {
			errorQueryDao.insert(errorQuery);
		} else {
			errorQueryDao.update(errorQuery);
		}
	}
	
	public void delete(DataErrorQuery errorQuery) {
		errorQueryDao.delete(errorQuery.getId());
	}
	
	private void initializeErrorType(CollectSurvey survey, DataErrorQuery q) {
		DataErrorType errorType = getErrorTypeById(survey, q.getTypeId());
		q.setType(errorType);
	}
	//End of DataErrorQuery methods
	
	//Start of DataErrorReport methods
	public DataErrorReport loadReport(CollectSurvey survey, int id) {
		DataErrorReport report = errorReportDao.loadById(id);
		DataErrorQuery query = errorQueryDao.loadById(survey, report.getQueryId());
		report.setQuery(query);
		List<DataErrorReportItem> items = errorReportItemDao.loadByReport(report);
		report.setItems(items);
		return report;
	}
	
	public void save(DataErrorReport report) {
		if (report.getId() == null) {
			errorReportDao.insert(report);
		} else {
			errorReportDao.update(report);
		}
	}

	public void saveReportItems(DataErrorReport report, List<DataErrorReportItem> items) {
		errorReportItemDao.insert(report, items);
	}
	
	public List<DataErrorReportItem> loadReportItems(DataErrorReport report) {
		return errorReportItemDao.loadByReport(report);
	}
	
	//End of DataErrorReport methods
	
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
		
		public DataErrorType get(int id) {
			return typesById.get(id);
		}
		
		public List<DataErrorType> getBySurvey(CollectSurvey survey) {
			return typesBySurvey.get(survey.getId());
		}
		
		public void update(DataErrorType e) {
			remove(e);
			put(e);
		}
		
		public void remove(DataErrorType e) {
			typesById.remove(e.getId());
			List<DataErrorType> list = typesBySurvey.get(e.getSurvey().getId());
			list.remove(e);
		}
	}

}
