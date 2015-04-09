/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.persistence.DataCleansingStepDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingStepManager extends AbstractSurveyObjectManager<DataCleansingStep, DataCleansingStepDao> {

	@Autowired
	@Override
	public void setDao(DataCleansingStepDao dao) {
		super.setDao(dao);
	}
	
}
