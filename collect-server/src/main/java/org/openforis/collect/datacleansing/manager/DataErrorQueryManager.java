/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataErrorQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataErrorQueryManager extends AbstractSurveyObjectManager<DataErrorQuery, DataErrorQueryDao> {

	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataQueryManager dataQueryManager;
	
	@Override
	@Autowired
	@Qualifier("dataErrorQueryDao")
	public void setDao(DataErrorQueryDao dao) {
		super.setDao(dao);
	}
	
	@Override
	public List<DataErrorQuery> loadBySurvey(CollectSurvey survey) {
		List<DataErrorQuery> result = super.loadBySurvey(survey);
		if (result != null) {
			for (DataErrorQuery q : result) {
				initializeItem(survey, q);
			}
		}
		return result;
	}

	@Override
	public DataErrorQuery loadById(CollectSurvey survey, int id) {
		DataErrorQuery obj = super.loadById(survey, id);
		initializeItem(survey, obj);
		return obj;
	}

	private void initializeItem(CollectSurvey survey, DataErrorQuery q) {
		initializeQuery(survey, q);
		initializeErrorType(survey, q);
	}
	
	private void initializeErrorType(CollectSurvey survey, DataErrorQuery q) {
		DataErrorType errorType = dataErrorTypeManager.loadById(survey, q.getTypeId());
		q.setType(errorType);
	}
	
	private void initializeQuery(CollectSurvey survey, DataErrorQuery q) {
		DataQuery query = dataQueryManager.loadById(survey, q.getQueryId());
		q.setQuery(query);
	}
}
