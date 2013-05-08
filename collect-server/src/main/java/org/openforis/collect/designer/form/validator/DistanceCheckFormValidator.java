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
public class DistanceCheckFormValidator extends CheckFormValidator {

	private String MIN_DISTANCE_FIELD = "minDistanceExpression";
	private String MAX_DISTANCE_FIELD = "maxDistanceExpression";
	
	private String MIN_OR_MAX_REQUIRED_MESSAGE_KEY = "survey.schema.node.check.distance.validation.min_or_max_distance_not_specified";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateMinOrMaxExpressionRequireness(ctx);
	}

	protected boolean validateMinOrMaxExpressionRequireness(ValidationContext ctx) {
		String minExpr = getValue(ctx, MIN_DISTANCE_FIELD);
		String maxExpr = getValue(ctx, MAX_DISTANCE_FIELD);
		if ( StringUtils.isBlank(minExpr) && StringUtils.isBlank(maxExpr) ) {
			this.addInvalidMessage(ctx, MIN_DISTANCE_FIELD, Labels.getLabel(MIN_OR_MAX_REQUIRED_MESSAGE_KEY));
			this.addInvalidMessage(ctx, MAX_DISTANCE_FIELD, Labels.getLabel(MIN_OR_MAX_REQUIRED_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
}
