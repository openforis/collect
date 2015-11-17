package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataQueryForm;
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
public class DataQueryValidator extends SimpleValidator<DataQueryForm> {

	private static final String TITLE_FIELD = "title";
	private static final String ENTITY_DEFINITION_ID_FIELD = "entityDefinitionId";
	private static final String ATTRIBUTE_DEFINITION_ID_FIELD = "attributeDefinitionId";
	private static final String CONDITIONS_FIELD = "conditions";

	@Autowired
	private DataQueryManager dataQueryManager;
	
	@Override
	public void validateForm(DataQueryForm target, Errors errors) {
		validateRequiredField(errors, TITLE_FIELD);
		if (validateRequiredFields(errors, ENTITY_DEFINITION_ID_FIELD, ATTRIBUTE_DEFINITION_ID_FIELD, CONDITIONS_FIELD)) {
			if (validateConditions(target, errors)) {
				validateUniqueness(target, errors);
			}
		}
	}

	private boolean validateConditions(DataQueryForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(target.getEntityDefinitionId());
		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(target.getAttributeDefinitionId());
		String expression = target.getConditions();
		boolean valid = validateBooleanExpression(errors, contextNodeDef, thisNodeDef, CONDITIONS_FIELD, expression);
		return valid;
	}

	private boolean validateUniqueness(DataQueryForm target, Errors errors) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		List<DataQuery> queries = dataQueryManager.loadBySurvey(survey);
		for (DataQuery dataQuery : queries) {
			if (! dataQuery.getId().equals(target.getId())) {
				boolean valid = true;
				if (dataQuery.getTitle().equalsIgnoreCase(target.getTitle())) {
					rejectDuplicateValue(errors, TITLE_FIELD);
					valid = false;
				} else if (
					Integer.valueOf(dataQuery.getEntityDefinitionId()).equals(target.getEntityDefinitionId())
					&& Integer.valueOf(dataQuery.getAttributeDefinitionId()).equals(target.getAttributeDefinitionId()) 
					&& dataQuery.getConditions().equals(target.getConditions())) {
					rejectDuplicateValue(errors, ENTITY_DEFINITION_ID_FIELD);
					rejectDuplicateValue(errors, ATTRIBUTE_DEFINITION_ID_FIELD);
					rejectDuplicateValue(errors, CONDITIONS_FIELD);
					valid = false;
				}
				if (! valid) {
					return false;
				}
			}
		}
		return true;
	}

}
