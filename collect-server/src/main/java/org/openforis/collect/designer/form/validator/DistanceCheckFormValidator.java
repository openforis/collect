/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
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
		super.internalValidate(ctx);
		if ( validateMinOrMaxExpressionRequireness(ctx) ) {
			validateFieldExpression(ctx, MIN_DISTANCE_FIELD);
			validateFieldExpression(ctx, MAX_DISTANCE_FIELD);
		}
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
	
	private boolean validateFieldExpression(ValidationContext ctx, String field) {
		ExpressionValidator expressionValidator = getExpressionValidator(ctx);
		NodeDefinition parentDefn = getContextNode(ctx);
		String expression = getValue(ctx, field);
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateValueExpression(parentDefn, expression)) {
			addInvalidMessage(ctx, field, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
}
