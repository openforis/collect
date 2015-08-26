/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataErrorQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataErrorQueryManager")
public class DataErrorQueryManager extends AbstractSurveyObjectManager<DataErrorQuery, DataErrorQueryDao> {

	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataErrorReportManager dataErrorReportManager;
	@Autowired
	private MessageSource messageSource;
	
	@Override
	@Autowired
	@Qualifier("dataErrorQueryDao")
	public void setDao(DataErrorQueryDao dao) {
		super.setDao(dao);
	}
	
	@Override
	public void delete(DataErrorQuery query) {
		List<DataErrorReport> reports = dataErrorReportManager.loadByQuery(query);
		if (reports.isEmpty()) {
			super.delete(query);
		} else {
			String queryCompleteTitle = query.getType().getCode() + " - " + query.getQuery().getTitle();
			String message = messageSource.getMessage("data_error_query.delete.error.used_by_report", new String[]{queryCompleteTitle}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
	
	@Override
	public List<DataErrorQuery> loadBySurvey(CollectSurvey survey) {
		List<DataErrorQuery> queries = super.loadBySurvey(survey);
		initializeItems(queries);
		return queries;
	}
	
	public List<DataErrorQuery> loadByQuery(DataQuery query) {
		List<DataErrorQuery> queries = dao.loadByQuery(query);
		initializeItems(queries);
		return queries;
	}
	
	public List<DataErrorQuery> loadByType(DataErrorType errorType) {
		List<DataErrorQuery> queries = dao.loadByType(errorType);
		initializeItems(queries);
		return queries;
	}

	@Override
	protected void initializeItem(DataErrorQuery q) {
		super.initializeItem(q);
		initializeQuery(q);
		initializeErrorType(q);
	}
	
	private void initializeErrorType(DataErrorQuery q) {
		DataErrorType errorType = dataErrorTypeManager.loadById((CollectSurvey) q.getSurvey(), q.getTypeId());
		q.setType(errorType);
	}
	
	private void initializeQuery(DataErrorQuery q) {
		DataQuery query = dataQueryManager.loadById((CollectSurvey) q.getSurvey(), q.getQueryId());
		q.setQuery(query);
	}

}
