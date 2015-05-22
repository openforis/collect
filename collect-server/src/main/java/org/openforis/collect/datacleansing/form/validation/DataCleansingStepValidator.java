package org.openforis.collect.datacleansing.form.validation;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
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

	@Autowired
	private DataQueryManager dataQueryManager;
	
	@Override
	public void validateForm(DataCleansingStepForm target, Errors errors) {
		validateRequiredField(errors, "title");
		if (validateRequiredField(errors, "queryId") & validateRequiredField(errors, "fixExpression")) {
			validateFixExpression(target, errors);
		}
	}

	private void validateFixExpression(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		
		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		String expression = target.getFixExpression();
		validateValueExpression(errors, contextNodeDef, thisNodeDef, "fixExpression", expression);
	}

}
