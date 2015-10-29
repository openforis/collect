package org.openforis.collect.datacleansing.form.validation;

import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.datacleansing.form.DataQueryTypeForm;
import org.openforis.collect.datacleansing.manager.DataQueryTypeManager;
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
public class DataQueryTypeValidator extends SimpleValidator<DataQueryTypeForm> {

	private static final String CODE_FIELD = "code";
	@Autowired
	private DataQueryTypeManager dataQueryTypeManager;
	
	@Override
	public void validateForm(DataQueryTypeForm target, Errors errors) {
		if (validateRequiredField(errors, CODE_FIELD)) {
			validateCodeUniqueness(target, errors);
		}
	}

	private boolean validateCodeUniqueness(DataQueryTypeForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		DataQueryType existingItem = dataQueryTypeManager.loadByCode(survey, target.getCode());
		if (existingItem != null && ! existingItem.getId().equals(target.getId())) {
			rejectDuplicateValue(errors, CODE_FIELD);
			return false;
		} else {
			return true;
		}
	}

}
