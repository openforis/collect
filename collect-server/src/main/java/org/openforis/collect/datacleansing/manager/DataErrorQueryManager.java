/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

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
@Component("dataErrorQueryManager")
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
