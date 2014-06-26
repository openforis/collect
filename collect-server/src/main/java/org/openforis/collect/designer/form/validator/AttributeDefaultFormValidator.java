package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefaultFormValidator extends FormValidator {
	
	private static final String PARENT_DEFINITION_ARG = "parentDefinition";

	protected static final String VALUE_OR_EXPRESSION_REQUIRED_MESSAGE_KEY = 
			"survey.schema.attribute.attribute_default.validation.value_or_expression_required";
	protected static final String CANNOT_SPECIFY_BOTH_VALUE_AND_EXPRESSION_MESSAGE_KEY = 
			"survey.schema.attribute.attribute_default.validation.cannot_specify_both_value_and_expression";
	protected static final String FIELD_REQUIRED_MESSAGE_KEY = "global.item.validation.required_field";
	
	protected static final String VALUE_FIELD = "value";
	protected static final String EXPRESSION_FIELD = "expression";
	protected static final String CONDITION_FIELD = "condition";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateCondition(ctx);
		validateValue(ctx);
		validateExpression(ctx);
	}

	private void validateCondition(ValidationContext ctx) {
		NodeDefinition contextNode = getContextNode(ctx);
		validateBooleanExpression(ctx, contextNode, CONDITION_FIELD);
	}

	private void validateValue(ValidationContext ctx) {
		String value = getValue(ctx, VALUE_FIELD);
		String expression = getValue(ctx, EXPRESSION_FIELD);
		if ( StringUtils.isBlank(value) && StringUtils.isBlank(expression) ) {
			addInvalidMessage(ctx, VALUE_FIELD, Labels.getLabel(VALUE_OR_EXPRESSION_REQUIRED_MESSAGE_KEY));
		} else if ( StringUtils.isNotBlank(value) && StringUtils.isNotBlank(expression) ) {
			addInvalidMessage(ctx, VALUE_FIELD, Labels.getLabel(CANNOT_SPECIFY_BOTH_VALUE_AND_EXPRESSION_MESSAGE_KEY));
		} else if ( StringUtils.isNotBlank(value) ) {
			AttributeDefinition contextNode = (AttributeDefinition) getContextNode(ctx);
			try {
				contextNode.createValue(value);
			} catch ( Exception e) {
				addInvalidMessage(ctx, VALUE_FIELD, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
			}
		}
	}
	
	private void validateExpression(ValidationContext ctx) {
		String value = getValue(ctx, VALUE_FIELD);
		String expression = getValue(ctx, EXPRESSION_FIELD);
		if ( StringUtils.isBlank(value) && StringUtils.isBlank(expression) ) {
			addInvalidMessage(ctx, EXPRESSION_FIELD, Labels.getLabel(VALUE_OR_EXPRESSION_REQUIRED_MESSAGE_KEY));
		} else if ( StringUtils.isNotBlank(value) && StringUtils.isNotBlank(expression) ) {
			addInvalidMessage(ctx, EXPRESSION_FIELD, Labels.getLabel(CANNOT_SPECIFY_BOTH_VALUE_AND_EXPRESSION_MESSAGE_KEY));
		} else {
			NodeDefinition contextNode = getContextNode(ctx);
			validateValueExpression(ctx, contextNode, EXPRESSION_FIELD);
		}
	}
	
	protected NodeDefinition getContextNode(ValidationContext ctx) {
		NodeDefinition result = (NodeDefinition) ctx.getValidatorArg(PARENT_DEFINITION_ARG);
		return result;
	}

}
