package org.openforis.collect.datacleansing.form.validation;

import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryValidator extends SimpleValidator<DataQueryForm> {

	@Override
	public boolean supports(Class<?> clazz) {
		return DataQueryForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validateForm(DataQueryForm target, Errors errors) {
		validateRequiredField(errors, "title");
		validateRequiredField(errors, "entityDefinitionId");
		validateRequiredField(errors, "attributeDefinitionId");
		if (validateRequiredField(errors, "conditions")) {
			validateConditions(target, errors);
		}
	}

	private void validateConditions(DataQueryForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(target.getEntityDefinitionId());
		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(target.getAttributeDefinitionId());
		String expression = target.getConditions();
		validateBooleanExpression(errors, contextNodeDef, thisNodeDef, "conditions", expression);
	}

}
