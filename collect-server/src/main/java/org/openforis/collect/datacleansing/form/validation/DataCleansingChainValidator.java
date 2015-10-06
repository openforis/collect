package org.openforis.collect.datacleansing.form.validation;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.form.DataCleansingChainForm;
import org.openforis.collect.datacleansing.manager.DataCleansingChainManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingChainValidator extends SimpleValidator<DataCleansingChainForm> {

	private static final String TITLE_FIELD = "title";
	private static final String STEP_IDS_FIELD = "stepIds";
	
	@Autowired
	private DataCleansingChainManager dataCleansingChainManager;
	
	@Override
	public void validateForm(DataCleansingChainForm target, Errors errors) {
		if (validateRequiredField(errors, TITLE_FIELD)) {
			validateTitleUniqueness(target, errors);
		}
		if (validateRequiredField(errors, STEP_IDS_FIELD)) {
			validateStepsUniqueness(target, errors);
		}
	}

	private boolean validateStepsUniqueness(DataCleansingChainForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataCleansingChain> chains = dataCleansingChainManager.loadBySurvey(survey);
		for (DataCleansingChain chain : chains) {
			if (! chain.getId().equals(target.getId())) {
				List<DataCleansingStep> steps = chain.getSteps();
				List<Integer> stepIds = new ArrayList<Integer>();
				for (DataCleansingStep step : steps) {
					stepIds.add(step.getId());
				}
				if (stepIds.equals(target.getStepIds())) {
					rejectDuplicateValue(errors, STEP_IDS_FIELD);
					return false;
				}
			}
		}
		return true;
	}

	private boolean validateTitleUniqueness(DataCleansingChainForm target, Errors errors) {
		String title = target.getTitle();
		CollectSurvey survey = getActiveSurvey();
		List<DataCleansingChain> chains = dataCleansingChainManager.loadBySurvey(survey);
		for (DataCleansingChain chain : chains) {
			if (! chain.getId().equals(target.getId()) && chain.getTitle().equalsIgnoreCase(title)) {
				rejectDuplicateValue(errors, TITLE_FIELD);
				return false;
			}
		}
		return true;
	}


}
