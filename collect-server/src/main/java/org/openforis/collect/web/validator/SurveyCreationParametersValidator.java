package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SurveyCreationParametersValidator extends SimpleValidator<SurveyCreationParameters> {

	@Override
	public void validateForm(SurveyCreationParameters parameters, Errors errors) {
		if (validateRequiredField(errors, "name")) {
			validateInternalName(errors, "name");
		}
		validateRequiredFields(errors, "templateType", "defaultLanguageCode", "userGroupId");
	}
	
}