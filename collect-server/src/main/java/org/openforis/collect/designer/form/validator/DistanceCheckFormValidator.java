/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public class DistanceCheckFormValidator extends CheckFormValidator {

	private static final String SOURCE_POINT_EXPRESSION_FIELD = "sourcePointExpression";
	private static final String DESTINATION_POINT_EXPRESSION_FIELD = "destinationPointExpression";
	private static final String MIN_DISTANCE_FIELD = "minDistanceExpression";
	private static final String MAX_DISTANCE_FIELD = "maxDistanceExpression";
	
	private String MIN_OR_MAX_REQUIRED_MESSAGE_KEY = "survey.schema.node.check.distance.validation.min_or_max_distance_not_specified";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		NodeDefinition contextNode = getContextNode(ctx);
		if ( validateMinOrMaxExpressionRequireness(ctx) ) {
			validateValueExpressionField(ctx, contextNode, MIN_DISTANCE_FIELD);
			validateValueExpressionField(ctx, contextNode, MAX_DISTANCE_FIELD);
		}
		validateValueExpressionField(ctx, contextNode, SOURCE_POINT_EXPRESSION_FIELD);
		if (validateRequired(ctx, DESTINATION_POINT_EXPRESSION_FIELD)) {
			validateValueExpressionField(ctx, contextNode, DESTINATION_POINT_EXPRESSION_FIELD);
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
	
}
