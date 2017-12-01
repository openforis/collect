package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.web.controller.SurveyController.SurveyImportParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SurveyImportParametersValidator extends SimpleValidator<SurveyImportParameters> {
	
	private static final String NAME_FIELD = "name";

	@Override
	public void validateForm(SurveyImportParameters parameters, Errors errors) {
		validateRequiredFields(errors, NAME_FIELD, "userGroupId");
	}

}