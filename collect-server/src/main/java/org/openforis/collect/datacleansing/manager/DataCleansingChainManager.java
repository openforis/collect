/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.persistence.DataCleansingChainDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
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
	@Transactional
	public void save(DataCleansingChain chain) {
		super.save(chain);
		List<DataCleansingStep> steps = chain.getSteps();
		for (DataCleansingStep step : steps) {
			dataCleansingStepManager.save(step);
		}
	}
	
	@Override
	protected void initializeItem(DataCleansingChain q) {
		super.initializeItem(q);
		List<Integer> stepIds = dao.loadStepIds(q);
		for (Integer stepId : stepIds) {
			DataCleansingStep step = dataCleansingStepManager.loadById(stepId);
			q.addStep(step);
		}
	}
	
}
