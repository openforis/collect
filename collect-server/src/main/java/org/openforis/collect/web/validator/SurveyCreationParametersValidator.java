package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SurveyCreationParametersValidator extends SimpleValidator<SurveyCreationParameters> {
	
	private static final String NAME_ALREADY_IN_USE_MESSAGE_KEY = "survey.validation.nameAlreadyInUse";
	private static final String NAME_FIELD = "name";
	@Autowired
	private SurveyManager surveyManager;

	@Override
	public void validateForm(SurveyCreationParameters parameters, Errors errors) {
		if (validateRequiredField(errors, NAME_FIELD) 
				&& validateInternalName(errors, NAME_FIELD)
				&& validateMinLength(errors, NAME_FIELD, 5)) {
			validateSurveyNameUniqueness(parameters, errors);
		}
		validateRequiredFields(errors, "templateType", "defaultLanguageCode", "userGroupId");
	}

	private boolean validateSurveyNameUniqueness(SurveyCreationParameters parameters, Errors errors) {
		String name = parameters.getName();
		SurveySummary existingSummary = surveyManager.loadSummaryByName(name);
		if (existingSummary != null) {
			errors.rejectValue(NAME_FIELD, NAME_ALREADY_IN_USE_MESSAGE_KEY);
			return false;
		} else {
			return true;
		}
	}
	
}