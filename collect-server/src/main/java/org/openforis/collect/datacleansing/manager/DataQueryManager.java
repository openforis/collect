/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataQueryManager")
public class DataQueryManager extends AbstractSurveyObjectManager<DataQuery, DataQueryDao> {

	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Override
	@Autowired
	@Qualifier("dataQueryDao")
	public void setDao(DataQueryDao dao) {
		super.setDao(dao);
	}

	@Override
	public void delete(DataQuery query) {
		List<DataCleansingStep> steps = dataCleansingStepManager.loadByQuery(query);
		if (steps.isEmpty()) {
			super.delete(query);
		} else {
			String message = String.format("The query %s is used by some other Data Cleansing Step and cannot be deleted", query.getTitle());
			throw new IllegalStateException(message);
		}
	}
	
}
