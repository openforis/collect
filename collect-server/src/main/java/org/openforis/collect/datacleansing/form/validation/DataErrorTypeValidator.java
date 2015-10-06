package org.openforis.collect.datacleansing.form.validation;

import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.form.DataErrorTypeForm;
import org.openforis.collect.datacleansing.manager.DataErrorTypeManager;
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
public class DataErrorTypeValidator extends SimpleValidator<DataErrorTypeForm> {

	private static final String CODE_FIELD = "code";
	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	
	@Override
	public void validateForm(DataErrorTypeForm target, Errors errors) {
		if (validateRequiredField(errors, CODE_FIELD)) {
			validateCodeUniqueness(target, errors);
		}
	}

	private boolean validateCodeUniqueness(DataErrorTypeForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		DataErrorType existingItem = dataErrorTypeManager.loadByCode(survey, target.getCode());
		if (existingItem != null && ! existingItem.getId().equals(target.getId())) {
			rejectDuplicateValue(errors, CODE_FIELD);
			return false;
		} else {
			return true;
		}
	}

}
