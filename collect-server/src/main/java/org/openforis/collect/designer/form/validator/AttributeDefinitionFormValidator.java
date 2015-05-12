/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.viewmodel.AttributeVM;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormValidator extends NodeDefinitionFormValidator {
	
	private static final String CALCULATED_FIELD = "calculated";

	protected static final String KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY = "survey.validation.attribute.key_attribute_cannot_be_multiple";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateChecks(ctx);
		validateAttributeDefaults(ctx);
		validateMultipleAndKey(ctx);
	}
	
	private void validateMultipleAndKey(ValidationContext ctx) {
		Boolean multiple = getValue(ctx, MULTIPLE_FIELD);
		Boolean key = getValue(ctx, AttributeDefinitionFormObject.KEY_FIELD, false);
		if ( key != null && key && multiple ) {
			addInvalidMessage(ctx, AttributeDefinitionFormObject.KEY_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
			addInvalidMessage(ctx, MULTIPLE_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
		}
	}

	protected void validateAttributeDefaults(ValidationContext ctx) {
		boolean calculated = isCalculated(ctx);
		if ( calculated ) {
			AttributeVM<?> vm = (AttributeVM<?>) getVM(ctx);
			List<AttributeDefault> attributeDefaults = vm.getAttributeDefaults();
			validateRequired(ctx, AttributeDefinitionFormObject.ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
		}
	}

	protected void validateChecks(ValidationContext ctx) {
		boolean calculated = isCalculated(ctx);
		if ( calculated ) {
			AttributeVM<?> vm = (AttributeVM<?>) getVM(ctx);
			List<Check<?>> checks = vm.getChecks();
			if ( checks != null && ! checks.isEmpty() ) {
				addInvalidMessage(ctx, AttributeDefinitionFormObject.CHECKS_FIELD, "survey.validation.attribute.cannot_specify_checks_for_calculated_attribute");
			}
		}
	}
	
	private Boolean isCalculated(ValidationContext ctx) {
		return getValueWithDefault(ctx, CALCULATED_FIELD, false);
	}
	
	@Override
	protected Set<String> getFieldNames(ValidationContext ctx) {
		Set<String> result = new HashSet<String>(super.getFieldNames(ctx));
		result.add(AttributeDefinitionFormObject.ATTRIBUTE_DEFAULTS_FIELD);
		result.add(AttributeDefinitionFormObject.CHECKS_FIELD);
		return result;
	}
}
