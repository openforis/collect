package org.openforis.collect.designer.form.validator;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveySummary;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyNameValidator extends BaseValidator {

	protected static final String DUPLICATE_NAME_MESSAGE_KEY = "survey.validation.error.duplicate_name";
	
	private SurveyManager surveyManager;
	private String surveyNameField;
	private boolean checkForDuplicates;
	
	public SurveyNameValidator(SurveyManager surveyManager, String surveyNameField, boolean checkForDuplicates) {
		super();
		this.surveyManager = surveyManager;
		this.surveyNameField = surveyNameField;
		this.checkForDuplicates = checkForDuplicates;
	}

	@Override
	public void validate(ValidationContext ctx) {
		if ( validateRequired(ctx, surveyNameField) && validateInternalName(ctx, surveyNameField) ) {
			if (checkForDuplicates) {
				validateNameUniqueness(ctx);
			}
		}
	}
	private boolean validateNameUniqueness(ValidationContext ctx) {
		String name = getValue(ctx, surveyNameField);
		SurveySummary existingSurveySummary = loadExistingSurveySummaryByName(ctx, name);
		if ( existingSurveySummary == null ) {
			return true;
		} else {
			this.addInvalidMessage(ctx, surveyNameField, Labels.getLabel(DUPLICATE_NAME_MESSAGE_KEY));
			return false;
		}
	}
	
	private SurveySummary loadExistingSurveySummaryByName(ValidationContext ctx, String name) {
		SurveySummary summary = surveyManager.loadSummaryByName(name);
		return summary;
	}
	
}