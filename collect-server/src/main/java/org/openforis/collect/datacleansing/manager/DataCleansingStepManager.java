/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Set;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStep.DataCleansingStepType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataCleansingStepDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingStepManager extends AbstractSurveyObjectManager<DataCleansingStep, DataCleansingStepDao> {

	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataCleansingChainManager dataCleansingChainManager;
	
	@Override
	@Transactional
	public void delete(DataCleansingStep step) {
		Set<DataCleansingChain> chainsUsingStep = dataCleansingChainManager.loadByStep(step);
		if (chainsUsingStep.isEmpty()) {
			dao.deleteStepValues(step.getId());
			super.delete(step);
		} else {
			throw new IllegalStateException(String.format("Cannote delete step with id %d: some chains are associated to it", step.getId()));
		}
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dao.deleteStepValues(survey);
		super.deleteBySurvey(survey);
	}
	
	public List<DataCleansingStep> loadByQuery(DataQuery query) {
		List<DataCleansingStep> steps = dao.loadByQuery(query);
		initializeItems(steps);
		return steps;
	}
	
	@Override
	protected void initializeItem(DataCleansingStep i) {
		super.initializeItem(i);
		initializeQuery(i);
	}
	
	@Override
	@Transactional
	public DataCleansingStep save(DataCleansingStep step) {
		super.save(step);
		dao.deleteStepValues(step.getId());
		if (step.getType() == DataCleansingStepType.ATTRIBUTE_UPDATE && ! step.getUpdateValues().isEmpty()) {
			dao.insertStepValues(step.getId(), step.getUpdateValues());
		}
		return step;
	}
	
	private void initializeQuery(DataCleansingStep step) {
		DataQuery query = dataQueryManager.loadById((CollectSurvey) step.getSurvey(), step.getQueryId());
		step.setQuery(query);
	}

}
