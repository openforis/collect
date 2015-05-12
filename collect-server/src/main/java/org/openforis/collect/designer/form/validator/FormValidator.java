package org.openforis.collect.designer.form.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.viewmodel.NodeDefinitionVM;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.impl.BinderImpl;
import org.zkoss.bind.sys.ValidationMessages;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormValidator extends BaseValidator {

	protected static final String INVALID_EXPRESSION_MESSAGE_KEY = "survey.validation.error.invalid_expression";
	protected static final String CIRCULAR_REFERENCE_IN_EXPRESSION_MESSAGE_KEY = "survey.validation.error.circular_reference";
	protected static final String RESERVED_NAME_MESSAGE_KEY = "survey.validation.error.reserved_name";
	
	protected boolean blocking;
	
	@Override
	public void validate(ValidationContext ctx) {
		if ( isEditingItem(ctx) ) {
			internalValidate(ctx);
			afterValidate(ctx);
		}
	}
	
	protected void afterValidate(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyBaseVM) {
			Map<String, List<String>> validationMessagesByField = getValidationMessagesByField(ctx);
			((SurveyBaseVM) vm).dispatchCurrentFormValidatedCommand(ctx.isValid(), blocking, validationMessagesByField);
		}
	}

	private Map<String, List<String>> getValidationMessagesByField(ValidationContext ctx) {
		Map<String, List<String>> validationMessagesByField = new LinkedHashMap<String, List<String>>();
		Set<String> fieldNames = getFieldNames(ctx);
		ValidationMessages validationMessages = ((BinderImpl) ctx.getBindContext().getBinder()).getValidationMessages();
		if ( validationMessages != null && validationMessages.getMessages() != null && validationMessages.getMessages().length > 0 ) {
			for (String fieldName : fieldNames) {
				List<String> notEmptyMessages = getNotEmptyStrings(validationMessages.getKeyMessages(fieldName));
				if ( ! notEmptyMessages.isEmpty() ) {
					validationMessagesByField.put(fieldName, notEmptyMessages);
				}
			}
		}
		return validationMessagesByField;
	}

	private List<String> getNotEmptyStrings(String[] messages) {
		List<String> notEmptyMessages = new ArrayList<String>();
		if ( messages != null ) {
			for (String message : messages) {
				if ( StringUtils.isNotEmpty(message) ) {
					notEmptyMessages.add(message);
				}
			}
		}
		return notEmptyMessages;
	}

	protected abstract void internalValidate(ValidationContext ctx);

	protected Set<String> getFieldNames(ValidationContext ctx) {
		Set<String> result = new HashSet<String>();
		result.addAll(getProperties(ctx).keySet());
		return result;
	}
	
	protected ExpressionValidator getExpressionValidator(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyBaseVM ) {
			ExpressionValidator result = ((SurveyBaseVM) vm).getExpressionValidator();
			return result;
		} else {
			return null;
		}
	}

	protected boolean validateBooleanExpression(ValidationContext ctx,
			NodeDefinition contextNode, String field) {
		String epression = (String) getValue(ctx, field);
		if ( StringUtils.isNotBlank(epression) ) {
			ExpressionValidator expressionValidator = getExpressionValidator(ctx);
			if ( ! expressionValidator.validateBooleanExpression(contextNode, epression) ) {
				addInvalidMessage(ctx, field, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
				return false;
			} else if ( ! expressionValidator.validateCircularReferenceAbsence(contextNode.getParentDefinition(), contextNode, epression)) {
				addInvalidMessage(ctx, field, Labels.getLabel(CIRCULAR_REFERENCE_IN_EXPRESSION_MESSAGE_KEY));
				return false;
			}
		}
		return true;
	}
	
	protected boolean validateValueExpression(ValidationContext ctx, NodeDefinition contextDefn, String field) {
		NodeDefinition parentDefn = contextDefn.getParentDefinition();
		return validateValueExpression(ctx, contextDefn, parentDefn, field);
	}
	
	protected boolean validateValueExpression(ValidationContext ctx, NodeDefinition contextDefn, NodeDefinition parentEntityDefn, String field) {
		String expression = getValue(ctx, field);
		if ( StringUtils.isNotBlank(expression) ) {
			ExpressionValidator expressionValidator = getExpressionValidator(ctx);
			if ( ! expressionValidator.validateValueExpression(contextDefn, parentEntityDefn, expression)) {
				addInvalidMessage(ctx, field, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
				return false;
			} else if ( ! expressionValidator.validateCircularReferenceAbsence(parentEntityDefn, contextDefn, expression)) {
				addInvalidMessage(ctx, field, Labels.getLabel(CIRCULAR_REFERENCE_IN_EXPRESSION_MESSAGE_KEY));
				return false;
			}
		}
		return true;
	}
	
	protected boolean validatePathExpression(ValidationContext ctx, NodeDefinition contextNode, String fieldName) {
		String expression = getValue(ctx, fieldName);
		NodeDefinitionVM<?> vm = (NodeDefinitionVM<?>) getVM(ctx);
		ExpressionValidator expressionValidator = vm.getExpressionValidator();
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateSchemaPathExpression(contextNode, expression)) {
			addInvalidMessage(ctx, fieldName, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
	
	protected boolean validateNameNotReserved(ValidationContext ctx, String nameField, String[] reservedNames) {
		String name = (String) getValue(ctx, nameField);
		if ( ArrayUtils.contains(reservedNames, name) ) {
			String message = Labels.getLabel(RESERVED_NAME_MESSAGE_KEY);
			addInvalidMessage(ctx, nameField, message);
			return false;
		} else {
			return true;
		}
	}

	public boolean isBlocking() {
		return blocking;
	}
	
	protected boolean isEditingItem(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyObjectBaseVM ) {
			return ((SurveyObjectBaseVM<?>) vm).isEditingItem();
		} else {
			return false;
		}
	}

}
