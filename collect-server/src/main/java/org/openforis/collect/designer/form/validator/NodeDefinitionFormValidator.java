package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionFormValidator extends FormValidator {

	protected static final String DESCRIPTION_FIELD = "description";
	protected static final String NAME_FIELD = "name";
	protected static final String MAX_COUNT_FIELD = "maxCount";
	protected static final String MULTIPLE_FIELD = "multiple";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
		validateMaxCount(ctx);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}
	
	protected void validateDescription(ValidationContext ctx) {
		//TODO
		//Object value = getValue(ctx, DESCRIPTION_FIELD);
	}
	
	protected void validateMaxCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if ( multiple != null && multiple.booleanValue() ) {
			Integer value = (Integer) getValue(ctx, MAX_COUNT_FIELD);
			if ( value == null ) {
				String message = Labels.getLabel(FIELD_REQUIRED_MESSAGE_KEY);
				addInvalidMessage(ctx, MAX_COUNT_FIELD, message);
			} else {
				validateGreaterThan(ctx, MAX_COUNT_FIELD, 1, true);
			}
		}
	}

}
