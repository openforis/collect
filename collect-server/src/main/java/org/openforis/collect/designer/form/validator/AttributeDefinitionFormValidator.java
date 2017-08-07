/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.ATTRIBUTE_DEFAULTS_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.CALCULATED_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.CHECKS_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.KEY_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.REFERENCED_ATTRIBUTE_PATH_FIELD;
import static org.openforis.collect.designer.form.NodeDefinitionFormObject.MULTIPLE_FIELD;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openforis.collect.designer.viewmodel.AttributeVM;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult.Flag;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormValidator extends NodeDefinitionFormValidator {

	protected static final String KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY = "survey.validation.attribute.key_attribute_cannot_be_multiple";
	private static final String REFERENCED_ATTRIBUTE_DELETED_MESSAGE_KEY = "survey.validation.attribute.referenced_attribute_deleted";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateChecks(ctx);
		validateAttributeDefaults(ctx);
		validateMultipleAndKey(ctx);
		validateReferencedAttribute(ctx);
	}

	private void validateMultipleAndKey(ValidationContext ctx) {
		Boolean multiple = getValue(ctx, MULTIPLE_FIELD);
		Boolean key = getValue(ctx, KEY_FIELD, false);
		if (key != null && key && multiple) {
			addInvalidMessage(ctx, KEY_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
			addInvalidMessage(ctx, MULTIPLE_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
		}
	}

	protected void validateAttributeDefaults(ValidationContext ctx) {
		boolean calculated = isCalculated(ctx);
		if (calculated) {
			AttributeVM<?> vm = getVM(ctx);
			List<AttributeDefault> attributeDefaults = vm.getAttributeDefaults();
			validateRequired(ctx, ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
		}
	}

	protected void validateChecks(ValidationContext ctx) {
		boolean calculated = isCalculated(ctx);
		if (calculated) {
			AttributeVM<?> vm = getVM(ctx);
			List<Check<?>> checks = vm.getChecks();
			if (checks != null && !checks.isEmpty()) {
				addInvalidMessage(ctx, CHECKS_FIELD,
						"survey.validation.attribute.cannot_specify_checks_for_calculated_attribute");
			}
		}
	}

	private Boolean isCalculated(ValidationContext ctx) {
		return getValueWithDefault(ctx, CALCULATED_FIELD, false);
	}

	@Override
	protected Set<String> getFieldNames(ValidationContext ctx) {
		Set<String> result = new HashSet<String>(super.getFieldNames(ctx));
		result.add(ATTRIBUTE_DEFAULTS_FIELD);
		result.add(CHECKS_FIELD);
		return result;
	}

	private void validateReferencedAttribute(ValidationContext ctx) {
		String referencedAttributePath = getValue(ctx, REFERENCED_ATTRIBUTE_PATH_FIELD, false);
		if (StringUtils.isNotBlank(referencedAttributePath)) {
			AttributeDefinition attrDef = (AttributeDefinition) getEditedNode(ctx);
			CollectSurvey survey = attrDef.getSurvey();
			AttributeDefinition referencedAttribute = (AttributeDefinition) survey.getSchema()
					.getDefinitionByPath(referencedAttributePath);
			if (referencedAttribute == null) {
				addInvalidMessage(ctx, REFERENCED_ATTRIBUTE_PATH_FIELD,
						Labels.getLabel(REFERENCED_ATTRIBUTE_DELETED_MESSAGE_KEY));
			} else {
				SurveyValidationResult validationResult = new SurveyValidator().validateReferencedKeyAttribute(attrDef,
						referencedAttribute);
				if (validationResult.getFlag() == Flag.ERROR) {
					addInvalidMessage(ctx, REFERENCED_ATTRIBUTE_PATH_FIELD,
							Labels.getLabel(validationResult.getMessageKey(), validationResult.getMessageArgs()));
				}
			}
		}
	}
}
