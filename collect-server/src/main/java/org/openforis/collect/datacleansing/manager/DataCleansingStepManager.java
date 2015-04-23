/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataCleansingStepDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingStepManager extends AbstractSurveyObjectManager<DataCleansingStep, DataCleansingStepDao> {

	@Autowired
	private DataQueryManager dataQueryManager;
	
	@Autowired
	@Override
	public void setDao(DataCleansingStepDao dao) {
		super.setDao(dao);
	}
	
	@Override
	public DataCleansingStep loadById(CollectSurvey survey, int id) {
		DataCleansingStep step = super.loadById(survey, id);
		initializeQuery(step);
		return step;
	}
	
	private void initializeQuery(DataCleansingStep q) {
		DataQuery query = dataQueryManager.loadById((CollectSurvey) q.getSurvey(), q.getQueryId());
		q.setQuery(query);
	}

}
