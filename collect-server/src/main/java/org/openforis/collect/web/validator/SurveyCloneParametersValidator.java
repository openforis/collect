package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.controller.SurveyController.SurveyCloneParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SurveyCloneParametersValidator extends SimpleValidator<SurveyCloneParameters> {
	
	private static final String NAME_ALREADY_IN_USE_MESSAGE_KEY = "survey.validation.nameAlreadyInUse";
	private static final String NEW_SURVEY_NAME_FIELD = "newSurveyName";
	private static final String ORIGINAL_SURVEY_NAME_FIELD = "originalSurveyName";
	private static final String ORIGINAL_SURVEY_NOT_FOUND_MESSAGE_KEY = "survey.clone.validation.originalSurveyNotFound";
	
	@Autowired
	private SurveyManager surveyManager;

	@Override
	public void validateForm(SurveyCloneParameters parameters, Errors errors) {
		if (validateRequiredField(errors, ORIGINAL_SURVEY_NAME_FIELD)) {
			validateOriginalSurveyExists(parameters, errors);
		}
		if (validateRequiredField(errors, NEW_SURVEY_NAME_FIELD) 
				&& validateInternalName(errors, NEW_SURVEY_NAME_FIELD)
				&& validateMinLength(errors, NEW_SURVEY_NAME_FIELD, 5)) {
			validateSurveyNameUniqueness(parameters, errors);
		}
		validateRequiredFields(errors, "originalSurveyType");
	}

	private void validateOriginalSurveyExists(SurveyCloneParameters parameters, Errors errors) {
		ErrorsHelper helper = new ErrorsHelper(errors);
		String originalSurveyName = helper.getStringValue(ORIGINAL_SURVEY_NAME_FIELD);
		SurveySummary summary = surveyManager.loadSummaryByName(originalSurveyName);
		if (summary == null) {
			errors.rejectValue(ORIGINAL_SURVEY_NAME_FIELD, ORIGINAL_SURVEY_NOT_FOUND_MESSAGE_KEY, new String[] {originalSurveyName}, null);
		}
	}

	private boolean validateSurveyNameUniqueness(SurveyCloneParameters params, Errors errors) {
		SurveySummary existingSummary = surveyManager.loadSummaryByName(params.getNewSurveyName());
		if (existingSummary != null) {
			errors.rejectValue(NEW_SURVEY_NAME_FIELD, NAME_ALREADY_IN_USE_MESSAGE_KEY);
			return false;
		} else {
			return true;
		}
	}
	
}