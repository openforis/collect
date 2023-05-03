package org.openforis.collect.designer.form.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.commons.lang.Strings;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionType;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
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
				String[] keyMessages = validationMessages.getKeyMessages(fieldName);
				if (keyMessages != null) {
					String[] notEmptyMessages = Strings.filterNotBlank(keyMessages);
					if ( notEmptyMessages.length > 0 ) {
						validationMessagesByField.put(fieldName, Arrays.asList(notEmptyMessages));
					}
				}
			}
		}
		return validationMessagesByField;
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

	protected boolean validateBooleanExpressionField(ValidationContext ctx,
			NodeDefinition contextNode, String field) {
		return validateExpressionField(ctx, ExpressionType.BOOLEAN, field, contextNode);
	}

	protected boolean validateExpressionField(ValidationContext ctx, ExpressionType type, String field,
			NodeDefinition contextNode) {
		return validateExpressionField(ctx, type, field, contextNode.getParentDefinition(), contextNode);
	}
	
	protected boolean validateExpressionField(ValidationContext ctx, ExpressionType type, String field,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef) {
		if (contextNodeDef == null) {
			return true;
		}
		String epression = (String) getValue(ctx, field);
		return validateExpression(ctx, type, field, contextNodeDef, thisNodeDef, epression);
	}

	protected boolean validateExpression(ValidationContext ctx, ExpressionType type, String field, 
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef, String epression) {
		if ( StringUtils.isBlank(epression) ) {
			return true;
		}
		ExpressionValidator expressionValidator = getExpressionValidator(ctx);
		ExpressionValidationResult result = expressionValidator.validateExpression(type, contextNodeDef, thisNodeDef, epression);
		if ( result.isError() ) {
			addInvalidMessage(ctx, field, generateErrorMessageLabel(result, INVALID_EXPRESSION_MESSAGE_KEY));
			return false;
		}
		ExpressionValidationResult circularReferenceValidationResult = expressionValidator.validateCircularReferenceAbsence(contextNodeDef, thisNodeDef, epression);
		if (circularReferenceValidationResult.isError()) {
			addInvalidMessage(ctx, field, generateErrorMessageLabel(circularReferenceValidationResult, CIRCULAR_REFERENCE_IN_EXPRESSION_MESSAGE_KEY));
			return false;
		}
		return true;
	}

	protected boolean validateValueExpressionField(ValidationContext ctx, NodeDefinition contextDefn, String field) {
		NodeDefinition parentDefn = contextDefn.getParentDefinition();
		return validateValueExpressionField(ctx, contextDefn, parentDefn, field);
	}
	
	protected boolean validateValueExpressionField(ValidationContext ctx, NodeDefinition contextDefn, NodeDefinition parentEntityDefn, String field) {
		return validateExpressionField(ctx, ExpressionType.VALUE, field, parentEntityDefn, contextDefn);
	}
	
	protected boolean validatePathExpressionField(ValidationContext ctx, NodeDefinition contextNode, String fieldName) {
		return validateExpressionField(ctx, ExpressionType.SCHEMA_PATH, fieldName, contextNode);
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

	private String generateErrorMessageLabel(ExpressionValidationResult result, String messageKey) {
		String message = StringUtils.defaultIfBlank(result.getDetailedMessage(), result.getMessage());
		String messageLabel = getMessage(messageKey, message);
		return messageLabel;
	}
	
	protected static String getMessage(String messageKey, String... messageArgs) {
		String[] normalizedArguments = normalizeMessageArguments(messageArgs);
		return Labels.getLabel(messageKey, normalizedArguments);
	}

	protected static String[] normalizeMessageArguments(String... messages) {
		String[] result = new String[messages.length];
		for (int i = 0; i < messages.length; i++) {
			result[i] = normalizeMessageArgument(messages[i]);
		}
		return result;
	}

	protected static String normalizeMessageArgument(String message) {
		return Strings.textToHtml(message);
	}

}
