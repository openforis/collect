package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingStepValidator extends SimpleValidator<DataCleansingStepForm> {

	private static final String TITLE_FIELD = "title";
	private static final String QUERY_ID_FIELD = "queryId";
	private static final String FIX_EXPRESSION_FIELD = "fixExpression";
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Override
	public void validateForm(DataCleansingStepForm target, Errors errors) {
		validateRequiredFields(errors, TITLE_FIELD);
		if (validateRequiredFields(errors, QUERY_ID_FIELD) & validateRequiredFields(errors, FIX_EXPRESSION_FIELD)) {
			if (validateFixExpression(target, errors)) {
				validateUniqueness(target, errors);
			}
		}
	}

	private boolean validateFixExpression(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		
		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		String expression = target.getFixExpression();
		boolean valid = validateValueExpression(errors, contextNodeDef, thisNodeDef, FIX_EXPRESSION_FIELD, expression);
		return valid;
	}

	private void validateUniqueness(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataCleansingStep> steps = dataCleansingStepManager.loadBySurvey(survey);
		for (DataCleansingStep step : steps) {
			if (! step.getId().equals(target.getId()) 
					&& step.getQueryId().equals(target.getQueryId())
					&& step.getFixExpression().equals(target.getFixExpression())) {
				rejectDuplicateValue(errors, QUERY_ID_FIELD);
				rejectDuplicateValue(errors, FIX_EXPRESSION_FIELD);
			}
		}
	}

}
