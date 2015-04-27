/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.Date;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.persistence.DataCleansingChainDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingChainManager extends AbstractSurveyObjectManager<DataCleansingChain, DataCleansingChainDao> {

	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Override
	@Autowired
	@Qualifier("dataCleansingChainDao")
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
	
	@Override
	@Transactional
	public void save(DataCleansingChain chain) {
		chain.setModifiedDate(new Date());
		if (chain.getId() == null) {
			chain.setCreationDate(new Date());
		}
		super.save(chain);
		List<DataCleansingStep> steps = chain.getSteps();
		for (DataCleansingStep step : steps) {
			dataCleansingStepManager.save(step);
		}
	}

	private void initializeSteps(CollectSurvey survey, DataCleansingChain q) {
		List<Integer> stepIds = dao.loadStepIds(q);
		for (Integer stepId : stepIds) {
			DataCleansingStep step = dataCleansingStepManager.loadById(stepId);
			q.addStep(step);
		}
	}
	
}
