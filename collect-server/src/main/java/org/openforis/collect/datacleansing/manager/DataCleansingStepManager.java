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
import org.springframework.beans.factory.annotation.Qualifier;
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
	@Qualifier("dataCleansingStepDao")
	@Override
	public void setDao(DataCleansingStepDao dao) {
		super.setDao(dao);
	}
	
	@Override
	protected void initializeItem(DataCleansingStep i) {
		super.initializeItem(i);
		initializeQuery(i);
	}
	
	private void initializeQuery(DataCleansingStep step) {
		DataQuery query = dataQueryManager.loadById((CollectSurvey) step.getSurvey(), step.getQueryId());
		step.setQuery(query);
	}

}
