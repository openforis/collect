package org.openforis.collect.designer.form.validator;

import java.util.Date;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class ModelVersionFormValidator extends SurveyObjectFormValidator<ModelVersion> {
	
	protected static final String NAME_FIELD = "name";
	protected static final String DATE_FIELD = "date";
	
	private static final String ITEM_DATE_ALREADY_DEFINED_MESSAGE_KEY = "survey.versioning.validation.error.duplicate_date";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateDate(ctx);
		validateName(ctx);
	}

	private boolean validateDate(ValidationContext ctx) {
		if (validateRequired(ctx, DATE_FIELD)) {
			return validateDateUniqueness(ctx);
		} else {
			return false;
		}
	}

	private boolean validateDateUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<ModelVersion> viewModel = getVM(ctx);
		ModelVersion editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		Date date = getValue(ctx, DATE_FIELD);
		for (ModelVersion modelVersion : survey.getVersions()) {
			if (modelVersion.getId() != editedItem.getId() && Dates.compareDateOnly(modelVersion.getDate(), date) == 0) {
				String message = Labels.getLabel(ITEM_DATE_ALREADY_DEFINED_MESSAGE_KEY);
				addInvalidMessage(ctx, DATE_FIELD, message);
				return false;
			}
		}
		return true;
	}

	protected boolean validateName(ValidationContext ctx) {
		if ( validateRequired(ctx, NAME_FIELD) ) {
			return validateNameUniqueness(ctx);
		} else {
			return false;
		}
	}

	protected boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<ModelVersion> viewModel = getVM(ctx);
		ModelVersion editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String name = getValue(ctx, NAME_FIELD);
		ModelVersion existingItem = survey.getVersion(name);
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
}
