/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.persistence.DataCleansingChainDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingChainManager extends AbstractSurveyObjectManager<DataCleansingChain, DataCleansingChainDao> {

	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Autowired
	@Override
	public void setDao(DataCleansingChainDao dao) {
		super.setDao(dao);
	}
	
	@Override
	public List<DataCleansingChain> loadBySurvey(CollectSurvey survey) {
		List<DataCleansingChain> result = super.loadBySurvey(survey);
		if (result != null) {
			for (DataCleansingChain c : result) {
				initializeSteps(survey, c);
			}
		}
		return result;
	}
	
	@Override
	public DataCleansingChain loadById(CollectSurvey survey, int id) {
		DataCleansingChain obj = super.loadById(survey, id);
		initializeSteps(survey, obj);
		return obj;
	}

	private void initializeSteps(CollectSurvey survey, DataCleansingChain q) {
		List<Integer> stepIds = dao.loadStepIds(q);
		for (Integer stepId : stepIds) {
			DataCleansingStep step = dataCleansingStepManager.loadById(stepId);
			q.addStep(step);
		}
	}
	
}
