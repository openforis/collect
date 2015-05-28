package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStep.UpdateType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.AbstractBindingResult;
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
	private static final String UPDATE_TYPE_FIELD = "updateType";
	private static final String FIX_EXPRESSION_FIELD = "fixExpression";
	private static final String FIELD_FIX_EXPRESSIONS_FIELD = "fieldFixExpressions";
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Override
	public void validateForm(DataCleansingStepForm target, Errors errors) {
		validateRequiredFields(errors, TITLE_FIELD);
		if (validateRequiredFields(errors, QUERY_ID_FIELD) & validateRequiredField(errors, UPDATE_TYPE_FIELD)) {
			String updateTypeVal = (String) errors.getFieldValue(UPDATE_TYPE_FIELD);
			UpdateType updateType = UpdateType.valueOf(updateTypeVal);
			switch (updateType) {
			case ATTRIBUTE:
				if (validateRequiredFields(errors, FIX_EXPRESSION_FIELD) && validateFixExpression(target, errors)) {
					validateUniqueness(target, errors);
				}
				break;
			case FIELD:
				if (validateRequiredFields(errors, FIELD_FIX_EXPRESSIONS_FIELD) && validateFieldFixExpressions(target, errors)) {
					validateUniqueness(target, errors);
				}
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean validateFieldFixExpressions(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		
		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		
		boolean valid = true;
		List<String> fieldExpressions = (List<String>) ((AbstractBindingResult) errors).getRawFieldValue(FIELD_FIX_EXPRESSIONS_FIELD);
		for (int fieldIdx = 0; fieldIdx < fieldExpressions.size(); fieldIdx++) {
			String expression = fieldExpressions.get(fieldIdx);
			if (StringUtils.isNotBlank(expression)) {
				valid = valid && validateValueExpression(errors, contextNodeDef, thisNodeDef, FIELD_FIX_EXPRESSIONS_FIELD + "[" + fieldIdx + "]", expression);
			}
		}
		return valid;
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
