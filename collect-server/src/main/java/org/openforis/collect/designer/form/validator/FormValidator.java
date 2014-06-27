package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.viewmodel.NodeDefinitionVM;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Binder;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormValidator extends BaseValidator {

	protected static final String INVALID_EXPRESSION_MESSAGE_KEY = "survey.validation.error.invalid_expression";
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
			((SurveyBaseVM) vm).dispatchCurrentFormValidatedCommand(ctx.isValid(), blocking);
		}
	}

	protected abstract void internalValidate(ValidationContext ctx);

	protected Object getVM(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		Binder binder = bindContext.getBinder();
		Object vmObject = binder.getViewModel();
		if ( vmObject == null ) {
			throw new IllegalStateException("Unable to find view model instance");
		}
		return vmObject;
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
		String condition = (String) getValue(ctx, field);
		ExpressionValidator expressionValidator = getExpressionValidator(ctx);
		if ( StringUtils.isNotBlank(condition) && ! expressionValidator.validateBooleanExpression(contextNode, condition) ) {
			addInvalidMessage(ctx, field, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
	
	protected boolean validateValueExpression(ValidationContext ctx, NodeDefinition contextDefn, String field) {
		NodeDefinition parentDefn = contextDefn.getParentDefinition();
		return validateValueExpression(ctx, contextDefn, parentDefn, field);
	}
	
	protected boolean validateValueExpression(ValidationContext ctx, NodeDefinition contextDefn, NodeDefinition parentEntityDefn, String field) {
		String expression = getValue(ctx, field);
		ExpressionValidator expressionValidator = getExpressionValidator(ctx);
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateValueExpression(contextDefn, parentEntityDefn, expression)) {
			addInvalidMessage(ctx, field, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
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
