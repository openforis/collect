/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	
	public Set<DataCleansingChain> loadByStep(DataCleansingStep step) {
		Set<DataCleansingChain> chains = dao.loadChainsByStep(step);
		initializeItems(chains);
		return chains;
	}
	
	@Override
	@Transactional
	public DataCleansingChain save(DataCleansingChain chain) {
		List<Integer> stepIds = new ArrayList<Integer>();
		for (DataCleansingStep step : chain.getSteps()) {
			stepIds.add(step.getId());
		}
		if (chain.getId() != null) {
			dao.deleteStepAssociations(chain);
		}
		super.save(chain);
		
		dao.insertStepAssociations(chain, stepIds);
		
		initializeItem(chain);
		
		return chain;
	}
	
	@Override
	public void delete(DataCleansingChain chain) {
		dao.deleteStepAssociations(chain);
		super.delete(chain);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dao.deleteStepAssociations(survey);
		super.deleteBySurvey(survey);
	}
	
	@Override
	protected void initializeItem(DataCleansingChain chain) {
		super.initializeItem(chain);
		chain.removeAllSteps();
		List<Integer> stepIds = dao.loadStepIds(chain);
		for (Integer stepId : stepIds) {
			DataCleansingStep step = dataCleansingStepManager.loadById((CollectSurvey) chain.getSurvey(), stepId);
			chain.addStep(step);
		}
	}
	
}
