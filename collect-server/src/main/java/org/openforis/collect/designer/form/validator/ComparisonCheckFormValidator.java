/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public class ComparisonCheckFormValidator extends CheckFormValidator {
	
	private String GREATER_THAN_FIELD = "greaterThan";
	private String LESS_THAN_FIELD = "lessThan";
	
	private String GREATER_OR_LESS_REQUIRED_MESSAGE_KEY = "survey.schema.node.check.comparison.validation.greater_or_equal_not_specified";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateGreaterOrLessRequireness(ctx);
	}

	protected boolean validateGreaterOrLessRequireness(ValidationContext ctx) {
		String greaterThan = getValue(ctx, GREATER_THAN_FIELD);
		String lessThan = getValue(ctx, LESS_THAN_FIELD);
		if ( StringUtils.isBlank(greaterThan) && StringUtils.isBlank(lessThan) ) {
			this.addInvalidMessage(ctx, GREATER_THAN_FIELD, Labels.getLabel(GREATER_OR_LESS_REQUIRED_MESSAGE_KEY));
			this.addInvalidMessage(ctx, LESS_THAN_FIELD, Labels.getLabel(GREATER_OR_LESS_REQUIRED_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
	

}
