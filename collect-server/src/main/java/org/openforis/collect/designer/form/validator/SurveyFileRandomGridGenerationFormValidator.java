package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileRandomGridGenerationFormValidator extends FormValidator {

	public static final String PERCENTAGE_FIELD = "percentage";
	public static final String NEXT_MEASUREMENT_FIELD = "nextMeasurement";

	@Override
	protected boolean isEditingItem(ValidationContext ctx) {
		// consider item being edited: it's not running in a master/detail context
		return true;
	}

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validatePercentage(ctx);
		validateNextMeasurement(ctx);
	}

	private boolean validatePercentage(ValidationContext ctx) {
		return validateRequired(ctx, PERCENTAGE_FIELD) && validateGreaterThan(ctx, PERCENTAGE_FIELD, 0)
				&& validateLessThan(ctx, PERCENTAGE_FIELD, 100);
	}

	private boolean validateNextMeasurement(ValidationContext ctx) {
		return validateRequired(ctx, NEXT_MEASUREMENT_FIELD);
	}

}
