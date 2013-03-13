package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.viewmodel.AttributeDefaultVM;
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
		String value = getValue(ctx, VALUE_FIELD);
		String expression = getValue(ctx, EXPRESSION_FIELD);
		if ( StringUtils.isBlank(value) ) {
			if ( StringUtils.isBlank(expression) ) {
				addInvalidMessage(ctx, VALUE_FIELD, Labels.getLabel(VALUE_OR_EXPRESSION_REQUIRED_MESSAGE_KEY));
				addInvalidMessage(ctx, EXPRESSION_FIELD, Labels.getLabel(VALUE_OR_EXPRESSION_REQUIRED_MESSAGE_KEY));
			}
		} else if ( StringUtils.isNotBlank(expression) ) {
			addInvalidMessage(ctx, VALUE_FIELD, Labels.getLabel(CANNOT_SPECIFY_BOTH_VALUE_AND_EXPRESSION_MESSAGE_KEY));
			addInvalidMessage(ctx, EXPRESSION_FIELD, Labels.getLabel(CANNOT_SPECIFY_BOTH_VALUE_AND_EXPRESSION_MESSAGE_KEY));
		}
	}
	
	protected NodeDefinition getParentDefintion(ValidationContext ctx) {
		NodeDefinition result = (NodeDefinition) ctx.getValidatorArg(PARENT_DEFINITION_ARG);
		return result;
	}

	protected AttributeDefaultVM getAttributeDefaultVM(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof AttributeDefaultVM ) {
			return (AttributeDefaultVM) vm;
		} else {
			throw new  IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
}
