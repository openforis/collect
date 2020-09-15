package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.web.controller.SurveyController.SurveyImportParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SurveyImportParametersValidator extends SimpleValidator<SurveyImportParameters> {

	private static final String NAME_FIELD = "name";
	private static final String USER_GROUP_ID_FIELD = "userGroupId";

	@Override
	public void validateForm(SurveyImportParameters parameters, Errors errors) {
		if (validateRequiredField(errors, NAME_FIELD) && validateMinLength(errors, NAME_FIELD, 5)) {
			validateInternalName(errors, NAME_FIELD);
		}
		validateRequiredField(errors, USER_GROUP_ID_FIELD);
	}
}
