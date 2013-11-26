package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyMainInfoVM;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveySummary;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormValidator extends FormValidator {
	
	protected static final String NAME_FIELD = "name";
	protected static final String URI_FIELD = "uri";

	protected static final String DUPLICATE_NAME_MESSAGE_KEY = "survey.validation.error.duplicate_name";
	protected static final String DUPLICATE_URI_MESSAGE_KEY = "survey.validation.error.duplicate_uri";

	public SurveyMainInfoFormValidator() {
		blocking = true;
	}
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateUri(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		String field = NAME_FIELD;
		if ( validateRequired(ctx, field) && validateInternalName(ctx, field) ) {
			boolean result = validateNameUniqueness(ctx);
			return result;
		} else {
			return false;
		}
	}
	
	protected boolean validateUri(ValidationContext ctx) {
		String field = URI_FIELD;
		if ( validateRequired(ctx, field) && validateUri(ctx, field) ) {
			boolean result = validateUriUniqueness(ctx);
			return result;
		} else {
			return false;
		}
	}

	private boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyMainInfoVM vm = (SurveyMainInfoVM) getVM(ctx);
		String name = getValue(ctx, NAME_FIELD);
		SurveySummary existingSurveySummary = loadExistingSurveySummaryByName(ctx, name);
		if ( existingSurveySummary != null && 
				! vm.isCurrentEditedSurvey(existingSurveySummary) ) {
			this.addInvalidMessage(ctx, NAME_FIELD, Labels.getLabel(DUPLICATE_NAME_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
	
	private boolean validateUriUniqueness(ValidationContext ctx) {
		SurveyMainInfoVM vm = (SurveyMainInfoVM) getVM(ctx);
		String uri = getValue(ctx, URI_FIELD);
		SurveySummary existingSurveySummary = loadExistingSurveySummaryByUri(ctx, uri);
		if ( existingSurveySummary != null && 
				! vm.isCurrentEditedSurvey(existingSurveySummary) ) {
			this.addInvalidMessage(ctx, URI_FIELD, Labels.getLabel(DUPLICATE_URI_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}

	private SurveySummary loadExistingSurveySummaryByName(ValidationContext ctx,
			String name) {
		SurveyMainInfoVM vm = (SurveyMainInfoVM) getVM(ctx);
		SurveyManager surveyManager = vm.getSurveyManager();
		SurveySummary summary = surveyManager.loadSummaryByName(name);
		return summary;
	}

	private SurveySummary loadExistingSurveySummaryByUri(ValidationContext ctx,
			String uri) {
		SurveyMainInfoVM vm = (SurveyMainInfoVM) getVM(ctx);
		SurveyManager surveyManager = vm.getSurveyManager();
		SurveySummary summary = surveyManager.loadSummaryByUri(uri);
		return summary;
	}
	
}
