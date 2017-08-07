package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.SurveyContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SRSFormValidator extends FormValidator {
	
	protected static final String ID_FIELD = "id";
	protected static final String WKT_FIELD = "wellKnownText";
	
	private static final String ITEM_ID_ALREADY_DEFINED_MESSAGE_KEY = "survey.srs.validation.id_already_defined";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateId(ctx);
	}

	protected boolean validateId(ValidationContext ctx) {
		boolean result = validateRequired(ctx, ID_FIELD);
		if ( result ) {
			result = validateIdUniqueness(ctx);
		}
		if (validateRequired(ctx, WKT_FIELD)) {
			result = result && validateWKT(ctx);
		} else {
			result = false;
		}
		return result;
	}

	protected boolean validateIdUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<SpatialReferenceSystem> viewModel = getVM(ctx);
		SpatialReferenceSystem editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String id = (String) getValue(ctx, ID_FIELD);
		SpatialReferenceSystem existingItem = survey.getSpatialReferenceSystem(id);
		if ( existingItem != null && ! existingItem.equals(editedItem) ) {
			String message = Labels.getLabel(ITEM_ID_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, ID_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
	private boolean validateWKT(ValidationContext ctx) {
		String wkt = (String) getValue(ctx, WKT_FIELD);
		CollectSurvey survey = getSurvey(ctx);
		SurveyContext context = survey.getContext();
		CoordinateOperations coordinateOperations = context.getCoordinateOperations();
		try {
			coordinateOperations.validateWKT(wkt);
			return true;
		} catch (Exception e) {
			String message = Labels.getLabel("survey.srs.validation.error.invalid_wkt", new String[]{e.getMessage()});
			addInvalidMessage(ctx, WKT_FIELD, message);
			return false;
		}
	}
	
	private CollectSurvey getSurvey(ValidationContext ctx) {
		SurveyObjectBaseVM<SpatialReferenceSystem> viewModel = getVM(ctx);
		CollectSurvey survey = viewModel.getSurvey();
		return survey;
	}
	
}
